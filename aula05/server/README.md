# 🔐 Autenticação com redes sociais no servidor - Spring Security OAuth2 Client (back-end)

Na **aula04** o front-end autenticava o usuário no Google e enviava o **ID Token** para a API validar. Nesta aula **a própria API realiza a autenticação com o Google**, atuando como um **cliente OAuth 2.0**. Para isso é utilizado o módulo **OAuth2 Client** do Spring Security, que implementa o fluxo **Authorization Code**:

1. O front-end redireciona o navegador para a API em `/oauth2/authorize/google`;
2. A API redireciona o navegador para a página de *login* do Google;
3. Após o usuário se autenticar, o Google redireciona o navegador de volta para a API em `/oauth2/callback/google`, com um **code** (código de autorização) de uso único;
4. A API troca o **code** por um **access token** diretamente com o Google (requisição servidor → servidor, utilizando o **client secret**) e, com esse *access token*, busca os dados do usuário (e-mail, nome, foto);
5. A API cadastra (primeiro acesso) ou atualiza o usuário no banco de dados, gera o **JWT da própria API** e redireciona o navegador de volta para o front-end, enviando o JWT na URL.

Os passos 2 a 4 são realizados pelo Spring Security. O nosso código é responsável pelos passos 1 e 5: configurar os *endpoints*, cadastrar o usuário, gerar o JWT e decidir para onde o usuário é redirecionado.

A partir daí, todas as requisições seguem exatamente o mesmo caminho de antes: o **JWTAuthorizationFilter** valida o JWT enviado no *header* `Authorization` e carrega as *authorities* do usuário.

> A criação das credenciais no Google Cloud Console e a definição da variável de ambiente `GOOGLE_CLIENT_SECRET` estão descritas no [README.md](../README.md) da pasta **aula05**.

## 📋 Resumo das alterações

O projeto parte do código do servidor da **aula04**, removendo a validação do ID Token (`security/social`, dependência `google-api-client` e propriedade `google.client-id`). Continuam iguais à aula04: o *enum* **AuthProvider**, o atributo `provider` da classe **User** e dos *scripts* SQL, a lógica do **UserService** (que define o `provider` `local` e a permissão `ROLE_USER`) e o bloqueio do *login* por senha para usuários de redes sociais no **JWTAuthenticationFilter**. As alterações realizadas foram:

- **`pom.xml`**: adicionada a dependência `spring-boot-starter-security-oauth2-client`.
- **`application.yml`**: registro do Google como provedor OAuth2 (`spring.security.oauth2.client`), importação do arquivo `.env` e propriedades da aplicação (`app.oauth2`).
- **`.env` / `.env.example`**: credenciais do Google e chave de assinatura do *cookie* (o `.env` não é versionado).
- **`config/AppProperties.java`** e **`config/WebConfig.java`**: leitura das propriedades `app.oauth2` e validação dos endereços de redirecionamento.
- **`service/UserService.java`**: o método `save` passou a ser `@Transactional` (ver seção 5.1).
- **`test/.../service/UserServiceTest.java`**: teste do cadastro de usuário fora de uma requisição do Spring MVC.
- **`utils/PasswordGenerator.java`**: geração de senha aleatória para os usuários cadastrados via rede social.
- **`utils/CookieUtils.java`**: métodos auxiliares para leitura, criação e remoção de *cookies*.
- **`security/oauth2/HttpCookieOAuth2AuthorizationRequestRepository.java`**: armazena a requisição de autorização OAuth2 em um *cookie* assinado.
- **`security/oauth2/user/*`**: classes que extraem os dados do usuário retornados por cada provedor.
- **`security/oauth2/CustomOAuth2UserService.java`**: cadastra ou atualiza o usuário no banco de dados.
- **`security/oauth2/UserPrincipal.java`**: representa o usuário autenticado via OAuth2.
- **`security/oauth2/TokenProvider.java`**: gera o JWT da API.
- **`security/oauth2/OAuth2AuthenticationSuccessHandler.java`** e **`OAuth2AuthenticationFailureHandler.java`**: redirecionam o usuário para o front-end com o JWT ou com a mensagem de erro.
- **`error/OAuth2AuthenticationProcessingException.java`**: exceção lançada quando a autenticação não pode ser concluída.
- **`security/WebSecurity.java`**: configuração do `oauth2Login()`.
- **`controller/AuthController.java`**: o *endpoint* `/auth/user-info` passou a retornar o usuário com as suas permissões.

