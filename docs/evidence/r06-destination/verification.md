# Ticket 12 — destinazione veicolo e rimborsi

Sessione del 2 novembre 2026, solo ticket 12. Prerequisito 11 e ticket 01–11
controllati tutti `State: closed`: [prerequisiti](prerequisites.json).
Nessun commit/staging; `Relazione_SWE` esclusa.

## Versione e baseline

Prima delle modifiche: fotografia di **617 file** in
`/tmp/drivehub-ticket12-session/before`, inclusi lavoro locale del progetto,
evidenze precedenti, piano esterno e ticket 12. Esclusi `.git`, `target` e
`Relazione_SWE`. [Manifest](baseline-sha256.json), [HEAD iniziale](initial-head.txt),
[hash indice iniziale](initial-index.sha256).
Il confronto della sessione usa questa fotografia: un diff da HEAD includerebbe
impropriamente il lavoro dei ticket 01–11.

Nuova baseline **297/297**, zero failure/errori/salti:
[log](baseline.log), [XML](baseline-surefire/), [versione compilata](baseline-files.sha256.json).
Il risultato 297/297 registrato dal ticket 11 è storico e distinto da questa prova.

## Chiamanti e responsabilità

| Confine | Intervento e verifica |
|---|---|
| `RentalService.changeVehicleRentalState` | Unico comando di cambio destinazione: target SALE/RENT, stato di partenza opposto riletto sotto FOR UPDATE; SOLD/BLOCKED/WAITING e richieste identiche rifiutati; motivi di dominio leggibili dopo rollback |
| Disponibilità noleggio | Riusa `RentDao.isVehicleAvailableForRent` per oggi/oggi sulla connessione del comando: start ≤ oggi ≤ end; blocca anche ieri→oggi e prima di cancellare qualsiasi futuro |
| `PostgresRentDao.findFutureByVehicleId` / `deleteFutureByVehicleId` | Inizio > CURRENT_DATE per anteprima e cancellazioni; confronto numero selezionato/eliminato impedisce successo dopo DELETE saltate |
| Conti 04 | Elenco ordinato per ID cliente, lock e rilettura di ogni conto, rimborso dell'importo persistito senza penali; conto mancante e scritture fallite annullano l'intera transazione, anche dopo un primo accredito |
| Veicolo / booking 11 | Stesso lock fino a commit/rollback; aggiornamento veicolo richiede una riga già dal lavoro precedente. Nessun lock di processo o ordine inverso dei conti |
| `SalesmanDashboardView.handleToggleState` | Unico chiamante applicativo del comando e dell'anteprima; conferma indicativa, successo solo al ritorno, eccezioni mostrate. Viste e firme invariate; i nuovi motivi del servizio arrivano nel dialogo esistente |
| `getFutureRentsForVehicle` | Unico chiamante applicativo è la conferma della vista; nuovo test pubblico verifica che oggi e storico non siano descritti come futuri rimborsabili |
| Altri chiamanti DAO | `bookRental` e `bookTestDrive` riusano disponibilità inclusiva invariata; `OperationQueryService` legge storico/clienti. `deleteFutureByVehicleId` dei noleggi è chiamato solo dal cambio destinazione, distinto dal metodo omonimo dei test drive |
| Test preesistenti | `BalanceConcurrencyIntegrationTest` chiama il comando in rimborsi concorrenti, conto mancante, rimborsi ripetuti allo stesso conto e rollback; invariato e rieseguito |
| Observer 08 / AppContext / schema / demo | Invariati. La gara booking-prima verifica cancellazione dei test drive futuri; nessun falso evento per il solo cambio destinazione. Nessuna nuova penale o modifica alla formula |

## Ambiente ricontrollato

[Identità, privilegi e sentinella](environment.txt), [Java/Maven](versions.txt),
[avvio PostgreSQL](postgres-startup.log), [Xvfb](xvfb.log).
File privato 0600 con sei variabili verificato senza stamparle; URL dedicati,
Java21 Linux e PG_VERSION 16 controllati prima del riuso. Cluster inizialmente fermo.
OpenJDK 21.0.12.1, Maven 3.9.16, PostgreSQL 16.15, dipendenze Maven offline.

