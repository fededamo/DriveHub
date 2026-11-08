# DriveHub

DriveHub è un'applicazione desktop per la gestione di una concessionaria: consente ai clienti di consultare veicoli, acquistare, noleggiare e prenotare test drive; venditori e manager gestiscono richieste, catalogo e operazioni. È sviluppata in Java 21 con JavaFX, Maven, JDBC e PostgreSQL 16.

## Avvio rapido su Windows

Servono **JDK 21**, **Maven 3.9+** e **Docker Desktop** installati; Java e Maven devono essere disponibili nel PATH. Avvia Docker Desktop almeno una volta e completa l'eventuale configurazione iniziale. Alla prima esecuzione serve una connessione Internet per scaricare l'immagine PostgreSQL e le dipendenze Maven.

Dalla cartella del progetto fai doppio clic su `run.bat`, oppure esegui:

```powershell
cd C:\DEV\DriveHub
.\run.bat
```

Lo script avvia Docker Desktop se necessario, crea al primo avvio il contenitore `drivehub-quickstart-db` con PostgreSQL e dati demo, quindi avvia l'app. I dati sono conservati nel volume Docker `drivehub-quickstart-data` tra un avvio e l'altro. La porta locale **55432** deve essere libera. Il database demo usa credenziali locali dimostrative e non è destinato alla produzione. Chiudere la finestra dell'app non arresta il database.

| Ruolo | Email | Password demo |
|---|---|---|
| Cliente | `mario.rossi@email.it` | `password` |
| Venditore | `paolo.costa@drivehub.it` | `password` |
| Manager | `vittorio.manager@drivehub.it` | `password` |

Per fermare il database dopo l'uso: `docker stop drivehub-quickstart-db`. Il successivo `run.bat` lo riavvia conservando i dati. Se l'inizializzazione si interrompe o la porta è occupata, controllare il messaggio nella finestra e i log con `docker logs drivehub-quickstart-db`; lo script non reinizializza automaticamente volumi esistenti.

Per configurare PostgreSQL manualmente o avviare l'app su altri sistemi, consulta [installazione e avvio](docs/installation.md). Gli script SQL di schema e demo sono distruttivi e vanno eseguiti solo su un database nuovo o sacrificabile.

## Architettura e funzionalità

La presentazione JavaFX usa servizi applicativi, oggetti di dominio e DAO JDBC; `AppContext` costruisce le dipendenze. La descrizione delle responsabilità, dei flussi e dei pattern è in [architettura](docs/architecture.md); i [diagrammi](docs/diagrams/README.md) includono sorgenti e indicazioni di lettura.

Il prezzo di un veicolo parte dalla base del modello o da una base manuale scelta dal manager. La formula applica lo sconto maggiore tra quello del modello e del veicolo, poi le riduzioni per usato, età e chilometraggio. Nell'acquisto, il totale viene concordato al pagamento dell'acconto del 20%; il saldo è la differenza rispetto a tale totale. Dettagli e motivazioni sono nell'[ADR sui prezzi](docs/adr/0001-base-manuale-e-prezzo-concordato.md).

## Build e test

Le richieste usano il punto di vista della concessionaria: `SALE` per il cliente
che compra (acconto e totale concordato), `PURCHASE` per la cessione del cliente
(stima non negativa, senza totale concordato). Il registro `Sale` conserva ancora
`PURCHASE` per la vendita al cliente e `ACQUISITION` negativa per l'acquisizione.
Questo contratto intermedio è verificabile su database nuovi dedicati; per
aggiornare dati esistenti occorrono anche i ticket 02 e 03. Non eseguire schema/demo
su un'installazione popolata per applicare questa modifica.

```bash
mvn -DskipTests clean package
mvn test
```

Il primo comando compila e crea il JAR; il secondo esegue i test senza database. I test PostgreSQL richiedono database e credenziali dedicati: istruzioni e limiti sono in [build e test isolati](docs/testing.md). Il JAR ordinario non include un runtime o tutte le dipendenze.

## Uso dell'AI

Per questa revisione è stato usato Codex per analizzare l'avvio, predisporre lo script Windows e aggiornare la documentazione. Le note del corso richiedono di documentare le altre eventuali applicazioni dell'AI e il relativo processo: integrare questa sezione con gli usi effettivi del gruppo prima della consegna.
