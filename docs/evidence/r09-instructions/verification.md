# Ticket16 — installazione e rigore teorico

Sessione del3novembre2026, solo16. Letti AGENTS, piano, glossario, ADR0001,
testing ed evidenze15. [Prerequisiti01–15](prerequisites.json) tutti chiusi e
senza criteri aperti. `Relazione_SWE` esclusa da letture, ricerche, copia,
modifiche e verifiche. Nessun commit/staging; nessun intervento sui diagrammi17.

## Versione locale e conservazione

Fotografia iniziale **1422file**, progetto, tracker e piano esterni, esclusi
`.git`, `target` e relazione: [manifest](baseline-sha256.json),
[HEAD](initial-head.txt), [indice](initial-index.sha256).
La copia iniziale è in `/tmp/drivehub-ticket16-session/before`.
Un diff da HEAD attribuirebbe impropriamente il lavoro01–15 a questa sessione:
le review confrontano esclusivamente la fotografia locale.

Installazione verificata in `/tmp/drivehub-ticket16-session/install/DriveHub`,
copiata dalla fotografia della **versione locale corrente**, inclusi file
modificati/non tracciati, non da un clone/checkout del solo HEAD.
[Controllo della copia](installation-copy.json): tutti i file iniziali del
progetto identici salvo `application.properties`, sostituito solo nella copia
con configurazione privata0600 del nuovo DB sacrificabile. Documenti correnti
sincronizzati prima della suite finale. Sorgenti, test, POM, schema/demo e
harness preesistenti del progetto originale non modificati.

## Cambiamenti e motivazione

- README come ingresso, [installazione](../../installation.md) completa e
  [architettura](../../architecture.md) sostenuta da riferimenti ai sorgenti;
  testing consolidato con comandi/confini e ambiente temporaneo esplicitato.
- Java21/Maven/PG16, btree_gist, proprietà effettivamente lette, credenziali
  locali e risorsa nel JAR; niente presunta configurazione applicativa via
  variabili dei test. Avvio ordinario distinto da schema/demo distruttivi.
- Demo facoltativa, credenziali valide per la fixture, schema vuoto senza
  manager/venditore, date storiche e conto/identificativo solo dimostrativi.
- Factory statiche distinte dal Factory Method GoF; Observer sincrono,
  navigazione nello stile Visitor, Singleton, DAO/DI/composition root distinti.
  SOLID/DDD descritti con esempi e compromessi, senza promessa di purezza.
- Dipendenze del codice distinte dal flusso dei dati; costruzione esplicita
  dei servizi e accessi globali residui. Prezzi/transazioni/lock/cancellazioni
  secondo i percorsi attuali, incluso il limite dello snapshot listino
  nell'avvio acquisto e delle query JDBC già avviate.

Nessun refactor, dipendenza nuova, modifica a diagrammi o test artificiale
per il testo. Il probe conservato fra le evidenze verifica invece il confine
reale d'installazione: configurazione PostgreSQL e tre login demo.
Nessun grafo Graphify esistente, nessuna generazione necessaria.

## Ambiente e prove nuove

I file temporanei13–15 erano assenti. Strumenti ricreati da pacchetti Ubuntu
ufficiali estratti, senza installazioni/servizi di sistema:
[versioni](versions.txt), [pacchetti/hash](tool-archives.json),
[comandi e script](commands.md).
Java/javac **21.0.12.1**, Maven **3.9.16** sullo stesso JDK,
server/client PostgreSQL **16.15**. Nuovo cluster solo **127.0.0.1:55434**;
DB/ruoli `drivehub_test` e copia applicativa `drivehub`, senza privilegi
amministrativi; test senza appartenenze o altri DB posseduti.
[Identità/privilegi iniziali](before-suite-environment.txt).
Xvfb isolato **:92**,1440×1000, solo socket locale astratto, senza TCP/Unix
filesystem: [comando](xvfb-command.txt), [log](xvfb.log).
Warning EGL su /dev/dri non impedisce il rendering software osservato.
Servizi e prove eseguiti fuori sandbox per rendere persistenti processi/socket.
Nessun DB applicativo preesistente contattato.

