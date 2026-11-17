"""Disposizioni progettuali manuali del ticket 03, non reverse engineering UML."""
from pathlib import Path
import html
import xml.etree.ElementTree as ET

OUT = Path(__file__).resolve().parents[2] / "diagrammi"
BLUE, GREEN, GOLD, GREY = "#dae8fc", "#d5e8d4", "#fff2cc", "#f5f5f5"


class Tavola:
    def __init__(self, name, title, subtitle, width=960):
        self.name, self.width = name, width
        self.xml = ET.Element("mxfile", host="draw.io", version="32.6.10")
        diagram = ET.SubElement(self.xml, "diagram", id=name, name=title)
        model = ET.SubElement(diagram, "mxGraphModel", page="0", pageScale="1", adaptiveColors="auto")
        self.root = ET.SubElement(model, "root")
        ET.SubElement(self.root, "mxCell", id="0")
        ET.SubElement(self.root, "mxCell", id="1", parent="0")
        self.n = 0
        self.text("title", title, 20, 12, width-40, 38, 26, True)
        self.text("subtitle", subtitle, 20, 55, width-40, 48, 18)

    def vertex(self, id, value, x, y, w, h, style):
        c = ET.SubElement(self.root, "mxCell", id=id, value=value, style=style+"html=1;", vertex="1", parent="1")
        ET.SubElement(c, "mxGeometry", x=str(x), y=str(y), width=str(w), height=str(h), attrib={"as": "geometry"})
        return id

    def text(self, id, text, x, y, w, h, size=18, bold=False):
        return self.vertex(id, html.escape(text).replace("\n", "<br>"), x, y, w, h,
                            f"text;whiteSpace=wrap;align=left;verticalAlign=middle;fontFamily=Arial;fontSize={size};fontStyle={int(bold)};spacing=4;")

    def box(self, id, title, members, x, y, w=280, h=150, kind="", fill=BLUE):
        size = min(21, (w-20)/max(max(map(len,title.splitlines())), len(kind)+4)*1.7)
        header = (f"&laquo;{html.escape(kind)}&raquo;<br>" if kind else "") + html.escape(title).replace("\n","<br>")
        value = f'<div style="text-align:center;font-size:{size:.1f}px;white-space:nowrap"><b>{header}</b></div>'
        if members:
            body_size = min(19, (w-24)/max(map(len,members))/.51)
            value += f'<hr><div style="text-align:left;font-size:{body_size:.1f}px;white-space:nowrap">' + "<br>".join(html.escape(m) for m in members) + "</div>"
        return self.vertex(id, value, x, y, w, h,
                            f"rounded=0;whiteSpace=wrap;fillColor={fill};strokeColor=#475569;spacing=9;fontFamily=Arial;verticalAlign=top;")

    def edge(self, a, b, label="", kind="dep", straight=False):
        self.n += 1
        styles = {
            "dep": "dashed=1;endArrow=open;endFill=0;",
            "assoc": "endArrow=open;endFill=0;",
            "extends": "endArrow=block;endFill=0;",
            "implements": "dashed=1;endArrow=block;endFill=0;",
        }
        route = "none" if straight else "orthogonalEdgeStyle"
        c = ET.SubElement(self.root, "mxCell", id=f"edge-{self.n}", value=label,
                          style=f"edgeStyle={route};rounded=0;html=1;fontFamily=Arial;fontSize=18;labelBackgroundColor=#ffffff;strokeColor=#334155;"+styles[kind],
                          edge="1", source=a, target=b, parent="1")
        ET.SubElement(c, "mxGeometry", relative="1", attrib={"as":"geometry"})

    def note(self, text, y, h=90):
        self.box("note", "Lettura della tavola", text.split("\n"), 20, y, self.width-40, h, fill=GOLD)

    def save(self):
        cells = self.root.findall("mxCell")
        ids = {c.get("id") for c in cells}
        assert len(ids) == len(cells)
        assert not any(c.get("id")==c.get("parent") for c in cells)
        vertices = [c for c in cells if c.get("vertex")]
        for i,a in enumerate(vertices):
            ga=a.find("mxGeometry")
            ax,ay,aw,ah=(float(ga.get(k)) for k in ("x","y","width","height"))
            for b in vertices[i+1:]:
                gb=b.find("mxGeometry")
                bx,by,bw,bh=(float(gb.get(k)) for k in ("x","y","width","height"))
                assert ax+aw<=bx or bx+bw<=ax or ay+ah<=by or by+bh<=ay, (self.name,a.get("id"),b.get("id"))
        for c in cells:
            if c.get("edge"):
                assert c.get("source") in ids and c.get("target") in ids
                assert c.find("mxGeometry") is not None
        ET.indent(self.xml)
        OUT.joinpath(self.name+".drawio").write_bytes(ET.tostring(self.xml, encoding="utf-8", xml_declaration=True))


