"""Controlli documentali del ticket 09; richiede PyMuPDF, non avvia il software."""
from collections import Counter
from pathlib import Path
import difflib
import hashlib
import json
import re
import subprocess
import tarfile

import pymupdf

evidence = Path(__file__).resolve().parent
root = evidence.parents[3]
report = root / "docs/relazione"
baseline = json.loads((evidence / "baseline.json").read_text())
allowed = {"docs/relazione/" + name for name in (
    "capitoli/1_introduzione.tex", "relazione-drivehub.pdf", "README.md",
    "piano.md", "indice.md", "allineamento.md",
    "tracker/issues/09-riallineamento-introduzione.md",
)}


def digest(path):
    return hashlib.sha256(path.read_bytes()).hexdigest()


changed = [p for p, value in baseline["files"].items()
           if not (root / p).is_file() or digest(root / p) != value["sha256"]]
assert set(changed) <= allowed, changed
assert subprocess.check_output(["git", "rev-parse", "HEAD"], cwd=root, text=True).strip() == baseline["head"]
current = set(subprocess.check_output(
    ["git", "ls-files", "--cached", "--others", "--exclude-standard", "-z"],
    cwd=root).decode().split("\0")) - {""}
current.update(str(p.relative_to(root)) for p in evidence.rglob("*") if p.is_file())
added = sorted(current - baseline["files"].keys())
assert all(p.startswith("docs/relazione/verifiche/ticket-09/") for p in added), added
delta = []
chapter = "docs/relazione/capitoli/1_introduzione.tex"
with tarfile.open(evidence / "baseline-editoriale.tar.gz") as archive:
    before = archive.extractfile(chapter).read().decode()
    after = (root / chapter).read_text()
    assert Counter(before.splitlines()) == Counter(after.splitlines())
    assert before != after
    assert after.index("\\textbf{Ambiente di esecuzione.}") < after.index("\\textbf{Processi aziendali ridotti.}")
    for path in sorted(changed):
        if path.endswith((".md", ".tex")):
            old = archive.extractfile(path).read().decode().splitlines(keepends=True)
            delta.extend(difflib.unified_diff(old, (root / path).read_text().splitlines(keepends=True),
                                            fromfile="baseline/" + path, tofile="finale/" + path))
    old_pdf = pymupdf.open(stream=archive.extractfile("docs/relazione/relazione-drivehub.pdf").read(), filetype="pdf")
pdf = pymupdf.open(report / "relazione-drivehub.pdf")
assert len(pdf) == len(old_pdf) == 113
pages = []
for i, page in enumerate(pdf):
    a, b = page.get_pixmap(), old_pdf[i].get_pixmap()
    row = {"pagina_fisica": i + 1, "etichetta": page.get_label(),
           "testo_identico": page.get_text() == old_pdf[i].get_text(),
           "render_identico": (a.width, a.height, a.samples) == (b.width, b.height, b.samples)}
    pages.append(row)
    if i != 5:
        assert row["testo_identico"] and row["render_identico"], row
    for link in page.get_links():
        if link["kind"] == pymupdf.LINK_GOTO:
            assert 0 <= link["page"] < len(pdf), (i, link)
assert not pages[5]["testo_identico"] and not pages[5]["render_identico"]
assert pdf.get_toc() == old_pdf.get_toc()
assert pdf.get_toc()[:4] == [[1, "Introduzione", 4], [2, "Descrizione", 4],
    [2, "Stack tecnologico", 5], [2, "Semplificazioni adottate", 6]]
extract = pymupdf.open(evidence / "capitolo-1-per-revisione.pdf")
assert len(extract) == 3
assert all(extract[i].get_text() == pdf[i + 3].get_text() for i in range(3))
aux = (evidence / "riferimenti.aux.txt").read_text()
labels = set(re.findall(r"\\newlabel\{([^}]+)\}", aux))
refs = set()
for path in list((report / "capitoli").glob("*.tex")) + list((report / "appendici").glob("*.tex")) + [report / "main.tex"]:
    source = path.read_text()
    refs.update(re.findall(r"\\(?:ref|pageref|autoref|eqref)\{([^}]+)\}", source))
    refs.update(re.findall(r"\\tavolaautori\{[^}]+\}\{[^}]+\}\{([^}]+)\}", source))
refs.discard("#3")  # Parametro della macro tavolaautori, espanso nei suoi tre invocanti.
assert refs <= labels, refs - labels
assert {"ch:introduzione", "sec:semplificazioni"} <= labels
log = (evidence / "latex-log.txt").read_text()
assert not re.search(r"undefined references|undefined on input|Label\(s\) may have changed|Overfull|Underfull|^!", log, re.M)
warnings = re.findall(r"ignored error: Infinite glue shrinkage found in box being split \[(\d+)\]", log)
assert warnings == ["104", "105", "106", "107", "108"], warnings
assert (report / "build/ticket09/main.pdf").read_bytes() == (report / "relazione-drivehub.pdf").read_bytes()
(evidence / "delta-documentale.diff").write_text("".join(delta))
(evidence / "delta-files.json").write_text(json.dumps({"modificati": sorted(changed), "aggiunti": added}, indent=2) + "\n")
(evidence / "confronto-pdf.json").write_text(json.dumps(pages, indent=2) + "\n")
result = {"file_baseline": len(baseline["files"]), "file_precedenti_conservati": len(baseline["files"]) - len(changed),
          "modificati": sorted(changed), "riferimenti_risolti": len(refs), "pagine_pdf": len(pdf),
          "pagine_identiche_alla_baseline": 112, "unica_pagina_modificata": 6,
          "messaggi_tex_residui": warnings, "test_applicativi_eseguiti": False}
(evidence / "esito-documentale.json").write_text(json.dumps(result, indent=2) + "\n")
print(json.dumps(result, ensure_ascii=False, indent=2))
