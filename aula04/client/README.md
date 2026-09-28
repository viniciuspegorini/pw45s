# 🔐 Autenticação com o Google no Lado Cliente (React)

Considerando a aplicação desenvolvida até a **aula03**, que já possuía autenticação via *token* JWT (usuário e senha) e controle de permissões por *role* nas rotas e no menu (descrito no [README.md da aula03](../../aula03/client/README.md)), nesta aula foi adicionada a opção de o usuário se autenticar utilizando a sua **conta Google**.

No lado cliente o trabalho é relativamente simples: exibir o botão **"Fazer login com o Google"**, receber o **ID Token** (`credential`) devolvido pelo Google e enviá-lo para a API no *endpoint* `POST /auth-social`. A API valida esse *token* e devolve o **mesmo** objeto retornado pelo *login* tradicional (`{ token, user }`). A partir desse ponto nada muda: o *token* é armazenado no `localStorage`, adicionado ao *header* `Authorization` do Axios, e as rotas continuam protegidas pelo **RequireAuth** com base nas *authorities* do usuário.

> A configuração das credenciais no Google Cloud Console está descrita no [README.md](../README.md) da pasta **aula04**, e as alterações na API no [README.md do server](../server/README.md).

## 📋 Resumo das alterações

- **`package.json`**: adicionada a biblioteca `@react-oauth/google`.
- **`.env` / `.env.example` / `vite-env.d.ts`**: o *Client ID* do Google passou a ser lido da variável de ambiente `VITE_GOOGLE_CLIENT_ID`.
- **`main.tsx`**: a aplicação foi envolvida pelo componente **GoogleOAuthProvider**, que recebe o *Client ID* do Google.
- **`AuthContext.tsx`**: adicionada a função `handleLoginSocial`, responsável por enviar o ID Token do Google para a API e armazenar o JWT retornado.
- **`pages/login/index.tsx`**: adicionado o botão **GoogleLogin** na página de *login*.

Os componentes **RequireAuth**, **AppRoutes**, **TopMenu** e as interfaces em `/src/commons/types.ts` **não precisaram ser alterados**, pois a API devolve o usuário autenticado via Google no mesmo formato (`AuthenticationResponse`) do *login* com usuário e senha.

---

## 1. 📦 Instalando a biblioteca @react-oauth/google

