# Ticket 07 — richiesta di acquisto elaborata una sola volta

Verifica del 2 novembre 2026. **Ticket 07 chiuso, 6/6 criteri verificati.**
Ticket 04 controllato chiuso prima degli interventi. **Ticket 06 ancora aperto in
verifica finale**: il passaggio al 07 è stato autorizzato esplicitamente dall'utente,
non dipende dalla chiusura del 06 e non la determina.

## Baseline e perimetro

HEAD `c2a81122ee6be44760ded4160fde229bd33e837c`, nessun commit della sessione.
Prima delle modifiche sono stati copiati i 272 file correnti tracciati/non ignorati
in `/tmp/drivehub-ticket07-baseline/repo`, escludendo esplicitamente
`Relazione_SWE/**`; acquisiti anche piano, ticket 07 e ticket 06 esterni.
Il [manifest](baseline-sha256.json) conserva gli hash della baseline, HEAD e indice.
Il [diff dedicato](session.patch), 650 righe, riguarda solo i [7 file elencati](files.txt):
4 sorgenti principali, 2 documenti e 1 nuovo test. Non è il diff cumulativo da HEAD.

[Conservazione](preservation.json): 266/272 file preesistenti identici; i 6 aggiornati
sono intenzionali e il confronto preserva il lavoro locale precedente, incluso il 06.
Ticket 06 esterno, HEAD e indice Git invariati. Nessuno staging, commit, push o
pubblicazione. `Relazione_SWE/**` esclusa da letture, ricerche, copie e verifiche.

## Mappa e correzione

- Saldo, rifiuto, approvazione e scadenza: tutti i chiamanti in `PurchaseService`
  adottano `RequestDao.findByIdForUpdate` nella connessione transazionale.
  L'unico implementatore, `PostgresRequestDao`, usa `FOR UPDATE` e rifiuta autocommit.
  La protezione è disponibile al futuro ticket 10, senza modificarne ora i flussi.
- Chiamanti pubblici controllati: azioni cliente e venditore, controllo scadenze
  all'accesso, letture `OperationQueryService`, composizione `AppContext`, prove
  PostgreSQL dei ticket precedenti. Le firme dei servizi restano invariate.
  Nessuna modifica necessaria a viste, schema, dominio o fixture applicative.
- Tipo ricontrollato; saldo soltanto con venditore e scadenza valida. Gli esiti
  economici richiedono transizione atomica del veicolo da `BLOCKED`, senza riscrivere
  dati commerciali letti prima. Zero righe provoca errore e rollback.
- L'elenco iniziale delle scadenze fornisce solo ID. Ogni richiesta viene riletta
  sotto lock; tipo, cliente, approvazione e data sono ricontrollati. Una richiesta
  già consumata/non più scaduta viene saltata, senza penale o notifica duplicata.
- `PostgresRequestDao.update/delete` e `PostgresBankAccountDao.updateBalance`
  richiedono una riga modificata. Inserimenti richiesta/vendita già controllano
  l'ID restituito. Mappati anche tutti gli altri chiamanti dei DAO condivisi:
  AuthService, RentalService e SaleToDealershipService; suite precedente preservata.
- Il protocollo conti del ticket 04 e il totale concordato persistito del 06
  rimangono invariati. Penale e rimborso conservano le regole esistenti;
  la notifica usa esattamente il rimborso scritto, anche per acconti di pochi centesimi.

## Ambiente e comandi effettivi

[Versioni e identità verificate](environment.txt): OpenJDK Linux 21.0.12.1,
Maven 3.9.16, PostgreSQL 16.15. All'inizio erano presenti solo Java 17 e Maven;
server, launcher, credenziali e vecchi file R00 in `/tmp` non esistevano.
Pacchetti ufficiali Ubuntu scaricati ed estratti solo in
`/tmp/drivehub-ticket07-tools/root`, senza installazioni/servizi di sistema.
Il tentativo di download nella sandbox falliva per DNS; download IPv4 riusciti
con escalation autorizzata. I symlink di configurazione del JDK estratto sono
stati risolti nella stessa directory temporanea prima della compilazione riuscita.

Cluster nuovo `/tmp/drivehub-ticket07-pgdata`, solo `127.0.0.1:55432`.
Ruolo/database `drivehub_test` non amministrativo, reset protetti da `TestDatabase`;
nuova copia distinta `drivehub` con `R00_SENTINEL`. Nessun contatto al database
applicativo preesistente. Non sono state eseguite suite simultanee sul DB dedicato.
Credenziali generate e launcher fuori repository; file segreti 0600 e nessuna
password privata nelle evidenze. Cluster **arrestato** con `pg_ctl -m fast -w stop`.

Il launcher temporaneo `python3 /tmp/drivehub-ticket07-maven.py <etichetta> <argomenti>`
imposta JAVA_HOME, legge le sei variabili dal file privato e invoca `mvn -B -o`.
Da ambiente predisposto, gli argomenti della tabella sono i comandi Maven ripetibili.
Log completi conservati qui; il launcher non è un prerequisito del progetto.

