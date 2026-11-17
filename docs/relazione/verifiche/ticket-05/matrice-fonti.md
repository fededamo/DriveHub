# Matrice affermazioni e frammenti → sorgenti

Riscontro statico del 13 novembre 2026, HEAD
`61ce18ae1d4e0458b63e970b793b0b3a634a3adf`. I file tecnici sono sotto
`src/main/java/it/unifi/drivehub/`, salvo il DDL esplicito.
Numeri di riga fisici, da 1; intervalli inclusivi. Ogni riga identifica
il passaggio del capitolo per sezione e tema, senza trasformare i sorgenti
in prove di esecuzione. [fonti.json](fonti.json) conserva le impronte;
[frammenti.json](frammenti.json) conserva testo esatto e intervalli inclusi
in blocchi `lstlisting`, senza pseudocodice o omissioni interne.

| ID | Passaggio / affermazione e limite | Fonti correnti (file:righe, simboli) |
| --- | --- | --- |
| D1 | 3.1.1 Vehicle: campi finali/mutabili, costruttore privato e factory pubblica, targa/km, VIN 1–17 dopo trim; nessuna prova di unicità in memoria | `domain/vehicle/Vehicle.java:17–76,95–115,151–167`; `src/main/resources/db/schema.sql:79–97,113–142` |
| D2 | 3.1.1: setState rifiuta null ma non governa le transizioni; anno/modello verificato dal servizio e trigger | `Vehicle.java:151–167`; `service/CatalogService.java:190–198`; `schema.sql:113–142`; transizioni `PurchaseService.java:65–98,147–183`, `RentalService.java:136–179` |
| D3 | 3.1.1 Request: ID/acconto/totale/tipo finali; venditore/scadenza mutabili; SALE/PURCHASE, centesimi UNNECESSARY, saldo e scadenza strettamente prima di oggi | `domain/operation/Request.java:18–74,92–127`; `domain/operation/RequestType.java`; `dao/postgres/PostgresRequestDao.java:214–226` |
| D4 | 3.1.2: gerarchia utenti, mutabilità email/hash, conto opzionale; nessuna composizione ricavata dal campo | `domain/user/User.java`; `AbstractUser.java:15–43,66–83`; `Client.java:7–42`; `Manager.java`, `Salesman.java`; `service/AuthService.java:46–49` |
| D5 | 3.1.2: sconti inclusivi 0–50, percentuale e bersaglio finali | `domain/discount/Discount.java:7–20`; `ModelDiscount.java:10–30`; `VehicleDiscount.java:10–30` |
| D6 | 3.1.2: riferimenti per ID in operazioni e conto; ricostruzione di nuove copie, record non rende Vehicle immutabile | `domain/operation/{Rent,Sale,TestDrive}.java` (campi/factory); `BankAccount.java:17–24`; `Vehicle.java:19–27`; `PostgresVehicleDao.java:146–150`; `PostgresRequestDao.java:167–171`; `CatalogService.java:72–95` |
| B1 | 3.2.1: divisione calcolatore/servizi/query, non catalogo dei metodi | `service/{PriceCalculatorService,CatalogService,OperationQueryService,PurchaseService,SaleToDealershipService,RentalService}.java`, costruttori e corpi richiamati sotto |
| B2 | 3.2.1: base manuale alternativa nella stessa formula, consigliato sempre modello; sconto massimo, fattori a cascata, double, HALF_UP, geometrica inclusiva | `domain/vehicle/Vehicle.java:118–141`; `service/PriceCalculatorService.java:26–113`; ADR 0001 |
| B3 | 3.2.1: UPDATE SALE→BLOCKED, prezzo/acconto persistiti, saldo dal totale; modello/sconti autonomi nell'avvio | `PurchaseService.java:65–98,147–183`; `PostgresVehicleDao.java:61–92,195–205`; `PostgresModelDao.java:70–98`; `PostgresDiscountDao.java:25–73`; `PostgresRequestDao.java:147–176` |
| B4 | 3.2.2: transazione con risultato e senza, connessione aperta per chiamata, commit prima del ritorno, rollback tentato; Exception e suppressed, nessun retry/isolamento impostato | `dao/DatabaseManager.java:49–109`; limite sull'esito di commit con guasto di rete è prudenza sul confine client, non guasto riprodotto |
| B5 | 3.2.2: noleggio usa conn anche per listino, lock veicolo/cliente/conto e disponibilità riletta; non serializzabilità generale | `RentalService.java:67–75,82–125`; `PostgresUserDao.java:108–119`; `PostgresRentDao.java` (isVehicleAvailableForRent); `schema.sql:278–294` |
| B6 | 3.2.2: Sale distinto da Request, tipo e importo non negativo; saldo/rifiuto/scadenza con penale 20% acconto; una transazione per candidato | `domain/operation/Sale.java:18–69`; `PurchaseService.java:101–183,195–232`; `ClientDashboardView.java:95–112` |
| B7 | 3.2.2: cessione senza pagamento alla proposta; accettazione da stima persistita; rifiuto conserva storico; titolarità da ultima SALE | `SaleToDealershipService.java:71–181`; `PostgresSaleDao.java:78–90` |
| B8 | 3.2.2: cambio destinazione SALE/RENT, noleggio odierno impedisce uscita, cancellazione/rimborso futuri e ordine conti | `RentalService.java:136–179`; `PostgresRentDao.java` (deleteFutureByVehicleId, start > CURRENT_DATE) |
| B9 | 3.2.3 / 3.5 Observer: registrazione, ciclo sincrono senza catch, stessa conn, pubblicazione prima commit, cancellazione tutti i test drive con data/ora > CURRENT_TIMESTAMP | `AppContext.java:94–96`; `event/{VehicleEventPublisher,VehicleCommitmentObserver,VehicleCommitmentEvent}.java`; `TestDriveCancellationObserver.java:22–25`; `PostgresTestDriveDao.java:201–209`; `PurchaseService.java:181`; `RentalService.java:116` |
| B10 | 3.2.3: wrapper SQL con causa, estrazione causa di dominio non uniforme; zero ammesso ma movimenti rifiutati | `DatabaseManager.java:66–109`; `RentalService.java:119–124`; `PostgresRequestDao.java:175–176`; `BankAccount.java:63–78`; `Request.java:48–65`; `SaleToDealershipService.java:126–136`; segnalazioni separate |
| B11 | 3.2.4: hasher iniettato, PBKDF2-HMAC-SHA256, sale casuale, parametri codificati, confronto isEqual; nessuna password in chiaro persistita | `AuthService.java:23–64`; `security/{PasswordHasher,Pbkdf2PasswordHasher}.java:17–80`; `AppContext.java:90–100`; `PostgresUserDao.java:169–184` |
| B12 | 3.2.4: cliente/conto nella stessa conn, saldo zero, ricarica bloccata; IBAN sintattico e non verifica bancaria | `AuthService.java:53–64,96–105,122–130`; `BankAccount.java:15–48`; `PostgresBankAccountDao.java:32–56,81–106`; `schema.sql:188–196` |
| B13 | 3.2.4: password verificata prima di scritture identità, NO KEY UPDATE/FK, CASCADE conto anche positivo | `AuthService.java:67–119`; `PostgresUserDao.java:215–282`; `schema.sql:194,256–258,270–271,287–288,308–310` |
| B14 | 3.2.4 / 3.4: ruoli UI non dimostrano autorizzazione generale; ID locali, email LOWER diversa da vincoli/trigger, nessuna unicità globale dimostrata | firme servizi; `PostgresUserDao.java:25–81,285–291`; `schema.sql:147–184,201–235`; [limiti pregressi](../ticket-04/difetti-applicativi.md) |
| P1 | 3.3.1: App sostituisce Scene, navigator anonimo, servizi/utente/logout; Client seleziona overload; costruzione login al logout | `App.java:54–115`; `Client.java:32–35`; `UserDashboardNavigator.java:7–14`; `Salesman.java`, `Manager.java` (openDashboard) |
| P2 | 3.3.1 / 3.5: costruttori di comodo accedono al contesto globale; DI non uniforme | `presentation/view/{LoginView,ClientDashboardView,SalesmanDashboardView,ManagerDashboardView,AccountManagementDialog}.java` (overload AppContext.getInstance); `AppContext.java:74–117` |
| P3 | 3.3.2: righe collettive con mappe e prezzi pronti, rendering catalogo senza SQL; altre consultazioni ancora sincrone | `CatalogService.java:72–95`; `ClientDashboardView.java:193–202,529–582,678–682`; nessuna cache/snapshot comune nei DAO autonomi richiamati |
| P4 | 3.3.2: Task/sorgente catturata, cancellazione precedente, current identity, successo/errore/Riprova; target resta disabilitato al fallimento; cancel a scene null | `presentation/view/AsyncLoad.java:24–74`; API [Task JavaFX 21](https://openjfx.io/javadoc/21/javafx.graphics/javafx/concurrent/Task.html) per gestori sul thread FX e cancellazione cooperativa, consultata il 13/11/2026 con web e Context7 |
| P5 | 3.3.3: preventivo/conferma, successo dopo servizio e refresh, nuove letture possono fallire dopo commit; comandi sincroni | `ClientDashboardView.java:205–234,495–526`; `PurchaseService.java:65–98,147–183`; `DatabaseManager.java:88–108`; è una conseguenza statica del try comune, non una nuova prova UI |
| P6 | 3.3.3: runLater non worker, scadenze stderr e non Riprova; dialoghi con UiUtils | `ClientDashboardView.java:95–112`; `AccountManagementDialog.java:52–165`; `LoginView.java:108–153`; [Platform JavaFX 21](https://openjfx.io/javadoc/21/javafx.graphics/javafx/application/Platform.html#runLater(java.lang.Runnable)) |
| J1 | 3.4.1: interfacce JDBC, conn propria vs ricevuta, statement/result chiusi senza chiudere conn esterna; guardia autocommit per lock | `dao/VehicleDao.java`; `dao/RequestDao.java`; `PostgresVehicleDao.java:61–92`; `PostgresRequestDao.java:77–112` |
| J2 | 3.4.1: UPDATE condizionale booleano, controlli righe nei percorsi protetti, RETURNING id e nuova entità; non tutti UPDATE contano | `PostgresVehicleDao.java:129–155,195–205`; `PostgresRequestDao.java:147–176,204–226`; `PostgresBankAccountDao.java:94–106`; controesempio `PostgresUserDao.java:189–210` |
| J3 | 3.4.2: null venditore/data/totale, ID senza navigazione, BigDecimal/Date, lettura tramite factory e fallimento dati incompatibili | `PostgresRequestDao.java:147–176,214–226`; `Request.java:38–65`; `PostgresVehicleDao.java:250–261`; `schema.sql:240–269` |
| J4 | 3.4.2: quattro enum Java/PostgreSQL e cast name/valueOf; nulla in più su importi/relazioni | `domain/vehicle/{VehicleState,PriceMode}.java`; `domain/operation/{RequestType,SaleType}.java`; `schema.sql:36–39`; `PostgresVehicleDao.java:130–144,250–261`; `PostgresRequestDao.java:148–165,225`; `PostgresSaleDao.java:94–114,123–135`; ADR 0002 |
| J5 | 3.4.2: user UNION ALL e switch, tabella da insieme chiuso, SQLSTATE 23503 in errore di dominio | `schema.sql:230–235`; `PostgresUserDao.java:25–81,189–241,285–291`; `AuthService.java:46–49` |
| I1 | 3.5: factory statiche non Factory Method GoF; costruttori privati e nessun Creator polimorfico; enum non basta per State | `Vehicle.java:29–76`; `Request.java:27–74`; `Sale.java:26–69`; `VehicleState.java`; riscontro strutturale, non etichetta Javadoc |
| I2 | 3.5: Observer GoF, effetti esterni non annullabili dal solo rollback JDBC; non eventi persistenti | fonti B9, `DatabaseManager.java:66–109`; limite dedotto dal perimetro JDBC del rollback |
| I3 | 3.5: double dispatch nello stile Visitor, insieme ruoli chiuso; dominio conosce navigazione, nessun attraversamento generico | fonti P1, `domain/user/User.java` (openDashboard); `PostgresUserDao.java:39–44` |
| I4 | 3.5: DAO e DI esterni a GoF, dipendenze concrete/JDBC e composition root; Singleton di accesso, non unica istanza possibile o thread safety generale | `AppContext.java:70–117`; `PurchaseService.java:35–59`; `AuthService.java:20–31`; fonti J1/P2 |

## Selezione, riuso e limiti

Tredici frammenti fra 3 e 19 righe fisiche (le righe lunghe vanno a capo
nel PDF). Sono estratti direttamente dai Java: il commento esplicativo è
nel testo circostante, senza aggiunte all'estratto. I numeri a sinistra
rimandano ai file originali; gli hash consentono di rilevare spostamenti
successivi. Non vengono riprodotti interi metodi ampi o l'elenco delle API.

Riusati appendice B/CL, PKG, navigazione per ruolo, SQ-01–03/SQ-OB,
REL-02/03: nessuna tavola nuova e nessun XML/esportazione precedente
modificati. Il capitolo 2 conserva il dettaglio progettuale dei flussi e
vincoli; qui il centro è il nesso fra codice, responsabilità e garanzia.
Le parti introduttive dei flussi sono un richiamo necessario a leggere i
frammenti, non nuove specifiche. Nessun risultato storico è qualificato
come test eseguito in questo ticket.
