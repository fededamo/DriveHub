# Comandi osservati — ticket17

Sessione3novembre2026; radice `/home/federico/dev/Drivehub/DriveHub`.
Baseline prima di qualunque modifica: `/tmp/drivehub-r17-baseline/tree`,
progetto + tracker + piano, esclusi `.git`, `target`, `Relazione_SWE`.
Inclusi file ignorati (per esempio log storici), modificati e non tracciati.
Salvati SHA256, `git rev-parse HEAD`, hash indice, `git diff --binary HEAD`
con esclusione relazione, diff indice e stato iniziale.01–16 controllati chiusi
e senza criteri aperti, [hash dei ticket](prerequisites.json).

## Ambiente isolato

Java predefinito17: non usato. Verificati nuovamente i binari estratti16 ancora
presenti sotto `/tmp/drivehub-ticket16-tools/root`, non presunti disponibili:
Java/javac21.0.12.1, Maven3.9.16 sullo stesso JDK, PostgreSQL client/server16.15,
Xvfb e librerie (`ldd` senza dipendenze mancanti), xkbcomp in `/tmp/x16`.
I pacchetti d'origine sono quelli del [registro16](../r09-instructions/tool-archives.json);
versioni17 in [environment.txt](environment.txt) e [versions.txt](versions.txt).
Nessuna installazione o servizio di sistema. `/tmp` resta temporanea.

[prepare.py](prepare.py) è il launcher16 adattato solo a directory17, porta55435
e display91. Copia la baseline, crea credenziali casuali in `env.json`0600
all'interno della sessione0700 e configura **solo la copia** dell'applicazione.
Il file privato non è pubblicato. [setup-db.py](setup-db.py) crea ruoli non
amministrativi `drivehub_test`/`drivehub`, DB separati, schema/demo e sentinella
solo nel nuovo cluster sacrificabile, non sulla porta5432.

```bash
python3 /tmp/drivehub-ticket17-session/prepare.py
pg_ctl -D /tmp/drivehub-ticket17-session/pgdata \
  -l /home/federico/dev/Drivehub/DriveHub/docs/evidence/r09-diagrams/postgres.log -w start
python3 /tmp/drivehub-ticket17-session/setup-db.py
python3 /tmp/drivehub-ticket17-session/check-db.py before-suite
```

PostgreSQL è stato inizialmente avviato nella sandbox e non è rimasto disponibile
fra comandi: primo mirato79test,77errori di connessione, non una regressione del
prodotto. [Log](sandbox-failed-targeted.log), [riepilogo](sandbox-failed-targeted-summary.json).
Riavviato **lo stesso nuovo cluster17** fuori sandbox; mirato ripetuto riuscito.
I servizi devono sopravvivere ai singoli comandi: PostgreSQL e Xvfb avviati fuori
sandbox; nessuna escalation rifiutata. [Comando Xvfb](xvfb-command.txt), solo
socket locale astratto91, senza TCP/Unix filesystem. Warning EGL non impedisce
il rendering software osservato; il solo PID non è considerato prova JavaFX.

## Prove runtime nuove

Dalla copia `/tmp/drivehub-ticket17-session/install/DriveHub`:

```bash
python3 /tmp/drivehub-ticket17-session/run-maven.py targeted -Ppostgres-tests \
  -Dtest=DatabaseIsolationIntegrationTest,PurchaseAgreementIntegrationTest,SaleOutcomeIntegrationTest,CatalogListingIntegrationTest,BookingRulesTest clean test
# Con JAVA_HOME/DISPLAY e le sei variabili DB nell'ambiente privato:
DRIVEHUB_UI_CLASS=it.unifi.drivehub.TableLifecycleUiCheck \
  python3 docs/evidence/r08-tables/run-ui.py
python3 /tmp/drivehub-ticket17-session/run-maven.py full -Ppostgres-tests clean test
python3 /tmp/drivehub-ticket17-session/check-db.py after-suite
pg_ctl -D /tmp/drivehub-ticket17-session/pgdata -m fast -w stop
```

[run-maven.py](run-maven.py) riusa16, aggiungendo solo l'etichetta `targeted`.
Salva input SHA256, log e riepilogo degli XML realmente prodotti; rimuove solo
`properties` ambientali dagli XML conservati, senza alterare testcase/esiti/output.
La compilazione è quella Maven reale, senza modificare il target21 o aggiungere test.
Le prove grafiche e le suite sono sequenziali, mai concorrenti sul DB.

