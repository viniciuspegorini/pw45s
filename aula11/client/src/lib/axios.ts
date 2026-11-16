import axios from "axios";

export const api = axios.create({
  // URL da API: definida no arquivo .env.development (npm run dev) ou .env.production (npm run build)
  baseURL: import.meta.env.VITE_SERVER_URL,
});
