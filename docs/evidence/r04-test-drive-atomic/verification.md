# Ticket 08 — vendita/noleggio e test drive atomici

Verifica del 2 novembre 2026: **5/5 criteri soddisfatti, ticket 08 chiuso**.
Prerequisito 04 e precedenti 01–07 controllati tutti `State: closed`.
Implementato soltanto 08. Nessun commit/staging; Relazione_SWE sempre esclusa.

## Versione e baseline

HEAD `c2a81122ee6be44760ded4160fde229bd33e837c` e indice invariati.
Prima delle modifiche acquisiti 352 file locali in
`/tmp/drivehub-ticket08-baseline`, inclusi documenti esterni e tutto il lavoro
preesistente. Esclusi `.git`, `target` e Relazione_SWE.
[Manifest della baseline](baseline-sha256.json), [patch della sola sessione](session.patch),
[13 file revisionati](files.txt), [conservazione](preservation.json).
Il confronto da HEAD includerebbe i ticket 01–07 e non è stato usato per la review.

340 file preesistenti identici; 10 aggiornati intenzionalmente e 2 vecchie
interfacce evento eliminate, sostituite da 2 nuove. Aggiunto un test. I ticket
01–07 e tutte le loro evidenze storiche restano identici. La patch è stata
verificata con `git apply --check --reverse -p2`; whitespace controllato.
[Hash della versione finale verificata](tested-files.sha256),
[controlli finali](integrity-checks.json).

Il [sorgente della regressione iniziale](regression-source.java.txt) e i
[manifest RED/GREEN](regression-files.sha256.json) distinguono la baseline, la prima
correzione e l'ampliamento dei test. I soli due commenti DAO/AppContext aggiunti
dopo il primo GREEN sono esclusi dagli hash di quel GREEN; la suite finale
ricompila la versione attuale completa. Nessun codice/test cambiato dopo suite
completa e review; documenti di chiusura successivi non sono nuove prove runtime.

## Chiamanti e responsabilità

| Percorso | Verifica/correzione |
|---|---|
| `PurchaseService.completePurchase` | Lock/rilettura richiesta 07, saldo da accordo 06, conto 04, vendita, richiesta consumata e stato SOLD conservati; notifica spostata prima del commit sulla stessa connessione |
| `RentalService.bookRental` | Prezzo/formula e lock conto conservati; salvataggio e notifica prima del commit; nessuna finta transizione RENT → RENT |
| Eventi/publisher | `VehicleCommitmentEvent` segnala un impegno commerciale; `VehicleCommitmentObserver` riceve la connessione; publisher sincrono senza catch che nasconda errori |
| `TestDriveCancellationObserver` | Mantiene la politica commerciale comune a vendita/noleggio; usa il DAO sulla connessione ricevuta, senza possedere un DatabaseManager o aprire transazioni; rimosso il log prematuro di successo |
| DAO/composizione | Riutilizzato `PostgresTestDriveDao.deleteFutureByVehicleId`, query ed errori propagati invariati; contratto temporale documentato; AppContext registra la politica una volta e la inietta in entrambi i servizi |
| Test drive/letture | `TestDriveService` e `OperationQueryService` osservano storico e prenotazioni da nuovi AppContext; booking/cancellazione manuale invariati |
| UI/messaggi | Azioni cliente acquisto/noleggio e test drive, azioni venditore e chiamanti DAO controllati; successo mostrato soltanto se il servizio ritorna, errore già gestito. Nessuna vista o stringa UI da modificare |
| Fixture/configurazione | Nuove fixture/trigger solo nel DB dedicato. Schema, demo, fixture precedenti, formula/base/centesimi 05, accordo 06 e protocolli 04/07 invariati |

Observer disaccoppia la politica sui test drive dai due servizi commerciali;
il percorso è obbligatorio e transazionale, senza notifiche differite o
infrastruttura distribuita. Gli eventi non simulano una variazione dello stato
quando viene confermato un noleggio su un veicolo già RENT.

## Ambiente e comandi eseguiti

[Ambiente](environment.txt): OpenJDK Linux 21.0.12.1, Maven 3.9.16, PostgreSQL 16.15.
File R00 assenti: verificata invece la disponibilità di JDK/binari, cluster,
launcher e file privato del 07 prima del riuso. Nessuna nuova installazione.
Il launcher temporaneo 08 deriva dal 07, usa Java 21, `mvn -B -o` e le sei
variabili private del 07 senza stampare segreti.

Cluster `/tmp/drivehub-ticket07-pgdata`, solo `127.0.0.1:55432`;
DB/ruolo `drivehub_test` e copia applicativa distinta `drivehub` con sentinella.
Identità, versione e sentinella ricontrollate in sola lettura. Nessun accesso al
DB applicativo preesistente. Prima partenza sandbox fallita per socket non
consentiti; avvio e prove poi riusciti con escalation autorizzata sul solo
ambiente sacrificabile. Credenziali fuori repository, file 0600.

