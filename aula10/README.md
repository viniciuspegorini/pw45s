# Integração com Inteligência Artificial (Spring AI)

Integração da API REST com modelos de IA utilizando o **Spring AI**: **OpenAI** e **DeepSeek** (modelos pagos, acessados com chave de API) e **Ollama** (modelos executados localmente).

Os conteúdos das aplicações cliente e servidor estão nas respectivas pastas, descritos no arquivo README.md de cada uma:

- [server](server/README.md): *endpoints* de IA, configuração das chaves de API (`.env`), instalação do Ollama, saída estruturada e monitoramento das chamadas aos modelos no Grafana;
- [client](client/README.md): páginas **Música** e **Ollama**.

Em relação aos projetos da pasta **aula09** foram alteradas as aplicações **server** e **client**. O servidor mantém o armazenamento de arquivos com RustFS e a observabilidade com OpenTelemetry + Grafana LGTM, e o dashboard do Grafana ganhou uma seção com as métricas das chamadas de IA.
