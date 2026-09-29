// O sufixo "?url" faz o Vite retornar o endereço do arquivo CSS (em vez de aplicá-lo na página).
// Os temas vêm do pacote primereact instalado, garantindo a mesma versão dos componentes.
import lightThemeUrl from "primereact/resources/themes/lara-light-blue/theme.css?url";
import darkThemeUrl from "primereact/resources/themes/lara-dark-blue/theme.css?url";

export type Theme = "light" | "dark";

const THEME_LINK_ID = "theme-link";
const THEME_STORAGE_KEY = "theme";

/**
 * Retorna o tema salvo no localStorage. Caso o usuário ainda não tenha escolhido um tema,
 * utiliza a preferência do sistema operacional.
 */
export const getSavedTheme = (): Theme => {
  const saved = localStorage.getItem(THEME_STORAGE_KEY);
  if (saved === "light" || saved === "dark") {
    return saved;
  }
  return window.matchMedia("(prefers-color-scheme: dark)").matches ? "dark" : "light";
};

/**
 * Aplica o tema na página, alterando o <link> do tema do PrimeReact, e salva a escolha no localStorage.
 */
export const applyTheme = (theme: Theme) => {
  let link = document.getElementById(THEME_LINK_ID) as HTMLLinkElement | null;
  if (!link) {
    link = document.createElement("link");
    link.id = THEME_LINK_ID;
    link.rel = "stylesheet";
    document.head.appendChild(link);
  }
  link.href = theme === "dark" ? darkThemeUrl : lightThemeUrl;
  localStorage.setItem(THEME_STORAGE_KEY, theme);
};