Dalla directory esterna: `python3 /tmp/drivehub-ticket08-maven.py <etichetta> <argomenti>`.
Gli argomenti Maven sono ripetibili con le variabili di `docs/testing.md`.

| Etichetta / argomenti | Esito | Prova |
|---|---|---|
| `red -Ppostgres-tests -Dtest=TestDriveCancellationIntegrationTest test` | 2 failure attese: servizi restituiscono successo nonostante l'errore PostgreSQL | [log](red.log), [XML](red.xml) |
| `green`, stessi argomenti e stessa regressione | 2/2 | [log](green.log), [XML](green.xml) |
| `cases`, stessi argomenti dopo ampliamento | 8/8 | [log](cases.log) |
| `targeted -Ppostgres-tests -Dtest=TestDriveCancellationIntegrationTest,PurchaseConcurrencyIntegrationTest,PurchaseAgreementIntegrationTest,BalanceConcurrencyIntegrationTest,PriceCalculatorServiceTest,PriceSelectionIntegrationTest test` | 117/117 | [log](targeted.log) |
| `full -Ppostgres-tests clean test` | **171/171, zero failure/errori/saltati** | [log](full.log), [14 XML](surefire/), [riepilogo](summary.json) |

Ricompilati 63 sorgenti principali e 15 test con target 21 e `-Xlint:all`.
40 prove senza DB e 131 PostgreSQL; inclusi conti 04, prezzi 05, accordo 06,
concorrenza richieste 07, fixture ripetibili, configurazione sicura e isolamento/sentinella.
Rimane il warning preesistente del modello dipendenze JavaFX. Cluster arrestato
con `pg_ctl -D /tmp/drivehub-ticket07-pgdata -m fast -w stop` (`server stopped`).

## Requisiti e prove

Tutte le nuove prove passano dai servizi pubblici composti da AppContext;
nessun mock come prova di persistenza. SQL aggiuntivo prepara fixture e guasti.
Riletture attraverso nuovi AppContext, non riuso di oggetti pre-transazione.

| Criterio | Test / risultato |
|---|---|
| Operazione e cancellazione atomiche, errori propagati | `cancellationFailureRollsBackTheCommercialOperation`, acquisto e noleggio: trigger AFTER DELETE per statement osserva conto già addebitato, operazione già scritta, richiesta consumata/stato SOLD per acquisto e tutte le cancellazioni; poi solleva `ticket08_cancellation_failed`. Il servizio fallisce; conto, operazioni, accordo/richiesta, stato e due test drive sono integralmente ripristinati |
| Successo, storico, tutti i futuri | `confirmationCancelsEveryFutureAndPreservesHistoryAndOtherVehicles`, entrambi i servizi: ieri resta; +1/+6/+20 giorni cancellati per più clienti; noleggio +5…+7 cancella anche fuori periodo; altro veicolo resta. Acquisto 20000/4000/16000, saldo conto 10000; noleggio tre giorni 57,05 e conto 29942,95 |
| Confine temporale coerente | `onlyDatesStrictlyAfterTheOperationTransactionStartAreCancelled`, entrambi: −1 microsecondo e uguale conservati, +1 eliminato; timestamp transazionale stabile PostgreSQL. Il trigger imposta un fuso locale soltanto nella transazione per rispettare fascia 09–16 anche di notte, senza alterare clock/schema |
| Nessun futuro | `confirmationAlsoSucceedsWithoutFutureTestDrives`, entrambi: nessun falso errore e una sola operazione persistita |
| Interfacce/composizione/Observer | Mappa sopra, compilazione completa e review dei chiamanti: nessun percorso obbligatorio dopo commit, nessuna seconda transazione |

## Standards

[Review indipendente](standards-review.md): **0 violazioni, 0 smell azionabili**.

## Spec

[Review indipendente](spec-review.md): **0 rilievi**.

Entrambi i revisori hanno usato contesti nuovi e la stessa patch fissata
SHA256 `52afb2fc12e7df429001b9c6a7cb78250ff672861b7324d47805513055a95f6c`.
Hanno verificato codice connesso e prove conservate senza rilanciare suite/DB.

## Limiti e prossimo passo

Nessuna vista modificata, quindi nessuna nuova verifica JavaFX/tastiera.
Non certificati conflitti concorrenti delle prenotazioni (ticket 11), altre
transizioni, prestazioni o screen reader. Nessuna nuova regola economica,
migrazione o infrastruttura; schema e condizioni dei ticket precedenti invariati.
Ambiente `/tmp` temporaneo, da verificare prima del prossimo riuso secondo
[testing.md](../../testing.md); cluster arrestato.

R04 completata per 06–08. R06/AC12 restano parziali per gli altri comportamenti;
AC13 trasversale, numero rilievi integralmente chiusi invariato a 7/20.
Nessun ticket successivo avviato; eventuale prosecuzione richiede una nuova
richiesta. Nessun blocco residuo del ticket 08.
