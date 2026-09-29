# 🔐 Autenticação com o Google realizada no servidor - Lado Cliente (React)

Na **aula04** o front-end era responsável por autenticar o usuário no Google: a biblioteca `@react-oauth/google` exibia o botão do Google, recebia o **ID Token** e o enviava para a API (`POST /auth-social`). Nesta aula **toda a autenticação com o Google ocorre na API** (ver o [README.md do server](../server/README.md)), e o papel do front-end fica ainda mais simples:

1. Exibir um *link* que leva o usuário para a API, em `/oauth2/authorize/google`;
2. Ao final do fluxo, a API redireciona o usuário de volta para a página de *login* do front-end com o **JWT da API** na URL (`/login?token=...`) ou com uma mensagem de erro (`/login?error=...`);
3. Com o *token*, buscar os dados do usuário em `GET /auth/user-info` e armazená-los, exatamente como no *login* com usuário e senha.

O front-end não possui mais nenhuma biblioteca ou credencial do Google.

> A configuração das credenciais no Google Cloud Console está descrita no [README.md](../README.md) da pasta **aula05**.

## 📋 Resumo das alterações

O projeto parte do código do cliente da **aula04**. As alterações realizadas foram:

- **`package.json`**: removida a biblioteca `@react-oauth/google`.
- **`main.tsx`**: removido o componente **GoogleOAuthProvider**.
- **`.env` / `vite-env.d.ts`**: removida a variável `VITE_GOOGLE_CLIENT_ID`, o *Client ID* não é mais necessário no front-end.
- **`AuthContext.tsx`**: a função `handleLoginSocial` (que enviava o ID Token para a API) foi substituída pela função `handleLoginSocialServer`, que recebe o JWT da API.
- **`pages/login/index.tsx`** e **`style.css`**: o botão **GoogleLogin** foi substituído por um *link* para a API, e a página passou a tratar os parâmetros `token` e `error` da URL.

Os componentes **RequireAuth**, **AppRoutes**, **TopMenu** e as interfaces em `/src/commons/types.ts` **não foram alterados**.

---

## 1. 🧹 Removendo a biblioteca do Google

Como o front-end não conversa mais com o Google, a biblioteca foi removida:

```bash
npm uninstall @react-oauth/google
```

E o componente **GoogleOAuthProvider** foi removido do **`/src/main.tsx`**, que volta a ter apenas os *providers* da aplicação:

```tsx
createRoot(document.getElementById("root")!).render(
  <StrictMode>
    <BrowserRouter>
      <PrimeReactProvider>
        <AuthProvider>
          <App />
        </AuthProvider>
      </PrimeReactProvider>
    </BrowserRouter>
  </StrictMode>
);
```

---

## 2. 🔑 Recebendo o token no AuthContext

No contexto de autenticação, em **`/src/context/AuthContext.tsx`**, foi criada a função **handleLoginSocialServer**. Primeiro, a assinatura é incluída na interface do contexto:

```tsx
interface AuthContextType {
  authenticated: boolean;
  authenticatedUser?: AuthenticatedUser;
  handleLogin: (authenticationResponse: AuthenticationResponse) => Promise<any>;
  handleLogout: () => void;
  hasPermission: (permission: string) => boolean;
  handleLoginSocialServer: (token: string) => Promise<boolean>; // adicionado
}
```

Em seguida, a implementação da função:

```tsx
const handleLoginSocialServer = async (token: string): Promise<boolean> => {
  try {
    // A API devolve apenas o token na URL, então os dados do usuário (e suas permissões)
    // são buscados em /auth/user-info, enviando o token recebido
    const response = await api.get<AuthenticatedUser>("/auth/user-info", {
      headers: { Authorization: `Bearer ${token}` },
    });
    // A partir daqui, o processo é o mesmo do login com usuário e senha
    await handleLogin({ token, user: response.data });
    navigate("/");
    return true;
  } catch {
    handleLogout();
    return false;
  }
};
```

E a função é disponibilizada no *Provider*, para ser acessada pelo *hook* `useAuth`:

```tsx
<AuthContext.Provider
  value={{
    authenticated,
    authenticatedUser,
    handleLogin,
    handleLogout,
    hasPermission,
    handleLoginSocialServer, // adicionado
  }}
>
```

Por que buscar o usuário em `/auth/user-info`? Porque o controle de acesso das rotas (**RequireAuth**) e do menu (**TopMenu**) depende das *authorities* do usuário (`ROLE_USER`, `ROLE_ADMIN`). Se apenas o *token* fosse armazenado, o `authenticatedUser` ficaria vazio e o usuário, mesmo autenticado, seria redirecionado para a página `/unauthorized`. Como a API responde com o mesmo formato do objeto `user` do *login* tradicional, a função **handleLogin**, já existente, é reutilizada para armazenar o *token* e o usuário no `localStorage` e atualizar o estado do contexto.

O *token* é enviado no *header* `Authorization` apenas dessa requisição (terceiro parâmetro do `api.get`): o *header* padrão do Axios só é configurado pelo `handleLogin`, depois que o usuário foi carregado com sucesso. Se o *token* for inválido, o `catch` garante que nenhum dado de autenticação fique armazenado.

---

## 3. 🟦 Adicionando o link do Google na página de login

