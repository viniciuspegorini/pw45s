package br.edu.utfpr.pb.pw45s.server.security.social;

import br.edu.utfpr.pb.pw45s.server.model.AuthProvider;
import br.edu.utfpr.pb.pw45s.server.model.User;
import br.edu.utfpr.pb.pw45s.server.repository.UserRepository;
import br.edu.utfpr.pb.pw45s.server.security.SecurityConstants;
import br.edu.utfpr.pb.pw45s.server.security.dto.AuthenticationResponse;
import br.edu.utfpr.pb.pw45s.server.security.dto.UserResponseDTO;
import br.edu.utfpr.pb.pw45s.server.service.UserService;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

@Tag(name = "Authentication")
@RestController
@RequestMapping("auth-social")
@Slf4j
public class SocialAuthController {

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final String LOWER = "abcdefghijklmnopqrstuvwxyz";
    private static final String UPPER = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String DIGITS = "0123456789";
    private static final int PASSWORD_LENGTH = 32;

    private final GoogleTokenVerifier googleTokenVerifier;
    private final UserService userService;
    private final UserRepository userRepository;

    public SocialAuthController(GoogleTokenVerifier googleTokenVerifier,
                                UserService userService,
                                UserRepository userRepository) {
        this.googleTokenVerifier = googleTokenVerifier;
        this.userService = userService;
        this.userRepository = userRepository;
    }

    @Operation(
            summary = "Login com o Google",
            description = "Recebe o ID Token do Google no header Auth-Id-Token (Bearer <idToken>) e retorna o token JWT da API.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Login efetuado com sucesso"),
                    @ApiResponse(responseCode = "401", description = "ID Token ausente ou inválido")
            }
    )
    @PostMapping
    public ResponseEntity<AuthenticationResponse> auth(
            @RequestHeader(value = "Auth-Id-Token", required = false) String idToken) {
        if (idToken == null || idToken.isBlank()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        try {
            // 1. Valida o ID Token recebido do Google
            GoogleIdToken.Payload payload = googleTokenVerifier.verify(
                    idToken.replace(SecurityConstants.TOKEN_PREFIX, ""));

            // 2. Só aceita contas com o e-mail verificado pelo Google
            if (!Boolean.TRUE.equals(payload.getEmailVerified())) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            // 3. O e-mail da conta Google é utilizado como username
            String username = payload.getEmail();
            User user = userRepository.findUserByUsername(username);

            // 4. Primeiro acesso: o usuário é cadastrado com a ROLE_USER (definida no UserService)
            if (user == null) {
                user = new User();
                user.setUsername(username);
                user.setDisplayName(getDisplayName((String) payload.get("name"), username));
                // O usuário não conhece essa senha: ele sempre se autenticará pelo Google.
                // Além disso, o JWTAuthenticationFilter bloqueia o login por senha para provider != local.
                user.setPassword(generateRandomPassword());
                user.setProvider(AuthProvider.google);
                userService.save(user);
            }

            // 5. Gera o JWT da API, da mesma forma que o JWTAuthenticationFilter
            String token = JWT.create()
                    .withSubject(username)
                    .withExpiresAt(new Date(System.currentTimeMillis() + SecurityConstants.EXPIRATION_TIME))
                    .sign(Algorithm.HMAC512(SecurityConstants.SECRET));

            return ResponseEntity.ok(new AuthenticationResponse(token, new UserResponseDTO(user)));
        } catch (GoogleTokenVerifier.InvalidGoogleTokenException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    /**
     * Gera uma senha aleatória de 32 caracteres para os usuários cadastrados via rede social.
     * A senha respeita a mesma regra da entidade User (letra minúscula, letra maiúscula e número)
     * e é gerada com SecureRandom, um gerador de números aleatórios adequado para uso criptográfico.
     */
    private String generateRandomPassword() {
        List<Character> password = new ArrayList<>();
        // garante ao menos um caractere de cada grupo exigido
        password.add(randomChar(LOWER));
        password.add(randomChar(UPPER));
        password.add(randomChar(DIGITS));
        // completa o restante da senha com caracteres de qualquer grupo
        while (password.size() < PASSWORD_LENGTH) {
            password.add(randomChar(LOWER + UPPER + DIGITS));
        }
        // embaralha para que os caracteres obrigatórios não fiquem sempre no início
        Collections.shuffle(password, RANDOM);

        StringBuilder sb = new StringBuilder();
        password.forEach(sb::append);
        return sb.toString();
    }

    private char randomChar(String chars) {
        return chars.charAt(RANDOM.nextInt(chars.length()));
    }

    // O displayName deve ter entre 4 e 50 caracteres (validação da entidade User)
    private String getDisplayName(String name, String username) {
        String displayName = (name == null || name.isBlank() || name.length() < 4) ? username : name;
        return displayName.length() > 50 ? displayName.substring(0, 50) : displayName;
    }
}
