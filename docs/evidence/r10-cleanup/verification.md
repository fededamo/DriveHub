# Ticket 15 — pulizia minima

Sessione del 2 novembre 2026, soltanto15. Letti AGENTS, piano, glossario,
ADR0001, testing ed [evidenze14](../r08-tables/verification.md).
[Prerequisiti01–14](prerequisites.json) chiusi, senza criteri aperti.
`Relazione_SWE` esclusa da letture, ricerche, snapshot, diff e verifiche.
Nessun commit/staging; nessun ticket successivo avviato.

## Baseline e perimetro

Fotografia iniziale **1276 file**, progetto, tracker e piano esterni, in
`/tmp/drivehub-ticket15-session/before`; esclusi `.git`, `target` e la relazione.
[Manifest](baseline-sha256.json), [HEAD](initial-head.txt), [indice](initial-index.sha256).
Il diff da HEAD comprenderebbe01–14: le review usano soltanto questa fotografia.

Nuova baseline `mvn -Ppostgres-tests clean test`: **394/394**,47 senzaDB e
347 PostgreSQL, zero failure/errori/salti, isolamento/sentinella compresi.
[Log](baseline.log), [XML](baseline-surefire/), [input](baseline-files.sha256.json).
Il394/394 del14 rimane storico: non è stato usato come prova corrente.

## Riferimenti e riduzione

[Ricerca iniziale](active-fxml-session-before.txt),
[tutti i file con riferimenti, comprese prove storiche](all-reference-files-before.txt),
[chiamanti AppContext e callback](context-callers-before.txt),
[risorse e moduli](resources-modules-before.txt).
Ricerca case-insensitive anche nei file nascosti, esclusi `.git`, `target` e
la relazione. Le prove storiche sono incluse nell'inventario dei riferimenti,
ma separate dai sorgenti e dalla documentazione corrente.
Controllo supplementare con `--no-ignore`, anche sulle estensioni FXML
maiuscole: [prima](all-including-ignored-before.txt),
[dopo](all-including-ignored-after.txt), nessun ulteriore uso o risorsa.

| Elemento | Riscontro e intervento |
|---|---|
| `javafx-fxml` | Solo dipendenza e commento POM; nessuna risorsa `.fxml`, import, loader, reflection o dichiarazione di modulo. Rimossa la dipendenza e corretto il suo commento |
| `AppContext.currentUser` | Solo dichiarazione e corpo dei tre metodi, nessun uso esterno; rimosso |
| `getCurrentUser`, `setCurrentUser`, `AppContext.logout` | Nessun chiamante in sorgenti, test o harness; rimossi |
| Import `domain.user.User` in AppContext | Serviva soltanto ai membri rimossi; eliminato |
| Login e disconnessione reali | `LoginView` passa l'utente a `App.showDashboardForUser`; `User.openDashboard` sceglie la vista. Le tre dashboard conservano il proprio utente e chiamano `onLogout`, che in App è `showLoginView`. Nessuna modifica |
| Test, fixture e persistente | Nessun chiamante da aggiornare; `src/test`, DAO, schema e demo invariati. Nessun test che rispecchi le rimozioni |

[Ricerca finale corrente](active-fxml-session-after.txt) e
[sorgenti/POM](runtime-fxml-session-after.txt): nessun riferimento runtime agli
elementi rimossi. Le callback di disconnessione restano usate.
I diagrammi `class-presentation`, `sequence-rent-payment` e `page-navigation`
descrivono già prima dell'intervento componenti FXML/SessionContext assenti:
incoerenza preesistente, non prodotta da15, rimane al lavoro documentale16–17.
README e istruzioni non descrivono i membri rimossi. Prove storiche intatte.

[Riduzione misurata](reduction.json): **una dipendenza, un campo, tre metodi,
un import; 20 righe nette in meno** fra i due file di produzione.
POM128→122righe, AppContext170→156; nessuna nuova dipendenza o comportamento.
Nessun grafo Graphify presente; nessuna generazione necessaria per questa pulizia.

## Ambiente ricontrollato

I file temporanei13/14 erano ancora presenti: verificati binari, versioni,
configurazione privata0600 e destinazioni prima del riuso.
Java Linux **21.0.12.1**, javac21, Maven **3.9.16** sullo stesso Java,
PostgreSQL **16.15**: [versioni](versions.txt).
Nessun abbassamento del requisito21, nessuna installazione di sistema.

Solo cluster sacrificabile `/tmp/drivehub-ticket13-pgdata`,
**127.0.0.1:55433**, ruolo e DB `drivehub_test`, senza privilegi amministrativi,
appartenenze o altri DB posseduti. Copia applicativa `drivehub` separata con
sentinella. [Controlli iniziali in sola lettura](environment.txt).
Sei variabili private in `/tmp/drivehub-ticket15-session/env.json`,0600,
mai copiate nelle prove. Nessun DB applicativo preesistente contattato.

Xvfb isolato **:93**,1440×1000, rendering software, listener TCP e Unix
filesystem disabilitati, solo socket locale astratto:
[comando](xvfb-command.txt), [log](xvfb.log).
Avvio e prove fuori sandbox per mantenere processi e socket fra comandi.
Nessun tentativo fallito presentato come prova riuscita.
Se `/tmp` scompare, ricreare l'ambiente secondo [testing](../../testing.md).

## Comandi ed esiti

Launcher privato `python3 /tmp/drivehub-ticket15-session/maven.py <etichetta>
<argomenti>`: Java21, sei variabili private, `mvn -B -o`, log/XML e hash input.

