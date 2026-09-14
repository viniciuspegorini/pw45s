# 🔐 Controle de Permissões de Usuário no Lado Cliente

Considerando a aplicação utilizada nas aulas aula01 e aula02 que foi desenvolvida com autenticação via *token* JWT e rotas protegidas (apenas verificando se o usuário estava ou não autenticado), o próximo passo foi refinar o controle de acesso para que **cada rota e cada item de menu leve em consideração o perfil (role) do usuário autenticado**, e não apenas se ele está ou não autenticado.

Até então, o componente **RequireAuth** liberava o acesso a qualquer usuário autenticado, independente de seu perfil. Com as alterações descritas a seguir, a aplicação passa a diferenciar usuários com o perfil `ROLE_USER` de usuários com o perfil `ROLE_ADMIN`, restringindo rotas e funcionalidades específicas apenas para quem possui a permissão necessária.

## 📋 Resumo das alterações

- **`AuthContext.tsx`**: adicionada a função `hasPermission`, responsável por verificar se o usuário autenticado possui uma determinada *authority* (permissão).
- **`RequireAuth`**: o componente passou a receber a propriedade `allowedRoles`, utilizada para verificar se o usuário autenticado possui permissão para acessar a rota.
- **`Unauthorized`**: nova página exibida quando um usuário autenticado tenta acessar uma rota para a qual não possui permissão.
- **`AppRoutes`**: as rotas foram reorganizadas em grupos, cada grupo definindo quais *roles* (`ROLE_USER`, `ROLE_ADMIN`) podem acessá-las.
- **`TopMenu`**: os itens do menu passaram a ser exibidos condicionalmente, de acordo com a permissão do usuário autenticado, utilizando a função `hasPermission`.
- **`ProductShow`/`ProductCard`**: nova página e componente criados como exemplo de funcionalidade restrita apenas ao perfil `ROLE_ADMIN`.

As interfaces **Authorities** e **AuthenticatedUser** (arquivo `/src/commons/types.ts`) já haviam sido criadas na etapa de autenticação (seção 7.1 do [README.md](./README.md)) e não precisaram ser alteradas, pois a API já retornava a lista de *authorities* do usuário dentro do *token* de autenticação:

```ts
export interface Authorities {
  authority: string;
}

export interface AuthenticatedUser {
  displayName: string;
  username: string;
  authorities: Authorities[];
}
```

Ou seja, a informação sobre o perfil do usuário (`ROLE_USER` ou `ROLE_ADMIN`) já estava disponível na aplicação desde o *login*, faltando apenas utilizá-la para controlar o acesso às rotas e a exibição dos componentes.

---

## 1. 🧩 Atualizando o AuthContext com a função hasPermission

Para que qualquer componente da aplicação possa verificar se o usuário autenticado possui uma determinada permissão, foi adicionada a função **hasPermission** ao contexto de autenticação, em **`/src/context/AuthContext.tsx`**:

```tsx
interface AuthContextType {
  authenticated: boolean;
  authenticatedUser?: AuthenticatedUser;
  handleLogin: (authenticationResponse: AuthenticationResponse) => Promise<any>;
  handleLogout: () => void;
  hasPermission: (permission: string) => boolean; // adicionado
}
```

```tsx
/**
 * Verifica se o usuário tem uma determinada permissão
 * @param authority Nome da permissão a ser verificada
 * @returns true se o usuário possuir a permissão
 */
const hasPermission = (authority: string): boolean => {
  if (!authenticatedUser?.authorities) {
    return false;
  }

  return authenticatedUser?.authorities.some(
    (auth) => auth.authority === authority
  );
};
```

E o valor de `hasPermission` foi adicionado ao *Provider* do contexto para que fique disponível através do *hook* `useAuth`:

```tsx
return (
  <AuthContext.Provider
    value={{
      authenticated,
      authenticatedUser,
      handleLogin,
      handleLogout,
      hasPermission, // adicionado
    }}
  >
    {children}
  </AuthContext.Provider>
);
```

A função percorre a lista de *authorities* do usuário autenticado (recebida da API no momento do *login*, dentro de `AuthenticatedUser.authorities`) verificando se alguma delas corresponde à permissão informada por parâmetro. Com essa função qualquer componente que utilize o *hook* `useAuth()` passa a conseguir verificar, de forma simples, se o usuário autenticado possui ou não uma determinada *role*.

