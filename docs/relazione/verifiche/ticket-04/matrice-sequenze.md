# Sequenze → collaborazioni → sorgenti

Riscontro **statico del 12 novembre 2026**, HEAD
`61ce18ae1d4e0458b63e970b793b0b3a634a3adf`. Nessun test o comando DB
eseguito. Le righe comprendono frecce e sintesi testuali della tavola.
Tutte le fonti hanno impronta nella baseline; [fonti.json](fonti.json)
indicizza quelle direttamente usate. I numeri sono linee dei sorgenti correnti.
Prefisso Java: `src/main/java/it/unifi/drivehub/`.

| Tavola / UC | Ingresso attore e handler | Collaborazioni effettive e messaggi | Sorgenti riscontrati |
| --- | --- | --- | --- |
| SQ-01a / UC-03 1–3, 3.a | Cliente conferma → `ClientDashboardView.handlePurchaseAction` → `initiatePurchase(vehicleId,clientId)`; la vista passa ID da VehicleRow/Client in memoria | `DatabaseManager.executeInTransactionWithResult`; VehicleDao `updateStateAtomically` SALE→BLOCKED, `findById` con c1; ModelDao `findById` e DiscountDao `findModelDiscount`/`findVehicleDiscount` **senza c1**; PriceCalculator `calculateVehiclePrice`/`calculatePurchaseDeposit`; BankAccountDao `findByClientIdForUpdate`, dominio `withdraw`, `updateBalance`; Request `createNewSaleRequest`, RequestDao `save` | `presentation/view/ClientDashboardView.java:205–237`; `service/PurchaseService.java:65–95`; `service/PriceCalculatorService.java:26–40,85–89`; `dao/postgres/PostgresVehicleDao.java:70–94,195–207`; `PostgresModelDao.java:70–98`; `PostgresDiscountDao.java:25–73`; `PostgresBankAccountDao.java:32–57,94–107`; `PostgresRequestDao.java:147–178` |
| SQ-01b / UC-04, UC-03 4.a | Venditore → `SalesmanDashboardView.handleApproveRequest` / `handleRejectRequest`; rami SALE | `approvePurchase` o `rejectPurchase`, ognuno `executeInTransaction`; RequestDao `findByIdForUpdate`; tipo SALE. Approva: `setSalesmanId`, `setExpDate(oggi.plusMonths(1))`, `update`. Rifiuta: conto FOR UPDATE, `deposit(acconto)`, update saldo, aggiornamento condizionale BLOCKED→SALE, `delete` richiesta | `presentation/view/SalesmanDashboardView.java:134–179`; `service/PurchaseService.java:101–141`; `dao/postgres/PostgresRequestDao.java:91–112,181–212`; `domain/operation/Request.java:110–143`; DAO conto/veicolo come SQ-01a |
| SQ-01c / UC-03 5–6, 5.a, 6.a | Cliente → `ClientDashboardView.handleCompletePurchaseAction`; anticipa controlli sulla copia UI, passa solo requestId | `completePurchase`; richiesta FOR UPDATE, tipo SALE, salesman/exp presenti, `!isExpired`; `getAgreedTotal`/`getRemainingAmount`; conto FOR UPDATE, `withdraw(residuo)`, update; `Sale.createSaleRecord`, SaleDao `save` **totale**, RequestDao `delete`, VehicleDao BLOCKED→SOLD, `publishCommitment(event,c3)` prima del ritorno callback | `ClientDashboardView.java:495–528`; `PurchaseService.java:147–184`; `domain/operation/Request.java:114–143`; `domain/operation/Sale.java:68–70`; `dao/postgres/PostgresSaleDao.java:93–121`; DAO richiesta/conto/veicolo come sopra; SQ-OB |
| SQ-01d / UC-03 6.b, UC-S02 | Costruttore `ClientDashboardView`: `Platform.runLater(this::checkExpirationsOnStartup)`; handler chiama controllo con clientId; nessun scheduler periodico | `checkAndHandleExpiredRequests`: `findByClientId` autonomo; ogni candidato avvia `executeInTransactionWithResult`; richiesta FOR UPDATE; guardie su esistenza/tipo/cliente/approvazione/scadenza; skip→null; penale20% acconto HALF_UP, rimborso differenza; conto FOR UPDATE e `deposit`; `Sale.createPenaltyRecord`/save, delete richiesta, BLOCKED→SALE; commit per candidato, poi messaggio | `ClientDashboardView.java:94–112`; `PurchaseService.java:195–233`; `PostgresRequestDao.java:59–74,91–112`; `Request.java:142–143`; `Sale.java:78–80`; DAO come sopra |
| SQ-02 / UC-02 3–6, 3.a–5.a | Cliente conferma dialogo dentro `handleRentAction`; callback passa vehicleId/clientId/date. Anteprima `calculateRentalCost(id,start,end)` distinta | `bookRental`: date prima transazione; VehicleDao `findByIdForUpdate`, stato RENT; UserDao `lockClient`; RentDao `isVehicleAvailableForRent`; `calculateRentalCost(vehicle,start,end,c5)` chiama ModelDao/DiscountDao con c5, PriceCalculator; conto lock, withdraw/importo intero/update; `Rent.create`/save; publisher e SQ-OB; ritorno Rent dopo commit | `ClientDashboardView.java:265–319`; `RentalService.java:59–126`; `PostgresUserDao.java:108–121` (NO KEY UPDATE); `PostgresRentDao.java:95–136`; `PostgresModelDao.java:79–98`; `PostgresDiscountDao.java:34–73`; `PriceCalculatorService.java:92–117`; `domain/operation/Rent.java`; SQL `ex_rent_vehicle_period` |
| SQ-03a / UC-05 1–4, 3.a | Cliente → handler locale `btnSubmit` in `ClientDashboardView.buildSellMyCarTab`; eventuale `btnEstimate` chiama preventivo separato. Passa ID modello, non Model | `submitSaleProposal`/executeWithResult; VehicleDao `findByVinForUpdate`; se noto SOLD: SaleDao `findLatestSaleForVehicle` (solo SALE, date DESC/id DESC), RequestDao `hasOpenRequestForVehicle`; controllo proponente/modello/anno, setter e update WAITING. Se nuovo: ModelDao senza c, Vehicle.create e save su c6. `calculateEstimate` rilegge modello/sconto autonomi e chiama `estimateVehicleValue`; Request PURCHASE/save con stima positiva o zero | `ClientDashboardView.java:362–434`; `SaleToDealershipService.java:55–113`; `PostgresVehicleDao.java:109–178`; `PostgresSaleDao.java:78–90`; `PostgresRequestDao.java:133–178`; `PriceCalculatorService.java:43–48`; `Request.java:84–86` |
| SQ-03b / UC-05 5–6, 5.a–6.a | Venditore → stessi handler con ramo PURCHASE, non UC-04 | `approveSaleProposal`/`rejectSaleProposal`, executeInTransaction; `lockWaitingPurchaseRequest`: richiesta e veicolo FOR UPDATE, PURCHASE/WAITING. Accetta: salesmanId non nullo, payout da deposit, conto lock/deposit/update, Sale PURCHASE/save, WAITING→SALE, delete request. Rifiuta: delete request, `hasAssociatedOperations` su test_drive/rent/sale, WAITING→SOLD oppure delete vehicle. Nessun Observer | `SalesmanDashboardView.java:134–179`; `SaleToDealershipService.java:121–181`; `PostgresVehicleDao.java:75–94,195–247`; DAO richiesta/conto/registro come sopra; `Sale.java:72–80` |
| SQ-OB / ref SQ-01c e SQ-02 | Ingresso da servizi già in transazione; **non** un nuovo attore o handler UI | `VehicleEventPublisher.publishCommitment` scorre lista; `TestDriveCancellationObserver.onVehicleCommitted(event,c)` → TestDriveDao `deleteFutureByVehicleId(event.vehicleId(),c)` → DELETE SQL `date > CURRENT_TIMESTAMP`; ritorno int ignorato; nessun commit nell'observer | `event/VehicleEventPublisher.java:13–29`; `event/VehicleCommitmentEvent.java`; `service/TestDriveCancellationObserver.java:18–25`; `dao/postgres/PostgresTestDriveDao.java:201–211`; `AppContext.java:90–106` collega publisher/observer ai due servizi |

