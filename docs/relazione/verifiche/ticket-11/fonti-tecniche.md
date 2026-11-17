# Fonti tecniche — ticket 11

Riscontro statico del 16 novembre 2026, HEAD
`61ce18ae1d4e0458b63e970b793b0b3a634a3adf`. Nessuna prova applicativa,
esecuzione Java, accesso DB o modifica a software/schema/dati.
I percorsi Java sotto sono relativi a `src/main/java/it/unifi/drivehub/`.
[fonti.json](fonti.json) identifica con impronte i 65 Java di produzione,
schema, configurazione, POM, README, architettura e due ADR: 72 file.
Il censimento identifica anche il contesto immutato; non dichiara una nuova
esecuzione o la lettura integrale di ogni test citato nei capitoli conservati.

| Destinazione | Fonti e riscontro |
| --- | --- |
| 3.1.1 | `domain/user/User.java`, `AbstractUser.java:11–103`, `Client.java`, `Salesman.java`, `Manager.java`, `BankAccount.java`. Classe base astratta con costruttore protetto, sottotipi reali, normalizzazione, uguaglianza stessa classe/ID o email senza ID, conto opzionale; regex IBAN e mutazioni dopo i controlli. |
| 3.1.2 | `domain/vehicle/{Brand,Model,Vehicle,PriceMode,VehicleState}.java`, `domain/discount/*.java`. Model immutabile; Vehicle mutabile con ID/modello/VIN/anno finali; VIN 1–17 dopo trim, targa/km, anni e base manuale. Discount 0–50. Nessun automa nel solo setState. |
| 3.1.3 | `domain/operation/Rent.java:31–79`, `TestDrive.java:29–51`. Date incluse e importi non negativi; fascia test drive 09:00–16:00, senza obbligo dei minuti zero prima delle 16 e senza campo fine/durata implementata. |
| 3.1.4 | `domain/operation/Request.java:38–127`, `Sale.java:33–76`, RequestType/SaleType. Totale/acconto SALE in centesimi esatti, stima PURCHASE senza totale, tipi espliciti; tolleranza temporale Sale di due minuti, assenza di tutte le combinazioni FK/tipo nella factory generale. |
| 3.1.5, 3.2.6, 3.5.1 | `event/*.java`, `service/TestDriveCancellationObserver.java:23–25`, `dao/postgres/PostgresTestDriveDao.java` (deleteFutureByVehicleId). Evento con solo vehicleId; publisher sincrono, Connection passata al destinatario, nessun catch del listener; cancellazione di tutti i futuri, non soltanto nel periodo Rent. |
| 3.2.1 | `dao/DatabaseManager.java:49–106`. DriverManager apre ogni Connection; Consumer e Function distinti, setAutoCommit(false), commit prima del ritorno; catch Exception, rollback tentato, causa e soppressa sul guasto rollback, chiusura try-with-resources. Nessun isolamento impostato, pool, retry o contesto globale della transazione. |
| 3.2.2 | `AppContext.java:71–117`, costruttori degli otto servizi. Composizione manuale di dieci DAO, hasher, publisher, observer e calcolatore concreto; responsabilità effettive, senza vecchi servizi/factory. |
| 3.2.3.1 | `service/TestDriveService.java:40–87`, `CatalogService.java:72–96`, `PostgresTestDriveDao.java:131–169`, `PostgresUserDao.java:108–120`. Ingresso, lock veicolo/cliente, conteggio futuro, disponibilità allo stesso timestamp e giorno Rent, salvataggio con conn, annullamento con titolarità. |
| 3.2.3.2 | `service/RentalService.java:59–178`, `PostgresRentDao.java:95–113`, `schema.sql:279–292`. Preventivo e conferma distinti, letture con conn, lock veicolo/cliente/conto, ordine addebito–Rent–evento–commit; overlap inclusivo, EXCLUDE; cambio destinazione, noleggio odierno, futuri e ordine conti. |
| 3.2.3.3 | `service/PurchaseService.java:65–233`. UPDATE SALE/BLOCKED prima del prezzo; letture modello/sconti senza conn; conto e accordo persistito; approvazione e rifiuto distinti, saldo totale meno acconto senza listino, Sale prima di delete/transizione/Observer. Scadenze: lista autonoma, una transazione per candidato, penale 20% acconto e rimborso residuo. Nessuna gestione che ignori il guasto del candidato. |
| 3.2.3.4 | `service/SaleToDealershipService.java:55–182`, `PostgresSaleDao.java:77–89`. calculateEstimate appartiene al servizio; chiama estimateVehicleValue sul calcolatore. VIN noto, ultima SALE al proponente, modello/anno e richiesta aperta; nuovo VIN senza lock di riga inesistente, stima ricalcolata e salvata. Esito con lock richiesta/veicolo/conto e stima persistita; nessun venditore costante o transazione annidata. |
| 3.2.4 | `service/PriceCalculatorService.java:26–111`, `Vehicle.java:122–142`, ADR 0001. Massimo sconti, base manuale, fattori double, esponente km reale /20000, round HALF_UP, acconto e somma geometrica inclusiva. Esempi ricalcolati con gli stessi passaggi numerici; nessun test Java eseguito. |
| 3.2.5 | `service/AuthService.java:33–132`, `security/{PasswordHasher,Pbkdf2PasswordHasher}.java`, `PostgresUserDao.java:215–291`, `PostgresBankAccountDao.java`. Login e conto separati, cliente/conto nella stessa transazione, ricarica con lock, password prima della scrittura, cancellazione con NO KEY UPDATE e FK; hash PBKDF2 (210.000 iterazioni, 16 byte di sale e 256 bit di chiave), parametri salvati e confronto MessageDigest.isEqual. Configurazione DatabaseManager: ambiente, properties, default dimostrativi. Nessuna sessione generale autorizzativa nei servizi. |
| 3.3.1–2 | `App.java:37–115`, `AppContext.java:71–117`, `UserDashboardNavigator.java`, tre sottotipi utenti; viste Login/Client/Salesman/Manager, AccountManagementDialog, UiUtils. Scene costruite in Java, servizi e callback, account comune, double dispatch. Il contesto non conserva l'utente. |
| 3.3.3 | `presentation/view/AsyncLoad.java:23–74`, `CatalogService.java:72–106`, `OperationQueryService.java:31–52`; ClientDashboardView: scadenze, handlePurchaseAction e handleCompletePurchaseAction. Supplier catturato, Task precedente cancellato, identità del task, errore/Riprova e Scene; letture asincrone selettive, comandi e altri refresh ancora sincroni dopo commit. |
| 3.4.1, 3.4.5 | Dieci interfacce DAO; `PostgresVehicleDao.java:61–95,195–206`, `PostgresRequestDao.java:189–227`, `PostgresBankAccountDao.java:91–102`. PreparedStatement, proprietà reale degli overload, conteggi nei percorsi protetti e non generalizzati, INSERT RETURNING id e nuova istanza. Nessuna identity map/UnitOfWork. |
| 3.4.2 | `PostgresUserDao.java:25–81,170–291`, `schema.sql:147–235`. UNION ALL e sottotipi, resolveTable chiuso; sequenza default condivisa ma PK locali, email LOWER in lettura contro UNIQUE/trigger esatti e ID escluso. DeleteUser236–241 traduce SQLSTATE23503 in IllegalStateException e conserva la causa. |
| 3.4.3 | `PostgresRequestDao.java:214–227` e scrittura, `PostgresSaleDao.java:93–135`; mapper Rent/TestDrive. BigDecimal e DECIMAL, DATE/TIMESTAMP senza fuso, getObject prima di getLong e setNull simmetrici; enum.name/valueOf e cast nativi, ADR 0002. |
| 3.4.4 | `src/main/resources/db/schema.sql`: enum 36–39, vincoli Vehicle 89–96, anni 113–143, conto 188–195, email 201–235, Request 240–258, TestDrive 264–273, Rent 279–292, Sale 298–310. Regole SQL distinte da titolarità, stato, approvazione e capienza nei servizi. |
| 3.5.1–6 | Fonti sopra e factory Vehicle/Request/Sale. Classificazione basata su collaborazioni: Observer GoF, calcolatore concreto senza Strategy, factory statiche diverse da Factory Method, dispatch stile Visitor, accesso Singleton predefinito con contesti alternativi, DAO/DI/coordinamento e idiomi Java. |

Il capitolo 2, le appendici A/B e gli undici nativi/esportazioni riallineati
nel ticket 10 sono riusati con gli stessi identificativi, verificandone le
impronte. Nessun intervento o nuova esportazione di diagrammi è stato necessario;
il rendering delle pagine del PDF non modifica le tavole. I quattordici
frammenti originali sono tracciati separatamente in
[copertura-frammenti.json](copertura-frammenti.json) e [confronto.md](confronto.md).

La lettura di README, `docs/architecture.md`, POM e configurazione conferma
Java 21/JavaFX, JDBC/PostgreSQL e inizializzazione esterna già descritti nel
corpo conservato. Nessun comando di avvio o inizializzazione è stato eseguito.
