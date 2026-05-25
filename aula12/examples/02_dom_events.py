"""Exemplo 2: DOM, @when e pyscript.web."""
from pyscript import when, web, display

msg = web.page["dom-message"]

@when("click", "#btn-greet")
def greet(_event):
    msg.textContent = "Evento click tratado em Python!"
    msg.classList.add("highlight")

@when("click", "#btn-reset-dom")
def reset(_event):
    msg.textContent = "Clique no botão para testar o decorador @when."
    msg.classList.remove("highlight")

display("Handlers registrados. Use os botões acima.")