## Connessioni, SQL e propagazione

- `DatabaseManager.java:49–51,66–106`: connessione nuova per invocazione,
  autocommit false, callback, commit, chiusura try-with-resources; Exception
  nel corpo/commit → tentativo rollback, errore rollback soppresso sulla
  causa, RuntimeException. Apertura/chiusura hanno il ramo SQLException.
  Non viene impostato un livello d'isolamento specifico. Non si promette
  assenza generale di deadlock o recupero certo da commit incerto.
- I DAO con parametro `Connection` usati dalle tavole preparano SQL sulla
  connessione ricevuta. Gli overload senza parametro aprono/chiudono una
  propria connessione. Il nome uguale di un metodo nel contratto non
  basta a dimostrare il riuso: verificati i corpi PostgreSQL reali.
- SQL è sintetizzato separatamente dai metodi. L'UPDATE condizionale del
  veicolo controlla il conteggio; le riletture richiesta/veicolo/conto con
  `ForUpdate` usano davvero `FOR UPDATE`. `lockClient` usa `NO KEY UPDATE`.
  Non si attribuisce un lock di chiave inesistente a `findByVinForUpdate`.
- `RentalService.bookRental` e `submitSaleProposal` riespongono cause
  applicative IllegalArgumentException/IllegalStateException quando avvolte;
  gli altri percorsi illustrati propagano l'eccezione del gestore. Le azioni
  manuali mostrano un errore UI. Il controllo scadenze intercetta Throwable
  e scrive su stderr: eventuali episodi già committati non vengono annullati.
- Le note `commit(c)`/`rollback c` descrivono JDBC `conn.commit()` e
  `conn.rollback()` nel gestore, non inesistenti metodi dei servizi.

## Selezione e omissioni

Le otto tavole sono episodi coordinati dei tre flussi, non otto nuovi casi
d'uso. Sono omessi getter ordinari, mapping completo delle righe,
PreparedStatement/setter, result set, factory minori, chiusura statement,
aggiornamento tabelle UI e messaggi di conferma ripetuti. Le letture
modello/sconti e i metodi locali di dominio sono esplicitati nelle note
anziché aggiungere molte linee di vita. Tali note sono parte del riscontro.
Le classi concrete non sono fuse in un inesistente partecipante “DAO”.

Non sono ripetuti flusso di login, gestione prezzi, cancellazione identità,
cambio destinazione UC-08 o prenotazione test drive UC-01, già descritti
nelle schede; il solo effetto Observer richiesto è dettagliato qui.
Non si rappresentano test come partecipanti, chiamate automatiche al saldo,
autorizzazioni non presenti, processi asincroni, lock del listino o uno
snapshot comune alle letture autonome. La selezione UI non diventa
un'associazione a oggetti nel DB. Le alternative rare di infrastruttura
sono riassunte nella regola comune d'errore, senza promettere atomicità
distribuita o consegna esattamente una volta.
