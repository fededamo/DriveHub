# Ticket 11 — prenotazioni concorrenti

Verifica del 2 novembre 2026. Implementato soltanto il ticket 11.
**6/6 criteri verificati; ticket 11 chiuso.** Suite finale e review indipendenti completate senza rilievi.
Prerequisito 08 e tutti i ticket 01–10 controllati chiusi: [controllo](prerequisites.json).
Nessun commit/staging; `Relazione_SWE` esclusa.

## Versione e conservazione

HEAD iniziale `c2a81122ee6be44760ded4160fde229bd33e837c`, indice invariato.
Prima delle modifiche acquisiti **576 file** in `/tmp/drivehub-ticket11/baseline`,
compresi piano e tracker esterni, sorgenti e tutte le evidenze preesistenti.
Esclusi `.git`, `target` e `Relazione_SWE`.
[Manifest baseline](baseline-sha256.json). La review usa il solo confronto con
questa baseline locale: un diff da HEAD includerebbe impropriamente il lavoro 01–10.

[Diff fissato per la review](review.patch), [14 file](review-files.txt), SHA256
`8344e02efb3cf795d9e287d049a886b51e15dad2e690233416e2587698d2c03e`.
[Hash della versione revisionata](review-files.sha256.json).
Gli hash di ciascuna esecuzione identificano sorgenti, test, schema, fixture e POM
realmente compilati; i documenti finali non costituiscono nuove prove runtime.

## Chiamanti e protocollo

| Confine | Cambiamento e verifica |
|---|---|
| `RentalService.bookRental` | Veicolo riletto con FOR UPDATE, stato RENT, lock cliente, disponibilità, prezzo, conto, inserimento e Observer sulla stessa connessione fino a commit/rollback |
| `TestDriveService.bookTestDrive` | Veicolo FOR UPDATE e stato prenotabile prima del lock cliente; limite tre riletto dopo il lock cliente; disponibilità e INSERT nella transazione |
| `UserDao` / PostgreSQL | Lock della riga client con FOR NO KEY UPDATE: serializza clienti anche su veicoli diversi e senza conto; richiede transazione attiva e cliente esistente |
| Conti 04 e acquisti 06–07 | Ordine veicolo → cliente → conto per booking; NO KEY UPDATE compatibile con KEY SHARE delle FK dei flussi economici. Conti multipli mantengono ID cliente crescente. Gare con acconto e saldo acquisto verificate |
| Stato non prenotabile | Rifiuto prima di attendere il cliente: impedisce il ciclo saldo acquisto(conto→FK veicolo), noleggio altro veicolo(cliente→conto), test drive su BLOCKED(veicolo→cliente) |
| `RentalService.calculateRentalCost` / ModelDao / DiscountDao | Quote e prenotazioni condividono la formula; overload JDBC sulla connessione del chiamante. La prenotazione usa il veicolo riletto dopo il lock, non il prezzo precedente all'attesa |
| `changeVehicleRentalState` | Stesso lock del veicolo per coordinarsi con booking. Nessuna nuova regola di transizione, rimborso o noleggio in corso del ticket 12 |
| Observer 08 / publisher / DAO test drive | Invariati: priorità commerciale, cancellazione di tutti i futuri su connessione economica, storico conservato, errore propagato. Gara nei due ordini e rollback dopo cancellazioni effettive verificati |
| Test drive manuale / letture | Cancellazione riduce il conteggio senza introdurre lock in ordine inverso; letture da nuovi AppContext osservano risultati persistiti |
| `AppContext` | Unico chiamante dei costruttori modificati; inietta UserDao nei due servizi. Nessun altro costruttore o implementazione DAO nel progetto |
| UI / messaggi | Azioni cliente preventivo/noleggio/test drive e azione venditore cambio destinazione esaminate. Le firme delle azioni restano uguali; motivi di dominio restituiti dopo rollback, secondo il precedente del servizio cessioni. Viste invariate |
| Dominio TestDrive | Estremo 16:00 inclusivo anche per frazioni di secondo: 16:00:00.000001 rifiutato coerentemente col DB |
| Schema / demo / fixture | EXCLUDE su veicolo e daterange inclusivo; UNIQUE test drive e fascia oraria conservati. Demo e fixture 01–10 invariate; dati aggiuntivi solo nel DB dedicato |

