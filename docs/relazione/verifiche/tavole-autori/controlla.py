"""Verifica tavole degli autori, conservazione, PDF e ZIP correnti; nessun accesso DB."""
from pathlib import Path
from urllib.parse import unquote
import hashlib
import html
import json
import re
import subprocess
import unicodedata
import xml.etree.ElementTree as ET
import zipfile

import pymupdf

HERE = Path(__file__).resolve().parent
DOC = HERE.parents[1]
ROOT = DOC.parents[1]
names = {
    "navigazione-autori": "Page Navigation Diagram.drawio",
    "casi-uso-autori": "Users SWE.drawio",
    "database-er-autori": "SWE ER.drawio",
}
sha = lambda p: hashlib.sha256(p.read_bytes()).hexdigest()
normalize = lambda s: re.sub(r"\s+", "", unicodedata.normalize("NFKC", s))

originals = {}
for name, original in names.items():
    path = ROOT / "docs" / original
    assert path.read_bytes() == subprocess.check_output(["git", "show", "HEAD:docs/" + original], cwd=ROOT)
    originals[original] = sha(path)
    before = {c.get("id") for c in ET.parse(path).getroot().find("diagram/mxGraphModel/root")}
    after = {c.get("id") for c in ET.parse(DOC / "diagrammi" / (name + ".drawio")).getroot().find("diagram/mxGraphModel/root")}
    assert len(before & after) / len(before) > .8, name

# Tutte le tavole di dettaglio precedenti restano immutate.
baseline = json.loads((DOC / "verifiche/ticket-08/baseline-conservazione.json").read_text())["files"]
protected = [p for p in baseline if p.startswith("docs/relazione/diagrammi/") and p.endswith((".drawio", ".drawio.pdf"))]
assert len(protected) == 64
assert all(sha(ROOT / p) == baseline[p] for p in protected)

nav = ET.parse(DOC / "diagrammi/navigazione-autori.drawio").getroot()
cells = {c.get("id"): c for c in nav.iter("mxCell")}
p = "Ku0o8qk710Uc4jAowcEN-"
assert any(c.get("source") == p + "6" and c.get("target") == p + "17" for c in cells.values())
assert not any(c.get("source") == p + "6" and c.get("target") == p + "14" for c in cells.values())
assert not any("Welcome" in c.get("value", "") or "Rental and Test Drive Overview" in c.get("value", "") for c in cells.values())
for role, count in [("17", 6), ("16", 3), ("18", 6)]:
    # Tab più comandi header: 4+2, 2+1 e 5+1; logout escluso.
    targets = [c.get("target") for c in cells.values() if c.get("source") == p + role and c.get("target") != p + "1"]
    assert len(targets) == count, (role, targets)

uc = ET.parse(DOC / "diagrammi/casi-uso-autori.drawio").getroot()
p = "WRvqtLsy7lfnVHb6Kxkt-"
assert [c.get("source") for c in uc.iter("mxCell") if c.get("target") == p + "8"] == [p + "19"]
assert not any(c.get("id") == p + "30" for c in uc.iter("mxCell"))
assert all(f"UC-{i:02}" in ET.tostring(uc, encoding="unicode") for i in range(1, 12))

er = ET.parse(DOC / "diagrammi/database-er-autori.drawio").getroot()
root = er.find("diagram/mxGraphModel/root")
tables = {c.get("value"): c for c in root if "shape=table;" in c.get("style", "")}
assert len(tables) == 14  # Tredici entità persistenti e il supertipo concettuale User.
assert "User (supertipo concettuale)" in tables
for field in ("price_mode", "agreed_total", "request_type", "sale_type", "vehicle_state"):
    assert field in ET.tostring(er, encoding="unicode")
assert any(c.get("source") == tables["Vehicle"].get("id") and c.get("target") == tables["Request"].get("id") and "endArrow=ERzeroToOne;" in c.get("style", "") for c in root)

# Copertura indipendente dal generatore: attributi e riferimenti dal DDL corrente.
def attributes(table):
    fields = set()
    for row in root:
        if row.get("parent") != table.get("id"):
            continue
        parts = sorted((f for f in root if f.get("parent") == row.get("id")),
                       key=lambda f: float(f.find("mxGeometry").get("x", "0")))
        if len(parts) == 3:
            fields.add(html.unescape(re.sub("<[^>]*>", "", parts[1].get("value", ""))).strip())
    return fields