### 3.1 O link para a API

Na página de *login*, em **`/src/pages/login/index.tsx`**, o botão do Google passa a ser um *link* comum (`<a href>`), e não uma requisição do Axios: a autenticação é feita por uma sequência de **redirecionamentos do navegador** (front-end → API → Google → API → front-end), e não por uma chamada AJAX.

A URL é montada a partir da URL base da API (configurada no **`/src/lib/axios.ts`**) e do endereço atual do front-end, que é enviado no parâmetro `redirect_uri`:

```tsx
import { api } from "@/lib/axios";
import "./style.css";
import googleLogo from "@/assets/google-logo.png";

// URL da API que inicia a autenticação com o Google. O parâmetro redirect_uri informa para qual
// endereço do front-end a API deve redirecionar o usuário (com o token) após a autenticação.
const GOOGLE_AUTH_URL = `${api.defaults.baseURL}/oauth2/authorize/google?redirect_uri=${encodeURIComponent(
  `${window.location.origin}/login`
)}`;
```

Resultado: `http://localhost:8080/oauth2/authorize/google?redirect_uri=http%3A%2F%2Flocalhost%3A5173%2Flogin`.

> ⚠️ O endereço enviado em `redirect_uri` precisa estar cadastrado na API, na propriedade `app.oauth2.authorized-redirect-uris` do `application.yml`. Caso contrário, a API se recusa a enviar o *token* para ele. Se o front-end for executado em outra porta ou domínio, esse valor deve ser atualizado na API.

O *link* é exibido abaixo do botão **Entrar**, utilizando as classes de botão do PrimeReact e a imagem `src/assets/google-logo.png`:

```tsx
<a
  className="p-button p-button-outlined p-button-secondary w-full social-btn"
  href={GOOGLE_AUTH_URL}
>
  <img src={googleLogo} alt="Google" /> Entrar com o Google
</a>
```

E o arquivo **`style.css`** alinha a imagem ao texto:

```css
.social-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0.5rem;
  text-decoration: none;
}

.social-btn img {
  height: 20px;
}
```

### 3.2 Tratando o retorno da API

Ao final do fluxo a API redireciona o navegador para `/login?token=...` (sucesso) ou `/login?error=...` (falha). Como o navegador carrega novamente a página de *login*, os parâmetros são lidos da URL com o *hook* `useLocation` em um `useEffect`:

```tsx
const { handleLogin, handleLoginSocialServer } = useAuth();
const { search } = useLocation();
// evita processar o token duas vezes (o StrictMode executa o useEffect duas vezes em desenvolvimento)
const socialLoginProcessed = useRef(false);

const showGoogleError = (detail = "Falha ao efetuar autenticação com o Google.") => {
  toast.current?.show({ severity: "error", summary: "Erro", detail, life: 5000 });
};

//Autenticação GOOGLE - retorno da API: /login?token=... ou /login?error=...
useEffect(() => {
  const params = new URLSearchParams(search);
  const token = params.get("token");
  const error = params.get("error");
  if ((!token && !error) || socialLoginProcessed.current) {
    return;
  }
  socialLoginProcessed.current = true;
  // remove o token/erro da URL (e do histórico do navegador)
  navigate("/login", { replace: true });

  if (error) {
    showGoogleError(error);
  } else if (token) {
    handleLoginSocialServer(token).then((success) => {
      if (!success) {
        showGoogleError();
      }
    });
  }
  // eslint-disable-next-line react-hooks/exhaustive-deps
}, [search]);
```

Alguns detalhes da implementação:

- **`navigate("/login", { replace: true })`**: remove o *token* da barra de endereços e substitui a entrada no histórico do navegador. Assim o *token* não fica visível na tela nem acessível pelo botão "Voltar".
- **`socialLoginProcessed`**: em modo de desenvolvimento o `<StrictMode>` executa os efeitos duas vezes. A referência (`useRef`) garante que o *token* seja processado apenas uma vez.
- **`error`**: a mensagem enviada pela API (por exemplo, "O e-mail da conta não foi verificado." ou "Você se cadastrou com a sua conta local...") é exibida ao usuário com o componente `Toast`.

---

## 4. ✅ Resumindo o fluxo de autenticação social

1. O usuário acessa `/login` e clica em **Entrar com o Google**.
2. O navegador é levado para a API (`/oauth2/authorize/google?redirect_uri=http://localhost:5173/login`), que o redireciona para a página de *login* do Google.
3. O usuário se autentica no Google, que redireciona o navegador de volta para a API (`/oauth2/callback/google`).
4. A API obtém os dados do usuário no Google, cadastra o usuário caso seja o seu primeiro acesso (com a permissão `ROLE_USER`), gera o **JWT da API** e redireciona o navegador para `http://localhost:5173/login?token=...`.
5. A página de *login* lê o *token*, remove-o da URL e chama `handleLoginSocialServer`.
6. `handleLoginSocialServer` busca os dados do usuário em `/auth/user-info` e chama `handleLogin`, que armazena o *token* e o usuário no `localStorage` e configura o *header* `Authorization` do Axios.
7. A partir daqui, o controle de acesso segue exatamente como nas aulas anteriores: o **RequireAuth** valida as *roles* de cada rota e o **TopMenu** exibe os itens de acordo com `hasPermission`.
