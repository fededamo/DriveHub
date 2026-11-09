# Build e test isolati

La conversione del ticket 03 è stata applicata all'installazione locale e lo
script, con i relativi test dedicati, è stato ritirato su richiesta. Le prove
precedenti restano storiche e riproducibili dal commit `d31a1ce`.
[Risultati della verifica corrente](evidence/db-consolidation-2026-11-09/verification.md).
La suite corrente verifica il contratto finale su PostgreSQL dedicato.

```bash
mvn -Ppostgres-tests clean test
# Con JDK 21, DISPLAY isolato e variabili DB di test:
python3 docs/evidence/db-consolidation-2026-11-09/run-ui.py
```

Non eseguire harness e suite contemporaneamente sullo stesso database.

Per il contratto delle richieste del ticket 01, comandi, risultati e limiti della
nuova esecuzione sono in [richieste della concessionaria](evidence/request-semantics-01/verification.md).
Le richieste usano `SALE` per l'acquisto del cliente e `PURCHASE` con stima non
negativa per la cessione. Il ticket 02 allinea il registro a `SALE`/`PURCHASE`/`PENALTY`
con importi non negativi; [prove correnti del registro](evidence/sale-semantics-02/verification.md).
Le evidenze delle revisioni precedenti conservano i significati allora in uso.

Richiesti **JDK 21**, Maven 3.9+ e, per le prove di persistenza, PostgreSQL 16.
`java -version` e `mvn -version` devono entrambi indicare Java 21; impostare
`JAVA_HOME` al JDK 21 Linux quando si lavora in WSL. Il JDK Windows non sostituisce
un JDK Linux se l'interoperabilità WSL non è disponibile. Il requisito del POM resta 21.

## Comandi

Dalla radice del progetto:

```bash
java -version
mvn -version
mvn test
```

`mvn test` compila il progetto e avvia solo i test senza database: dominio, prezzi e
rifiuto delle configurazioni non sicure. I test con tag `postgres` sono esclusi;
il risultato non costituisce verifica di DAO, fixture, transazioni o flussi persistenti.
Il plugin resources è fissato a 3.3.1 per evitare che la versione implicita cambi con Maven.
La prima esecuzione richiede accesso a Maven Central se le dipendenze non sono già in cache.

## PostgreSQL dedicato

Predisporre una sola volta, da amministratore PostgreSQL, **un nuovo database** e
un ruolo senza privilegi amministrativi. Nessun test crea o ricrea database/ruoli.

```sql
CREATE ROLE drivehub_test LOGIN NOSUPERUSER NOCREATEDB NOCREATEROLE NOREPLICATION NOBYPASSRLS;
-- In psql, impostare la password senza inserirla nel file o nella cronologia SQL:
\password drivehub_test
CREATE DATABASE drivehub_test OWNER drivehub_test;
REVOKE CONNECT ON DATABASE drivehub_test FROM PUBLIC;
GRANT CONNECT ON DATABASE drivehub_test TO drivehub_test;
```

Il ruolo deve possedere solo questo database, non appartenere ad altri ruoli e non
ricevere accesso alle tabelle applicative. Mantenere lo schema predefinito `public`.
L'applicazione continua a leggere `src/main/resources/application.properties`;
non configurarla con il database o il ruolo di test.

Configurazione richiesta, senza valori impliciti:

```bash
export DRIVEHUB_TEST_DB_URL='jdbc:postgresql://localhost:5432/drivehub_test'
export DRIVEHUB_TEST_DB_USER='drivehub_test'
read -rsp 'Password PostgreSQL test: ' DRIVEHUB_TEST_DB_PASSWORD; echo
export DRIVEHUB_TEST_DB_PASSWORD
```

L'URL accetta un singolo hostname con porta opzionale; parametri JDBC, service,
host multipli e nomi codificati sono rifiutati. Database e ruolo devono chiamarsi
esattamente `drivehub_test`. La configurazione viene validata prima della connessione;
identità del server, proprietario, schema e privilegi vengono verificati prima del reset.
Una configurazione mancante o non sicura fa **fallire** il test, senza saltarlo.

```bash
# Configurazione senza DB
mvn -Dtest=TestDatabaseConfigurationTest test
# Fixture e scadenze: solo il database dedicato
mvn -Ppostgres-tests -Dtest=DatabaseConnectionIntegrationTest,DemoSqlValidationTest,FullWorkflowIntegrationTest test
```

Ogni prova di flusso ricrea schema e demo nel database dedicato e costruisce un
`AppContext` con il relativo `DatabaseManager`, senza Singleton applicativo.
`schema.sql` inizializza/resetta le strutture; `demo.sql` svuota e ripopola i dati.
Il loader di test esegue gli script interi nella stessa transazione e tenta il rollback
su errore, senza spezzare le funzioni PostgreSQL in corrispondenza dei punti e virgola.
Non avviare due suite contemporaneamente sullo stesso database di test.

`DemoSqlValidationTest` confronta tutte le righe e le sequenze dopo due ricaricamenti
della demo e un secondo reset completo. Il test delle scadenze imposta date relative
nel solo database di test; le date storiche della demo applicativa restano invariate.
Gli altri difetti funzionali indicati nel piano non vengono dichiarati risolti da queste prove.

## Sentinella applicativa e suite completa

La prova sentinella richiede anche un database applicativo distinto, già inizializzato,
e credenziali esplicite per leggerlo. Preferire una copia applicativa su un cluster
sacrificabile, con lo stesso schema. Su quel database predisporre una volta il marchio
sentinella (aggiunta non distruttiva, senza eseguire schema/demo sui dati applicativi):

