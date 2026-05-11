package br.edu.utfpr.pb.pw45s.server.ai;

import cn.hutool.cron.task.RunnableTask;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/ai/ollama")
@Slf4j
public class OllamaDemoController {

	private final ChatModel ollamaChatModel;

	public OllamaDemoController(@Qualifier("ollamaChatModel") ChatModel ollamaChatModel) {
		this.ollamaChatModel = ollamaChatModel;
	}

	@GetMapping("/ask")
	public ResponseEntity<String> ask(
			@RequestParam(defaultValue = "Responda em uma frase: o que é Spring Boot?") String prompt) {
        String resposta = null;
        try {
            resposta = ollamaChatModel.call(prompt);
        } catch (Exception e) {
            log.error("Erro ao receber o resposta: {}", e.getMessage());
			throw new RuntimeException("Falha ao conectar-se com o Ollama.");
        }
        return ResponseEntity.ok(resposta);
    }
}
