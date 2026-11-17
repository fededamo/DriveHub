"""Verifica aggiornata del ticket 04. Nessun avvio Java, test applicativo o DB."""
from hashlib import sha256
from pathlib import Path
from urllib.parse import unquote
import html
import json
import re
import unicodedata
import xml.etree.ElementTree as ET
import pymupdf

HERE = Path(__file__).resolve().parent
DOC = HERE.parents[1]
REPO = DOC.parents[1]
VIS = DOC / "build/visual-ticket04"
VIS.mkdir(parents=True, exist_ok=True)


def normalizza(text):
    return " ".join(unicodedata.normalize("NFKC", text).split()).replace("’", "'")


def compatto(text):
    return re.sub(r"\s+", "", normalizza(html.unescape(text))).replace("\u00ad", "")


def testo_pagine(pdf, start=0, stop=None):
    result = []
    for page in list(pdf)[start:stop]:
        lines = page.get_text().splitlines()
        if lines and lines[-1].strip().isdigit():
            lines.pop()
        result.append("\n".join(lines))
    return normalizza("\n".join(result))


doc = pymupdf.open(DOC / "relazione-drivehub.pdf")
assert not doc.is_encrypted
assert (DOC / "relazione-drivehub.pdf").read_bytes() == (DOC / "build/main.pdf").read_bytes()
toc = doc.get_toc()


def intervallo(title, following):
    return next(p-1 for _, t, p in toc if t == title), next(p-1 for _, t, p in toc if t == following)


indice = (DOC / "indice.md").read_text()
chapters = re.findall(r"^## [1-5]\. (.+)$", indice, re.M)
appendices = re.findall(r"^- \*\*[ABC] — (.+?)\.\*\*", indice, re.M)
sections = re.findall(r"^- \*\*\d+\.\d+ (.+?)\.\*\*", indice, re.M)
assert len(chapters) == 5 and len(sections) == 25 and len(appendices) == 3
assert [normalizza(t) for level, t, _ in toc if level == 1] == list(map(normalizza, chapters+appendices))
assert [normalizza(t) for level, t, _ in toc if level == 2][:25] == list(map(normalizza, sections))
new = intervallo("Sequence Diagram", "Implementazione")
chapter2 = intervallo(chapters[1], chapters[2])
newtext = testo_pagine(doc, *new)
assert "Provvisorio" not in newtext and "??" not in newtext
subtitles = [t for level, t, p in toc if level == 3 and new[0] < p <= new[1]]
assert subtitles == ["Acquisto di un veicolo", "Prenotazione di un noleggio",
                     "Acquisizione di un veicolo del Customer", "Schema ER",
                     "Modello relazionale", "Utenti, vincoli e semplificazioni"], subtitles
for title in chapters[2:]+appendices[2:]:
    page = next(p for _, t, p in toc if t == title)
    assert "Provvisorio" in doc[page-1].get_text(), title
for title in chapters[:2]+appendices[:2]:
    page = next(p for _, t, p in toc if t == title)
    assert "Provvisorio" not in doc[page-1].get_text(), title
assert "per revisione" in normalizza(doc[0].get_text())

# I sorgenti/figure precedenti sono tutti protetti; confronta anche il testo PDF.
old1 = pymupdf.open(HERE.parent / "ticket-01/capitolo-1.pdf")
assert testo_pagine(doc, *intervallo(chapters[0], chapters[1])) == testo_pagine(old1)
old2 = pymupdf.open(HERE.parent / "ticket-02/ticket-02-per-revisione.pdf")
old3 = pymupdf.open(HERE.parent / "ticket-03/ticket-03-per-revisione.pdf")
current_old2 = (testo_pagine(doc, *intervallo(sections[3], sections[7])) + " "
                + testo_pagine(doc, *intervallo(appendices[0], appendices[1])))
old2text = testo_pagine(old2)
# L'avviso di stato cambia; non cambia il contenuto di 2.1–2.4/A.
notice_re = r"(?:Le sezioni 2\.1.*?restano esplicitamente provvisorie\.|Il capitolo 2 e le appendici A.B sono completi.*?già approvati\.)"
assert re.sub(notice_re, "", current_old2) == re.sub(notice_re, "", old2text)
current_old3 = (testo_pagine(doc, *intervallo("Package Diagram", "Sequence Diagram")) + " "
                + testo_pagine(doc, *intervallo(appendices[1], appendices[2])))
assert current_old3 == testo_pagine(old3)

links, fonts = 0, set()
for i, page in enumerate(doc):
    assert abs(page.rect.width-595.28) < 1 and abs(page.rect.height-841.89) < 1
    assert "??" not in page.get_text()
    for link in page.get_links():
        if link["kind"] in (pymupdf.LINK_GOTO, pymupdf.LINK_NAMED):
            assert 0 <= link["page"] < len(doc)
            if link["kind"] == pymupdf.LINK_NAMED:
                assert doc.resolve_link("#"+link["nameddest"])[0] == link["page"]
            links += 1
    fonts.update(f[0] for f in page.get_fonts())
    page.get_pixmap(matrix=pymupdf.Matrix(1.5,1.5)).save(VIS / f"pagina-{i+1:02}.png")
assert all(doc.extract_font(f)[3] for f in fonts)

