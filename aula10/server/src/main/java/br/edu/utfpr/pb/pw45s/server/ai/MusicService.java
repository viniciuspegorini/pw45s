package br.edu.utfpr.pb.pw45s.server.ai;

import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.ai.ollama.api.OllamaChatOptions;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Objects;

/**
 * Gera músicas utilizando o Spring AI. O mesmo código funciona com qualquer provedor, pois todos implementam
 * a interface ChatModel; muda apenas o bean injetado (openAiChatModel, deepSeekChatModel ou ollamaChatModel).
 */
@Service
public class MusicService {
    public static final String BASE_MUSIC = """
        Escreva uma Música do gênero Rock sobre a Vida de um programador java,
        a música deve ter 2 versos e um refrão
        """;

    private final Map<AIProvider, ChatModel> chatModels;
    private final String ollamaModel;

    public MusicService(@Qualifier("openAiChatModel") ChatModel openAiChatModel,
                        @Qualifier("deepSeekChatModel") ChatModel deepSeekChatModel,
                        @Qualifier("ollamaChatModel") ChatModel ollamaChatModel,
                        @Value("${spring.ai.ollama.chat.options.model}") String ollamaModel) {
        this.ollamaModel = ollamaModel;
        this.chatModels = Map.of(
                AIProvider.OPENAI, openAiChatModel,
                AIProvider.DEEPSEEK, deepSeekChatModel,
                AIProvider.OLLAMA, ollamaChatModel);
    }

    public String getMusic(AIProvider provider) {
        return chatModels.get(provider).call(BASE_MUSIC);
    }

    public MusicDTO getMusicByGenreAndTheme(String genre, String theme, AIProvider provider) {
        // Converte a resposta do modelo (JSON) em um objeto MusicDTO.
        // O getFormat() gera as instruções e o JSON Schema que são adicionados ao prompt
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

        // Modelos locais menores podem gerar um JSON incompleto. No Ollama é possível forçar a saída no formato
        // do JSON Schema gerado pelo BeanOutputConverter (opção "format"). As opções informadas na requisição
        // substituem as do application.yml, por isso o modelo também precisa ser informado
        Prompt prompt = provider == AIProvider.OLLAMA
                ? promptTemplate.create(OllamaChatOptions.builder()
                        .model(ollamaModel)
                        .format(outputConverter.getJsonSchemaMap())
                        .build())
                : promptTemplate.create();

        ChatResponse response = chatModels.get(provider).call(prompt);
        return outputConverter.convert(Objects.requireNonNull(
                Objects.requireNonNull(response.getResult()).getOutput().getText()));
    }
}
