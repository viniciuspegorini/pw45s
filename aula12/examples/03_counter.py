"""Exemplo 3: Contador interativo."""
from pyscript import when, web

count = 0
label = web.page["counter-value"]

def render():
    label.textContent = str(count)

@when("click", "#btn-inc")
def inc(_event):
    global count
    count += 1
    render()

@when("click", "#btn-dec")
def dec(_event):
    global count
    count -= 1
    render()

@when("click", "#btn-reset-counter")
def reset(_event):
    global count
    count = 0
    render()

render()
