# DriveHub

**Repository:** [github.com/fededamo/DriveHub](https://github.com/fededamo/DriveHub)

**Progetto di Ingegneria del Software, A.A. 2025/2026** — Federico D'Amodio e Matteo Orlandi, Università degli Studi di Firenze.

DriveHub è un'applicazione desktop JavaFX per le attività essenziali di un concessionario: consultazione dei veicoli, test drive, noleggi, richieste di acquisto e valutazione delle auto proposte dai clienti. Il catalogo e lo stato dei veicoli collegano le operazioni dei tre ruoli.

| Ruolo | Funzioni principali |
| --- | --- |
| Cliente (`CLIENT`) | Consulta il catalogo, prenota test drive e noleggi, richiede un acquisto con acconto, propone un veicolo e consulta le proprie operazioni. |
| Venditore (`SALESMAN`) | Valuta le proposte dei clienti, autorizza il completamento degli acquisti e gestisce la destinazione dei veicoli tra vendita e noleggio. |
| Manager (`MANAGER`) | Gestisce marche, modelli e veicoli, applica sconti e consulta le vendite registrate. |

I pagamenti sono simulati mediante conti interni: non vengono eseguite transazioni bancarie reali. La registrazione dello staff è aperta solo per facilitare la dimostrazione del prototipo.

## Documentazione del progetto

- [Relazione finale in PDF](Relazione_SWE/DriveHub_Relazione.pdf) e [sorgente LaTeX](Relazione_SWE/main.tex): contesto, requisiti, architettura, progettazione, implementazione e test.
- [Template dei casi d'uso](Relazione_SWE/templates), [mockup](Relazione_SWE/images/mockups) e [diagrammi di navigazione](docs/diagrams) per seguire i flussi descritti nella relazione.
- [Sorgenti dei diagrammi](docs/diagrams), [modello ER di riferimento](docs/reference/er.png) e [migrazione SQL](src/main/resources/db/migration/init.sql). I file `.drawio` sono conservati in `docs/` e `docs/source-diagrams/`.
- [Linee guida del project work, versione gennaio 2025](docs/reference/project-guidelines-Jan2025.pdf): riferimento metodologico dell'A.A. 2024/2025 usato per organizzare gli artefatti. Il progetto e la relazione sono dell'A.A. 2025/2026.

La cartella [`docs/evidence`](docs/evidence) conserva risultati di test di baseline precedenti. Non vanno confusi con i risultati ottenuti eseguendo oggi la suite sul checkout corrente. La relazione distingue inoltre le funzioni attuali da requisiti e mockup storici.

## Requisiti e avvio

Per compilare e avviare l'interfaccia servono **JDK 21**, **Maven 3.9+** e una sessione desktop con le librerie native richieste da JavaFX. Gli script di avvio e reset richiedono **Docker con Compose** (Docker Desktop su Windows, Docker Engine con plugin Compose su Linux) e la porta locale **5432** libera. La prima esecuzione Maven può richiedere Internet per scaricare le dipendenze. `java -version`, `mvn -version` e `docker compose version` permettono di controllare le installazioni.

Su **Windows, da PowerShell**, nella directory della repository:

```powershell
.\run.bat
```

Su **Linux, da Bash in una sessione grafica**:

```bash
./run.sh
```

Se `.env` non esiste, gli script lo creano da `.env.example` con una **password casuale**. Un file `.env` già presente non viene sovrascritto. Gli script avviano PostgreSQL tramite Compose, ne attendono lo stato *healthy* e verificano l'autenticazione prima di eseguire Maven. Se esiste già un volume PostgreSQL creato con un'altra password, occorre ripristinare quella password in `.env`; la nuova password casuale non modifica quella del volume. L'applicazione legge `.env` dalla directory del progetto e applica la migrazione SQL al primo avvio. In `.env.example` il seed è disattivato: per popolare il database con i dati dimostrativi usare il comando di reset qui sotto. **Il reset sostituisce i dati applicativi esistenti.** Con `DRIVEHUB_DB_SEED_DEMO=true` anche l'avvio cancellerebbe e ricaricherebbe i dati demo: gli script di avvio lo bloccano e chiedono di impostare `false`. `.env` è escluso da Git.

Reset esplicito del database:

| Windows | Linux |
| --- | --- |
| `.\reset-db.bat` | `./reset-db.sh` |

Se Compose non parte, controllare che Docker sia avviato con `docker info` e lo stato del servizio con `docker compose ps`. Il reset si interrompe prima di eseguire SQL se PostgreSQL non diventa pronto.

Il reset usa `mvn compile exec:java`: compila il progetto anche se `target/` non esiste. Se il database o lo schema non esistono ancora, Compose crea il database e l'applicazione applica `init.sql` prima di caricare `demo.sql`. Servono comunque i sorgenti del progetto, JDK 21, Maven e le dipendenze Maven scaricabili o già in cache.

### Accessi e dati dimostrativi

Dopo il reset, questi tre account permettono di provare ciascun ruolo. **La password comune è `password`**, valida solo per la demo locale:

| Ruolo | Email |
| --- | --- |
| Cliente | `mario.rossi@email.it` |
| Venditore | `paolo.costa@drivehub.it` |
| Manager | `vittorio.manager@drivehub.it` |

`demo.sql` contiene in totale 15 clienti, 4 venditori e 1 manager, oltre a marche, modelli, 56 veicoli in vari stati, conti simulati, richieste d'acquisto, test drive, noleggi e vendite. Questi account non sono presenti in un database nuovo finché non viene eseguito il reset; non usare la password demo in un ambiente reale.

Se PostgreSQL non è raggiungibile, l'applicazione ripiega su **H2 in memoria** per la dimostrazione. I dati di questa modalità scompaiono alla chiusura; per verificare la persistenza occorre controllare che PostgreSQL sia avviato. Si può creare un account dalla schermata **Registrati**.

Per fermare il database senza eliminare il volume:

```powershell
docker compose down
```

## Configurazione

Le impostazioni possono essere fornite con proprietà JVM, nel file `.env` o come variabili d'ambiente. L'ordine di precedenza dell'applicazione è: **proprietà JVM → `.env` → ambiente → valore predefinito**.

| Variabile | Proprietà JVM | Valore predefinito dell'applicazione |
| --- | --- | --- |
| `DRIVEHUB_DB_URL` | `drivehub.db.url` | `jdbc:postgresql://localhost:5432/drivehub` |
| `DRIVEHUB_DB_USER` | `drivehub.db.user` | `drivehub` |
| `DRIVEHUB_DB_PASSWORD` | `drivehub.db.password` | `drivehub` |
| `DRIVEHUB_DB_SEED_DEMO` | `drivehub.db.seed-demo` | `true` |

Il valore predefinito della password riguarda solo l'applicazione: **Docker Compose richiede una password** in `.env`. Se il file manca, gli script ne generano una casuale; se lo crei manualmente da `.env.example`, sostituisci `change-me-local-only` prima dell'avvio. Database e applicazione usano la stessa configurazione. Il nome del database del container si imposta con `DRIVEHUB_DB_NAME` (predefinito: `drivehub`). Il valore predefinito del seed nel codice è `true`, ma `.env.example` lo imposta esplicitamente a `false` per evitare reset automatici.

## Compilazione e test

Su **Windows (PowerShell)**:

```powershell
mvn clean test
mvn package
python Relazione_SWE/validate_report.py
```

Su **Linux (Bash)** gli stessi comandi Maven si eseguono dalla radice del progetto; per il validatore usare `python3 Relazione_SWE/validate_report.py`. Python 3 è richiesto per il validatore e per gli script di verifica dedicati.

La suite Maven standard esclude i gruppi `postgres` e `gui`; copre dominio, servizi, contratti FXML, vincoli architetturali e DAO con H2 in modalità PostgreSQL. H2 non sostituisce una verifica su PostgreSQL reale. Gli script dedicati richiedono Bash, Python 3 e Docker; sono destinati a Linux o a un ambiente WSL con Docker funzionante:

```bash
bash scripts/test_postgres.sh     # PostgreSQL 16 in un container temporaneo
bash scripts/test_full_stack.sh   # PostgreSQL 16, JavaFX e Xvfb in container
```

L'ultima verifica locale su Windows, il 21 ottobre 2026, ha superato **57 test su 57**. La configurazione Compose e la sintassi degli script PowerShell sono state controllate. L'avvio grafico, il packaging JAR, la compilazione LaTeX e i comandi Linux non sono stati verificati in questo ambiente; il packaging richiede inoltre un plugin Maven non presente nella cache locale. I profili dedicati e le evidenze archiviate hanno condizioni e baseline proprie; consultare la sezione *Testing* della relazione per i limiti dei risultati.

Per ricompilare la relazione occorre una distribuzione LaTeX con i pacchetti indicati in [`main.tex`](Relazione_SWE/main.tex):

```bash
cd Relazione_SWE
latexmk -pdf main.tex
```

Il PDF finale già consegnabile resta versionato anche se LaTeX non è installato.

## Struttura e metodo

```text
src/main/java/.../presentation   Interfaccia JavaFX, controller e navigazione
src/main/java/.../business       Servizi e regole applicative
src/main/java/.../domain         Entità e invarianti del dominio
src/main/java/.../dao            Interfacce DAO e accesso JDBC ai dati
src/main/resources/db            Migrazione e dati dimostrativi
src/test                       Test unitari, funzionali e di integrazione
docs/                          Diagrammi, mockup, riferimenti ed evidenze
Relazione_SWE/                 Sorgenti LaTeX, figure e PDF finale
```

Come richiesto dalle linee guida del project work, la relazione collega i casi d'uso ai template, ai mockup, ai diagrammi di navigazione e ai test; presenta inoltre architettura, modello dei dati e scelte implementative. I diagrammi PlantUML e Draw.io sono sorgenti progettuali conservati nella repository; lo script di rendering produce le immagini senza ricavare UML dal codice.

Nel lavoro sono stati usati strumenti di IA generativa per analisi comparativa, revisione del testo, diagrammi, codice e test, come dichiarato nella relazione. Le affermazioni sui risultati verificati si riferiscono alle esecuzioni e ai file indicati, non a materiale storico.