def packages():
    t=Tavola("package-architettura", "PKG — Package e dipendenze", "Prefisso comune: it.unifi.drivehub. Freccia tratteggiata: dipendenza del codice.", 1040)
    t.box("root", "it.unifi.drivehub", ["App: avvio e viste", "AppContext: composizione"], 360,120,320,125,fill=GOLD)
    t.box("view", "presentation.view", ["Viste JavaFX e dialoghi", "Input, tabelle e caricamenti"],20,320,300,120)
    t.box("svc", "service", ["Casi d'uso, prezzi, transazioni", "Letture e politica di cancellazione"],360,450,320,125,fill=GREEN)
    t.box("event", "event", ["Contratto Observer e publisher", "Evento con vehicleId"],720,450,300,125,fill=GOLD)
    t.box("sec", "security", ["Contratto hash/verify", "PBKDF2 e sale casuale"],20,650,300,120,fill=GOLD)
    t.box("dao", "dao", ["Contratti per risorsa", "DatabaseManager e JDBC"],360,760,320,120,fill=GREY)
    t.box("pg", "dao.postgres", ["Mapping, SQL e lock", "Dieci realizzazioni dei DAO"],720,760,300,120,fill=GREY)
    for a,b in [("root","view"),("root","svc"),("view","svc"),("svc","event"),("svc","sec"),("svc","dao"),("pg","dao")]: t.edge(a,b)
    # Le dipendenze trasversali sono esplicitate come righe per evitare archi sovrapposti.
    t.text("domains", "Dominio — quattro package distinti, senza dipendenze fra loro",20,920,1000,40,22,True)
    for id,title,body,x,y in [
        ("user","domain.user",["Ruoli, identità e conto", "Contratto di navigazione"],20,980),
        ("vehicle","domain.vehicle",["Marca, modello, veicolo", "Stato e base del prezzo"],530,980),
        ("discount","domain.discount",["Sconti modello / veicolo", "Percentuale intera 0–50"],20,1130),
        ("operation","domain.operation",["Richieste, registro, noleggi", "Test drive; riferimenti per ID"],530,1130)]: t.box(id,title,body,x,y,490,120,fill=GREEN)
    t.note("Dipendenze trasversali effettive (→ indica il fornitore):\nit.unifi.drivehub → dao.postgres, dao, event, security, domain.user, oltre agli archi sopra.\npresentation.view → it.unifi.drivehub (overload globali) e i quattro package domain.\nservice, dao e dao.postgres → ciascuno dei quattro package domain.\nNessuna dipendenza del dominio da servizi/DAO/JavaFX; event usa java.sql.Connection.",1290,205)
    t.save()