```sql
INSERT INTO public.brand (name) VALUES ('R00_SENTINEL') ON CONFLICT DO NOTHING;
```

```bash
# Sostituire URL e utente con quelli della destinazione applicativa da verificare.
export DRIVEHUB_SENTINEL_DB_URL='jdbc:postgresql://localhost:5432/drivehub'
export DRIVEHUB_SENTINEL_DB_USER='drivehub'
read -rsp 'Password PostgreSQL sentinella: ' DRIVEHUB_SENTINEL_DB_PASSWORD; echo
export DRIVEHUB_SENTINEL_DB_PASSWORD
mvn -Ppostgres-tests -Dtest=DatabaseIsolationIntegrationTest test
# Suite completa, comprende sia le prove senza DB sia tutte le prove PostgreSQL
mvn -Ppostgres-tests test
```

`DatabaseIsolationIntegrationTest` apre la connessione sentinella in sola lettura,
controlla che configurazioni applicative usate per il reset siano rifiutate e che
la sentinella resti presente dopo i reset e il ricaricamento del database di test.
URL e ruolo sentinella devono essere distinti da quelli di test. I segreti non vanno
committati né passati come parametri Maven. Alla fine si possono rimuovere le variabili
password dalla shell con `unset DRIVEHUB_TEST_DB_PASSWORD DRIVEHUB_SENTINEL_DB_PASSWORD`.

Se Java 21, Maven Central o PostgreSQL mancano, registrare il comando fallito e il
motivo nel ticket e nel piano: un test non avviato o escluso **non è una verifica superata**.

## Verifica corrente e ambiente temporaneo

Per installazione, configurazione applicativa, inizializzazione e demo facoltativa
seguire [installation.md](installation.md). Schema/demo sono distruttivi su un DB
popolato; l'avvio ordinario non li esegue. La [verifica16](evidence/r09-instructions/verification.md)
usa una copia isolata della versione locale corrente, inclusi file modificati e
non tracciati necessari: un checkout del solo HEAD non riprodurrebbe questa versione.

| Comando / controllo | Confine dimostrato | Cosa non dimostra |
|---|---|---|
| `mvn -DskipTests clean package` | Compilazione e JAR ordinario | Test eseguiti o installer autonomo |
| `mvn test` | Dominio, prezzi, configurazioni sicure senza DB | DAO, transazioni, fixture o concorrenza PostgreSQL |
| `mvn -Ppostgres-tests clean test` | Test senza DB e PostgreSQL, isolamento/sentinella se configurati | UI, altri OS o prestazioni universali |
| `mvn javafx:run` | Avvio della finestra JavaFX | Connessione DB dimostrata dalla sola schermata pubblica |
| Harness JavaFX/Robot | Flussi/assert specifici su display isolato | Certificazione screen reader o tutti i flussi grafici |

Java21, Maven sullo stesso JDK, versione del server PostgreSQL, ruoli, proprietari,
porta, sentinella e display devono essere ricontrollati a ogni sessione. I percorsi
`/tmp/drivehub-r00-*` e `ticket07/13/14/15-*` descritti nelle evidenze precedenti
sono temporanei, non prerequisiti disponibili: se mancano, ricreare strumenti e
cluster dedicati senza contattare il DB applicativo preesistente. Non copiare
configurazioni private nelle evidenze o nel repository.

Per i controlli grafici, dopo la compilazione, predisporre un display separato
(ad esempio `Xvfb :92 -screen 0 1440x1000x24 -nolisten tcp`) e impostare `DISPLAY`
al display effettivamente verificato libero. Registrare PID/log, attendere che il
server sia pronto e arrestare soltanto quel processo a fine prova. Il comando
Xvfb va adattato alle librerie/font disponibili; il mero PID non prova JavaFX.
Gli harness sotto usano solo il DB di test e possono ricrearne le fixture.
`AppStartupUiCheck` fa eccezione: non scrive nel DB e usa il contesto applicativo;
per provarlo isolatamente occorre una configurazione privata per una copia
sacrificabile, come nelle evidenze15/16. Non eseguire suite e harness insieme.

Le sezioni per ticket che seguono documentano l'introduzione delle verifiche e
i limiti osservati in quelle sessioni. I rinvii a lavori allora successivi sono
storici:01–17 risultano chiusi. La [matrice AC01–AC20 del18](evidence/r11-integrated/acceptance-matrix.md)
collega ticket, percorsi principali, alternativi ed errori a prove nominate;
le [evidenze18](evidence/r11-integrated/verification.md) distinguono nuovi risultati,
tentativi falliti, ripresa dopo perdita di /tmp e limiti. I conteggi precedenti
non sostituiscono una nuova esecuzione. Nessun test artificiale della documentazione.

Il [ticket17](evidence/r09-diagrams/verification.md) collega i [diagrammi correnti](diagrams/README.md)
a sorgenti e test reali e registra separatamente render, ispezione e nuove prove.
Il394/394 del16 resta storico. [Indice delle evidenze](evidence/README.md):
i vecchi manifest full-stack/jdk21-test e i risultati01–16 non attestano una nuova
esecuzione17. Questa attività non chiude la verifica integrata18.

## Saldi concorrenti — ticket 04

```bash
mvn -Ppostgres-tests -Dtest=BalanceConcurrencyIntegrationTest test
mvn -Ppostgres-tests clean test
```

`BalanceConcurrencyIntegrationTest` usa il database dedicato e i servizi pubblici:
acconto/saldo acquisto, rifiuto/scadenza, cessione, noleggio/rimborsi e ricarica.
Una connessione di controllo blocca il conto; `pg_stat_activity` e
`pg_blocking_pids` attestano due backend distinti in attesa prima del rilascio.
Il gate funziona sia sulla vecchia UPDATE sia sulla nuova lettura con lock:
stessa regressione prima/dopo, senza sleep o DAO simulati. Attese, future,
query di controllo e chiusura dei worker hanno limiti di 10 secondi; in uscita
il gate viene rilasciato e le query ancora in attesa vengono cancellate.