L'estensione PostgreSQL `btree_gist` fornisce l'uguaglianza GiST del veicolo;
il vincolo `ex_rent_vehicle_period` protegge anche INSERT concorrenti diretti,
senza dipendere dai lock applicativi. Conferma del primo writer → secondo
rifiutato con SQLSTATE 23P01; rollback del primo → secondo ammesso.
Periodi che condividono un giorno confliggono, giorni adiacenti distinti restano
prenotabili. Riferimenti primari PostgreSQL 16:
[btree_gist trusted](https://www.postgresql.org/docs/16/btree-gist.html),
[lock di riga](https://www.postgresql.org/docs/16/explicit-locking.html#LOCKING-ROWS).

## Ambiente

[Identità/privilegi/sentinella](environment.txt), [Java e Maven](versions.txt).
OpenJDK Linux 21.0.12.1, Maven 3.9.16, PostgreSQL 16.15.
Ricontrollati prima del riuso file privato 0600 e sei variabili senza stamparle,
binari, PG_VERSION e assenza di postmaster.pid; PostgreSQL e Xvfb arrestati.
R00 temporaneo assente; riusato solo ambiente temporaneo del ticket 07.
Nessuna installazione o servizio di sistema aggiunto.

Cluster `/tmp/drivehub-ticket07-pgdata`, solo `127.0.0.1:55432`;
DB/ruolo `drivehub_test` non amministrativo e copia applicativa distinta `drivehub`
con `R00_SENTINEL`. Nessun DB applicativo preesistente contattato.
Primo avvio sandbox negato sui socket; escalation autorizzata sul solo cluster
sacrificabile. `btree_gist` installata dallo schema col ruolo test non superuser.
Credenziali fuori repository, mai copiate nelle evidenze.

## Comandi e risultati

Dalla directory esterna:
`python3 /tmp/drivehub-ticket11/maven.py <etichetta> <argomenti>`.
Il launcher deriva dal 10, usa JAVA_HOME 21, variabili private 07 e `mvn -B -o`.
I comandi ripetibili senza launcher sono in [testing.md](../../testing.md).

| Etichetta / selezione Maven | Risultato |
|---|---|
| `baseline -Ppostgres-tests clean test` | **259/259**, zero errori/failure/salti: nuova baseline locale, distinta dalla suite storica 10 |
| `rental-red -Ppostgres-tests -Dtest=BookingConcurrencyIntegrationTest test` | 1 failure attesa: due noleggi sovrapposti confermati |
| `rental-green`, stessa selezione | 1/1 |
| `client-red -Ppostgres-tests -Dtest=BookingConcurrencyIntegrationTest#sameClientCannotExceedThreeTestDrivesAcrossDifferentVehicles test` | 1 failure attesa: quattro test drive dello stesso cliente |
| `client-green -Ppostgres-tests -Dtest=BookingConcurrencyIntegrationTest test` | 2/2 |
| `mixed-red -Ppostgres-tests -Dtest=BookingConcurrencyIntegrationTest#rentalWinsAgainstATestDriveStartedBeforeItsCommit test` | 1 failure attesa: test drive incompatibile sopravvive al noleggio |
| `mixed-green -Ppostgres-tests -Dtest=BookingConcurrencyIntegrationTest test` | 3/3 |
| `constraint-red -Ppostgres-tests -Dtest=BookingConcurrencyIntegrationTest#databaseRejectsInclusiveRentalOverlapWithoutServices test` | 1 failure attesa: scrittura SQL diretta sovrapposta ammessa |
| `constraint-green -Ppostgres-tests -Dtest=BookingConcurrencyIntegrationTest test` | 4/4 |
| `price-red -Ppostgres-tests -Dtest=BookingConcurrencyIntegrationTest#rentalUsesThePriceRereadAfterTheVehicleLock test` | 1 failure attesa: addebito 20 invece di 10 dopo modifica base sotto lock |
| `price-green -Ppostgres-tests -Dtest=BookingConcurrencyIntegrationTest,PriceSelectionIntegrationTest test` | 15/15 |
| `hours-red -Dtest=BookingRulesTest test` | 1 failure attesa su 2: 16:00:00.000001 ammesso dal dominio |
| `hours-green`, stessi argomenti | 2/2 |
| `messages-red -Ppostgres-tests -Dtest=BookingConcurrencyIntegrationTest#rejectedBookingsExposeTheBusinessReasonAfterRollback test` | 2 failure attese: motivo di dominio nascosto dal wrapper transazionale |
| `messages-green -Ppostgres-tests -Dtest=BookingConcurrencyIntegrationTest test` | 7/7 |
| `cases -Ppostgres-tests -Dtest=BookingRulesTest,BookingConcurrencyIntegrationTest test` | 34/34 |
| `targeted`, selezione estesa sotto | 244/244 prima dell'ultima regressione |
| `full -Ppostgres-tests clean test` | 296/296 **intermedia**: precedente alla correzione finale del deadlock |
| `lock-order-red -Ppostgres-tests -Dtest=BookingConcurrencyIntegrationTest#purchaseCompletionAndRentalCannotDeadlockWithATestDriveOnTheBlockedVehicle test` | 1 failure attesa; tre PID/blocker osservati e PostgreSQL `deadlock detected` |
| `lock-order-green`, stessi argomenti | 1/1; rifiuto di stato osservato prima di rilasciare gli altri due servizi |
| `final-targeted`, selezione estesa sotto | **245/245**, zero errori/failure/salti |
| `final-full -Ppostgres-tests clean test` | **297/297**, zero failure/errori/salti: **47 senza DB, 250 PostgreSQL** |

Selezione estesa esatta per `targeted` e `final-targeted`:

```bash
mvn -Ppostgres-tests -Dtest=BookingRulesTest,BookingConcurrencyIntegrationTest,TestDriveCancellationIntegrationTest,BalanceConcurrencyIntegrationTest,PriceCalculatorServiceTest,PriceSelectionIntegrationTest,PurchaseAgreementIntegrationTest,PurchaseConcurrencyIntegrationTest,SaleProposalIntegrationTest,SaleOutcomeIntegrationTest,SaleTypeTest,DemoSqlValidationTest,DatabaseIsolationIntegrationTest test
```

Log `<etichetta>.log`, XML `<etichetta>-surefire/` e hash
`<etichetta>-files.sha256.json` sono conservati in questa directory.
[Identità delle otto regressioni RED/GREEN](regression-identity.json): il sorgente
completo del test è identico in ogni coppia, anche quando la selezione GREEN
include i casi già corretti. Gli ampliamenti successivi sono versioni distinte.
Il primo tentativo della diagnostica PID aveva un cast SQL mancante: corretto
il solo harness prima del RED riportato; non contato come riproduzione del bug.

## Criteri e prove

| Criterio ticket | Prova |
|---|---|
| Stessa transazione e lock coerenti | Gare pubbliche su veicolo, cliente e conto; prezzo/stato riletti dopo attesa; acconto concorrente e saldo acquisto con tre servizi; conti 04 conservati |
| Vincoli DB | Sovrapposizione inclusiva SQL diretta, due writer concorrenti commit/rollback, SQLSTATE 23P01; unicità TD 23505 e orari 23514 |
| Noleggi concorrenti | Sovrapposti rifiutati senza addebito del perdente; periodi adiacenti su due connessioni entrambi confermati; stessa data su veicoli diversi con fondi insufficienti senza sovrascrivere il saldo |
| Test drive concorrenti | Stesso cliente, veicoli diversi, conteggio iniziale due → tre; clienti diversi stesso timestamp → uno; cliente senza conto; 09:00 e 16:00 ammessi |
| Gara mista / Observer 08 | Noleggio primo → test drive rifiutato; TD primo → cancellazione prima del commit. Errore AFTER DELETE vede addebito, noleggio e cancellazione effettivi, poi rollback ripristina TD già confermato |
| Date / fondi / rollback | Estremi inclusivi, contenimento, adiacenza, oggi/passato e periodo inverso; test drive su inizio/interno/fine noleggio e fuori periodo; 19.99 insufficienti per 20 senza effetti; AFTER INSERT su rent/TD vede righe effettive e provoca rollback; regressioni 08 rieseguite |

Le nuove prove osservano servizi pubblici e nuovi AppContext; SQL aggiuntivo
prepara fixture/guasti oppure verifica esplicitamente il confine della persistenza.
Nessun mock come prova del DB. Worker/gate e connessioni distinti;
`pg_stat_activity`/`pg_blocking_pids` registrano le attese prima del rilascio.
Per il solo rifiuto anticipato della gara a tre, il completamento della Future
costituisce la sincronizzazione osservabile alternativa al terzo lock.
Nessuno sleep arbitrario; query, attese, future e terminazione worker hanno
limiti di 10 secondi e cleanup dei gate/query bloccate.

## Review indipendenti

[Standards](standards-review.md): **0 violazioni documentate,0 smell azionabili**.
[Spec](spec-review.md): **0 rilievi**. Due agenti indipendenti, contesti nuovi,
solo il diff fissato sopra; nessuna review da HEAD. Nessun codice/test cambiato
dopo le review o la suite finale. Documenti di chiusura successivi non sono
nuove prove runtime.

[Riepilogo XML](summary.json), [conservazione](preservation.json),
[controlli finali](integrity-checks.json), [patch finale sessione](session.patch).
Ricompilati64sorgenti principali e20file test con Java21;38 casi nuovi (2 senza DB, 
36 PostgreSQL). Tutti i576file della baseline presenti:562 intatti e14modifiche
intenzionali inclusi piano e ticket 11. Evidenze e ticket 01–10 identici.
HEAD/indice invariati. [Sentinella finale](final-environment.txt),
[arresto cluster](shutdown.txt); Xvfb non avviato. **AC05/#5 chiuso,11/20**;
R06 resta parziale per 12 e AC13 trasversale. Nessun blocco residuo del11.

## Limiti

Schema ricreabile D02: per installazioni esistenti predisporre `btree_gist`,
risolvere periodi già sovrapposti e aggiungere il vincolo prima dell'uso.
Non resettare dati applicativi; non è una migrazione storica automatica.
Nessuna vista modificata, quindi nessuna nuova prova GUI/tastiera richiesta.
Warning Maven del modello dipendenze JavaFX preesistente, senza errori javac.
Nessuna certificazione screen reader o benchmark prestazioni.

01–10 e relative evidenze preservati. Regole aggiuntive di stato, rimborsi e
noleggi in corso restano al 12; account, rendering e documentazione generale
non anticipati. Ambiente `/tmp` temporaneo da ricontrollare prima del riuso.