def startup():
    t=Tavola("classi-avvio", "CL-01 — Avvio e composizione", "Package it.unifi.drivehub. Tipi grigi: contesto esterno alla vista.")
    t.box("fx","Application",[],20,130,280,75,kind="JavaFX",fill=GREY)
    t.box("app","App",["− ctx: AppContext", "+ start(Stage)", "− showDashboardForUser(User)"],20,320,390,170)
    t.box("ctx","AppContext",["− instance: AppContext {static}","− databaseManager: DatabaseManager", "+ AppContext(DatabaseManager)","+ getInstance(): AppContext {static}","+ getPurchaseService(): PurchaseService"],490,320,450,230)
    t.edge("app","fx",kind="extends"); t.edge("app","ctx","ctx [1]", "assoc")
    t.box("nav","UserDashboardNavigator",["showClientDashboard(Client)","showSalesmanDashboard(Salesman)","showManagerDashboard(Manager)"],20,660,390,170,kind="interface · domain.user",fill=GREY)
    t.box("wiring","Collaboratori costruiti",["Postgres*Dao → contratti DAO", "Pbkdf2PasswordHasher → PasswordHasher", "VehicleEventPublisher + observer", "Otto servizi applicativi / di calcolo"],490,660,450,190,kind="nota",fill=GOLD)
    t.edge("app","nav","crea implementazione anonima")
    t.edge("ctx","wiring","costruisce e collega")
    t.note("App estende Application, non implementa UserDashboardNavigator.\nL'implementazione del navigator è anonima in showDashboardForUser.\nctx è valorizzato da start; [1] descrive l'applicazione avviata. Nessuna composizione UML.",920,155)
    t.save()


def users():
    t=Tavola("classi-utenti", "CL-02 — Identità e ruoli", "Package it.unifi.drivehub.domain.user. Il conto è un riferimento opzionale in memoria.")
    t.box("user","User",["+ openDashboard(navigator)","+ updateEmail(newEmail)","+ updateHashPassword(hash)"],20,120,350,155,kind="interface")
    t.box("nav","UserDashboardNavigator",["+ showClientDashboard(Client)","+ showSalesmanDashboard(Salesman)","+ showManagerDashboard(Manager)"],540,120,400,155,kind="interface")
    t.edge("user","nav","parametro")
    t.box("abstract","AbstractUser",["# id: Long", "# email: String", "# hashPassword: String", "# validateAttributes(...)"],340,350,280,200,kind="abstract")
    t.edge("abstract","user",kind="implements")
    t.box("client","Client",["+ createWithoutAccount(...)","+ openDashboard(navigator)"],20,750,280,140)
    t.box("sales","Salesman",["+ openDashboard(navigator)"],340,750,280,140)
    t.box("manager","Manager",["+ openDashboard(navigator)"],660,750,280,140)
    for a in ("client","sales","manager"):t.edge(a,"abstract",kind="extends",straight=True)
    t.box("account","BankAccount",["− clientId: Long {ID}", "− balance: BigDecimal", "+ deposit(amount)","+ withdraw(amount)"],20,1030,400,200)
    t.edge("client","account","bankAccount [0..1]","assoc")
    t.note("Triangolo vuoto: generalizzazione; tratteggio + triangolo: realizzazione.\nFreccia continua: navigazione del riferimento; cardinalità solo al bersaglio.\nBankAccount conserva clientId, non un riferimento Client. Nessun rombo di composizione.\nUserDashboardNavigator riceve i tre sottotipi e viene invocato da openDashboard.",1300,180)
    t.save()


def vehicles():
    t=Tavola("classi-catalogo", "CL-03 — Catalogo e scelta della base", "Package it.unifi.drivehub.domain.vehicle. I collegamenti per chiave restano attributi.")
    t.box("brand","Brand",["− name: String", "+ create(name)"],580,120,360,130)
    t.box("model","Model",["− id: Long", "− brand: String {chiave}","− releasePrice: BigDecimal", "− releaseYear: int", "+ create(...)"],20,120,430,220)
    t.box("v","Vehicle",["− id: Long", "− modelId: Long {ID}","− vin: String", "− licencePlate: String [0..1]", "− kilometers: int", "− price: BigDecimal {base}", "+ getSelectedBasePrice(Model)", "+ setPrice(amount)"],20,430,430,320)
    t.box("state","VehicleState",["SALE, RENT, BLOCKED", "WAITING, SOLD"],580,450,360,120,kind="enumeration")
    t.box("price","PriceMode",["AUTOMATIC", "MANUAL"],580,660,360,120,kind="enumeration")
    t.edge("v","state","state [1]","assoc");t.edge("v","price","priceMode [1]","assoc")
    t.edge("v","model","getSelectedBasePrice")
    t.note("modelId non è un oggetto Model; Model.brand è una stringa, non un oggetto Brand.\nLa dipendenza Vehicle → Model deriva dal parametro del metodo di scelta della base.\nVincoli locali: km ≥ 0; senza targa km = 0; base ≥ 0; anno fra 1900 e oggi.\nMANUAL sostituisce la base nella formula; non fissa il totale finale (ADR 0001).",870,180)
    t.save()


