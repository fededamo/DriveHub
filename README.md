# DriveHub

DriveHub è un'applicazione desktop per la gestione di una concessionaria: consente ai clienti di consultare veicoli, acquistare, noleggiare e prenotare test drive; venditori e manager gestiscono richieste, catalogo e operazioni. È sviluppata in Java 21 con JavaFX, Maven, JDBC e PostgreSQL 16.

## Avvio rapido su Windows

Servono **JDK 21**, **Maven 3.9+** e **Docker Desktop** installati; Java e Maven devono essere disponibili nel PATH. Avvia Docker Desktop almeno una volta e completa l'eventuale configurazione iniziale. Alla prima esecuzione serve una connessione Internet per scaricare l'immagine PostgreSQL e le dipendenze Maven.

Dalla cartella del progetto fai doppio clic su `run.bat`, oppure esegui:

```powershell
cd C:\DEV\DriveHub
.\run.bat
```

Lo script avvia Docker Desktop se necessario, crea al primo avvio il contenitore `drivehub-db` con PostgreSQL e dati demo, quindi avvia l'app. I dati sono conservati nel volume Docker `drivehub-quickstart-data` tra un avvio e l'altro. La porta locale **55432** deve essere libera. Il database demo usa credenziali locali dimostrative e non è destinato alla produzione. Chiudere la finestra dell'app non arresta il database.

| Ruolo | Email | Password demo |
|---|---|---|
| Cliente | `mario.rossi@email.it` | `password` |
| Venditore | `paolo.costa@drivehub.it` | `password` |
| Manager | `vittorio.manager@drivehub.it` | `password` |

Per fermare il database dopo l'uso: `docker stop drivehub-db`. Il successivo `run.bat` lo riavvia conservando i dati. Se l'inizializzazione si interrompe o la porta è occupata, controllare il messaggio nella finestra e i log con `docker logs drivehub-db`; lo script non reinizializza automaticamente volumi esistenti.

## Avvio manuale da terminale

Puoi avviare DriveHub senza `run.bat` o `run.ps1`, usando PostgreSQL installato sul computer. In questo caso Docker non serve. Se hai già un database inizializzato e compatibile con questa versione, salta la creazione e il caricamento dei dati: configura il collegamento e avvia l'app.

### Prerequisiti

- **JDK 21** e **Maven 3.9+**.
- **PostgreSQL 16**, client **psql** e file dell'estensione **btree_gist** disponibili sul server (pacchetto contrib, se previsto dalla distribuzione).
- Un ambiente grafico per JavaFX; su Linux servono anche le librerie GTK3/X11.
- Internet alla prima esecuzione per scaricare le dipendenze. Maven scarica JavaFX e il driver JDBC: non serve installare un SDK JavaFX separato.

Verifica che i comandi siano nel PATH e che anche `mvn -version` indichi Java **21**:

```bash
java -version
javac -version
mvn -version
psql --version
```

Apri il terminale nella cartella **DriveHub che contiene `pom.xml`**. Tutti i percorsi seguenti partono da lì; i comandi sono su una sola riga e si possono usare anche in PowerShell.

### Prima configurazione del database

Questi passaggi servono **una sola volta, su un nuovo database**. **`schema.sql` e `demo.sql` cancellano i dati esistenti: non eseguirli su un database da conservare.**

1. Avvia il servizio PostgreSQL con gli strumenti del tuo sistema e collegati come amministratore (nell'esempio l'utente è `postgres`):

   ```bash
   psql -h 127.0.0.1 -p 5432 -U postgres -d postgres
   ```

2. Dentro `psql`, crea l'utente e il database applicativi:

   ```sql
   CREATE ROLE drivehub LOGIN NOSUPERUSER NOCREATEDB NOCREATEROLE NOREPLICATION NOBYPASSRLS;
   \password drivehub
   CREATE DATABASE drivehub OWNER drivehub;
   REVOKE CONNECT ON DATABASE drivehub FROM PUBLIC;
   GRANT CONNECT ON DATABASE drivehub TO drivehub;
   \q
   ```

   `\password` ti chiede di scegliere la password del database; `\q` torna al terminale.

3. Modifica `src/main/resources/application.properties`, inserendo la password scelta al posto di `PASSWORD_SCELTA`:

   ```properties
   db.url=jdbc:postgresql://127.0.0.1:5432/drivehub
   db.user=drivehub
   db.password=PASSWORD_SCELTA
   ```

   Adatta host e porta se necessario e non committare password personali. Le variabili `DRIVEHUB_DB_URL`, `DRIVEHUB_DB_USER` e `DRIVEHUB_DB_PASSWORD`, se impostate, prevalgono sul file.