names = json.loads((HERE / "tavole.json").read_text())
assert len(names) == 13 and len(set(names)) == 13
sizes = []
for name in names:
    file = DOC / "diagrammi" / (name+".drawio")
    tree = ET.parse(file)
    cells = tree.findall(".//mxCell")
    ids = {c.get("id") for c in cells}
    assert len(ids) == len(cells)
    for c in cells:
        if c.get("edge"):
            g = c.find("mxGeometry")
            assert g is not None
            assert (c.get("source") in ids and c.get("target") in ids) or len(g.findall("mxPoint")) == 2
    exported = pymupdf.open(str(file)+".pdf")
    assert len(exported) == 1
    embedded = unquote(exported.metadata["subject"]).replace(r"\(", "(").replace(r"\)", ")")
    assert ET.canonicalize(file.read_text(), strip_text=True) == ET.canonicalize(embedded, strip_text=True), name
    text = compatto(exported[0].get_text())
    for c in cells:
        label = compatto(re.sub("<[^>]+>", " ", c.get("value", "")))
        assert not label or label in text, (name, c.get("id"), label)
    spans = [s for b in exported[0].get_text("dict")["blocks"] if "lines" in b for line in b["lines"] for s in line["spans"]]
    assert spans and all((exported[0].rect+(-1,-1,1,1)).contains(pymupdf.Rect(s["bbox"])) for s in spans)
    exported[0].get_pixmap(matrix=pymupdf.Matrix(1.5,1.5)).save(VIS / (name+".png"))
    # L'inclusione effettiva è verificata dal log LaTeX e osservata nel PDF.
    assert "diagrammi/"+name+".drawio.pdf" in (DOC / "build/main.log").read_text()
    sizes.append((name, round(min(s["size"] for s in spans), 2)))

# Riscontro lessicale indipendente: non genera il modello progettuale dal DDL.
schema = (REPO / "src/main/resources/db/schema.sql").read_text()
tables = re.findall(r'CREATE TABLE "([^"]+)"', schema)
fks = re.findall(r'CONSTRAINT "([^"]+)" FOREIGN KEY', schema)
assert len(tables) == 13 and len(fks) == 15
data_matrix = (HERE / "matrice-dati.md").read_text()
assert all(name in data_matrix for name in tables+fks)
relational_labels = " ".join((DOC / "diagrammi" / (name+".drawio")).read_text() for name in names if "relazionale" in name)
for name, body in re.findall(r'CREATE TABLE "([^"]+)" \((.*?)\n\);', schema, re.S):
    assert name in relational_labels
    for column in re.findall(r'^\s+"([^"]+)"\s+', body, re.M):
        assert column in relational_labels, (name, column)
assert len(re.findall(r"CREATE TYPE \w+ AS ENUM", schema)) == 4

baseline = json.loads((HERE / "baseline-conservazione.json").read_text())
assert all((REPO / p).is_file() and sha256((REPO / p).read_bytes()).hexdigest() == h for p,h in baseline.items())
assert all(sha256((REPO / row["source"]).read_bytes()).hexdigest() == row["sha256"] for row in json.loads((HERE / "fonti.json").read_text()))
log = (DOC / "build/main.log").read_text()
assert not re.search(r"^!|Warning:|Overfull|Underfull|Missing character", log, re.M)
warnings = re.findall(r"^warning: (.+)$", (HERE / "compilazione.txt").read_text(), re.M)
expected = "Trying to include PDF file with version (1.7), which is newer than current output PDF setting (1.5)."
assert all(w == expected for w in warnings), warnings
assert doc.metadata["format"] == "PDF 1.7"
for name, area, title in [("ticket-04-per-revisione", new, "Sezioni 2.7–2.8"),
                          ("capitolo-2-per-revisione", chapter2, "Capitolo 2 completo")]:
    extract = pymupdf.open()
    extract.insert_pdf(doc, from_page=area[0], to_page=area[1]-1)
    extract.set_metadata({"title": "DriveHub — "+title+" per revisione degli autori"})
    extract.save(HERE / (name+".pdf"))

print(f"PDF aperto e renderizzato: {len(doc)} pagine A4, {links} collegamenti interni risolti, {len(fonts)} font incorporati.")
print(f"2.7–2.8 complete: pagine fisiche {new[0]+1}–{new[1]}; capitolo 2: {chapter2[0]+1}–{chapter2[1]}.")
print("Indice e sei sottosezioni in ordine; capitoli 1–2 e A–B completi per revisione, capitoli 3–5 e C provvisori.")
print("13 draw.io e PDF su singola pagina: XML incorporato identico, etichette integrali e dentro la pagina.")
print("Copertura statica: 13 tabelle, 15 FK, tutti gli attributi, quattro enum; matrici/fonti controllate.")
print(f"Conservazione: {len(baseline)} file protetti intatti; evidenze e manifest 01–03, software, SQL, storico, figure precedenti preservati.")
print("Confronti PDF: capitolo 1 identico; 2.1–2.4/A e 2.5–2.6/B identici agli estratti storici, salvo avviso di stato e numeri pagina.")
print(f"LaTeX: nessun riferimento irrisolto, box difettoso o carattere mancante; {len(warnings)} soli avvisi noti di versione PDF Tectonic.")
print("Font minimi nelle esportazioni (prima della riduzione nel documento):", sizes)
print("Osservazione visiva da registrare separatamente: gli assert non dimostrano la semantica o la leggibilità.")
print("SHA-256 PDF:", sha256((DOC / "relazione-drivehub.pdf").read_bytes()).hexdigest())
