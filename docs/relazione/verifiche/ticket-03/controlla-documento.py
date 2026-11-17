"""Controlli documentali del ticket 03; nessun avvio applicativo o accesso DB."""
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
VIS = DOC / "build/visual-ticket03"
VIS.mkdir(parents=True, exist_ok=True)


def normalizza(text):
    return " ".join(unicodedata.normalize("NFKC", text).split()).replace("’", "'")


doc = pymupdf.open(DOC / "relazione-drivehub.pdf")
assert not doc.is_encrypted
assert (DOC / "relazione-drivehub.pdf").read_bytes() == (DOC / "build/main.pdf").read_bytes()
toc = doc.get_toc()
indice = (DOC / "indice.md").read_text()
chapters = re.findall(r"^## [1-5]\. (.+)$", indice, re.M)
appendices = re.findall(r"^- \*\*[ABC] — (.+?)\.\*\*", indice, re.M)
sections = re.findall(r"^- \*\*\d+\.\d+ (.+?)\.\*\*", indice, re.M)
assert len(chapters)==5 and len(sections)==25 and len(appendices)==3
assert [normalizza(t) for l,t,_ in toc if l==1] == list(map(normalizza,chapters+appendices))
assert [normalizza(t) for l,t,_ in toc if l==2][:25] == list(map(normalizza,sections))


def intervallo(title, following):
    return (next(p for _,t,p in toc if t==title)-1,
            next(p for _,t,p in toc if t==following)-1)


new = intervallo("Package Diagram", "Sequence Diagram")
app_b = intervallo(appendices[1],appendices[2])
new_text = "\n".join(doc[i].get_text() for a,b in (new,app_b) for i in range(a,b))
assert "Provvisorio" not in new_text and "??" not in new_text
for title in sections[9:11]+chapters[2:]+appendices[2:]:
    page = next(p for _,t,p in toc if t==title)
    assert "Provvisorio" in doc[page-1].get_text(), title
subtitles=[t for l,t,p in toc if l==3 and new[0]<p<=new[1]]
assert subtitles==["Domain Model","Business Layer","DAO","Presentation Layer"],subtitles

# Contenuti completati: sorgenti protetti e testo invariato nel PDF.
old1=pymupdf.open(HERE.parent/"ticket-01/capitolo-1.pdf")
a,b=intervallo(chapters[0],chapters[1])
assert [normalizza(doc[i].get_text()) for i in range(a,b)]==[normalizza(p.get_text()) for p in old1]
old2=pymupdf.open(HERE.parent/"ticket-02/ticket-02-per-revisione.pdf")


def contenuto_precedente(page):
    lines=page.get_text().splitlines()
    assert lines[-1].strip().isdigit()
    text="\n".join(lines[:-1])  # Esclude il numero di pagina, aggiornato dopo 2.6.
    text=re.sub(r"Le sezioni .*?provvisorie\.","",text,count=1,flags=re.S)
    return normalizza(text)


current_old=[]
for a,b in (intervallo(sections[3],sections[7]),intervallo(appendices[0],appendices[1])):
    current_old.extend(contenuto_precedente(doc[i]) for i in range(a,b))
assert current_old==[contenuto_precedente(page) for page in old2]

links,fonts=0,set()
for i,page in enumerate(doc):
    assert abs(page.rect.width-595.28)<1 and abs(page.rect.height-841.89)<1
    assert "??" not in page.get_text()
    for link in page.get_links():
        if link["kind"] in (pymupdf.LINK_GOTO,pymupdf.LINK_NAMED):
            assert 0<=link["page"]<len(doc)
            if link["kind"]==pymupdf.LINK_NAMED:
                assert doc.resolve_link("#"+link["nameddest"])[0]==link["page"]
            links+=1
    fonts.update(f[0] for f in page.get_fonts())
    page.get_pixmap(matrix=pymupdf.Matrix(1.5,1.5)).save(VIS/f"pagina-{i+1:02}.png")
assert all(doc.extract_font(f)[3] for f in fonts)

inventory=json.loads((HERE/"inventario-sorgenti.json").read_text())
assert len(inventory)==65 and len({r["package"] for r in inventory})==11
assert {str(p.relative_to(REPO)) for p in (REPO/"src/main/java").rglob("*.java")}=={r["source"] for r in inventory}
view_for={"":"classi-avvio","domain.user":"classi-utenti","domain.vehicle":"classi-catalogo",
          "domain.discount":"classi-sconti","domain.operation":"classi-operazioni","service":"classi-servizi",
          "event":"classi-eventi","dao":"classi-dao","dao.postgres":"classi-postgres",
          "presentation.view":"classi-presentazione","security":"classi-sicurezza"}
