# PostgreSQL — Banco de dados

O **PostgreSQL** é o sistema de gerenciamento de banco de dados relacional utilizado pela API Spring do projeto PW45S. Os dados da aplicação (usuários, entidades de negócio, etc.) persistem no volume Docker mapeado em `appdata/pgdata`.

## O que será criado

| Item | Descrição |
|------|-----------|
| **Imagem** | `postgres:14.2` |
| **Container** | `postgresql` |
| **Porta** | `5432` (host e container) |
| **Redes** | `web`, `database` |
| **Volume** | `./appdata/pgdata` → `/var/lib/postgresql/data` |

### Variáveis de ambiente

No compose da pasta `postgres/`:

- `POSTGRES_DB`: nome do banco inicial (ex.: `pw45s` no compose modular; `postgres` no compose raiz)
- `POSTGRES_USER`: usuário padrão (`postgres`)
- `POSTGRES_PASSWORD`: senha definida no `.env` ou no compose
- `TZ`: `America/Sao_Paulo`

No `docker-compose.yml` da raiz, a senha vem de `${POSTGRES_PASSWORD}`.

### Integração com Traefik (TCP)

O serviço pode ser exposto externamente com TLS via entrypoint TCP `postgresql` do Traefik (`HostSNI` no domínio configurado). Isso permite conexão remota segura ao banco sem expor credenciais em HTTP.

### Healthcheck

O container verifica disponibilidade com `pg_isready -U postgres` a cada 5 segundos.

### Banco da aplicação

O compose modular já define `POSTGRES_DB=pw45s`. No deploy pela raiz, após subir o stack, pode ser necessário criar o banco manualmente:

```bash
docker exec -it postgresql psql -U postgres -c "CREATE DATABASE pw45s;"
```

A API Spring e o Jenkins (credencial `postgres-id`) utilizam esse banco.

## Execução isolada

Na pasta `postgres/` (com rede `web` já criada):

```bash
docker compose up -d
```

## Referências

- [Documentação oficial do PostgreSQL](https://www.postgresql.org/docs/)
- [Imagem Docker oficial postgres](https://hub.docker.com/_/postgres)
- [Variáveis de ambiente da imagem](https://hub.docker.com/_/postgres#environment-variables)
- [Traefik — roteamento TCP](https://doc.traefik.io/traefik/routing/routers/#configuring-tcp-routers)
- [Spring Boot — DataSource com PostgreSQL](https://docs.spring.io/spring-boot/reference/data/sql.html)
