package br.edu.utfpr.pb.pw45s.server.controller;

import br.edu.utfpr.pb.pw45s.server.model.User;
import br.edu.utfpr.pb.pw45s.server.security.dto.UserResponseDTO;
import br.edu.utfpr.pb.pw45s.server.service.AuthService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
@RequestMapping("auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * Retorna os dados do usuário autenticado (displayName, username e authorities), no mesmo formato
     * do objeto "user" retornado pelo login com usuário e senha.
     * Utilizado pelo front-end após a autenticação com a rede social, quando recebe apenas o token.
     */
    @GetMapping("user-info")
    public UserResponseDTO getUserInfo(Principal principal) {
        User user = (User) authService.loadUserByUsername(principal.getName());
        return new UserResponseDTO(user);
    }
}
