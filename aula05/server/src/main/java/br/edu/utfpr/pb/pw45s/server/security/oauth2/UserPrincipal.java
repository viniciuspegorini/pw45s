package br.edu.utfpr.pb.pw45s.server.security.oauth2;

import br.edu.utfpr.pb.pw45s.server.model.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;
import java.util.Map;

/**
 * Representa o usuário autenticado por meio de uma rede social.
 * Une as informações do usuário da aplicação (username e permissões, armazenados no banco de dados)
 * com os atributos retornados pelo provedor OAuth2 (Google).
 */
public class UserPrincipal implements OAuth2User {

    private final Long id;
    private final String username;
    private final Collection<? extends GrantedAuthority> authorities;
    private final Map<String, Object> attributes;

    private UserPrincipal(Long id, String username,
                          Collection<? extends GrantedAuthority> authorities,
                          Map<String, Object> attributes) {
        this.id = id;
        this.username = username;
        this.authorities = authorities;
        this.attributes = attributes;
    }

    public static UserPrincipal create(User user, Map<String, Object> attributes) {
        return new UserPrincipal(user.getId(), user.getUsername(), user.getAuthorities(), attributes);
    }

    public Long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    // As permissões são as do usuário cadastrado no banco (ROLE_USER, ROLE_ADMIN, ...)
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    @Override
    public String getName() {
        return username;
    }
}
