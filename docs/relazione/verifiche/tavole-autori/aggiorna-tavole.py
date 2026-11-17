"""Correzioni mirate delle tre tavole degli autori; gli originali restano conservati."""
from copy import deepcopy
from pathlib import Path
import re
import xml.etree.ElementTree as ET

DOC = Path(__file__).resolve().parents[2]
ORIGINALS = DOC.parent
OUT = DOC / "diagrammi"


def load(name):
    tree = ET.parse(ORIGINALS / name)
    root = tree.getroot().find("diagram/mxGraphModel/root")
    return tree, root, {c.get("id"): c for c in root}


def style(cell, **values):
    parts = cell.get("style", "").split(";")
    parts = [p for p in parts if p and p.split("=")[0] not in values]
    cell.set("style", ";".join(parts + [f"{k}={v}" for k, v in values.items()]) + ";")


def remove(root, ids):
    ids = set(ids)
    while True:
        extra = {c.get("id") for c in root if any(c.get(k) in ids for k in ("parent", "source", "target"))}
        if extra <= ids:
            break
        ids |= extra
    for c in list(root):
        if c.get("id") in ids:
            root.remove(c)


def edge(root, id_, source, target, label="", **styles):
    c = ET.SubElement(root, "mxCell", id=id_, source=source, target=target,
                      parent="1", edge="1", value=label,
                      style="edgeStyle=orthogonalEdgeStyle;html=1;rounded=0;")
    style(c, **styles)
    ET.SubElement(c, "mxGeometry", relative="1", **{"as": "geometry"})
    return c


def clone(root, template, id_, text, x, y):
    c = deepcopy(template)
    c.set("id", id_)
    c.set("value", text)
    c.find("mxGeometry").set("x", str(x))
    c.find("mxGeometry").set("y", str(y))
    root.append(c)
    return c


def save(tree, name):
    for m in tree.getroot().iter("mxGraphModel"):
        m.set("adaptiveColors", "auto")
    for root in tree.getroot().iter("root"):
        cells = list(root)
        ids = {c.get("id") for c in cells}
        assert len(ids) == len(cells), name
        for c in cells:
            assert all(c.get(k) is None or c.get(k) in ids for k in ("parent", "source", "target")), c.attrib
            assert c.get("id") != c.get("parent"), c.attrib
            if c.get("edge") == "1":
                assert c.find("mxGeometry") is not None
    ET.indent(tree)
    tree.write(OUT / name, encoding="utf-8", xml_declaration=False)


# Navigazione: stessa struttura accesso -> ruolo -> dashboard -> schede/comandi.
tree, root, c = load("Page Navigation Diagram.drawio")
prefix = "Ku0o8qk710Uc4jAowcEN-"
labels = {
    1: "LoginView<br>Accedi / Registrati", 5: "Accedi", 6: "Registrati<br>(solo cliente)",
    14: "Ruolo dell'utente<br>autenticato", 16: "Venditore", 17: "Cliente", 18: "Manager",
    28: "Marchi", 29: "Veicoli", 30: "Registro economico", 31: "Gestione Account<br>(dialogo)",
    33: "Richieste in Sospeso<br>(SALE / PURCHASE)", 34: "Gestione Stato Inventario<br>(SALE / RENT)",
    37: "Auto in Vendita", 38: "Le Mie Operazioni", 39: "Ricarica Saldo<br>(comando header)",
    41: "Vendi la tua Auto", 42: "Auto a Noleggio", 11: "Errore", 10: "Errore",
    27: "Disconnetti", 24: "Disconnetti", 25: "Disconnetti",
}
for n, text in labels.items():
    c[prefix + str(n)].set("value", text)
for id_ in ("g02yWQ5K493F6th6uxH4-4", "g02yWQ5K493F6th6uxH4-7"):
    c[id_].set("value", "Gestione Account<br>(dialogo)")
c["QxHydpno4o-kZMKg6v4i-4"].set("value", "Crea account cliente")
remove(root, [prefix + "32", prefix + "35"])
for cell in root:
    if cell.get("source") == prefix + "6" and cell.get("target") == prefix + "14":
        cell.set("target", prefix + "17")
        cell.set("value", "Registrazione riuscita")
        cell.find("mxGeometry").clear()
        cell.find("mxGeometry").attrib.update(relative="1", **{"as": "geometry"})
        cell.set("style", "edgeStyle=orthogonalEdgeStyle;html=1;endArrow=classic;")
    if cell.get("source") == prefix + "5" and cell.get("target") == prefix + "14":
        cell.set("value", "Accesso riuscito")
