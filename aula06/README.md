# Upload de arquivos - Base64 no Banco de dados e Arquivo no Sistema de Arquivos.

Os conteúdos das aplicações cliente e servidor estão nas respectivas pastas, descritos no arquivo README.md de cada uma:

- [server/README.md](./server/README.md): *endpoints* de *upload* (`/products/upload-db` e `/products/upload-fs`), armazenamento da imagem no banco de dados (BLOB) ou no sistema de arquivos, e validação do arquivo enviado.
- [client/README.md](./client/README.md): envio do produto e da imagem com `FormData` (`multipart/form-data`) e exibição da imagem em Base64.

Os projetos partem do código da pasta **aula04** (API com Spring Boot 4, Java 25, Spring Security, JWT, MapStruct e autenticação com o Google por meio do ID Token; cliente React com autenticação com usuário e senha ou com o Google e controle de permissões por *role*). Foram alteradas as aplicações **server** e **client**, sendo adicionado o *upload* de uma imagem para a classe `Product`.

## ▶️ Executando

1. Inicie a API: na pasta `server`, execute `./mvnw spring-boot:run` (porta `8080`, requer Java 25). As imagens enviadas para o sistema de arquivos são gravadas em `server/uploads`.
2. Inicie o cliente: na pasta `client`, copie o arquivo `.env.example` para `.env` (*Client ID* do Google), execute `npm install` e depois `npm run dev` (porta `5173`).
3. Acesse `http://localhost:5173`, autentique-se com o usuário `admin` (senha `123`) e cadastre um produto com imagem em **Produtos > Novo**. O usuário precisa da permissão `ROLE_ADMIN`, pois a API restringe `/products/**` a administradores.