---

## 1. 📦 Dependência

O suporte ao fluxo OAuth 2.0 / OpenID Connect é adicionado com o *starter* do Spring Boot. No arquivo **`pom.xml`**:

```xml
<!-- Autenticação com redes sociais (Google) - OAuth2 Client do Spring Security -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-security-oauth2-client</artifactId>
</dependency>
```

> No Spring Boot 4 os *starters* de segurança foram renomeados. Em projetos com Spring Boot 3 o equivalente é `spring-boot-starter-oauth2-client`.

---

## 2. ⚙️ Registrando o Google como provedor OAuth2

No **`application.yml`** é feito o registro (*client registration*) do Google. Como o Google é um provedor conhecido pelo Spring Security, não é necessário informar as URLs de autorização, de *token* e de dados do usuário: basta informar as credenciais e os escopos:

```yaml
spring:
  security:
    oauth2:
      client:
        registration:
          google:
            # Credenciais criadas no Google Cloud Console. O client-secret é um SEGREDO:
            # informe-o na variável de ambiente GOOGLE_CLIENT_SECRET, nunca no código-fonte.
            client-id: ${GOOGLE_CLIENT_ID:<SEU_CLIENT_ID>.apps.googleusercontent.com}
            client-secret: ${GOOGLE_CLIENT_SECRET:}
            # URL da API para a qual o Google redireciona após a autenticação
            # (deve estar cadastrada em "URIs de redirecionamento autorizados" no Google Cloud Console)
            redirect-uri: "{baseUrl}/oauth2/callback/{registrationId}"
            scope:
              - email
              - profile
```

- **`google`**: é o *registrationId*, utilizado nas URLs (`/oauth2/authorize/google`, `/oauth2/callback/google`) e comparado com o *enum* **AuthProvider**.
- **`client-secret`**: utilizado pela API para trocar o *code* pelo *access token*. Por isso ele **nunca** pode estar no front-end ou no código-fonte, e é lido da variável `GOOGLE_CLIENT_SECRET` (ver seção 2.1).
- **`redirect-uri`**: `{baseUrl}` e `{registrationId}` são substituídos pelo Spring, resultando em `http://localhost:8080/oauth2/callback/google`.
- **`scope`**: permissões solicitadas ao usuário; `email` e `profile` dão acesso ao e-mail, nome e foto.

### 2.1 Arquivo .env

A sintaxe `${GOOGLE_CLIENT_SECRET:}` busca o valor em uma variável; após os `:` fica o valor padrão (vazio, no caso do *secret*). Para não precisar definir variáveis de ambiente no sistema operacional ou na IDE, os valores ficam no arquivo **`.env`**, na raiz do projeto `server`:

```properties
# Credenciais OAuth 2.0 criadas no Google Cloud Console (APIs e serviços > Credenciais)
GOOGLE_CLIENT_ID=<SEU_CLIENT_ID>.apps.googleusercontent.com
GOOGLE_CLIENT_SECRET=<informe-aqui-a-chave-secreta-do-cliente>

# Chave utilizada para assinar o cookie da requisição de autorização OAuth2 (texto aleatório e longo)
OAUTH2_COOKIE_SECRET=<informe-aqui-um-texto-aleatorio>
```

O Spring Boot não lê arquivos `.env` automaticamente. Por isso o arquivo é importado no **`application.yml`** com a propriedade `spring.config.import`:

```yaml
spring:
  # Carrega as variáveis do arquivo .env da pasta do projeto (se existir), no formato CHAVE=valor.
  # Os valores podem ser utilizados como ${CHAVE}. Variáveis de ambiente do sistema têm precedência.
  config:
    import: optional:file:.env[.properties]
```

- **`optional:`**: a aplicação inicia normalmente mesmo que o arquivo não exista (por exemplo, em produção, onde os valores são informados como variáveis de ambiente).
- **`file:.env`**: o caminho é relativo à pasta em que a aplicação é executada (a pasta `server`, ao utilizar `./mvnw spring-boot:run`).
- **`[.properties]`**: como o nome do arquivo não possui extensão, é necessário informar ao Spring o formato do conteúdo (`CHAVE=valor`, o mesmo de um arquivo `.properties`).

