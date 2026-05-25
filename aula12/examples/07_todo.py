"""Exemplo 7: Lista de tarefas com pyscript.web."""
from pyscript import when, web

task_input = web.page["task-input"]
task_list = web.page["task-list"]


def add_task(text):
    if not text.strip():
        return
    li = web.li(
        web.input_(type="checkbox", classes=["task-check"]),
        web.span(text.strip()),
        classes=["task-item"],
    )
    task_list.append(li)


@when("click", "#btn-add-task")
def on_add(_event):
    add_task(task_input.value)
    task_input.value = ""


@when("keydown", "#task-input")
def on_key(event):
    if event.key == "Enter":
        add_task(task_input.value)
        task_input.value = ""


@when("change", "#task-list")
def on_toggle(event):
    """Delegação: checkboxes são criados dinamicamente após o @when."""
    target = event.target
    if not target.classList.contains("task-check"):
        return
    li = target.closest("li")
    if target.checked:
        li.classList.add("done")
    else:
        li.classList.remove("done")
