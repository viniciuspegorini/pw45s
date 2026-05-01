import { StrictMode } from "react";
import { createRoot } from "react-dom/client";
import "./index.css";

import App from "@/App.tsx";

import { PrimeReactProvider } from "primereact/api";
import { BrowserRouter } from "react-router-dom";

import "primereact/resources/themes/lara-light-indigo/theme.css"; //theme
import "primereact/resources/primereact.min.css"; //core css
import "primeicons/primeicons.css"; //icons
import "primeflex/primeflex.css"; //flex utilities

import { AuthProvider } from "@/context/AuthContext";
import { GoogleOAuthProvider } from "@react-oauth/google";

const themeId = "theme-link";
const themeHref =
  "https://unpkg.com/primereact/resources/themes/lara-light-blue/theme.css";

const link = document.createElement("link");
link.id = themeId;
link.rel = "stylesheet";
link.href = themeHref;
document.head.appendChild(link);

createRoot(document.getElementById("root")!).render(
  <StrictMode>
    <BrowserRouter>
      <PrimeReactProvider>
        <GoogleOAuthProvider clientId="310109923674-la5thl4s4t0b2ajp6acdhq7tra74dn31.apps.googleusercontent.com">
          <AuthProvider>
            <App />
          </AuthProvider>
        </GoogleOAuthProvider>
      </PrimeReactProvider>
    </BrowserRouter>
  </StrictMode>
);