4. Dal terminale, verifica di collegarti al database giusto e al server PostgreSQL 16:

   ```bash
   psql -X -W -h 127.0.0.1 -p 5432 -U drivehub -d drivehub -v ON_ERROR_STOP=1 -c "SELECT current_database(), current_user; SHOW server_version;"
   ```

   Se la destinazione è corretta, crea le tabelle sul nuovo database vuoto:

   ```bash
   psql -X -W -h 127.0.0.1 -p 5432 -U drivehub -d drivehub -v ON_ERROR_STOP=1 --single-transaction -f src/main/resources/db/schema.sql
   ```

5. **Facoltativo:** carica catalogo e utenti demo per provare tutti i ruoli:

   ```bash
   psql -X -W -h 127.0.0.1 -p 5432 -U drivehub -d drivehub -v ON_ERROR_STOP=1 --single-transaction -f src/main/resources/db/demo.sql
   ```

   Puoi usare le credenziali demo nella tabella sopra. Senza demo, catalogo e utenti sono vuoti: puoi registrare nuovi clienti, ma non manager o venditori dalla schermata pubblica.

### Avvio dell'app e avvii successivi

Con PostgreSQL in esecuzione, compila e avvia l'app dalla cartella contenente `pom.xml`:

```bash
mvn compile javafx:run
```

Per gli avvii successivi basta avere PostgreSQL acceso ed eseguire `mvn javafx:run`. Non ripetere creazione del database, schema o demo. Chiudere la finestra dell'app non arresta PostgreSQL.

Se usi il **database Docker già inizializzato da `run.bat`**, avvia Docker e poi esegui `docker start drivehub-db`. Salta la prima configurazione e usa URL `jdbc:postgresql://127.0.0.1:55432/drivehub`, utente `drivehub` e password demo `drivehub-demo` nel file di configurazione, quindi avvia l'app con Maven.

Per dettagli e risoluzione dei problemi, consulta [installazione e avvio](docs/installation.md).

## Architettura e funzionalità

La presentazione JavaFX usa servizi applicativi, oggetti di dominio e DAO JDBC; `AppContext` costruisce le dipendenze. La descrizione delle responsabilità, dei flussi e dei pattern è in [architettura](docs/architecture.md); i [diagrammi](docs/diagrams/README.md) includono sorgenti e indicazioni di lettura.

Il prezzo di un veicolo parte dalla base del modello o da una base manuale scelta dal manager. La formula applica lo sconto maggiore tra quello del modello e del veicolo, poi le riduzioni per usato, età e chilometraggio. Nell'acquisto, il totale viene concordato al pagamento dell'acconto del 20%; il saldo è la differenza rispetto a tale totale. Dettagli e motivazioni sono nell'[ADR sui prezzi](docs/adr/0001-base-manuale-e-prezzo-concordato.md).

## Build e test

Le richieste usano il punto di vista della concessionaria: `SALE` per il cliente
che compra (acconto e totale concordato), `PURCHASE` per la cessione del cliente
(stima non negativa, senza totale concordato). Il registro `Sale` usa `SALE` per la vendita al cliente, `PURCHASE` per
l'acquisto dal cliente e `PENALTY` per la penale, con importi non negativi.
I quattro enum Java sono rappresentati da enum nativi PostgreSQL: `vehicle_state`,
`request_type`, `price_mode` e `sale_type` ([ADR0002](docs/adr/0002-enumerazioni-native-postgresql.md)).
L'installazione locale è stata consolidata e aggiornata al contratto finale.
Lo script di conversione è stato rimosso dopo l'uso, anche dai test, su richiesta.
[Stato dell'aggiornamento e backup](docs/data-conversion.md). Inizializzazione e
demo servono soltanto a database nuovi o sacrificabili.

```bash
mvn -DskipTests clean package
mvn test
```

Il primo comando compila e crea il JAR; il secondo esegue i test senza database. I test PostgreSQL richiedono database e credenziali dedicati: istruzioni e limiti sono in [build e test isolati](docs/testing.md). Il JAR ordinario non include un runtime o tutte le dipendenze.

## Uso dell'AI

Per questa revisione è stato usato Codex per analizzare l'avvio, predisporre lo script Windows e aggiornare la documentazione. Le note del corso richiedono di documentare le altre eventuali applicazioni dell'AI e il relativo processo: integrare questa sezione con gli usi effettivi del gruppo prima della consegna.