Le fixture aggiuntive esistono solo nel DB di test. Trigger temporanei verificano
che i movimenti siano già avvenuti e poi provocano un errore PostgreSQL per
controllare il rollback di saldo, stato, richieste e annullamenti. I trigger e
le funzioni di errore sono rimossi a fine prova. I rimborsi su più conti vengono
provati anche con ordine di inserimento opposto e con conto mancante.

Protocollo DAO: ogni modifica del saldo deve partire da
`BankAccountDao.findByClientIdForUpdate` e usare la stessa connessione di
`DatabaseManager.executeInTransaction*` fino al commit/rollback. Per più conti
si acquisiscono i lock per ID cliente crescente; letture di visualizzazione
restano senza lock. Questo protegge i movimenti del conto, senza certificare
l'unicità delle richieste o le prenotazioni concorrenti degli altri ticket.

[Comandi, RED/GREEN, isolamento e limiti](evidence/r02-balances/verification.md).


## Prezzo consigliato e base manuale — ticket 05

```bash
mvn -Dtest=PriceCalculatorServiceTest test
mvn -Ppostgres-tests -Dtest=PriceSelectionIntegrationTest test
mvn -Ppostgres-tests clean test
```

`PriceCalculatorServiceTest` verifica base manuale 10.000 € con sconto 10% →
9.000 €, sconti massimi e svalutazioni in cascata, manuale uguale al modello, arrotondamento della base a centesimi HALF_UP prima della formula,
ritorno all'automatico e validazione senza cambiare la scelta.
`PriceSelectionIntegrationTest` usa soltanto PostgreSQL dedicato e servizi pubblici:
creazione/modifica/rilettura delle modalità, cambio della base modello con manuale
invariato, ritorno al consigliato, zero e input invalidi, catalogo/acquisto in entrambe
le modalità, noleggio geometrico a 1 e 3 giorni inclusivi e stima della cessione
separata dalla base commerciale. Le sole scritture SQL aggiuntive preparano fixture.
Nessun DAO simulato certifica la persistenza.

Schema ricreabile secondo D02: `vehicle.price_mode` accetta `AUTOMATIC` e `MANUAL`,
con default automatico; `price` resta una base non negativa, usata solo in manuale.
La demo imposta esplicitamente l'automatico e allinea le basi memorizzate a quelle
dei modelli. Non eseguire il reset sul DB applicativo: per installazioni esistenti
lo schema deve essere aggiornato prima di avviare questo codice. Non è prevista
una migrazione dei dati storici in questo ticket.

L'aggiornamento della scelta commerciale scrive solo base/modalità; gli altri
aggiornamenti DAO conservano la modalità. Il protocollo dei conti del ticket 04
resta invariato. Il congelamento del totale acquisto, successivo al ticket 05,
è descritto nella sezione del ticket 06 qui sotto.

Il controllo JavaFX eseguibile e le evidenze sono in
[evidence/r03-prices](evidence/r03-prices/verification.md). Usa i costruttori pubblici
delle viste, JavaFX Robot e un display Xvfb isolato con una fixture PostgreSQL ridotta.
Questo è un controllo funzionale e di tastiera; le query durante il rendering e
il caricamento asincrono restano al ticket 14.

## Totale concordato all'acconto — ticket 06

```bash
mvn -Dtest=RequestAgreementTest test
mvn -Ppostgres-tests -Dtest=PurchaseAgreementIntegrationTest test
mvn -Ppostgres-tests -Dtest=RequestAgreementTest,PurchaseAgreementIntegrationTest,PriceCalculatorServiceTest,PriceSelectionIntegrationTest,BalanceConcurrencyIntegrationTest,DemoSqlValidationTest test
mvn -Ppostgres-tests clean test
```

`Request` conserva `agreedTotal` e `deposit` immutabili per l'acquisto; il saldo da
pagare è la loro differenza. `PurchaseService.initiatePurchase` continua a usare la
formula condivisa sulla base esplicita e arrotondata, poi salva totale e acconto
nella stessa transazione della prenotazione e dell'addebito protetto del conto.
Il completamento usa soltanto gli importi persistiti: nessuna lettura di basi,
sconti o anno per determinare il saldo o l'importo della vendita.

Lo schema ricreabile aggiunge `request.agreed_total DECIMAL(10,2)`, obbligatorio e
non negativo per SALE, con acconto compreso fra zero e totale; per PURCHASE resta
NULL. La stima di cessione è non negativa in `deposit`, senza normalizzazione del segno.
La demo dichiara i totali dei suoi accordi storici, senza ricavarli dal listino
corrente. D02 non richiede una migrazione storica: prima di usare il nuovo codice
su installazioni esistenti occorre adeguare lo schema e fornire i totali realmente
concordati. Non inferirli dai prezzi correnti e non eseguire reset sui dati applicativi.

`PurchaseAgreementIntegrationTest` osserva solo servizi pubblici e PostgreSQL reale:
20.000/4.000/16.000 in entrambe le modalità, cambio basi/modalità/sconti,
nuovo AppContext, accordo della fixture riferito all'anno precedente, centesimi,
riserva già occupata, approvazione/scadenza e fondi insufficienti. Trigger temporanei
confermano l'addebito e iniettano errori su inserimento richiesta/vendita,
cancellazione richiesta e aggiornamento veicolo; la rilettura verifica il rollback
integrale. SQL aggiuntivo prepara fixture e guasti, nessun mock certifica la persistenza.