O arquivo **`.env`** está no `.gitignore` e **não deve ser versionado**, pois contém o *client secret*. O projeto possui o arquivo **`.env.example`**, com a mesma estrutura e sem os valores secretos, que serve de modelo: basta copiá-lo para `.env` e preencher os valores.

```bash
cp .env.example .env
```

### 2.2 Propriedades da aplicação

Também foram criadas propriedades da própria aplicação, com o prefixo `app.oauth2`:

```yaml
app:
  oauth2:
    # Endereços do front-end para os quais a API pode redirecionar o usuário após a autenticação
    authorized-redirect-uris:
      - http://localhost:5173/login
    # Chave utilizada para assinar o cookie que armazena a requisição de autorização OAuth2
    cookie-secret: ${OAUTH2_COOKIE_SECRET:utfpr-pw45s-cookie-secret-altere-em-producao}
```

Essas propriedades são lidas pelo *record* **AppProperties** (habilitado com `@EnableConfigurationProperties(AppProperties.class)` na classe **WebConfig**), que também é responsável por validar o endereço de redirecionamento informado pelo front-end:

```java
@ConfigurationProperties(prefix = "app.oauth2")
public record AppProperties(List<String> authorizedRedirectUris, String cookieSecret) {

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
```

> ⚠️ Essa validação é essencial: como o JWT é enviado na URL de retorno, sem ela qualquer site poderia iniciar a autenticação informando `redirect_uri=https://site-malicioso.com` e receber o *token* do usuário.

---

## 3. 🍪 Armazenando a requisição de autorização em um cookie

Entre o redirecionamento para o Google (passo 2) e o retorno do Google (passo 3), o Spring Security precisa guardar a **requisição de autorização**, que contém o parâmetro `state` (protege o fluxo contra ataques CSRF) e o `code_verifier` do PKCE. Por padrão ela é armazenada na sessão HTTP, mas a nossa API é *stateless* (`SessionCreationPolicy.STATELESS`). Por isso foi criada a classe **HttpCookieOAuth2AuthorizationRequestRepository**, que armazena a requisição em um *cookie*:

```java
@Slf4j
@Component
public class HttpCookieOAuth2AuthorizationRequestRepository
        implements AuthorizationRequestRepository<OAuth2AuthorizationRequest> {

    public static final String OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME = "oauth2_auth_request";
    public static final String REDIRECT_URI_PARAM_COOKIE_NAME = "redirect_uri";
    private static final int COOKIE_EXPIRE_SECONDS = 180;
    private static final String HMAC_ALGORITHM = "HmacSHA256";

    private final byte[] secret;

    public HttpCookieOAuth2AuthorizationRequestRepository(AppProperties appProperties) {
        this.secret = appProperties.cookieSecret().getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public OAuth2AuthorizationRequest loadAuthorizationRequest(HttpServletRequest request) {
        return CookieUtils.getCookie(request, OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME)
                .map(cookie -> deserialize(cookie.getValue()))
                .orElse(null);
    }

    @Override
    public void saveAuthorizationRequest(OAuth2AuthorizationRequest authorizationRequest,
                                         HttpServletRequest request, HttpServletResponse response) {
        if (authorizationRequest == null) {
            removeAuthorizationRequestCookies(request, response);
            return;
        }

        CookieUtils.addCookie(response, OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME,
                serialize(authorizationRequest), COOKIE_EXPIRE_SECONDS);
        // Endereço do front-end para o qual o usuário será redirecionado após a autenticação
        // ex.: /oauth2/authorize/google?redirect_uri=http://localhost:5173/login
        String redirectUriAfterLogin = request.getParameter(REDIRECT_URI_PARAM_COOKIE_NAME);
        if (StringUtils.hasText(redirectUriAfterLogin)) {
            CookieUtils.addCookie(response, REDIRECT_URI_PARAM_COOKIE_NAME,
                    redirectUriAfterLogin, COOKIE_EXPIRE_SECONDS);
        }
    }

    // ... removeAuthorizationRequest, removeAuthorizationRequestCookies, serialize, deserialize e sign
}
```

Além da requisição de autorização, também é armazenado em um *cookie* o parâmetro `redirect_uri` enviado pelo front-end, que indica para qual endereço o usuário deve voltar ao final do fluxo. Os *cookies* são `HttpOnly` (não podem ser lidos por JavaScript) e expiram em 3 minutos.

