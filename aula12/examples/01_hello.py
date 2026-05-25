"""Exemplo 1: Hello World e informações do interpretador."""
from pyscript import display
import sys

display("Olá do PyScript! Python rodando no navegador.")
display(f"Versão: {sys.version.split()[0]}")
display(f"Implementação: {sys.implementation.name}")