---

## 2. 🚧 Atualizando o componente RequireAuth para validar as roles

O componente **RequireAuth**, criado na seção 8.1 do [README.md](./README.md), verificava apenas se o usuário estava autenticado. Ele foi alterado para receber a propriedade **allowedRoles**, um *array* com as *roles* que possuem permissão para acessar as rotas filhas, em **`/src/components/require-auth/index.tsx`**:

```tsx
import { useContext } from "react";
import { useLocation, Navigate, Outlet } from "react-router-dom";
import { AuthContext } from "@/context/AuthContext";

interface RequireAuthProps {
  allowedRoles: string[];
}

export function RequireAuth({ allowedRoles }: RequireAuthProps) {
  const { authenticated, authenticatedUser } = useContext(AuthContext);
  const location = useLocation();

  return authenticatedUser?.authorities?.find((authority) =>
    allowedRoles?.includes(authority.authority)
  ) ? (
    <Outlet />
  ) : authenticated ? (
    <Navigate to="/unauthorized" state={{ from: location }} replace />
  ) : (
    <Navigate to="/login" state={{ from: location }} replace />
  );
}
```

A lógica do componente passa a considerar três cenários possíveis:

1. **Usuário autenticado e com permissão**: o usuário possui, dentro de `authenticatedUser.authorities`, ao menos uma *authority* presente em `allowedRoles`. Nesse caso o componente `Outlet` é renderizado, exibindo a rota filha normalmente.
2. **Usuário autenticado, mas sem permissão**: o usuário está autenticado (`authenticated === true`), porém nenhuma de suas *authorities* está presente em `allowedRoles`. Nesse caso o usuário é redirecionado para a rota `/unauthorized`.
3. **Usuário não autenticado**: assim como antes, o usuário é redirecionado para a página de `/login`.

---

## 3. 🚫 Criando a página Unauthorized

Para o segundo cenário descrito acima (usuário autenticado, mas sem permissão para acessar o recurso), foi criada a página **Unauthorized**, dentro de **`/src/pages/unauthorized/index.tsx`**, seguindo o mesmo padrão utilizado na página **NotFound** (seção 12 do [README.md](./README.md)):

```tsx
import { Link } from "react-router-dom";

export function Unauthorized() {
  return (
    <article style={{ padding: "100px" }}>
      <h1>Oops!</h1>
      <p>Você não tem permissão para acessar o recurso solicitado!</p>
      <div className="flexGrow">
        <Link to="/">Home</Link>
      </div>
    </article>
  );
}
```

---

## 4. 🔗 Ajustando as rotas com o controle de permissões

Com o componente **RequireAuth** já preparado para receber as *roles* permitidas, o arquivo **`/src/routes/app-routes/index.tsx`** foi ajustado para definir quais perfis podem acessar cada grupo de rotas. Para isso foi criada a constante **ROLES**, evitando o uso de *strings* fixas espalhadas pelo código:

```tsx
import { Route, Routes } from "react-router-dom";
import { LoginPage } from "@/pages/login";
import { RegisterPage } from "@/pages/register";
import { HomePage } from "@/pages/home";
import { RequireAuth } from "@/components/require-auth";
import { Layout } from "@/components/layout";
import { CategoryListPage } from "@/pages/category-list";
import { CategoryFormPage } from "@/pages/category-form";
import { ProductListPage } from "@/pages/product-list";
import { ProductFormPage } from "@/pages/product-form";
import { NotFound } from "@/pages/not-found";
import { ProductShow } from "@/pages/product-show";
import { Unauthorized } from "@/pages/unauthorized";

const ROLES = {
  User: "ROLE_USER",
  Admin: "ROLE_ADMIN",
};

export function AppRoutes() {
  return (
    <Routes>
      <Route path="/" element={<Layout />}>
        {/* public routes */}
        <Route path="login" element={<LoginPage />} />
        <Route path="register" element={<RegisterPage />} />

        {/* protected routes - Roles: User e Admin */}
        <Route
          element={<RequireAuth allowedRoles={[ROLES.User, ROLES.Admin]} />}
        >
          <Route path="/" element={<HomePage />} />
          <Route path="/home" element={<HomePage />} />

          <Route path="unauthorized" element={<Unauthorized />} />

          <Route path="/categories" element={<CategoryListPage />} />
          <Route path="/categories/new" element={<CategoryFormPage />} />
          <Route path="/categories/:id" element={<CategoryFormPage />} />

          <Route path="/products" element={<ProductListPage />} />
          <Route path="/products/new" element={<ProductFormPage />} />
          <Route path="/products/:id" element={<ProductFormPage />} />

          {/* catch all */}
          <Route path="*" element={<NotFound />} />
        </Route>

        {/* protected routes - Role: Admin */}
        <Route element={<RequireAuth allowedRoles={[ROLES.Admin]} />}>
          <Route path="/products/show" element={<ProductShow />} />
        </Route>
      </Route>
    </Routes>
  );
}
```