mapping = {"Brand": "brand", "Model": "model", "ModelDiscount": "model_discount",
           "Vehicle": "vehicle", "VehicleDiscount": "vehicle_discount", "Client": "client",
           "Salesman": "salesman", "Manager": "manager", "BankAccount": "bank_account",
           "Request": "request", "TestDrive": "test_drive", "Rent": "rent", "Sale": "sale"}
sql = (ROOT / "src/main/resources/db/schema.sql").read_text()
ddl = dict(re.findall(r'CREATE TABLE "(\w+)" \((.*?)\n\);', sql, re.S))
assert len(ddl) == 13
fk_pairs = set()
for entity, name in mapping.items():
    fields = attributes(tables[entity])
    if entity in {"Client", "Salesman", "Manager"}:
        fields |= attributes(tables["User (supertipo concettuale)"])
    assert fields == set(re.findall(r'^    "(\w+)" ', ddl[name], re.M)), (entity, fields)
    fk_pairs |= {frozenset((name, parent)) for parent in re.findall(r'REFERENCES "(\w+)"', ddl[name])}
entity_ids = {tables[e].get("id"): name for e, name in mapping.items()}
drawn_pairs = {frozenset((entity_ids[c.get("source")], entity_ids[c.get("target")]))
               for c in root if c.get("edge") == "1" and c.get("source") in entity_ids and c.get("target") in entity_ids}
assert len(fk_pairs) == 15 and fk_pairs == drawn_pairs

pdf = pymupdf.open(DOC / "relazione-drivehub.pdf")
a3 = [i + 1 for i, page in enumerate(pdf) if page.rect.width > 1000]
assert len(a3) == 4, a3
assert all(abs(page.rect.height - 842) < 1 for page in pdf)
positions = {}
diagrams = list((DOC / "diagrammi").glob("*.drawio"))
assert len(diagrams) == 35
for path in diagrams:
    source = ET.parse(path).getroot()
    for root in source.iter("root"):
        cells = list(root)
        ids = {c.get("id") for c in cells}
        assert len(ids) == len(cells), path.name
        for c in cells:
            assert all(c.get(k) is None or c.get(k) in ids for k in ("parent", "source", "target"))
            assert c.get("edge") != "1" or c.find("mxGeometry") is not None
    export = pymupdf.open(str(path) + ".pdf")
    embedded = unquote(export.metadata["subject"]).replace(r"\(", "(").replace(r"\)", ")")
    assert ET.canonicalize(path.read_text(), strip_text=True) == ET.canonicalize(embedded, strip_text=True), path.name
    if path.stem in names:
        matches = []
        for page in export:
            text = normalize(page.get_text())
            hits = [i + 1 for i, p in enumerate(pdf) if text in normalize(p.get_text())]
            assert len(hits) == 1, (path.name, hits)
            matches += hits
        positions[path.stem] = matches

for page in pdf:
    assert "??" not in page.get_text()
    for font in page.get_fonts(full=True):
        assert pdf.extract_font(font[0])[3], font
    for link in page.get_links():
        if link["kind"] == pymupdf.LINK_NAMED:
            assert pdf.resolve_link("#" + link["nameddest"]), link
        elif link["kind"] == pymupdf.LINK_GOTO:
            assert 0 <= link["page"] < len(pdf), link

log = (HERE / "compilazione-finale.txt").read_text()
assert not re.search(r"Warning|Overfull|Underfull|Missing character|undefined|^!", log, re.M)
diagnostics = [l for l in log.splitlines() if "ignored error" in l]
assert len(diagnostics) == 5, diagnostics

with zipfile.ZipFile(HERE / "sorgenti-relazione.zip") as archive:
    assert len(archive.namelist()) == 101
    assert archive.testzip() is None
    for name in archive.namelist():
        assert archive.read(name) == (DOC / name).read_bytes(), name

result = {"pages": len(pdf), "a3_pages": a3, "author_diagrams": positions,
          "drawio_pdf_pairs": len(diagrams), "preserved_detail_files": len(protected),
          "ddl_tables": len(ddl), "ddl_columns": sum(len(re.findall(r'^    "(\w+)" ', body, re.M)) for body in ddl.values()),
          "foreign_key_pairs": len(fk_pairs),
          "original_sha256": originals, "tex_diagnostics": diagnostics,
          "pdf_sha256": sha(DOC / "relazione-drivehub.pdf"),
          "zip_sha256": sha(HERE / "sorgenti-relazione.zip")}
(HERE / "esito.json").write_text(json.dumps(result, ensure_ascii=False, indent=2) + "\n")
print(json.dumps(result, ensure_ascii=False, indent=2))