Il controllo [JavaFX eseguibile](evidence/r04-agreement/AgreementUiCheck.java) riusa
i metodi Robot del ticket 05. Il [launcher](evidence/r04-agreement/run-ui.py) esegue
seed e verifica in due JVM distinte, sul solo DB dedicato e su Xvfb `:96` senza TCP:
importi ricaricati in cliente/venditore, cessione separata, frecce e Tab/Spazio/Invio,
approvazione, saldo e avvio acquisto con messaggi coerenti. La regola CSS circoscritta
alle due tabelle mantiene leggibili i valori selezionati. Prestazioni e caricamento
delle tabelle restano al ticket 14; nessuna certificazione screen reader.
[Evidenze, comandi, limiti e review](evidence/r04-agreement/verification.md).

## Richieste di acquisto elaborate una sola volta — ticket 07

```bash
mvn -Ppostgres-tests -Dtest=PurchaseConcurrencyIntegrationTest test
mvn -Ppostgres-tests -Dtest=PurchaseConcurrencyIntegrationTest,PurchaseAgreementIntegrationTest,BalanceConcurrencyIntegrationTest test
mvn -Ppostgres-tests clean test
```

`RequestDao.findByIdForUpdate` richiede una transazione attiva e rilegge la richiesta
con `SELECT ... FOR UPDATE` sulla connessione del chiamante. Saldo, rifiuto,
scadenza e approvazione mantengono il lock fino al commit/rollback. Il tipo viene
ricontrollato; il saldo richiede venditore, scadenza presente e non superata.
La scadenza usa l'elenco iniziale soltanto per gli ID: rilegge anche cliente,
tipo, approvazione e data sotto lock, saltando richieste già consumate o non più
scadute. La notifica viene restituita dopo il commit e usa il rimborso effettivo.

I movimenti conservano il protocollo del conto del ticket 04. Gli aggiornamenti
di saldo, aggiornamento/eliminazione richiesta e transizione atomica del veicolo
da `BLOCKED` devono interessare una riga; gli inserimenti devono restituire l'ID.
Un esito mancante provoca rollback, anche se PostgreSQL non solleva un errore.
Il totale concordato del ticket 06 rimane la fonte degli importi al saldo.

`PurchaseConcurrencyIntegrationTest` usa servizi pubblici, nuovi AppContext,
connessioni PostgreSQL distinte e gate SQL. `pg_stat_activity` e
`pg_blocking_pids` verificano le sessioni effettivamente in attesa prima del
rilascio; nessuno sleep o DAO simulato. Copre saldo/saldo, saldo/rifiuto nei due
ordini, saldo/scadenza, rifiuti e scadenze concorrenti/ripetuti e approvazioni
da sessioni obsolete. Il gate sulla richiesta verifica anche una scadenza letta
prima di una proroga concorrente, nei due ordini. Attese, query e worker hanno
limiti di 10 secondi; il cleanup rilascia i gate e cancella le query bloccate.

Acconto 4.000 €: penale 800 € e rimborso 3.200 € una sola volta. Sono verificati
anche approvazione incompleta, tipo diverso, stato veicolo incoerente e conto
mancante. Trigger PostgreSQL temporanei iniettano errori e scritture saltate
(`RETURN NULL`) su conto, vendita, richiesta e veicolo: nuove letture dei servizi
confermano saldo, accordo, prenotazione e registro dopo rollback.

