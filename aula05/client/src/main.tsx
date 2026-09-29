import { StrictMode } from "react";
import { createRoot } from "react-dom/client";
import "./index.css";

import App from "@/App.tsx";

import { PrimeReactProvider } from "primereact/api";
import { BrowserRouter } from "react-router-dom";

import "primereact/resources/primereact.min.css"; //core css
import "primeicons/primeicons.css"; //icons
import "primeflex/primeflex.css"; //flex utilities

import { AuthProvider } from "@/context/AuthContext";
import { applyTheme, getSavedTheme } from "@/commons/theme";

// O tema (claro ou escuro) é aplicado antes de renderizar a aplicação, em todas as páginas
applyTheme(getSavedTheme());

createRoot(document.getElementById("root")!).render(
  <StrictMode>
    <BrowserRouter>
      <PrimeReactProvider>
        <AuthProvider>
          <App />
        </AuthProvider>
      </PrimeReactProvider>
    </BrowserRouter>
  </StrictMode>
);
