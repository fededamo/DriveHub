# Installazione e avvio

Procedura per una **nuova copia locale** di DriveHub e un database applicativo
nuovo. Per verificare modifiche non committate copiare la directory di lavoro
corrente, inclusi i file non tracciati necessari; un clone del solo HEAD non
contiene le revisioni locali. Escludere `.git`, `target` e `Relazione_SWE`.
I percorsi dei comandi seguenti sono relativi alla radice `DriveHub/`.

## Prerequisiti

- **JDK 21**, con `java` e `javac`; **Maven 3.9+**. In WSL usare un JDK Linux.
- **PostgreSQL 16**, client `psql` e file dell'estensione **btree_gist**
  (pacchetti PostgreSQL/contrib della distribuzione). Il proprietario del nuovo
  DB esegue `CREATE EXTENSION` attraverso lo script dello schema.
- Ambiente grafico per JavaFX e relative librerie native. Su Linux occorrono
  GTK3/X11; per le sole prove senza schermo usare un display Xvfb isolato.
- Accesso a Maven Central alla prima risoluzione delle dipendenze. JavaFX e
  driver JDBC sono dichiarati nel POM: non occorre un SDK JavaFX separato.

Installare questi strumenti con i pacchetti appropriati al proprio sistema;
poi verificare, senza abbassare il target del POM:

```bash
java -version
javac -version
mvn -version
psql --version
```

Le prime tre uscite devono identificare Java **21**, compreso il runtime usato
da Maven. Se necessario impostare `JAVA_HOME` alla propria directory JDK21 e
anteporre `"$JAVA_HOME/bin"` a `PATH`. Verificare anche la versione del server
con `SHOW server_version;`: la versione del client non la dimostra.

## Database applicativo e configurazione

Avviare PostgreSQL secondo il proprio sistema. In una sessione `psql` da
amministratore, creare **una sola volta** un nuovo ruolo e database applicativi:

```sql
CREATE ROLE drivehub LOGIN NOSUPERUSER NOCREATEDB NOCREATEROLE NOREPLICATION NOBYPASSRLS;
\password drivehub
CREATE DATABASE drivehub OWNER drivehub;
REVOKE CONNECT ON DATABASE drivehub FROM PUBLIC;
GRANT CONNECT ON DATABASE drivehub TO drivehub;
```

`\password` chiede il segreto senza inserirlo nel file SQL o nella cronologia.
Scegliere una password locale propria. Usare `drivehub_test` esclusivamente
per i test, mai per l'applicazione.

Modificare **nella propria copia** `src/main/resources/application.properties`:

```properties
db.url=jdbc:postgresql://127.0.0.1:5432/drivehub
db.user=drivehub
db.password=<password locale scelta per il ruolo drivehub>
```

Sostituire il segnaposto e adattare host/porta ai valori reali. I tre nomi di
proprietà sono quelli letti da [DatabaseManager](../src/main/java/it/unifi/drivehub/dao/DatabaseManager.java).
Le variabili `DRIVEHUB_TEST_*` e `DRIVEHUB_SENTINEL_*` configurano le prove,
**non** l'applicazione. La risorsa viene copiata in `target/classes` da Maven;
dopo una modifica rieseguire il comando di avvio dalla radice del progetto.
Non distribuire o committare password locali: anche il JAR prodotto contiene
la risorsa. La configurazione è in chiaro; non esiste un gestore di segreti.

Il costruttore predefinito usa valori locali di fallback se la risorsa o una
chiave mancano e ignora un errore di lettura del file: controllare esplicitamente
tutte e tre le chiavi. Non presumere che una variabile d'ambiente le sostituisca.

