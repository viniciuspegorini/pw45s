# Spring Boot e integração com IA

Nesta aula a API REST desenvolvida com **Spring Boot 4** é integrada a provedores de Inteligência Artificial utilizando o [Spring AI](https://docs.spring.io/spring-ai/reference/):

- **OpenAI** e **DeepSeek**: modelos pagos, acessados por meio de uma chave de API (*API key*);
- **Ollama**: modelos executados localmente, sem custo e sem chave de API.

Também é apresentado um exemplo de chamada **HTTP direta** à API da OpenAI (sem o Spring AI), para comparar as duas abordagens.

O projeto é a continuação da [aula09](../../aula09/server/README.MD): mantém o armazenamento de arquivos com **RustFS** e a observabilidade com **OpenTelemetry + Grafana LGTM**. As chamadas aos modelos de IA também são monitoradas (quantidade de chamadas, erros, tokens consumidos e tempo de resposta).

---

## O que o exemplo cobre

| Abordagem | Onde está | Uso |
|-----------|-----------|-----|
| Spring AI (`ChatModel`) com OpenAI, DeepSeek ou Ollama | `MusicService`, `MusicController` | Geração de uma música com prompt parametrizado (`PromptTemplate`) e saída estruturada (`BeanOutputConverter` + `MusicDTO`). O provedor é escolhido pelo parâmetro `ai` |
| Spring AI + Ollama (local) | `OllamaDemoController` | Pergunta livre ao modelo local, sem autenticação |
| HTTP manual (OpenAI) | `ChatGPTService`, `ChatGPTController` | Requisição montada manualmente com o `RestClient`, sem o Spring AI |

---

## Requisitos

- **Java 25** e **Maven** (ou o wrapper do projeto: `mvnw.cmd` no Windows, `./mvnw` no Linux/macOS);
- **Docker**: o Spring Boot Docker Compose inicia automaticamente os containers do RustFS e do Grafana LGTM (`compose.yaml`);
- **Ollama** instalado, para utilizar os modelos locais (seção abaixo);
- Chaves de API da OpenAI e/ou DeepSeek, apenas se for utilizar esses provedores.

> A aplicação inicia normalmente **sem as chaves de API**. Nesse caso, as chamadas à OpenAI e ao DeepSeek retornam erro (HTTP 502) e o Ollama pode ser utilizado normalmente.

---

## Dependências

As versões dos *starters* são gerenciadas pelo `spring-ai-bom`. A versão **2.0.x** do Spring AI é compatível com o Spring Boot 4.0:

```xml
<properties>
    <spring-ai.version>2.0.2</spring-ai.version>
</properties>

<dependencies>
    <dependency>
        <groupId>org.springframework.ai</groupId>
        <artifactId>spring-ai-starter-model-openai</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.ai</groupId>
        <artifactId>spring-ai-starter-model-deepseek</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.ai</groupId>
        <artifactId>spring-ai-starter-model-ollama</artifactId>
    </dependency>
</dependencies>

<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>org.springframework.ai</groupId>
            <artifactId>spring-ai-bom</artifactId>
            <version>${spring-ai.version}</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>
```

---

## Chaves de API

### OpenAI

1. Acesse [https://platform.openai.com/](https://platform.openai.com/) e faça login (ou crie uma conta).
2. Abra a área de chaves de API: [https://platform.openai.com/api-keys](https://platform.openai.com/api-keys).
3. Clique em **Create new secret key** e copie o valor (**ele é exibido apenas uma vez**).
4. A conta precisa ter créditos: sem saldo, as chamadas retornam erro. Preços: [https://openai.com/api/pricing](https://openai.com/api/pricing).

### DeepSeek (opcional)

1. Crie uma conta e uma chave em [https://platform.deepseek.com/](https://platform.deepseek.com/).
2. Assim como na OpenAI, a conta precisa ter saldo (sem saldo a API retorna `402 Insufficient Balance`).

### Arquivo `.env`

As chaves **nunca** devem ser escritas no código ou no `application.yml`. Na pasta `server`, copie o arquivo de exemplo e preencha os valores:

```bash
cp .env.example .env
```

```env
SPRING_PORT=8080
OPENAI_API_KEY=sk-...
OPENAI_MODEL=gpt-4o-mini
DEEPSEEK_API_KEY=sk-...
OLLAMA_BASE_URL=http://localhost:11434
OLLAMA_MODEL=llama3.2
```

O arquivo `.env` está no `.gitignore` e não deve ser enviado ao repositório.

O próprio Spring Boot lê o arquivo, sem bibliotecas extras, por meio da propriedade `spring.config.import`. Se o arquivo não existir, ele é ignorado. Variáveis de ambiente do sistema operacional têm precedência sobre os valores do `.env`:

```yaml
spring:
  config:
    import: optional:file:.env[.properties]
```

---

## Instalar e usar o Ollama (modelo local)

1. **Download e instalação**: [https://ollama.com/download](https://ollama.com/download) (Windows, macOS ou Linux). O serviço é executado na porta **11434**.
2. **Baixe o modelo** configurado no projeto (padrão `llama3.2`, aproximadamente 2 GB):

   ```bash
   ollama pull llama3.2
   ```

3. Teste no terminal:

   ```bash
   ollama run llama3.2 "Diga olá em uma frase."
   ```

Para utilizar outro modelo (por exemplo `mistral` ou `qwen2.5`), baixe-o com `ollama pull` e altere a variável `OLLAMA_MODEL`.

> A primeira chamada pode demorar (o modelo é carregado na memória). Sem placa de vídeo, o modelo é executado na CPU e as respostas são mais lentas.

---

## Configuração no `application.yml`

```yaml
spring:
  ai:
    openai:
      # Sem chave a aplicação inicia normalmente, mas as chamadas à OpenAI retornam erro de autenticação
      api-key: ${OPENAI_API_KEY:sem-chave}
      chat:
        options:
          model: ${OPENAI_MODEL:gpt-4o-mini}
    deepseek:
      api-key: ${DEEPSEEK_API_KEY:sem-chave}
      chat:
        options:
          model: deepseek-chat
          temperature: 0.8
    ollama:
      base-url: ${OLLAMA_BASE_URL:http://localhost:11434}
      chat:
        options:
          model: ${OLLAMA_MODEL:llama3.2}
```

O valor após `:` é o padrão utilizado quando a variável não existe. O texto `sem-chave` permite iniciar a aplicação sem as chaves (o Spring AI exige que a propriedade `api-key` tenha algum valor).

---

## Executar o projeto

Na pasta `server`:

```bash
./mvnw spring-boot:run
```

O Spring Boot Docker Compose inicia os containers do RustFS e do Grafana LGTM. A API fica disponível na porta **8080** (alterável com `SPRING_PORT`).

---

## Endpoints

O **Spring Security** exige o token JWT na maioria das rotas. Para obter o token, cadastre um usuário (`POST /users`) e faça login (`POST /login`).

| Método e caminho | Autenticação | Descrição |
|------------------|--------------|-----------|
| `GET /ai/ollama/ask?prompt=...` | Não | Resposta em texto do modelo local (Ollama) |
| `GET /music?ai=...` | Sim (JWT) | Música gerada com um prompt fixo |
| `GET /music/parameters?genre=...&theme=...&ai=...` | Sim (JWT) | Música com gênero e tema informados, retornada como JSON (`MusicDTO`) |
| `POST /gpt` | Sim (JWT) | Corpo: texto da pergunta. Resposta da OpenAI via HTTP manual (`ChatGPTService`) |

O parâmetro `ai` aceita `OPENAI` (padrão), `DEEPSEEK` ou `OLLAMA`. Um valor inválido retorna HTTP 400.

Exemplo de resposta de `GET /music/parameters?genre=rock&theme=café e bugs&ai=OLLAMA`:

```json
{
  "title": "Café e Bugs",
  "music": "Verso 1:\nCafé quente na mão,\nbugs dançando ao redor...",
  "genre": "rock",
  "theme": "café e bugs"
}
```

### Tratamento de erros

Quando o provedor falha (chave inválida, conta sem saldo, Ollama parado ou modelo não baixado), a API retorna uma mensagem genérica para o cliente e registra o erro detalhado apenas no log:

| Situação | Resposta |
|----------|----------|
| Erro na OpenAI ou DeepSeek (`/music`, `/gpt`) | `502 Bad Gateway` |
| Ollama indisponível (`/ai/ollama/ask`) | `503 Service Unavailable` |

---

## Trechos de código

### Um serviço para todos os provedores (`MusicService`)

Todos os provedores implementam a interface `ChatModel`. O Spring AI registra um *bean* para cada provedor (`openAiChatModel`, `deepSeekChatModel` e `ollamaChatModel`). Com vários provedores no *classpath*, é necessário indicar qual *bean* injetar com `@Qualifier`:

```java
@Service
public class MusicService {

    private final Map<AIProvider, ChatModel> chatModels;

    public MusicService(@Qualifier("openAiChatModel") ChatModel openAiChatModel,
                        @Qualifier("deepSeekChatModel") ChatModel deepSeekChatModel,
                        @Qualifier("ollamaChatModel") ChatModel ollamaChatModel, ...) {
        this.chatModels = Map.of(
                AIProvider.OPENAI, openAiChatModel,
                AIProvider.DEEPSEEK, deepSeekChatModel,
                AIProvider.OLLAMA, ollamaChatModel);
    }

    public String getMusic(AIProvider provider) {
        return chatModels.get(provider).call(BASE_MUSIC);
    }
}
```

### Prompt parametrizado e saída estruturada

O `PromptTemplate` substitui as variáveis `{genre}`, `{theme}` e `{format}` do prompt. O `BeanOutputConverter` gera as instruções de formato (um JSON Schema baseado na classe `MusicDTO`) e converte o JSON retornado pelo modelo em um objeto Java:

```java
BeanOutputConverter<MusicDTO> outputConverter = new BeanOutputConverter<>(MusicDTO.class);

String promptString = """
    Escreva uma música do gênero {genre} sobre {theme}.
    A música deve ter 2 versos e um refrão e ser escrita em português do Brasil.
    O campo "title" deve conter o título, o campo "music" a letra completa
    e os campos "genre" e "theme" os valores informados.
    {format}
    """;

PromptTemplate promptTemplate = new PromptTemplate(promptString);
promptTemplate.add("genre", genre);
promptTemplate.add("theme", theme);
promptTemplate.add("format", outputConverter.getFormat());

ChatResponse response = chatModels.get(provider).call(promptTemplate.create());
return outputConverter.convert(response.getResult().getOutput().getText());
```

Modelos locais menores podem gerar um JSON incompleto. No Ollama é possível **forçar** a resposta no formato do JSON Schema com a opção `format`. As opções informadas na requisição substituem as do `application.yml`, por isso o modelo também precisa ser informado:

```java
Prompt prompt = provider == AIProvider.OLLAMA
        ? promptTemplate.create(OllamaChatOptions.builder()
                .model(ollamaModel)
                .format(outputConverter.getJsonSchemaMap())
                .build())
        : promptTemplate.create();
```

### Chamada HTTP manual à OpenAI (`ChatGPTService`)

Sem o Spring AI, é necessário montar a requisição e interpretar a resposta da API manualmente:

```java
Map<String, Object> request = Map.of(
        "model", model,
        "messages", List.of(Map.of("role", "user", "content", userMessage)));

Map<String, Object> response = restClient.post()
        .contentType(MediaType.APPLICATION_JSON)
        .body(request)
        .retrieve()
        .body(Map.class);

// { "choices": [ { "message": { "role": "assistant", "content": "..." } } ] }
List<Map<String, Object>> choices = (List<Map<String, Object>>) response.get("choices");
Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
return (String) message.get("content");
```

Com o Spring AI, o mesmo resultado é obtido com `chatModel.call(userMessage)`, e trocar de provedor exige apenas trocar o *bean* injetado.

---

## Observabilidade das chamadas de IA

O Spring AI registra métricas e *traces* de cada chamada aos modelos, que são exportados via OpenTelemetry (configuração da [aula09](../../aula09/server/README.MD)). As principais métricas são:

| Métrica | Descrição | Principais rótulos |
|---------|-----------|--------------------|
| `gen_ai_client_operation_milliseconds_count` / `_sum` | Quantidade e duração das chamadas | `gen_ai_system` (openai, deepseek, ollama), `gen_ai_request_model`, `error` |
| `gen_ai_client_token_usage_total` | Tokens consumidos | `gen_ai_request_model`, `gen_ai_token_type` (input, output, total) |

O dashboard **API - Observabilidade e IA (aula10)** é provisionado automaticamente no Grafana (http://localhost:3000 > **Dashboards** > pasta **Aula10**). Ele contém os painéis da aula09 e uma seção **Inteligência Artificial** com:

- quantidade de chamadas aos modelos, chamadas com erro, tokens consumidos e tempo médio de resposta;
- chamadas por provedor, modelo e resultado (`error=none` indica sucesso);
- tokens por minuto, por modelo e tipo;
- tempo médio de resposta por modelo.

O consumo de tokens é a base da cobrança nos provedores pagos, por isso é importante monitorá-lo.

No Tempo também é possível visualizar o *span* de cada chamada ao modelo (por exemplo `chat llama3.2`) dentro do *trace* da requisição HTTP.

---

## Referências

- Spring AI: [https://docs.spring.io/spring-ai/reference/](https://docs.spring.io/spring-ai/reference/)
- Spring AI - Saída estruturada: [https://docs.spring.io/spring-ai/reference/api/structured-output-converter.html](https://docs.spring.io/spring-ai/reference/api/structured-output-converter.html)
- Spring AI - Ollama: [https://docs.spring.io/spring-ai/reference/api/chat/ollama-chat.html](https://docs.spring.io/spring-ai/reference/api/chat/ollama-chat.html)
- Spring AI - Observabilidade: [https://docs.spring.io/spring-ai/reference/observability/index.html](https://docs.spring.io/spring-ai/reference/observability/index.html)
- OpenAI API keys: [https://platform.openai.com/api-keys](https://platform.openai.com/api-keys)
- Ollama: [https://ollama.com/](https://ollama.com/)
