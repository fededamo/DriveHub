"""Controlli documentali 08; non esegue software o verificatori storici."""
from pathlib import Path
import hashlib
import json
import re
import tarfile
import textwrap
import xml.etree.ElementTree as ET
import zipfile
from urllib.parse import unquote

import pymupdf

ROOT = Path(__file__).resolve().parents[4]
DOC = ROOT / "docs/relazione"
OUT = Path(__file__).resolve().parent


def sha(path):
    return hashlib.sha256(path.read_bytes()).hexdigest()


baseline = json.loads((OUT / "baseline-conservazione.json").read_text())
allowed = {
    "docs/relazione/main.tex",
    "docs/relazione/capitoli/2_architettura.tex",
    "docs/relazione/appendici/a_casi_uso.tex",
    "docs/relazione/relazione-drivehub.pdf",
    "docs/relazione/README.md",
    "docs/relazione/piano.md",
}
changed = []
for name, expected in baseline["files"].items():
    path = ROOT / name
    assert path.is_file(), f"Eliminato: {name}"
    if sha(path) != expected:
        assert name in allowed, f"Modifica fuori perimetro: {name}"
        changed.append(name)

with tarfile.open(OUT / "baseline-editoriale.tar.gz") as archive:
    for entry in archive.getmembers():
        data = archive.extractfile(entry).read()
        assert hashlib.sha256(data).hexdigest() == baseline["files"][entry.name]

snippets = []
for ticket, chapter in [(5, "3_implementazione.tex"), (6, "4_testing.tex")]:
    text = (DOC / "capitoli" / chapter).read_text()
    blocks = re.findall(r"\\begin\{lstlisting\}\[(.*?)\](.*?)\\end\{lstlisting\}", text, re.S)
    for item in json.loads((DOC / f"verifiche/ticket-{ticket:02}/frammenti.json").read_text()):
        current = "".join((ROOT / item["source"]).read_text().splitlines(keepends=True)[item["firstline"] - 1:item["lastline"]])
        block = next(body for options, body in blocks if item["label"] in options)
        assert current == item["text"]
        assert textwrap.dedent(block).strip() == textwrap.dedent(current).strip()
        snippets.append(item["label"])

tex = "\n".join(p.read_text() for p in [DOC / "main.tex", *sorted((DOC / "capitoli").glob("*.tex")), *sorted((DOC / "appendici").glob("*.tex"))])
labels = re.findall(r"\\label\{([^}]+)\}", tex)
refs = re.findall(r"\\(?:ref|pageref)\{([^}]+)\}", tex)
# Le schede generano label UC tramite la macro, senza duplicarle a mano.
labels += ["uc:" + x for x in re.findall(r"\\scheda\{([^}]+)\}", tex)]
labels += re.findall(r"label=(lst:[\w:-]+)", tex)
labels += ["fig:" + x for x in re.findall(r"\}\{(ui-[^}]+)\}", tex)]
assert len(labels) == len(set(labels)), "Label duplicate"
assert set(refs) <= set(labels), set(refs) - set(labels)
assert not re.search(r"\\provvisorio|ancora da redigere|TODO|FIXME", tex)
figures = re.findall(r"\\includegraphics(?:\[[^\]]*\])?\{([^}]+)\}", tex)
for name in figures:
    if "#" in name:
        continue
    assert (DOC / name).is_file(), name
assert all((DOC / "screenshot" / (name + ".png")).is_file() for name in re.findall(r"\\schermata\{([^}]+)\}", tex))

drawio = []
for path in sorted((DOC / "diagrammi").glob("*.drawio")):
    with pymupdf.open(str(path) + ".pdf") as pdf:
        embedded = unquote(pdf.metadata["subject"]).replace(r"\(", "(").replace(r"\)", ")")
        assert ET.canonicalize(path.read_text(), strip_text=True) == ET.canonicalize(embedded, strip_text=True), path.name
    drawio.append(path.name)

pdfpath = DOC / "relazione-drivehub.pdf"
pdf = pymupdf.open(pdfpath)
assert not pdf.is_encrypted
links = 0
fonts = set()
for i, page in enumerate(pdf):
    assert abs(page.rect.width - 595.28) < 1 and abs(page.rect.height - 841.89) < 1
    assert "??" not in page.get_text(), i + 1
    for link in page.get_links():
        if link["kind"] == pymupdf.LINK_NAMED:
            dest = pdf.resolve_link("#" + link["nameddest"])
            assert dest and 0 <= dest[0] < len(pdf) and dest[0] == link["page"], (i + 1, link)
            links += 1
        elif link["kind"] == pymupdf.LINK_GOTO:
            assert 0 <= link["page"] < len(pdf)
            links += 1
    for font in page.get_fonts(full=True):
        fonts.add(font[0])
        assert pdf.extract_font(font[0])[3], font
assert links > 150
outline = pdf.get_toc()
assert [title for level, title, _ in outline if level == 1] == [
    "Introduzione", "Analisi dei requisiti e progettazione", "Implementazione",
    "Testing", "Uso dell'AI", "Schede complete dei casi d'uso", "Tavole delle classi", "Matrice delle verifiche",
]

package = OUT / "sorgenti-relazione.zip"
if package.exists():
    with zipfile.ZipFile(package) as archive:
        assert archive.testzip() is None
        for name in archive.namelist():
            assert not Path(name).is_absolute() and ".." not in Path(name).parts
            assert archive.read(name) == (DOC / name).read_bytes(), name
        required = ["main.tex", "LEGGIMI-consegna.md"]
        required += [str(p.relative_to(DOC)) for folder in ["capitoli", "appendici", "diagrammi", "screenshot"] for p in (DOC / folder).iterdir() if p.suffix in [".tex", ".drawio", ".pdf", ".png"]]
        assert set(required) == set(archive.namelist())

result = {
    "baseline_files": len(baseline["files"]), "changed": changed,
    "unchanged": len(baseline["files"]) - len(changed),
    "snippets_exact": len(snippets), "drawio_pdf_exact": len(drawio),
    "pages": len(pdf), "internal_links": links, "embedded_fonts": len(fonts),
    "pdf_sha256": sha(pdfpath), "outline": outline,
    "zip_sha256": sha(package) if package.exists() else None,
}
(OUT / "esito-documentale.json").write_text(json.dumps(result, ensure_ascii=False, indent=2) + "\n")
print(json.dumps({k: v for k, v in result.items() if k != "outline"}, ensure_ascii=False, indent=2))
