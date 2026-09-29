package br.edu.utfpr.pb.pw45s.server.security.oauth2;

import br.edu.utfpr.pb.pw45s.server.config.AppProperties;
import br.edu.utfpr.pb.pw45s.server.utils.CookieUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.web.AuthorizationRequestRepository;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.util.Base64;

/**
 * Como a API é stateless (não utiliza sessão), a requisição de autorização OAuth2 (que contém o
 * parâmetro "state", utilizado para proteger o fluxo contra CSRF) é armazenada em um cookie entre
 * o redirecionamento para o Google e o retorno do Google para a API.
 * <p>
 * O conteúdo do cookie é assinado com HMAC-SHA256: um cookie alterado no navegador é descartado
 * antes de ser desserializado.
 */
@Slf4j
@Component
public class HttpCookieOAuth2AuthorizationRequestRepository
        implements AuthorizationRequestRepository<OAuth2AuthorizationRequest> {

    public static final String OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME = "oauth2_auth_request";
    public static final String REDIRECT_URI_PARAM_COOKIE_NAME = "redirect_uri";
    private static final int COOKIE_EXPIRE_SECONDS = 180;
    private static final String HMAC_ALGORITHM = "HmacSHA256";

    private final byte[] secret;

    public HttpCookieOAuth2AuthorizationRequestRepository(AppProperties appProperties) {
        this.secret = appProperties.cookieSecret().getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public OAuth2AuthorizationRequest loadAuthorizationRequest(HttpServletRequest request) {
        return CookieUtils.getCookie(request, OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME)
                .map(cookie -> deserialize(cookie.getValue()))
                .orElse(null);
    }

    @Override
    public void saveAuthorizationRequest(OAuth2AuthorizationRequest authorizationRequest,
                                         HttpServletRequest request, HttpServletResponse response) {
        if (authorizationRequest == null) {
            removeAuthorizationRequestCookies(request, response);
            return;
        }

        CookieUtils.addCookie(response, OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME,
                serialize(authorizationRequest), COOKIE_EXPIRE_SECONDS);
        // Endereço do front-end para o qual o usuário será redirecionado após a autenticação
        // ex.: /oauth2/authorize/google?redirect_uri=http://localhost:5173/login
        String redirectUriAfterLogin = request.getParameter(REDIRECT_URI_PARAM_COOKIE_NAME);
        if (StringUtils.hasText(redirectUriAfterLogin)) {
            CookieUtils.addCookie(response, REDIRECT_URI_PARAM_COOKIE_NAME,
                    redirectUriAfterLogin, COOKIE_EXPIRE_SECONDS);
        }
    }

    @Override
    public OAuth2AuthorizationRequest removeAuthorizationRequest(HttpServletRequest request,
                                                                 HttpServletResponse response) {
        return this.loadAuthorizationRequest(request);
    }

    public void removeAuthorizationRequestCookies(HttpServletRequest request, HttpServletResponse response) {
        CookieUtils.deleteCookie(request, response, OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME);
        CookieUtils.deleteCookie(request, response, REDIRECT_URI_PARAM_COOKIE_NAME);
    }

    // Serializa o objeto e adiciona a assinatura: <conteúdo em base64>.<assinatura em base64>
    private String serialize(OAuth2AuthorizationRequest authorizationRequest) {
        try (ByteArrayOutputStream bytes = new ByteArrayOutputStream();
             ObjectOutputStream out = new ObjectOutputStream(bytes)) {
            out.writeObject(authorizationRequest);
            out.flush();
            String payload = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes.toByteArray());
            return payload + "." + sign(payload);
        } catch (IOException | GeneralSecurityException e) {
            throw new IllegalStateException("Erro ao serializar a requisição de autorização OAuth2.", e);
        }
    }

    // Verifica a assinatura ANTES de desserializar o conteúdo do cookie
    private OAuth2AuthorizationRequest deserialize(String value) {
        try {
            int separator = value.lastIndexOf('.');
            if (separator <= 0) {
                return null;
            }
            String payload = value.substring(0, separator);
            byte[] signature = value.substring(separator + 1).getBytes(StandardCharsets.UTF_8);
            byte[] expected = sign(payload).getBytes(StandardCharsets.UTF_8);
            if (!MessageDigest.isEqual(expected, signature)) {
                log.warn("Cookie da requisição de autorização OAuth2 com assinatura inválida.");
                return null;
            }
            try (ObjectInputStream in = new ObjectInputStream(
                    new ByteArrayInputStream(Base64.getUrlDecoder().decode(payload)))) {
                return (OAuth2AuthorizationRequest) in.readObject();
            }
        } catch (IOException | ClassNotFoundException | ClassCastException
                 | IllegalArgumentException | GeneralSecurityException e) {
            log.warn("Não foi possível ler o cookie da requisição de autorização OAuth2: {}", e.getMessage());
            return null;
        }
    }

    private String sign(String payload) throws GeneralSecurityException {
        Mac mac = Mac.getInstance(HMAC_ALGORITHM);
        mac.init(new SecretKeySpec(secret, HMAC_ALGORITHM));
        return Base64.getUrlEncoder().withoutPadding()
                .encodeToString(mac.doFinal(payload.getBytes(StandardCharsets.UTF_8)));
    }
}
