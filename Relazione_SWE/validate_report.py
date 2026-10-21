"""Controlli statici della relazione quando non e' disponibile una distribuzione TeX."""

from collections import Counter
from pathlib import Path
import re
import sys


ROOT = Path(__file__).resolve().parent
SEARCH_IMAGES = [ROOT / "images"]
errors: list[str] = []
seen: dict[Path, str] = {}


def read_tree(path: Path, active: set[Path]) -> str:
    path = path.resolve()
    if path in active:
        errors.append(f"Input ricorsivo: {path}")
        return ""
    if not path.is_file():
        errors.append(f"Input mancante: {path}")
        return ""
    content = path.read_text(encoding="utf-8")
    seen[path] = content
    children = []
    for name in re.findall(r"\\input\{([^}]+)\}", content):
        child = ROOT / (name if name.endswith(".tex") else name + ".tex")
        children.append(read_tree(child, active | {path}))
    return content + "\n" + "\n".join(children)


combined = read_tree(ROOT / "main.tex", set())
labels = [label for label in re.findall(r"\\label\{([^}]+)\}", combined) if "#" not in label]
labels += re.findall(r"\\dhfigure[^\n]*\{(fig:[^}]+)\}", combined)
screens = re.findall(r"\\uiscreen\{([^}]+)\}\{[^}]+\}\{([^}]+)\}", combined)
screens += re.findall(r"\\uiauthscreen\{([^}]+)\}\{[^}]+\}\{([^}]+)\}\{[^}]+\}", combined)
labels += [label for _, label in screens]
labels += re.findall(r"\\begin\{lstlisting\}\[[^\n]*label=\{([^}]+)\}", combined)
refs = re.findall(r"\\(?:ref|pageref)\{([^}]+)\}", combined)
for label, count in Counter(labels).items():
    if count > 1:
        errors.append(f"Label duplicata: {label} ({count})")
for ref in sorted(set(refs) - set(labels)):
    errors.append(f"Riferimento senza label: {ref}")

figures = re.findall(r"\\dhfigure\{[^}]+\}\{([^}]+)\}", combined)
figures += ["mockups/" + name for name, _ in screens]
figures += [
    name for name in re.findall(r"\\includegraphics(?:\[[^]]*\])?\{([^}]+)\}", combined)
    if "#" not in name
]
for name in figures:
    if not any((folder / name).is_file() for folder in SEARCH_IMAGES):
        errors.append(f"Immagine mancante: {name}")

stack: list[str] = []
for match in re.finditer(r"\\(begin|end)\{([^}]+)\}", combined):
    kind, name = match.groups()
    if kind == "begin":
        stack.append(name)
    elif not stack or stack.pop() != name:
        errors.append(f"Ambiente chiuso fuori ordine: {name}")
if stack:
    errors.append("Ambienti non chiusi: " + ", ".join(stack))

print(
    f"File TeX: {len(seen)}; figure referenziate: {len(figures)}; "
    f"label: {len(labels)}; riferimenti: {len(refs)}"
)
for error in errors:
    print("ERRORE:", error)
sys.exit(1 if errors else 0)
