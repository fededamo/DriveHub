"""Tavole progettate a mano in XML draw.io; non estrae UML dal codice."""
from pathlib import Path
import xml.etree.ElementTree as ET

OUT = Path(__file__).resolve().parents[2] / "diagrammi"


class Tavola:
    def __init__(self, nome):
        self.nome = nome
        self.model = ET.Element("mxGraphModel", adaptiveColors="auto", grid="1", page="0")
        self.root = ET.SubElement(self.model, "root")
        ET.SubElement(self.root, "mxCell", id="0")
        ET.SubElement(self.root, "mxCell", id="1", parent="0")

    def nodo(self, id, testo, x, y, w, h, stile=""):
        c = ET.SubElement(self.root, "mxCell", id=id, value=testo,
                          style="whiteSpace=wrap;html=1;fontFamily=Helvetica;fontSize=18;"
                          "fontColor=#172b4d;strokeColor=#53657a;fillColor=#ffffff;" + stile,
                          vertex="1", parent="1")
        ET.SubElement(c, "mxGeometry", x=str(x), y=str(y), width=str(w), height=str(h), **{"as": "geometry"})

    def arco(self, a, b, associazione=False):
        c = ET.SubElement(self.root, "mxCell", id=f"e-{a}-{b}", source=a, target=b,
                          style="html=1;strokeColor=#53657a;" +
                          ("edgeStyle=sideToSideEdgeStyle;rounded=0;endArrow=none;" if associazione else "edgeStyle=orthogonalEdgeStyle;rounded=1;endArrow=block;"),
                          edge="1", parent="1")
        ET.SubElement(c, "mxGeometry", relative="1", **{"as": "geometry"})

    def salva(self):
        ids = [c.attrib["id"] for c in self.root]
        assert len(ids) == len(set(ids))
        for c in self.root:
            if c.get("edge"):
                assert c.get("source") in ids and c.get("target") in ids
                assert c.find("mxGeometry") is not None
        OUT.mkdir(parents=True, exist_ok=True)
        p = OUT / f"{self.nome}.drawio"
        ET.indent(self.model)
        ET.ElementTree(self.model).write(p, encoding="utf-8", xml_declaration=True)
        assert ET.parse(p).getroot().tag == "mxGraphModel"


SUPPORTO = {
    "UC-S01": "Registrazione del cliente",
    "UC-S02": "Accesso al sistema",
    "UC-S03": "Gestione dell'identità",
    "UC-S04": "Ricarica del conto demo",
}
CASI = {
    "UC-01": "Prenotazione di un test drive",
    "UC-02": "Prenotazione di un noleggio",
    "UC-03": "Vendita al cliente",
    "UC-04": "Autorizzazione del venditore",
    "UC-05": "Acquisto dal cliente",
    "UC-06": "Gestione degli sconti",
    "UC-07": "Consultazione del catalogo",
    "UC-08": "Cambio di destinazione del veicolo",
    "UC-09": "Gestione del catalogo<br>e delle basi prezzo",
    "UC-10": "Consultazione del registro economico",
    "UC-11": "Consultazione delle proprie operazioni",
}
for ruolo, casi, supporti in [
    ("cliente", ["UC-07", "UC-01", "UC-02", "UC-03", "UC-05", "UC-11"], list(SUPPORTO)),
    ("venditore", ["UC-04", "UC-05", "UC-08"], ["UC-S02", "UC-S03"]),
    ("manager", ["UC-09", "UC-06", "UC-10"], ["UC-S02", "UC-S03"]),
]:
    t = Tavola("casi-uso-" + ruolo)
    h = 130 + 76 * (len(casi) + len(supporti))
    t.nodo("sistema", "DriveHub", 180, 0, 520, h,
           "verticalAlign=top;align=left;spacing=12;fontStyle=1;fillColor=#f9fbfd;")
    t.nodo("attore", ruolo.capitalize(), 30, h / 2 - 70, 90, 120,
           "shape=umlActor;verticalLabelPosition=bottom;verticalAlign=top;fillColor=none;")
    t.nodo("livello", "Obiettivi applicativi" + (" e sottofunzione UC-04" if ruolo == "venditore" else ""),
           195, 40, 490, 30, "strokeColor=none;fillColor=none;fontSize=17;fontStyle=1;")
    for i, id in enumerate(casi):
        t.nodo(id, id + "<br>" + CASI[id], 235, 82 + i * 76, 410, 64,
               "ellipse;fillColor=#e5effa;fontSize=17;")
        t.arco("attore", id, True)
    y = 82 + 76 * len(casi)
    t.nodo("livello-supporto", "Supporto all'interazione (livello inferiore)", 195, y, 490, 30,
           "strokeColor=none;fillColor=none;fontSize=17;fontStyle=1;")
    for i, id in enumerate(supporti):
        t.nodo(id, id + "<br>" + SUPPORTO[id], 235, y + 42 + i * 76, 410, 58,
               "ellipse;fillColor=#eeeeee;")
        t.arco("attore", id, True)
    t.salva()