[Evidenze della sessione e diff rispetto alla baseline dedicata](evidence/r04-purchase-once/verification.md).
Ticket 06 chiuso il 2 novembre 2026 dopo registrazione delle prove e review
indipendenti Standards/Spec senza rilievi. I suoi 19 test sono inclusi nella suite
163/163 del 07; viste/harness GUI del 06 e lavoro locale precedente preservati.
[Chiusura e continuità delle evidenze](evidence/r04-agreement/verification.md#tracciabilità-della-chiusura--2-novembre-2026).
Le cancellazioni obbligatorie dei test drive e le transizioni degli altri flussi
restano ai ticket successivi. Nessuna UI o regola economica nuova.

## Cancellazione atomica dei test drive — ticket 08

```bash
mvn -Ppostgres-tests -Dtest=TestDriveCancellationIntegrationTest test
mvn -Ppostgres-tests -Dtest=TestDriveCancellationIntegrationTest,PurchaseConcurrencyIntegrationTest,PurchaseAgreementIntegrationTest,BalanceConcurrencyIntegrationTest,PriceCalculatorServiceTest,PriceSelectionIntegrationTest test
mvn -Ppostgres-tests clean test
```

`TestDriveCancellationIntegrationTest` passa da `PurchaseService.completePurchase`
e `RentalService.bookRental`, composti da `AppContext` sul solo PostgreSQL dedicato.
La notifica sincrona `VehicleCommitmentEvent` porta la connessione del chiamante
all'Observer: addebito, vendita/noleggio, richiesta, stato e cancellazioni hanno
un solo commit/rollback. Nessuna transazione aggiuntiva o eccezione nascosta.

La politica usa `date > CURRENT_TIMESTAMP`, con il timestamp stabile di inizio
transazione PostgreSQL. Uguale e precedente restano nello storico. La prova del
confine inserisce da trigger date a −1/0/+1 microsecondo nella stessa transazione;
imposta solo lì un fuso locale che mantenga l'ora nella fascia 09–16 anche di notte,
senza cambiare clock o vincoli dello schema. I test drive futuri vengono eliminati
prima, durante e dopo il periodo noleggiato, per tutti i clienti; quelli di un
altro veicolo restano. È verificato anche il caso senza test drive futuri.

Un trigger `AFTER DELETE ... FOR EACH STATEMENT` osserva addebito, operazione,
richiesta consumata/stato venduto e tutte le cancellazioni, poi solleva un errore.
Il servizio deve fallire; nuovi `AppContext` verificano il rollback integrale,
inclusi entrambi i test drive eliminati. SQL prepara solo fixture e guasti;
nessun mock certifica la persistenza. Funzioni/trigger sono rimossi a fine prova.

Formula/base/centesimi 05, accordo 06, conti 04 e unicità elaborazioni 07 sono
verificati dalle suite precedenti incluse. Nessuna vista modificata; conflitti
concorrenti di prenotazione al ticket 11, altre transizioni ai rispettivi ticket.
[Comandi, esiti, versione e review](evidence/r04-test-drive-atomic/verification.md).


## Proposta di cessione e titolarità — ticket 09

```bash
mvn -Dtest=SaleTypeTest test
mvn -Ppostgres-tests -Dtest=SaleProposalIntegrationTest,PriceSelectionIntegrationTest test
mvn -Ppostgres-tests clean test
```

`submitSaleProposal` rilegge il VIN noto con `SELECT ... FOR UPDATE` nella
transazione della proposta. Richiede stato `SOLD`, ultima vendita effettiva al
proponente e assenza di qualsiasi richiesta aperta, anche un acquisto approvato.
Modello e anno devono corrispondere al VIN registrato; targa e chilometri possono
essere aggiornati, mentre base/modalità commerciale e storico sono conservati.
I rifiuti di dominio vengono propagati dopo il rollback con il messaggio leggibile
dalla vista esistente; gli errori SQL mantengono la gestione transazionale.

`SaleType` distingue `SALE` (vendita al cliente), `PURCHASE` (acquisto dal cliente) e `PENALTY`.
Tutte le factory e il mapping JDBC conservano il tipo; l'ultima vendita usa solo
`SALE`, ordinata per data e poi ID decrescente a parità di data. Il controllo
non deduce la proprietà dal segno o dall'importo: anche una vendita a zero conta.
Il ticket 02 mantiene pagamenti e flussi, aggiornando i tipi e rifiutando gli importi negativi.

Schema ricreabile secondo D02: `sale.type` è obbligatorio e vincolato ai tre valori;
`UNIQUE request(vehicle)` protegge tutte le richieste aperte, perché le richieste
concluse vengono rimosse. Per il VIN nuovo resta il vincolo univoco del veicolo:
il concorrente perdente fallisce senza sovrascrivere i dati del vincitore e senza
veicoli parziali. Non servono lock di processo o procedure esterne di proprietà.
Prima di usare questo codice su un'installazione esistente occorre adeguare lo
schema, classificare le operazioni storiche dai fatti effettivi e risolvere le
richieste duplicate. Non inferire i tipi dai segni attuali; non eseguire reset sui
dati applicativi. La demo conserva tutte le 40 operazioni con importi, date e
attribuzioni precedenti, aggiungendo solo il tipo già descritto dalle fixture.

`SaleProposalIntegrationTest` usa servizi pubblici/AppContext e PostgreSQL dedicato.
Le gare aprono connessioni distinte e fermano la prima proposta sull'INSERT della
richiesta tramite un lock SQL esterno. `pg_stat_activity` e `pg_blocking_pids`
attestano prima uno e poi due backend distinti in attesa; il secondo attende il
veicolo noto o l'unicità del VIN nuovo. Nessuno sleep arbitrario. Query, gate e
worker hanno limiti di 10 secondi e cleanup con rilascio/cancellazione dei lock.
Trigger temporanei provano rollback dopo scrittura del veicolo e aggiornamenti
saltati; la rilettura passa da nuovi AppContext. SQL prepara fixture e guasti,
senza mock come prova della persistenza. Nessuna vista modificata.

[Evidenze, versione verificata, comandi e limiti](evidence/r05-sale-proposal/verification.md).
L'ambiente `/tmp/drivehub-ticket07-*` è temporaneo: verificare file privati,
Java 21, binari, porta e configurazione prima del riuso; la suite 08 resta storica.


## Esito della cessione e registro — ticket 10

```bash
mvn -Ppostgres-tests -Dtest=SaleOutcomeIntegrationTest,SaleTypeTest test
mvn -Ppostgres-tests clean test
```

Approvazione e rifiuto rileggono e bloccano richiesta e veicolo nella stessa
transazione: tipo PURCHASE, stato WAITING, poi conto del cliente con il protocollo 04.
Lo storico completo (vendite, acquisizioni, noleggi e test drive) è controllato
sulla connessione del chiamante; l'ultima SALE resta un controllo distinto
di titolarità del ticket 09. Gli esiti verificano tutte le scritture.

Il cliente riceve +16000; il registro della concessionaria conserva 16000 con
tipo PURCHASE (uscita). SALE e PENALTY indicano entrate, con importi non negativi. Il tipo esplicito
si conserva anche a zero e con attribuzioni storiche nulle; il registro manager
non lo deduce da importi o soggetti mancanti. La regola esistente del conto
continua a rifiutare pagamenti di zero, senza consumare la proposta.

`SaleOutcomeIntegrationTest` usa servizi pubblici, AppContext distinti e PostgreSQL
reale: doppio invio, gare nei due ordini, sessioni obsolete, dati mancanti, vincoli,
errori e scritture saltate. Il gate SQL osserva PID distinti e pg_blocking_pids
prima del rilascio, senza sleep; query, worker e cleanup hanno timeout.
Trigger AFTER verificano tutte le scritture effettive prima di provocare il
rollback finale, anche dopo l'eliminazione fisica di un VIN nuovo.

Schema ricreabile secondo D02: `ck_sale_amount_type` richiede importi >= 0
per SALE, PURCHASE e PENALTY. Prima dell'uso su installazioni esistenti occorre adeguare
schema e importi delle acquisizioni già classificate dai fatti; non inferire il
tipo dai vecchi segni e non resettare dati applicativi. La demo non contiene
acquisizioni: i suoi importi, soggetti e attribuzioni restano invariati.

Il [controllo JavaFX](evidence/r05-sale-outcome/SaleOutcomeUiCheck.java) riusa Robot
dei ticket 05/06; il [launcher](evidence/r05-sale-outcome/run-ui.py) richiede
JAVA_HOME 21, le variabili DB di questa pagina e DISPLAY su un display di test
isolato. Dopo la compilazione Maven, eseguire:

```bash
python3 docs/evidence/r05-sale-outcome/run-ui.py
```

Seed e controllo usano JVM distinte e soltanto il DB dedicato; l'output predefinito
è `/tmp/drivehub-ticket10/ui`, personalizzabile con DRIVEHUB_UI_DIR. Il controllo
esercita approvazione, sessione obsoleta, rifiuti, saldo cliente, tipi/importi del
registro manager e navigazione con frecce, Tab, Spazio e Invio.
[Evidenze, comandi ed esiti della versione](evidence/r05-sale-outcome/verification.md).

## Prenotazioni concorrenti — ticket 11

```bash
mvn -Dtest=BookingRulesTest test
mvn -Ppostgres-tests -Dtest=BookingConcurrencyIntegrationTest test
mvn -Ppostgres-tests -Dtest=BookingRulesTest,BookingConcurrencyIntegrationTest,TestDriveCancellationIntegrationTest,BalanceConcurrencyIntegrationTest,PriceSelectionIntegrationTest test
mvn -Ppostgres-tests clean test
```

Le prenotazioni usano una sola connessione transazionale: veicolo con `FOR UPDATE`,
cliente con `FOR NO KEY UPDATE`, quindi conto con il protocollo 04 quando occorre
un addebito. Il lock cliente serializza anche test drive su veicoli diversi e non
richiede un conto; `NO KEY UPDATE` resta compatibile con i lock delle chiavi esterne
presi dagli altri flussi economici. I conti multipli mantengono l'ordine crescente
per ID cliente. Le cancellazioni manuali riducono le prenotazioni e non introducono
lock in ordine inverso. Il cambio destinazione prende lo stesso lock veicolo;
le sue ulteriori regole di stato rimangono al ticket 12.

Il test drive rifiuta lo stato non prenotabile subito dopo il lock veicolo,
prima di attendere il cliente: la gara a tre con saldo acquisto e noleggio su
un altro veicolo verifica che non si formi un ciclo veicolo → cliente → conto.
Il gate osserva tre backend in attesa nel RED; nel GREEN osserva il rifiuto
completato del terzo servizio prima di rilasciare gli altri due.

Disponibilità, limite tre e prezzo del noleggio vengono riletti dopo i lock;
modello e sconti usano la connessione del chiamante. La priorità commerciale resta
quella del ticket 08: se il test drive precede il noleggio, l'Observer lo cancella
nella transazione economica; se il noleggio precede il test drive, questo rilegge
il periodo occupato e viene rifiutato. Lo storico temporale dell'Observer resta
basato su `date > CURRENT_TIMESTAMP` della transazione.

Lo schema ricreabile richiede l'estensione PostgreSQL **btree_gist**, inclusa nei
contrib PostgreSQL 16 e trusted. `CREATE EXTENSION IF NOT EXISTS btree_gist` viene
eseguito dal proprietario del DB dedicato durante il reset. Su installazioni
esistenti predisporre l'estensione e risolvere eventuali periodi sovrapposti prima
di aggiungere `ex_rent_vehicle_period`; non eseguire schema/demo sui dati applicativi.
Il vincolo `EXCLUDE` confronta veicolo e `daterange(start, end, '[]')`: entrambi gli
estremi sono inclusi; due intervalli adiacenti che non condividono un giorno sono
ammessi. Unicità `(vehicle, date)` e fascia 09:00–16:00 dei test drive sono conservate.

Le regressioni usano servizi pubblici e PostgreSQL dedicato, con connessioni
distinte, gate SQL e osservazione di PID e `pg_blocking_pids` prima del rilascio.
SQL diretto verifica anche vincoli e concorrenza senza affidarsi ai lock dei servizi.
Trigger di errore verificano scritture effettive prima del rollback. Nessuno sleep
arbitrario e nessun mock certificano la persistenza. Query, attese e worker hanno
timeout; fixture e guasti esistono soltanto nel DB dedicato. Non eseguire altre
suite PostgreSQL contemporaneamente sullo stesso DB.

La suite 259/259 del ticket 10 è storica. Baseline locale e verifiche attuali,
hash delle versioni, RED/GREEN e limiti sono nelle
[evidenze del ticket 11](evidence/r06-bookings/verification.md).

Riferimenti PostgreSQL 16: [btree_gist trusted](https://www.postgresql.org/docs/16/btree-gist.html)
e [compatibilità dei lock di riga](https://www.postgresql.org/docs/16/explicit-locking.html#LOCKING-ROWS).

## Destinazione del veicolo e rimborsi — ticket 12

```bash
mvn -Ppostgres-tests -Dtest=VehicleDestinationIntegrationTest test
mvn -Ppostgres-tests -Dtest=VehicleDestinationIntegrationTest,BookingConcurrencyIntegrationTest,BalanceConcurrencyIntegrationTest,TestDriveCancellationIntegrationTest test
mvn -Ppostgres-tests clean test
python3 docs/evidence/r06-destination/run-ui.py
```

Il comando venditore accetta solo SALE → RENT e RENT → SALE. Rilegge lo stato
dopo il lock veicolo del ticket 11, rifiutando SOLD/BLOCKED/WAITING e richieste
sullo stesso stato. Prima di destinare alla vendita verifica sulla stessa
connessione l'assenza di noleggi che comprendano oggi, con entrambi gli estremi
inclusivi: anche un noleggio iniziato ieri e terminato oggi impedisce il cambio.

L'anteprima della vista è indicativa: elenca i noleggi con inizio strettamente
successivo a oggi. Il comando rilegge e cancella i futuri nella transazione,
verifica il numero di cancellazioni effettive, rimborsa gli importi memorizzati
integralmente e aggiorna il veicolo prima dell'unico commit. I conti vengono
bloccati per ID cliente crescente con il protocollo 04. Conto mancante, rimborso
fallito, cancellazione o aggiornamento saltato annullano tutte le scritture.
Nessuna nuova penale; storico e altri veicoli restano invariati.

`VehicleDestinationIntegrationTest` usa servizi pubblici e nuovi AppContext sul
PostgreSQL dedicato. Le gare nei due ordini osservano PID distinti e
`pg_blocking_pids` prima di rilasciare il gate SQL, senza sleep; query, attese,
worker e cleanup hanno timeout. Una prenotazione futura confermata prima viene
rimborsata; una iniziata oggi impedisce SALE. Il rollback del cambio lascia
procedere la prenotazione concorrente. I trigger di errore osservano cancellazioni,
accrediti e stato realmente scritti prima del rollback; una sequenza diagnostica
attesta il primo accredito prima del successivo conto mancante. Nessun mock
certifica la persistenza. Lock 11 e Observer 08 sono conservati.

Il launcher JavaFX richiede JAVA_HOME 21, variabili DB di questa pagina e DISPLAY
su Xvfb isolato; seed e controllo girano in JVM distinte. Riusa Robot 05/06/10
per frecce, Tab, Spazio e Invio: conferma/rimborso multi-cliente, noleggio iniziato
ieri, vista obsoleta BLOCKED, SALE → RENT e conto mancante con rollback.
Le pause Robot preesistenti servono al rendering; le gare PostgreSQL non usano sleep.
Le [evidenze della sessione](evidence/r06-destination/verification.md) distinguono
baseline corrente, RED/GREEN, suite finale, GUI e review. Il 297/297 del ticket 11
resta storico; l'ambiente `/tmp` va ricontrollato prima di ogni riuso.

## Cancellazione identità e storico — ticket 13

```bash
mvn -Ppostgres-tests -Dtest=IdentityDeletionIntegrationTest test
mvn -Ppostgres-tests clean test
# Dopo la compilazione, sul solo DB dedicato e un display Xvfb isolato:
python3 docs/evidence/r07-identity/run-ui.py
```

`AccountManagementDialog` continua a passare il ruolo visualizzato. La sola
mappatura `PostgresUserDao.resolveTable` accetta Cliente/CLIENT,
Venditore/SALESMAN e Manager/MANAGER per modifiche e cancellazione; il controllo
delle associazioni riusa la stessa mappatura, senza traduzioni nella UI o nel
servizio. Tutti i chiamanti sono elencati nelle [evidenze13](evidence/r07-identity/verification.md).

`AuthService.deleteAccount` verifica la password e usa una sola transazione per
lock dell'identità `FOR NO KEY UPDATE`, lock del conto se cliente, controllo di
richieste/test drive/noleggi/vendite e DELETE di esattamente una riga. L'ordine
identità → conto conserva quello delle prenotazioni11; `NO KEY UPDATE` lascia
terminare gli inserimenti con FK KEY SHARE di un acquisto già sul conto. Dopo
l'attesa il controllo rilegge le associazioni. Le FK RESTRICT proteggono anche
associazioni inserite dopo il controllo e accessi SQL diretti; il vincolo
`fk_sale_salesman` conserva il venditore invece di azzerarlo. Un conflitto FK
nella cancellazione è presentato come rifiuto, con rollback. Nessun cambiamento
all'ordine dei conti04 o agli eventi08, agli accordi06, alle prenotazioni11 o ai
rimborsi12.

I test usano servizi pubblici e PostgreSQL reale, gate SQL/advisory,
`pg_stat_activity`/`pg_blocking_pids`, backend distinti e timeout di 10 secondi,
senza sleep nelle gare. Trigger e sequenze diagnostiche attestano scritture
prima degli errori: eliminazione di identità e conto, accredito e acquisizione,
creazione del veicolo di cessione. Si verificano commit, rifiuti e rollback
nei due ordini delle gare, mantenendo attribuzioni, saldo, richieste e veicoli.
Una DELETE saltata non può generare un successo. Ruoli/credenziali errati,
richieste di entrambi i tipi, storico e identità senza vincoli sono inclusi.

L'identità di accesso è distinta dal conto dimostrativo: il solo conto, anche
con disponibilità positiva, non impedisce la cancellazione secondo le regole
esistenti e viene rimosso per CASCADE. Il dialogo lo esplicita prima della
conferma; richieste e storico impediscono invece la cancellazione. La prova
JavaFX verifica frecce/Tab/Spazio/Invio/Esc, password, conferma e annullamento,
motivi leggibili e callback soltanto dopo successo. Seed e controllo avvengono
in JVM distinte; le attese Robot riguardano il rendering, mai le gare DB.

Schema ricreabile secondo D02. Per un'installazione esistente, prima di usare
il nuovo codice aggiornare **solo il vincolo**, senza eseguire schema/demo:

```sql
BEGIN;
ALTER TABLE sale DROP CONSTRAINT fk_sale_salesman;
ALTER TABLE sale ADD CONSTRAINT fk_sale_salesman
  FOREIGN KEY (salesman) REFERENCES salesman(id) ON DELETE RESTRICT;
COMMIT;
```

Le attribuzioni già azzerate in passato non sono ricostruibili da questo
vincolo. Nessun aggiornamento o reset applicativo è stato eseguito nella prova.
L'ambiente13 è temporaneo: Java21/PG16 estratti in
`/tmp/drivehub-ticket13-tools/root`, nuovo cluster
`/tmp/drivehub-ticket13-pgdata`, **127.0.0.1:55433**, display Xvfb **:95**.
Le configurazioni private in `/tmp/drivehub-ticket13-session/env.json` non sono
conservate nel repository; ricontrollare o ricreare l'ambiente se /tmp scompare.


## Tabelle reattive — ticket 14

```bash
mvn -Ppostgres-tests -Dtest=CatalogListingIntegrationTest,PriceCalculatorServiceTest,PriceSelectionIntegrationTest test
# Con Java21, variabili DB dedicate e un display Xvfb isolato già attivo:
DRIVEHUB_UI_CLASS=it.unifi.drivehub.TableLifecycleUiCheck python3 docs/evidence/r08-tables/run-ui.py
DRIVEHUB_UI_CLASS=it.unifi.drivehub.SlowModelsUiCheck python3 docs/evidence/r08-tables/run-ui.py
mvn -Ppostgres-tests clean test
```

`CatalogListingIntegrationTest` usa i servizi pubblici e PostgreSQL reale per
prezzi automatici/manuali, massimo sconto, centesimi, usato/anno/chilometri,
tariffa iniziale, rimozione sconti, nuova base/nome modello e nuova sessione.
Le righe preparate sono sostituite a ogni lettura: nessuna cache persistente,
nessuna modifica a schema, DAO o ai comandi economici. Il listino usa quattro
letture collettive indipendenti dal numero di veicoli; gli sconti modello due.

Le viste usano `Task` JavaFX e worker virtuali Java21. Soltanto il task corrente
può aggiornare dati e stato; cancellazione/sostituzione impediscono pubblicazioni
obsolete, anche dopo un errore. Durante caricamento o errore i dati del controllo
sono disabilitati; Aggiorna/Riprova rimane raggiungibile con Tab e Spazio.
La cancellazione non promette l'interruzione immediata di una query JDBC già
avviata: il worker chiude le risorse e il suo risultato non viene pubblicato.
Le azioni di dominio conservano le verifiche e i prezzi correnti dei servizi.

Gli harness osservano i caricamenti tramite proprietà JavaFX, latch e lock
PostgreSQL (`pg_stat_activity`/`pg_blocking_pids`), senza sleep per sincronizzare
letture. Un driver diagnostico delega a JDBC reale e conta connessioni e
esecuzioni durante celle, refresh e scorrimento. Un gate dopo una vera query
trattiene il vecchio risultato/errore fino al completamento della nuova richiesta;
la chiusura della connessione viene osservata prima delle asserzioni finali.
La prova d'errore rinomina temporaneamente una tabella soltanto nel DB dedicato
e la ripristina in `finally`. I timeout limitano attese e query di controllo.
Le pause Robot05 riusate riguardano solo i gesti/rendering; le attese di frame
aggiuntive usano `AnimationTimer`. Non eseguire harness e suite contemporaneamente.

Il launcher richiede `JAVA_HOME`, `DISPLAY` e le variabili DB di testing;
`DRIVEHUB_UI_DIR` sceglie una directory separata per ogni esecuzione,
`DRIVEHUB_UI_CLASS` sceglie l'harness. L'ambiente13 è stato ricontrollato:
Java21/PG16 estratti, cluster dedicato127.0.0.1:55433 e copia sentinella,
Xvfb94 sul solo socket astratto, senza TCP. Il Java predefinito è17 e non va
usato per queste prove. Se /tmp manca ricreare gli strumenti come sopra.
[Evidenze e limiti della sessione14](evidence/r08-tables/verification.md).

## Pulizia minima — ticket 15

Le ricerche del ticket15 precedono le rimozioni: nessuna risorsa o caricamento
FXML e nessun chiamante di `AppContext.getCurrentUser`, `setCurrentUser` o
`logout`. Le viste continuano a ricevere l'utente e la callback di disconnessione.
Nessun test aggiunto per verificare la forma del codice rimosso.

```bash
mvn -DskipTests compile
# Con Java21, variabili DB dedicate e display Xvfb isolato già attivo:
DRIVEHUB_UI_CLASS=it.unifi.drivehub.TableLifecycleUiCheck python3 docs/evidence/r08-tables/run-ui.py
mvn -Ppostgres-tests clean test
```

Il controllo aggiuntivo [AppStartupUiCheck](evidence/r10-cleanup/AppStartupUiCheck.java)
invoca il vero `App.start`: finestra di accesso visibile, tema, schede e pulsante
raggiungibile con Tab. Riusa i controlli Robot05 e i frame14; non esegue login o
scritture DB. Il classpath della prova antepone un `application.properties`
privato indirizzato alla sola copia sacrificabile. Non modificare la
configurazione applicativa del repository. Comandi di compilazione/avvio,
versione, log, schermata e nuova baseline sono nelle [evidenze15](evidence/r10-cleanup/verification.md).
Il394/394 del14 rimane storico; `/tmp` e i servizi vanno sempre ricontrollati.
