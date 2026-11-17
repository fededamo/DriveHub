"""Disposizioni progettuali manuali del ticket 04: non legge Java né SQL."""
from pathlib import Path
import html
import json
import math
import xml.etree.ElementTree as ET

HERE = Path(__file__).resolve().parent
OUT = HERE.parents[1] / "diagrammi"
BLUE, RED, GRAY = "#175b88", "#a33030", "#52616b"


class Tavola:
    def __init__(self, name, title, width=960):
        self.name, self.width, self.n = name, width, 1
        self.model = ET.Element("mxGraphModel", adaptiveColors="auto", grid="1", page="0")
        self.root = ET.SubElement(self.model, "root")
        ET.SubElement(self.root, "mxCell", id="0")
        ET.SubElement(self.root, "mxCell", id="1", parent="0")
        self.text(title, 10, 8, width-20, 48, 27, bold=True)

    def cell(self, value, x, y, w, h, style):
        self.n += 1
        c = ET.SubElement(self.root, "mxCell", id=f"v{self.n}", value=value,
                          style=style+";html=1;whiteSpace=wrap;", vertex="1", parent="1")
        ET.SubElement(c, "mxGeometry", x=str(x), y=str(y), width=str(w), height=str(h), **{"as":"geometry"})
        return c.get("id")

    def text(self, value, x, y, w, h, size=20, bold=False, color="#172b3a", fill="none", align="center"):
        return self.cell(html.escape(value).replace("\n", "<br>"), x,y,w,h,
                         f"text;strokeColor=none;fillColor={fill};fontColor={color};fontSize={size};fontStyle={int(bold)};align={align};verticalAlign=middle;spacing=4")

    def box(self, value, x,y,w,h,fill="#edf4f8",size=20):
        return self.cell(html.escape(value).replace("\n","<br>"),x,y,w,h,
                         f"rounded=0;strokeColor=#647a8c;fillColor={fill};fontSize={size};spacing=8;align=center;verticalAlign=middle")

    def line(self, x1,y1,x2,y2,color=BLUE,dash=False,arrow=True):
        self.n += 1
        c=ET.SubElement(self.root,"mxCell",id=f"e{self.n}",edge="1",parent="1",
             style=f"html=1;strokeColor={color};strokeWidth=1.6;dashed={int(dash)};endArrow={'open' if arrow else 'none'};endFill=0;")
        g=ET.SubElement(c,"mxGeometry",relative="1",**{"as":"geometry"})
        ET.SubElement(g,"mxPoint",x=str(x1),y=str(y1),**{"as":"sourcePoint"})
        ET.SubElement(g,"mxPoint",x=str(x2),y=str(y2),**{"as":"targetPoint"})

    def relation(self, source,target,label):
        self.n+=1
        c=ET.SubElement(self.root,"mxCell",id=f"e{self.n}",value=html.escape(label),edge="1",parent="1",source=source,target=target,
             style="html=1;edgeStyle=none;endArrow=none;startArrow=none;strokeColor=#175b88;strokeWidth=1.6;fontSize=22;labelBackgroundColor=#ffffff;")
        ET.SubElement(c,"mxGeometry",relative="1",**{"as":"geometry"})

    def save(self):
        ids={c.get("id") for c in self.root}; assert len(ids)==len(self.root)
        for c in self.root:
            if c.get("edge"):
                assert c.find("mxGeometry") is not None
                assert (c.get("source") in ids and c.get("target") in ids) or len(c.find("mxGeometry"))==2
            if c.get("vertex"):
                g=c.find("mxGeometry"); assert float(g.get("width"))>0 and float(g.get("height"))>0
                assert float(g.get("x"))>=0 and float(g.get("y"))>=0
        ET.indent(self.model)
        (OUT/(self.name+".drawio")).write_text(ET.tostring(self.model,encoding="unicode")+"\n")
        return self.name