clone(root, c[prefix + "28"], "autori-tab-modelli", "Modelli", 1090, 1130)
clone(root, c[prefix + "29"], "autori-tab-sconti", "Sconti", 1090, 1210)
edge(root, "autori-nav-modelli", prefix + "18", "autori-tab-modelli", endArrow="classic")
edge(root, "autori-nav-sconti", prefix + "18", "autori-tab-sconti", endArrow="classic")
for cell in root:
    if cell.get("vertex") == "1":
        style(cell, fontSize="16")
        if cell.get("parent") == "1" and cell.get("id") not in {prefix + "1", prefix + "14"}:
            cell.find("mxGeometry").set("width", "155")
            cell.find("mxGeometry").set("height", "65")
# Separazioni locali per i nuovi nomi, mantenendo le diramazioni degli autori.
for n, x, y in [(33, 100, 1040), (34, 350, 1040), (28, 1040, 1060),
                (29, 1060, 980), (30, 890, 1300), (31, 840, 1380),
                (37, -290, 1000), (38, -230, 1080), (42, -170, 1160),
                (41, -110, 1240), (39, -50, 1320)]:
    c[prefix + str(n)].find("mxGeometry").attrib.update(x=str(x), y=str(y))
c["g02yWQ5K493F6th6uxH4-7"].find("mxGeometry").set("x", "600")
c["g02yWQ5K493F6th6uxH4-4"].find("mxGeometry").attrib.update(x="10", y="1400")
c[prefix + "33"].find("mxGeometry").attrib.update(width="200", height="80")
# Corridoi esterni: i collegamenti non attraversano più i nomi delle schede.
def route(cell, points, exit_x, exit_y, entry_x, entry_y):
    style(cell, exitX=exit_x, exitY=exit_y, entryX=entry_x, entryY=entry_y)
    g = cell.find("mxGeometry")
    g.clear()
    g.attrib.update(relative="1", **{"as": "geometry"})
    array = ET.SubElement(g, "Array", **{"as": "points"})
    for x, y in points:
        ET.SubElement(array, "mxPoint", x=str(x), y=str(y))


for id_, x, y in [("autori-tab-modelli", 990, 1140), ("autori-tab-sconti", 940, 1220)]:
    next(cell for cell in root if cell.get("id") == id_).find("mxGeometry").attrib.update(x=str(x), y=str(y))
for source, targets, side in [
    (prefix + "17", [prefix + str(n) for n in (37, 38, 42, 41, 39)] + ["g02yWQ5K493F6th6uxH4-4"], 0),
    (prefix + "18", [prefix + "29", prefix + "28", "autori-tab-modelli", "autori-tab-sconti", prefix + "30", prefix + "31"], 1),
]:
    for i, target in enumerate(targets):
        cell = next(cell for cell in root if cell.get("source") == source and cell.get("target") == target)
        destination = next(cell for cell in root if cell.get("id") == target).find("mxGeometry")
        target_y = float(destination.get("y")) + float(destination.get("height")) / 2
        track = -450 + i * 25 if side == 0 else 1340 + i * 25
        exit_y = .1 + i * .15
        route(cell, [(track, 830 + 65 * exit_y), (track, target_y)], side, exit_y, side, .5)
success = next(cell for cell in root if cell.get("source") == prefix + "6" and cell.get("target") == prefix + "17")
route(success, [(567.5, 500), (80, 500), (80, 862.5)], .5, 1, 1, .5)
save(tree, "navigazione-autori.drawio")


# Casi d'uso: attori, generalizzazioni ed ellissi originali.
tree, root, c = load("Users SWE.drawio")
prefix = "WRvqtLsy7lfnVHb6Kxkt-"
labels = {
    1: "Utente", 3: "Venditore", 15: "Manager", 19: "Cliente",
    8: "Registrarsi<br>[UC-S01]", 11: "Accedere<br>[UC-S02]",
    21: "Prenotare un<br>test drive<br>[UC-01]", 22: "Noleggiare<br>un veicolo<br>[UC-02]",
    23: "Richiedere e completare<br>un acquisto<br>[UC-03]",
    24: "Proporre un veicolo<br>alla concessionaria<br>[UC-05]",
    28: "Cambiare<br>SALE / RENT<br>[UC-08]", 29: "Autorizzare<br>il saldo<br>[UC-04]",
    31: "Valutare la<br>proposta cliente<br>[UC-05]", 36: "Consultare il<br>registro economico<br>[UC-10]",
    37: "Gestire veicoli<br>e basi prezzo<br>[UC-09]", 38: "Gestire sconti<br>[UC-06]",
    39: "Gestire marche<br>e modelli<br>[UC-09]",
}
for n, text in labels.items():
    c[prefix + str(n)].set("value", text)
