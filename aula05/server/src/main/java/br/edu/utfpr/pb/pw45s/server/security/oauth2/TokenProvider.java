package br.edu.utfpr.pb.pw45s.server.security.oauth2;

import br.edu.utfpr.pb.pw45s.server.security.SecurityConstants;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.Date;

/**
 * Gera o token JWT da API para o usuário autenticado com a rede social.
 * O token é gerado com os mesmos SECRET, EXPIRATION_TIME e algoritmo do JWTAuthenticationFilter,
 * assim ele é validado pelo JWTAuthorizationFilter sem nenhuma alteração.
 */
@Service
public class TokenProvider {

    public String createToken(Authentication authentication) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();

        return JWT.create()
                // o subject do token é o username do usuário (o e-mail da conta Google)
                .withSubject(userPrincipal.getUsername())
                .withExpiresAt(new Date(System.currentTimeMillis() + SecurityConstants.EXPIRATION_TIME))
                .sign(Algorithm.HMAC512(SecurityConstants.SECRET));
    }
}