AppStartupUiCheck15 è compilato con `javac --module-path` dei JAR JavaFX21.0.2,
`--add-modules javafx.controls`, classpath `target/classes`, `target/test-classes`,
driver PostgreSQL42.7.3 e directory UI temporanea. Avvio con `java`, stessi
module-path/classpath, `-Dprism.order=sw`, argomenti classe
`it.unifi.drivehub.AppStartupUiCheck` e directory UI. Comandi completi in
[startup-check.log](startup-check.log). Nessun nuovo harness o test sul testo.

Il primo avvio della suite finale si è interrotto con143, senza riepilogo:
[log parziale](interrupted-full.log), [input](interrupted-full-inputs.sha256.json).
Origine del SIGTERM non determinata; non è attribuito alcun successo.
Ripetuto con [complete-full.py](complete-full.py), launcher autonomo con
`start_new_session=True`, log/stato d'uscita persistenti: **exit0,394/394**.
[Log launcher](full-launcher.log). Il394/394del16 rimane storico.

[check-db.py](check-db.py) controlla DB/ruoli/privilegi e sentinella READ ONLY,
impronte ordinate di13tabelle/7sequenze prima e dopo tutte le prove. Arrestati
solo pgdata17 e il PID Xvfb91 registrato, controllati listener/socket assenti:
[shutdown.txt](shutdown.txt). Nessun DB applicativo preesistente contattato.

## Rendering e ispezione

JAR scaricato da Maven Central, versione fissata1.2026.2:
[origine e SHA256](renderer.json). Nessuna dipendenza nel POM.
`java -jar ... -version` segnala `dot` assente ed esce16; i render strutturali
usano **Smetana incluso** e quelli di sequenza il renderer incluso: Graphviz
esterno non necessario. Prime due sintassi corrette prima del render finale;
i tentativi e i comandi sono in [render.log](render.log).

```bash
java -XX:+PerfDisableSharedMem -Djava.awt.headless=true \
  -jar /tmp/drivehub-ticket17-session/plantuml.jar \
  -charset UTF-8 -failfast2 -tsvg docs/diagrams/current/*.puml
java -XX:+PerfDisableSharedMem -Djava.awt.headless=true \
  -jar /tmp/drivehub-ticket17-session/plantuml.jar \
  -charset UTF-8 -failfast2 -tpng \
  -o /tmp/drivehub-ticket17-session/diagram-png docs/diagrams/current/*.puml
```

Entrambi i render finali exit0. SVG conservati vicino ai sorgenti; PNG temporanei
usati per ispezione, senza duplicare i18render nel repository.
Una prima impaginazione troppo larga, etichette di navigazione sovrapposte e
`maxMessageSize55` (pixel) corretti: firme DAO abbreviate, raggruppamento visivo,
navigazione generale semplificata e messaggi a280pixel. Nuovo render e ispezione
dei file cambiati. SVG zoomabili; nessuna verifica di impaginazione su carta/A4.
Tentativo di contact sheet non eseguito per Pillow assente: nessuna dipendenza
aggiunta, ispezionati direttamente i PNG singoli con il visualizzatore.

Controllati riferimenti locali delle pagine modificate, hash sorgenti/render,
assenza di pagine errore SVG, whitespace delle sole righe aggiunte, preservazione
baseline e identità HEAD/indice. Sono controlli degli artefatti, non nuovi test
artificiali della documentazione. Diff congelato per review indipendenti rispetto
alla fotografia locale iniziale, mai rispetto al solo HEAD. Relazione sempre esclusa.

Review Spec iniziale:1P2 sui self-loop di test drive/ricarica cliente, che nel
codice chiudono il dialogo prima del comando/esito. Corretto il solo diagramma,
brevi etichette di ritorno e note separate per annullamento/OK; nuovi SVG/PNG exit0,
hash e ispezione. [Patch correttiva](review-correction.patch),
[hash](review-correction-files.sha256.json), [follow-up Spec](spec-followup.md)
e [Standards](standards-followup.md):0rilievi residui. Nessuna suite ripetuta per
questa correzione documentale, nessun input runtime modificato.