remove(root, [prefix + "30"])
for n in (1, 3, 15, 19):
    style(c[prefix + str(n)], verticalLabelPosition="bottom", verticalAlign="top", fontSize="13")
for cell in root:
    if cell.get("source") == prefix + "1" and cell.get("target") == prefix + "8":
        cell.set("source", prefix + "19")
        cell.set("style", "html=1;endArrow=none;")
        cell.find("mxGeometry").clear()
        cell.find("mxGeometry").attrib.update(relative="1", **{"as": "geometry"})
    elif cell.get("source") == prefix + "1" and cell.get("target") == prefix + "11":
        style(cell, endArrow="none")
c[prefix + "8"].find("mxGeometry").attrib.update(x="460", y="2060")
c["z5OWCV3LV5pAFw0giZIU-2"].set("value", "Consultare<br>catalogo<br>[UC-07]")
c["z5OWCV3LV5pAFw0giZIU-4"].set("value", "Consultare le<br>proprie operazioni<br>[UC-11]")
clone(root, c[prefix + "11"], "autori-gestione-identita", "Gestire identità<br>[UC-S03]", 250, 1900)
edge(root, "autori-utente-identita", prefix + "1", "autori-gestione-identita", endArrow="none").set("style", "html=1;endArrow=none;")
clone(root, c[prefix + "8"], "autori-ricarica", "Ricaricare<br>conto demo<br>[UC-S04]", 460, 2240)
edge(root, "autori-cliente-ricarica", prefix + "19", "autori-ricarica", endArrow="none").set("style", "html=1;endArrow=none;")
# Gli ovali rimangono sfalsati; si elimina la sovrapposizione fra casi adiacenti.
positions = {21: (950, 1900), 22: (1120, 1985), 23: (950, 2070), 24: (1120, 2155),
             28: (1010, 1610), 29: (1170, 1700), 31: (1090, 1810),
             36: (1010, 1260), 37: (1170, 1350), 38: (1010, 1440), 39: (1170, 1530)}
for n, (x, y) in positions.items():
    c[prefix + str(n)].find("mxGeometry").attrib.update(x=str(x), y=str(y))
for cell in root:
    if "ellipse;" in cell.get("style", ""):
        style(cell, fontSize="12")
        cell.find("mxGeometry").set("width", "150")
c["z5OWCV3LV5pAFw0giZIU-2"].find("mxGeometry").attrib.update(x="950", y="2240")
c["z5OWCV3LV5pAFw0giZIU-4"].find("mxGeometry").attrib.update(x="1120", y="2325")
c["z5OWCV3LV5pAFw0giZIU-10"].find("mxGeometry").attrib.update(x="1400", y="2040")
save(tree, "casi-uso-autori.drawio")


# ER: conservati tabelle a tre colonne, gerarchia concettuale e zampe di corvo.
tree, root, c = load("SWE ER.drawio")
tables = {cell.get("value"): cell for cell in root if "shape=table;" in cell.get("style", "")}


def rows(table):
    result = []
    for row in root:
        if row.get("parent") == table.get("id"):
            fields = sorted((f for f in root if f.get("parent") == row.get("id")),
                            key=lambda f: float(f.find("mxGeometry").get("x", "0")))
            result.append((row, fields))
    return result


def field(table, name):
    return next(fields for _, fields in rows(tables[table])
                if len(fields) == 3 and re.sub("<[^>]*>", "", fields[1].get("value", "")).strip() == name)


def addrow(table, name, sqltype, keys):
    target = tables[table]
    template, fields = next((r, f) for r, f in rows(target) if len(f) == 3)
    row = deepcopy(template)
    rid = "autori-" + table.lower() + "-" + name
    row.set("id", rid)
    row.find("mxGeometry").set("y", target.find("mxGeometry").get("height"))
    root.append(row)
    for i, (old, value) in enumerate(zip(fields, [keys, name, ": " + sqltype])):
        f = deepcopy(old)
        f.set("id", rid + "-" + str(i))
        f.set("parent", rid)
        f.set("value", value)
        root.append(f)
    geom = target.find("mxGeometry")
    geom.set("height", str(float(geom.get("height")) + 30).removesuffix(".0"))


