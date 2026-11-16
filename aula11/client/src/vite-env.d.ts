/// <reference types="vite/client" />

// Variáveis de ambiente utilizadas pela aplicação (arquivos .env.development e .env.production)
interface ImportMetaEnv {
  readonly VITE_SERVER_URL: string;
  readonly VITE_STORAGE_URL: string;
  readonly VITE_GOOGLE_CLIENT_ID: string;
}

interface ImportMeta {
  readonly env: ImportMetaEnv;
}
