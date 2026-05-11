# Spring Boot e integração com IA 

A aula 10 é um exemplo de uma API REST com **Spring Boot 4** integrada a provedores de IA: **OpenAI** e **DeepSeek** via [Spring AI](https://docs.spring.io/spring-ai/reference/), **Ollama** para modelo local, e um exemplo adicional com **HTTP direto** à API de chat da OpenAI (`RestTemplate`).

---

## O que o exemplo cobre

| Abordagem | Onde está | Uso |
|-----------|-----------|-----|
| Spring AI + OpenAI (`ChatModel`) | `MusicService`, starter `spring-ai-starter-model-openai` | Prompt simples e saída estruturada (`BeanOutputConverter` + `MusicDTO`) |
| Spring AI + DeepSeek | `DeepSeekMusicService`, `spring-ai-starter-model-deepseek` | Mesmo fluxo de música, escolhido via parâmetro na rota |
| Spring AI + Ollama (local) | `OllamaDemoController`, `spring-ai-starter-model-ollama` | `GET` de teste sem JWT (ver segurança abaixo) |
| HTTP manual (OpenAI) | `ChatGPTService`, `ChatGPTController` | `POST /gpt` com corpo texto; usa a REST da OpenAI diretamente |

Documentação oficial do Spring AI: [Getting started](https://docs.spring.io/spring-ai/reference/getting-started.html) e [Ollama Chat](https://docs.spring.io/spring-ai/reference/api/chat/ollama-chat.html).

---

## Requisitos

- **Java 21** ou superior (definido no `pom.xml`).
- **Maven** (ou use o wrapper na raiz do módulo: `mvnw.cmd` no Windows, `./mvnw` no Linux/macOS).
- Conta e chaves nos provedores que você for habilitar (OpenAI, DeepSeek opcional).
- **Ollama** instalado apenas se for testar o endpoint local descrito mais abaixo.

---

## Criar e usar a API Key da OpenAI

1. Acesse [https://platform.openai.com/](https://platform.openai.com/) e faça login (ou crie conta).
2. Abra a área de chaves de API: [https://platform.openai.com/api-keys](https://platform.openai.com/api-keys).
3. Clique em **Create new secret key**, dê um nome opcional e copie o valor (**ele só é mostrado uma vez**).
4. Guarde a chave em variável de ambiente ou no arquivo `.env` (nunca commite o `.env`):

   ```env
   OPENAI_API_KEY=sk-...
   ```

5. O Spring AI lê a chave pela propriedade `spring.ai.openai.api-key` (mapeada no `application.yml` para `${OPENAI_API_KEY}`). O `ChatGPTService` legado usa a mesma chave via `spring.ai.openai.api-key` / fallback em `ChatGPTService`.

Política de uso e faturamento: [https://platform.openai.com/docs/guides/rate-limits](https://platform.openai.com/docs/guides/rate-limits) e preços em [https://openai.com/pricing](https://openai.com/pricing).

### DeepSeek (opcional)

1. Crie conta e chave em [https://platform.deepseek.com/](https://platform.deepseek.com/) (ou documentação atual do provedor).
2. No `.env`:

   ```env
   DEEPSEEK_API_KEY=sk-...
   ```

---

## Instalar e usar o Ollama (modelo local)

1. **Download e instalação**: [https://ollama.com/download](https://ollama.com/download) (Windows, macOS ou Linux). O instalador configura o serviço em geral na porta **11434**.
2. Verifique se o Ollama está rodando (ícone no tray no Windows, ou `ollama serve` em ambientes onde o serviço não sobe sozinho).
3. **Baixe um modelo** compatível com o nome configurado no projeto (padrão `llama3.2`):

   ```bash
   ollama pull llama3.2
   ```

   Outro modelo comum é `mistral`. Se mudar, ajuste `OLLAMA_MODEL` ou `spring.ai.ollama.chat.options.model` no `application.yml`.
4. Teste no terminal:

   ```bash
   ollama run llama3.2 "Diga olá em uma frase."
   ```

5. Variáveis opcionais no `.env` (veja também `.env.example`):

   ```env
   OLLAMA_BASE_URL=http://localhost:11434
   OLLAMA_MODEL=llama3.2
   ```

---

## Configuração: `application.yml` e `.env`

Na raiz do módulo `server`, copie o exemplo e preencha:

```bash
cp .env.example .env
```

O `ServerApplication` carrega o `.env` **antes** do Spring subir (variáveis de ambiente e propriedades de sistema já definidas **não** são sobrescritas).

Trecho principal de IA no `application.yml`:

```yaml
spring:
  ai:
    openai:
      api-key: ${OPENAI_API_KEY:}
      chat:
        model: gpt-4o
    deepseek:
      base-url: https://api.deepseek.com
      api-key: ${DEEPSEEK_API_KEY}
      chat:
        enabled: true
        model: deepseek-chat
        temperature: 0.8
    ollama:
      base-url: ${OLLAMA_BASE_URL:http://localhost:11434}
      chat:
        options:
          model: ${OLLAMA_MODEL:llama3.2}
```

Dependências Maven relevantes (versões geridas pelo `spring-ai-bom`):

```xml
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
```

---

## Executar o projeto

Na pasta `server`:

```bash
./mvnw spring-boot:run
```

No Windows (PowerShell), se usar variáveis só na sessão atual:

```powershell
$env:OPENAI_API_KEY = "sk-..."
.\mvnw.cmd spring-boot:run
```

Porta padrão: **8080** (`SPRING_PORT` no `application.yml` altera).

---

## Endpoints de exemplo e segurança

O **Spring Security** exige JWT na maioria das rotas. As rotas abertas incluem (entre outras) `GET /ai/ollama/**`, cadastro de usuário, H2, produtos, categorias e Swagger.

| Método e caminho | Autenticação | Descrição |
|------------------|--------------|-----------|
| `GET /ai/ollama/ask?prompt=...` | Não | Resposta de texto do modelo Ollama local |
| `GET /music` | Sim (JWT) | Música gerada com OpenAI (`MusicService`) |
| `GET /music/parameters?genre=...&theme=...&ai=DEEPSEEK` | Sim (JWT) | Música com parâmetros; `ai=DEEPSEEK` usa DeepSeek |
| `POST /gpt` | Sim (JWT) | Corpo: texto da pergunta; resposta via `ChatGPTService` (HTTP OpenAI) |

Para rotas protegidas, autentique-se conforme o fluxo JWT do projeto (Swagger em `/swagger-ui.html` ou `/swagger-ui/` conforme versão do springdoc).

---

## Trechos de código documentados

### Carregar `.env` antes do Spring (`ServerApplication`)

```java
public static void main(String[] args) {
    loadDotEnv();
    SpringApplication.run(ServerApplication.class, args);
}

private static void loadDotEnv() {
    Dotenv dotenv = Dotenv.configure()
            .ignoreIfMissing()
            .load();
    for (DotenvEntry entry : dotenv.entries()) {
        String key = entry.getKey();
        String value = entry.getValue();
        if (key == null || key.isBlank() || value == null) {
            continue;
        }
        if (System.getenv(key) != null || System.getProperty(key) != null) {
            continue;
        }
        System.setProperty(key, value);
    }
}
```

### Spring AI: injetar o `ChatModel` da OpenAI pelo nome do bean

O auto-config do Spring AI registra um `ChatModel` com nome **`openAiChatModel`**. Com vários provedores no classpath, use sempre **`@Qualifier`** explícito.

```java
@Service
public class MusicService {
    private final ChatModel aiClient;

    @Autowired
    public MusicService(@Qualifier("openAiChatModel") ChatModel aiClient) {
        this.aiClient = aiClient;
    }

    public String getMusic() {
        return aiClient.call(BASE_MUSIC);
    }
    // ...
}
```

### Ollama local: controller de exemplo

Bean **`ollamaChatModel`**:

```java
@RestController
@RequestMapping("/ai/ollama")
public class OllamaDemoController {

    private final ChatModel ollamaChatModel;

    public OllamaDemoController(@Qualifier("ollamaChatModel") ChatModel ollamaChatModel) {
        this.ollamaChatModel = ollamaChatModel;
    }

    @GetMapping("/ask")
    public ResponseEntity<String> ask(
            @RequestParam(defaultValue = "Responda em uma frase: o que é Spring Boot?") String prompt) {
        return ResponseEntity.ok(ollamaChatModel.call(prompt));
    }
}
```

### OpenAI via HTTP (`ChatGPTController`)

```java
@RestController
@RequestMapping("/gpt")
public class ChatGPTController {

    private final ChatGPTService chatGPTService;

    public ChatGPTController(ChatGPTService chatGPTService) {
        this.chatGPTService = chatGPTService;
    }

    @PostMapping
    public String askQuestion(@RequestBody String message) {
        return chatGPTService.ask(message);
    }
}
```

---

## Referências rápidas

- OpenAI API keys: [https://platform.openai.com/api-keys](https://platform.openai.com/api-keys)
- Ollama: [https://ollama.com/](https://ollama.com/)
- Spring AI: [https://docs.spring.io/spring-ai/reference/](https://docs.spring.io/spring-ai/reference/)
