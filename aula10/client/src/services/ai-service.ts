import type { AIProvider, IResponse } from "@/commons/types";
import { api } from "@/lib/axios";

// URLs base para as requisições de IA
const musicURL = "/music";
const ollamaURL = "/ai/ollama";

// Gera uma música com o gênero e o tema informados, utilizando o provedor de IA escolhido (OpenAI, DeepSeek ou Ollama)
const getMusicWithParameters = async (
  genre: string,
  theme: string,
  ai: AIProvider = "OPENAI"
): Promise<IResponse> => {
  let response = {} as IResponse;
  try {
    const data = await api.get(`${musicURL}/parameters`, { params: { genre, theme, ai } });
    response = {
      status: 200,
      success: true,
      message: "Música gerada com sucesso!",
      data: data.data,
    };
  } catch (err: any) {
    response = {
      status: err.response?.status,
      success: false,
      message: "Falha ao gerar a música",
      data: err.response?.data,
    };
  }
  return response;
};

// Gera uma música com o prompt fixo definido na API
const getMusic = async (): Promise<IResponse> => {
  let response = {} as IResponse;
  try {
    const data = await api.get(`${musicURL}`);
    response = {
      status: 200,
      success: true,
      message: "Música gerada com sucesso!",
      data: data.data,
    };
  } catch (err: any) {
    response = {
      status: err.response?.status,
      success: false,
      message: "Falha ao gerar a música",
      data: err.response?.data,
    };
  }
  return response;
};

// Envia um prompt para o modelo local executado pelo Ollama
const askOllama = async (prompt: string): Promise<IResponse> => {
  let response = {} as IResponse;
  try {
    const data = await api.get(`${ollamaURL}/ask`, { params: { prompt } });
    response = {
      status: 200,
      success: true,
      message: "Resposta recebida com sucesso!",
      data: data.data as object,
    };
  } catch (err: any) {
    response = {
      status: err.response?.status,
      success: false,
      message: "Falha ao consultar o Ollama",
      data: err.response?.data,
    };
  }
  return response;
};

// Objeto que exporta todas as funções
const AIService = {
  getMusicWithParameters,
  getMusic,
  askOllama,
};

export default AIService;
