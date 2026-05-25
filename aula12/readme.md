# PW45S - aula12 - PyScript + Pyodide

Exemplos utilizando **Python no navegador** usando [PyScript](https://pyscript.net/) **2026.3.1** com o interpretador [Pyodide](https://pyodide.org/). Não é necessário servidor backend: todo o código Python será executado no cliente.

## O que é PyScript?

O **PyScript** é uma plataforma open source que permite executar Python diretamente em páginas HTML. Por baixo dos panos, o **Pyodide** compila o CPython para WebAssembly (WASM) e roda no sandbox do navegador.

Principais vantagens para ensino e protótipos:

- Zero instalação de Python no computador (apenas navegador + servidor HTTP estático).
- Integração com HTML, CSS e JavaScript.
- Suporte a pacotes científicos (NumPy, Matplotlib, etc.) via `micropip`.
- API moderna: `display()`, `@when`, módulo `pyscript.web`.

## Estrutura do projeto

```
aula12/
├── index.html          # Página principal com todos os exemplos
├── css/
│   └── styles.css      # Estilização da apresentação
├── examples/           # Scripts Python carregados pelo PyScript
│   ├── 01_hello.py
│   ├── 02_dom_events.py
│   ├── 03_counter.py
│   ├── 04_numpy.py
│   ├── 05_plot.py
│   ├── 06_tictactoe.py
│   ├── 07_todo.py
│   ├── 08_pyjokes.py
│   └── 09_pyeditor.py
├── pyscript.toml       # Configuração dos exemplos type="py"
├── pyeditor.toml       # Configuração do PyEditor (files, worker)
└── README.md
```

## Versões utilizadas

| Componente | Versão / origem |
|------------|-----------------|
| PyScript   | [2026.3.1](https://pyscript.net/releases/2026.3.1/) (CDN) |
| Pyodide    | Empacotado com o PyScript 2026.3.1 |
| CSS/JS core | `https://pyscript.net/releases/2026.3.1/core.css` e `core.js` |

A inclusão no HTML segue o padrão oficial:

```html
<link rel="stylesheet" href="https://pyscript.net/releases/2026.3.1/core.css" />
<script type="module" src="https://pyscript.net/releases/2026.3.1/core.js"></script>
```

Cada exemplo Python é referenciado com:

```html
<script type="py" src="./examples/01_hello.py" target="out-hello"></script>
```

## Como executar localmente

O navegador exige **servir os arquivos por HTTP** (não abra `index.html` com `file://` — caminhos relativos e CORS podem falhar).

> **PyEditor (exemplo 9):** o arquivo `js/mini-coi.js` deve ser o primeiro script no `<head>` para habilitar workers no `python -m http.server` sem headers COOP manuais. Na primeira execução do editor, recarregue a página se o service worker acabou de registrar.

### Opção 1 — Python

```bash
cd aula12
python -m http.server 8080
```

Acesse: http://localhost:8080

### Opção 2 — Node.js (npx)

```bash
cd aula12
npx --yes serve -p 8080
```

### Opção 3 — VS Code

Extensão **Live Server**: clique com o botão direito em `index.html` → *Open with Live Server*.

> **Primeira carga:** o Pyodide e pacotes como NumPy/Matplotlib são baixados da rede. Pode levar dezenas de segundos. Recarregamentos seguintes usam cache do navegador.

## Exemplos incluídos

| # | Arquivo | Conceito demonstrado |
|---|---------|----------------------|
| 1 | `01_hello.py` | `display()`, `sys.version` |
| 2 | `02_dom_events.py` | Decorador `@when("click", "#id")` |
| 3 | `03_counter.py` | Estado Python + `pyscript.web` |
| 4 | `04_numpy.py` | Pacote NumPy via `config` JSON |
| 5 | `05_plot.py` | Matplotlib: `display(fig)` em alvos HTML |
| 6 | `06_tictactoe.py` | Jogo completo, tabuleiro e vitória/empate |
| 7 | `07_todo.py` | Lista dinâmica, tecla Enter, checkboxes |
| 8 | `08_pyjokes.py` | Pacote PyPI `pyjokes`, `get_joke()`, categorias |
| 9 | `09_pyeditor.py` | PyEditor + `[files]`: ler `data/` no filesystem Pyodide |


### Requisitos

- Servir a página por **HTTP** (`python -m http.server`) — não funciona com `file://`
- Caminhos relativos à URL da página (ex.: `./data/arquivo.xml`)
- `mini-coi.js` no `<head>` para o worker do PyEditor

## Configuração de pacotes

O PyScript 2026 exige **uma única configuração** por interpretador na página. Vários `config` diferentes em `<script type="py">` geram erro e os exemplos ficam parados.

Este projeto usa `<py-config>` no `index.html`:

```html
<py-config>
packages = ["numpy", "matplotlib", "pyjokes"]
</py-config>
```

Alternativas equivalentes: `pyscript.toml`, JSON em `config="./pyscript.json"` (apenas um por página).

## Indicador de carregamento (Pyodide)

Enquanto o Pyodide e os pacotes são baixados, a página exibe:

- **Overlay global** (`#pyodide-loader`) com spinner, mensagem e barra de progresso
- **Estado por saída** (`data-loading`, `data-loading-text`) nas áreas `.py-output`
- Atualização via eventos do PyScript em `js/loading.js`:
  - `py:progress` — etapas como `Loading Pyodide`, `Loading packages`
  - `py:ready` — interpretador pronto para executar
  - `py:done` — um script terminou
  - `py:all-done` — todos os exemplos concluídos (overlay some)

Se ficar travado, abra o console do navegador (F12) e confira erros de rede ou de config.

Consulte quais pacotes funcionam em: https://packages.pyscript.net/

## APIs principais usadas

### `display()`

Mostra texto, objetos Python e figuras matplotlib em um elemento da página:

```python
from pyscript import display

display("Olá!")
display(fig, target="plot-line")  # gráfico em div específica
```

### `@when`

Registra handlers de eventos do navegador:

```python
from pyscript import when

@when("click", "#meu-botao")
def clicou(event):
    print(event.target.textContent)
```

**Importante:** elementos criados *depois* do `@when` não recebem o listener automaticamente. Para UI dinâmica, use delegação no container pai ou chame `when("click", elemento)(handler)` após criar o elemento.

### `pyscript.web`

Interface Pythonica para o DOM:

```python
from pyscript import web

el = web.page["meu-id"]
el.textContent = "Atualizado"
web.page.body.append(web.button("Novo"))
```

## Principais observações

- Nem todo pacote do PyPI está disponível no Pyodide.
- Primeira execução com pacotes é lenta (download + instalação no WASM).
- Threads reais são limitadas; operações pesadas podem travar a UI (use [Workers](https://docs.pyscript.net/2026.3.1/user-guide/workers/) em apps maiores).
- Dados sensíveis não devem ficar no código cliente.

## Referências

- [Documentação PyScript 2026.3.1](https://docs.pyscript.net/2026.3.1/)
- [PyScript releases (CDN)](https://pyscript.net/releases/2026.3.1/)
- [Pyodide](https://pyodide.org/)
- [Pacotes compatíveis](https://packages.pyscript.net/)
