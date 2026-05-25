"""Exemplo 5: Gráficos com matplotlib."""
from pyscript import display
import matplotlib.pyplot as plt
import numpy as np

# Gráfico de linha
fig1, ax1 = plt.subplots(figsize=(5, 3), dpi=100)
x = np.linspace(0, 2 * np.pi, 100)
ax1.plot(x, np.sin(x), label="sin(x)")
ax1.plot(x, np.cos(x), label="cos(x)")
ax1.set_title("Funções trigonométricas")
ax1.legend()
ax1.grid(True, alpha=0.3)
display(fig1, target="plot-line")
plt.close(fig1)

# Gráfico de barras — vendas de produtos gamers
fig2, ax2 = plt.subplots(figsize=(5.5, 3), dpi=100)
categorias = ["Teclado", "Mouse", "Monitor", "Fone", "GPU"]
valores = [128, 95, 72, 210, 156]
cores = ["#005db7", "#ffbe00", "#003366", "#1a75c9", "#7f7f7f"]
barras = ax2.bar(categorias, valores, color=cores)
ax2.set_title("Vendas por produto gamer")
ax2.set_ylabel("Unidades vendidas")
ax2.legend(
    barras,
    categorias,
    title="Produtos",
    loc="center left",
    bbox_to_anchor=(1.02, 0.5),
    borderaxespad=0,
)
ax2.grid(True, axis="y", alpha=0.3)
fig2.subplots_adjust(right=0.68)
display(fig2, target="plot-bar")
plt.close(fig2)