Perceba que agora existem **dois grupos de rotas protegidas**, cada um envolvido por um `<Route element={<RequireAuth allowedRoles={[...]} />}>` diferente:

- O primeiro grupo (Home, Categorias, Produtos, etc.) recebe `allowedRoles={[ROLES.User, ROLES.Admin]}`, ou seja, pode ser acessado por qualquer usuário autenticado, independente do perfil.
- O segundo grupo, com a rota `/products/show`, recebe `allowedRoles={[ROLES.Admin]}`, podendo ser acessado **somente** por usuários com o perfil `ROLE_ADMIN`. Um usuário autenticado com o perfil `ROLE_USER` que tentar acessar essa rota será redirecionado para `/unauthorized`.

---

## 5. ⚓ Exibindo os itens do menu de acordo com a permissão

Não basta impedir o acesso às rotas: itens de menu que levam a funcionalidades restritas não devem ser exibidos para usuários sem a permissão necessária. Para isso, o componente **TopMenu** (seção 9.1 do [README.md](./README.md)) passou a utilizar a função `hasPermission`, obtida a partir do *hook* `useAuth`:

```tsx
const { authenticated, handleLogout, hasPermission } = useAuth(); // hasPermission adicionado

const items: MenuItem[] = authenticated
  ? [
      { label: "Home", icon: "pi pi-home", command: () => navigate("/") },
      {
        label: "Categorias",
        icon: "pi pi-box",
        items: [
          { label: "Listar", icon: "pi pi-list", command: () => navigate("/categories") },
          { label: "Novo", icon: "pi pi-plus", command: () => navigate("/categories/new") },
        ],
      },
      {
        label: "Produtos",
        icon: "pi pi-box",
        items: [
          { label: "Listar", icon: "pi pi-list", command: () => navigate("/products") },
          { label: "Novo", icon: "pi pi-plus", command: () => navigate("/products/new") },
        ],
      },
      // item exibido apenas para usuários com a permissão ROLE_ADMIN
      hasPermission("ROLE_ADMIN")
        ? { label: "Prod. Show", icon: "pi pi-search", command: () => navigate("/products/show") }
        : {},
    ]
  : [];
```

O item de menu **"Prod. Show"** só é adicionado ao *array* `items` quando `hasPermission("ROLE_ADMIN")` retorna `true`. Dessa forma, usuários com o perfil `ROLE_USER` não enxergam esse atalho no menu, mesmo que, por algum outro caminho (digitando a URL diretamente, por exemplo), ainda continuem impedidos de acessar a rota pelo **RequireAuth**, configurado na etapa anterior.

> 💡 É importante reforçar que esconder o item do menu é apenas uma melhoria de usabilidade (evita que o usuário tente acessar algo que não pode). A validação de permissão que realmente **protege** a rota é feita pelo componente **RequireAuth**, e a validação definitiva deve sempre ocorrer também no lado servidor.

---

## 6. 📦 Criando uma funcionalidade exclusiva para administradores

Para demonstrar, na prática, uma funcionalidade restrita a um perfil específico, foi criada a página **ProductShow**, que exibe os produtos cadastrados em formato de cartões (utilizando o componente **ProductCard**), disponível apenas para usuários com o perfil `ROLE_ADMIN`.

