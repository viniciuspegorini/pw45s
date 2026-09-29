package br.edu.utfpr.pb.pw45s.server.service;

import br.edu.utfpr.pb.pw45s.server.model.AuthProvider;
import br.edu.utfpr.pb.pw45s.server.model.User;
import br.edu.utfpr.pb.pw45s.server.repository.UserRepository;
import br.edu.utfpr.pb.pw45s.server.utils.PasswordGenerator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * O cadastro de usuários via rede social é executado pelo CustomOAuth2UserService dentro de um filtro
 * do Spring Security, ou seja, fora de uma requisição do Spring MVC e sem o "Open Session in View".
 * Este teste (propositalmente sem @Transactional) reproduz essa situação.
 */
@SpringBootTest
class UserServiceTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Test
    void save_outsideOfTransaction_registersSocialUserWithRoleUser() {
        User user = new User();
        user.setUsername("usuario.google@gmail.com");
        user.setDisplayName("Usuário Google");
        user.setPassword(PasswordGenerator.generate());
        user.setProvider(AuthProvider.google);

        userService.save(user);

        User saved = userRepository.findUserByUsername("usuario.google@gmail.com");
        assertThat(saved).isNotNull();
        assertThat(saved.getProvider()).isEqualTo(AuthProvider.google);
        assertThat(saved.getAuthorities())
                .extracting(authority -> authority.getAuthority())
                .containsExactly("ROLE_USER");
    }
}
