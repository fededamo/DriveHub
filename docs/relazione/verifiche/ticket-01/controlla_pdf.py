"""Controllo documentale del ticket 01; richiede PyMuPDF, non avvia DriveHub."""

from hashlib import sha256
from pathlib import Path
import re
import unicodedata
from urllib.parse import unquote
import xml.etree.ElementTree as ET

import pymupdf


def normalizza(testo):
    return " ".join(unicodedata.normalize("NFKC", testo).split()).replace("’", "'")


radice = Path(__file__).resolve().parents[2]
pdf = radice / "relazione-drivehub.pdf"
documento = pymupdf.open(pdf)
assert not documento.is_encrypted
indice = (radice / "indice.md").read_text(encoding="utf-8")
segnalibri = documento.get_toc()
capitoli = [titolo for _, titolo in re.findall(r"^## ([1-5])\. (.+)$", indice, re.M)]
appendici = re.findall(r"^- \*\*[ABC] — (.+?)\.\*\*", indice, re.M)
sezioni = re.findall(r"^- \*\*\d+\.\d+ (.+?)\.\*\*", indice, re.M)
assert len(capitoli) == 5 and len(appendici) == 3 and len(sezioni) == 25
assert [normalizza(t) for livello, t, _ in segnalibri if livello == 1] == [
    normalizza(t) for t in capitoli + appendici
]
assert [normalizza(t) for livello, t, _ in segnalibri if livello == 2] == [
    normalizza(t) for t in sezioni
]
assert all(1 <= pagina <= len(documento) for _, _, pagina in segnalibri)
frontespizio = normalizza(documento[0].get_text())
for dato in ("Federico D'Amodio", "Matteo Orlandi", "Ingegneria del Software",
             "Enrico Vicario", "2025/2026", "Università degli Studi di Firenze"):
    assert dato in frontespizio, dato

principali = [pagina for livello, _, pagina in segnalibri if livello == 1]
testo_capitolo = "\n".join(documento[i].get_text() for i in range(principali[0]-1, principali[1]-1))
assert not re.search(r"Provvisorio|H2|FXML|Mockito|JaCoCo|\?\?", testo_capitolo)
assert all("Provvisorio" in documento[pagina-1].get_text() for pagina in principali[1:])
log = (radice / "build/main.log").read_text(encoding="utf-8")
assert not re.search(r"^!|Warning:|Overfull|Underfull|Missing character", log, re.M)
collegamenti = 0
font_usati = set()
immagini = radice / "build/visual"
immagini.mkdir(parents=True, exist_ok=True)
for numero, pagina in enumerate(documento, 1):
    assert abs(pagina.rect.width-595.28) < 1 and abs(pagina.rect.height-841.89) < 1
    assert "??" not in pagina.get_text()
    for link in pagina.get_links():
        assert link["kind"] in (pymupdf.LINK_GOTO, pymupdf.LINK_NAMED)
        assert 0 <= link["page"] < len(documento)
        if link["kind"] == pymupdf.LINK_NAMED:
            assert documento.resolve_link("#" + link["nameddest"])[0] == link["page"]
        collegamenti += 1
    font_usati.update(font[0] for font in pagina.get_fonts())
    pagina.get_pixmap(matrix=pymupdf.Matrix(1.35, 1.35)).save(immagini / f"pagina-{numero:02}.png")
assert all(documento.extract_font(xref)[3] for xref in font_usati)

estratto = pymupdf.open()
estratto.insert_pdf(documento, from_page=principali[0]-1, to_page=principali[1]-2)
estratto.set_metadata({"title": "DriveHub — Capitolo 1 per revisione",
                      "author": documento.metadata["author"]})
estratto.save(Path(__file__).with_name("capitolo-1.pdf"))
prova = Path(__file__).parent / "drawio/prova-esportazione.drawio"
esportazione = pymupdf.open(str(prova) + ".pdf")
assert len(esportazione) == 1 and "Esportazione PDF" in esportazione[0].get_text()
assert ET.canonicalize(prova.read_text(encoding="utf-8"), strip_text=True) == ET.canonicalize(
    unquote(esportazione.metadata["subject"]), strip_text=True
)
esportazione[0].get_pixmap(matrix=pymupdf.Matrix(2, 2)).save(immagini / "prova-drawio.png")
print(f"PDF aperto e renderizzato: {len(documento)} pagine A4, senza cifratura.")
print(f"Indice: {len(capitoli)} capitoli, {len(appendici)} appendici, {len(sezioni)} sezioni concordate, nell'ordine previsto.")
print(f"Frontespizio: dati confermati presenti. Collegamenti interni validi: {collegamenti}.")
print(f"Font incorporati: {len(font_usati)}. Log LaTeX: nessun warning, box difettoso o carattere mancante.")
print(f"Capitolo 1: pagine fisiche {principali[0]}–{principali[1]-1}, estratto in capitolo-1.pdf.")
print("Capitoli successivi e appendici: segnaposto provvisori presenti.")
print("Draw.io: PDF aperto e renderizzato; XML incorporato recuperato e corrispondente al sorgente.")
print("Il controllo visivo delle immagini richiede un'ispezione separata.")
print("SHA-256 relazione-drivehub.pdf:", sha256(pdf.read_bytes()).hexdigest())
