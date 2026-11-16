# RustFS — Armazenamento de objetos (S3)

O **RustFS** é um serviço de armazenamento de objetos, escrito em Rust, compatível com a API do **Amazon S3**. No projeto PW45S, ele guarda as imagens dos produtos enviadas pela API Spring. É o mesmo serviço utilizado em desenvolvimento desde a [aula07](../../../aula07/server/README.md).

## O que será criado

| Item | Descrição |
|------|-----------|
| **Imagem** | `rustfs/rustfs:1.0.1` |
| **Container** | `rustfs` |
| **API S3** | porta `9000` (mapeada como `8000` no host) |
| **Console web** | porta `9001` (mapeada como `8001` no host) |
| **Volume** | `./appdata/rustfs/data` → `/data` |
| **Redes** | `web`, `database` |

### Variáveis de ambiente

| Variável | Descrição |
|----------|-----------|
| `RUSTFS_ACCESS_KEY` | Usuário administrador (*access key* do S3), definido no `.env` |
| `RUSTFS_SECRET_KEY` | Senha do administrador (*secret key* do S3), definida no `.env` |
| `RUSTFS_VOLUMES` | Pasta, dentro do container, em que os objetos são gravados (`/data`) |
| `RUSTFS_ADDRESS` | Endereço da API S3 (`0.0.0.0:9000`) |
| `RUSTFS_CONSOLE_ENABLE` / `RUSTFS_CONSOLE_ADDRESS` | Habilita a console web na porta `9001` |
| `TZ` | Fuso horário |

### Permissão da pasta de dados

O container do RustFS não é executado como `root` (usuário com UID `10001`). Antes de subir o serviço pela primeira vez, crie a pasta de dados e altere o dono dela, senão o RustFS não consegue gravar os arquivos:

```bash
mkdir -p ./appdata/rustfs/data
sudo chown -R 10001:10001 ./appdata/rustfs/data
```

### Endpoints expostos

| Serviço | Subdomínio (exemplo) | Uso |
|---------|----------------------|-----|
| API S3 | `rustfs.${DOMAIN}` | Upload/download pela API e leitura das imagens pelo navegador |
| Console | `rustfs-console.${DOMAIN}` | Interface web para buckets e objetos |

### Integração com a aplicação

A API Spring ([`../../server/`](../../server/README.md)) utiliza o cliente S3 do AWS SDK. No pipeline do Jenkins:

| Variável da API | Valor |
|-----------------|-------|
| `STORAGE_ENDPOINT` | `https://rustfs.${DOMAIN}` |
| `STORAGE_PORT` / `STORAGE_SECURE` | `443` / `true` |
| `STORAGE_ACCESS_KEY` / `STORAGE_SECRET_KEY` | Credencial global `pw45s_storage_id` do Jenkins (mesmos valores de `RUSTFS_ACCESS_KEY` e `RUSTFS_SECRET_KEY`) |
| `STORAGE_BUCKET_NAME` | `pw45s` |

O bucket `pw45s` é criado pela API no primeiro upload, com leitura pública dos objetos. A aplicação cliente exibe as imagens pela URL `https://rustfs.${DOMAIN}/pw45s/<arquivo>` (variável `VITE_STORAGE_URL`).

### DNS na Cloudflare

Conforme o README principal, criar registros tipo **A** para `rustfs` e `rustfs-console` apontando para o IP do Droplet na Digital Ocean.

## Execução isolada

Na pasta `rustfs/`, com o arquivo `.env` contendo `DOMAIN`, `RUSTFS_ACCESS_KEY` e `RUSTFS_SECRET_KEY`:

```bash
docker compose up -d
```

## Referências

- [RustFS — Documentação](https://docs.rustfs.com/)
- [RustFS — GitHub](https://github.com/rustfs/rustfs)
- [AWS SDK for Java 2.x — S3](https://docs.aws.amazon.com/sdk-for-java/latest/developer-guide/examples-s3.html)
- [Traefik — múltiplos roteadores no mesmo container](https://doc.traefik.io/traefik/routing/routers/)
