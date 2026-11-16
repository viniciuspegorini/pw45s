# Aplicação cliente (React) - Integração com IA

A aplicação cliente desta aula é baseada na [aplicação cliente da aula07](../../aula07/client/README.md) (cadastros de categorias e produtos, autenticação e upload da imagem do produto). Foram adicionadas as páginas que consomem os *endpoints* de IA da API, descritos no [README.md do server](../server/README.md).

## O que foi adicionado

| Arquivo | Descrição |
|---------|-----------|
| `src/services/ai-service.ts` | Funções que chamam os *endpoints* de IA da API (`/music` e `/ai/ollama/ask`) |
| `src/pages/music-form/index.tsx` | Página **Música**: formulário com gênero, tema e provedor de IA. Exibe a música gerada em um `Card` |
| `src/pages/ollama-form/index.tsx` | Página **Ollama**: envia um prompt livre ao modelo local e exibe a resposta |
| `src/commons/types.ts` | Tipos `AIProvider`, `IMusicRequest`, `IMusicResponse` e `IOllamaRequest` |
| `src/components/top-menu/index.tsx` | Menu **IA**, com os itens **Música** e **Ollama** |
| `src/routes/app-routes/index.tsx` | Rotas protegidas `/music` e `/ollama` |

## Página Música

O usuário informa o gênero e o tema da música e escolhe o provedor de IA:

| Opção | Valor enviado (`ai`) | Observação |
|-------|----------------------|------------|
| OpenAI | `OPENAI` | Requer chave de API com saldo |
| DeepSeek | `DEEPSEEK` | Requer chave de API com saldo |
| Ollama (local) | `OLLAMA` | Gratuito, executado na máquina (requer o Ollama instalado) |

A requisição é enviada com os parâmetros na *query string* (o axios codifica os caracteres especiais, como acentos e espaços):

```ts
const getMusicWithParameters = async (genre: string, theme: string, ai: AIProvider = "OPENAI") => {
  const data = await api.get(`/music/parameters`, { params: { genre, theme, ai } });
  // ...
};
```

A API retorna a música no formato do tipo `IMusicResponse`:

```ts
export interface IMusicResponse {
  title: string;
  music: string;
  genre: string;
  theme: string;
}
```

Se o provedor falhar (chave inválida, conta sem saldo ou serviço indisponível), a API retorna HTTP 502 e a página exibe uma mensagem de erro.

## Página Ollama

Envia o texto digitado para `GET /ai/ollama/ask?prompt=...` e exibe a resposta do modelo local. A primeira chamada pode demorar, pois o modelo precisa ser carregado na memória.

## Executando a aplicação

Com a API em execução na porta 8080:

```bash
npm install
npm run dev
```
