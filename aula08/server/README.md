# Server - API Spring Boot com Observabilidade (ELK + Filebeat)

Este projeto e uma API REST desenvolvida com Spring Boot. O projeto inclui autenticacao JWT, persistencia com JPA/Flyway e monitoramento de logs com Elasticsearch, Kibana e Filebeat.

## Tecnologias utilizadas

- Java 24
- Spring Boot 3.5.5
- Spring Web, Spring Data JPA, Spring Security, Validation
- H2 Database (ambiente de desenvolvimento)
- Flyway (migracoes de banco)
- JWT (`java-jwt`)
- OpenAPI/Swagger (`springdoc-openapi`)
- Actuator
- Logback JSON (`logstash-logback-encoder`)
- Docker Compose (Elasticsearch, Kibana e Filebeat)

## Estrutura principal

- `src/main/java`: codigo da aplicacao (controllers, services, security etc.)
- `src/main/resources`: configuracoes, migrations Flyway e logback
- `docker-compose.yml`: stack de observabilidade (Elastic, Kibana e Filebeat)
- `filebeat/`: imagem e configuracao do Filebeat
- `logs/`: arquivos de log da aplicacao (consumidos pelo Filebeat)

## Pre-requisitos

- Java 24
- Maven 3.9+ (ou usar `mvnw`/`mvnw.cmd`)
- Docker e Docker Compose

## Como executar a API

A aplicacao sobe por padrao com perfil `dev` (definido em `application.properties`).

## Como subir Elasticsearch, Kibana e Filebeat

No diretorio `server/`:

```bash
docker compose up -d --build
```

Servicos e portas:

- Elasticsearch: `http://localhost:9200`
- Kibana: `http://localhost:5601`
- Filebeat: envia logs JSON da pasta `logs/` para o Elasticsearch

> Observacao: no `docker-compose.yml`, a seguranca do Elasticsearch esta desabilitada para facilitar uso local (`xpack.security.enabled=false`).

## Fluxo de logs

1. A API grava logs JSON em `logs/app.json` (configurado em `logback-spring.xml`).
2. O Filebeat le `logs/*.json` via volume Docker.
3. O Filebeat envia os eventos para o indice `spring-api-logs-YYYY.MM.DD`.
4. O Kibana permite explorar e visualizar esses logs.

## Endpoints e acessos uteis

- Swagger UI: `http://localhost:8080/swagger-ui/index.html`
- OpenAPI: `http://localhost:8080/v3/api-docs`
- H2 Console (dev): `http://localhost:8080/h2-console`
- Actuator: `http://localhost:8080/actuator`
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

