package br.edu.utfpr.pb.pw45s.server.ai;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

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
		try {
			return ResponseEntity.ok(ollamaChatModel.call(prompt));
		} catch (Exception e) {
			log.error("Erro ao receber a resposta do Ollama: {}", e.getMessage());
			// 503: o Ollama não está em execução ou o modelo configurado não foi baixado
			throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Falha ao conectar-se com o Ollama.");
		}
	}
}