def sequenza(name,title,actor,handler,command,participants,steps,footer):
    t=Tavola(name,title)
    t.box(actor,20,70,160,70,"#f4f4f4",18)
    t.box(handler,230,70,320,70,size=18)
    t.box(participants[0],670,70,265,70,size=18)
    t.line(180,95,230,95); t.line(550,95,670,95)
    t.text("«ref»" if name=="sequenza-observer" else "«azione»",175,54,100,28,16)
    t.text("«chiamata»" if name=="sequenza-observer" else "«handler»",550,54,120,28,16)
    t.text(command,20,151,915,57,21,bold=True)
    t.text("Continuazione del servizio sopra; dettaglio sincrono sotto." if name=="sequenza-observer" else "Ingresso UI sopra; dettaglio selettivo sotto. ID scalari nei parametri; oggetti nelle note «memoria».",20,213,915,48,18)
    xs=[80+i*(800/(len(participants)-1)) for i in range(len(participants))]
    assert all(b-a>128 for a,b in zip(xs,xs[1:])), "Intestazioni sovrapposte"
    heights=[max(78,26*math.ceil(len(s[0])/88)+14) if len(s)==1 else 78 for s in steps]
    start=383; end=start+sum(heights)
    def participant_label(p):
        return p.replace("Postgres","Postgres\n").replace("BankAccountDao","BankAccount\nDao").replace("DatabaseManager","Database\nManager").replace("VehicleEventPublisher","VehicleEvent\nPublisher").replace("SaleToDealershipService","SaleTo\nDealership\nService").replace("TestDriveCancellationObserver","TestDrive\nCancellation\nObserver").replace("PurchaseService","Purchase\nService").replace("RentalService","Rental\nService")
    for x,p in zip(xs,participants):
        t.box(participant_label(p),x-64,275,128,90,size=20)
        t.line(x,365,x,end+12,GRAY,True,False)
    y=start
    for index,step in enumerate(steps):
        if len(step)==1:
            t.text(step[0],8,y-2,940,heights[index]-8,21,fill="#fff7df",align="left")
        else:
            a,b,label,*kind=step
            alternative=bool(kind and kind[0]=="alt"); returned=bool(kind and kind[0]=="return")
            x1,x2=xs[a],xs[b]
            w=max(440,abs(x2-x1)-8); center=(x1+x2)/2
            x=max(4,min(952-w,center-w/2))
            t.text(label,x,y-6,w,58,21,color=RED if alternative else BLUE,fill="#ffffff")
            t.line(x1,y+55,x2,y+55,RED if alternative else BLUE,returned)
        y+=heights[index]
    t.text(footer,10,end+32,940,150,21,fill="#f0f3f5",align="left")
    t.save()


sequenza("sequenza-vendita-avvio","SQ-01a · Vendita al cliente: avvio e acconto",
 "Cliente: conferma", "ClientDashboardView\nhandlePurchaseAction()", "initiatePurchase(vehicleId, clientId) · UC-03, passi 1–3 / 3.a",
 ["PurchaseService","DatabaseManager","PostgresVehicleDao","PostgresBankAccountDao","PostgresRequestDao","PostgreSQL"],[
 (0,1,"executeInTransactionWithResult(λ)"),
 ("T1 · DatabaseManager apre c1, setAutoCommit(false); esegue λ(c1). Nessuna transazione attraversa i successivi episodi UI.",),
 (0,2,"updateStateAtomically(id,\nSALE, BLOCKED, c1)"),
 (2,5,"«SQL c1» UPDATE condizionale;\nse zero righe: errore → rollback"),
 (0,2,"findById(vehicleId, c1)"),
 ("«sintesi fuori T1» PostgresModelDao.findById(modelId) apre a1; PostgresDiscountDao.findModelDiscount / findVehicleDiscount aprono a2 / a3. Nessun c1 passato.",),
 ("«memoria» PriceCalculatorService.calculateVehiclePrice(model, vehicle, mDisc, vDisc), poi calculatePurchaseDeposit(total): 20%. Il dialogo precedente era un'anteprima.",),
 (0,3,"findByClientIdForUpdate(clientId, c1)"),
 (3,5,"«SQL c1» SELECT conto FOR UPDATE"),
 ("«memoria» account.withdraw(deposit); poi PostgresBankAccountDao.updateBalance(iban, balance, c1) → UPDATE su c1. Conto assente/fondi insufficienti: errore.",),
 (0,4,"save(Request.createNewSaleRequest(\nvehicleId, clientId, deposit, total), c1)"),
 (4,5,"«SQL c1» INSERT request\nSALE, agreed_total, deposit"),
 ("T1 · λ restituisce Request; DatabaseManager: commit(c1), chiusura c1, ritorno alla vista. Importi fissati nella richiesta; nessuna riga sale all'acconto.",)
 ],"Blu: percorso principale; rosso: alternativa che interrompe; tratteggio: ritorno/lifeline. «SQL», «sintesi», «memoria» sono descrizioni, non metodi Java.\nQualsiasi Exception nel corpo/commit: tentativo di rollback c1 e RuntimeException alla vista; nessuna scrittura c1 parziale (salvo guasti di connessione/commit incerto).")

