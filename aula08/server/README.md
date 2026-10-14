# Server - API Spring Boot com Observabilidade (ELK + Filebeat)

Este projeto e uma API REST desenvolvida com Spring Boot, baseada no projeto da aula07. O projeto inclui autenticacao JWT (e Google), upload de arquivos com RustFS, persistencia com JPA/Flyway e monitoramento de logs com Elasticsearch, Kibana e Filebeat.

## Tecnologias utilizadas

- Java 25
- Spring Boot 4.0.6
- Spring Web MVC, Spring Data JPA, Spring Security, Validation
- H2 Database (ambiente de desenvolvimento)
- Flyway (migracoes de banco)
- JWT (`java-jwt`)
- OpenAPI/Swagger (`springdoc-openapi`)
- Actuator + Spring Boot Admin Client 4.0.4
- RustFS (armazenamento compativel com S3)
- Log estruturado nativo do Spring Boot (JSON no formato ECS - Elastic Common Schema)
- Docker Compose (RustFS, Elasticsearch, Kibana e Filebeat)

## Estrutura principal

- `src/main/java`: codigo da aplicacao (controllers, services, security etc.)
- `src/main/resources`: configuracoes e migrations Flyway
- `docker-compose.yml`: RustFS e stack de observabilidade (Elastic, Kibana e Filebeat)
- `filebeat/`: imagem e configuracao do Filebeat
- `logs/`: arquivos de log da aplicacao (consumidos pelo Filebeat)

## Pre-requisitos

- Java 25
- Maven 3.9+ (ou usar `mvnw`/`mvnw.cmd`)
- Docker e Docker Compose

## Como executar a API

A aplicacao sobe por padrao com perfil `dev` (definido em `application.yml`).

## Como subir Elasticsearch, Kibana e Filebeat

No diretorio `server/`:

```bash
docker compose up -d --build
```

Servicos e portas:

- RustFS (API S3): `http://localhost:9000`
- RustFS (console): `http://localhost:9001`
- Elasticsearch: `http://localhost:9200`
- Kibana: `http://localhost:5601` (dashboard importado automaticamente pelo servico `kibana-setup`)
- Filebeat: envia logs JSON da pasta `logs/` para o Elasticsearch

> Observacao: no `docker-compose.yml`, a seguranca do Elasticsearch esta desabilitada para facilitar uso local (`xpack.security.enabled=false`).

## Fluxo de logs

1. A API grava logs JSON no formato ECS em `logs/app.json`, usando o log estruturado nativo do Spring Boot (`logging.structured.format.file: ecs` no `application.yml`). O console continua no formato texto.
2. O Filebeat le `logs/*.json` via volume Docker.
3. O Filebeat envia os eventos para o indice `spring-api-logs-YYYY.MM.DD`.
4. O Kibana permite explorar e visualizar esses logs.

O mesmo arquivo `logs/app.json` pode ser visualizado na aba **Logfile** do Spring Boot Admin.

### Campos adicionados em cada requisicao (`CrudLogFilter`)

O filtro `CrudLogFilter` executa antes do Spring Security e adiciona ao MDC os campos abaixo, que aparecem em todas as linhas de log da requisicao e podem ser filtrados no Kibana:

| Campo | Descricao |
|-------|-----------|
| `requestId` | ID de correlacao. Reaproveita o header `X-Request-Id` enviado pelo cliente ou gera um UUID; o valor e devolvido no header `X-Request-Id` da resposta |
| `crudOperation` | `CREATE`, `READ`, `UPDATE`, `DELETE`, `LOGIN` ou `OTHER` |
| `httpMethod` / `endpoint` | Metodo HTTP e URI da requisicao |
| `status` / `durationMs` | Status HTTP e tempo de resposta, gravados no log de acesso ao final da requisicao |

Exemplo de linha de log (log de acesso):

```json
{"@timestamp":"2026-10-14T01:11:13.818Z","log":{"level":"INFO","logger":"br.edu.utfpr.pb.pw45s.server.filter.CrudLogFilter"},"service":{"name":"server"},"message":"POST /login -> 401","crudOperation":"LOGIN","endpoint":"/login","httpMethod":"POST","durationMs":"253","requestId":"88d487a0-3636-48d7-bf4a-1459983f82d6","status":"401"}
```