### 3.1 Assinando o cookie

O objeto `OAuth2AuthorizationRequest` é serializado (serialização Java) e codificado em Base64 para ser armazenado no *cookie*. Porém, um *cookie* fica no navegador e **pode ser alterado pelo usuário**, e desserializar dados manipulados por terceiros é uma vulnerabilidade grave (*insecure deserialization*), que pode permitir a execução de código no servidor. Por isso o conteúdo do *cookie* é **assinado com HMAC-SHA256**, e a assinatura é verificada **antes** da desserialização:

```java
// Serializa o objeto e adiciona a assinatura: <conteúdo em base64>.<assinatura em base64>
private String serialize(OAuth2AuthorizationRequest authorizationRequest) {
    try (ByteArrayOutputStream bytes = new ByteArrayOutputStream();
         ObjectOutputStream out = new ObjectOutputStream(bytes)) {
        out.writeObject(authorizationRequest);
        out.flush();
        String payload = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes.toByteArray());
        return payload + "." + sign(payload);
    } catch (IOException | GeneralSecurityException e) {
        throw new IllegalStateException("Erro ao serializar a requisição de autorização OAuth2.", e);
    }
}

// Verifica a assinatura ANTES de desserializar o conteúdo do cookie
private OAuth2AuthorizationRequest deserialize(String value) {
    try {
        int separator = value.lastIndexOf('.');
        if (separator <= 0) {
            return null;
        }
        String payload = value.substring(0, separator);
        byte[] signature = value.substring(separator + 1).getBytes(StandardCharsets.UTF_8);
        byte[] expected = sign(payload).getBytes(StandardCharsets.UTF_8);
        if (!MessageDigest.isEqual(expected, signature)) {
            log.warn("Cookie da requisição de autorização OAuth2 com assinatura inválida.");
            return null;
        }
        try (ObjectInputStream in = new ObjectInputStream(
                new ByteArrayInputStream(Base64.getUrlDecoder().decode(payload)))) {
            return (OAuth2AuthorizationRequest) in.readObject();
        }
    } catch (IOException | ClassNotFoundException | ClassCastException
             | IllegalArgumentException | GeneralSecurityException e) {
        log.warn("Não foi possível ler o cookie da requisição de autorização OAuth2: {}", e.getMessage());
        return null;
    }
}

private String sign(String payload) throws GeneralSecurityException {
    Mac mac = Mac.getInstance(HMAC_ALGORITHM);
    mac.init(new SecretKeySpec(secret, HMAC_ALGORITHM));
    return Base64.getUrlEncoder().withoutPadding()
            .encodeToString(mac.doFinal(payload.getBytes(StandardCharsets.UTF_8)));
}
```

Somente quem conhece a chave `app.oauth2.cookie-secret` (a API) consegue gerar uma assinatura válida. Um *cookie* alterado é descartado, e o Spring Security encerra a autenticação com o erro `authorization_request_not_found`. A comparação é feita com `MessageDigest.isEqual`, que leva o mesmo tempo independentemente de onde está a diferença, evitando ataques de temporização (*timing attacks*).

A classe **CookieUtils** (`utils/CookieUtils.java`) possui apenas os métodos auxiliares `getCookie`, `addCookie` e `deleteCookie`.

---

## 4. 👤 Obtendo os dados do usuário

Cada provedor OAuth2 retorna os dados do usuário com nomes de atributos diferentes (o Google utiliza `sub`, `name`, `email`, `email_verified` e `picture`). Para que o restante da aplicação não dependa desses nomes foi criada a classe abstrata **OAuth2UserInfo**, em **`security/oauth2/user/`**:

```java
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
```

E a implementação para o Google, **GoogleOAuth2UserInfo**:

```java
public class GoogleOAuth2UserInfo extends OAuth2UserInfo {

    public GoogleOAuth2UserInfo(Map<String, Object> attributes) {
        super(attributes);
    }

    @Override
    public String getId() { return (String) attributes.get("sub"); }

    @Override
    public String getName() { return (String) attributes.get("name"); }

    @Override
    public String getEmail() { return (String) attributes.get("email"); }

    @Override
    public boolean isEmailVerified() { return Boolean.TRUE.equals(attributes.get("email_verified")); }

    @Override
    public String getImageUrl() { return (String) attributes.get("picture"); }
}
```

