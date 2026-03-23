package br.edu.utfpr.pb.pw45s.server.ai;

import br.edu.utfpr.pb.pw45s.server.ai.deepseek.DeepSeekMusicService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("music")
public class MusicController {
    private final MusicService musicService;
    private final DeepSeekMusicService deepSeekMusicService;

    public MusicController(MusicService musicService, DeepSeekMusicService deepSeekMusicService) {
        this.musicService = musicService;
        this.deepSeekMusicService = deepSeekMusicService;
    }

    @GetMapping()
    public ResponseEntity<String> generateMusic(){
        return ResponseEntity.ok(musicService.getMusic());
    }

    // /music/parameters?theme={{theme}}&genre={{genre}}
    @GetMapping("/parameters")
    public ResponseEntity<MusicDTO> generateMusicWithParams(@RequestParam("genre") String genre, @RequestParam("theme") String theme, @RequestParam(value = "ai", required = false) String ai) {
        try {
            if (ai != null && ai.equalsIgnoreCase(AIProvider.DEEPSEEK.toString())) {
                return ResponseEntity.ok(deepSeekMusicService.getMusicByGenreAndTheme(genre, theme));
            } else {
                return ResponseEntity.ok(musicService.getMusicByGenreAndTheme(genre, theme));
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }


}