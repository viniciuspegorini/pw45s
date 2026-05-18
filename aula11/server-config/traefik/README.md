# Traefik — Proxy reverso

O **Traefik** é o ponto de entrada do ambiente de produção simulado. Ele atua como proxy reverso e balanceador de carga, descobrindo automaticamente os containers Docker na rede `web` e roteando o tráfego HTTP/HTTPS para cada serviço conforme os labels definidos nos `docker-compose.yml`.

## O que será criado

| Item | Descrição |
|------|-----------|
| **Imagem** | `traefik:v3.6` (mín. para Docker Engine 29+) |
| **Container** | `traefik` |
| **Portas expostas** | `80` (HTTP), `443` (HTTPS) |
| **Rede** | `web` (externa) e `database` |

### Funcionalidades principais

- **Entrypoints**: `web` (:80), `websecure` (:443) e `postgresql` (:5432) para acesso TCP ao banco via TLS.
- **Provider Docker**: leitura do socket `/var/run/docker.sock` para configurar rotas dinamicamente.
- **Certificados TLS**: emissão automática via Let's Encrypt (ACME), com desafio DNS no provedor configurado em `DNS_PROVIDER` (ex.: Cloudflare).
- **Domínio wildcard**: certificado para `${DOMAIN}` e subdomínios `*.${DOMAIN}`.
- **Dashboard**: disponível em `https://traefik.${DOMAIN}`, protegido por autenticação básica (`TRAEFIK_USER` / `TRAEFIK_PASSWORD`).
- **Redirecionamento**: todo tráfego HTTP é redirecionado para HTTPS.

### Pré-requisitos antes do deploy

1. Criar a rede Docker externa: `docker network create --driver=bridge --attachable web`
2. Configurar variáveis no `.env` na raiz de `server-config`: `DOMAIN`, `EMAIL`, `DNS_PROVIDER`, `CF_API_TOKEN`, `TRAEFIK_USER`, `TRAEFIK_PASSWORD`
3. Criar `appdata/traefik/acme.json` com permissão `600` (armazena certificados Let's Encrypt)
4. Gerar hash da senha do dashboard com `htpasswd -nbB` (no `.env`, dobrar cada `$` como `$$`)

### Subdomínios roteados por outros serviços

O Traefik não hospeda as aplicações; ele encaminha requisições para Jenkins, MinIO, pgAdmin, PostgreSQL e, após o pipeline, para API e cliente React.

## Execução isolada

Na pasta `traefik/`:

```bash
docker compose up -d
```

No ambiente completo, o serviço também está definido no `docker-compose.yml` da raiz de `server-config`.

## Referências

- [Documentação oficial do Traefik](https://doc.traefik.io/traefik/)
- [Traefik com Docker](https://doc.traefik.io/traefik/providers/docker/)
- [Certificados ACME / Let's Encrypt](https://doc.traefik.io/traefik/https/acme/)
- [Desafio DNS (Cloudflare)](https://doc.traefik.io/traefik/https/acme/#dnschallenge)
- [Dashboard e API](https://doc.traefik.io/traefik/operations/dashboard/)
- [Middleware de autenticação básica](https://doc.traefik.io/traefik/middlewares/http/basicauth/)