sequenza("sequenza-vendita-esito","SQ-01b · Vendita al cliente: autorizzazione o rifiuto",
 "Venditore: decide", "SalesmanDashboardView\nhandleApproveRequest / handleRejectRequest", "approvePurchase(requestId, salesmanId) oppure rejectPurchase(requestId) · UC-04",
 ["PurchaseService","DatabaseManager","PostgresRequestDao","PostgresBankAccountDao","PostgresVehicleDao","PostgreSQL"],[
 (0,1,"executeInTransaction(λ)"),
 ("T2 · Nuova c2 con autocommit false; una sola decisione per invocazione. Nessun lock derivato dalla selezione UI.",),
 (0,2,"findByIdForUpdate(requestId, c2)"),
 (2,5,"«SQL c2» SELECT request FOR UPDATE"),
 ("«guardia comune» richiesta presente e type = SALE; altrimenti eccezione. Non è verificato qui che salesman fosse ancora nullo.",),
 ("alt [approvazione — principale] «memoria» request.setSalesmanId(salesmanId); setExpDate(oggi.plusMonths(1)). Nessun addebito né registro economico.",),
 (0,2,"update(request, c2)"),
 (2,5,"«SQL c2» UPDATE request"),
 ("else [rifiuto — UC-03 4.a / UC-04 3.a] «memoria» conto riletto sotto lock, account.deposit(request.getDeposit()). Rimborso integrale.",),
 (0,3,"findByClientIdForUpdate(clientId, c2);\nupdateBalance(iban, balance, c2)","alt"),
 (0,4,"updateStateAtomically(vehicleId,\nBLOCKED, SALE, c2)","alt"),
 (0,2,"delete(requestId, c2)","alt"),
 ("end alt · I DAO eseguono SELECT/UPDATE/DELETE su c2. DatabaseManager: commit e chiusura c2 prima del successo UI; su Exception tenta rollback e propaga RuntimeException.",)
 ],"Le due branche sono invocazioni alternative, ciascuna con la propria transazione; non sono due azioni eseguite in sequenza.\nIl lock serializza il singolo ID richiesta; non è un controllo generale dei permessi del chiamante né un divieto applicativo di riapprovazione.")

sequenza("sequenza-vendita-saldo","SQ-01c · Vendita al cliente: saldo e registrazione",
 "Cliente: conferma saldo", "ClientDashboardView\nhandleCompletePurchaseAction()", "completePurchase(requestId) · UC-03, passi 5–6 / 5.a / 6.a",
 ["PurchaseService","PostgresRequestDao","PostgresBankAccountDao","PostgresSaleDao","PostgresVehicleDao","VehicleEventPublisher","PostgreSQL"],[
 ("T3 · DatabaseManager.executeInTransaction(λ): apre c3, setAutoCommit(false), λ(c3). La vista passa l'ID della Request selezionata, non la sua copia come accordo autorevole.",),
 (0,1,"findByIdForUpdate(requestId, c3)"),
 (1,6,"«SQL c3» SELECT request FOR UPDATE"),
 ("«guardie» type SALE, salesmanId ed expDate presenti, !isExpired(). Oggi = scadenza è ammesso. Errore → rollback; la UI anticipa alcuni controlli sulla copia selezionata.",),
 ("«memoria» request.getAgreedTotal(); getRemainingAmount() = totale − acconto persistiti. Nessun ricalcolo di listino al saldo.",),
 (0,2,"findByClientIdForUpdate(clientId, c3)"),
 ("«memoria» account.withdraw(residuo); PostgresBankAccountDao.updateBalance(iban, balance, c3) → UPDATE su c3.",),
 (0,3,"save(Sale.createSaleRecord(\nvehicleId, salesmanId, clientId, totale), c3)"),
 (3,6,"«SQL c3» INSERT sale, type SALE"),
 (0,1,"delete(requestId, c3)"),
 (0,4,"updateStateAtomically(vehicleId,\nBLOCKED, SOLD, c3)"),
 (0,5,"publishCommitment(\nnew VehicleCommitmentEvent(vehicleId), c3)"),
 ("ref SQ-OB · Notifica e cancellazioni test drive sincrone su c3; al ritorno DatabaseManager esegue commit e chiude c3. Soltanto dopo la vista mostra il successo.",)
 ],"Ogni DAO usa c3; SQL descrittivo riassunto dove indicato. Qualsiasi errore, incluso l'Observer, risale al gestore: tentativo di rollback e RuntimeException alla vista.\nIl servizio usa il cliente memorizzato nella richiesta; non riceve l'identità del chiamante per un controllo generale di titolarità.")

