# Riscontro tecnico e storico integrato

17 novembre 2026, HEAD `61ce18ae1d4e0458b63e970b793b0b3a634a3adf`.
Il censimento [fonti.json](fonti.json) identifica 281 fonti pertinenti già
usate nei ticket; [intervalli-fonti.json](intervalli-fonti.json) e
[estratti-fonti.txt](estratti-fonti.txt) conservano 28 intervalli numerati
leggibili. La lettura è **S0**, non un test eseguito. I 42 blocchi correnti
del corpo sono confrontati letteralmente con Java o comandi documentati in
[frammenti-correnti.json](frammenti-correnti.json); [pagine-frammenti.json](pagine-frammenti.json)
ne verifica anche la presenza completa nel PDF.

| Affermazione decisiva | Preparazione, ordine e oracolo letti | Confine |
| --- | --- | --- |
| Totale fissato all’acconto, tre transazioni | `PurchaseService:65–233`: transizione condizionale, rilettura veicolo, calcolo e acconto; approvazione separata; saldo da agreedTotal e deposit, registro ed evento prima del commit. `PurchaseAgreementIntegrationTest:26–78`: fixture, modifica listino, nuovo contesto, saldo e riletture. | Modelli/sconti iniziali possono essere letti autonomamente; non si promette snapshot unico del listino né atomicità fra sessioni. |
| Noleggio | `RentalService:59–178`: date, lock veicolo/cliente, disponibilità, modello/sconti sulla Connection, calcolo, conto, scritture ed evento. `PriceSelectionIntegrationTest:25–30,61–69`: preparazione e confronto della formula. | Preventivo diverso dalla conferma; periodi inclusivi, vincolo EXCLUDE e rollback della singola transazione. |
| Cessione e registro | `SaleToDealershipService:55–182`: VIN, stato/titolarità/storico, stima ricalcolata e PURCHASE senza pagamento iniziale; esito separato, accredito e registro positivi. `SaleTypeTest:16–71` distingue i tre tipi. | Zero persistibile diverso da accredito accettato dal conto; nessuna vecchia ACQUISITION o stima negativa presentata come attuale. |
| Observer transazionale | `VehicleEventPublisher:28–32`, `TestDriveCancellationObserver:15–26`, `DatabaseManager:49–106`: chiamata sincrona sulla stessa Connection, poi commit/rollback del gestore. `TestDriveCancellationIntegrationTest:25–181`: fixture, trigger AFTER DELETE dopo addebito e scritture, assert e riletture di saldo/stato/richiesta/test drive. | Nessuna coda, outbox o consegna universale; la prova selettiva non copre ogni possibile errore JDBC. |
| Concorrenza e guasti | `PurchaseConcurrencyIntegrationTest:35–85,172–179,267–355`: preparazione, race/attese, side effect prima dell’errore, assert finali e helper. | Gate e query con timeout, connessioni distinte; non dimostra assenza generale di deadlock. |
| Identità | `AuthServiceIntegrationTest:19–104`: fixture, email duplicata, password, assert e helper; `IdentityDeletionIntegrationTest:309–323`: stato prima/dopo. `PostgresUserDao:215–291` e `schema.sql:36–39,147–310`: vincoli locali/trigger. | Nessuna unicità globale/concorrenza certificata, nessuna autorizzazione universale dedotta dal ruolo UI. |
| Mapping DAO | `PostgresRequestDao:147–168,214–227`, enum SQL e ADR 0002; 42 estratti riscontrati nelle loro fonti. | Lettura statica dei mapper diversa da assert diretti su ogni attributo o combinazione null. |
| Conto e importi | `PriceCalculatorService:26–111`, ADR 0001, esempi del corpo e `PriceCalculatorServiceTest:93–166`. | Formula didattica, arrotondamento, tolleranza di 0,02 dove effettivamente asserita; nessuna stima di mercato. |
| Tabelle e avvio | `AsyncLoad:23–75`, `App:34–122`: Task, risultato corrente, disposal e navigator anonimo. | Letture asincrone, non tutte le scritture; nessuna nuova prova GUI, screen reader o benchmark. |
| Demo | `DemoSqlValidationTest:16–51`: snapshot ordinati prima/dopo tredici tabelle e sette sequenze. | Assert storico non certificazione di conservazione di qualsiasi DB. |

Le sei schede principali mantengono preparazione, flussi, alternative e
paragrafi “Prove e portata” in 2.2; A aggiunge nove schede senza duplicarle.
B mantiene undici package e 65 tipi principali con membri selezionati;
CL-08a/b separa contratti e gestore dello stesso package. C associa S24–39,
casi, metodi/assert e limiti senza trasformare i conteggi in coverage.
I confronti analitici 09–12 restano fonti del trattamento dei singoli
argomenti; la conservazione byte per byte di quei sorgenti e delle relative
148 pagine impedisce di attribuire al 13 riscritture non avvenute.

## Risultati storici

[riscontro-storico.json](riscontro-storico.json) rilegge i log/XML originali:
E1 del 9 novembre: 423 riusciti; mirato 82; RED contratto 4 con 2 failure e
DAO 81 con 77 errori, sessioni distinte. E1 conserva log per classe, senza
manifest runtime o XML per metodo; `git diff` tra il tree fissato dalla
review e HEAD è vuoto per `src`/POM, non prova dei byte runtime eseguiti.
E2 del 3 novembre: 394 riusciti su 22 classi; mirato 363 con 4 errori al
primo tentativo e 363 riusciti al secondo; oggi 28 input differiscono dal
manifest. Sentinella finale di tredici tabelle e sette sequenze invariata.
E3 distingue sette marcatori di successo, originali e copie adattate,
tentativi SaleOutcome/Destination falliti, prima fase Xvfb e mancanza della
sentinella esterna finale. Il vecchio `SaleType.ACQUISITION` resta
incompatibile con l’enum corrente, senza modificarne l’harness.

Versioni, date, comandi e ambienti restano nel capitolo 4/C e nelle fonti
[fonti-storiche.json](fonti-storiche.json); non sono stati riprodotti.
Non si sommano le sessioni e non si ricava copertura strutturale.

## Limiti e difetti separati

I rapporti `difetti-applicativi.md` dei ticket 04–05 restano intatti e sono
richiamati in C.3: zero negli importi, email non globalmente univoche e
letture UI dopo commit nello stesso try. Il ticket 13 non li corregge né
li dichiara risolti. Nessun nuovo difetto applicativo è stato dimostrato.
Il controllo documentale non estende le garanzie di prove selettive e non
attribuisce approvazione agli autori.
