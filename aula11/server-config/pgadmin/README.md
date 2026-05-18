# pgAdmin — Administração do PostgreSQL

O **pgAdmin 4** é a interface web para administração e desenvolvimento em cima do PostgreSQL. Ele permite criar bancos e tabelas, executar consultas SQL, gerenciar usuários e inspecionar o estado do servidor sem usar apenas a linha de comando.

## O que será criado

| Item | Descrição |
|------|-----------|
| **Imagem** | `dpage/pgadmin4` |
| **Container** | `pgadmin` |
| **Porta no host** | `15432` → `80` (interface web interna) |
| **Redes** | `web`, `database` |

### Variáveis de ambiente

- `PGADMIN_DEFAULT_EMAIL`: e-mail de login inicial (no compose raiz: `${PGADMIN_DEFAULT_EMAIL}`)
- `PGADMIN_DEFAULT_PASSWORD`: senha do administrador pgAdmin
- `TZ`: fuso horário (`America/Sao_Paulo`)
- No compose raiz: `SERVER_NAME=pgadmin.${DOMAIN}`

### Acesso

- **Via Traefik**: `https://pgadmin.${DOMAIN}` (compose raiz) ou subdomínio configurado nos labels (ex.: `pgadmin.viniciuspegorini.com.br` no compose modular)
- **Direto na máquina**: `http://<IP-do-droplet>:15432`

Após o primeiro login, é necessário **registrar o servidor PostgreSQL** em pgAdmin apontando para o host `postgresql` (nome do container na rede `database`) na porta `5432`, com o usuário e senha definidos no serviço Postgres.

### Integração com Traefik

Labels HTTP expõem o pgAdmin em HTTPS com certificado Let's Encrypt, na mesma rede `web` usada pelo proxy.

## Execução isolada

Na pasta `pgadmin/`:

```bash
docker compose up -d
```

Requer PostgreSQL em execução na rede `database` e, para HTTPS externo, o Traefik na rede `web`.

## Referências

- [Site oficial do pgAdmin](https://www.pgadmin.org/)
- [Documentação pgAdmin 4](https://www.pgadmin.org/docs/pgadmin4/latest/index.html)
- [Imagem Docker dpage/pgadmin4](https://hub.docker.com/r/dpage/pgadmin4)
- [Conectar pgAdmin a um servidor PostgreSQL](https://www.pgadmin.org/docs/pgadmin4/latest/connecting.html)
- [Traefik — roteadores HTTP](https://doc.traefik.io/traefik/routing/routers/#entrypoints)