for row in inventory:
    assert sha256((REPO/row["source"]).read_bytes()).hexdigest()==row["sha256"]
    suffix=row["package"].removeprefix("it.unifi.drivehub").lstrip(".")
    name="classi-transazioni" if row["type"]=="DatabaseManager" else view_for[suffix]
    xml=ET.parse(DOC/"diagrammi"/(name+".drawio"))
    labels=" ".join(html.unescape(c.get("value","")) for c in xml.findall(".//mxCell"))
    assert row["type"] in labels,(name,row["type"])

diagrams=sorted(p for p in (DOC/"diagrammi").glob("*.drawio") if p.stem.startswith(("classi-","package-")))
assert len(diagrams)==13
minimum=[]
for file in diagrams:
    tree=ET.parse(file); cells=tree.findall(".//mxCell")
    ids={c.get("id") for c in cells};assert len(ids)==len(cells)
    for c in cells:
        if c.get("edge"):
            assert c.get("source") in ids and c.get("target") in ids
            assert c.find("mxGeometry") is not None
            assert "diamond" not in c.get("style","")
    exported=pymupdf.open(str(file)+".pdf")
    assert len(exported)==1 and exported[0].get_text().strip()
    embedded=unquote(exported.metadata["subject"]).replace(r"\(","(").replace(r"\)",")")
    assert ET.canonicalize(file.read_text(),strip_text=True)==ET.canonicalize(embedded,strip_text=True),file.name
    exported[0].get_pixmap(matrix=pymupdf.Matrix(1.5,1.5)).save(VIS/(file.stem+".png"))
    # Ogni label deve essere presente nel PDF esportato: controlla anche note e bordi.
    spans=[s for block in exported[0].get_text("dict")["blocks"] if "lines" in block for line in block["lines"] for s in line["spans"]]
    assert spans
    bounds=exported[0].rect+(-1,-1,1,1)
    assert all(bounds.contains(pymupdf.Rect(s["bbox"])) for s in spans)
    minimum.append((file.stem,round(min(s["size"] for s in spans),2)))

baseline=json.loads((HERE/"baseline-conservazione.json").read_text())
assert all((REPO/p).is_file() and sha256((REPO/p).read_bytes()).hexdigest()==h for p,h in baseline.items())
log=(DOC/"build/main.log").read_text()
assert not re.search(r"^!|Warning:|Overfull|Underfull|Missing character",log,re.M)
warnings=re.findall(r"^warning: (.+)$",(HERE/"compilazione.txt").read_text(),re.M)
expected="Trying to include PDF file with version (1.7), which is newer than current output PDF setting (1.5)."
assert all(w==expected for w in warnings),warnings
assert doc.metadata["format"]=="PDF 1.7"

extract=pymupdf.open()
for a,b in (new,app_b):extract.insert_pdf(doc,from_page=a,to_page=b-1)
extract.set_metadata({"title":"DriveHub — Sezioni 2.5–2.6 e appendice B per revisione"})
extract.save(HERE/"ticket-03-per-revisione.pdf")
print(f"PDF aperto e renderizzato: {len(doc)} pagine A4, {links} link interni risolti, {len(fonts)} font incorporati.")
print("Indice: 5 capitoli, 25 sezioni, appendici A–C; sottosezioni 2.6 nell'ordine concordato.")
print(f"2.5–2.6 complete: pagine fisiche {new[0]+1}–{new[1]}; B completa: {app_b[0]+1}–{app_b[1]}.")
print("Provvisori: 2.7–2.8, capitoli 3–5, appendice C. Nessuna approvazione degli autori dichiarata.")
print("13 nuovi draw.io validi, 13 esportazioni su singola pagina con XML incorporato corrispondente.")
print("Copertura: 11 package, 65 tipi principali; impronte sorgenti identiche al riscontro iniziale.")
print(f"Conservazione: {len(baseline)} file protetti invariati, inclusi evidenze/manifest 01–02, screenshot, diagrammi precedenti, software e SQL.")
print("Capitolo 1: testo identico all'estratto storico. 2.1–2.4/A: testo delle 32 pagine identico all'estratto 02, esclusi avviso di stato e numerazione; sorgenti e figure invariati.")
print(f"LaTeX: nessun box difettoso, carattere mancante o riferimento irrisolto; {len(warnings)} soli avvisi noti Tectonic PDF 1.7/1.5.")
print("Font minimi nelle esportazioni prima della riduzione nel documento:",minimum)
print("Controllo visivo da registrare separatamente; questi assert non lo sostituiscono.")
print("SHA-256 PDF:",sha256((DOC/"relazione-drivehub.pdf").read_bytes()).hexdigest())