field("Vehicle", "state")[2].set("value", ": vehicle_state")
field("Request", "vehicle")[0].set("value", "FK, U, N")
addrow("Vehicle", "price_mode", "price_mode", "N")
addrow("Request", "agreed_total", "DECIMAL(10, 2)", "")
addrow("Request", "type", "request_type", "N")
addrow("Sale", "type", "sale_type", "N")
for cell in root:
    if cell.get("source") == tables["Vehicle"].get("id") and cell.get("target") == tables["Request"].get("id"):
        style(cell, endArrow="ERzeroToOne")
    if cell.get("source") == tables["Client"].get("id") and cell.get("edge") == "1":
        cell.set("target", tables["Request"].get("id"))
    if cell.get("style"):
        style(cell, fontSize="16")
# User è il supertipo del modello concettuale, non una tabella con PK globale.
tables["User"].set("value", "User (supertipo concettuale)")
field("User", "id")[0].set("value", "ID")
field("User", "email")[0].set("value", "N")
for name in ("Client", "Salesman", "Manager"):
    for _, fields in rows(tables[name]):
        for cell in fields:
            if "Non ci sono attributi" in cell.get("value", ""):
                cell.set("value", "PK id; U email (per ruolo)")
# Più spazio per i tipi nativi e per i nomi degli attributi aggiunti.
for table in tables.values():
    table.find("mxGeometry").set("width", "350")
    for row, fields in rows(table):
        row.find("mxGeometry").set("width", "350")
        if len(fields) == 3:
            for cell, x, width in zip(fields, (0, 80, 205), (80, 125, 145)):
                cell.find("mxGeometry").attrib.update(x=str(x), width=str(width))
        elif len(fields) == 1:
            fields[0].find("mxGeometry").set("width", "350")
# Legenda e note sono già una seconda pagina della tavola degli autori.
notes = tree.getroot().findall("diagram")[1].find("mxGraphModel/root")
note_cells = [cell for cell in notes if cell.get("value")]
note_cells[0].set("value", "<b>LEGENDA</b><br>PK: primaria; FK: riferimento; N: NOT NULL; U: UNIQUE.<br>ID nel supertipo: identificativo concettuale, non PK globale.<br>t, d: ruoli totali e disgiunti nel modello concettuale.<br>I limiti delle garanzie SQL sono discussi nella relazione.")
note_cells[1].set("value", "<b>TIPI E VINCOLI ATTUALI</b><br>vehicle_state: SALE, RENT, BLOCKED, WAITING, SOLD.<br>request_type: SALE, PURCHASE; price_mode: AUTOMATIC, MANUAL.<br>sale_type: SALE, PURCHASE, PENALTY.<br>DECIMAL(10,2): dieci cifre complessive, due decimali; spazio variabile.<br>Request: un solo record per veicolo; agreed_total obbligatorio solo per SALE.<br>TestDrive: UQ (vehicle,date); Rent: periodi inclusivi non sovrapponibili per veicolo.<br>Il conto è opzionale nel DDL, creato insieme al cliente dal servizio.")
note_cells[2].set("value", "<b>MAPPING DELLE IDENTITÀ E REGISTRO</b><br>User è un supertipo concettuale: client, salesman e manager sono tre tabelle.<br>La vista user usa UNION ALL; PK id e U email sono locali al ruolo.<br>La sequenza comune non impone unicità globale; il trigger email ha limiti concorrenti.<br>Sale.vehicle e Sale.salesman sono nullable; la penale del servizio non li valorizza.<br>Sale.vehicle usa ON DELETE SET NULL: il riferimento può mancare anche per cancellazione.")
for i, cell in enumerate(note_cells):
    style(cell, fontSize="16", align="left", verticalAlign="top")
    if i < 2:
        group = next(n for n in notes if n.get("id") == cell.get("parent"))
        width = 520 if i == 0 else 880
        group.find("mxGeometry").attrib.update(x=str(40 if i == 0 else 600), y="40", width=str(width), height="290")
        border = next(n for n in notes if n.get("parent") == group.get("id") and not n.get("value"))
        border.find("mxGeometry").attrib.update(width=str(width), height="290")
        cell.find("mxGeometry").attrib.update(x="25", y="20", width=str(width - 50), height="250")
    else:
        cell.find("mxGeometry").attrib.update(x="40", y="380", width="1440", height="230")
save(tree, "database-er-autori.drawio")
print("Tre tavole corrette; identificativi e notazione degli autori conservati.")
