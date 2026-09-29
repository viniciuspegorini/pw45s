package br.edu.utfpr.pb.pw45s.server.security.oauth2.user;

import lombok.Getter;

import java.util.Map;

/**
 * Cada provedor OAuth2 (Google, GitHub, Facebook, ...) retorna os dados do usuário com nomes de
 * atributos diferentes. Esta classe define os dados que a aplicação precisa, e cada provedor
 * possui uma implementação que sabe extraí-los dos seus atributos.
 */
@Getter
public abstract class OAuth2UserInfo {
    protected Map<String, Object> attributes;

    protected OAuth2UserInfo(Map<String, Object> attributes) {
        this.attributes = attributes;
    }

    public abstract String getId();

    public abstract String getName();

    public abstract String getEmail();

    public abstract boolean isEmailVerified();

    public abstract String getImageUrl();
}
