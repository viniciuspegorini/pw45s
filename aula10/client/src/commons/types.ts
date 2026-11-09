export interface IUserRegister {
  displayName: string;
  username: string;
  password: string;
}

export interface IResponse {
  status?: number;
  success?: boolean;
  message?: string;
  data?: object
}

export interface IUserLogin {
  username: string;
  password: string;
}

export interface Authorities {
  authority: string;
}

export interface AuthenticatedUser {
  displayName: string;
  username: string;
  authorities: Authorities[];
}

export interface AuthenticationResponse {
  token: string;
  user: AuthenticatedUser;
}

export interface ICategory {
  id?: number;
  name: string;
}

export interface IProduct {
  id?: number;
  name: string;
  description: string;
  price: number;
  category: ICategory;
  imageName?: string;
  contentType?: string;
  imageFileName?: string;
  imageFile?: string;
}

// Provedores de IA disponíveis na API para a geração de músicas (OLLAMA = modelo local)
export type AIProvider = "OPENAI" | "DEEPSEEK" | "OLLAMA";

export interface IMusicRequest {
  genre: string;
  theme: string;
  ai: AIProvider;
}

export interface IMusicResponse {
  title: string;
  music: string;
  genre: string;
  theme: string;
}

export interface IOllamaRequest {
  prompt: string;
}