Riusato solo `/tmp/drivehub-ticket07-pgdata`: avvio sandbox fallito per socket;
un avvio senza override è fallito per directory socket di sistema mancante.
Avvio definitivo autorizzato con host 127.0.0.1, porta **55432** e socket in /tmp.
Solo `drivehub_test`/ruolo non amministrativo per scritture, più copia applicativa
sacrificabile `drivehub` per la sentinella. Nessun DB applicativo preesistente
contattato, nessuna installazione di sistema. Credenziali mai copiate nelle prove.

Xvfb del ticket 10 ricontrollato: primo binario non trovava `/usr/bin/xkbcomp`;
riusato il binario temporaneo `/tmp/t10/Xvfb` con percorso corretto già predisposto.
Display **:98**, 1440×1000, senza TCP, rendering software. Warning EGL su /dev/dri
non impediscono le prove JavaFX. Ambiente temporaneo da ricontrollare al riuso.

## TDD e verifiche

Launcher locale: `python3 /tmp/drivehub-ticket12-session/maven.py <etichetta> <argomenti>`;
usa Java21, variabili private e `mvn -B -o`. Comandi senza launcher in [testing.md](../../testing.md).
Ogni esecuzione conserva log, XML Surefire e hash di tutti i sorgenti, schema,
fixture e POM effettivamente compilati.

| Etichetta / argomenti Maven | Esito |
|---|---|
| `baseline -Ppostgres-tests clean test` | 297/297, zero failure/errori/salti |
| `states-red -Ppostgres-tests -Dtest=VehicleDestinationIntegrationTest test` | 8 failure attese: stati non riapribili e transizioni identiche accettati |
| `states-green`, stessi argomenti | 8/8 |
| `dates-red -Ppostgres-tests -Dtest=VehicleDestinationIntegrationTest#rentalIncludingTodayPreventsSaleAndAllRefunds test` | 4 failure attese: noleggi di oggi ignorati |
| `dates-green -Ppostgres-tests -Dtest=VehicleDestinationIntegrationTest test` | 12/12 |
| `future-red -Ppostgres-tests -Dtest=VehicleDestinationIntegrationTest#futureRentalPreviewExcludesTodayAndHistory test` | 1 failure attesa: oggi incluso fra i futuri |
| `future-green -Ppostgres-tests -Dtest=VehicleDestinationIntegrationTest test` | 13/13 |
| `cancellation-red -Ppostgres-tests -Dtest=VehicleDestinationIntegrationTest#skippedCancellationCannotCommitRefundsOrSale test` | 1 failure attesa: DELETE saltata lasciava un noleggio e permetteva SALE |
| `cancellation-green -Ppostgres-tests -Dtest=VehicleDestinationIntegrationTest test` | 14/14 |
| `refund-cases -Ppostgres-tests -Dtest=VehicleDestinationIntegrationTest test` | 26/27, un errore del nuovo test: il getter di un conto mancante lancia, anziché restituire null; corretta solo l'aspettativa del test |
| `race-cases -Ppostgres-tests -Dtest=VehicleDestinationIntegrationTest test` | **33/33**, zero failure/errori/salti |
| `targeted`, selezione estesa sotto | **278/278**, zero failure/errori/salti |
| `final-full -Ppostgres-tests clean test` | **330/330**, zero failure/errori/salti; 47 senza DB e 283 PostgreSQL |
| `calendar-red`, prova esplorativa poi rimossa | Aspettativa non valida: il driver JDBC allinea il fuso della sessione al default JVM; cambiare quel default spostava anche il giorno DB, senza simulare due clock divergenti. Non è una riproduzione del prodotto né una coppia RED/GREEN |

Le quattro coppie [RED/GREEN hanno il sorgente del test identico](regression-identity.json)
in ciascuna fase. Il GREEN include anche i casi precedenti; gli ampliamenti
successivi hanno hash distinti. La versione finale del test coincide con quella
dei mirati 278/278. Le gare già protette dal lock 11 non sono presentate come
nuovi difetti riprodotti in RED.

Selezione esatta di `targeted`:

```bash
mvn -Ppostgres-tests -Dtest=VehicleDestinationIntegrationTest,BookingRulesTest,BookingConcurrencyIntegrationTest,TestDriveCancellationIntegrationTest,BalanceConcurrencyIntegrationTest,PriceCalculatorServiceTest,PriceSelectionIntegrationTest,PurchaseAgreementIntegrationTest,PurchaseConcurrencyIntegrationTest,SaleProposalIntegrationTest,SaleOutcomeIntegrationTest,SaleTypeTest,DemoSqlValidationTest,DatabaseIsolationIntegrationTest test
```