| Etichetta / argomenti Maven | Esito |
|---|---|
| `compile -Dtest=TestDatabaseConfigurationTest test` | 7/7, Java 21; [log](compile.txt) |
| `red -Ppostgres-tests -Dtest=PurchaseConcurrencyIntegrationTest#concurrentCompletionsChargeAndSellOnlyOnce test` | 1 failure attesa: anche la seconda sessione conclude la richiesta; [log](red.txt), [XML](red.xml) |
| `green` con gli stessi argomenti della regressione | 1/1 dopo la correzione; [log](green.txt) |
| `races -Ppostgres-tests -Dtest=PurchaseConcurrencyIntegrationTest test` | primi 10 casi concorrenti, 10/10; [log](races.txt) |
| `targeted -Ppostgres-tests -Dtest=PurchaseConcurrencyIntegrationTest,PurchaseAgreementIntegrationTest,BalanceConcurrencyIntegrationTest test` | prima estensione 83/83; [log](targeted.txt) |
| `final-targeted` con gli stessi argomenti | **88/88**, inclusi 60 casi 07, 12 accordo 06 e 16 conti 04; [log](final-targeted.txt) |
| `full -Ppostgres-tests clean test` | **163/163**, 0 failure/errori/saltati; 40 senza DB e 123 PostgreSQL; [log](full.txt), [XML](surefire/), [riepilogo](summary.json) |

Ricompilati 63 sorgenti principali e 14 sorgenti test, target 21 con `-Xlint:all`.
Superati anche configurazione sicura, fixture ripetibili e sentinella applicativa.
Rimane il warning Maven preesistente sul modello delle dipendenze JavaFX (6 problemi).
[Hash dei file verificati](tested-files.sha256); nessun codice/test cambiato dopo
la suite completa e le revisioni indipendenti.

## Requisiti e prove osservabili

Tutte le prove in `PurchaseConcurrencyIntegrationTest` passano da servizi pubblici
con PostgreSQL reale; le query aggiuntive preparano fixture/guasti e osservano lock.
Due sessioni si fermano su gate SQL, con PID distinti e `pg_blocking_pids` osservati
prima del rilascio. Nessun mock o sleep. Query, attese e worker limitati a 10 secondi;
cleanup con rilascio gate e cancellazione delle query ancora bloccate.

| Criterio | Prove eseguite |
|---|---|
| Lock condiviso e rilettura transazionale | `concurrentCompletionsChargeAndSellOnlyOnce`, `requestLockRequiresAnActiveTransaction` |
| Saldo/rifiuto e richieste consumate | `competingPhasesConsumeTheRequestOnce`: due ordini, rifiuto/rifiuto, rifiuto/scadenza, approvazione obsoleta |
| Saldo/scadenza e dati pre-transazione obsoleti | `completionAndExpirationRecheckTheLockedDeadline`, `staleExpirationCandidateCannotRefundACompletedOrExtendedRequest`: confine oggi/ieri e proroga nei due ordini |
| 4000 → penale 800 / rimborso 3200 una volta | `competingPhasesConsumeTheRequestOnce`: scadenza/scadenza e ripetizione dopo commit; un solo record penale, nessuna vendita |
| Approvazione/tipo/scadenza | `incompleteApprovalCannotEnableCompletionOrExpiration`, `purchaseCommandsRejectSaleRequests`; test di scadenza e approvazione del 06 preservati |
| Rollback coerente | `failedOrSkippedWritesRollBackTheWholePhase`: 24 casi errore/scrittura saltata su ogni fase e scrittura; `inconsistentVehicleStateRollsBackTheWholePhase` (12), `missingAccountPreventsEveryWrite` (3) |
| Messaggio e centesimi | `expirationUsesTheRefundActuallyWrittenInTheNotification`: acconto 0,03 → penale 0,01 e rimborso/notifica 0,02 |

Riletti da nuovi AppContext conto, accordo, richiesta, veicolo e registro. Verificati
vendita unica da 20000 e saldo 30000 da 50000 iniziali; rifiuto con rimborso integrale
riporta 50000 senza vendite; scadenza lascia 49200 con sola penale 800. Il controllo
ripetuto non cambia di nuovo conto/registro. Guasti dopo i movimenti effettivi e
scritture con zero righe annullano l'intera fase, lasciando accordo e prenotazione.

## Standards

[Rapporto indipendente](standards-review.md): **0 violazioni documentate**, un
possibile Duplicated Code non bloccante fra i coordinatori delle gare dei ticket
04 e 07. Non estratto un helper comune: i gate differiscono (solo conto rispetto
a richiesta/conto/proroga) e il refactor non è richiesto per la correzione.

## Spec

[Rapporto indipendente](spec-review.md): **0 rilievi** su requisiti mancanti/parziali,
implementazione errata o scope creep. L'esito runtime è certificato dalla suite
principale sopra, non dalla sola revisione statica.

Totale review: Standards 0 violazioni / 1 osservazione a bassa priorità;
Spec 0 rilievi. Nessun rilievo bloccante rimasto.

## Limiti e prossimo passo

Nessuna UI modificata o verifica grafica in questa sessione. Non anticipati
cancellazione atomica dei test drive/Observer (ticket 08), cessione (09/10),
prenotazioni e transizioni degli altri flussi. Il rollback certificato qui riguarda
le scritture della fase economica 07; la notifica Observer dopo commit conserva
il limite già assegnato al ticket 08. Nessuna nuova regola economica o migrazione.
Ambiente `/tmp` temporaneo: se rimosso, ripredisporlo secondo `docs/testing.md`.

Chiusi AC06/rilievo #6 nel perimetro del 07; AC13 resta trasversale e parziale.
R04 resta parziale: **06 aperto in verifica finale, 07 chiuso, 08 non avviato**.
Prossimo passo: riprendere la verifica finale del 06; altri ticket soltanto su
successiva richiesta. Nessun lavoro del 06 chiuso implicitamente.
