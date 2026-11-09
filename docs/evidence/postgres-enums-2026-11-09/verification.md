# Quattro enum nativi PostgreSQL

Intervento ADR0002 sulla branch `dev`, baseline `main` / `8d089bc`.
L'utente ha approvato anche l'aggiornamento locale e la rimozione dello script
temporaneo dopo l'uso. Il database applicativo non è stato usato dalla suite.

## Codice e contratto

`vehicle.price_mode` usa `price_mode` (`AUTOMATIC`, `MANUAL`); `sale.type` usa
`sale_type` (`SALE`, `PURCHASE`, `PENALTY`). `vehicle_state` e `request_type`
conservano valori e ordine precedenti. I quattro domini sono distinti anche
in PostgreSQL. Restano `NOT NULL`, il default `AUTOMATIC` e gli altri vincoli.

Le tre scritture di modalità prezzo in `PostgresVehicleDao` e l'inserimento in
`PostgresSaleDao` convertono esplicitamente il parametro al rispettivo tipo SQL,
come già avveniva per stato veicolo e tipo richiesta. Nessuna modifica a formule,
importi, significati delle operazioni, demo, servizi o interfaccia grafica.

## Test e build

Ambiente: OpenJDK 21.0.12.1 Linux, Maven 3.9.16, PostgreSQL 16.15 su Docker.
JDK e client PostgreSQL 16.13 sono stati estratti da pacchetti ufficiali soltanto
in `/tmp`: nessuna modifica al Java di sistema o alle dipendenze del progetto.
Il contenitore temporaneo `drivehub-enums-check`, porta 55441, ha ospitato database
separati per test, sentinella, ripristino e prova della conversione. Il ruolo
`drivehub_test` possiede soltanto il database omonimo ed è privo di privilegi
amministrativi. Password passate tramite ambiente, fuori dai log e dal repository.

| Fase | Risultato | Evidenza |
|---|---|---|
| Contratto prima della modifica | 4 test, 2 failure attese: colonne ancora `character varying` | [contract-red.txt](contract-red.txt) |
| Nuovo schema, DAO precedenti | Contratto 4/4; 77 errori JDBC attesi su 81 test | [Log integrali compressi](tdd-failures.zip), `dao-red.txt` |
| Cast prezzo aggiornati | Restano errori sul parametro `sale_type` e SQLSTATE atteso precedente | [Log integrali compressi](tdd-failures.zip), `price-green-sale-red.txt` |
| Contratto, prezzi, registro, ricaricamenti demo | **82/82**, nessun errore o salto | [targeted-green.txt](targeted-green.txt) |
| Suite completa | **423/423**, zero failure, errori e salti | [full-suite.txt](full-suite.txt) |
| JAR ordinario | Build riuscita; test già eseguiti nella fase precedente | [package.txt](package.txt) |

Comandi Maven, con `JAVA_HOME` Linux 21 e variabili esplicite di test/sentinella:

```bash
mvn -o -B -Ppostgres-tests -Dtest=PostgresEnumIntegrationTest test
mvn -o -B -Ppostgres-tests -Dtest=PostgresEnumIntegrationTest,PriceSelectionIntegrationTest,SaleOutcomeIntegrationTest,DemoSqlValidationTest test
mvn -o -B -Ppostgres-tests clean test
mvn -o -B -DskipTests package
git diff --check
```

Il nuovo test verifica tramite SQL tipo nativo, etichette e rifiuto dei valori
estranei per tutti e quattro gli attributi. Il rifiuto dell'operazione
`ACQUISITION` nel test esistente resta verificato senza modificare i dati;
SQLSTATE passa da `23514` (CHECK) a `22P02` (valore non valido per l'enum).
I test esistenti coprono persistenza delle modalità prezzo, tutti i tipi di
registro, importi zero, commit, rollback, concorrenza e isolamento.

## Backup, prova su copia e aggiornamento locale

Backup e fotografie complete privati, con directory 700 e file 600:
`/home/federico/dev/Drivehub/.drivehub-backups/2026-11-09-enums/`.
Nessun dump o dato personale è stato aggiunto al repository.

Il backup iniziale è stato ripristinato su un database distinto. Ogni riga delle
14 tabelle, incluso il marker del launcher, e tutte le 7 sequenze coincidono
con l'originale. La conversione su copia ha conservato tutti questi valori.
Un errore provocato dopo gli effettivi `ALTER` ha verificato il rollback completo
di dati e schema. La prima prova del verificatore si era fermata prima degli
`ALTER` perché tentava di leggere la sequenza come tipo composito: corretto il
solo controllo temporaneo, la prova di rollback è poi riuscita.

Una seconda esecuzione è stata rifiutata senza modifiche; etichette, default e
nullabilità della copia convertita coincidono con quelli di uno schema nuovo.
I DAO aggiornati hanno letto anche la copia dei dati reali:
[controllo sulla copia](converted-copy-dao.txt).

Prima della scrittura locale sono stati ricontrollati dati, schema e assenza di
connessioni applicative. La stessa conversione provata è stata applicata in una
transazione con lock e limiti di attesa, controllando al suo interno righe e
sequenze prima del commit. Non sono stati invocati servizi di pagamento né
ricaricati schema o demo sull'originale.

Risultato locale: **56 veicoli, 13 richieste, 41 operazioni**, con tutte le righe,
associazioni, date, accordi, prezzi, saldi e sequenze invariati. Sono cambiate
soltanto le due definizioni di colonna previste; rimossi i due CHECK sostituiti
dagli enum. Il backup finale è stato a sua volta ripristinato e verificato.
Una lettura tramite DAO sul database locale ha confermato tutti gli enum senza
movimenti applicativi: [controllo locale](local-dao.txt). Ricontrollate anche tutte
le righe e sequenze della sentinella, rimaste identiche.

Lo script temporaneo è stato rimosso; non sono consegnati runner di migrazione,
fixture storiche o test dipendenti dalla conversione. Restano test sul contratto
corrente e queste evidenze. [Esiti strutturati e hash dei backup](database-checks.json).

## Limiti e ripristino

[Review indipendenti Standards e Spec](review.md): zero rilievi su entrambi gli
assi; limiti della review documentati nel rapporto. Dopo la review sono stati
aggiunti soltanto il rapporto e gli esiti della pulizia temporanea.

Il PostgreSQL temporaneo è stato arrestato regolarmente con `pg_ctl`; la porta
55441 è chiusa e quello applicativo sulla 55432 è rimasto attivo con quattro enum.
La rimozione del contenitore di prova e del suo volume è rimasta bloccata
dall'errore WSL `UtilAcceptVsock: accept4 failed 110` all'avvio del client Docker
Windows. Rimane quindi il contenitore **spento** `drivehub-enums-check`;
per completare la sola pulizia da un terminale Windows con Docker funzionante:

```bash
docker rm --volumes drivehub-enums-check
```

Nessuna nuova esecuzione GUI/Robot o di `run.bat`; sono verificati build, DAO e
flussi persistenti. Le viste non sono cambiate e i diagrammi correnti descrivono
enum Java e flussi invariati. Le prove non certificano altri sistemi operativi
o installazioni e il launcher non converte automaticamente altri database.

Tornare a `main` non annulla la modifica PostgreSQL: per usare il codice precedente
occorre ripristinare anche il backup `before-enums.dump` su un database separato
e verificare il ripristino prima di sostituire quello in uso.
