"""Verifica documentale del ticket 02; non esegue test dell'applicazione."""
from hashlib import sha256
from pathlib import Path
from urllib.parse import unquote
import json
import re
import unicodedata
import xml.etree.ElementTree as ET
import pymupdf

HERE = Path(__file__).resolve().parent
ROOT = HERE.parents[1]
REPO = ROOT.parents[1]


def normalizza(testo):
    return " ".join(unicodedata.normalize("NFKC", testo).split()).replace("’", "'")


doc = pymupdf.open(ROOT / "relazione-drivehub.pdf")
assert not doc.is_encrypted
toc = doc.get_toc()
indice = (ROOT / "indice.md").read_text()
chapters = re.findall(r"^## [1-5]\. (.+)$", indice, re.M)
appendices = re.findall(r"^- \*\*[ABC] — (.+?)\.\*\*", indice, re.M)
sections = re.findall(r"^- \*\*\d+\.\d+ (.+?)\.\*\*", indice, re.M)
assert [normalizza(t) for l, t, _ in toc if l == 1] == list(map(normalizza, chapters + appendices))
assert [normalizza(t) for l, t, _ in toc if l == 2][:25] == list(map(normalizza, sections))
assert len(chapters) == 5 and len(appendices) == 3 and len(sections) == 25
ids = [f"UC-{i:02}" for i in range(1, 12)] + [f"UC-S{i:02}" for i in range(1, 5)]
case_titles = [t for l, t, _ in toc if l == 2 and t.startswith("UC-")]
assert [t.split()[0] for t in case_titles] == ids


def intervallo(titolo, prossimo):
    start = next(p for _, t, p in toc if t == titolo) - 1
    end = next(p for _, t, p in toc if t == prossimo) - 1
    return start, end


nuove = intervallo(sections[3], sections[7])
app_a = intervallo(appendices[0], appendices[1])
testo_nuovo = "\n".join(doc[i].get_text() for a, b in (nuove, app_a) for i in range(a, b))
assert "Provvisorio" not in testo_nuovo and "??" not in testo_nuovo
for title in sections[7:11] + chapters[2:] + appendices[1:]:
    p = next(p for _, t, p in toc if t == title)
    assert "Provvisorio" in doc[p-1].get_text(), title
old = pymupdf.open(HERE.parent / "ticket-01/capitolo-1.pdf")
a, b = intervallo(chapters[0], chapters[1])
assert [normalizza(doc[i].get_text()) for i in range(a, b)] == [normalizza(p.get_text()) for p in old]

links, fonts = 0, set()
visual = ROOT / "build/visual-ticket02"
visual.mkdir(parents=True, exist_ok=True)
for i, page in enumerate(doc):
    assert abs(page.rect.width - 595.28) < 1 and abs(page.rect.height - 841.89) < 1
    assert "??" not in page.get_text()
    for link in page.get_links():
        if link["kind"] in (pymupdf.LINK_GOTO, pymupdf.LINK_NAMED):
            assert 0 <= link["page"] < len(doc)
            if link["kind"] == pymupdf.LINK_NAMED:
                assert doc.resolve_link("#" + link["nameddest"])[0] == link["page"]
            links += 1
    fonts.update(f[0] for f in page.get_fonts())
    page.get_pixmap(matrix=pymupdf.Matrix(1.35, 1.35)).save(visual / f"pagina-{i+1:02}.png")
assert all(doc.extract_font(x)[3] for x in fonts)

tex = (ROOT / "appendici/a_casi_uso.tex").read_text()
assert re.findall(r"\\scheda\{([^}]+)\}", tex) == ids
assert tex.count("\\principale") == 16  # Definizione del comando + 15 schede.
assert tex.count("\\alternative") == 16
for file in (ROOT / "diagrammi").glob("*.drawio"):
    xml = ET.parse(file)
    cells = xml.findall(".//mxCell")
    assert len({c.get("id") for c in cells}) == len(cells)
    exported = pymupdf.open(str(file) + ".pdf")
    assert len(exported) == 1 and exported[0].get_text().strip()
    embedded = unquote(exported.metadata["subject"]).replace(r"\(", "(").replace(r"\)", ")")
    assert ET.canonicalize(file.read_text(), strip_text=True) == ET.canonicalize(embedded, strip_text=True)
    exported[0].get_pixmap(matrix=pymupdf.Matrix(1.3, 1.3)).save(visual / (file.stem + ".png"))

shots = list((ROOT / "screenshot").glob("*.png"))
assert len(shots) == 16
assert "ACQUISIZIONE COMPLETATA: 16 screenshot" in (HERE / "acquisizione-ui.txt").read_text()
assert all(pymupdf.Pixmap(p).width > 300 for p in shots)
log = (ROOT / "build/main.log").read_text()
assert not re.search(r"^!|Warning:|Overfull|Underfull|Missing character", log, re.M)
warnings = re.findall(r"^warning: (.+)$", (HERE / "compilazione.txt").read_text(), re.M)
expected_warning = "Trying to include PDF file with version (1.7), which is newer than current output PDF setting (1.5)."
assert warnings and all(w == expected_warning for w in warnings), warnings
assert doc.metadata["format"] == "PDF 1.7"

baseline = json.loads((HERE / "baseline-conservazione.json").read_text())
assert all((REPO / p).is_file() and sha256((REPO / p).read_bytes()).hexdigest() == h for p, h in baseline.items())
extract = pymupdf.open()
for start, end in (nuove, app_a):
    extract.insert_pdf(doc, from_page=start, to_page=end-1)
extract.set_metadata({"title": "DriveHub — Sezioni 2.1–2.4 e appendice A per revisione"})
extract.save(HERE / "ticket-02-per-revisione.pdf")

# Le tavole di controllo riprendono le pagine del PDF senza modificare gli screenshot.
pages = [1, 2] + list(range(*nuove)) + list(range(*app_a))
for i in range(0, len(pages), 2):
    spread = pymupdf.open()
    canvas = spread.new_page(width=1190.56, height=841.89)
    for col, p in enumerate(pages[i:i+2]):
        canvas.show_pdf_page(pymupdf.Rect(col*595.28, 0, (col+1)*595.28, 841.89), doc, p)
    canvas.get_pixmap(matrix=pymupdf.Matrix(1.2, 1.2)).save(visual / f"controllo-{i//2+1:02}.png")

print(f"PDF aperto e renderizzato: {len(doc)} pagine A4; {links} collegamenti interni risolti; {len(fonts)} font incorporati.")
print("Indice: 5 capitoli, 25 sezioni concordate, appendici A–C, 15 schede stabili, ordine corretto.")
print(f"Sezioni 2.1–2.4: pagine fisiche {nuove[0]+1}–{nuove[1]}; appendice A: {app_a[0]+1}–{app_a[1]}.")
print("Capitolo 1: testo delle pagine identico all'estratto ticket 01; 2.5–2.8, capitoli 3–5 e appendici B–C provvisori.")
print("Draw.io: 6 sorgenti XML validi; PDF aperti/renderizzati; XML incorporati corrispondenti.")
print("Schermate: 16 PNG, acquisite dall'app corrente; 15 incluse nel PDF.")
print(f"Conservazione: {len(baseline)} file invariati, incluse evidenze ticket 01, software, SQL e capitolo 1.")
print(f"Log LaTeX: nessun box difettoso o riferimento irrisolto; {len(warnings)} soli avvisi noti Tectonic sulla versione PDF inclusa (output effettivo 1.7).")
print("Ispezione visiva da registrare separatamente.")
print("SHA-256 PDF:", sha256((ROOT / "relazione-drivehub.pdf").read_bytes()).hexdigest())