A classe **OAuth2UserInfoFactory** retorna a implementação correta a partir do *registrationId*. Para adicionar um novo provedor (por exemplo, GitHub) basta registrá-lo no `application.yml`, criar a classe `GithubOAuth2UserInfo` e adicioná-la à *factory*:

```java
public static OAuth2UserInfo getOAuth2UserInfo(String registrationId, Map<String, Object> attributes) {
    if (registrationId.equalsIgnoreCase(AuthProvider.google.toString())) {
        return new GoogleOAuth2UserInfo(attributes);
    } else {
        throw new OAuth2AuthenticationProcessingException(
                "Desculpe! A autenticação com " + registrationId + " não é suportada.");
    }
}
```

---

## 5. 🧑‍💼 Cadastrando o usuário: CustomOAuth2UserService

Após trocar o *code* pelo *access token*, o Spring Security chama o **OAuth2UserService** configurado para buscar os dados do usuário no Google. A classe **CustomOAuth2UserService** estende o serviço padrão (`DefaultOAuth2UserService`): o método `super.loadUser()` busca os dados no Google, e a nossa implementação cadastra ou atualiza o usuário no banco de dados da aplicação:

```java
@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;
    private final UserService userService;

    // construtor com a injeção das dependências...

    @Override
    public OAuth2User loadUser(OAuth2UserRequest oAuth2UserRequest) throws OAuth2AuthenticationException {
        try {
            return processOAuth2User(oAuth2UserRequest, super.loadUser(oAuth2UserRequest));
        } catch (AuthenticationException ex) {
            throw ex;
        } catch (Exception ex) {
            // Lançar uma AuthenticationException faz com que o OAuth2AuthenticationFailureHandler seja executado
            throw new InternalAuthenticationServiceException(ex.getMessage(), ex);
        }
    }

    private OAuth2User processOAuth2User(OAuth2UserRequest oAuth2UserRequest, OAuth2User oAuth2User) {
        String registrationId = oAuth2UserRequest.getClientRegistration().getRegistrationId();
        OAuth2UserInfo oAuth2UserInfo = OAuth2UserInfoFactory.getOAuth2UserInfo(registrationId, oAuth2User.getAttributes());

        if (!StringUtils.hasText(oAuth2UserInfo.getEmail())) {
            throw new OAuth2AuthenticationProcessingException("E-mail não encontrado.");
        }
        // O e-mail é utilizado como username, então só são aceitas contas com o e-mail verificado
        if (!oAuth2UserInfo.isEmailVerified()) {
            throw new OAuth2AuthenticationProcessingException("O e-mail da conta não foi verificado.");
        }

        AuthProvider provider = AuthProvider.valueOf(registrationId);
        User user = userRepository.findUserByUsername(oAuth2UserInfo.getEmail());
        if (user != null) {
            // Um usuário cadastrado com usuário e senha (ou outra rede social) não pode entrar com o Google
            if (!provider.equals(user.getProvider())) {
                throw new OAuth2AuthenticationProcessingException(
                        "Você se cadastrou com a sua conta " + user.getProvider() +
                        ". Utilize a sua conta " + user.getProvider() + " para autenticar-se.");
            }
            user = updateExistingUser(user, oAuth2UserInfo);
        } else {
            user = registerNewUser(provider, oAuth2UserInfo);
        }

        return UserPrincipal.create(user, oAuth2User.getAttributes());
    }

    private User registerNewUser(AuthProvider provider, OAuth2UserInfo oAuth2UserInfo) {
        User user = new User();
        user.setProvider(provider);
        user.setUsername(oAuth2UserInfo.getEmail());
        user.setDisplayName(getDisplayName(oAuth2UserInfo.getName(), oAuth2UserInfo.getEmail()));
        // O usuário não conhece essa senha: ele sempre se autenticará pela rede social.
        // Além disso, o JWTAuthenticationFilter bloqueia o login por senha para provider != local.
        user.setPassword(PasswordGenerator.generate());
        // O UserService criptografa a senha e adiciona a permissão ROLE_USER
        userService.save(user);
        return user;
    }

    private User updateExistingUser(User existingUser, OAuth2UserInfo oAuth2UserInfo) {
        existingUser.setDisplayName(getDisplayName(oAuth2UserInfo.getName(), existingUser.getUsername()));
        return userRepository.save(existingUser);
    }

    // O displayName deve ter entre 4 e 50 caracteres (validação da entidade User)
    private String getDisplayName(String name, String username) { ... }
}
```