def discounts():
    t=Tavola("classi-sconti", "CL-04 — Sconti selettivi", "Package it.unifi.drivehub.domain.discount.")
    t.box("d","Discount",["# value: int {0..50}","# Discount(value)"],300,150,360,140,kind="abstract")
    t.box("m","ModelDiscount",["− modelId: Long {ID}","+ create(modelId, value)"],20,460,420,140)
    t.box("v","VehicleDiscount",["− vehicleId: Long {ID}","+ create(vehicleId, value)"],520,460,420,140)
    t.edge("m","d",kind="extends");t.edge("v","d",kind="extends")
    t.note("Discount centralizza il limite della percentuale; le factory richiedono l'ID non nullo.\nNon esistono campi Model o Vehicle negli sconti: nessuna associazione UML a tali entità.\nLa scelta del massimo fra sconto modello e veicolo appartiene al calcolatore dei prezzi.",720,155)
    t.save()


def operations():
    t=Tavola("classi-operazioni", "CL-05 — Richieste, prenotazioni e registro", "Package it.unifi.drivehub.domain.operation. I nomi SALE/PURCHASE seguono la concessionaria.")
    t.box("req","Request",["− vehicleId, clientId: Long {ID}","− salesmanId: Long [0..1] {ID}","− deposit: BigDecimal", "− agreedTotal: BigDecimal [0..1]", "− expDate: LocalDate [0..1]", "+ createNewSaleRequest(...)","+ createNewPurchaseRequest(...)","+ getRemainingAmount()"],20,120,450,340)
    t.box("rt","RequestType",["SALE: vendita al cliente", "PURCHASE: acquisto dal cliente"],550,170,390,150,kind="enumeration")
    t.edge("req","rt","type [1]","assoc")
    t.box("sale","Sale",["− vehicleId, salesmanId: Long [0..1] {ID}","− clientId: Long {ID}","− amount: BigDecimal {≥ 0}","− date: LocalDateTime", "+ createSaleRecord(...)","+ createPurchaseRecord(...)","+ createPenaltyRecord(...)"],20,550,450,300)
    t.box("st","SaleType",["SALE, PURCHASE, PENALTY"],550,570,390,110,kind="enumeration")
    t.edge("sale","st","type [1]","assoc")
    t.box("rent","Rent",["− vehicleId, clientId: Long {ID}","− amount: BigDecimal {≥ 0}","− startDate, endDate: LocalDate", "+ overlapsWith(start, end)"],20,950,450,210)
    t.box("td","TestDrive",["− vehicleId, clientId: Long {ID}","− date: LocalDateTime", "+ isFuture()"],550,950,390,170)
    t.note("Nessun riferimento a oggetti Client, Vehicle o Salesman: solo identificativi.\nRequest.SALE: 0 ≤ deposit ≤ agreedTotal; saldo = totale − acconto.\nRequest.PURCHASE: deposit è la stima; agreedTotal è nullo. Sale è il registro economico.\nIl 20% è una regola del servizio; gli stati commerciali appartengono a Vehicle.",1240,180)
    t.save()


