# Deploy das aplicações client e server com Docker

Os conteúdos das aplicações estão nas respectivas pastas, descritos no arquivo README.md de cada uma:

| Pasta | Conteúdo |
|-------|----------|
| [`server/`](server/README.md) | API Spring Boot 4 / Java 25: `Dockerfile`, `docker-compose.yml` (produção), `compose.dev.yaml` (desenvolvimento) e `Jenkinsfile` |
| [`client/`](client/README.md) | Aplicação React (Vite) servida pelo Nginx: `Dockerfile`, `nginx.conf`, `docker-compose.yml` e `Jenkinsfile` |
| [`server-config/`](server-config/Readme.MD) | Infraestrutura do servidor de produção: Traefik, Jenkins, PostgreSQL, MinIO e pgAdmin |

O código-fonte das aplicações **server** e **client** é o mesmo da [aula09](../aula09/README.md) (API com armazenamento de arquivos compatível com o S3 e observabilidade com OpenTelemetry). Nesta aula foram adicionados os arquivos de Docker, Docker Compose e Jenkins, e as configurações que mudam entre os ambientes (URLs, credenciais, perfil do Spring) passaram a ser lidas de variáveis de ambiente.