Pontos importantes:

- O **e-mail** da conta Google é utilizado como `username`, por isso só são aceitas contas com o e-mail **verificado** pelo Google.
- Diferente da aula04, um usuário que se cadastrou com usuário e senha (`provider` = `local`) **não** pode entrar com uma conta Google de mesmo e-mail: cada usuário se autentica sempre pela mesma origem.
- Qualquer exceção lançada aqui é convertida em uma `AuthenticationException`, o que faz o Spring Security executar o **OAuth2AuthenticationFailureHandler** (seção 7).

### 5.1 Transação no UserService

O cadastro do usuário é feito pelo método `save` do **UserService**, que busca a permissão `ROLE_USER` com o `authorityRepository.findByAuthority` e depois salva o usuário. Cada método de um *repository* do Spring Data executa na sua própria transação. Por isso, ao final do `findByAuthority` a `Authority` retornada fica **desanexada** (*detached*) do contexto de persistência. Como o relacionamento `userAuthorities` da classe **User** possui `CascadeType.PERSIST`, o `userRepository.save(user)` tenta persistir também essa `Authority` e falha com o erro:

```
InternalAuthenticationServiceException: Detached entity passed to persist: br.edu.utfpr.pb.pw45s.server.model.Authority
```

No cadastro pelo formulário (`POST /users`) o erro não acontece porque o Spring Boot mantém o mesmo `EntityManager` aberto durante toda a requisição do *controller* (*Open Session in View*). O **CustomOAuth2UserService**, porém, é executado dentro de um **filtro do Spring Security**, antes de a requisição chegar ao Spring MVC, então não conta com esse recurso.

A solução é executar todo o método `save` em **uma única transação**, com a anotação `@Transactional`. Assim a `Authority` buscada continua gerenciada pelo mesmo `EntityManager` que salva o usuário:

```java
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
```

O teste **`UserServiceTest`** (`src/test/java/.../service/UserServiceTest.java`) reproduz essa situação: ele chama o `save` fora de uma requisição e sem transação, e verifica que o usuário é cadastrado com a permissão `ROLE_USER`.

### 5.2 Senha aleatória

O atributo `password` é obrigatório na entidade **User**, então o usuário cadastrado via rede social recebe uma **senha aleatória**, gerada pela classe **PasswordGenerator** (`utils/PasswordGenerator.java`):

```java
public static String generate() {
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
```

- É utilizada a classe **`SecureRandom`** (e não `Random`), adequada para uso criptográfico.
- A senha possui 32 caracteres e respeita a mesma regra da entidade **User** (letra minúscula, letra maiúscula e número).
- A senha é criptografada com **BCrypt** pelo **UserService**, e ninguém a conhece. Além disso, o **JWTAuthenticationFilter** (da aula04) recusa o *login* por senha de usuários cujo `provider` não seja `local`.

### 5.3 UserPrincipal

O objeto retornado pelo `loadUser()` precisa implementar a interface `OAuth2User`. A classe **UserPrincipal** une os dados do usuário da aplicação (o `username` e as **permissões cadastradas no banco de dados**) com os atributos retornados pelo Google:

```java
public class UserPrincipal implements OAuth2User {

    private final Long id;
    private final String username;
    private final Collection<? extends GrantedAuthority> authorities;
    private final Map<String, Object> attributes;

    public static UserPrincipal create(User user, Map<String, Object> attributes) {
        return new UserPrincipal(user.getId(), user.getUsername(), user.getAuthorities(), attributes);
    }

    // getters: getId(), getUsername(), getAuthorities(), getAttributes()

    @Override
    public String getName() {
        return username;
    }
}
```

---

## 6. 🎫 Gerando o JWT: TokenProvider

A classe **TokenProvider** gera o JWT da API para o usuário autenticado, com os mesmos `SECRET`, `EXPIRATION_TIME` e algoritmo (`HMAC512`) do **JWTAuthenticationFilter**. Assim, o **JWTAuthorizationFilter** valida o *token* sem nenhuma alteração:

```java
@Service
public class TokenProvider {

    public String createToken(Authentication authentication) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();

        return JWT.create()
                // o subject do token é o username do usuário (o e-mail da conta Google)
                .withSubject(userPrincipal.getUsername())
                .withExpiresAt(new Date(System.currentTimeMillis() + SecurityConstants.EXPIRATION_TIME))
                .sign(Algorithm.HMAC512(SecurityConstants.SECRET));
    }
}
```

