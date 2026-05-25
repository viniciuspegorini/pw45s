"""Exemplo 4: NumPy no Pyodide."""
from pyscript import display
import numpy as np

display("NumPy carregado com sucesso!", target="out-numpy", append=False)
display(f"Array: {np.array([1, 2, 3, 4, 5])}", target="out-numpy")
display(f"Média: {np.array([1, 2, 3, 4, 5]).mean():.2f}", target="out-numpy")
display(
    f"Soma dos quadrados: {(np.array([1, 2, 3, 4, 5]) ** 2).sum()}",
    target="out-numpy",
)

x = np.linspace(0, 2 * np.pi, 5)
display(f"linspace: {np.round(x, 2)}", target="out-numpy")
