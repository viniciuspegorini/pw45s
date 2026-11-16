package br.edu.utfpr.pb.pw45s.server;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

// O perfil "test" utiliza o banco H2 em memória, não sendo necessário o PostgreSQL para executar os testes
@SpringBootTest
@ActiveProfiles("test")
class ServerApplicationTests {

	@Test
	void contextLoads() {
	}

}