---

## 7. ↩️ Redirecionando para o front-end

### 7.1 Sucesso: OAuth2AuthenticationSuccessHandler

Quando a autenticação é concluída com sucesso, o **OAuth2AuthenticationSuccessHandler** gera o JWT e redireciona o navegador para o endereço informado pelo front-end (armazenado no *cookie* `redirect_uri`), adicionando o *token* no parâmetro `token` da URL. Antes disso, o endereço é validado com o método `isAuthorizedRedirectUri` da classe **AppProperties**:

```java
@Override
public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                    Authentication authentication) throws IOException {
    if (response.isCommitted()) {
        return;
    }

    String redirectUri = CookieUtils.getCookie(request, REDIRECT_URI_PARAM_COOKIE_NAME)
            .map(Cookie::getValue)
            .orElse(appProperties.defaultRedirectUri());

    clearAuthenticationAttributes(request, response);

    // O token só é enviado para endereços cadastrados em app.oauth2.authorized-redirect-uris
    if (!appProperties.isAuthorizedRedirectUri(redirectUri)) {
        response.sendError(HttpStatus.BAD_REQUEST.value(),
                "Endereço de redirecionamento não autorizado.");
        return;
    }

    String targetUrl = UriComponentsBuilder.fromUriString(redirectUri)
            .queryParam("token", tokenProvider.createToken(authentication))
            .build().toUriString();
    getRedirectStrategy().sendRedirect(request, response, targetUrl);
}
```

Resultado: `http://localhost:5173/login?token=eyJhbGciOiJIUzUxMiIs...`. O método `clearAuthenticationAttributes` remove os *cookies* da requisição de autorização, que não são mais necessários.

> 💡 Enviar o *token* na URL é uma forma simples de devolvê-lo ao front-end após uma sequência de redirecionamentos, mas a URL pode ficar registrada no histórico do navegador. Por isso o front-end remove o *token* da URL assim que o lê (ver o [README.md do client](../client/README.md)).

### 7.2 Falha: OAuth2AuthenticationFailureHandler

Se ocorrer qualquer erro (usuário cancelou a autenticação, e-mail não verificado, usuário cadastrado com outro `provider`, *cookie* inválido, etc.), o **OAuth2AuthenticationFailureHandler** redireciona o navegador para o front-end com a mensagem de erro no parâmetro `error`. O endereço também é validado: se não for um endereço autorizado, é utilizado o endereço padrão:

```java
@Override
public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
                                    AuthenticationException exception) throws IOException {
    String redirectUri = CookieUtils.getCookie(request, REDIRECT_URI_PARAM_COOKIE_NAME)
            .map(Cookie::getValue)
            // só redireciona para endereços autorizados
            .filter(appProperties::isAuthorizedRedirectUri)
            .orElse(appProperties.defaultRedirectUri());

    String targetUrl = UriComponentsBuilder.fromUriString(redirectUri)
            .queryParam("error", exception.getLocalizedMessage())
            .encode()
            .build().toUriString();

    httpCookieOAuth2AuthorizationRequestRepository.removeAuthorizationRequestCookies(request, response);

    getRedirectStrategy().sendRedirect(request, response, targetUrl);
}
```

Resultado: `http://localhost:5173/login?error=...`.

---

## 8. 🛡️ Configurando o oauth2Login no WebSecurity

Por fim, todas as classes são ligadas na classe **WebSecurity**. As rotas `/oauth2/**` são liberadas, pois são acessadas antes de o usuário estar autenticado:

```java
http.authorizeHttpRequests((authorize) -> authorize
        .requestMatchers(HttpMethod.POST, "/users/**").permitAll()
        .requestMatchers("/error/**").permitAll()
        .requestMatchers("/h2-console/**").permitAll()
        //permite que as rotas do fluxo OAuth2 (login com o Google) sejam acessadas sem o usuário estar autenticado
        .requestMatchers("/oauth2/**").permitAll()
        // ... Swagger e regras por role (iguais à aula04)
        .anyRequest().authenticated()
);

// Autenticação com redes sociais: a API atua como cliente OAuth2 do Google
http.oauth2Login(oauth2Login -> oauth2Login
        // URL que inicia a autenticação: /oauth2/authorize/{registrationId}, ex.: /oauth2/authorize/google
        .authorizationEndpoint(authorizationEndpoint -> authorizationEndpoint
                .baseUri("/oauth2/authorize")
                // a API é stateless, então a requisição de autorização é armazenada em um cookie
                .authorizationRequestRepository(cookieAuthorizationRequestRepository))
        // URL para a qual o Google redireciona após a autenticação: /oauth2/callback/{registrationId}
        .redirectionEndpoint(redirectionEndpoint -> redirectionEndpoint
                .baseUri("/oauth2/callback/*"))
        // busca os dados do usuário no Google e cadastra/atualiza o usuário no banco de dados
        .userInfoEndpoint(userInfoEndpoint -> userInfoEndpoint
                .userService(customOAuth2UserService))
        // gera o token JWT e redireciona para o front-end
        .successHandler(oAuth2AuthenticationSuccessHandler)
        // redireciona para o front-end com a mensagem de erro
        .failureHandler(oAuth2AuthenticationFailureHandler)
);
```

Os objetos `customOAuth2UserService`, `cookieAuthorizationRequestRepository`, `oAuth2AuthenticationSuccessHandler` e `oAuth2AuthenticationFailureHandler` são injetados no construtor da classe **WebSecurity**.

> Os valores padrão do Spring Security são `/oauth2/authorization/{registrationId}` e `/login/oauth2/code/{registrationId}`. Eles foram alterados apenas para deixar as URLs mais curtas. Se alterar o `redirectionEndpoint`, altere também o `redirect-uri` no `application.yml` e no Google Cloud Console.

---

## 9. 🙋 Endpoint /auth/user-info

Como o front-end recebe apenas o JWT na URL, ele precisa buscar os dados do usuário autenticado e as suas permissões. Para isso o *endpoint* `GET /auth/user-info` (que exige autenticação) passou a retornar um **UserResponseDTO**, o mesmo objeto `user` retornado pelo *login* com usuário e senha:

```java
@GetMapping("user-info")
public UserResponseDTO getUserInfo(Principal principal) {
    User user = (User) authService.loadUserByUsername(principal.getName());
    return new UserResponseDTO(user);
}
```

```json
{ "displayName": "Nome do Usuário", "username": "usuario@gmail.com", "authorities": [ { "authority": "ROLE_USER" } ] }
```

> Antes, o *endpoint* retornava o **UserDTO**, que possui o atributo `password` e expunha o *hash* da senha do usuário.

---

## 10. 🧪 Testando

Com a API em execução (e o `GOOGLE_CLIENT_SECRET` informado no `.env`), acesse no navegador:

```
http://localhost:8080/oauth2/authorize/google?redirect_uri=http://localhost:5173/login
```

Após autenticar-se no Google, o navegador será redirecionado para `http://localhost:5173/login?token=...`. O *token* pode ser utilizado para consultar os dados do usuário:

```bash
curl http://localhost:8080/auth/user-info -H "Authorization: Bearer <token>"
```

O novo usuário pode ser consultado no console do H2 (`http://localhost:8080/h2-console`, JDBC URL `jdbc:h2:mem:pw45s-dev`), na tabela `TB_USER`, com o `PROVIDER` igual a `google`.

Erros comuns:

| Erro | Causa |
| --- | --- |
| `redirect_uri_mismatch` (página do Google) | `http://localhost:8080/oauth2/callback/google` não está cadastrada em **URIs de redirecionamento autorizados**. |
| `?error=[invalid_request] client_secret is missing.` | O `GOOGLE_CLIENT_SECRET` não foi informado: o arquivo `.env` não existe, está vazio ou a aplicação não foi executada a partir da pasta `server`. |
| `?error=[invalid_token_response] ... invalid_client` | *Client ID* ou *client secret* incorretos no `.env`. |
| `?error=[authorization_request_not_found]` | O *cookie* expirou (mais de 3 minutos na página do Google), foi alterado ou a autenticação foi iniciada em outro navegador. |
| `?error=Você se cadastrou com a sua conta local...` | Já existe um usuário cadastrado com usuário e senha com o mesmo e-mail. |

> 💡 Os usuários iniciais (`admin`, `teste`) continuam se autenticando com usuário e senha (`123`) em `POST /login`.