A biblioteca [@react-oauth/google](https://www.npmjs.com/package/@react-oauth/google) encapsula o **Google Identity Services** (script oficial do Google para autenticação) em componentes e *hooks* React:

```bash
npm install @react-oauth/google
```

Após a instalação, a dependência é adicionada ao **`package.json`**:

```json
"dependencies": {
  "@react-oauth/google": "^0.12.2",
  ...
}
```

---

## 2. 🧩 Configurando o GoogleOAuthProvider

### 2.1 Client ID em variável de ambiente

Para não deixar o *Client ID* fixo no código, ele é lido de uma variável de ambiente do Vite. Como o arquivo `.env` é ignorado pelo Git, o projeto possui o arquivo **`.env.example`**, que deve ser copiado para `.env` antes de executar a aplicação:

```bash
cp .env.example .env
```

```properties
# .env
VITE_GOOGLE_CLIENT_ID=<SEU_CLIENT_ID>.apps.googleusercontent.com
```

> O Vite só expõe para o código do navegador as variáveis iniciadas com `VITE_`. Após alterar o `.env` é necessário reiniciar o `npm run dev`.

Para que o TypeScript reconheça a variável (e ofereça autocompletar), ela foi tipada em **`/src/vite-env.d.ts`**:

```ts
/// <reference types="vite/client" />

interface ImportMetaEnv {
  readonly VITE_GOOGLE_CLIENT_ID: string;
}

interface ImportMeta {
  readonly env: ImportMetaEnv;
}
```

### 2.2 Envolvendo a aplicação com o GoogleOAuthProvider

Para que o botão de *login* do Google funcione em qualquer página, a aplicação deve ser envolvida pelo componente **GoogleOAuthProvider**, que carrega o *script* do Google e disponibiliza o *Client ID* para os componentes filhos. Em **`/src/main.tsx`**:

```tsx
import { AuthProvider } from "@/context/AuthContext";
import { GoogleOAuthProvider } from "@react-oauth/google"; // adicionado

createRoot(document.getElementById("root")!).render(
  <StrictMode>
    <BrowserRouter>
      <PrimeReactProvider>
        <GoogleOAuthProvider clientId={import.meta.env.VITE_GOOGLE_CLIENT_ID}>
          <AuthProvider>
            <App />
          </AuthProvider>
        </GoogleOAuthProvider>
      </PrimeReactProvider>
    </BrowserRouter>
  </StrictMode>
);
```

O `clientId` deve ser o **mesmo** ID do cliente configurado na API (propriedade `google.client-id`), pois a API verifica se o ID Token recebido foi emitido para esse *Client ID*.

---

## 3. 🔑 Adicionando o handleLoginSocial ao AuthContext

No contexto de autenticação, em **`/src/context/AuthContext.tsx`**, foi adicionada a função **handleLoginSocial**. Primeiro, a assinatura é incluída na interface do contexto:

```tsx
interface AuthContextType {
  authenticated: boolean;
  authenticatedUser?: AuthenticatedUser;
  handleLogin: (authenticationResponse: AuthenticationResponse) => Promise<any>;
  handleLogout: () => void;
  hasPermission: (permission: string) => boolean;
  handleLoginSocial: (idToken: string) => Promise<boolean>; // adicionado
}
```

Em seguida, a implementação da função:

```tsx
const handleLoginSocial = async (idToken: string): Promise<boolean> => {
  try {
    // 1. Envia o ID Token do Google para a API, apenas nesta requisição
    const response = await api.post<AuthenticationResponse>(
      "/auth-social",
      null,
      { headers: { "Auth-Id-Token": `Bearer ${idToken}` } }
    );
    // 2. A resposta tem o mesmo formato do login tradicional,
    //    então é reaproveitada a função handleLogin
    await handleLogin(response.data);
    navigate("/");
    return true;
  } catch {
    // 3. Token inválido/expirado (HTTP 401) ou API indisponível
    handleLogout();
    return false;
  }
};
```

Alguns detalhes da implementação:

- O *header* `Auth-Id-Token` é informado no terceiro parâmetro do `api.post` (configuração **da requisição**), e não em `api.defaults.headers`. Assim ele é enviado somente para `/auth-social` e não é necessário removê-lo depois, nem mesmo quando ocorre um erro.
- Como a API devolve um `AuthenticationResponse` (`{ token, user }`), a função **handleLogin**, já existente, é reutilizada para armazenar o *token* e o usuário no `localStorage`, configurar o *header* `Authorization` e atualizar o estado do contexto.
- Em caso de falha, o `catch` garante que nenhum dado de autenticação fique armazenado e a função retorna `false`, permitindo que a página de *login* exiba uma mensagem ao usuário.

E, por fim, a função é disponibilizada no *Provider* para que possa ser acessada pelo *hook* `useAuth`:

```tsx
return (
  <AuthContext.Provider
    value={{
      authenticated,
      authenticatedUser,
      handleLogin,
      handleLogout,
      hasPermission,
      handleLoginSocial, // adicionado
    }}
  >
    {children}
  </AuthContext.Provider>
);
```

Perceba a diferença entre os dois tipos de *token* envolvidos:

| Token | Emitido por | Enviado em | Utilizado para |
| --- | --- | --- | --- |
| **ID Token** (`credential`) | Google | *Header* `Auth-Id-Token`, apenas na requisição `POST /auth-social` | Provar para a API que o usuário se autenticou no Google. |
| **JWT da API** (`response.data.token`) | Nossa API (server) | *Header* `Authorization`, em todas as requisições seguintes | Autenticar e autorizar o usuário nos *endpoints* da API (igual ao *login* tradicional). |

O ID Token do Google **não** é armazenado: ele é utilizado apenas uma vez para obter o JWT da API. O nome do *header* deve ser exatamente o mesmo lido pela API e liberado na configuração de CORS do servidor.

---

## 4. 🟦 Adicionando o botão do Google na página de login

Na página de *login*, em **`/src/pages/login/index.tsx`**, foi importado o componente **GoogleLogin** e o tipo **CredentialResponse**:

```tsx
import { GoogleLogin, CredentialResponse } from "@react-oauth/google";
```

A função `handleLoginSocial` é obtida do *hook* `useAuth`, e foi criada a função `onSuccess`, executada quando o usuário conclui a autenticação no Google:

```tsx
const { handleLogin, handleLoginSocial } = useAuth(); // handleLoginSocial adicionado

const showGoogleError = () => {
  toast.current?.show({
    severity: "error",
    summary: "Erro",
    detail: "Falha ao efetuar autenticação com o Google.",
    life: 3000,
  });
};

//Autenticação GOOGLE
const onSuccess = async (response: CredentialResponse) => {
  if (!response.credential || !(await handleLoginSocial(response.credential))) {
    showGoogleError();
  }
};
```

O objeto `CredentialResponse` possui o atributo **`credential`**, que é o **ID Token** (um JWT assinado pelo Google). É esse valor que é repassado à função `handleLoginSocial`. Se o Google não devolver o `credential` ou se a API recusar o *token* (`handleLoginSocial` retorna `false`), é exibida uma mensagem de erro com o componente `Toast`.

Por fim, o botão é adicionado ao formulário, logo abaixo do botão **Entrar**:

```tsx
<Button
  type="submit"
  label="Entrar"
  icon="pi pi-sign-in"
  className="w-full"
  loading={loading || isSubmitting}
  disabled={loading || isSubmitting}
/>
<div className="mb-3">
  <GoogleLogin
    locale="pt-BR"
    onSuccess={onSuccess}
    onError={showGoogleError}
  />
</div>
```

O componente **GoogleLogin** renderiza o botão padrão do Google (a propriedade `locale="pt-BR"` exibe o texto em português). Ao ser clicado, é aberto um *popup* para o usuário escolher a conta Google. Em caso de sucesso é chamada a função `onSuccess`; em caso de falha no próprio Google (por exemplo, o usuário fechou o *popup*) é chamada a função `onError`, que também exibe a mensagem de erro.

> ⚠️ Se o botão não for exibido ou o *popup* retornar o erro `origin_mismatch`, verifique se o endereço em que o front-end está sendo executado (ex.: `http://localhost:5173`) foi cadastrado em **Origens JavaScript autorizadas** no Google Cloud Console.

---

## 5. ✅ Resumindo o fluxo de autenticação social

1. O usuário acessa `/login` e clica no botão **Fazer login com o Google**.
2. O Google autentica o usuário (em um *popup*) e devolve para a aplicação um **ID Token** (`credential`).
3. A função `onSuccess` repassa o ID Token para a função `handleLoginSocial` do **AuthContext**.
4. `handleLoginSocial` envia o ID Token para a API (`POST /auth-social`, *header* `Auth-Id-Token`).
5. A API valida o ID Token junto ao Google, cadastra o usuário caso seja o seu primeiro acesso (com a permissão `ROLE_USER`) e devolve o **JWT da API** e os dados do usuário.
6. O cliente armazena o JWT e o usuário no `localStorage`, configura o *header* `Authorization` do Axios e redireciona para a página inicial.
7. A partir daqui, o controle de acesso segue exatamente como na aula03: o **RequireAuth** valida as *roles* de cada rota e o **TopMenu** exibe os itens de acordo com `hasPermission`.

Dessa forma, a aplicação passa a oferecer duas formas de autenticação (usuário/senha e conta Google), ambas convergindo para o mesmo mecanismo de autenticação e autorização baseado no JWT emitido pela API.
