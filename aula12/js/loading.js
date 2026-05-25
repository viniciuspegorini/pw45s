/**
 * Indicadores de carregamento do Pyodide / PyScript.
 * Deve rodar após o DOM existir (script no final do body).
 */
function initPyodideLoader() {
  const loader = document.getElementById("pyodide-loader");
  const statusEl = document.getElementById("pyodide-status");
  const progressEl = document.getElementById("pyodide-progress");
  const dismissBtn = document.getElementById("pyodide-dismiss");

  const outputs = document.querySelectorAll(".py-output[data-loading]");
  const demos = document.querySelectorAll("[data-py-demo]");
  const scripts = document.querySelectorAll('script[type="py"][src]');
  const totalScripts = scripts.length;

  let progressValue = 5;
  let doneCount = 0;
  let hidden = false;

  function setStatus(message) {
    if (!statusEl || !message) return;
    statusEl.textContent = translateMessage(message);
  }

  function translateMessage(msg) {
    const map = {
      "Loading interpreter": "Carregando interpretador Python…",
      "Loaded interpreter": "Interpretador pronto.",
      "Loading Pyodide": "Baixando e iniciando Pyodide…",
      "Loaded Pyodide": "Pyodide carregado.",
      "Loading Packages Graph": "Verificando pacotes disponíveis…",
      "Loaded Packages Graph": "Grafo de pacotes carregado.",
      "Loading packages": "Instalando pacotes…",
      "Loading remote packages": "Carregando pacotes remotos…",
      "Loaded remote packages": "Pacotes remotos prontos.",
      "Loading Storage": "Preparando armazenamento…",
      "Loading files": "Carregando arquivos…",
      "Loading JS modules": "Carregando módulos JavaScript…",
      "Loading fetch": "Buscando recursos…",
    };
    return map[msg] || msg;
  }

  function bumpProgress(message) {
    if (!progressEl) return;
    if (/Loaded|done/i.test(message)) {
      progressValue = Math.min(progressValue + 15, 95);
    } else {
      progressValue = Math.min(progressValue + 5, 90);
    }
    progressEl.value = progressValue;
  }

  function hideLoader(reason) {
    if (hidden) return;
    hidden = true;
    if (progressEl) progressEl.value = 100;
    if (reason) setStatus(reason);
    else setStatus("Pronto! Navegue pelos exemplos abaixo.");
    if (loader) {
      loader.classList.add("is-hidden");
      loader.setAttribute("aria-busy", "false");
    }
    document.body.classList.add("pyscript-ready");
    markOutputsBusy(false);
  }

  function markOutputsBusy(busy) {
    outputs.forEach((el) => {
      if (busy) el.setAttribute("data-loading", "true");
      else el.removeAttribute("data-loading");
    });
    demos.forEach((el) => el.classList.toggle("is-loading", busy));
  }

  function checkAllDone() {
    if (doneCount >= totalScripts) {
      hideLoader();
    } else if (statusEl) {
      setStatus(`Exemplos carregados: ${doneCount} de ${totalScripts}…`);
    }
  }

  markOutputsBusy(true);

  document.addEventListener("py:progress", (event) => {
    const message =
      typeof event.detail === "string"
        ? event.detail
        : event.detail?.message || String(event.detail ?? "");
    setStatus(message);
    bumpProgress(message);
  });

  document.addEventListener("py:ready", () => {
    setStatus("Executando exemplos Python…");
    bumpProgress("Loaded Pyodide");
  });

  document.addEventListener("py:done", (event) => {
    doneCount += 1;
    const script = event.detail?.script;
    const targetId = script?.getAttribute?.("target");
    if (targetId) {
      const out = document.getElementById(targetId);
      if (out) out.removeAttribute("data-loading");
    }
    checkAllDone();
  });

  document.addEventListener("py:all-done", () => hideLoader());

  if (dismissBtn) {
    dismissBtn.addEventListener("click", () => {
      hideLoader("Overlay fechado manualmente. Veja o console (F12) se algo falhou.");
    });
  }

  // Fallback: não bloquear a página para sempre
  setTimeout(() => {
    if (!hidden) {
      hideLoader(
        doneCount > 0
          ? `Carregamento parcial (${doneCount}/${totalScripts}). Confira o console (F12).`
          : "Tempo esgotado. Verifique rede, servidor HTTP e o console (F12)."
      );
    }
  }, 90000);
}

if (document.readyState === "loading") {
  document.addEventListener("DOMContentLoaded", initPyodideLoader);
} else {
  initPyodideLoader();
}
