# Adicionando Observabilidade em uma API REST com Spring Boot

Neste projeto será apresentado o módulo `spring-boot-starter-opentelemetry` que foi introduzido no **Spring Boot 4.0.x**, proporcionando uma forma simplificada de integrar a observabilidade do **OpenTelemetry** em uma aplicação Spring Boot.

O Spring Boot 4.0 adicionou o OpenTelemetry *starter* criado pelo time de desenvolvimento do Spring. Anteriormente era necessário a adição de diversas bibliotecas e configurações para adicionar observabilidade, agora o *starter* provê::

- **Single dependency**: Basta adicionar a dependência `spring-boot-starter-opentelemetry`
- **Automatic OTLP export**: Métricas e *traces* são exportados via protocolo OTLP
- **Micrometer integration**: Utiliza o Micrometer para exportar os *traces* no formato OTLP
- **Vendor-neutral**: Integra com ferramentas compatíveis com OpenTelemetry (Grafana, Jaeger, etc.)

> **Como isso é possível?** [Modularização do Spring Boot](https://spring.io/blog/2025/10/28/modularizing-spring-boot)


### Como adicionar o OpenTelemetry Protocol (OTLP) a uma aplicação desenvolvida com Spring Boot?

Existem três maneiras de configurar o OpenTelemetry com Spring Boot:

1. **OpenTelemetry Java Agent** - Não necessita de ajustes no código-fonte da aplicação, mas pode ter problemas de compatibilidade de versão.
2. **Third-party OpenTelemetry Starter** - Faz parte do projeto OTel, mas depende de dependências em versão alfa.
3. **Spring Boot Starter (será utilizado no projeto)—** - Possui suporte oficial do time que desenvolve o Spring, está estável, possui fácil integração.

A ideia principal é utilizar **o protocolo (OTLP)**, não uma biblioteca específica. O Spring Boot utiliza o Micrometer internamente, mas exporta tudo via OTLP para qualquer *backend* compatível

### Spring Boot Actuator

O **Spring Boot Actuator** é a abordagem tradicional do Spring para observabilidade e pronta para produção. Veja como ele se compara:

| Aspecto | Spring Boot Actuator | OpenTelemetry Starter |
|--------|---------------------|----------------------|
| **Protocolo** | Prometheus, OTLP, JMX, + vários outros | outrosOTLP (neutro em relação a fornecedor) |
| **Tracing Distribuído** | Nativo via Micrometer Tracing (requer dependência de ponte/bridge) | Nativo, automático |
| **Backend Lock-in** | Neutro via Micrometer (suporta mais de 15 backends, incluindo OTLP) | unciona com qualquer backend OTLP |
| **Health Checks** | Nativo em `/actuator/health` | Não incluído (requer o Actuator) |
| **Prontidão para Produção** | Suíte completa (info, env, beans, métricas, etc.) | Focado apenas em telemetria |
| **Complexidade de Setup** | Mais endpoints para configurar/proteger | Endpoint OTLP único |
| **Dependências (Spring Boot 4)** | `spring-boot-starter-actuator` + deps de ponte | `spring-boot-starter-opentelemetry` |

**Escolha o Actuator quando:**
- Você precisar de verificações de saúde (*health checks*) e provas de *readiness/liveness* para o Kubernetes.    
  -Você quiser expor informações da aplicação, variáveis de ambiente ou detalhes dos *beans* do Spring.
- Sua *stack* de monitoramento já for baseada em Prometheus com o modelo de *scraping* (raspagem de dados).

**Escolha o OpenTelemetry Starter quando:**

- Você desejar observabilidade neutra em relação ao fornecedor (trocar de *backend* facilmente).
- O rastreamento distribuído (*distributed tracing*) entre serviços for uma prioridade.
- Você preferir telemetria baseada em "*push*" (envio ativo) em vez de "*pull-based scraping*" (raspagem de dados).

**Observação:** Eles não são mutuamente exclusivos — muitas aplicações em produção utilizam ambos (Actuator para _health/readiness_ e OTel para telemetria).

## Pré-requisitos

- Java 17+
- Maven
- Docker (para o Grafana LGTM)

A *stack* **LGTM** é a suíte de observabilidade de código aberto da Grafana Labs. O acrônimo significa:

-   **Loki** — para logs (sistema de agregação de logs)
-   **Grafana** — para visualização e *dashboards*
-   **Tempo** — para *traces* (*backend* de rastreamento distribuído)
-   **Mimir** — para métricas (armazenamento de longo prazo para métricas do Prometheus)

## Dependencies

A dependência principal é o OpenTelemetry *starter*:

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-opentelemetry</artifactId>
</dependency>
```

O starter inclui:
- API do OpenTelemetry
- Ponte (*bridge*) do Micrometer Tracing para OpenTelemetry
- Exportadores OTLP para métricas e *traces*

## Configuração

```yaml
spring:
  application:
    name: server

management:
  tracing:
    sampling:
      probability: 1.0  # 100% sampling for development
  otlp:
    metrics:
      export:
        url: http://localhost:4318/v1/metrics
  opentelemetry:
    tracing:
      export:
        otlp:
          endpoint: http://localhost:4318/v1/traces
    logging:
      export:
        otlp:
          endpoint: http://localhost:4318/v1/logs
```

**Observações:**
-   **sampling.probability**: Defina como `1.0` para desenvolvimento (coleta todos os *traces*). Use valores menores em produção (o padrão é `0.1`).
-   **Porta 4318**: *Endpoint* HTTP OTLP (use a porta 4317 para gRPC).
-   **O módulo `spring-boot-docker-compose`**: Configura automaticamente esses *endpoints* ao utilizar o Docker Compose.

### Entendendo a Configuração de Exportação OTLP

**`management.otlp.metrics.export.url`** — Informa ao Spring Boot para onde enviar as **métricas** (contadores, gauges, histogramas, como contagem de requisições, tempos de resposta e uso de memória). Os dados são enviados para um coletor compatível com OTLP.

**`management.opentelemetry.tracing.export.otlp.endpoint`** — Informa ao Spring Boot para onde enviar os ***traces*** (dados de fluxo e temporização que mostram como as requisições se movem pela sua aplicação, com spans detalhando cada operação e duração).

**Por que duas configurações separadas?** A observabilidade do Spring Boot evoluiu ao longo do tempo:
-   As métricas utilizam o exportador OTLP do Micrometer (por isso o prefixo `management.otlp.metrics`).
-   Os traces utilizam a ponte de rastreamento do OpenTelemetry (por isso o prefixo `management.opentelemetry.tracing`).

Ambos enviam dados para o mesmo coletor (porta 4318), mas os caminhos de configuração diferem devido à forma como as bibliotecas são integradas.

## Visualizar os *traces* no Grafana

Execute a aplicação, que como está configurada com o Spring Boot Docker Compose, irá iniciar o o conteiner LGTM junto com a aplicação. Então, basta acessar:

- http://localhost:3000
- Acessar o menu **Explore**
- Selecionar **Tempo** como *data source*
- Clicar em **Search** e selecionar o *service** "server"
- Clicar em um *trace* para visualizar os detalhes do *span*

4. **Visualizar as métricas:**

- No Grafana, acessar **Explore**
- Selecionar **Prometheus** como data source
- Query for metrics like `http_server_requests_seconds_count`

## Endpoints

Executar os diferentes *endpoits* da API:

| Endpoint | Description |
|----------|-------------|
| `GET /categories` | Lista de categorias |
| `GET /products` | Lista de Produtos |
| `POST /user` | Cadastro de um novo usuário |


## O que você obtém automaticamente

Com o `spring-boot-starter-opentelemetry`, você obtém instrumentação automática para:

-   **Requisições de servidor HTTP** (todos os *endpoints* de *controllers*)

-   **Requisições de cliente HTTP** (RestTemplate, RestClient, WebClient)

-   **Chamadas de banco de dados JDBC**

-   **IDs de *Trace/Span* nos logs**


## Visualizando Logs com Contexto de Rastreamento (*Trace Context*)

Os *logs* da aplicação incluem automaticamente os IDs de _trace_ e _span_. Por exemplo:
```
2026-05-01T00:32:44.218-03:00 ERROR 115056 --- [server] [nio-8080-exec-1] [ee4747834c4278bd2a4bbfbb2028a6ad-234771242b440c92] b.e.u.p.p.s.e.ExceptionHandlerAdvice     : Error: validation error
```

O contexto de rastreamento (*trace context*) aparece como `[traceId-spanId]` (ex., `ee4747834c4278bd2a4bbfbb2028a6ad-234771242b440c92`).


### Entendendo os IDs de Trace e Span

-   **Trace ID** (`ee4747834c4278bd2a4bbfbb2028a6ad`): Identifica todo o fluxo da requisição através de todos os serviços. Cada log e _span_ de uma única requisição compartilha este mesmo ID.

-   **Span ID** (`234771242b440c92`): Identifica uma operação específica dentro do _trace_. Uma única requisição pode ter múltiplos _spans_ (ex: *controller* → banco de dados → API externa).


### Por que isso é importante

1.  **Salte do trace para os logs**: No Grafana, ao visualizar um _trace_ no Tempo, é possível clicar em um _span_ para ver apenas os *logs* emitidos durante aquela operação exata, não sendo necessário pesquisar entre milhares de linhas de log.

2.  **Salte dos logs para o trace**: Se você encontrar um erro nos *logs*, basta copiar o _trace ID_ e pesquisá-lo no Tempo para ver o fluxo completo da requisição e identificar exatamente onde ela falhou.

3.  **Depure operações específicas**: Se uma requisição chama múltiplos serviços ou realiza diversas operações, o _span ID_ informa exatamente qual parte da requisição gerou uma determinada entrada de *log*.

### Example: Multi-Span Request

```
Request: POST /user
├── Span A (controller) ─── *logs* exibe o ID do span A
│   └── Span B (service) ─── *logs* exibe o ID do span C
```

Ou seja, ao visualizar um logo com o ID do span B, o mesmo aconteceu dentro da camada *service*, não no *controller*.

## Exportando Logs via OTLP

Esta demonstração está configurada para exportar logs para o Grafana/Loki via OTLP. Isso permite visualizar, pesquisar e correlacionar *logs* com _traces_ diretamente no Grafana.

### Importante: Requisito de Versão do Spring Boot

**O Spring Boot 4.0.1 ou posterior é necessário** para a exportação de logs via OTLP sem o módulo _actuator_.

No Spring Boot 4.0.0, a configuração `OtlpLoggingAutoConfiguration` fazia parte do módulo _actuator_, exigindo que você adicionasse o `spring-boot-starter-actuator` apenas para exportar *logs*. Isso foi corrigido no [issue #48488](https://github.com/spring-projects/spring-boot/issues/48488) e lançado no Spring Boot 4.0.1, que moveu a autoconfiguração de exportação de logs para o módulo principal do OpenTelemetry.

### Como tudo será integrado:

O Spring Boot fornece a autoconfiguração para exportar *logs* no formato OTLP, mas não instala um _appender_ no Logback por padrão. Para habilitar a exportação de logs, será necessário:

#### 1. Adicionar a Dependência do Logback Appender

```xml
<dependency>
    <groupId>io.opentelemetry.instrumentation</groupId>
    <artifactId>opentelemetry-logback-appender-1.0</artifactId>
    <version>2.21.0-alpha</version>
</dependency>

```

> **Observação:** O sufixo `-alpha` indica que isto ainda é marcado como instável pelo projeto OpenTelemetry. Atualmente, não existem versões estáveis (não-alpha) deste _appender_.

#### 2. Criar o arquivo `logback-spring.xml`

Criar o arquivo `src/main/resources/logback-spring.xml` para adicionar o OpenTelemetry *appender*:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<configuration>
    <include resource="org/springframework/boot/logging/logback/base.xml"/>

    <appender name="OTEL" class="io.opentelemetry.instrumentation.logback.appender.v1_0.OpenTelemetryAppender">
    </appender>

    <root level="INFO">
        <appender-ref ref="CONSOLE"/>
        <appender-ref ref="OTEL"/>
    </root>
</configuration>
```


Esta configuração:
-   Importa a configuração de log padrão do **Spring Boot**
-   Adiciona um _appender_ do **OpenTelemetry** que envia os logs para o SDK do OpenTelemetry
-   Vincula ambos os _appenders_ (console e OTEL) ao _root logger_

#### 3. Criando o *bean* de instalação do *Appender*

O _appender_ do OpenTelemetry precisa saber qual instância do `OpenTelemetry` deve utilizar. Criar o componente:

```java
@Component
class InstallOpenTelemetryAppender implements InitializingBean {

    private final OpenTelemetry openTelemetry;

    InstallOpenTelemetryAppender(OpenTelemetry openTelemetry) {
        this.openTelemetry = openTelemetry;
    }

    @Override
    public void afterPropertiesSet() {
        OpenTelemetryAppender.install(this.openTelemetry);
    }
}
```

Este bean:
-   Obtém a instância de `OpenTelemetry` autoconfigurada e injetada pelo **Spring Boot**
-   Instala-a no _appender_ do Logback após o contexto do Spring estar pronto
-   Habilita o _appender_ para enviar logs através do SDK do OpenTelemetry para o _endpoint_ OTLP

#### 4. Autoconfiguração do Docker Compose

Ao utilizar o `spring-boot-docker-compose` com a imagem `grafana/otel-lgtm`, o Spring Boot **configura automaticamente** os endpoints OTLP para métricas, *traces* e logs. Não será necessário especificar as URLs dos *endpoints* explicitamente no arquivo `application.yaml`.

A autoconfiguração detecta o contêiner em execução e configura:
-   *Endpoint* de métricas: `http://localhost:4318/v1/metrics`
-   *Endpoint* de *traces*: `http://localhost:4318/v1/traces`
-   *Endpoint* de *logs*: `http://localhost:4318/v1/logs`

### Visualizando os logs no Grafana

1. http://localhost:3000
2. Acessar o menu **Explore**
3. Selecionar **Loki** como *data source*
4. Query for logs: `{service_name="server"}`
5. Clicar em uma entrada de *log* para ver seu contexto de rastreamento e, em seguida, clicar no **Trace ID** para saltar diretamente para o _trace_ no Tempo.

## Próximos passos

Recursos extras para aprimorar o *log*:

1. **Adicionar spans customizados** utilizando a anotação `@Observed`:

```java
@Observed(name = "my-operation")
public void myMethod() {
    // ...
}
```

2. **Adicionar trace ID em respostas** utilizando um *filter servlet*:

```java
@Component
class TraceIdFilter extends OncePerRequestFilter {

  private final Tracer tracer;

  TraceIdFilter(Tracer tracer) {
    this.tracer = tracer;
  }

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
    String traceId = getTraceId();
    if (traceId != null) {
      response.setHeader("X-Trace-Id", traceId);
    }
    filterChain.doFilter(request, response);
  }

  private @Nullable String getTraceId() {
    TraceContext context = this.tracer.currentTraceContext().context();
    return context != null ? context.traceId() : null;
  }

}
```

3. **Realizar chamadas em serviços distribuídos** para poder visualizar *traces* distribuídos entre aplicações distintas.

## Referências

- [OpenTelemetry with Spring Boot - Spring Blog](https://spring.io/blog/2025/11/18/opentelemetry-with-spring-boot)
- [Spring Boot Tracing Documentation](https://docs.spring.io/spring-boot/reference/actuator/tracing.html)
- [OpenTelemetry Documentation](https://opentelemetry.io/docs/)
- [Micrometer Tracing](https://micrometer.io/docs/tracing)