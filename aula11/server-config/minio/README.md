# MinIO — Armazenamento de objetos (S3)

O **MinIO** fornece armazenamento de objetos compatível com a API **Amazon S3**. No projeto PW45S, ele guarda arquivos não estruturados enviados pela API Spring (imagens, uploads, backups, etc.), substituindo ou simulando um bucket S3 em produção.

## O que será criado

| Item | Descrição |
|------|-----------|
| **Imagem** | `minio/minio:latest` (raiz) ou `quay.io/minio/minio` (compose modular) |
| **Container** | `minio` |
| **API S3** | porta `9000` (mapeada como `8000` no host no stack completo) |
| **Console web** | porta `9001` (mapeada como `8001` no host) |
| **Volume** | `./appdata/minio/data` → `/data` |
| **Redes** | `web`, `database` |

### Variáveis de ambiente

- `MINIO_ROOT_USER`: usuário administrador (equivalente à access key)
- `MINIO_ROOT_PASSWORD`: senha administrador (secret key)
- `MINIO_BROWSER_REDIRECT_URL`: URL pública da console (ex.: `http://minio-console.${DOMAIN}`)
- `TZ`: fuso horário

### Endpoints expostos

| Serviço | Subdomínio (exemplo) | Uso |
|---------|----------------------|-----|
| API S3 | `minio.${DOMAIN}` | Upload/download via SDK ou cliente S3 |
| Console | `minio-console.${DOMAIN}` | Interface web para buckets e políticas |

O comando de inicialização é: `server /data --console-address :9001`.

### Integração com a aplicação

A API Spring deve configurar endpoint, access key e secret compatíveis com o MinIO. No Jenkins, a credencial global `pw45s_minio_id` armazena usuário e senha para o pipeline de deploy do servidor.

### DNS na Cloudflare

Conforme o README principal, criar registros tipo **A** para `minio` e `minio-console` apontando para o IP do Droplet na Digital Ocean.

## Execução isolada

Na pasta `minio/`:

```bash
docker compose up -d
```

## Referências

- [Documentação oficial do MinIO](https://min.io/docs/minio/linux/index.html)
- [MinIO Docker — quickstart](https://min.io/docs/minio/container/index.html)
- [API compatível com Amazon S3](https://min.io/docs/minio/linux/developers/minio-drivers.html)
- [Console do MinIO](https://min.io/docs/minio/linux/administration/minio-console.html)
- [AWS SDK for Java — S3 client](https://docs.aws.amazon.com/sdk-for-java/latest/developer-guide/examples-s3.html) (útil para integração Spring)
- [Traefik — múltiplos roteadores no mesmo container](https://doc.traefik.io/traefik/routing/routers/)