Verificare destinazione e permessi prima di eseguire SQL, usando gli stessi
host/porta/ruolo/database della configurazione (qui l'esempio locale):

```bash
psql -X -W -h 127.0.0.1 -p 5432 -U drivehub -d drivehub \
  -v ON_ERROR_STOP=1 -c 'SELECT current_database(), current_user; SHOW server_version;'
```

## Inizializzazione, demo facoltativa e reset

**Solo sul nuovo DB vuoto destinato a questa installazione**, creare lo schema:

```bash
psql -X -W -h 127.0.0.1 -p 5432 -U drivehub -d drivehub \
  -v ON_ERROR_STOP=1 --single-transaction -f src/main/resources/db/schema.sql
```

`schema.sql` **non è una migrazione**: contiene `DROP ... CASCADE` e ricrea
tabelle, tipi, vincoli, trigger e sequenze. Rieseguirlo su un DB popolato è un
**reset distruttivo**. `ON_ERROR_STOP` e la singola transazione fanno fallire
e annullare lo script su errore; non rendono sicura una destinazione sbagliata.
Se manca `btree_gist`, predisporne i file sul server prima di riprovare.

Lo schema basta per avviare la schermata pubblica e registrare nuovi clienti;
catalogo e identità manager/venditore sono inizialmente vuoti. La schermata
pubblica non registra manager o venditori. Per esplorare i tre ruoli si può
caricare **facoltativamente**, solo sul DB dimostrativo sacrificabile:

```bash
psql -X -W -h 127.0.0.1 -p 5432 -U drivehub -d drivehub \
  -v ON_ERROR_STOP=1 --single-transaction -f src/main/resources/db/demo.sql
```

Anche `demo.sql` è **distruttivo**: elimina i dati applicativi e li sostituisce
con la fixture, riallineando le sequenze. Non è un'aggiunta di alcuni esempi,
non è necessario all'avvio e non va eseguito su dati da conservare.

Solo dopo aver caricato questa demo, password **`password`** per le identità
della fixture, ad esempio:

| Ruolo | Email |
|---|---|
| Cliente | `mario.rossi@email.it` |
| Venditore | `paolo.costa@drivehub.it` |
| Manager | `vittorio.manager@drivehub.it` |

Le date della demo sono storiche, non uno scenario sempre futuro: gli acquisti
scaduti vengono trattati all'accesso del cliente. Il conto è dimostrativo;
l'identificativo generato ha forma simile a un IBAN e non certifica un conto
bancario reale né un checksum bancario.

Installazioni già popolate richiedono un adeguamento dello schema e dati
coerenti prima di usare questa versione: prezzo/modalità, totale concordato,
tipo/segno delle operazioni, unicità richieste, esclusione sovrapposizioni e FK
storiche sono descritti in [testing](testing.md). La decisione D02 ammette un
DB ricreabile; non viene fornita una migrazione completa dei dati preesistenti.
Non ricavare totali storici dal listino corrente o tipi dai soli vecchi segni.

## Build e avvio ordinario

```bash
# Build del JAR e compilazione dei test; non esegue i test
mvn -DskipTests clean package
# Avvio ordinario dalla radice del progetto
mvn javafx:run
```

Su Windows `run.bat` richiama lo stesso goal Maven: richiede ugualmente JDK21,
Maven nel PATH, PostgreSQL già avviato/configurato e schema predisposto.
Il JAR ordinario non è un installer autonomo con runtime e dipendenze inclusi.

L'avvio **non** crea DB/ruoli, non esegue schema/demo e non resetta i dati.
`App.start` costruisce il contesto e mostra accesso/registrazione; la sola
finestra iniziale non prova la connessione DB. Il login e le successive azioni
usano PostgreSQL. In caso di errore controllare destinazione, credenziali,
servizio, schema e console; per un crash non gestito controllare anche
`app_error.log`. Non usare un reset come rimedio ordinario a un errore di avvio.

Per riavviare, chiudere la finestra e ripetere soltanto `mvn javafx:run` con il
servizio PostgreSQL disponibile. La prima installazione e la demo non vanno ripetute.

## Prove e limiti

`mvn test` verifica solo i test senza DB. La suite completa richiede ruolo/DB
dedicati e sentinella distinta: seguire [Build e test isolati](testing.md).
Suite e harness grafici possono resettare solo il DB di test; non eseguirli
contemporaneamente sullo stesso DB.

La [verifica16](evidence/r09-instructions/verification.md) registra una copia
della versione locale corrente, versioni effettive, inizializzazione/demo,
build, avvio su Xvfb e suite con isolamento/sentinella. I percorsi `/tmp` delle
sessioni precedenti non sono prerequisiti installabili: verificarne l'esistenza
e ricreare gli strumenti se assenti. Windows, altri display e distribuzione
con installer richiedono prove proprie; il `394/394` del15 è storico.
