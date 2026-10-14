package br.edu.utfpr.pb.pw45s.server.controller;

import br.edu.utfpr.pb.pw45s.server.dto.UserDTO;
import br.edu.utfpr.pb.pw45s.server.mapper.UserMapper;
import br.edu.utfpr.pb.pw45s.server.model.User;
import br.edu.utfpr.pb.pw45s.server.service.AuthService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
@RequestMapping("auth")
public class AuthUserController {

    private final AuthService authService;
    private final UserMapper userMapper;

    public AuthUserController(AuthService authService, UserMapper userMapper) {
        this.authService = authService;
        this.userMapper = userMapper;
    }

    @GetMapping("user-info")
    public UserDTO getUserInfo(Principal principal) {
        String username = principal.getName();
        User user = (User) authService.loadUserByUsername(username);
        return userMapper.toDto(user);
    }
}

