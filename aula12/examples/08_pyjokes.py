"""Exemplo 8: Piadas de programador com pyjokes (PyPI via micropip)."""
from pyscript import when, web
import pyjokes

joke_el = web.page["joke-text"]
meta_el = web.page["joke-meta"]
category_select = web.page["joke-category"]

# API pyjokes: apenas neutral, chuck e all (ver pyjokes.CATEGORIES)
CATEGORIAS = {
    "neutral": "Neutras (programador)",
    "chuck": "Chuck Norris",
    "all": "Todas",
}


def show_joke():
    cat = category_select.value
    joke = pyjokes.get_joke(language="en", category=cat)
    joke_el.textContent = joke
    meta_el.textContent = (
        f"Categoria: {CATEGORIAS.get(cat, cat)} · idioma: en · pacote pyjokes"
    )


show_joke()


@when("click", "#btn-joke")
def nova_piada(_event):
    show_joke()


@when("change", "#joke-category")
def mudar_categoria(_event):
    show_joke()
