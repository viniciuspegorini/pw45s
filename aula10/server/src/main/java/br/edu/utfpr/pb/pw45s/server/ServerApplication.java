package br.edu.utfpr.pb.pw45s.server;

import io.github.cdimascio.dotenv.Dotenv;
import io.github.cdimascio.dotenv.DotenvEntry;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ServerApplication {

	public static void main(String[] args) {
		loadDotEnv();
		SpringApplication.run(ServerApplication.class, args);
	}

	/** Lê o arquivo .env no diretório de trabalho e define system properties só onde ainda não há env/property. */
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
}