| Prova | Esito nuovo |
|---|---|
| `psql --single-transaction -v ON_ERROR_STOP=1 -f schema.sql` sulla nuova copia applicativa | Schema vuoto, client=0, btree_gist presente |
| Caricamento separato facoltativo `demo.sql` |15clienti,40operazioni; [log](database-setup.log) |
| `mvn -DskipTests clean package` |65sorgenti target21, JAR prodotto, BUILD SUCCESS; test non eseguiti. [Log](build.log), [input](build-inputs.sha256.json) |
| `mvn test` |**47/47**, zero failure/errori/salti; [log](without-db.log), [XML](without-db-surefire/), [riepilogo](without-db-summary.json) |
| Configurazione applicativa e login demo |DB/ruolo/versione/sentinella corretti e tre ruoli autenticati con AuthService; [log](startup-check.log) |
| App.start con JavaFX/Robot |**APP STARTUP SUCCESS**, tema, schede e Tab; [schermata ispezionata](startup-login.png) |
| `mvn javafx:run`, due avvii ordinari |Finestra App900×650 osservata due volte, PIDJava distinti5664/6146; nessuno schema/demo ripetuto. [Primo log](javafx-run-1.log), [finestra1](javafx-window-1.txt), [secondo log](javafx-run-2.log), [finestra2](javafx-window-2.txt). Goal interrotti dal launcher dopo l'osservazione |
| `mvn -Ppostgres-tests clean test` finale |**394/394**,47senzaDB/347PostgreSQL, zero failure/errori/salti; [log](full.log), [XML](full-surefire/), [riepilogo](full-summary.json), [input](full-inputs.sha256.json) |

Il394/394 del15 resta storico; i log/XML qui sono nuove esecuzioni16.
Include DatabaseIsolationIntegrationTest, fixture/demo e tutte le regressioni
precedenti con gare PostgreSQL effettive, PID/blocker e rollback.
Test e harness non eseguiti contemporaneamente.

La suite lascia intatta la copia applicativa: sentinella finale riletta
**READ ONLY** e hash di tutte le13tabelle/7sequenze identici
[prima](before-suite-application.sha256.json)/[dopo](after-suite-application.sha256.json).
[Controllo finale](after-suite-environment.txt), [arresto](shutdown.txt),
[log server](postgres.log). I guasti SQL nel log sono quelli deliberati delle
regressioni. PostgreSQL e Xvfb arrestati; verifica successiva fuori sandbox: listener55434,
socketX92 e PIDXvfb assenti, come registrato in shutdown.txt.

## Review e stato

Review indipendenti [Standards](standards-review.md) **0violazioni/0smell
azionabili**, [Spec](spec-review.md) **PASS,0rilievi**. [Patch congelata](review.patch),
[15file](review-files.txt), [hash input review](review-files.sha256.json), SHA256
`5302f7b4445f7e12af229b056d9a3cf46a8b9c5172f00c74f0d314af3035ca0b`. Confronto con fotografia locale iniziale,
non HEAD; verificati anche sorgenti, manifest/log/XML e confini delle prove.

[Delta documentale di chiusura](closing-docs.patch) separato dalla review
congelata; [ricontrollo indipendente](closing-review.md).
[Diff finale](session.patch), [hash file della sessione](session-files.sha256.json).
La chiusura non costituisce una nuova prova runtime. Nessun input produttivo
o harness modificato dopo le prove e la review.

[Conservazione](preservation.json): **1422file presenti,1418identici,
4aggiornamenti documentali intenzionali,zeroeliminazioni**; ticket/evidenze01–15
e successivi intatti, HEAD/indice invariati. [Controlli documentali](document-checks.json):
link locali e whitespace del diff verificati, integrità della fotografia e
input runtime identici build→47test→394test; password private assenti dalle
prove. Temporanei privati non pubblicati, sessione0700/env0600.

**6/6criteri verificati, ticket16 chiuso; AC15/#15 e AC18/#18 completati,
17/20rilievi chiusi**. R09 resta parziale per diagrammi17 e AC13 trasversale.
Nessun commit/staging, nessun ticket successivo avviato.

Limiti: Linux/WSL e Xvfb, nessuna nuova certificazione screen reader, altro
OS/display, installer autonomo o benchmark. Il goal Maven prova l'avvio della
finestra, i login sono verificati tramite i servizi; non tutti i flussi sono
riesercitati nella GUI. Nessuna migrazione completa di installazioni popolate,
nuova garanzia globale di concorrenza o risoluzione dei diagrammi17.
R09 resta parziale per17 e AC13 resta trasversale.
