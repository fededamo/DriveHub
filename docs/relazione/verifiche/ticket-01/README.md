# Ticket 01 — Struttura LaTeX e capitolo 1

Verifica documentale del **12 novembre 2026**, sul repository
`/home/federico/dev/Drivehub/DriveHub`, HEAD
`61ce18ae1d4e0458b63e970b793b0b3a634a3adf`.
Il ticket è quello fornito nella conversazione: non è stato recuperato o
pubblicato un file di issue. Nessun commit è stato creato.

## Risultato

- [PDF della relazione](../../relazione-drivehub.pdf): 15 pagine A4.
- [Estratto del capitolo 1 per revisione](capitolo-1.pdf): pagine numerate 1–3,
  corrispondenti alle pagine fisiche 4–6 del documento completo.
- [Sorgente principale](../../main.tex) e [capitolo 1](../../capitoli/1_introduzione.tex).
- Frontespizio con i dati confermati; cinque capitoli, appendici A–C e tutte
  le 25 sezioni dell'indice concordato, nello stesso ordine.
- Capitolo 1 completo nelle tre sezioni Descrizione, Stack tecnologico e
  Semplificazioni adottate. Capitoli 2–5 e appendici sono esplicitamente
  provvisori. La relazione completa rimane da redigere nei ticket successivi.

## Riscontro delle affermazioni del capitolo 1

Le fonti sono state lette e confrontate staticamente: **non è stata avviata
l'applicazione e non è stato interrogato o inizializzato alcun database**.

| Affermazione | Fonti correnti riscontrate |
| --- | --- |
| Java 21, JavaFX 21.0.2, driver PostgreSQL 42.7.3, JUnit 5.10.2 e plugin Maven | [pom.xml](../../../../pom.xml). Non risultano dipendenze H2, Mockito o JaCoCo nel POM corrente. |
| Viste costruite in Java, CSS e dashboard per ruolo | [App](../../../../src/main/java/it/unifi/drivehub/App.java), [LoginView](../../../../src/main/java/it/unifi/drivehub/presentation/view/LoginView.java), le tre dashboard in `presentation/view` e `src/main/resources/css/style.css`. Nessun FXML nelle risorse correnti. |
| Letture asincrone delle tabelle, aggiornamenti sul thread JavaFX | [AsyncLoad](../../../../src/main/java/it/unifi/drivehub/presentation/view/AsyncLoad.java), callback di `Task` e utilizzi nelle dashboard. Non si afferma che ogni azione della UI sia asincrona. |
| Cliente, venditore e manager con responsabilità distinte | `ClientDashboardView`, `SalesmanDashboardView` e `ManagerDashboardView`, confrontate con i servizi invocati e la composizione in [AppContext](../../../../src/main/java/it/unifi/drivehub/AppContext.java). |
| Registrazione soltanto del cliente e conto inizialmente vuoto | `LoginView.handleRegister`, [AuthService](../../../../src/main/java/it/unifi/drivehub/service/AuthService.java) (`registerClient`, `topUpClientBalance`, `generateUniqueIban`) e [BankAccount](../../../../src/main/java/it/unifi/drivehub/domain/user/BankAccount.java). Gli account staff sono presenti nei dati demo; la UI pubblica non li crea. |
| Acconto del 20%, totale concordato e saldo; base manuale nella stessa formula | [PriceCalculatorService](../../../../src/main/java/it/unifi/drivehub/service/PriceCalculatorService.java), [PurchaseService](../../../../src/main/java/it/unifi/drivehub/service/PurchaseService.java), `Request` e [ADR 0001](../../../adr/0001-base-manuale-e-prezzo-concordato.md). |
| Distinzione vendita al cliente / acquisto dal cliente e registro economico | [Glossario](../../GLOSSARY.md), `RequestType`, `SaleType`, servizi e testi delle dashboard correnti. |
| JDBC senza ORM, connessioni configurate e transazioni | [DatabaseManager](../../../../src/main/java/it/unifi/drivehub/dao/DatabaseManager.java), `AppContext` e DAO in `dao/postgres`. Nel file `application.properties` sono state verificate le chiavi, senza riportarne i valori. |
| PostgreSQL 16, quattro enum nativi e inizializzazione separata dall'app Java | [README applicativo](../../../../README.md), [schema](../../../../src/main/resources/db/schema.sql), [ADR 0002](../../../adr/0002-enumerazioni-native-postgresql.md), `App`, `AppContext`, `DatabaseManager` e [run.ps1](../../../../run.ps1). Lo script Windows inizializza il proprio DB nuovo e conserva il volume; non è un bootstrap Java all'avvio. |
| Test senza database distinti dal profilo PostgreSQL | `pom.xml` e [istruzioni dei test](../../../testing.md). Non viene attribuito un nuovo risultato a queste prove. |
| Processi non modellati: sedi, officina, contratti, fatturazione e integrazioni bancarie | Entità correnti in `domain`, tabelle di `schema.sql`, servizi e funzioni esposte nelle tre dashboard; confronto con [architettura](../../../architecture.md). Il limite riguarda i processi applicativi, non l'assenza di eventuali dati anagrafici. |

