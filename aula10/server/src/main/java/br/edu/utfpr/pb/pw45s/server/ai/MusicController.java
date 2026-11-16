package br.edu.utfpr.pb.pw45s.server.ai;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@Slf4j
@RestController
@RequestMapping("music")
public class MusicController {
    private final MusicService musicService;

    public MusicController(MusicService musicService) {
        this.musicService = musicService;
    }

    // /music?ai=OLLAMA (padrão: OPENAI)
    @GetMapping()
    public ResponseEntity<String> generateMusic(@RequestParam(value = "ai", defaultValue = "OPENAI") AIProvider ai) {
        try {
            return ResponseEntity.ok(musicService.getMusic(ai));
        } catch (Exception e) {
            throw providerError(ai, e);
        }
    }

    // /music/parameters?genre={{genre}}&theme={{theme}}&ai=DEEPSEEK (padrão: OPENAI)
    @GetMapping("/parameters")
    public ResponseEntity<MusicDTO> generateMusicWithParams(@RequestParam("genre") String genre,
                                                            @RequestParam("theme") String theme,
                                                            @RequestParam(value = "ai", defaultValue = "OPENAI") AIProvider ai) {
        try {
            return ResponseEntity.ok(musicService.getMusicByGenreAndTheme(genre, theme, ai));
        } catch (Exception e) {
            throw providerError(ai, e);
        }
    }

    // O erro detalhado do provedor (chave inválida, sem saldo, serviço fora do ar...) fica apenas no log da API
    private ResponseStatusException providerError(AIProvider ai, Exception e) {
        log.error("Erro ao gerar a música com a IA {}: {}", ai, e.getMessage());
        return new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                "Falha ao gerar a música com " + ai + ". Verifique a chave de API, o saldo da conta ou se o serviço está disponível.");
    }
}