def services():
    t=Tavola("classi-servizi", "CL-06 — Servizi e collaborazioni", "Package it.unifi.drivehub.service. I tipi esterni restano attributi o riferimenti qualificati.",1040)
    t.box("auth","AuthService",["− userDao: UserDao", "− bankAccountDao: BankAccountDao", "− hasher: PasswordHasher", "+ login(email, password)","+ registerClient(...)"],20,120,480,200)
    t.box("td","TestDriveService",["− testDriveDao: TestDriveDao", "− vehicleDao: VehicleDao", "− userDao: UserDao", "+ bookTestDrive(...)","+ cancelTestDrive(...)"],540,120,480,200)
    t.box("p","PurchaseService",["− priceCalculator: PriceCalculatorService", "+ initiatePurchase(...)","+ approvePurchase(...)","+ completePurchase(...)"],20,370,480,180)
    t.box("s","SaleToDealershipService",["− priceCalculator: PriceCalculatorService", "+ submitSaleProposal(...)","+ approveSaleProposal(...)"],540,370,480,180)
    t.box("r","RentalService",["− priceCalculator: PriceCalculatorService", "+ bookRental(...)","+ changeVehicleRentalState(...)"],20,600,480,170)
    t.box("calc","PriceCalculatorService",["+ calculateVehiclePrice(...)","+ calculatePurchaseDeposit(...)","+ calculateTotalRentalCost(...)"],540,600,480,170)
    t.box("catalog","CatalogService",["− priceCalculator: PriceCalculatorService", "+ getVehicleRows(state)","+ updateVehiclePricing(...)"],20,820,480,180)
    t.box("q","OperationQueryService",["− requestDao: RequestDao", "− testDriveDao: TestDriveDao", "− rentDao: RentDao; saleDao: SaleDao", "+ getClientRequests(clientId)"],540,820,480,180)
    t.box("obs","TestDriveCancellationObserver",["− testDriveDao: TestDriveDao", "+ onVehicleCommitted(event, conn)"],20,1050,480,150)
    t.box("rows","Record annidati di CatalogService",["VehicleRow(Vehicle, nomi e prezzi)", "ModelDiscountRow(ModelDiscount, nome)"],540,1050,480,150,kind="nota",fill=GOLD)
    t.note("DatabaseManager è un campo di tutti i servizi sopra, salvo calcolatore, query e observer.\nPurchase, cessione e noleggio usano DAO veicoli, modelli, sconti, conti e operazioni.\nPurchase e Rental conservano anche eventPublisher: VehicleEventPublisher.\nObserver realizza event.VehicleCommitmentObserver (CL-07).\nI campi priceCalculator mostrano riferimenti condivisi, senza composizione UML.",1270,205)
    t.save()


def events():
    t=Tavola("classi-eventi", "CL-07 — Eventi nella transazione", "Package it.unifi.drivehub.event. Grigio: collaboratori del package service.")
    t.box("pub","VehicleEventPublisher",["− observers", "+ attach(observer); detach(observer)","+ publishCommitment(event, conn)"],20,440,420,175)
    t.box("obs","VehicleCommitmentObserver",["+ onVehicleCommitted(event, conn)"],540,440,400,145,kind="interface")
    t.edge("pub","obs","[0..*]","assoc")
    t.box("evt","VehicleCommitmentEvent",["vehicleId: Long {ID}"],20,750,420,120,kind="record")
    t.edge("pub","evt","parametro")
    t.box("impl","TestDriveCancellationObserver",["− testDriveDao: TestDriveDao", "+ onVehicleCommitted(event, conn)"],540,750,400,175,kind="service",fill=GREY)
    t.edge("impl","obs",kind="implements");t.edge("impl","evt","event")
    t.box("call","PurchaseService / RentalService",["− eventPublisher: VehicleEventPublisher", "pubblicazione prima del commit"],20,120,420,155,kind="service · selezione",fill=GREY)
    t.edge("call","pub","eventPublisher [1]","assoc")
    t.note("Observer e publisher ricevono la Connection JDBC del chiamante come parametro.\nLa notifica è sincrona; l'errore si propaga e impedisce il commit del servizio.\nL'evento conserva solo vehicleId. L'observer cancella i test drive futuri del veicolo.\nAppContext registra l'observer; non sono previsti coda, outbox o eventi persistenti.",1010,180)
    t.save()


