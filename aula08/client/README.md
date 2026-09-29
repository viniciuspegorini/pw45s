# Criando uma Aplicação React com TypeScript utilizando Vite

Neste projeto será desenvolvida a camada de visualização (*front-end*) da solução web proposta na disciplina PW44S - Programação para Web - Turma 4SI da Universidade Tecnológica Federal do Paraná - Campus Pato Branco. A aplicação web será desenvolvida utilizando a biblioteca React com auxílio de algumas bibliotecas.

## <img src="https://raw.githubusercontent.com/marwin1991/profile-technology-icons/refs/heads/main/icons/react.png" alt="UTFPR Logo" width="20" /> React

O React é uma biblioteca JavaScript para criar interfaces de usuário. O React é **declarativo** fazendo com que a criação de interfaces de usuários (UIs) interativas seja uma tarefa fácil. Uma das premissas do React é que o desenvolvedor crie *views* simplificadas para cada estado da aplicação, e o React irá atualizar e renderizar de forma eficiente apenas os componentes necessários na medida em que os dados mudam. A criação de *views* declarativas fazem com que seu código seja mais previsível e simples de depurar [[1](#Referências)].

O React assim como outras bibliotecas e *frameworks* JavaScript é baseado em componentes. O que facilita gerenciar de maneira mais eficaz os diferentes módulos da aplicação. O React permite criar componentes encapsulados que gerenciam seu próprio estado, para então combina-lo para formar UIs complexas.

No React toda lógica do componente é escrita em JavaScript ou TypeScript e não em *templates*, permitindo ao desenvolvedor passar diversos tipos de dados ao longo da sua aplicação e ainda manter o estado da aplicação fora do DOM.

## O JSX - JavaScript XML ou JavaScript Syntax Extension

O React utiliza JSX (**JavaScript XML ou JavaScript Syntax Extension**) ou TSX (**TypeScript + JSX**)  para a criação dos componentes, que é uma extensão de sintaxe para JavaScript. O **JSX** é uma extensão de sintaxe para JavaScript que permite escrever elementos de interface de usuário de forma declarativa em um formato similar ao HTML, diretamente dentro do código JavaScript. 

Apesar do JSX ser semelhante, não é HTML, por exemplo, o  JSX parece com HTML, mas é renderizado como elementos do React:
```jsx 
const element = <h1>Hello, world!</h1>;
```
Permite integração com JavaScript. É possível inserir expressões JavaScript dentro do JSX usando `{}`. Exemplo:
```jsx
const name = "João";
const element = <h1>Olá, {name}!</h1>;
```
E no final é Transpilado para JavaScript. O JSX não é entendido nativamente pelos navegadores, então ele é convertido em JavaScript puro (geralmente pelo Babel ou TypeScript). Por exemplo, o código:
```jsx
//JSX
const element = <h1>Olá!</h1>;
```
É transpilado para:
```js
//JS
const element = React.createElement('h1', null, 'Olá!');
```


## 📦 Requisitos

Antes de começar, certifique-se de que você tem os seguintes itens instalados:
- IDE: 
	- <img width="20" src="https://raw.githubusercontent.com/marwin1991/profile-technology-icons/refs/heads/main/icons/visual_studio_code.png" alt="Visual Studio Code" title="Visual Studio Code"/> [Visual Studio Code](https://code.visualstudio.com/) ou 
	- <img width="20" src="https://raw.githubusercontent.com/marwin1991/profile-technology-icons/refs/heads/main/icons/webstorm.png" alt="WebStorm" title="WebStorm"/> [Web Storm](https://www.jetbrains.com/webstorm/)
- <img src="https://raw.githubusercontent.com/marwin1991/profile-technology-icons/refs/heads/main/icons/node_js.png" alt="Node" width="20" /> [Node.js](https://nodejs.org/) (versão 18 ou superior)
- <img src="https://raw.githubusercontent.com/marwin1991/profile-technology-icons/refs/heads/main/icons/npm.png" alt="Npm" width="20" /> [npm](https://www.npmjs.com/) instalado

## 🛠️ Criação do Projeto com Vite

### 1. <img width="20" src="https://raw.githubusercontent.com/marwin1991/profile-technology-icons/refs/heads/main/icons/vite.png" alt="Vite" title="Vite"/> Vite

Para criação do projeto será utilizado o Vite [2], que é uma ferramenta de construção para projetos web que visa fornecer uma experiência de desenvolvimento mais rápida e com uma quantidade menor de arquivos e configurações necessárias.
Para criar o projeto basta ter instalado no computador o **node.js** versão **18 ou superior**, então basta executar no terminal o comando:

```bash
npm create vite@latest client -- --template react-ts
```

Em que o nome do projeto será: **client**, a biblioteca: **React** e a linguagem de programação: **TypeScript**.

### 2. Acessar a pasta do projeto

Acessando a pasta:

```bash
cd client
```

### 3. Instalar as dependências

Para instalar as dependências:
```bash
npm install
```

### 4. Iniciar o servidor de desenvolvimento

Para iniciar a aplicação basta executar:

```bash
npm run dev
```

O terminal irá gerar uma resposta semelhante a essa:
```cmd
PS C:\dev\client> npm run dev

> client@0.0.0 dev
> vite

  VITE v6.4.1  ready in 279 ms

  ➜  Local:   http://127.0.0.1:5173/
  ➜  Network: use --host to expose
```
Na mensagem gerada é possível visualizar que a aplicação foi iniciada e está sendo executada na porta **5173**. Portanto, para testar a aplicação em um navegador basta acessar o endereço: [http://127.0.0.1:5173/](http://127.0.0.1:5173/)  ou [http://localhost:5173/](http://localhost:5173/). A página que irá abrir possui um botão que incrementa um contador a cada clique, essa página será alterada durante o desenvolvimento do projeto. Uma característica importante do projeto criado, é que ao alterarmos qualquer item do código **JSX** do componente **App.tsx** ele será automaticamente atualizado no navegador ao salvar o arquivo, não necessitando reiniciar o servidor toda vez que uma nova alteração é feita no código.

#### 🧪 Scripts disponíveis
-   `dev` - Inicia o servidor de desenvolvimento.
    
-   `build` - Gera a versão de produção do projeto.
    
-   `preview` - Visualiza localmente a *build* de produção.


#### 🗂️ Estrutura inicial
```
client/
├── public/
├── src/
│   ├── assets/
│   ├── App.tsx, App.css
│   ├── index.css
│   ├── main.tsx
│   └── vite-env.d.ts
├── eslint.config.js
├── index.html
├── package.json
├── tsconfig.json
└── vite.config.ts
```

Após finalizado esse processo abrir a pasta do projeto (*client*) no editor ou IDE que será utilizado no desenvolvimento do projeto.

Entendendo a estrutura do projeto criado:
- **node_modules**: pasta com as dependências do projeto.
- **public**: pasta com os arquivos públicos da aplicação, no caso do projeto apenas uma imagem.
- **src**: pasta com os arquivos de código do projeto, componentes, services, folhas de estilo, entre outros.
	-   **main.tsx**: esse arquivo declara a renderização da aplicação utilizando a biblioteca _react-dom_. Esse arquivo não precisa ser modificado, ele serve de _entrypoint_ da aplicação e por isso deve ter a configuração mínima para tal.
	- **App.tsx**: é um componente componente React que vem criado no projeto inicial. Ao abrir o arquivo, é possível observar que existe código JSX dentro dele. Esse código é retornado pelo método _render()_, que é padrão de todos os objetos  _Component_  do React. Ele retorna a representação HTML daquele componente que será exibida no navegador.
	- **App.css**: é o arquivo com os estilos CSS para o componente _App.js_.
- **index.html**: é o único arquivo HTML da aplicação e é obrigatório. É onde todos os componentes da aplicação serão renderizados. Não será necessário modificá-lo e deve ser mantida a div _root_, pois o conteúdo dinâmico da aplicação será exibido dentro dessa div.
 - **eslint.config.js**: é usado para configurar o **ESLint**, que é uma ferramenta de análise estática que identifica e corrige problemas no código JavaScript ou TypeScript. Serve para garantir que o código escrito seja consistente, siga boas práticas e evite erros.
- **package.json**:  é um dos principais arquivos do projeto, serve como um **manifesto** que descreve o projeto e gerencia suas dependências, scripts e metadados. Permite definir as informações de identificação do Projeto. Gerenciamento de Dependências. Definição de Scripts para iniciar o projeto, executar a rotina de testes, realizar o *build* da aplicação.
- **tsconfig.json** (*.app.json e *.node.json): Esses arquivos especificam como o TypeScript deve compilar o código. Isso inclui opções como: definição de diretórios de entrada e saída; **Target do JavaScript, por exemplo, `"target": "ES6"` para compilar o código TypeScript em JavaScript compatível com ES6; Integração com JSX do React, entre outras opções.
- **vite.config.ts**: define as configurações para o Vite, incluindo opções de construção, *plugins* e comportamento do servidor de desenvolvimento.


## ⌨️Desenvolvimento da aplicação

### 1. ⚒️ Ajustando a estrutura inicial do projeto

Antes de iniciar o desenvolvimento do projeto serão removidos alguns conteúdos presentes no projeto padrão criado com Vite e alguns ajustes no CSS. Ajustar o arquivo **index.css**, que ficará com o seguinte conteúdo:

```css
:root {
  font-family: system-ui, Avenir, Helvetica, Arial, sans-serif;
  line-height: 1.5;
  font-weight: 400;  
}

body {
  margin: 0;
  display: flex;
  place-items: center;
  min-width: 320px;
  min-height: 100vh;
}
```

Ajustar o arquivo **App.css**, que ficará com o seguinte conteúdo:

```css
#root {
  max-width: 1280px;
  margin: 0 auto;
  padding: 2rem;
  text-align: center;
}
```

Ajustar o arquivo **App.tsx**, que ficará com o seguinte conteúdo:
```jsx
import './App.css'

function App() {

  return (
    <>
      <h1>Bem vindo!</h1>
    </>
  )
}

export default App
```

Agora que foram removidos os trechos de código do projeto padrão podemos adicionar as bibliotecas que serão utilizadas no desenvolvimento do projeto.

---
### 2. 🎨 Bibliotecas - instalação e configuração

As bibliotecas para React permitem adicionar funcionalidades extras para aplicação, por meio de soluções prontas e testadas. Permite também o desenvolvimento mais rápido uma vez que soluções para requisições HTTP, validação de formulários, rotas, componentes de UI já estão prontas para uso dentro da aplicação. Neste projeto serão utilizadas as bibliotecas:
-   `PrimeReact` - Componentes de interface de usuário (UI).    
-   `Prime Icons` - Biblioteca com conjunto de ícones.
 -  `Prime Flex` - CSS responsivo.    
-   `React Router` - Gerenciamento de rotas da aplicação.
-   `Axios` - Cliente HTTP, utilizado nas chamadas à API REST.
-   `React Hook Form` - Gerenciamento dos formulários da aplicação.
-   `@types/node` - Definições de tipo para muitas bibliotecas node.1


#### 2.1 <img src="https://primefaces.org/cdn/primereact/images/favicon.ico" width="20px" /> PrimeReact

Instalando a biblioteca:

```bash
npm install primereact
```
Ajustando o componente main.tsx, adicionando o **PrimeReactProvider**:

```jsx
import { StrictMode } from "react";
import { createRoot } from "react-dom/client";
import "./index.css";
import App from "./App.tsx";

import { PrimeReactProvider } from "primereact/api";

createRoot(document.getElementById("root")!).render(
  <StrictMode>
    <PrimeReactProvider>
      <App />
    </PrimeReactProvider>
  </StrictMode>
);
```

#### 2.2 Prime Icons

Instalando a biblioteca:
```bash
npm install primeicons
```
Ajustando o componente main.tsx, basta adicionar o CSS:
```jsx
//...
import 'primeicons/primeicons.css'

createRoot(document.getElementById("root")!).render(
//...
);
```

#### 2.3 Prime Flex

Instalando a biblioteca:
```bash
npm install primeflex
```
Ajustando o componente main.tsx, basta adicionar o CSS:
```jsx
//...
import 'primeflex/primeflex.css';

createRoot(document.getElementById("root")!).render(
//...
);
```

#### 2.4 React Router

Instalando a biblioteca:
```bash
 npm install react-router-dom
```
Ajustando o componente main.tsx, basta adicionar o **BrowserRouter**:
```jsx
import { StrictMode } from "react";
import { createRoot } from "react-dom/client";
import "./index.css";
import App from "./App.tsx";
import { PrimeReactProvider } from "primereact/api";
import "primereact/resources/themes/lara-light-indigo/theme.css"; //theme
import "primereact/resources/primereact.min.css"; //core css

import { BrowserRouter } from "react-router-dom";

createRoot(document.getElementById("root")!).render(
  <StrictMode>
    <BrowserRouter>
      <PrimeReactProvider>
        <App />
      </PrimeReactProvider>
    </BrowserRouter>
  </StrictMode>
);
```

#### 2.5 Axios

Instalando a biblioteca:
```bash
 npm install axios
```
No momento nenhuma configuração adicional será necessária para o funcionamento do Axios.

#### 2.6 React Hook Form

Instalando a biblioteca:
```bash
npm install react-hook-form
```
No momento nenhuma configuração adicional será necessária para o funcionamento da biblioteca React Hook Form.

#### 2.7 @types/node

Continuando a configuração do projeto, agora será otimizado o processo de importar os componentes na aplicação, instalando a dependência de desenvolvimento *@types/node*.
```cmd
npm i --include=dev @types/node
```
O próximo passo será configurar o aquivo **vite.config.ts** que irá ficar com o seguinte conteúdo:
```ts
import path from 'path'
import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

// https://vitejs.dev/config/
export default defineConfig({
    plugins: [react()],
    resolve: {
        alias: {
            '@': path.resolve(__dirname, 'src')
        }
    }
})
```
Assim, todos a importação de componentes agora, pode ser realizadas a partir do diretório raiz, desde que o caminho usado no *import* inicie-se por '@'. Para finalizar basta configurar o arquivo **tsconfig.app.json** adicionando o conteúdo abaixo na propriedade *compilerOptions*:
```json
...
"compilerOptions": {
    ... manter o restante do código anterior, apenas adicionando o trecho abaixo
    "baseUrl": ".",
    "paths": {
        "@/*": ["./src/*"]
    }
}
... manter o restante do código...
```

---

### 3. 🏠Criando o componente para página Home

O componente **HomePage** será o primeiro componente a ser criado na aplicação. Esse componente será criado dentro da pasta **/src/pages/home**, essa estrutura de pastas não existe e deverá ser criada. Dentro da pasta **/home** deverá ser criado o arquivo **index.tsx**, com o seguinte conteúdo:
```jsx
export const HomePage = () => {
  return (
    <>
      <h1>Bem-vindo à Página Inicial</h1>
    </>
  );
};
```

Com o componente **HomePage** criado, agora é necessário exibi-lo para os usuários, para isso será necessário alterar o conteúdo co componente **App.tsx** com o código:
```jsx
import './App.css'
import { HomePage } from './pages/home'

function App() {
  return (
    <>
      <HomePage />
    </>
  )
}
export default App
```

Com o primeiro componente criado, agora o processo será repetido, criando os componentes para cadastro de usuário e autenticação. O React permite exibir um componente a cada momento ou fazer uma pilha de componentes, mas como cada componente que será criado irá representar uma funcionalidade específica da aplicação, será necessário controlar a exibição dos componentes de acordo com as rotas (URL) solicitadas pelo usuário, por exemplo, ao acessar o endereço [http://localhost:5473/login ](http://localhost:5473/login), por exemplo, será exibida a página de autenticação, ao ao acessar o endereço [http://localhost:5473/](http://localhost:5473/) será exibido o componente HomePage.

O próximo passo será criar o componente para cadastro de usuário, mas antes disso será necessário configurar a biblioteca Axios para que possam ser realizadas as requisições HTTP para a API.

### 4.  🌎Configuração do Axios para as requisições HTTP

Para realizar as requisições HTTP para API será utilizado o cliente HTTP Axios, a configuração consiste em definir uma URL padrão para API, que será adicionada no arquivo **/src/lib/axios.ts**. A API desenvolvida com Spring Framework está sendo executada no endereço `http://localhost:8080` caso a porta da API tenha sido alterada na aplicação **server**, também deverá ser alterada nesse trecho de código.
```ts
import axios from 'axios';

export const api = axios.create({
    baseURL: 'http://localhost:8080',
});
```

Com o Axios configurado agora os *services* que precisam fazer requisições HTTP para API podem ser configurados.

### 5.  🧑Cadastro de usuário

O cadastro de usuários será composto pela página de cadastro de usuários, representada pelo componente **RegisterPage**. A definição do tipo de objeto que será enviado para API (**IUserRegister**) e pela camada de serviço que será responsável por realizar a requisição HTTP para a API (**AuthService**).

#### 5.1 Interfaces IUserRegister e IResponse

As interfaces **IUserRegister** e **IResponse** serão definidas no arquivo **/src/commons/types.ts** (o arquivo e estrutura de pasta deverão ser criados) com os atributos necessários para o cadastro de um novo usuário na API, conforme o código:

```ts
export interface IUserRegister {
    displayName: string;
    username: string;
    password: string;
}

export interface IResponse {
    status?: number;
    success?: boolean;
    message?: string;
    data?: object
}
```
A interface **IResponse** representa a reposta da requisição HTTP enviada pelo servidor e será utilizada dentro dos componente para recuperar o *status* da requisição e também os dados enviados pelo servidor, por meio da propriedade *data*.

#### 5.2 Service AuthService

As chamadas HTTP serão realizadas por meio de *services* com o objetivo de deixar o código mais organizado e de fácil manutenção. Para isso, será criada a pasta **/src/services/** e dentro o arquivo **auth-service.ts**. No arquivo *auth-service.ts* será implementado a função assíncrona *signup()* que será responsável por realizar um HTTP POST para API com um objeto JSON representando um usuário.
```ts
import type { IUserRegister, IResponse } from "@/commons/types";
import { api } from "@/lib/axios";

/**
 * Função para realizar uma requisição HTTP para API para cadastrar um novo usuário
 * @param user - Dados do usuário que será cadastrado do tipo IUserRegister
 * @returns - Retorna a resposta da API
 */
const signup = async (user: IUserRegister): Promise<IResponse> => {
  let response = {} as IResponse;
  try {
    const data = await api.post("/users", user);
    response = {
      status: 200,
      success: true,
      message: "Usuário cadastrado com sucesso",
      data: data.data,
    };  
  } catch (err: any) {
    response = {
      status: 400,
      success: false,
      message: "Usuário não pode ser cadastrado",
      data: err.response.data,
    };
  }
  return response;
};
const AuthService = {
  signup,
};
export default AuthService;
```

Definindo a função com *async*/*await* podemos lidar com **código assíncrono**, uma vez que a requisição HTTP não irá ocorrer de maneira instantânea. O `async` transforma uma função em uma Promise automaticamente. E, o `await` pausa a execução da função até a Promise ser resolvida.

Para resolver o problema de validação do *eslint* nos objetos do tipo *any* vamos deixar global a configuração `@typescript-eslint/no-explicit-any`, no arquivo *.eslintrc** que está na pasta raiz, ajustar:
```json
rules: {
    "@typescript-eslint/no-explicit-any": "off"
},
```

#### 5.2 RegisterPage

O componente **RegisterPage** será criado dentro da pasta **/src/pages/register**, essa estrutura de pastas não existe e deverá ser criada. Dentro da pasta **/register** deverá ser criado o arquivo **index.tsx**. O formulário irá utilizar os componentes de UI do PrimeReact e React Hook Forma para controlar o estado do formulário.
```jsx
import { useForm, Controller } from "react-hook-form";
import { InputText } from "primereact/inputtext";
import { Password } from "primereact/password";
import { Button } from "primereact/button";
import { Card } from "primereact/card";
import { Link, useNavigate } from "react-router-dom";
import { classNames } from "primereact/utils";
import { useRef, useState } from "react";
import type { IUserRegister } from "@/commons/types";
import AuthService from "@/services/auth-service";
import { Toast } from "primereact/toast";

export const RegisterPage = () => {
  const {
    control,
    handleSubmit,
    formState: { errors, isSubmitting },
  } = useForm<IUserRegister>({
    defaultValues: { username: "", password: "", displayName: "" },
  }); // Formulário controlado com React Hook Form
  const { signup } = AuthService; // Método no authService que realiza uma requisição HTTP POST para /users na API
  const [loading, setLoading] = useState(false); // Objeto que controla o estado da requisição HTTP
  const navigate = useNavigate(); // Hook do React Touter para redirecionar o usuário para uma nova rota
  const toast = useRef<Toast>(null); // Hook para possibilitar o uso do componente Toast para exibir as mensagens de sucesso ou erro.

  const onSubmit = async (data: IUserRegister) => { // Função assíncrona para realizar o envio dos dados para API
    setLoading(true);
    try {
      const response = await signup(data);
      if (response.status === 200 && response.data) {
        toast.current?.show({
          severity: "success",
          summary: "Sucesso",
          detail: "Usuário cadastrado com sucesso.",
          life: 3000,
        });
        setTimeout(() => {
          navigate("/login");
        }, 1000);
      } else {
        toast.current?.show({
          severity: "error",
          summary: "Erro",
          detail: "Falha ao cadastrar usuário.",
          life: 3000,
        });
      }
    } catch {
      toast.current?.show({
        severity: "error",
        summary: "Erro",
        detail: "Falha ao cadastrar usuário.",
        life: 3000,
      });
    } finally {
      setLoading(false);
    }
  };
  return (
    <div className="flex justify-center items-start pt-30 px-4 surface-ground">
      <Toast ref={toast} />
      <Card title="Registrar Conta" className="w-full max-w-md">
        <form onSubmit={handleSubmit(onSubmit)} className="p-fluid space-y-4">
          <div>
            <label className="block mb-2">Nome de Exibição</label>
            <Controller
              name="displayName"
              control={control}
              rules={{ required: "Campo obrigatório" }}
              render={({ field }) => (
                <InputText
                  {...field}
                  className={classNames({ "p-invalid": errors.displayName })}
                  placeholder="Ex: João das Neves"
                />
              )}
            />
            {errors.displayName && (
              <small className="p-error">{errors.displayName.message}</small>
            )}
          </div>
          <div>
            <label className="block mb-2">Usuário</label>
            <Controller
              name="username"
              control={control}
              rules={{ required: "Campo obrigatório" }}
              render={({ field }) => (
                <InputText
                  {...field}
                  className={classNames({ "p-invalid": errors.username })}
                  placeholder="Ex: jsnow"
                />
              )}
            />
            {errors.username && (
              <small className="p-error">{errors.username.message}</small>
            )}
          </div>
          <div>
            <label className="block mb-2">Senha</label>
            <Controller
              name="password"
              control={control}
              rules={{
                required: "Campo obrigatório",
                minLength: { value: 6, message: "Mínimo 6 caracteres" },
              }}
              render={({ field }) => (
                <Password
                  {...field}
                  toggleMask
                  feedback={false}
                  className={classNames({ "p-invalid": errors.password })}
                />
              )}
            />
            {errors.password && (
              <small className="p-error">{errors.password.message}</small>
            )}
          </div>
          <Button
            type="submit"
            label="Registrar"
            loading={loading || isSubmitting}
            disabled={loading || isSubmitting}
            className="w-full mt-3"
          />
          <div className="text-center mt-3">
            <small>
              Já tem uma conta?{" "}
              <Link to="/login" className="text-primary">
                Fazer login
              </Link>
            </small>
          </div>
        </form>
      </Card>
    </div>
  );
};
```
Para verificar e testar o componente criado, podemos editar o componente **App.tsx** com o seguinte conteúdo:
```jsx
import "./App.css";
import { RegisterPage } from "./pages/register";

function App() {
  return (
    <>
        <RegisterPage />
    </>
  );
}
export default App;
```
---

### 6. 🔐 Autenticação

Com o processo de criação de um novo usuário finalizado, o próximo passo é permitir a autenticação desse usuário na aplicação, para isso será criado o componente **LoginPage** que vai conter o formulário para o usuário informar os dados de **username** e **password**. 

#### 6.1 Atualizando o AuthService e type.ts com a lógica para Autenticação

Será necessário criar a interface **IUserLogin** para representar o objeto que será enviado para API, essa alteração será realizada no arquivo */src/commons/types.ts*:

```ts
//...
export interface IUserLogin {
    username: string;
    password: string;
}
```

Agora será realizada a alteração do arquivo auth-service.ts adicionando a função responsável por realizar a requisição HTTP POST contendo um JSON com os atributos **username e password**:

```ts
import type { IUserRegister, IUserLogin, IResponse } from "@/commons/types";
import { api } from "@/lib/axios";

/**
 * Função para cadastrar um novo usuário
 * @param user - Dados do usuário que será cadastrado do tipo IUserRegister
 * @returns - Retorna a resposta da API
 */
const signup = async (user: IUserRegister): Promise<IResponse> => {
  let response = {} as IResponse;
  try {
    const data = await api.post("/users", user);
    response = {
      status: 200,
      success: true,
      message: "Usuário cadastrado com sucesso",
      data: data.data,
    };  
  } catch (err: any) {
    response = {
      status: 400,
      success: false,
      message: "Usuário não pode ser cadastrado",
      data: err.response.data,
    };
  }
  return response;
};

/**
 * Função para realizar a autenticação do usuário
 * @param user - Dados do usuário que será autenticado do tipo IUserLogin (username e password)
 * @returns - Retorna a resposta da API
 * Além disso salva o token no localStorage e adiciona o token no cabeçalho da requisição
 */
const login = async (user: IUserLogin) => {
  let response = {} as IResponse;
  try {
    const data = await api.post("/login", user);
    response = {
      status: 200,
      success: true,
      message: "Login bem-sucedido",
      data: data.data,
    };
  } catch (err: any) {
    response = {
      status: 401,
      success: false,
      message: "Usuário ou senha inválidos",
      data: err.response.data,
    };
  }
  return response;
};

const AuthService = {
  signup,
  login,
};
export default AuthService;
```

Com a camada responsável por realizar as requisições HTTP implementada será possível criar o componente responsável por exibir o formulário de entrada de dados na pasta **/src/pages/login/** será criado o arquivo **index.tsx**:

```jsx
import { useRef, useState } from "react";
import { Controller, useForm } from "react-hook-form";
import { InputText } from "primereact/inputtext";
import { Password } from "primereact/password";
import { Button } from "primereact/button";
import { Card } from "primereact/card";
import { Link, useNavigate } from "react-router-dom";
import type { IUserLogin } from "@/commons/types";
import AuthService from "@/services/auth-service";
import { Toast } from "primereact/toast";

export const LoginPage = () => {
  const {
    control,
    handleSubmit,
    formState: { errors, isSubmitting },
  } = useForm<IUserLogin>({ defaultValues: { username: "", password: "" } });  
  const navigate = useNavigate();
  const { login } = AuthService;
  const toast = useRef<Toast>(null);
  const [loading, setLoading] = useState(false);

  const onSubmit = async (userLogin: IUserLogin) => {
    setLoading(true);
    try {
      const response = await login(userLogin);
      if (response.status === 200 && response.data) {                
        toast.current?.show({
          severity: "success",
          summary: "Sucesso",
          detail: "Login efetuado com sucesso.",
          life: 3000,
        });
        setTimeout(() => {
          navigate("/");
        }, 1000);
      } else {
        toast.current?.show({
          severity: "error",
          summary: "Erro",
          detail: "Falha ao efetuar login.",
          life: 3000,
        });
      }
    } catch {
      toast.current?.show({
        severity: "error",
        summary: "Erro",
        detail: "Falha ao efetuar login.",
        life: 3000,
      });
    } finally {
      setLoading(false);
    }
  };
  return (
    <div className="flex justify-content-center align-items-center min-h-screen p-4">
      <Toast ref={toast} />
      <Card title="Login" className="w-full sm:w-20rem shadow-2">
        <form
          onSubmit={handleSubmit(onSubmit)}
          className="flex flex-column gap-3"
        >
          <div>
            <label htmlFor="username" className="block mb-2">
              Usuário
            </label>
            <Controller
              name="username"
              control={control}
              rules={{ required: "Informe o nome de usuário" }}
              render={({ field }) => (
                <InputText
                  id="username"
                  {...field}
                  className={errors.username ? "p-invalid w-full" : "w-full"}
                />
              )}
            />
            {errors.username && (
              <small className="p-error">{errors.username.message}</small>
            )}
          </div>
          <div>
            <label htmlFor="password" className="block mb-2">
              Senha
            </label>
            <Controller
              name="password"
              control={control}
              rules={{ required: "Informe a senha" }}
              render={({ field }) => (
                <Password
                  id="password"
                  {...field}
                  toggleMask
                  feedback={false}
                  className={errors.password ? "p-invalid w-full" : "w-full"}
                  inputClassName="w-full"
                />
              )}
            />
            {errors.password && (
              <small className="p-error">{errors.password.message}</small>
            )}
          </div>
          <Button
            type="submit"
            label="Entrar"
            icon="pi pi-sign-in"
            className="w-full"
            loading={loading || isSubmitting}
            disabled={loading || isSubmitting}
          />
        </form>
        <div className="text-center mt-3">
          <small>
            Não tem uma conta?{" "}
            <Link to="/register" className="text-primary">
              Criar conta
            </Link>
          </small>
        </div>
      </Card>
    </div>
  );
};
```
Para verificar e testar o componente criado, podemos editar o componente **App.tsx** com o seguinte conteúdo:
```jsx
import "./App.css";
import { LoginPage} from "./pages/register";

function App() {
  return (
    <>
      <LoginPage />
    </>
  );
}
export default App;
```
---

### 7. 🔂 Criando o contexto da aplicação
Após termos autenticado o usuário será necessário utilizar o *token* JWT no cabeçalho das requisições HTTP para API que necessitam de autenticação. Para manter o usuário autenticado na aplicação será utilizada a React Context API e também será realizada uma alteração no objeto do axios.

A **React Context API** é uma ferramenta nativa do React que pode ser utilizada para compartilhar dados entre componentes sem precisar passar _props_ manualmente em cada nível da árvore de componentes. Neste projeto será utilizada pois vários componentes precisam acessar os dados de autenticação, mas também poderia ser utilizada para armazenar dados como temas ou configurações globais.

#### 7.1 Criando as interfaces para os dados de autenticação
Após autenticar o usuário a API envia como resposta o *token* de autenticação via objeto JSON, que será definido no arquivo  **/src/commons/types.ts**:

```ts
export interface Authorities {
  authority: string;
}

export interface AuthenticatedUser {
  displayName: string;
  username: string;
  authorities: Authorities[];
}

export interface AuthenticationResponse {
  token: string;
  user: AuthenticatedUser;
}
```
#### 7.2 Criando o contexto e *hook* de autenticação

Nessa etapa serão criados os arquivos **/src/context/AuthContext.tsx** e **/src/context/hooks/use-auth.ts**. O arquivo **AuthContext.tsx** terá o seguinte conteúdo:

```jsx
import { createContext, useEffect, useState } from "react";
import type { ReactNode } from "react";
import type { AuthenticatedUser, AuthenticationResponse } from "@/commons/types";
import { api } from "@/lib/axios";
import { useNavigate } from "react-router-dom";

interface AuthContextType {
  authenticated: boolean;
  authenticatedUser?: AuthenticatedUser;
  handleLogin: (authenticationResponse: AuthenticationResponse) => Promise<any>;
  handleLogout: () => void;
}

interface AuthProviderProps {
  children: ReactNode;
}

const AuthContext = createContext({} as AuthContextType);

export const AuthProvider = ({ children }: AuthProviderProps) => {
  const [authenticated, setAuthenticated] = useState(false);
  const [authenticatedUser, setAuthenticatedUser] =
    useState<AuthenticatedUser>();
  const navigate = useNavigate();

  useEffect(() => {
    const storedUser = localStorage.getItem("user");
    const storedToken = localStorage.getItem("token");

    if (storedUser && storedToken) {
      setAuthenticatedUser(JSON.parse(storedUser));
      setAuthenticated(true);
      api.defaults.headers.common["Authorization"] = `Bearer ${JSON.parse(
        storedToken
      )}`;
      navigate("/");
    }
  // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const handleLogin = async (
    authenticationResponse: AuthenticationResponse
  ) => {
    try {
      localStorage.setItem(
        "token",
        JSON.stringify(authenticationResponse.token)
      );
      localStorage.setItem("user", JSON.stringify(authenticationResponse.user));
      api.defaults.headers.common[
        "Authorization"
      ] = `Bearer ${authenticationResponse.token}`;

      setAuthenticatedUser(authenticationResponse.user);
      setAuthenticated(true);
    } catch {
      setAuthenticatedUser(undefined);
      setAuthenticated(false);
    }
  };

  const handleLogout = async () => {
    localStorage.removeItem("token");
    localStorage.removeItem("user");
    api.defaults.headers.common["Authorization"] = "";

    setAuthenticated(false);
    setAuthenticatedUser(undefined);
  };

  return (
    <AuthContext.Provider
      value={{ authenticated, authenticatedUser, handleLogin, handleLogout }}
    >
      {children}
    </AuthContext.Provider>
  );
};

export { AuthContext };
``` 

E o conteúdo do arquivo **use-auth.ts**:

```ts
import { AuthContext } from "@/context/AuthContext";
import { useContext } from "react";

export const useAuth = () => {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error("useAuth precisa estar dentro de AuthProvider");
  }
  return context;
};
```

Agora será necessário ajusta o **AuthProvider** no arquivo *main.tsx*:
```jsx
import { StrictMode } from "react";
import { createRoot } from "react-dom/client";
import "./index.css";

import App from "@/App.tsx";

import { PrimeReactProvider } from "primereact/api";
import { BrowserRouter } from "react-router-dom";

import "primereact/resources/themes/lara-light-indigo/theme.css"; //theme
import "primereact/resources/primereact.min.css"; //core css
import "primeicons/primeicons.css"; //icons
import "primeflex/primeflex.css"; //flex utilities
import { AuthProvider } from "@/context/AuthContext";

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

Com o **AuthProvider** configurado, o próximo passo será desenvolver o componente de autenticação.

#### 7.3 Utilizando o contexto dentro do componente LoginPage

```jsx
//...
import type { AuthenticationResponse, IUserLogin } from  "@/commons/types"; //adicionar a importação do AuthenticationResponse
import { useAuth } from  "@/context/hooks/use-auth"; // adicionar a importação do hook useAuth
//...
export  const  LoginPage  = () => {
//...
  const { handleLogin } =  useAuth(); // A função handleLogin será utilizada para atualizar o contexto com o usuário autenticado.
  const onSubmit = async (userLogin: IUserLogin) => {
    setLoading(true);
    try {
      const response = await login(userLogin);
      if (response.status === 200 && response.data) {
        const authenticationResponse = response.data as AuthenticationResponse; // Define o objeto com token após a autenticação
        handleLogin(authenticationResponse); // o contexto é atualizado com os dados da autenticação
        toast.current?.show({
          severity: "success",
          summary: "Sucesso",
          detail: "Login efetuado com sucesso.",
          life: 3000,
        });
        setTimeout(() => {
          navigate("/");
        }, 1000);
      } else {
        toast.current?.show({
          severity: "error",
          summary: "Erro",
          detail: "Falha ao efetuar login.",
          life: 3000,
        });
      }
    } catch {
      toast.current?.show({
        severity: "error",
        summary: "Erro",
        detail: "Falha ao efetuar login.",
        life: 3000,
      });
    } finally {
      setLoading(false);
    }
  };
//...	
```

Com isso o contexto da aplicação e o componente de autenticação estão criados e podemos iniciar a configuração das rotas da aplicação.

---
### 8. 🔗 Configurando as rotas da aplicação

Para controlar as rotas da aplicação será utilizada a biblioteca React Router [5]. Assim, de acordo com a URL informada no navegador um componente será renderizado para o usuário.

Como React Router já está instalado basta configurar as rotas da aplicação para os componentes que precisam de autenticação (CRUDs de categorias e produtos, que serão criados ainda e página Home) e os que não precisam (cadastro de usuários e autenticação, já criados). 

Para o funcionamento do React Router serão criados dois componentes o **RequireAuth** que será utilizado para filtrar os componentes que necessitam de autenticação para serem acessados e o **AppRoutes** que irá conter todas as rotas da aplicação, mantendo a relação entre as rotas e os componentes que devem ser renderizados pelo React. 

#### 8.1 Componente RequireAuth

Para verificar quais rotas necessitam de autenticação e melhorar a experiência do usuário será criado o componente  **RequireAuth** dentro de **/src/components/require-auth/index.tsx**:

```jsx
import { useContext } from "react";
import { useLocation, Navigate, Outlet } from "react-router-dom";
import { AuthContext } from "@/context/AuthContext";

export function RequireAuth() {
  const { authenticated } = useContext(AuthContext);
  const location = useLocation();

  return authenticated ? (
    <>
      <Outlet />
    </>
  ) : (
    <Navigate to="/login" state={{ from: location }} replace />
  );
}
```

#### 8.2 Componente AppRoutes

Criar o arquivo **/src/routes/app-routes/index.tsx** que irá conter o componente **AppRoutes**:

```jsx
import { Route, Routes } from "react-router-dom";
import { LoginPage } from "@/pages/login";
import { RegisterPage } from "@/pages/register";
import { HomePage } from "@/pages/home";
import { RequireAuth } from "@/components/require-auth";

export function AppRoutes() {
  return (
    <Routes>
      {/* public routes */}
      <Route path="login" element={<LoginPage />} />
      <Route path="register" element={<RegisterPage />} />

      {/* protected routes */}
      <Route element={<RequireAuth />}>
        <Route path="/" element={<HomePage />} />
        <Route path="/home" element={<HomePage />} />
      </Route>
    </Routes>
  );
}
```
---
### 9. ⚓ Criando os componentes de Menu e Layout

Para melhorar a navegação e usabilidade da aplicação será criado um menu superior (**TopMenu**) e um componente que irá ser o *layout* principal da aplicação (**Layout**).

#### 9.1 Componente TopMenu

O componente TopMenu irá conter o menu superior da aplicação com os links para os diferentes componentes (Home, Lista e Cadastro de Categoria, Lista e Cadastro de Produtos. Além de exibir a possibilidade de troca de tema (claro, escuro).

Antes do componente, será criado o arquivo **src/commons/theme.ts**, responsável por aplicar o tema do PrimeReact (claro ou escuro) na página. Os temas são carregados do próprio pacote **primereact** instalado no projeto: o sufixo `?url` faz o Vite retornar o endereço do arquivo CSS, que é utilizado em um elemento `<link>` no `<head>` da página. Dessa forma o tema sempre tem a mesma versão dos componentes e não depende de um servidor externo (CDN).

```ts
// O sufixo "?url" faz o Vite retornar o endereço do arquivo CSS (em vez de aplicá-lo na página).
// Os temas vêm do pacote primereact instalado, garantindo a mesma versão dos componentes.
import lightThemeUrl from "primereact/resources/themes/lara-light-blue/theme.css?url";
import darkThemeUrl from "primereact/resources/themes/lara-dark-blue/theme.css?url";

export type Theme = "light" | "dark";

const THEME_LINK_ID = "theme-link";
const THEME_STORAGE_KEY = "theme";

/**
 * Retorna o tema salvo no localStorage. Caso o usuário ainda não tenha escolhido um tema,
 * utiliza a preferência do sistema operacional.
 */
export const getSavedTheme = (): Theme => {
  const saved = localStorage.getItem(THEME_STORAGE_KEY);
  if (saved === "light" || saved === "dark") {
    return saved;
  }
  return window.matchMedia("(prefers-color-scheme: dark)").matches ? "dark" : "light";
};

/**
 * Aplica o tema na página, alterando o <link> do tema do PrimeReact, e salva a escolha no localStorage.
 */
export const applyTheme = (theme: Theme) => {
  let link = document.getElementById(THEME_LINK_ID) as HTMLLinkElement | null;
  if (!link) {
    link = document.createElement("link");
    link.id = THEME_LINK_ID;
    link.rel = "stylesheet";
    document.head.appendChild(link);
  }
  link.href = theme === "dark" ? darkThemeUrl : lightThemeUrl;
  localStorage.setItem(THEME_STORAGE_KEY, theme);
};
```

> ⚠️ Não utilize endereços de CDN sem versão, como `https://unpkg.com/primereact/resources/themes/...`: eles apontam sempre para a última versão publicada da biblioteca. A partir do PrimeReact 11 os temas deixaram de existir nessa pasta, e esses endereços passaram a retornar erro 404, fazendo com que a troca de tema deixasse de funcionar.

O componente **TopMenu** utiliza as funções `getSavedTheme` e `applyTheme` para exibir e alterar o tema:
```jsx
import React, { useEffect, useState } from "react";
import { Menubar } from "primereact/menubar";
import type { MenuItem } from "primereact/menuitem";
import { Avatar } from "primereact/avatar";
import { Button } from "primereact/button";
import { useNavigate } from "react-router-dom";
import { useAuth } from "@/context/hooks/use-auth";
import { InputSwitch } from "primereact/inputswitch";
import { applyTheme, getSavedTheme } from "@/commons/theme";

const TopMenu: React.FC = () => {
  const navigate = useNavigate();
  const user = "user@email.com";
  const [darkMode, setDarkMode] = useState<boolean>(() => getSavedTheme() === "dark");
  const { authenticated, handleLogout } = useAuth();

  useEffect(() => {
    applyTheme(darkMode ? "dark" : "light");
  }, [darkMode]);

  const handleLogoutClick = () => {
    handleLogout();
    navigate("/login");
  };

  const items: MenuItem[] = authenticated
    ? [
        { label: "Home", icon: "pi pi-home", command: () => navigate("/") },        
      ]
    : [];

  const start = (
    <div
      className="flex align-items-center gap-2 cursor-pointer"
      onClick={() => navigate("/")}
    >
      <img
        src="/assets/images/utfpr-logo-nb.png"
        alt="Logo"
        height={32}
        style={{ objectFit: "contain" }}
      />
      <span className="font-bold text-lg hidden sm:block">PW44S</span>
    </div>
  );

  const end = (
    <div className="flex align-items-center gap-3">
      <div className="flex items-center gap-2">
        <i
          className={`pi pi-sun ${
            darkMode ? "text-gray-400" : "text-yellow-500"
          }`}
          style={{ marginTop: "5px" }}
        />
        <InputSwitch
          checked={darkMode}
          onChange={(e) => setDarkMode(e.value ?? false)}
        />
        <i
          className={`pi pi-moon ${
            darkMode ? "text-blue-300" : "text-gray-400"
          }`}
          style={{ marginTop: "5px" }}
        />
      </div>

      {authenticated && (
        <>
          <span className="font-semibold hidden sm:block">{user}</span>
          <Avatar
            image="https://api.dicebear.com/9.x/bottts-neutral/svg?seed=Caleb"
            shape="square"
          />
          <Button
            icon="pi pi-sign-out"
            className="p-button-text"
            onClick={handleLogoutClick}
          />
        </>
      )}
    </div>
  );

  return (
    <div
      style={{
        position: "fixed",
        top: 0,
        left: 0,
        right: 0,
        width: "100%",
        zIndex: 1000,
        backgroundColor: "var(--surface-ground)",
        boxShadow: "0 2px 8px rgba(0,0,0,0.1)",
      }}
      className="fixed top-0 left-0 w-full z-50"
    >
      <Menubar model={items} start={start} end={end} />
    </div>
  );
};

export default TopMenu;
```

Agora é necessário ajustar o arquivo *main.tsx*: o *import* fixo do tema (`lara-light-indigo`) é removido, e o tema salvo pelo usuário é aplicado antes de a aplicação ser renderizada, para que todas as páginas (inclusive as que não exibem o menu, como a de *login*) utilizem o tema escolhido. O arquivo vai ficar com o seguinte conteúdo:
```jsx
import { StrictMode } from "react";
import { createRoot } from "react-dom/client";
import "./index.css";

import App from "@/App.tsx";

import { PrimeReactProvider } from "primereact/api";
import { BrowserRouter } from "react-router-dom";

import "primereact/resources/primereact.min.css"; //core css
import "primeicons/primeicons.css"; //icons
import "primeflex/primeflex.css"; //flex utilities
import { applyTheme, getSavedTheme } from "@/commons/theme";
import { AuthProvider } from "@/context/AuthContext";

// O tema (claro ou escuro) é aplicado antes de renderizar a aplicação, em todas as páginas
applyTheme(getSavedTheme());

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

Por fim, para que o fundo da página e a cor do texto também acompanhem o tema, no arquivo *index.css* o elemento `body` utiliza as variáveis CSS definidas pelos temas do PrimeReact:
```css
body {
  margin: 0;
  /* cores definidas pelo tema do PrimeReact (claro ou escuro) */
  background-color: var(--surface-ground);
  color: var(--text-color);
  display: flex;
  place-items: center;
  min-height: 100vh;
}
```

#### 9.2 Componente Layout

O componente de Layout será utilizado como base para todas as outras páginas da aplicação, podem ser adicionado componentes de menu, menu lateral, rodapé, entre outros. No caso da aplicação desenvolvida será adicionado apenas o menu superior. Dentro de **src/components/layout/index.tsx**.

```jsx
import { Outlet } from "react-router-dom";
import TopMenu from "@/components/top-menu";

export function Layout() {
  return (
    <>
      <TopMenu />
      <main style={{ paddingTop: "40px" }}>
        <Outlet />
      </main>
    </>
  );
}
```
O componente **Layout** será utilizado no componente **AppRoutes**, assim todas as rotas da aplicação exibirão os componentes seguindo as mesmas características.

```jsx
import { Route, Routes } from "react-router-dom";
import { LoginPage } from "@/pages/login";
import { RegisterPage } from "@/pages/register";
import { HomePage } from "@/pages/home";
import { RequireAuth } from "@/components/require-auth";
import { Layout } from "@/components/layout";

export function AppRoutes() {
  return (
    <Routes>
      <Route path="/" element={<Layout />}>
        {/* public routes */}
        <Route path="login" element={<LoginPage />} />
        <Route path="register" element={<RegisterPage />} />

        {/* protected routes */}
        <Route element={<RequireAuth />}>
          <Route path="/" element={<HomePage />} />
          <Route path="/home" element={<HomePage />} />
        </Route>
      </Route>
    </Routes>
  );
}
```

Agora e possível se cadastrar, autenticar e navegar pelos componentes da aplicação, o próximo passo será desenvolver os CRUDs de Categoria e Produto.

---
### 10. 🏷️ CRUD de Categorias

#### 10.1 Interface e *Service* de Categoria
O primeiro passo será criar a interface **ICategory**, que representa uma categoria. A interface será desenvolvida no arquivo **/src/commons/types.ts**, bastando adicionar ao arquivo:

```ts
//...
export  interface  ICategory {
    id?:  number;
    name:  string;
}
```
Agora será desenvolvido o *service* **CategoryService**, para que possam ser realizadas requisições HTTP para a API. Criar o arquivo **/src/services/category-service.ts**:

```ts
import type { ICategory, IResponse } from "@/commons/types";
import { api } from "@/lib/axios";

// URL base para as requisições de categoria
const categoryURL = "/categories";

/**
 * Função para salvar uma categoria
 * @param category - Dados da categoria que será salva
 * @returns - Retorna uma Promise com a resposta da API
 **/
const save = async (category: ICategory): Promise<IResponse> => {
  let response = {} as IResponse;
  try {
    const data = await api.post(categoryURL, category);
    response = {
      status: 200,
      success: true,
      message: "Categoria salva com sucesso!",
      data: data.data,
    };
  } catch (err: any) {
    response = {
      status: err.response.status,
      success: false,
      message: "Falha ao salvar categoria",
      data: err.response.data,
    };
  }
  return response;
};

/**
 * Função para buscar todas as categorias
 * @returns - Retorna uma Promise com a resposta da API
 * com a lista de categorias
 **/
const findAll = async (): Promise<IResponse> => {
  let response = {} as IResponse;
  try {
    const data = await api.get(categoryURL);
    response = {
      status: 200,
      success: true,
      message: "Lista de categorias carregada com sucesso!",
      data: data.data,
    };
  } catch (err: any) {
    response = {
      status: err.response.status,
      success: false,
      message: "Falha ao carregar a lista de categorias",
      data: err.response.data,
    };
  }
  return response;
};

/**
 * Função para remover uma categoria
 * @param id - Recebe o id da categoria que será removida
 * @returns - Retorna uma Promise com a resposta da API
 */
const remove = async (id: number): Promise<IResponse> => {
  let response = {} as IResponse;
  try {
    const data = await api.delete(`${categoryURL}/${id}`);
    response = {
      status: 200,
      success: true,
      message: "Categoria removida com sucesso!",
      data: data.data,
    };
  } catch (err: any) {
    response = {
      status: err.response.status,
      success: false,
      message: "Falha ao remover categoria",
      data: err.response.data,
    };
  }
  return response;
};

/**
 * Função para buscar uma categoria pelo id
 * @param id - Recebe o id da categoria que será buscada
 * @returns - Retorna uma Promise com a resposta da API
 */
const findById = async (id: number): Promise<IResponse> => {
  let response = {} as IResponse;
  try {
    const data = await api.get(`${categoryURL}/${id}`);
    response = {
      status: 200,
      success: true,
      message: "Categoria carregada com sucesso!",
      data: data.data,
    };
  } catch (err: any) {
    response = {
      status: err.response.status,
      success: false,
      message: "Falha ao carregar categoria",
      data: err.response.data,
    };
  }
  return response;
};

// Objeto que exporta todas as funções
const CategoryService = {
  save,
  findAll,
  remove,
  findById,
};

export default CategoryService;
```

#### 10.2  Lista de Categorias

Com a interface e *service* criados, podemos criar o componente que será utilizado para exibir a lista das categorias vindas da API. O componente **CategoryListPage** será criado dentro de **/src/pages/category-list/index.tsx**:

```jsx
import { useEffect, useRef, useState } from "react";
import { DataTable } from "primereact/datatable";
import { Column } from "primereact/column";
import { Button } from "primereact/button";
import type { ICategory } from "@/commons/types";
import CategoryService from "@/services/category-service";
import { useNavigate } from "react-router-dom";
import { Toast } from "primereact/toast";

export const CategoryListPage = () => {
  const [data, setData] = useState<ICategory[]>([]);
  const { findAll, remove } = CategoryService;
  const navigate = useNavigate();
  const toast = useRef<Toast>(null);

  // hook do react para executar ações ao carregar o componente
  // carrega a lista de categorias
  useEffect(() => {
    loadData();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  // função para carregar a lista de categorias
  const loadData = async () => {
    const response = await findAll();

    if (response.status === 200) {
      setData(Array.isArray(response.data) ? response.data : []);
    } else {
      toast.current?.show({
        severity: "error",
        summary: "Erro",
        detail: "Não foi possível carregar a lista de categorias.",
        life: 3000,
      });
    }
  };

  const handleEdit = (category: ICategory) => {
    navigate(`/categories/${category.id}`);
  };

  const handleDelete = async (category: ICategory) => {
    if (confirm(`Tem certeza que deseja excluir "${category.name}"?`)) {
      if (category.id) {
        try {
          await remove(category.id);
          setData((prev) => prev.filter((c) => c.id !== category.id));
          toast.current?.show({
            severity: "success",
            summary: "Sucesso",
            detail: "Registro removido com sucesso",
            life: 3000,
          });
        } catch {
          toast.current?.show({
            severity: "error",
            summary: "Erro",
            detail: "Não foi possível remover o registro.",
            life: 3000,
          });
        }
      }
    }
  };

  const actionTemplate = (rowData: ICategory) => (
    <div className="flex gap-2">
      <Button
        icon="pi pi-pencil"
        className="p-button-sm p-button-text"
        onClick={() => handleEdit(rowData)}
        tooltip="Editar"
      />
      <Button
        icon="pi pi-trash"
        className="p-button-sm p-button-text p-button-danger"
        onClick={() => handleDelete(rowData)}
        tooltip="Excluir"
      />
    </div>
  );

  return (
    <div className="card">
      <Toast ref={toast} />
      <h2 className="text-xl mb-3">Lista de Categorias</h2>
      <DataTable
        value={data}
        stripedRows
        emptyMessage="Nenhuma categoria encontrada."
      >
        <Column field="id" header="ID" style={{ width: "10%" }} />
        <Column field="name" header="Nome" />
        <Column body={actionTemplate} header="Ações" style={{ width: "20%" }} />
      </DataTable>
    </div>
  );
};
```
Para que o componente **CategoryListPage** seja exibido devemos adicionar a nova rota ao componente **AppRoutes**:
```jsx
//...
import { CategoryListPage } from "@/pages/category-list";

export function AppRoutes() {
//...
        {/* protected routes */}
        <Route element={<RequireAuth />}>
		  //...
          <Route path="/categories" element={<CategoryListPage />} />
        </Route>
//...
}
```
Para finalizar a lista de categorias será necessário adicionar o atalho para página ao componente **TopMenu**, ajustando apenas a contante *items* que armazena um *array* com os itens de menu que serão apresentados no topo da página:

```jsx
	/...
	const items: MenuItem[] = authenticated
    ? [
        { label: "Home", icon: "pi pi-home", command: () => navigate("/") },
        {
          label: "Categorias",
          icon: "pi pi-box",
          items: [
            {
              label: "Listar",
              icon: "pi pi-list",
              command: () => navigate("/categories"),
            },            
          ],
        },        
      ]
    : [];
   //...
```

#### 10.3  Cadastro e edição de Categorias
Com a interface, *service* e o componente de lista criados, podemos criar o componente que será utilizado para cadastro e edição de categorias. O componente **CategoryFormPage** será criado dentro de **/src/pages/category-form/index.tsx**:

```jsx
import { useEffect, useState, useRef } from "react";
import { useParams, useNavigate } from "react-router-dom";
import { Toast } from "primereact/toast";
import type { ICategory, IResponse } from "@/commons/types";
import CategoryService from "@/services/category-service";
import { Controller, useForm } from "react-hook-form";
import { InputText } from "primereact/inputtext";
import { Button } from "primereact/button";

export const CategoryFormPage = () => {
  const { id } = useParams<{ id: string }>();
  const [loading, setLoading] = useState(false);
  const [category, setCategory] = useState<ICategory | undefined>(undefined);
  const {
    control,
    handleSubmit,
    reset,
    formState: { errors, isSubmitting },
  } = useForm<ICategory>({
    defaultValues: { name: "" },
  });
  const toast = useRef<Toast>(null);
  const navigate = useNavigate();
  const { findById, save } = CategoryService;

  const isEdit = !!id;

  useEffect(() => {
    loadCategory();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [id]);

  const loadCategory = async () => {
    if (isEdit) {
      setLoading(true);
      const response = (await findById(parseInt(id!))) as IResponse;
      try {
        if (response.status === 200) {
          setCategory(response.data as ICategory);
          reset(response.data as ICategory);
        } else {
          toast.current?.show({
            severity: "error",
            summary: "Erro",
            detail: "Falha ao carregar o registro.",
            life: 3000,
          });
        }
      } catch {
        setCategory(undefined);
      } finally {
        setLoading(false);
      }
    }
  };

  const onSubmit = async (data: ICategory) => {
    setLoading(true);
    try {
      const response = await save(data);
      if (
        (response.status === 201 || response.status === 200) &&
        response.data
      ) {
        toast.current?.show({
          severity: "success",
          summary: "Sucesso",
          detail: "Categoria salva com sucesso.",
          life: 3000,
        });
        setTimeout(() => {
          navigate("/categories");
        }, 1000);
      } else {
        toast.current?.show({
          severity: "error",
          summary: "Erro",
          detail: "Não foi possível salvar o registro.",
          life: 3000,
        });
      }
    } catch {
      toast.current?.show({
        severity: "error",
        summary: "Erro",
        detail: "Não foi possível salvar o registro.",
        life: 3000,
      });
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="container mx-auto px-4 pt-24 max-w-xl">
      <Toast ref={toast} />
      <h2 className="text-2xl mb-4">
        {isEdit ? "Editar Categoria" : "Nova Categoria"}
      </h2>
      {!isEdit || category ? (
        <form onSubmit={handleSubmit(onSubmit)} className="space-y-4 p-fluid">
          <div>
            <label htmlFor="name" className="block mb-2">
              Nome da Categoria
            </label>
            <Controller
              name="name"
              control={control}
              rules={{ required: "O nome é obrigatório" }}
              render={({ field }) => (
                <InputText
                  id="name"
                  {...field}
                  placeholder="Digite o nome da categoria"
                />
              )}
            />
            {errors.name && (
              <small className="p-error">{errors.name.message}</small>
            )}
          </div>
          <div className="flex justify-end gap-2 mt-4">
            <Button
              type="button"
              label="Cancelar"
              className="p-button-secondary"
              onClick={() => navigate("/categories")}
              loading={loading || isSubmitting}
              disabled={loading || isSubmitting}
            />
            <Button
              type="submit"
              label={isEdit ? "Atualizar" : "Salvar"}
              loading={loading || isSubmitting}
              disabled={loading || isSubmitting}
            />
          </div>
        </form>
      ) : (
        <p>Carregando...</p>
      )}
    </div>
  );
};
```

Para que o componente **CategoryListPage** seja exibido devemos adicionar a nova rota ao componente **AppRoutes**:
```jsx
//...
import { CategoryListPage } from "@/pages/category-list";
import { CategoryFormPage } from  "@/pages/category-form";

export function AppRoutes() {
//...
        {/* protected routes */}
        <Route element={<RequireAuth />}>
		      //...
          <Route path="/categories" element={<CategoryListPage />} />
          <Route  path="/categories/new"  element={<CategoryFormPage  />}  />
		      <Route  path="/categories/:id"  element={<CategoryFormPage  />}  />
        </Route>
//...
}
```
Para finalizar o cadastro de categorias será necessário adicionar o atalho para página ao componente **TopMenu**, ajustando apenas a contante *items* que armazena um *array* com os itens de menu que serão apresentados no topo da página:

```jsx
	/...
	const items: MenuItem[] = authenticated
    ? [
        { label: "Home", icon: "pi pi-home", command: () => navigate("/") },
        {
          label: "Categorias",
          icon: "pi pi-box",
          items: [
            {
              label: "Listar",
              icon: "pi pi-list",
              command: () => navigate("/categories"),
            },
            {
              label: "Novo",
              icon: "pi pi-plus",
              command: () => navigate("/categories/new"),
            },                        
          ],
        },        
      ]
    : [];
   //...
```

Com o componente **CategoryFormPage** criado finalizamos o CRUD de categorias. O próximo passo será o desenvolvimento do CRUD de produtos.

---
### 11. 📱CRUD de Produtos

#### 11.1 Interface e *Service* de Produto
O primeiro passo será criar a interface **IProduct **, que representa uma produto. A interface será desenvolvida no arquivo **/src/commons/types.ts**, bastando adicionar ao arquivo:
```ts
//...
export interface IProduct {
  id?: number;
  name: string;
  description: string;
  price: number;
  category: ICategory;
  imageName?: string;
  contentType?: string;
}
```
Agora será desenvolvido o *service* **ProductService**, para que possam ser realizadas requisições HTTP para a API. Criar o arquivo **/src/services/product-service.ts**:

```ts
import type { IProduct, IResponse } from "@/commons/types";
import { api } from "@/lib/axios";

// URL base para as requisições de produtos
const categoryURL = "/products";

/**
 * Função para salvar um produto
 * @param category - Dados do produto que será salvo
 * @returns - Retorna uma Promise com a resposta da API
 **/
const save = async (category: IProduct): Promise<IResponse> => {
  let response = {} as IResponse;
  try {
    const data = await api.post(categoryURL, category);
    response = {
      status: 200,
      success: true,
      message: "Produto salvo com sucesso!",
      data: data.data,
    };
  } catch (err: any) {
    response = {
      status: err.response.status,
      success: false,
      message: "Falha ao salvar produto",
      data: err.response.data,
    };
  }
  return response;
};

/**
 * Função para buscar todos os produtos
 * @returns - Retorna uma Promise com a resposta da API
 * com a lista de produtos
 **/
const findAll = async (): Promise<IResponse> => {
  let response = {} as IResponse;
  try {
    const data = await api.get(categoryURL);
    response = {
      status: 200,
      success: true,
      message: "Lista de produtos carregada com sucesso!",
      data: data.data,
    };
  } catch (err: any) {
    response = {
      status: err.response.status,
      success: false,
      message: "Falha ao carregar a lista de produtos",
      data: err.response.data,
    };
  }
  return response;
};

/**
 * Função para remover um produto
 * @param id - Recebe o id do produto que será removido
 * @returns - Retorna uma Promise com a resposta da API
 */
const remove = async (id: number): Promise<IResponse> => {
  let response = {} as IResponse;
  try {
    const data = await api.delete(`${categoryURL}/${id}`);
    response = {
      status: 200,
      success: true,
      message: "Produto removido com sucesso!",
      data: data.data,
    };
  } catch (err: any) {
    response = {
      status: err.response.status,
      success: false,
      message: "Falha ao remover o produto",
      data: err.response.data,
    };
  }
  return response;
};

/**
 * Função para buscar um produto pelo id
 * @param id - Recebe o id do produto que será buscado
 * @returns - Retorna uma Promise com a resposta da API
 */
const findById = async (id: number): Promise<IResponse> => {
  let response = {} as IResponse;
  try {
    const data = await api.get(`${categoryURL}/${id}`);
    response = {
      status: 200,
      success: true,
      message: "Produto carregado com sucesso!",
      data: data.data,
    };
  } catch (err: any) {
    response = {
      status: err.response.status,
      success: false,
      message: "Falha ao carregar o produto",
      data: err.response.data,
    };
  }
  return response;
};

// Objeto que exporta todas as funções
const ProductService = {
  save,
  findAll,
  remove,
  findById,
};

export default ProductService;
```

#### 11.2  Lista de Produtos

Com a interface e *service* criados, podemos criar o componente que será utilizado para exibir a lista dos produtos vindos da API. O componente **ProductListPage** será criado dentro de **/src/pages/product-list/index.tsx**:

```jsx
import { useEffect, useRef, useState } from "react";
import { DataTable } from "primereact/datatable";
import { Column } from "primereact/column";
import { Button } from "primereact/button";
import type { IProduct } from "@/commons/types";
import ProductService from "@/services/product-service";
import { useNavigate } from "react-router-dom";
import { Toast } from "primereact/toast";

export const ProductListPage = () => {
  const [data, setData] = useState<IProduct[]>([]);
  const { findAll, remove } = ProductService;
  const navigate = useNavigate();
  const toast = useRef<Toast>(null);

  // hook do react para executar ações ao carregar o componente
  // carrega a lista de produtos
  useEffect(() => {
    loadData();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  // função para carregar a lista de produtos
  const loadData = async () => {
    const response = await findAll();

    if (response.status === 200) {
      setData(Array.isArray(response.data) ? response.data : []);
    } else {
      toast.current?.show({
        severity: "error",
        summary: "Erro",
        detail: "Não foi possível carregar a lista de produtos.",
        life: 3000,
      });
    }
  };

  const handleEdit = (product: IProduct) => {
    navigate(`/products/${product.id}`);
  };

  const handleDelete = async (product: IProduct) => {
    if (confirm(`Tem certeza que deseja excluir "${product.name}"?`)) {
      if (product.id) {
        try {
          await remove(product.id);
          setData((prev) => prev.filter((c) => c.id !== product.id));
          toast.current?.show({
            severity: "success",
            summary: "Sucesso",
            detail: "Registro removido com sucesso",
            life: 3000,
          });
        } catch {
          toast.current?.show({
            severity: "error",
            summary: "Erro",
            detail: "Não foi possível remover o registro.",
            life: 3000,
          });
        }
      }
    }
  };

  const actionTemplate = (rowData: IProduct) => (
    <div className="flex gap-2">
      <Button
        icon="pi pi-pencil"
        className="p-button-sm p-button-text"
        onClick={() => handleEdit(rowData)}
        tooltip="Editar"
      />
      <Button
        icon="pi pi-trash"
        className="p-button-sm p-button-text p-button-danger"
        onClick={() => handleDelete(rowData)}
        tooltip="Excluir"
      />
    </div>
  );

  const priceTemplate = (rowData: IProduct) => {
    return rowData.price.toLocaleString("pt-BR", {
      style: "currency",
      currency: "BRL",
    });
  };

  return (
    <div className="container mx-auto px-4 pt-24">
      <Toast ref={toast} />
      <h2 className="text-2xl mb-4">Lista de Produtos</h2>
      <DataTable value={data} stripedRows>
        <Column field="id" header="ID" style={{ width: "5%" }} />
        <Column field="name" header="Nome" />
        <Column field="description" header="Descrição" />
        <Column header="Preço" body={priceTemplate} style={{ width: "15%" }} />
        <Column field="category.name" header="Categoria" />
        <Column body={actionTemplate} header="Ações" style={{ width: "15%" }} />
      </DataTable>
    </div>
  );
};
```
Para que o componente **ProductListPage** seja exibido devemos adicionar a nova rota ao componente **AppRoutes**:
```jsx
//...
import { ProductListPage } from  "@/pages/product-list";

export function AppRoutes() {
//...
        {/* protected routes */}
        <Route element={<RequireAuth />}>
		  //...
          <Route path="/products" element={<ProductListPage />} />
        </Route>
//...
}
```

Para finalizar a lista de produtos será necessário adicionar o atalho para página ao componente **TopMenu**, ajustando apenas a contante *items* que armazena um *array* com os itens de menu que serão apresentados no topo da página:

```jsx
	/...
    const items: MenuItem[] = authenticated
      ? [
        { label: "Home", icon: "pi pi-home", command: () => navigate("/") },
        {
          label: "Categorias",
          icon: "pi pi-box",
          items: [
            {
              label: "Listar",
              icon: "pi pi-list",
              command: () => navigate("/categories"),
            },
            {
              label: "Novo",
              icon: "pi pi-plus",
              command: () => navigate("/categories/new"),
            },
          ],
        },
        {
          label: "Produtos",
          icon: "pi pi-box",
          items: [
            {
              label: "Listar",
              icon: "pi pi-list",
              command: () => navigate("/products"),
            },
          ],
        },
      ]
    : [];
   //...
```

#### 11.3  Cadastro e edição de Produtos

Com a interface, *service* e o componente de lista criados, podemos criar o componente que será utilizado para cadastro e edição de produtos. O componente **ProductFormPage** será criado dentro de **/src/pages/product-form/index.tsx**:

```jsx
import { useForm, Controller } from "react-hook-form";
import { InputText } from "primereact/inputtext";
import { InputNumber } from "primereact/inputnumber";
import { Dropdown } from "primereact/dropdown";
import { Button } from "primereact/button";
import { useNavigate, useParams } from "react-router-dom";
import { useEffect, useRef, useState } from "react";
import type { ICategory, IProduct, IResponse } from "@/commons/types";
import { Toast } from "primereact/toast";
import CategoryService from "@/services/category-service";
import ProductService from "@/services/product-service";

export const ProductFormPage = () => {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const toast = useRef<Toast>(null);
  const [loading, setLoading] = useState(false);
  const [product, setProduct] = useState<IProduct | undefined>(undefined);

  const [categories, setCategories] = useState<ICategory[]>([]);
  const {
    control,
    handleSubmit,
    reset,
    formState: { errors, isSubmitting },
  } = useForm<IProduct>({
    defaultValues: { name: "", description: "", price: 0, category: undefined },
  });
  const { findAll } = CategoryService;
  const { findById, save } = ProductService;

  const isEdit = !!id;

  // Simula carregamento de categorias e, se for edição, do produto
  useEffect(() => {
    // Simula fetch categorias
    const loadCategories = async () => {
      const response = await findAll();
      if (response.data && Array.isArray(response.data)) {
        setCategories(response.data as ICategory[]);
      } else {
        setCategories([]);
      }
    };
    loadCategories();

    loadProduct();
  // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [id]); // Adiciona id como dependência para recarregar o produto se o id mudar

  const loadProduct = async () => {
    if (isEdit) {
      setLoading(true);
      const response = (await findById(parseInt(id!))) as IResponse;
      try {
        if (response.status === 200) {
          setProduct(response.data as IProduct);
          reset(response.data as IProduct);
        } else {
          toast.current?.show({
            severity: "error",
            summary: "Erro",
            detail: "Falha ao carregar o registro.",
            life: 3000,
          });
        }
      } catch {
        setProduct(undefined);
      } finally {
        setLoading(false);
      }
    }
  };

  const onSubmit = async (data: IProduct) => {
    setLoading(true);
    try {
      const response = await save(data);
      if (
        (response.status === 201 || response.status === 200) &&
        response.data
      ) {
        toast.current?.show({
          severity: "success",
          summary: "Sucesso",
          detail: "Registro salvo com sucesso.",
          life: 3000,
        });
        setTimeout(() => {
          navigate("/products");
        }, 1000);
      } else {
        toast.current?.show({
          severity: "error",
          summary: "Erro",
          detail: "Não foi possível salvar o registro.",
          life: 3000,
        });
      }
    } catch {
      toast.current?.show({
        severity: "error",
        summary: "Erro",
        detail: "Não foi possível salvar o registro.",
        life: 3000,
      });
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="container mx-auto px-4 pt-24 max-w-2xl">
      <Toast ref={toast} />

      <h2 className="text-2xl font-bold mb-4">
        {isEdit ? "Editar Produto" : "Cadastrar Produto"}
      </h2>
      {!isEdit || product ? (
        <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
          <div>
            <label className="block mb-1">Nome</label>
            <Controller
              name="name"
              control={control}
              rules={{ required: "Nome é obrigatório" }}
              render={({ field }) => (
                <InputText {...field} className="w-full" />
              )}
            />
            {errors.name && (
              <p className="text-red-500 text-sm">{errors.name.message}</p>
            )}
          </div>

          <div>
            <label className="block mb-1">Descrição</label>
            <Controller
              name="description"
              control={control}
              rules={{ required: "Descrição é obrigatória" }}
              render={({ field }) => (
                <InputText {...field} className="w-full" />
              )}
            />
            {errors.description && (
              <p className="text-red-500 text-sm">
                {errors.description.message}
              </p>
            )}
          </div>

          <div>
            <label className="block mb-1">Preço</label>
            <Controller
              name="price"
              control={control}
              rules={{ required: "Preço é obrigatório" }}
              render={({ field }) => (
                <InputNumber
                  value={field.value}
                  onValueChange={(e) => field.onChange(e.value)}
                  className="w-full"
                  mode="currency"
                  currency="BRL"
                  locale="pt-BR"
                />
              )}
            />
            {errors.price && (
              <p className="text-red-500 text-sm">{errors.price.message}</p>
            )}
          </div>

          <div>
            <label className="block mb-1">Categoria</label>
            <Controller
              name="category"
              control={control}
              rules={{ required: "Categoria é obrigatória" }}
              render={({ field }) => (
                <Dropdown
                  {...field}
                  options={categories}
                  optionLabel="name"
                  placeholder="Selecione uma categoria"
                  className="w-full"
                />
              )}
            />
            {errors.category && (
              <p className="text-red-500 text-sm">{errors.category.message}</p>
            )}
          </div>

          <div className="flex justify-end gap-2 mt-4">
            <Button
              type="button"
              label="Cancelar"
              className="p-button-secondary"
              onClick={() => navigate("/products")}
              loading={loading || isSubmitting}
              disabled={loading || isSubmitting}
            />
            <Button
              type="submit"
              label={isEdit ? "Atualizar" : "Salvar"}
              loading={loading || isSubmitting}
              disabled={loading || isSubmitting}
            />
          </div>
        </form>
      ) : (
        <p>Carregando...</p>
      )}
    </div>
  );
};
```

Para que o componente **ProductListPage** seja exibido devemos adicionar a nova rota ao componente **AppRoutes**:
```jsx
//...
import { ProductListPage } from "@/pages/product-list";
import { ProductFormPage } from  "@/pages/product-form";

export function AppRoutes() {
//...
        {/* protected routes */}
        <Route element={<RequireAuth />}>
		      //...
          <Route path="/categories" element={<ProductListPage />} />
          <Route  path="/categories/new"  element={<ProductFormPage />}  />
		      <Route  path="/categories/:id"  element={<ProductFormPage />}  />
        </Route>
//...
}
```
Para finalizar o cadastro de produtos será necessário adicionar o atalho para página ao componente **TopMenu**, ajustando apenas a contante *items* que armazena um *array* com os itens de menu que serão apresentados no topo da página:

```jsx
	/...
    const items: MenuItem[] = authenticated
      ? [
        { label: "Home", icon: "pi pi-home", command: () => navigate("/") },
        {
          label: "Categorias",
          icon: "pi pi-box",
          items: [
            {
              label: "Listar",
              icon: "pi pi-list",
              command: () => navigate("/categories"),
            },
            {
              label: "Novo",
              icon: "pi pi-plus",
              command: () => navigate("/categories/new"),
            },
          ],
        },
        {
          label: "Produtos",
          icon: "pi pi-box",
          items: [
            {
              label: "Listar",
              icon: "pi pi-list",
              command: () => navigate("/products"),
            },
            {
              label: "Novo",
              icon: "pi pi-plus",
              command: () => navigate("/products/new"),
            },
          ],
        },
      ]
    : [];
   //...
```

Com o componente **ProductFormPage** criado finalizamos o CRUD de produtos e o projeto.

---

### 12. 🚫 Tratando o erro 404 Not Found

Para tratar o erro de página não encontrada no lado cliente da aplicação será desenvolvido o componente NotFound, em: **/src/pages/not-found/index.tsx**.

```jsx
import { Link } from "react-router-dom";

export function NotFound() {
  return (
    <article style={{ padding: "100px" }}>
      <h1>Oops!</h1>
      <p>Página não encontrada!</p>
      <div className="flexGrow">
        <Link to="/">Home</Link>
      </div>
    </article>
  );
}
```
O qual será adicionado ao componente **AppRoutes**, o componente receberá como ***path*** o valor **\***, assim caso a URL não esteja em nenhuma rota tratada no componente, será exibido o componente **NotFound**.

```jsx
import { Route, Routes } from "react-router-dom";
import { Layout } from "@/components/layout";
import { LoginPage } from "@/pages/login";
import { RegisterPage } from "@/pages/register";
import { HomePage } from "@/pages/home";
import { RequireAuth } from "@/components/require-auth";
import { CategoryListPage } from "@/pages/category-list";
import { CategoryFormPage } from "@/pages/category-form";
import { ProductListPage } from "@/pages/product-list";
import { ProductFormPage } from "@/pages/product-form";
import { NotFound } from "@/pages/not-found";

export function AppRoutes() {
  return (
    <Routes>
      <Route path="/" element={<Layout />}>
        {/* public routes */}
        <Route path="login" element={<LoginPage />} />
        <Route path="register" element={<RegisterPage />} />

        {/* protected routes */}
        <Route element={<RequireAuth />}>
          <Route path="/" element={<HomePage />} />
          <Route path="/home" element={<HomePage />} />
          
          <Route path="/categories" element={<CategoryListPage />} />
          <Route path="/categories/new" element={<CategoryFormPage />} />
          <Route path="/categories/:id" element={<CategoryFormPage />} />

          <Route path="/products" element={<ProductListPage />} />
          <Route path="/products/new" element={<ProductFormPage />} />
          <Route path="/products/:id" element={<ProductFormPage />} />
        </Route>

        {/* catch all */}
        <Route path="*" element={<NotFound />} />
      </Route>
    </Routes>
  );
}
```
---
# 📚 Referências

[1] React. Disponível em: https://pt-br.reactjs.org/.

[2] Vite. Disponível em: https://vitejs.dev/

[3] React. Disponível em: https://reactjs.org/docs/state-and-lifecycle.html

[4] React Hooks. Disponível em: https://reactjs.org/docs/hooks-intro.html

[5] React Router Dom. Disponível em: https://reactrouter.com/

[6] useEffect. Disponível em: https://reactjs.org/docs/hooks-effect.html

[7] React Hook Form. Disponível em: https://react-hook-form.com/

[8] Chakra UI. Disponível em: https://chakra-ui.com/

[9] React Icons. Disponível em: https://react-icons.github.io/react-icons/

[10] Bootstrap. Disponível em: https://getbootstrap.com/