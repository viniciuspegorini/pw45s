import { IResponse } from "@/commons/types";
import { api } from "@/lib/axios";

// URL base para as requisições de produtos
const musicURL = "/music";
const ollamaURL = "/ai/ollama";

const getMusicWithParameters = async (genre: string, theme: string): Promise<IResponse> => {
  let response = {} as IResponse;
  try {
    const data = await api.get(`${musicURL}/parameters?genre=${genre}&theme=${theme}`);
    response = {
      status: 200,
      success: true,
      message: "Musica gerada com sucesso!",
      data: data.data,
    };
  } catch (err: any) {
    response = {
      status: err.response.status,
      success: false,
      message: "Falha ao salvar produto",
      data: err.response.data,
    };
  }
  return response;
};

const getMusic = async (): Promise<IResponse> => {
  let response = {} as IResponse;
  try {
    const data = await api.get(`${musicURL}`);
    response = {
      status: 200,
      success: true,
      message: "Musica gerada com sucesso!",
      data: data.data,
    };
  } catch (err: any) {
    response = {
      status: err.response.status,
      success: false,
      message: "Falha ao salvar produto",
      data: err.response.data,
    };
  }
  return response;
};

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
