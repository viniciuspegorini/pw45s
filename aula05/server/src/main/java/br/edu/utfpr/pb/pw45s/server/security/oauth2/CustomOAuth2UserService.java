package br.edu.utfpr.pb.pw45s.server.security.oauth2;

import br.edu.utfpr.pb.pw45s.server.error.OAuth2AuthenticationProcessingException;
import br.edu.utfpr.pb.pw45s.server.model.AuthProvider;
import br.edu.utfpr.pb.pw45s.server.model.User;
import br.edu.utfpr.pb.pw45s.server.repository.UserRepository;
import br.edu.utfpr.pb.pw45s.server.security.oauth2.user.OAuth2UserInfo;
import br.edu.utfpr.pb.pw45s.server.security.oauth2.user.OAuth2UserInfoFactory;
import br.edu.utfpr.pb.pw45s.server.service.UserService;
import br.edu.utfpr.pb.pw45s.server.utils.PasswordGenerator;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * Chamado pelo Spring Security após o retorno do Google, quando a API já trocou o "code" pelo
 * access token. O método super.loadUser() busca os dados do usuário no Google (e-mail, nome, foto)
 * e esta classe cadastra ou atualiza o usuário no banco de dados da aplicação.
 */
@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;
    private final UserService userService;

    public CustomOAuth2UserService(UserRepository userRepository, UserService userService) {
        this.userRepository = userRepository;
        this.userService = userService;
    }

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
    private String getDisplayName(String name, String username) {
        String displayName = (!StringUtils.hasText(name) || name.length() < 4) ? username : name;
        return displayName.length() > 50 ? displayName.substring(0, 50) : displayName;
    }
}
