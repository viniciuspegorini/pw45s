package br.edu.utfpr.pb.pw45s.server.service;

import br.edu.utfpr.pb.pw45s.server.model.AuthProvider;
import br.edu.utfpr.pb.pw45s.server.model.Authority;
import br.edu.utfpr.pb.pw45s.server.model.User;
import br.edu.utfpr.pb.pw45s.server.repository.AuthorityRepository;
import br.edu.utfpr.pb.pw45s.server.repository.UserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthorityRepository authorityRepository;

    public UserService(UserRepository userRepository, AuthorityRepository authorityRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = new BCryptPasswordEncoder();
        this.authorityRepository = authorityRepository;
    }

    // @Transactional: a busca da permissão e o cadastro do usuário ocorrem na mesma transação,
    // evitando o erro "Detached entity passed to persist" ao salvar fora de uma requisição do Spring MVC
    @Transactional
    public void save(User user) {
        user.setPassword( passwordEncoder.encode(user.getPassword()) );

        // Usuários cadastrados pelo formulário (POST /users) não informam o provider
        if (user.getProvider() == null) {
            user.setProvider(AuthProvider.local);
        }

        Set<Authority> authorities = new HashSet<>();
        authorities.add(authorityRepository.findByAuthority("ROLE_USER"));
        user.setUserAuthorities(authorities);

        this.userRepository.save(user);
    }

}