sequenza("sequenza-vendita-scadenza","SQ-01d · Vendita al cliente: controllo delle scadenze",
 "Cliente: apre dashboard", "ClientDashboardView\ncheckExpirationsOnStartup()", "checkAndHandleExpiredRequests(clientId) · UC-03 6.b / UC-S02",
 ["PurchaseService","DatabaseManager","PostgresRequestDao","PostgresBankAccountDao","PostgresSaleDao","PostgresVehicleDao","PostgreSQL"],[
 ("«innesco» Il costruttore della vista programma checkExpirationsOnStartup con Platform.runLater. Non esiste qui un timer periodico né un intervento automatico alla data di scadenza.",),
 (0,2,"findByClientId(clientId)"),
 (2,6,"«SQL c0 autonoma» SELECT richieste;\nconnessione poi chiusa"),
 ("loop [per ogni candidato, anche se poi non scaduto] T4i: nuova c, executeInTransactionWithResult(λ), autocommit false. Nessuna transazione comune all'intera lista.",),
 (0,2,"findByIdForUpdate(candidate.getId(), c)"),
 ("alt [assente / non SALE / altro cliente / non approvata / non scaduta] λ restituisce null; commit senza effetti e prossimo candidato.",),
 ("else [approvata e scaduta: expDate < oggi] «memoria» penale = 20% acconto, HALF_UP ai centesimi; rimborso = acconto − penale.",),
 (0,3,"findByClientIdForUpdate(clientId, c)"),
 ("«memoria» account.deposit(rimborso); PostgresBankAccountDao.updateBalance(iban, balance, c) → UPDATE su c.",),
 (0,4,"save(Sale.createPenaltyRecord(\nclientId, penale), c)"),
 (0,2,"delete(request.getId(), c)"),
 (0,5,"updateStateAtomically(vehicleId,\nBLOCKED, SALE, c)"),
 ("end alt · DatabaseManager: commit/chiusura c; aggiunge il messaggio solo dopo il ritorno. Fine loop: lista alla vista. Errore: rollback del solo candidato corrente e uscita dal metodo.",)
 ],"La vista mostra i messaggi restituiti e aggiorna il saldo. Su Throwable scrive invece un avviso su stderr: non presenta lo stesso dialogo d'errore delle azioni manuali.\nCandidati già trattati in transazioni precedenti restano confermati anche se un candidato successivo fallisce. Nessuna notifica Observer in questo episodio.")

sequenza("sequenza-noleggio","SQ-02 · Noleggio: prenotazione e addebito",
 "Cliente: conferma date", "ClientDashboardView\nhandleRentAction(), callback del dialogo", "bookRental(vehicleId, clientId, start, end) · UC-02, passi 3–6 / 3.a–5.a",
 ["RentalService","PostgresVehicleDao","PostgresUserDao","PostgresRentDao","PostgresBankAccountDao","VehicleEventPublisher","PostgreSQL"],[
 ("Prima di T5: rifiuta start < oggi o end < start. T5: DatabaseManager.executeInTransactionWithResult(λ), nuova c5, autocommit false.",),
 (0,1,"findByIdForUpdate(vehicleId, c5)"),
 (1,6,"«SQL c5» SELECT vehicle FOR UPDATE;\nservizio richiede stato RENT"),
 (0,2,"lockClient(clientId, c5)"),
 (2,6,"«SQL c5» SELECT client\nFOR NO KEY UPDATE"),
 (0,3,"isVehicleAvailableForRent(\nvehicleId, start, end, c5)"),
 ("«SQL c5» nessun rent con start ≤ fine richiesta ed end ≥ inizio richiesta. Se occupato: errore. EXCLUDE protegge inoltre gli intervalli inclusivi sullo stesso veicolo.",),
 ("«sintesi su c5» calculateRentalCost(vehicle, start, end, c5): PostgresModelDao.findById e PostgresDiscountDao.findModelDiscount / findVehicleDiscount con c5; PriceCalculatorService ricalcola costo del periodo incluso.",),
 (0,4,"findByClientIdForUpdate(clientId, c5)"),
 ("«memoria» account.withdraw(totalCost); PostgresBankAccountDao.updateBalance(iban, balance, c5) → UPDATE su c5. Addebito integrale, non un acconto.",),
 (0,3,"save(Rent.create(null, vehicleId,\nclientId, totalCost, start, end), c5)"),
 (0,5,"publishCommitment(\nnew VehicleCommitmentEvent(vehicleId), c5)"),
 ("ref SQ-OB · Cancella tutti i test drive futuri del veicolo su c5, anche fuori periodo. Ritorno → commit/chiusura c5 → Rent alla vista; stato del veicolo resta RENT.",)
 ],"Errore di dati, stato, conto, salvataggio o Observer: tentativo di rollback dell'intera T5. RentalService riespone la causa IllegalArgumentException/IllegalStateException se avvolta dal gestore, altrimenti rilancia il failure.\nIl preventivo calculateRentalCost(vehicleId, start, end) era una transazione distinta senza prenotazione né accordo congelato.")

