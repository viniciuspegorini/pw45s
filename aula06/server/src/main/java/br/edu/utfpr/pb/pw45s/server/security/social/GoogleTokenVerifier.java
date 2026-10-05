package br.edu.utfpr.pb.pw45s.server.security.social;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Component
@Slf4j
public class GoogleTokenVerifier {

    private final GoogleIdTokenVerifier verifier;

    // O Client ID é lido da propriedade google.client-id do application.yml
    public GoogleTokenVerifier(@Value("${google.client-id}") String clientId) {
        // O verificador é criado uma única vez, pois ele mantém em cache as chaves públicas do Google
        this.verifier = new GoogleIdTokenVerifier
                .Builder(new NetHttpTransport(), GsonFactory.getDefaultInstance())
                // o token deve ter sido emitido pelo Google
                .setIssuers(List.of("https://accounts.google.com", "accounts.google.com"))
                // o token deve ter sido emitido para a nossa aplicação
                .setAudience(Collections.singletonList(clientId))
                .build();
    }

    /**
     * Valida a assinatura, o emissor, a audiência e a expiração do ID Token.
     * @param idTokenString ID Token recebido do front-end
     * @return o payload do token (email, name, picture, ...)
     * @throws InvalidGoogleTokenException caso o token seja inválido
     */
    public GoogleIdToken.Payload verify(String idTokenString) {
        GoogleIdToken idToken = null;
        try {
            idToken = verifier.verify(idTokenString);
        } catch (Exception e) {
            log.error("Erro ao validar o ID Token do Google: {}", e.getMessage());
        }
        if (idToken == null) {
            throw new InvalidGoogleTokenException();
        }
        return idToken.getPayload();
    }

    public static class InvalidGoogleTokenException extends RuntimeException {
        public InvalidGoogleTokenException() {
            super("Google ID Token inválido.");
        }
    }
}