La [relazione precedente](../../../../Relazione_SWE/main.tex) è stata usata per
impaginazione e struttura, senza copiarne il testo tecnico. Il confronto con
[indice](../../indice.md), [piano](../../piano.md) e [specifica](../../tracker/spec.md)
ha fissato titoli e ordine. Le sottosezioni strutturali di progettazione sono
predisposte; il dettaglio dei capitoli ancora provvisori sarà sviluppato nella
rispettiva stesura, verificando i nomi correnti invece di replicare classi rimosse.

## Ambiente e compilazione effettiva

Ubuntu 26.04.1 LTS in WSL; strumenti LaTeX e draw.io assenti dal `PATH`.
Sono stati scaricati dai progetti ufficiali ed estratti **soltanto in `/tmp`**:
[Tectonic 0.17.0](https://github.com/tectonic-typesetting/tectonic/releases/tag/tectonic%400.17.0)
per Linux x86-64 musl e
[draw.io Desktop 32.6.10](https://github.com/jgraph/drawio-desktop/releases/tag/v32.6.10)
dal pacchetto amd64 Debian. Nessuna installazione di sistema o dipendenza
aggiunta al software applicativo.

PyMuPDF 2.1 è stato installato con `uv` in un ambiente temporaneo dedicato:

```bash
UV_CACHE_DIR=/tmp/drivehub-relazione-tools/uv-cache uv venv /tmp/drivehub-relazione-tools/venv
UV_CACHE_DIR=/tmp/drivehub-relazione-tools/uv-cache uv pip install --python /tmp/drivehub-relazione-tools/venv/bin/python pymupdf
```

Dalla directory `docs/relazione`, prima compilazione con download delle risorse:

```bash
mkdir -p build
XDG_CACHE_HOME=/tmp/drivehub-relazione-tools/cache /tmp/drivehub-relazione-tools/tectonic --untrusted --keep-intermediates --keep-logs --outdir build main.tex > build/compilazione.txt 2>&1
```

Compilazione finale, eseguita anche senza rete usando esclusivamente la cache:

```bash
XDG_CACHE_HOME=/tmp/drivehub-relazione-tools/cache /tmp/drivehub-relazione-tools/tectonic --only-cached --untrusted --keep-intermediates --keep-logs --outdir build main.tex > build/compilazione.txt 2>&1
cp build/main.pdf relazione-drivehub.pdf
/tmp/drivehub-relazione-tools/venv/bin/python verifiche/ticket-01/controlla_pdf.py > verifiche/ticket-01/controlli-pdf.txt
sha256sum -c verifiche/ticket-01/SHA256SUMS
```

Esito della compilazione: **codice di uscita 0**, PDF prodotto da TeX e
`xdvipdfmx`, indice e riferimenti stabilizzati tramite passaggi automatici.
[Output della compilazione finale](compilazione.txt), [log LaTeX](latex-log.txt),
[controlli PDF](controlli-pdf.txt), [script del controllo documentale](controlla_pdf.py)
e [impronte dei sorgenti e degli artefatti](SHA256SUMS).

Il controllo apre il PDF, ne renderizza tutte le pagine e confronta capitoli,
appendici e sezioni con `indice.md`. Sono verificati **50 collegamenti interni**
con destinazioni risolte, **7 font incorporati**, formato A4 e assenza di
cifratura. Il log finale non contiene warning, box sovrapposti o troppo laschi,
caratteri mancanti o riferimenti irrisolti. Gli intermedi sono in `build/`,
ignorata da Git; il PDF autonomo è conservato fuori da tale directory.

## Prova draw.io

Prova separata in [drawio/](drawio/prova-esportazione.drawio), senza inclusioni
nei sorgenti LaTeX. La skill draw.io è stata letta e sono state consultate le
[regole XML ufficiali](https://raw.githubusercontent.com/jgraph/drawio-mcp/main/shared/xml-essentials.md).
Il diagramma ha etichette esplicite di prova, due riquadri e una freccia;
controllo XML e repair pass della skill non hanno richiesto correzioni.

Dalla radice del repository è stata eseguita l'esportazione reale:

```bash
/tmp/drivehub-relazione-tools/drawio/opt/drawio/drawio --disable-gpu --user-data-dir=/tmp/drivehub-relazione-tools/drawio-profile -x -f pdf -e -b 10 -o docs/relazione/verifiche/ticket-01/drawio/prova-esportazione.drawio.pdf docs/relazione/verifiche/ticket-01/drawio/prova-esportazione.drawio > docs/relazione/verifiche/ticket-01/drawio/esportazione.log 2>&1
```

Esito: **codice di uscita 0**; [PDF esportato](drawio/prova-esportazione.drawio.pdf)
di una pagina, aperto e renderizzato; [output](drawio/esportazione.txt).
L'XML recuperato dal campo `Subject` del PDF corrisponde al sorgente mediante
confronto canonico XML. Non è un allegato PDF separato. Il sorgente `.drawio`
rimane comunque conservato accanto all'esportazione.

## Controllo visivo

Sono state ispezionate le immagini renderizzate di tutte le 15 pagine: il
frontespizio, le due pagine dell'indice, le tre pagine del capitolo 1 e tutte
le pagine provvisorie dei capitoli e delle appendici. Controllati titoli,
accenti, numerazione, righe, margini, leggibilità e marcatura dei segnaposto.
Le immagini finali sono riproducibili con lo script in `build/visual/`.

Le tre sezioni del capitolo 1 sono state disposte su pagine separate per evitare
un titolo isolato a fondo pagina e un punto elenco spezzato. Il titolo lungo
della sezione DAO è allineato a sinistra per evitare spazi eccessivi tra parole.
Anche il PDF draw.io è stato ispezionato visivamente: titolo, etichette, colori,
freccia e bordi sono visibili senza tagli.

## Impedimenti incontrati e limiti

- La risoluzione DNS di `github.com` falliva nella sandbox ordinaria. Download
  e prima compilazione sono riusciti con l'esecuzione autorizzata fuori dalla
  sandbox; binari, cache e ambiente Python sono rimasti in `/tmp`.
- La ricerca dell'installazione Windows tramite PowerShell si è fermata con
  `UtilBindVsockAnyPort: socket failed 1` in WSL. Non è stata dedotta l'assenza
  di draw.io su Windows: è stata usata la copia Linux portabile.
- L'avvio Electron nella sandbox falliva con
  `sandbox_host_linux.cc:41 ... Operation not permitted`. L'esportazione è
  riuscita fuori dalla sandbox, con profilo temporaneo e `--disable-gpu`,
  mantenendo la sandbox interna di Electron.
- I primi controlli hanno rilevato un avviso `inputenc` sotto XeTeX e un
  `Underfull` nel titolo DAO: corretti nei sorgenti e ricompilati. Il controllo
  dei link è stato adattato alle destinazioni nominate prodotte da Tectonic;
  tutte sono state effettivamente risolte.
- Non sono stati eseguiti test applicativi, avvii JavaFX o prove su database.
  La verifica tecnica del testo è statica; non è una nuova certificazione
  funzionale dell'applicazione.
- Non sono state verificate altre distribuzioni LaTeX o altre esportazioni
  draw.io oltre al PDF. Gli strumenti temporanei e la cache non fanno parte
  della consegna; le dipendenze vanno nuovamente predisposte se `/tmp` è pulita.

Il controllo `git diff --exit-code HEAD -- . ':!docs/relazione'` dalla radice
del repository è terminato con codice 0. I file applicativi tracciati, lo
schema, i dati, la vecchia relazione e le evidenze storiche non sono modificati.
Le istruzioni documentali e i file decisionali già presenti come non tracciati
sono stati preservati. Non è stato generato un nuovo grafo del repository:
non era disponibile un grafo locale da consultare e non serviva per questo
perimetro documentale.

## Revisione indipendente

Come previsto dalla skill `implement`, è stata applicata la skill `code-review`
con due revisioni indipendenti in sola lettura. Il riferimento è l'HEAD iniziale
riportato sopra; nessun nuovo commit è presente. I nuovi file sono non tracciati
e sono stati esaminati come aggiunte, senza usare un diff tra commit vuoto come
prova di assenza di modifiche.

| Standards | Spec |
| --- | --- |
| Nessuna violazione concreta delle istruzioni documentali o delle convenzioni tipografiche; nessun odore strutturale che richieda modifiche. | Nessun requisito mancante o implementato erroneamente nel perimetro del ticket 01; nessuna estensione significativa del perimetro. |
| Confermati sorgenti e artefatti sotto `docs/relazione`, terminologia coerente, assenza di modifiche tracciate fuori perimetro e corrispondenza tra PDF/log distribuiti e output di build. | Confermati indice, frontespizio, testo del capitolo 1 tramite fonti tecniche mirate, PDF reale apribile e prova draw.io separata. |

Le revisioni non hanno modificato file, ricompilato il documento o eseguito test
applicativi. La revisione umana del capitolo 1 resta il prossimo passaggio
editoriale; non è stata presentata come già avvenuta.
