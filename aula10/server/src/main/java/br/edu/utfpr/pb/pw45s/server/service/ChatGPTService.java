package br.edu.utfpr.pb.pw45s.server.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

/**
 * Exemplo de chamada à API de chat da OpenAI SEM o Spring AI: a requisição HTTP é montada manualmente
 * com o RestClient, para comparar com a abordagem do Spring AI (MusicService).
 */
@Service
public class ChatGPTService {
    private static final String API_URL = "https://api.openai.com/v1/chat/completions";

    private final RestClient restClient;
    private final String model;

    public ChatGPTService(@Value("${spring.ai.openai.api-key}") String apiKey,
                          @Value("${spring.ai.openai.chat.options.model}") String model) {
        this.model = model;
        this.restClient = RestClient.builder()
                .baseUrl(API_URL)
                .defaultHeaders(headers -> headers.setBearerAuth(apiKey))
                .build();
    }

    @SuppressWarnings("unchecked")
    public String ask(String userMessage) {
        Map<String, Object> request = Map.of(
                "model", model,
                "messages", List.of(Map.of("role", "user", "content", userMessage)));

        Map<String, Object> response = restClient.post()
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .body(Map.class);

        // Estrutura da resposta: { "choices": [ { "message": { "role": "assistant", "content": "..." } } ] }
        List<Map<String, Object>> choices = (List<Map<String, Object>>) response.get("choices");
        Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
        return (String) message.get("content");
    }
}