I 33 casi nuovi verificano i due versi ammessi, tutti gli stati non riapribili,
target invalidi/null, veicolo assente, date inclusive, futuro da domani, storico e
altro veicolo. Rimborsi 100,25 + 50 al cliente 1 e 200,75 al cliente 2, senza penali.
Trigger PostgreSQL verificano cancellazioni e primo accredito prima di fallire
sul secondo conto o sull'ultimo aggiornamento. Un trigger AFTER sul veicolo vede
entrambi gli accrediti, tutte le cancellazioni e l'aggiornamento effettivo prima
dell'errore. La sequenza diagnostica nel caso conto mancante conferma che il primo
accredito era stato realmente scritto. Nuovi AppContext verificano il rollback.

Le gare usano connessioni distinte e un gate SQL sul conto 1, con PID distinti e
`pg_blocking_pids` osservati prima del rilascio. Cambio-prima → booking rifiutato;
booking-prima futuro → rimborso; booking-prima oggi → cambio rifiutato;
cambio fallito dopo scritture → booking confermato. Due cambi concorrenti non
rimborsano due volte. Nessuno sleep nelle gare; query, attese, future e cleanup
hanno timeout. Nessun mock certifica persistenza o concorrenza.

## GUI e tastiera

[Harness](DestinationUiCheck.java), [launcher](run-ui.py), [log](ui.log):
**UI CHECK SUCCESS**, seed/check in JVM distinte sul solo DB dedicato.
Riutilizzati Robot 05/06/10 e costruttori pubblici delle viste. Frecce per schede
e righe, Tab per il pulsante, Spazio per attivarlo, Invio per confermare.
Le pause degli helper Robot preesistenti attendono il rendering, mai le gare DB.

Cinque schermate ispezionate: [conferma](future-confirmation.png),
[rimborso](refunded.png), [noleggio in corso](ongoing-rejected.png),
[vista obsoleta BLOCKED](stale-rejected.png), [conto mancante](refund-rollback.png).
Confermati saldi 1100,25/1200,75 dopo rimborso, passaggio inverso SALE→RENT e
rollback della destinazione e di entrambi i noleggi quando manca il conto 2.
Nessuna nuova penale. Nessuna certificazione screen reader o benchmark prestazioni.

## Chiusura

Suite completa finale **330/330**, inclusi fixture, isolamento/sentinella e
regressioni 04–11: [log](final-full.log), [XML](final-full-surefire/),
[hash degli input runtime](final-full-files.sha256.json), [riepilogo](summary.json).
Nessun codice/test modificato dopo la suite.
Review indipendenti: [Standards](standards-review.md) **0 violazioni/0 smell
azionabili**, [Spec](spec-review.md) **0 rilievi**. Due agenti con contesti nuovi,
solo [diff della sessione](review.patch), nove file; SHA256
`cc5a9ebc798368e98d841ea8c4bb7f5a7efc139dec5cca0483ddacfa80fd0368`.
[Elenco](review-files.txt), [hash snapshot](review-files.sha256.json).
Nessun codice/test/harness cambiato dopo prove finali e review; documenti di
chiusura successivi non costituiscono nuove prove runtime.

[Conservazione](preservation.json): 617 file iniziali presenti, 612 identici e
cinque aggiornamenti intenzionali (servizio, DAO noleggi, testing, piano e ticket12),
zero eliminazioni. Evidenze precedenti intatte; HEAD/indice invariati.
[Controlli](integrity-checks.json), [stato ambiente alla ripresa](shutdown.txt).
Il ricontrollo finale della sentinella è terminato con exit0 prima della ripresa;
il suo file temporaneo non è sopravvissuto. La prova persistente di isolamento e
sentinella rimane negli XML/log della suite finale. Alla ripresa il cluster
/tmp precedente era assente e non risultavano processi postgres/Xvfb residui;
non si attribuisce un arresto esplicito a un comando non eseguito.

**6/6 criteri verificati; ticket12 chiuso.** R06 completata per08/11/12,
AC07/#7 chiuso; AC13 resta trasversale. Nessun ticket successivo, nessun commit.
Ambiente di prova temporaneo da ricreare prima di nuove esecuzioni.
