"""Exemplo 6: Jogo da velha com @when e pyscript.web."""
from pyscript import when, web

BOARD = [" "] * 9
CURRENT = "X"
GAME_OVER = False

status_el = web.page["ttt-status"]
cells = web.page.find(".ttt-cell")


def check_winner():
    lines = [
        (0, 1, 2), (3, 4, 5), (6, 7, 8),
        (0, 3, 6), (1, 4, 7), (2, 5, 8),
        (0, 4, 8), (2, 4, 6),
    ]
    for a, b, c in lines:
        if BOARD[a] != " " and BOARD[a] == BOARD[b] == BOARD[c]:
            return BOARD[a]
    if " " not in BOARD:
        return "draw"
    return None


def update_ui():
    for i, cell in enumerate(cells):
        mark = BOARD[i]
        cell.textContent = "" if mark == " " else mark
        cell.disabled = GAME_OVER or mark != " "
        cell.classes.clear()
        if mark == "X":
            cell.classes.add("x")
        elif mark == "O":
            cell.classes.add("o")

    winner = check_winner()
    status_el.classes.discard("win")
    status_el.classes.discard("draw")
    if winner == "X" or winner == "O":
        status_el.textContent = f"Jogador {winner} venceu!"
        status_el.classes.add("win")
    elif winner == "draw":
        status_el.textContent = "Empate!"
        status_el.classes.add("draw")
    else:
        status_el.textContent = f"Vez do jogador {CURRENT}"


@when("click", ".ttt-cell")
def play(event):
    global CURRENT, GAME_OVER
    if GAME_OVER:
        return

    idx = int(event.target.dataset.index)
    if BOARD[idx] != " ":
        return

    BOARD[idx] = CURRENT
    result = check_winner()
    if result:
        GAME_OVER = True
    else:
        CURRENT = "O" if CURRENT == "X" else "X"
    update_ui()


@when("click", "#btn-ttt-reset")
def reset(_event):
    global BOARD, CURRENT, GAME_OVER
    BOARD = [" "] * 9
    CURRENT = "X"
    GAME_OVER = False
    update_ui()


update_ui()