### 6.1 Componente ProductCard

Criado em **`/src/components/product-card/index.tsx`**, é responsável por exibir as informações de um produto (nome, preço, descrição e imagem) dentro de um `Card` do PrimeReact:

```tsx
import { IProduct } from "@/commons/types";
import { Button } from "primereact/button";
import { Card } from "primereact/card";

interface ProductCardProps {
  product: IProduct;
}

export const ProductCard = ({ product }: ProductCardProps) => {
  return (
    <div key={product.id} className="p-col-12 p-sm-6 p-md-4 p-lg-3 mb-4">
      <Card
        title={product.name}
        subTitle={`R$ ${product.price.toFixed(2)}`}
        header={
          <img
            alt={product.name}
            src="https://primefaces.org/cdn/primereact/images/product/blue-band.jpg"
            style={{ width: "100%", height: "200px", objectFit: "cover" }}
          />
        }
        footer={
          <div>
            <Button label="Comprar" icon="pi pi-shopping-cart" className="p-button-sm p-mr-2" />
            <Button label="Detalhes" icon="pi pi-info-circle" className="p-button-secondary p-button-sm" />
          </div>
        }
      >
        <p>{product.description}</p>
      </Card>
    </div>
  );
};
```

### 6.2 Página ProductShow

Criada em **`/src/pages/product-show/index.tsx`**, reutiliza o **ProductService** já existente (seção 11.1 do [README.md](./README.md)) para carregar a lista de produtos da API e renderiza um **ProductCard** para cada produto encontrado:

```tsx
import { useEffect, useRef, useState } from "react";
import type { IProduct } from "@/commons/types";
import ProductService from "@/services/product-service";
import { Toast } from "primereact/toast";
import { ProductCard } from "@/components/product-card";

export const ProductShow = () => {
  const [products, setProducts] = useState<IProduct[]>([]);
  const { findAll } = ProductService;
  const toast = useRef<Toast>(null);

  useEffect(() => {
    loadData();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const loadData = async () => {
    const response = await findAll();

    if (response.status === 200) {
      setProducts(Array.isArray(response.data) ? response.data : []);
    } else {
      toast.current?.show({
        severity: "error",
        summary: "Erro",
        detail: "Não foi possível carregar a lista de produtos.",
        life: 3000,
      });
    }
  };

  return (
    <div className="p-grid p-justify-start p-align-start">
      {products.map((product) => (
        <ProductCard key={product.id} product={product} />
      ))}
    </div>
  );
};
```

Essa página é registrada na rota `/products/show`, dentro do grupo protegido com `allowedRoles={[ROLES.Admin]}` (ver seção 4), e o link para acessá-la só é exibido no menu para usuários com a permissão `ROLE_ADMIN` (ver seção 5).

---

## 7. ✅ Resumindo o fluxo de permissões

Com todas as alterações descritas, o fluxo de controle de acesso da aplicação passa a funcionar da seguinte forma:

1. O usuário realiza o *login* e a API retorna, junto do *token*, a lista de **authorities** do usuário (`ROLE_USER` e/ou `ROLE_ADMIN`), armazenadas no **AuthContext** e persistidas no `localStorage`.
2. Ao navegar pela aplicação, o componente **RequireAuth** verifica, para cada grupo de rotas, se alguma das *authorities* do usuário está entre as `allowedRoles` definidas em **AppRoutes**.
3. Caso o usuário não esteja autenticado, é redirecionado para `/login`.
4. Caso o usuário esteja autenticado, mas não possua a permissão necessária, é redirecionado para `/unauthorized`.
5. Caso o usuário possua a permissão necessária, a rota é renderizada normalmente.
6. Em paralelo, o componente **TopMenu** utiliza a função `hasPermission` do **AuthContext** para exibir, ou não, os itens de menu que levam a funcionalidades restritas, evitando que o usuário sequer veja atalhos para os quais não tem acesso.

Dessa forma, a aplicação passa a tratar, no lado cliente, tanto a **autenticação** (quem é o usuário) quanto a **autorização** (o que esse usuário pode acessar), sempre em conjunto com as validações equivalentes realizadas na API (server), responsável pela proteção definitiva dos dados.
