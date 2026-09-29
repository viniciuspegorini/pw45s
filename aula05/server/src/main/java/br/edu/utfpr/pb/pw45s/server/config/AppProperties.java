package br.edu.utfpr.pb.pw45s.server.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.net.URI;
import java.util.List;

/**
 * Propriedades da aplicação definidas no application.yml com o prefixo "app.oauth2".
 *
 * @param authorizedRedirectUris endereços do front-end para os quais a API pode redirecionar
 *                               o usuário (com o token JWT) após a autenticação com a rede social
 * @param cookieSecret           chave utilizada para assinar o cookie da requisição de autorização OAuth2
 */
@ConfigurationProperties(prefix = "app.oauth2")
public record AppProperties(List<String> authorizedRedirectUris, String cookieSecret) {

    /**
     * Verifica se o endereço informado pelo front-end está na lista de endereços autorizados.
     * Sem essa verificação, qualquer site poderia iniciar a autenticação informando o seu próprio
     * endereço no parâmetro redirect_uri e receber o token JWT do usuário.
     */
    public boolean isAuthorizedRedirectUri(String uri) {
        try {
            URI clientRedirectUri = URI.create(uri);
            return authorizedRedirectUris.stream()
                    .map(URI::create)
                    .anyMatch(authorizedUri ->
                            authorizedUri.getScheme().equalsIgnoreCase(String.valueOf(clientRedirectUri.getScheme()))
                            && authorizedUri.getHost().equalsIgnoreCase(String.valueOf(clientRedirectUri.getHost()))
                            && authorizedUri.getPort() == clientRedirectUri.getPort()
                            && authorizedUri.getPath().equals(clientRedirectUri.getPath()));
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    // Endereço utilizado quando o front-end não informar o redirect_uri
    public String defaultRedirectUri() {
        return authorizedRedirectUris.getFirst();
    }
}