sequenza("sequenza-cessione-proposta","SQ-03a · Acquisto dal cliente: proposta",
 "Cliente: invia dati", "ClientDashboardView\nbuildSellMyCarTab(), handler btnSubmit", "submitSaleProposal(clientId, modelId, vin, licencePlate, km, year) · UC-05, passi 1–4 / 3.a",
 ["SaleToDealershipService","DatabaseManager","PostgresVehicleDao","PostgresSaleDao","PostgresRequestDao","PostgreSQL"],[
 (0,1,"executeInTransactionWithResult(λ)"),
 ("T6 · Nuova c6, autocommit false; nessun accredito nella proposta. L'eventuale preventivo UI calculateEstimate era una chiamata separata e facoltativa.",),
 (0,2,"findByVinForUpdate(vin, c6)"),
 (2,5,"«SQL c6» SELECT per UPPER(vin)\nFOR UPDATE (solo righe esistenti)"),
 ("alt [VIN noto] richiede SOLD; findLatestSaleForVehicle e hasOpenRequestForVehicle sotto c6. La titolarità dipende dall'ultima SALE (date DESC, id DESC), non da qualsiasi storico.",),
 (0,3,"findLatestSaleForVehicle(vehicleId, c6)"),
 (0,4,"hasOpenRequestForVehicle(vehicleId, c6)"),
 ("«guardie» ultima SALE al proponente, nessuna richiesta, modelId/anno coincidenti. «memoria» aggiorna targa, km, WAITING; PostgresVehicleDao.update(vehicle, c6).",),
 ("else [VIN nuovo] PostgresModelDao.findById(modelId) apre c0 autonoma per la base; Vehicle.create(..., WAITING); PostgresVehicleDao.save(newVehicle, c6). Non c'è un lock di un VIN assente.",),
 ("end alt · «sintesi fuori T6» calculateEstimate(modelId, plate, km, year) rilegge modello e sconto modello su connessioni autonome; valida anno e usa PriceCalculatorService.estimateVehicleValue.",),
 (0,4,"save(Request.createNewPurchaseRequest(\nvehicle.getId(), clientId, estimatedPrice), c6)"),
 (4,5,"«SQL c6» INSERT request PURCHASE,\ndeposit = stima; agreed_total = NULL"),
 ("λ restituisce Request; DatabaseManager commit/chiusura c6 → vista. VIN e request.vehicle UNIQUE possono rifiutare inserimenti concorrenti: non si promette uno snapshot del listino.",)
 ],"Errore: tentativo di rollback c6, comprese creazione/aggiornamento del veicolo. submitSaleProposal riespone IllegalArgumentException/IllegalStateException avvolte, altrimenti rilancia il failure.\nLa stima persistita fissa l'importo dell'eventuale esito positivo; non è ancora una registrazione economica.")

