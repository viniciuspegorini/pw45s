package br.edu.utfpr.pb.pw45s.server.service;

import br.edu.utfpr.pb.pw45s.server.model.Authority;
import br.edu.utfpr.pb.pw45s.server.model.User;
import br.edu.utfpr.pb.pw45s.server.repository.AuthorityRepository;
import br.edu.utfpr.pb.pw45s.server.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

@Service
@Slf4j
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthorityRepository authorityRepository;

    public UserService(UserRepository userRepository, AuthorityRepository authorityRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = new BCryptPasswordEncoder();
        this.authorityRepository = authorityRepository;
    }

    public void save(User user) {

        user.setPassword( passwordEncoder.encode(user.getPassword()) );
        log.info("Encoding the password.");
        Set<Authority> authorities = new HashSet<>();
        authorities.add(authorityRepository.findByAuthority("ROLE_USER"));
        user.setUserAuthorities(authorities);
        log.info("Authority  added.");
        this.userRepository.save(user);
    }

}
