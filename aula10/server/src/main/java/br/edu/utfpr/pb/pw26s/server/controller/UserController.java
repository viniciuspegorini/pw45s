package br.edu.utfpr.pb.pw26s.server.controller;

import br.edu.utfpr.pb.pw26s.server.model.User;
import br.edu.utfpr.pb.pw26s.server.service.UserService;
import br.edu.utfpr.pb.pw26s.server.utils.GenericResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("users")
@Slf4j
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    public GenericResponse createUser(@RequestBody @Valid User user) {
        log.info("Creating new user with username: {}", user.getUsername());
        userService.save(user);
        log.info("New user created with id: {}", user.getId());
        return new GenericResponse("Registro salvo.");
    }

    @PatchMapping
    public GenericResponse createUserPatch(@RequestBody @Valid User user) {
        userService.save(user);
        return new GenericResponse("Registro salvo");
    }

    @GetMapping
    public String getString() {
        return "O usuário está autenticado!";
    }

}