sequenza("sequenza-cessione-esito","SQ-03b · Acquisto dal cliente: esito",
 "Venditore: decide", "SalesmanDashboardView\nhandleApproveRequest / handleRejectRequest", "approveSaleProposal(requestId, salesmanId) oppure rejectSaleProposal(requestId) · UC-05, passi 5–6 / 5.a–6.a",
 ["SaleToDealershipService","PostgresRequestDao","PostgresVehicleDao","PostgresBankAccountDao","PostgresSaleDao","PostgreSQL"],[
 ("T7 · DatabaseManager.executeInTransaction(λ): nuova c7 con autocommit false. Approva e rifiuta sono invocazioni alternative; ciascuna rilegge i dati.",),
 (0,1,"findByIdForUpdate(requestId, c7)"),
 (0,2,"findByIdForUpdate(request.vehicleId, c7)"),
 ("«sintesi» lockWaitingPurchaseRequest richiede request.type PURCHASE e vehicle.state WAITING. SELECT FOR UPDATE su richiesta e veicolo. Errori → rollback.",),
 ("alt [accettazione — principale] salesmanId obbligatorio; payout = request.getDeposit(), senza ricalcolo della stima.",),
 (0,3,"findByClientIdForUpdate(clientId, c7)"),
 ("«memoria» account.deposit(payout); PostgresBankAccountDao.updateBalance(iban, balance, c7) → UPDATE su c7.",),
 (0,4,"save(Sale.createPurchaseRecord(\nvehicleId, salesmanId, clientId, payout), c7)"),
 (0,2,"updateStateAtomically(vehicleId,\nWAITING, SALE, c7)"),
 (0,1,"delete(requestId, c7)"),
 ("else [rifiuto] PostgresRequestDao.delete(requestId, c7), poi PostgresVehicleDao.hasAssociatedOperations(vehicleId, c7): controlla test_drive, rent e sale.",),
 ("alt [con storico] updateStateAtomically(vehicleId, WAITING, SOLD, c7); else [senza storico] PostgresVehicleDao.delete(vehicleId, c7). Nessun movimento sul conto né nuova riga sale.",),
 ("end alt · I DAO eseguono SQL su c7. DatabaseManager commit e chiusura prima del successo UI; su Exception tenta rollback e propaga RuntimeException alla vista. Nessun Observer in T7.",)
 ],"Il registro type PURCHASE conserva un esborso della concessionaria con importo non negativo; l'accredito al cliente avviene solo qui, nella stessa transazione.\nLimite attuale: Request ammette stima zero ma BankAccount.deposit richiede > 0; in quel caso l'accettazione fallisce e torna in rollback (difetto separato, non corretto).")

sequenza("sequenza-observer","SQ-OB · Collaborazione comune: cancellazioni sincrone",
 "Saldo / noleggio", "ref SQ-01c / SQ-02\nchiamata del servizio, non gesto UI", "publishCommitment(new VehicleCommitmentEvent(vehicleId), c) · continuazione della T3 o T5",
 ["VehicleEventPublisher","TestDriveCancellationObserver","PostgresTestDriveDao","PostgreSQL"],[
 ("La c è quella del chiamante, già in transazione. L'evento contiene solo vehicleId, non l'oggetto Vehicle né una Connection propria. AppContext registra questo observer nel publisher.",),
 (0,1,"onVehicleCommitted(event, c)"),
 (1,2,"deleteFutureByVehicleId(event.vehicleId(), c)"),
 (2,3,"«SQL c» DELETE FROM test_drive\nWHERE vehicle = ? AND date > CURRENT_TIMESTAMP"),
 (3,2,"conteggio cancellazioni", "return"),
 (2,1,"int (non usato dall'observer)","return"),
 (1,0,"ritorno sincrono void","return"),
 ("Ritorno al servizio → ritorno della callback → DatabaseManager.commit(c). L'Observer non esegue commit e non apre una nuova transazione. Conserva test drive già trascorsi; elimina tutti i futuri, anche fuori dal noleggio.",),
 ("alt [SQLException nel DELETE] PostgresTestDriveDao la avvolge in RuntimeException; risale attraverso observer e publisher senza recupero locale; DatabaseManager tenta rollback della c del saldo/noleggio.",)
 ],"Notifica sincrona prima del commit: nessuna freccia asincrona, coda, outbox o garanzia distribuita. Il publisher scorre gli osservatori registrati nello stesso thread.\nSe il rollback fallisce, DatabaseManager conserva la SQLException come soppressa sulla causa. Guasti di apertura/chiusura e commit incerto non diventano una garanzia di recupero totale.")


def er_tavola(name,title,rows,foot):
    t=Tavola(name,title)
    t.text("Ogni riga è una relazione ER. Le entità ripetute sono le stesse, non copie persistenti.",20,65,920,50,20)
    t.text("Il numero vicino a un'entità indica quante sue istanze corrispondono a una dell'altra.",20,117,920,50,20)
    for i,(left,right,relation,lc,rc) in enumerate(rows):
        y=195+i*123
        a=t.box(left,15,y,270,88,size=21)
        r=t.cell(html.escape(relation),390,y+9,180,70,"rhombus;strokeColor=#647a8c;fillColor=#ffffff;fontSize=18;")
        b=t.box(right,675,y,270,88,size=21)
        t.relation(a,r,lc);t.relation(r,b,rc)
    t.text(foot,15,205+len(rows)*123,930,140,20,fill="#f0f3f5",align="left")
    t.save()