Exemplos de consulta no Kibana (KQL): `requestId : "88d487a0-..."` (todos os logs de uma requisicao), `status >= 500`, `crudOperation : "LOGIN" and status : 401` (falhas de login).

As requisicoes para `/actuator/**` (feitas periodicamente pelo Spring Boot Admin) nao sao registradas.

## Dashboard pronto no Kibana

Ao executar `docker compose up -d --build`, o servico `kibana-setup` aguarda o Kibana ficar disponivel e importa o arquivo `kibana/dashboard.ndjson`, que contem:

- o *data view* `spring-api-logs*` (campo de tempo `@timestamp`);
- o dashboard **API - Observabilidade (aula08)**, com os paineis:
  - Total de requisicoes, tempo medio de resposta, erros 5xx e falhas de login (401);
  - Requisicoes por operacao (CRUD) ao longo do tempo e distribuicao das operacoes;
  - Requisicoes por status HTTP e tempo medio por endpoint;
  - Tabela com os endpoints mais acessados (quantidade, tempo medio e maximo);
  - Logs de `WARN` e `ERROR` (com `endpoint` e `requestId` para correlacao).

Para acessar: http://localhost:5601 > **Dashboards** > **API - Observabilidade (aula08)**. O periodo padrao e a ultima 1 hora, com atualizacao automatica a cada 10 segundos.

Os paineis (exceto os logs de WARN/ERROR) consideram apenas as linhas do log de acesso do `CrudLogFilter` (consulta `durationMs:*`).

### Alterando o dashboard

Depois de editar o dashboard no Kibana, exporte-o novamente para manter o arquivo atualizado no repositorio:

```bash
curl -X POST "http://localhost:5601/api/saved_objects/_export" -H "kbn-xsrf: true" -H "Content-Type: application/json"   -d '{"objects":[{"type":"dashboard","id":"api-observabilidade"}],"includeReferencesDeep":true,"excludeExportDetails":true}'   -o kibana/dashboard.ndjson
```

Para importar manualmente: **Stack Management** > **Saved Objects** > **Import** e selecionar o arquivo `kibana/dashboard.ndjson`.

## Health check do RustFS

A classe `StorageHealthIndicator` verifica se o RustFS esta acessivel e se o bucket configurado existe. O resultado aparece como componente `storage` em `http://localhost:8080/actuator/health` e na tela de detalhes do Spring Boot Admin. Se o RustFS estiver fora do ar, o status da aplicacao passa a ser `DOWN`.

## Endpoints e acessos uteis

- Swagger UI: `http://localhost:8080/swagger-ui/index.html`
- OpenAPI: `http://localhost:8080/v3/api-docs`
- H2 Console (dev): `http://localhost:8080/h2-console`
- Actuator: `http://localhost:8080/actuator`
- Spring Boot Admin (projeto `admin`): `http://localhost:8081`
- Kibana: `http://localhost:5601`

## Autenticacao

- O projeto usa JWT para autenticar requisicoes protegidas.
- Cadastro de usuario (`POST /users`) e endpoints publicos (`/swagger-ui/**`, `/v3/**`, `/actuator/**`, `/h2-console/**`) sao liberados.
- Para os demais endpoints, envie o token no header:

```text
Authorization: Bearer <seu_token>
```

## Endpoints da API (resumo)

- `POST /users` e `GET /users`
- `GET/POST/PUT/DELETE /products`
- `GET/POST/PUT/DELETE /categories`

> Existem tambem operacoes auxiliares como paginacao, contagem e verificacao de existencia nos recursos CRUD.

## Banco de dados e migracoes

- Em `dev`, o projeto usa H2 em memoria.
- As migracoes sao executadas via Flyway:
  - `db/dev`
  - `db/prod`
  - `db/test`

## Encerrando os servicos Docker

```bash
docker compose down
```

Se quiser remover tambem o volume de dados do Elasticsearch:

```bash
docker compose down -v
```

