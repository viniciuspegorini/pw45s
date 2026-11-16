package br.edu.utfpr.pb.pw45s.server.security.social;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken.Payload;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.Collections;

@Component
@Slf4j
public class GoogleTokenVerifier {

    private static final HttpTransport transport = new NetHttpTransport();
    private static final JsonFactory jsonFactory = GsonFactory.getDefaultInstance();

    // ID do cliente OAuth do Google, lido da variável de ambiente GOOGLE_CLIENT_ID (application.yaml)
    private final String clientId;

    public GoogleTokenVerifier(@Value("${google.client-id}") String clientId) {
        this.clientId = clientId;
    }

    public Payload verify(String idTokenString)
            throws GeneralSecurityException, IOException {
        final GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier
                                    .Builder(transport, jsonFactory)
                                        .setIssuers(Arrays.asList("https://accounts.google.com",
                                                "accounts.google.com"))
                                        .setAudience(Collections.singletonList(clientId))
                                        .build();
        log.info("Validating token: {}", idTokenString);
        GoogleIdToken idToken = null;
        try {
          idToken = verifier.verify(idTokenString);
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        if (idToken == null) {
            throw new RuntimeException("Google idToken is invalid.");
        }
        return idToken.getPayload();
    }
}