def er_catalogo():
    er_tavola("database-er-catalogo","ER-01 · Catalogo e veicoli nelle operazioni",[
        ("brand\nPK name","model\nPK id · CK name","classifica","1","0..*"),
        ("model\nPK id · CK name","model_discount\nPK/FK model","sconto\nmodello","1","0..1"),
        ("model\nPK id · CK name","vehicle\nPK id · CK vin","identifica il\nmodello","1","0..*"),
        ("vehicle\nPK id · CK vin","vehicle_discount\nPK/FK vehicle","sconto\nveicolo","1","0..1"),
        ("vehicle\nPK id · CK vin","request [ER-02]\nPK id · CK/FK vehicle","richiesta\naperta","1","0..1"),
        ("vehicle\nPK id · CK vin","test_drive [ER-02]\nPK id · CK (vehicle,date)","giro di\nprova","1","0..*"),
        ("vehicle\nPK id · CK vin","rent [ER-02]\nPK id","noleggio","1","0..*"),
        ("vehicle\nPK id · CK vin","sale [ER-02]\nPK id · vehicle nullable","riferimento\nnel registro","0..1","0..*")
    ],"CK: chiave candidata alternativa alla PK. vehicle.licence_plate è UQ nullable, non CK.\n[ER-02] riprende le medesime operazioni per le relazioni con le identità.\nCardinalità da FK/NOT NULL/UNIQUE; azioni di cancellazione e vincoli di valore in REL-01–03.")


def er_identita():
    er_tavola("database-er-identita","ER-02 · Identità, conto e attribuzioni",[
        ("client\nPK id · CK email","bank_account\nPK iban · CK/FK client","conto\ndimostrativo","1","0..1"),
        ("client\nPK id · CK email","request [ER-01]\nPK id","richiedente","1","0..*"),
        ("client\nPK id · CK email","sale [ER-01]\nPK id","cliente del\nregistro","1","0..*"),
        ("client\nPK id · CK email","test_drive [ER-01]\nPK id","prenotante","1","0..*"),
        ("client\nPK id · CK email","rent [ER-01]\nPK id","noleggiante","1","0..*"),
        ("salesman\nPK id · CK email","request [ER-01]\nPK id · salesman nullable","autorizza","0..1","0..*"),
        ("salesman\nPK id · CK email","sale [ER-01]\nPK id · salesman nullable","attribuzione\nnel registro","0..1","0..*")
    ],"manager: PK id, CK email; nessuna FK applicativa (entità isolata).\nuser è una vista UNION ALL, non una tabella padre. user_id_seq non è una PK globale.\n13 tabelle complessive in ER-01/02. Il conto è opzionale nello schema; il servizio di registrazione lo crea insieme all'identità.")


def relazionale(name,title,tables,notes):
    t=Tavola(name,title)
    t.text("PK = primaria; CK = candidata alternativa; FK → riferimento; ? = nullable; UQ = UNIQUE. Tutti gli altri attributi sono NOT NULL.",20,65,920,68,20)
    y=155
    for table,lines in tables:
        h=50+len(lines)*31
        t.box(table,20,y,920,45,"#dcebf5",23)
        t.text("\n".join(lines),20,y+45,920,h-45,21,fill="#f7f9fa",align="left")
        y+=h+24
    t.text(notes,20,y+5,920,140,20,fill="#fff7df",align="left")
    t.save()


