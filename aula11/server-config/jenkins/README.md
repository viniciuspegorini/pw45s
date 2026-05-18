# Jenkins — CI/CD (integração e entrega contínuas)

O **Jenkins** automatiza build, testes e deploy das aplicações **cliente React** e **servidor Spring** nos repositórios de deploy do curso. A imagem customizada inclui o Docker Engine dentro do container para que os pipelines possam construir e publicar novas imagens no mesmo host.

## O que será criado

| Item | Descrição |
|------|-----------|
| **Imagem base** | `jenkins/jenkins:lts` (via `Dockerfile-jenkins`) |
| **Container** | `jenkins` |
| **Portas** | `8081:8080` (UI), `50000:50000` (agentes JNLP) |
| **Rede** | `web` |
| **Volumes** | `appdata/jenkins` (dados), `appdata/jenkins_tmp`, socket Docker |

### Imagem customizada (`Dockerfile-jenkins`)

A imagem estende o Jenkins LTS instalando:

- Docker CE, CLI, Buildx e Compose plugin (Debian)
- Usuário `jenkins` no grupo `docker` para executar `docker` nos pipelines

No `docker-compose.yml` da raiz, o build usa o `Dockerfile` na pasta pai (`server-config/Dockerfile`), com conteúdo equivalente.

### Acesso

- **HTTPS (produção)**: `https://jenkins.${DOMAIN}/`
- **HTTP direto**: `http://<IP>:8081`
- Variável `JENKINS_URL` no stack completo aponta para a URL pública HTTPS

No compose modular desta pasta, o Jenkins pode ser iniciado com prefixo `/jenkins` via argumento de linha de comando.

### Recuperar senha inicial

```bash
docker exec jenkins cat /var/jenkins_home/secrets/initialAdminPassword
```

Ou acompanhar os logs: `docker logs -f jenkins`.

### Pipelines previstos

| Item Jenkins | Repositório | Função |
|--------------|-------------|--------|
| **Server** | [pw45s-server-deploy](https://github.com/viniciuspegorini/pw45s-server-deploy) | Deploy da API Spring |
| **Client** | [pw45s-client-deploy](https://github.com/viniciuspegorini/pw45s-client-deploy) | Deploy do frontend React |

Tipo: **Pipeline** com script from SCM (Git), branch `main`.

### Credenciais globais sugeridas

Configurar em *Manage Jenkins → Credentials → System → Global credentials*:

| ID | Uso |
|----|-----|
| `postgres-id` | Usuário/senha PostgreSQL |
| `pw45s_google_client_id` | OAuth Google (cliente) |
| `pw45s_minio_id` | Access key / secret MinIO |

### Privilégios

No stack da raiz, o container roda como `root` com `privileged: true` e monta `/var/run/docker.sock` para orquestrar containers no host — padrão comum em laboratório, mas que exige cautela em produção real.

## Execução isolada

Na pasta `jenkins/`:

```bash
docker compose -f docker-compose-jenkins.yml up -d --build
```

## Referências

- [Documentação oficial do Jenkins](https://www.jenkins.io/doc/)
- [Imagem Docker jenkins/jenkins](https://hub.docker.com/r/jenkins/jenkins)
- [Pipeline as Code](https://www.jenkins.io/doc/book/pipeline/)
- [Jenkins Credentials](https://www.jenkins.io/doc/book/using/using-credentials/)
- [Usar Docker dentro do Jenkins](https://www.jenkins.io/doc/book/installing/docker/)
- [Docker Engine — instalação Debian](https://docs.docker.com/engine/install/debian/)
- Repositório servidor: [pw45s-server-deploy](https://github.com/viniciuspegorini/pw45s-server-deploy)
- Repositório cliente: [pw45s-client-deploy](https://github.com/viniciuspegorini/pw45s-client-deploy)