| Etichetta / comando Maven | Esito |
|---|---|
| baseline `-Ppostgres-tests clean test` |394/394,47 senzaDB/347 PostgreSQL, zero failure/errori/salti |
| compile `-DskipTests compile` |65 sorgenti compilati, target21, BUILD SUCCESS; nessun test dichiarato eseguito da questo comando |
| final-full `-Ppostgres-tests clean test` |394/394,47 senzaDB/347 PostgreSQL, zero failure/errori/salti |

[Compilazione](compile.log), [input](compile-files.sha256.json),
[suite finale](final-full.log), [XML](final-full-surefire/),
[input finali](final-full-files.sha256.json), [riepilogo](summary.json).
Include tutte le regressioni01–14, fixture, isolamento e sentinella.
Classpath Surefire di baseline con FXML, finale senza FXML.
I warning Maven del modello OpenJFX sono già presenti nella baseline.
Nessun TDD artificiale per le cancellazioni; verifiche di compilazione e avvio
al confine reale, suite esistente integralmente rieseguita.

## Avvio e GUI

Il [controllo di avvio](AppStartupUiCheck.java) riusa Robot05 e i frame14,
invoca il vero `new App().start(stage)` sul thread JavaFX. Finestra LoginView
visibile, titolo e tema, due schede, pulsante raggiungibile con Tab:
**APP STARTUP SUCCESS**. Non esegue login o query DB.
[Log](startup-ui.log), [schermata ispezionata](startup-startup-login.png),
[input](startup-files.sha256.json), [hash harness](startup-harness.sha256.json).

Compilazione e comando di avvio equivalenti, dalla radice del progetto,
con `JAVA_HOME`21, `DISPLAY` isolato, `JFX` contenente soltanto i jar Linux
javafx-base/graphics/controls21.0.2, `JDBC` il driver PostgreSQL42.7.3,
`UI_DIR` directory temporanea e `PRIVATE_CONFIG` directory privata contenente
`application.properties` per la sola copia sacrificabile:

```bash
CP="$PRIVATE_CONFIG:target/classes:target/test-classes:$JDBC:$UI_DIR"
"$JAVA_HOME/bin/javac" --module-path "$JFX" --add-modules javafx.controls \
  -cp "$CP" -d "$UI_DIR" docs/evidence/r03-prices/PriceUiCheck.java \
  docs/evidence/r08-tables/ReactiveTablesUiCheck.java \
  docs/evidence/r10-cleanup/AppStartupUiCheck.java
"$JAVA_HOME/bin/java" --module-path "$JFX" --add-modules javafx.controls \
  -Dprism.order=sw -cp "$CP" it.unifi.drivehub.AppStartupUiCheck "$UI_DIR"
DRIVEHUB_UI_CLASS=it.unifi.drivehub.TableLifecycleUiCheck \
  python3 docs/evidence/r08-tables/run-ui.py
```

Il launcher14 è stato riusato senza modificarlo con output separato e
variabili DB dedicate; harness e suite mai contemporanei.
Ciclo completo **UI CHECK SUCCESS**: caricamento bloccato/PID, prezzi aggiornati,
errore SQL/riprova, vecchio risultato/errore ignorati, tre ruoli, celle e
scorrimento senza nuove query/connessioni, tastiera:
[log](lifecycle-ui.log), [input](lifecycle-files.sha256.json).
Le sette nuove schermate sono conservate con prefisso `lifecycle-`.
Nessun cambiamento grafico o certificazione screen reader/altro OS/display;
nessun nuovo benchmark. Il controllo di avvio invoca App.start, non verifica
il packaging o il goal Maven javafx:run.

Input `src` e POM identici fra compilazione, avvio, ciclo grafico e suite finale;
nessun codice/test/harness cambiato dopo queste prove.
[Sentinella finale READ ONLY](sentinel-final.txt),
[arresto PostgreSQL e Xvfb](shutdown.txt), [log PostgreSQL](postgres.log).

## Review e chiusura

Review indipendenti [Standards](standards-review.md) **0 violazioni residue/0
smell azionabili**, [Spec](spec-review.md) **0 rilievi**. Unica osservazione
iniziale Standards: commento tracker anteposto allo storico, corretto e
ricontrollato dal revisore prima di chiudere. [Patch congelata](review.patch),
[file](review-files.txt), [hash](review-files.sha256.json),7file, SHA256
`e9f6ca73b53ea5104eb82526235e0a4d1004f8fbef91558f9528952c8a4fdb7b`.
Le review confrontano la fotografia della sessione, senza attribuire01–14.

[Delta documentale di chiusura](closing-docs.patch),
[controllo indipendente del delta](closing-review.md),
[diff finale della sessione](session.patch), [hash finali](session-files.sha256.json).
Gli aggiornamenti documentali non costituiscono nuove prove runtime.
[Conservazione](preservation.json):1276file iniziali presenti,1271 identici,
5 aggiornamenti intenzionali,zero eliminazioni; test, ticket/evidenze01–14
e successivi intatti. HEAD/indice invariati. Controllati hash input runtime,
integrità della fotografia, whitespace del diff, link locali e assenza delle
password private nelle prove. I servizi risultano arrestati anche fuori sandbox:
pg_ctl status3, PID Xvfb assente, listener55433/X93 non disponibili.

**4/4 criteri verificati, ticket15 chiuso, R10 e AC20/#20 completati,
15/20 rilievi chiusi**. AC13 resta trasversale. Nessun residuo15 o ticket
successivo avviato; nessun commit/staging, `Relazione_SWE` sempre esclusa.