er_catalogo(); er_identita()
relazionale("database-relazionale-catalogo","REL-01 · Relazioni del catalogo",[
 ("brand",["name VARCHAR(50) PK"]),
 ("model",["id BIGINT PK identity · brand VARCHAR(50) FK → brand.name",
             "name VARCHAR(50) CK · release_price DECIMAL(10,2) ≥ 0",
             "release_year SMALLINT ≥ 1900"]),
 ("model_discount",["model BIGINT PK/FK → model.id · value SMALLINT fra 0 e 50"]),
 ("vehicle",["id BIGINT PK identity · model BIGINT FK → model.id · vin VARCHAR(17) CK",
               "licence_plate VARCHAR(10) ? UQ · kilometers INT ≥ 0 · price DECIMAL(10,2) ≥ 0",
               "price_mode price_mode DEFAULT AUTOMATIC · year SMALLINT ≥ 1900",
               "state vehicle_state · CHECK: licence_plate IS NOT NULL OR kilometers = 0"]),
 ("vehicle_discount",["vehicle BIGINT PK/FK → vehicle.id · value SMALLINT fra 0 e 50"])
],"DELETE: model.brand e vehicle.model RESTRICT; model_discount.model e vehicle_discount.vehicle CASCADE.\nUPDATE: solo model.brand CASCADE; per tutte le altre FK dello schema vale NO ACTION predefinito.\nTrigger bidirezionali sui cambi anno/modello: vehicle.year ≥ model.release_year. price è una base commerciale, non il prezzo finale della formula.")
relazionale("database-relazionale-identita","REL-02 · Relazioni delle identità e vista user",[
 ("client",["id BIGINT PK DEFAULT nextval('user_id_seq')",
            "name, surname VARCHAR(50) · email VARCHAR(50) CK · hash_password VARCHAR(255)",
            "CHECK email LIKE '%_@_%._%' (forma minima, non verifica dell'indirizzo)"]),
 ("salesman",["id BIGINT PK DEFAULT nextval('user_id_seq')",
              "name, surname VARCHAR(50) · email VARCHAR(50) CK · hash_password VARCHAR(255)",
              "CHECK email LIKE '%_@_%._%'"]),
 ("manager",["id BIGINT PK DEFAULT nextval('user_id_seq')",
             "name, surname VARCHAR(50) · email VARCHAR(50) CK · hash_password VARCHAR(255)",
             "CHECK email LIKE '%_@_%._%'"]),
 ("bank_account",["iban VARCHAR(27) PK · client BIGINT CK/FK → client.id",
                  "balance DECIMAL(10,2) DEFAULT 0.00 · CHECK balance ≥ 0 · DELETE CASCADE"]),
 ("user — VISTA, non tabella",["UNION ALL di client / salesman / manager: id, name, surname, email, hash_password",
                           "+ role costante CLIENT / SALESMAN / MANAGER, scelto dal ramo della vista"])
],"Trigger BEFORE INSERT / UPDATE OF email cercano l'email esatta nelle altre tabelle, escludendo lo stesso id. Non c'è UNIQUE globale né vincolo globale sugli id.\nLa sequenza comune alimenta gli inserimenti ordinari; i trigger non sono una prova di esclusione fra scritture concorrenti. I ruoli della vista sono ruoli applicativi, non PostgreSQL.")
relazionale("database-relazionale-operazioni","REL-03 · Richieste, prenotazioni e registro",[
 ("request",["id BIGINT PK identity · vehicle BIGINT CK/FK → vehicle.id",
             "client BIGINT FK → client.id · salesman BIGINT ? FK → salesman.id",
             "deposit DECIMAL(10,2) · agreed_total DECIMAL(10,2) ? · exp_date DATE ?",
             "type request_type · CHECK SALE: 0 ≤ deposit ≤ agreed_total, totale non nullo",
             "CHECK PURCHASE: deposit ≥ 0 e agreed_total IS NULL · tutte le FK DELETE RESTRICT"]),
 ("test_drive",["id BIGINT PK identity · vehicle BIGINT FK → vehicle.id · client BIGINT FK → client.id",
                "date TIMESTAMP · CK (vehicle, date) · CHECK ora fra 09:00 e 16:00 inclusi",
                "FK DELETE RESTRICT · nessun CHECK SQL su futuro, minuti o massimo tre attivi"]),
 ("rent",["id BIGINT PK identity · vehicle BIGINT FK → vehicle.id · client BIGINT FK → client.id",
           "amount DECIMAL(10,2) ≥ 0 · start DATE · end DATE · CHECK end ≥ start",
           "EXCLUDE USING gist (vehicle WITH =, daterange(start, end, '[]') WITH &&)",
           "FK DELETE RESTRICT · estremi inclusivi; btree_gist supporta l'esclusione per veicolo"]),
 ("sale",["id BIGINT PK identity · vehicle BIGINT ? FK → vehicle.id DELETE SET NULL",
           "salesman BIGINT ? FK → salesman.id DELETE RESTRICT",
           "client BIGINT FK → client.id DELETE RESTRICT · amount DECIMAL(10,2) ≥ 0",
           "date TIMESTAMP · type sale_type · nessun CHECK SQL sui riferimenti per tipo"])
],"identity = GENERATED BY DEFAULT AS IDENTITY. Tutte le FK non citate come CASCADE/SET NULL sono RESTRICT in DELETE; in UPDATE sono NO ACTION.\nLo schema non collega sale a request: l'esito registra e poi elimina la richiesta. Nessun CHECK SQL impone il 20%, un mese, il futuro delle date o la coerenza fra stato e operazioni.")

names=sorted(p.stem for p in OUT.glob("*.drawio") if p.stem.startswith(("sequenza-","database-")))
assert len(names)==13, names
(HERE/"tavole.json").write_text(json.dumps(names,indent=2)+"\n")
print("13 tavole manuali create; XML, ID, terminali e geometrie validi.")
