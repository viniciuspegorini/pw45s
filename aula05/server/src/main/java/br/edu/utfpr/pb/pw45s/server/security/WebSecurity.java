package br.edu.utfpr.pb.pw45s.server.security;

import br.edu.utfpr.pb.pw45s.server.security.oauth2.CustomOAuth2UserService;
import br.edu.utfpr.pb.pw45s.server.security.oauth2.HttpCookieOAuth2AuthorizationRequestRepository;
import br.edu.utfpr.pb.pw45s.server.security.oauth2.OAuth2AuthenticationFailureHandler;
import br.edu.utfpr.pb.pw45s.server.security.oauth2.OAuth2AuthenticationSuccessHandler;
import br.edu.utfpr.pb.pw45s.server.service.AuthService;
import lombok.SneakyThrows;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@EnableWebSecurity
@Configuration
public class WebSecurity {

    // Service responsável por buscar um usuário no banco de dados por meio do método loadByUsername()
    private final AuthService authService;
    // Objeto responsável por realizar o tratamento de exceção quando o usuário informar credenciais incorretas ao autenticar-se.
    private final AuthenticationEntryPoint authenticationEntryPoint;
    // Objetos utilizados na autenticação com redes sociais (OAuth2)
    private final CustomOAuth2UserService customOAuth2UserService;
    private final HttpCookieOAuth2AuthorizationRequestRepository cookieAuthorizationRequestRepository;
    private final OAuth2AuthenticationSuccessHandler oAuth2AuthenticationSuccessHandler;
    private final OAuth2AuthenticationFailureHandler oAuth2AuthenticationFailureHandler;

    public WebSecurity(AuthService authService,
                       AuthenticationEntryPoint authenticationEntryPoint,
                       CustomOAuth2UserService customOAuth2UserService,
                       HttpCookieOAuth2AuthorizationRequestRepository cookieAuthorizationRequestRepository,
                       OAuth2AuthenticationSuccessHandler oAuth2AuthenticationSuccessHandler,
                       OAuth2AuthenticationFailureHandler oAuth2AuthenticationFailureHandler) {
        this.authService = authService;
        this.authenticationEntryPoint = authenticationEntryPoint;
        this.customOAuth2UserService = customOAuth2UserService;
        this.cookieAuthorizationRequestRepository = cookieAuthorizationRequestRepository;
        this.oAuth2AuthenticationSuccessHandler = oAuth2AuthenticationSuccessHandler;
        this.oAuth2AuthenticationFailureHandler = oAuth2AuthenticationFailureHandler;
    }

    @Bean
    @SneakyThrows
    public SecurityFilterChain filterChain(HttpSecurity http) {
        AuthenticationManagerBuilder authenticationManagerBuilder =
                http.getSharedObject(AuthenticationManagerBuilder.class);
        authenticationManagerBuilder
                .userDetailsService(authService)
                .passwordEncoder(passwordEncoder());
        // authenticationManager -> responsável por gerenciar a autenticação dos usuários
        AuthenticationManager authenticationManager =
                authenticationManagerBuilder.build();

        //Configuração para funcionar o console do H2.
        http.headers(headers -> headers.frameOptions(HeadersConfigurer.FrameOptionsConfig::disable));
        // desabilita o uso de csrf
        http.csrf(AbstractHttpConfigurer::disable);

        // Adiciona configuração de CORS
        http.cors(cors -> corsConfigurationSource());

        //define o objeto responsável pelo tratamento de exceção ao entrar com credenciais inválidas
        http.exceptionHandling(exceptionHandling -> exceptionHandling.authenticationEntryPoint(authenticationEntryPoint));

        // configura a authorização das requisições
        http.authorizeHttpRequests((authorize) -> authorize
                //permite que a rota "/users" seja acessada, mesmo sem o usuário estar autenticado desde que o método HTTP da requisição seja POST
                .requestMatchers(HttpMethod.POST, "/users/**").permitAll()
                //permite que a rota "/error" seja acessada por qualquer    requisição mesmo o usuário não estando autenticado
                .requestMatchers("/error/**").permitAll()
                //permite que a rota "/h2-console" seja acessada por qualquer requisição mesmo o usuário não estando autenticado
                .requestMatchers("/h2-console/**").permitAll()
                //permite que as rotas do fluxo OAuth2 (login com o Google) sejam acessadas sem o usuário estar autenticado
                .requestMatchers("/oauth2/**").permitAll()

                //documentação da API (Swagger)
                .requestMatchers("/v3/**").permitAll()
                .requestMatchers("/api-docs/**").permitAll()
                .requestMatchers("/swagger-ui/**").permitAll()

                // Somente usuários com permissão de admin podem acessar /products (qualquer requisição HTTP)
                .requestMatchers("/products/**").hasAnyRole("ADMIN")
                .requestMatchers(HttpMethod.GET, "/categories/**").hasAnyRole("ADMIN", "USER")
                .requestMatchers("/categories/**").hasAnyRole("USER")
                .requestMatchers("/users/**").hasAnyRole("ADMIN")

                //as demais rotas da aplicação só podem ser acessadas se o usuário estiver autenticado
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
        http.authenticationManager(authenticationManager)
                //Filtro da Autenticação - sobrescreve o método padrão do Spring Security para Autenticação.
                .addFilter(new JWTAuthenticationFilter(authenticationManager, authService))
                //Filtro da Autorização - - sobrescreve o método padrão do Spring Security para Autorização.
                .addFilter(new JWTAuthorizationFilter(authenticationManager, authService))
                //Como será criada uma API REST e todas as requisições que necessitam de autenticação/autorização serão realizadas com o envio do token JWT do usuário, não será necessário fazer controle de sessão no *back-end*.
                .sessionManagement(sessionManagement -> sessionManagement.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        return http.build();
    }

    // Criação do objeto utilizado na criptografia da senha, ele é usado no UserService ao cadastrar um usuário e pelo authenticationManagerBean para autenticar um usuário no sistema.
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /*
        O compartilhamento de recursos de origem cruzada (CORS) é um mecanismo para integração de aplicativos.
        O CORS define uma maneira de os aplicativos Web clientes carregados em um domínio interagirem com recursos em um domínio diferente.
    */
    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        // Lista das origens autorizadas, no nosso caso que iremos rodar a aplicação localmente o * poderia ser trocado
        // por: http://localhost:porta, em que :porta será a porta em que a aplicação cliente será executada
        configuration.setAllowedOrigins(List.of("*"));
        // Lista dos métodos HTTP autorizados
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "HEAD", "TRACE", "CONNECT"));
        // Lista dos Headers autorizados, o Authorization será o header que iremos utilizar para transferir o Token
        configuration.setAllowedHeaders(List.of("Authorization","x-xsrf-token",
                "Access-Control-Allow-Headers", "Origin",
                "Accept", "X-Requested-With", "Content-Type",
                "Access-Control-Request-Method",
                "Access-Control-Request-Headers", "Auth-Id-Token"));
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