def daos():
    t=Tavola("classi-dao", "CL-08a — Contratti DAO", "Package it.unifi.drivehub.dao. conn: java.sql.Connection; firme abbreviate con i parametri rilevanti.",1040)
    entries=[
        ("UserDao",["findByEmail(email): Optional<User>","lockUser(id, role, conn)","saveClient(client, conn)"]),
        ("BankAccountDao",["findByClientIdForUpdate(id, conn)","updateBalance(iban, balance, conn)"]),
        ("BrandDao",["findAll(): List<Brand>","save(brand, conn)"]),
        ("ModelDao",["findById(id, conn): Optional<Model>","save(model, conn)"]),
        ("VehicleDao",["findByIdForUpdate(id, conn)","updateStateAtomically(..., conn)","updatePricing(vehicle, conn)"]),
        ("DiscountDao",["findModelDiscount(id, conn)","saveVehicleDiscount(discount, conn)"]),
        ("RequestDao",["findByIdForUpdate(id, conn)","save(request, conn); update(...)"]),
        ("RentDao",["isVehicleAvailableForRent(..., conn)","deleteFutureByVehicleId(id, conn)"]),
        ("TestDriveDao",["countActiveByClientId(id, conn)","deleteFutureByVehicleId(id, conn)"]),
        ("SaleDao",["findLatestSaleForVehicle(id, conn)","save(sale, conn)"]),
    ]
    for n,(title,members) in enumerate(entries):
        t.box("d"+str(n),title,members,20+(n%2)*520,120+(n//2)*190,480,160,kind="interface",fill=GREY)
    t.note("I contratti restituiscono entità, Optional e liste; non sono un ORM.\nLe firme selezionano letture, lock e scritture, senza elencare ogni operazione CRUD.\nDatabaseManager e le callback transazionali sono nella tavola CL-08b dello stesso package.",1100,155)
    t.save()
    t=Tavola("classi-transazioni", "CL-08b — Gestione delle transazioni", "Package it.unifi.drivehub.dao. Tipi annidati qualificati con DatabaseManager.",1040)
    t.box("db","DatabaseManager",["+ getConnection(): Connection", "+ executeInTransaction(TransactionConsumer)","+ executeInTransactionWithResult(TransactionFunction<T>): T"],20,160,1000,170)
    t.box("tc","DatabaseManager.\nTransactionConsumer",["+ execute(conn): void throws Exception"],20,500,480,155,kind="interface")
    t.box("tf","DatabaseManager.\nTransactionFunction<T>",["+ execute(conn): T throws Exception"],540,500,480,155,kind="interface")
    t.edge("db","tc","parametro");t.edge("db","tf","parametro")
    t.note("I DAO non estendono DatabaseManager: i contratti espongono entità e JDBC.\nLe due interfacce annidate sono callback del corpo transazionale, non DAO aggiuntivi.\nNon tutti gli overload conn dei contratti garantiscono da soli il riuso: alcuni default\ndelegano alla lettura senza conn; le implementazioni PostgreSQL pertinenti li ridefiniscono.\nIl gestore apre la connessione, disabilita autocommit e tenta commit o rollback.",780,205)
    t.save()


def postgres():
    t=Tavola("classi-postgres", "CL-09 — Realizzazioni PostgreSQL", "Package it.unifi.drivehub.dao.postgres. A destra i contratti esterni del package dao.",1040)
    for n,name in enumerate(["User","BankAccount","Brand","Model","Vehicle","Discount","Request","Rent","TestDrive","Sale"]):
        t.box("p"+str(n),"Postgres"+name+"Dao",["− db: DatabaseManager"],20,120+n*112,490,95,fill=GREY)
        t.box("i"+str(n),name+"Dao",[],700,132+n*112,320,70,kind="interface · dao",fill=GREY)
        t.edge("p"+str(n),"i"+str(n),kind="implements")
    t.note("Ogni implementazione conserva un riferimento db: DatabaseManager ricevuto dal costruttore.\nLe letture autonome aprono/chiudono connessioni; i percorsi transazionali usano conn.\nPreparedStatement e mapping ResultSet → entità; lock nei metodi ForUpdate.\nVehicle, Request e Sale convertono esplicitamente gli enum PostgreSQL (ADR 0002).\nSono omessi metodi ripetuti di mapping/CRUD; nessuna ereditarietà fra i dieci DAO.",1290,205)
    t.save()


def presentation():
    t=Tavola("classi-presentazione", "CL-10 — Viste e caricamenti", "Package it.unifi.drivehub.presentation.view. Grigio: classi JavaFX esterne.",1040)
    t.box("bp","BorderPane",[],790,420,230,90,kind="JavaFX",fill=GREY)
    for id,title,body,x,y,w,h in [
        ("login","LoginView",["− authService: AuthService", "− onLoginSuccess: Consumer<User>"],20,120,670,130),
        ("client","ClientDashboardView",["− client: Client; onLogout: Runnable", "− purchaseService: PurchaseService", "− rentalService: RentalService"],20,290,670,150),
        ("sales","SalesmanDashboardView",["− salesman: Salesman", "− saleToDealershipService: SaleToDealershipService", "− operationQueryService: OperationQueryService"],20,480,670,150),
        ("manager","ManagerDashboardView",["− manager: Manager", "− catalogService: CatalogService", "− operationQueryService: OperationQueryService"],20,670,670,150)]:
        t.box(id,title,body,x,y,w,h); t.edge(id,"bp",kind="extends")
    t.box("async","AsyncLoad<T>",["− source: Supplier<T>", "− current: Task<T> [0..1]", "~ load()"],20,880,480,160,kind="package-private")
    t.box("hb","HBox",[],700,920,320,75,kind="JavaFX",fill=GREY);t.edge("async","hb",kind="extends")
    t.box("account","AccountManagementDialog",["+ show(..., authService, ...) {static}"],20,1100,480,140)
    t.box("utils","UiUtils",["+ formatCurrency(amount) {static}","+ showError(owner, title, message) {static}"],540,1100,480,140)
    t.edge("account","utils")
    t.note("Le tre dashboard usano AsyncLoad, AccountManagementDialog e UiUtils (archi omessi).\nLogin e dashboard estendono BorderPane; AccountManagementDialog non estende Dialog.\nGli overload di comodo ricorrono ad AppContext.getInstance; App inietta i servizi.\nAsyncLoad usa Task e worker virtuali; solo il caricamento corrente pubblica il risultato.\nNessun controller/FXML; restano azioni economiche sincrone negli handler.",1320,205)
    t.save()


def security():
    t=Tavola("classi-sicurezza", "CL-11 — Credenziali", "Package it.unifi.drivehub.security. Contratto ristretto al trattamento delle password.")
    t.box("iface","PasswordHasher",["+ hash(rawPassword): String", "+ verify(rawPassword, encodedHash): boolean"],180,140,600,160,kind="interface")
    t.box("impl","Pbkdf2PasswordHasher",["− iterations: int", "− saltBytes: int", "− keyBits: int", "− random: SecureRandom", "+ hash(rawPassword): String", "+ verify(rawPassword, encodedHash): boolean"],180,500,600,280)
    t.edge("impl","iface",kind="implements")
    t.note("AuthService conserva PasswordHasher; AppContext sceglie Pbkdf2PasswordHasher.\nPBKDF2WithHmacSHA256, sale casuale e parametri memorizzati nella stringa codificata.\nQuesta astrazione non è un sistema di autorizzazione: la navigazione per ruolo è in User/App.\nI servizi non ricevono tutti un'identità autenticata da verificare a ogni invocazione.",900,180)
    t.save()


if __name__ == "__main__":
    for build in (packages,startup,users,vehicles,discounts,operations,services,events,daos,postgres,presentation,security):
        build()
    print("13 tavole manuali scritte; autocontrolli XML, ID e terminali degli archi superati.")