NAV = {
    "cliente": [
        ("Auto in Vendita", "Seleziona veicolo<br>Acquista (acconto 20%) [UC-03]<br>Prenota Test Drive [UC-01]", "Conferma Avvio Operazione<br>oppure Prenotazione Test Drive<br>OK / Annulla → esito"),
        ("Auto a Noleggio", "Seleziona veicolo<br>Prenota Noleggio [UC-02]<br>Prenota Test Drive [UC-01]", "Prenotazione Noleggio: date e costo<br>oppure Prenotazione Test Drive: data e ora<br>OK / Annulla → esito"),
        ("Vendi la tua Auto", "Compila proposta [UC-05]<br>Calcola Preventivo<br>Invia Proposta di Vendita", "Preventivo nella scheda<br>Proposta Inviata / errore<br>Ritorno alla stessa scheda"),
        ("Le Mie Operazioni", "Consulta richieste, noleggi, test drive [UC-11]<br>Paga saldo concordato [UC-03]<br>Annulla Test Drive Selezionato [UC-01]", "Conferma Saldo Finale → esito<br>Annullamento test drive → esito<br>Ritorno e aggiornamento delle tabelle"),
    ],
    "venditore": [
        ("Richieste in Sospeso", "Seleziona richiesta<br>Approva Richiesta / Rifiuta Richiesta<br>[UC-04 per SALE; UC-05 per PURCHASE]", "Esito / errore, senza conferma preventiva<br>SALE approvata: cliente può saldare<br>PURCHASE accettata: accredito e acquisto"),
        ("Gestione Stato Inventario<br>(SALE / RENT)", "Seleziona veicolo<br>Modifica Stato<br>[UC-08]", "Conferma Cambio Stato<br>Avviso rimborsi futuri per RENT → SALE<br>Conferma / Annulla → esito"),
    ],
    "manager": [
        ("Marchi", "Nome → Aggiungi Brand<br>Selezione → Elimina Selezionato<br>[UC-09]", "Aggiunta / eliminazione: esito o errore<br>Nessuna conferma preventiva<br>Vincoli sulle associazioni"),
        ("Modelli", "Marchio, nome, base, anno → Aggiungi Modello<br>Selezione → Elimina Selezionato<br>[UC-09]", "Aggiunta / eliminazione: esito o errore<br>Nessuna conferma preventiva<br>Vincoli sulle associazioni"),
        ("Veicoli", "Aggiungi Veicolo / Elimina Selezionato<br>Automatico o base manuale<br>Applica base alla selezione [UC-09]", "Form nella scheda, esito / errore<br>Nessuna conferma preventiva al DELETE<br>Base manuale: input nella scheda"),
        ("Sconti", "Modello oppure ID veicolo + % → Salva<br>Selezione sconto → Rimuovi<br>[UC-06]", "Salva: esito / errore<br>Rimuovi: aggiornamento della tabella<br>Nessun nuovo percorso di pagina"),
        ("Registro economico", "Consulta vendite al cliente,<br>acquisti dal cliente e penali<br>[UC-10]", "Tabella di sola consultazione<br>Aggiorna / Riprova nel caricamento<br>Nessun dialogo di modifica"),
    ],
}
for ruolo, righe in NAV.items():
    t = Tavola("navigazione-" + ruolo)
    t.nodo("accesso", "Accedi [UC-S02]<br>Registrati [UC-S01, solo cliente]", 20, 20, 300, 85,
           "rounded=1;fillColor=#eeeeee;")
    t.nodo("dashboard", "Dashboard " + ruolo + "<br>accesso riuscito", 405, 20, 300, 85,
           "rounded=1;fillColor=#d9e8fa;fontStyle=1;")
    t.nodo("comuni", "Header: Gestione Account [UC-S03]<br>" +
           ("Ricarica Saldo [UC-S04]<br>" if ruolo == "cliente" else "") +
           "Disconnetti → Accedi", 790, 20, 330, 85, "rounded=1;fillColor=#eeeeee;fontSize=17;")
    t.arco("accesso", "dashboard")
    t.arco("dashboard", "comuni")
    t.nodo("schede", "Schede della dashboard — selezione libera, nessuna finestra nuova", 20, 160,
           1100, len(righe) * 140 + 60,
           "verticalAlign=top;spacingTop=12;fontStyle=1;fillColor=#f9fbfd;")
    t.arco("dashboard", "schede")
    for i, (tab, azioni, esiti) in enumerate(righe):
        y = 215 + i * 140
        t.nodo(f"tab{i}", tab, 40, y, 220, 95, "rounded=1;fillColor=#d9e8fa;fontStyle=1;")
        t.nodo(f"azioni{i}", azioni, 310, y, 350, 95, "rounded=1;fontSize=17;")
        t.nodo(f"dialoghi{i}", esiti, 710, y, 390, 95, "rounded=1;fillColor=#fff2d9;fontSize=17;")
        t.arco(f"tab{i}", f"azioni{i}")
        t.arco(f"azioni{i}", f"dialoghi{i}")
    t.nodo("legenda", "Blu: schermate / tab. Bianco: azioni. Ocra: dialoghi o risultati nella stessa scheda.<br>"
           "Gestione Account: Modifica Email, Modifica Password, Elimina Identità" +
           (", Ricarica Saldo (cliente)." if ruolo == "cliente" else ".") +
           "<br>Chiudi / Annulla: ritorno al chiamante. Errore: permanenza nel contesto; uscita o eliminazione riuscita: Accedi.",
           20, 240 + len(righe) * 140, 1100, 100,
           "strokeColor=none;fillColor=none;align=left;fontSize=17;")
    t.salva()
print("Creati e validati 6 sorgenti draw.io con identificativi stabili.")
