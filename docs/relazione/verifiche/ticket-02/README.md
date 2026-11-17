# Ticket 02 — Casi d'uso, schermate e navigazione

Verifica documentale del **12 novembre 2026**, repository
`/home/federico/dev/Drivehub/DriveHub`, HEAD iniziale
`61ce18ae1d4e0458b63e970b793b0b3a634a3adf`.
La specifica del ticket è quella fornita nella conversazione; nessun file di
ticket preesistente o materiale temporaneo è stato usato per definirla.
Nessun commit creato. Il capitolo 1 resta per revisione degli autori.

## Risultato e perimetro

- [PDF completo](../../relazione-drivehub.pdf) ed
  [estratto per revisione di 2.1–2.4 e appendice A](ticket-02-per-revisione.pdf).
- [Sorgente principale](../../main.tex),
  [integrazione capitolo 2](../../capitoli/2_progettazione.tex),
  [sezioni 2.1–2.4](../../capitoli/2_funzioni.tex),
  [schede complete](../../appendici/a_casi_uso.tex).
- [Sei diagrammi draw.io ed esportazioni](../../diagrammi/README.md): casi
  d'uso e navigazione per cliente, venditore, manager.
- [Sedici screenshot correnti](../../screenshot/), quindici inclusi nel PDF.
  La vista generale del catalogo noleggio `05-cliente-noleggio.png` è
  conservata come evidenza supplementare; nel PDF è rappresentato il dialogo.
- Quindici schede: UC-01–UC-06 principali, UC-07–UC-11 complementari e
  UC-S01–UC-S04 di supporto. Le alternative `n.a` rimandano al passo `n`
  della medesima scheda. UC-04 è una sottofunzione applicativa di UC-03.

PDF finale: **45 pagine A4**, **91 collegamenti interni risolti**, 19 font
incorporati. Le sezioni 2.1–2.4 occupano le pagine fisiche **7–24** (numerate
4–21); l'appendice A le pagine fisiche **30–43** (numerate 27–40).
L'estratto per revisione contiene queste 32 pagine.

I titoli e l'ordine 2.1–2.4 sono invariati. Sezioni 2.5–2.8, capitoli 3–5 e
appendici B–C sono ancora provvisori. Il PDF è una stesura progressiva, non
una consegna finale già approvata. L'appendice A è completata: non si applica
più l'asserzione del controllo storico che la voleva provvisoria.

## Fonti e riscontro tecnico

Lette le istruzioni di `docs/AGENTS.md` e `docs/agents/`, specifica, indice,
piano, glossario, README della relazione, verifiche ticket 01 e prerequisiti
in `docs/installation.md` e `docs/testing.md`. ADR 0001 per base manuale e
accordo; ADR 0002 resta valido, senza modifiche agli enum o allo schema.
La vecchia relazione, in particolare `Relazione_SWE/capitoli/2a_casi_uso.tex`,
ha fornito solo struttura e riconoscimento dei sei casi principali.

| Casi / affermazioni | Sorgenti correnti riscontrati |
| --- | --- |
| UC-01: stati, disponibilità, massimo tre attivi, annullamento proprio | `TestDriveService`, `TestDrive`, `PostgresTestDriveDao`, `ClientDashboardView.handleTestDriveAction` |
| UC-02: periodo incluso, ricalcolo, addebito intero, cancellazioni collegate | `RentalService.bookRental`, `PriceCalculatorService`, `PostgresRentDao`, `TestDriveCancellationObserver` |
| UC-03: acconto 20%, totale fissato, saldo, penale e scadenze all'accesso | `PurchaseService`, `Request`, `ClientDashboardView.handlePurchaseAction`, `handleCompletePurchaseAction`, `checkExpirationsOnStartup` |
| UC-04: approvazione, venditore e mese per il saldo; rifiuto con rimborso | `PurchaseService.approvePurchase`, `rejectPurchase`, `SalesmanDashboardView`, `OperationQueryService` |
| UC-05: proposta, VIN noto/nuovo, titolarità, accettazione e storico | `SaleToDealershipService`, le dashboard cliente/venditore e `PostgresSaleDao` |
| UC-06: interi 0–50, massimo sconto, aggiornamento/rimozione | `CatalogService`, `Discount`, `PriceCalculatorService`, `ManagerDashboardView.buildDiscountsTab` |
| UC-07–UC-11: cataloghi, destinazione, basi prezzo, registro e operazioni proprie | `CatalogService`, `RentalService.changeVehicleRentalState`, `OperationQueryService`, le tre dashboard |
| UC-S01–UC-S04: registrazione solo cliente, accesso per ruolo, identità e conto demo | `AuthService`, `LoginView`, `AccountManagementDialog`, `App` |
| Navigazione e letture | `App.showDashboardForUser`, `LoginView`, `buildUi` delle dashboard, handler dei pulsanti, `AsyncLoad` |

Sono distinti il punto di vista della concessionaria e le etichette UI del
cliente. Una richiesta `SALE` riguarda una vendita al cliente; `PURCHASE`
riguarda un acquisto dal cliente. Non è descritto un controllo dei permessi
autonomo in ogni servizio. Il comando di autorizzazione non viene presentato
come abilitazione dello staff o come completamento della vendita.

## Ambiente e acquisizione effettiva

[Versioni e ambiente](ambiente.txt), [impronte dei pacchetti temporanei](pacchetti-sha256.txt).
JDK 21, PostgreSQL 16, Xvfb e relative librerie mancavano dal sistema e sono
stati scaricati dai mirror ufficiali, estratti in
`/tmp/drivehub-ticket02-tools/root`, senza installazioni di sistema.
Tectonic, draw.io e l'ambiente Python sono stati riutilizzati soltanto dopo
averne verificato esistenza e versioni in `/tmp/drivehub-relazione-tools`.
Questi strumenti sono temporanei, non prerequisiti garantiti su un altro PC.

È stato creato un **cluster nuovo** in `/tmp/drivehub-ticket02-session/pgdata`,
server PostgreSQL 16.15 su **127.0.0.1:55442**, DB
**drivehub_ticket02_demo**, proprietario **drivehub_ticket02**, ruolo senza
privilegi amministrativi. L'autenticazione trust è limitata a questo cluster
demo locale. Identità del database e server sono state controllate prima
dell'inizializzazione. Non è stato contattato alcun archivio operativo.

Comandi dalla radice del repository, con `PATH`, `JAVA_HOME` e
`LD_LIBRARY_PATH` rivolti agli strumenti estratti:

```bash
initdb -D /tmp/drivehub-ticket02-session/pgdata --locale=C.UTF-8 \
  --encoding=UTF8 --auth-local=trust --auth-host=trust -U ticket02_admin
pg_ctl -D /tmp/drivehub-ticket02-session/pgdata \
  -l /tmp/drivehub-ticket02-session/postgres.log \
  -o '-h 127.0.0.1 -p 55442 -k /tmp/drivehub-ticket02-session/socket' start
psql -X -h 127.0.0.1 -p 55442 -U ticket02_admin -d postgres -v ON_ERROR_STOP=1 \
  -c 'CREATE ROLE drivehub_ticket02 LOGIN NOSUPERUSER NOCREATEDB NOCREATEROLE;' \
  -c 'CREATE DATABASE drivehub_ticket02_demo OWNER drivehub_ticket02;'
psql -X -h 127.0.0.1 -p 55442 -U drivehub_ticket02 -d drivehub_ticket02_demo \
  -v ON_ERROR_STOP=1 --single-transaction \
  -f src/main/resources/db/schema.sql -f src/main/resources/db/demo.sql
```

[Output dell'inizializzazione](inizializzazione-demo.txt). Schema e demo sono
stati eseguiti **solo sul nuovo DB sacrificabile**. Nel medesimo ambiente si
sono adattate le date per evitare richieste già scadute e mostrare prenotazioni:

```sql
UPDATE request SET exp_date=CURRENT_DATE+30
WHERE type='SALE' AND exp_date IS NOT NULL;
UPDATE test_drive SET date=(CURRENT_DATE+20+id::int)+TIME '10:00';
UPDATE rent SET start=CURRENT_DATE+10, "end"=CURRENT_DATE+12 WHERE id=1;
```

[Esito e identità della destinazione](adattamento-demo.txt). Le date non sono
un adeguamento della fixture SQL del repository: sono dati dimostrativi nel
cluster temporaneo, mantenendo gli importi della demo. Password demo `password`
e identità documentate nell'installazione; nessuna credenziale personale.

I 65 sorgenti di produzione sono stati compilati con `javac --release 21`
in `/tmp/drivehub-ticket02-session/classes`, senza usare i bytecode precedenti
di produzione in `target/`. Classpath con risorse correnti e JDBC 42.7.3;
module path con i tre moduli JavaFX 21.0.2 Linux della cache Maven.
[Output compilatore](compilazione-app.txt): nessuna diagnostica.

[CaptureTicket02.java](CaptureTicket02.java) riusa `fx`, `check`, `screenshot`
e gli strumenti JavaFX Robot di `docs/evidence/r03-prices/PriceUiCheck.java`.
Il suo vecchio `main` e `TestDatabase.reset()` **non vengono eseguiti**;
`target/test-classes` è solo una dipendenza di compilazione del vecchio harness.
Non sono stati aggiunti test applicativi. Il nuovo harness è esclusivamente
di acquisizione e navigazione documentale, con asserzioni sulla destinazione,
sulla vista corrente, sui dati caricati e sui dialoghi aperti.

```bash
# T02_MODULES: javafx-base/graphics/controls 21.0.2 Linux.
# T02_CP: classi appena compilate in /tmp, src/main/resources, driver JDBC.
javac --module-path "$T02_MODULES" --add-modules javafx.controls \
  -cp "$T02_CP:target/test-classes" -d /tmp/drivehub-ticket02-session/classes \
  docs/evidence/r03-prices/PriceUiCheck.java \
  docs/relazione/verifiche/ticket-02/CaptureTicket02.java
java --module-path "$T02_MODULES" --add-modules javafx.controls \
  -Dprism.order=sw -cp "$T02_CP:target/test-classes" \
  it.unifi.drivehub.CaptureTicket02 docs/relazione/screenshot
```

L'app riceve `DRIVEHUB_DB_URL=jdbc:postgresql://127.0.0.1:55442/drivehub_ticket02_demo`,
`DRIVEHUB_DB_USER=drivehub_ticket02`, password vuota per il solo cluster trust,
`DISPLAY=:92`. Xvfb ha schermo 1600×1000×24 e nessun ascolto TCP/socket
filesystem. Le dashboard sono ridimensionate a 1500×930; i dialoghi sono
catturati direttamente nella loro finestra reale, senza ritaglio o ricostruzione.
Gli screenshot completi possono richiedere zoom per leggere le singole celle;
gli originali PNG conservano la risoluzione dell'acquisizione.

[Log finale dell'acquisizione](acquisizione-ui.txt): **completamento riuscito**,
tre login reali tramite `App.start`/`LoginView`, sedici acquisizioni, ritorni
all'accesso. Il harness usa gli handler reali per tab e dialoghi e li chiude
senza confermare operazioni commerciali. Le schermate non certificano tutte
le alternative delle schede e non sostituiscono una suite funzionale.

Dopo le acquisizioni PostgreSQL è stato arrestato con `pg_ctl -D
/tmp/drivehub-ticket02-session/pgdata -m fast -w stop` e il processo Xvfb
dedicato è stato terminato. [Esito dell'arresto PostgreSQL](chiusura-ambiente.txt).
Gli strumenti e il cluster rimangono in `/tmp` solo come materiale sacrificabile.

## Diagrammi e compilazione

Letta la skill draw.io e consultate le
[regole XML ufficiali](https://raw.githubusercontent.com/jgraph/drawio-mcp/main/shared/xml-essentials.md).
[Generatore delle disposizioni manuali](crea-diagrammi.py), con autocontrolli
su XML, identificativi e terminali degli archi. I diagrammi non sono ricavati
automaticamente dalle classi. Ogni esportazione usa draw.io Desktop 32.6.10,
`--disable-gpu`, profilo temporaneo, `-x -f pdf -e --crop -b 10`.
I sei file `*-esportazione.txt` in questa directory riportano i comandi
effettivi e **codice di uscita 0**. Ogni PDF è su una sola pagina.

Compilazione effettiva dalla directory `docs/relazione`:

```bash
XDG_CACHE_HOME=/tmp/drivehub-relazione-tools/cache \
  /tmp/drivehub-relazione-tools/tectonic --only-cached --untrusted \
  --keep-intermediates --keep-logs --outdir build main.tex \
  > verifiche/ticket-02/compilazione.txt 2>&1
cp build/main.pdf relazione-drivehub.pdf
/tmp/drivehub-relazione-tools/venv/bin/python \
  verifiche/ticket-02/controlla_documento.py \
  > verifiche/ticket-02/controlli-documento.txt
```

[Output Tectonic](compilazione.txt), [log LaTeX](latex-log.txt),
[controlli documentali](controlli-documento.txt),
[script aggiornato](controlla_documento.py). Compilazione e controllo conclusi
con codice 0. I passaggi automatici di Tectonic stabilizzano indice e riferimenti.
Il controllo verifica ordine dei capitoli e delle 25 sezioni, quindici
schede, riferimenti interni, font incorporati, presenza delle sezioni ancora
provvisorie e completamento di 2.1–2.4 e A. Apre e renderizza tutte le pagine
e i sei PDF draw.io; confronta l'XML incorporato con il sorgente canonico,
decodificando URL ed escape delle parentesi aggiunti al Subject dall'esportatore.

Resta il solo avviso di versione PDF di Tectonic durante le letture per
determinare le dimensioni delle figure: output effettivo **PDF 1.7**, diagrammi
1.7. È il comportamento descritto nella
[segnalazione del progetto Tectonic](https://github.com/tectonic-typesetting/tectonic/issues/1193).
Il log finale non ha box sovrapposti/troppo laschi, caratteri mancanti o
riferimenti irrisolti; gli avvisi presenti non vengono occultati.

## Controllo visivo e conservazione

Le immagini di controllo sono riproducibili in `build/visual-ticket02/`;
l'estratto per revisione è conservato fuori da `build/`. Il controllo visivo
è registrato in [controllo-visivo.md](controllo-visivo.md).

[Baseline iniziale](baseline-conservazione.json): impronte di 1973 file,
comprendenti i file tracciati esterni a questa relazione, il capitolo 1 e
tutte le evidenze del ticket 01. Il controllo aggiornato ne verifica la
conservazione; confronta inoltre il testo delle pagine del capitolo 1 con
l'estratto storico. Il manifest del ticket 01 resta una fotografia della
vecchia stesura, non un controllo da aggiornare al PDF nuovo.
[Esito del controllo Git esterno al perimetro](conservazione.txt): codice 0.

## Impedimenti e limiti

- I primi download apt su HTTP fallivano; usati i mirror ufficiali HTTPS.
  Le versioni JDK elencate dalla cache apt erano state rimosse: scelta la
  versione effettivamente disponibile sul mirror Ubuntu. Le impronte dei
  pacchetti usati sono conservate.
- I link assoluti del JDK estratto puntavano a configurazioni non installate:
  rilocati solo quelli per cui esisteva la copia estratta in `/tmp`.
- Xvfb richiedeva librerie e `xkbcomp` assenti. Estratti i pacchetti necessari;
  nella sola copia `Xvfb-ticket02` il percorso binario `/usr/bin` è stato
  sostituito con `/tmp/xkb`, di identica lunghezza, dove il link punta al
  compilatore estratto. Nessun file in `/usr` modificato.
- Due tentativi di acquisizione si sono fermati sul dialogo: il pulsante
  omonimo veniva cercato nell'intera dashboard anziché nella tab selezionata.
  Corretta la sola selezione nell'harness. Conservati
  [primo tentativo](acquisizione-tentativo-1.txt),
  [secondo tentativo](acquisizione-tentativo-2.txt) e
  [acquisizione intera precedente ai dettagli](acquisizione-finestre-intere.txt).
- Il controllo ha rilevato esportazioni iniziali su più fogli: corrette con
  `--crop`. L'istruzione `--layout libavoid` indicata dalla skill non è
  supportata dalla CLI disponibile ([esito](instradamento.txt)); usato il
  router laterale nativo per tenere gli archi fuori dalle ellissi, senza
  attribuire alla CLI un layout riuscito non eseguito.
- Corretti gli overflow tipografici dei riferimenti ai sorgenti e le
  interruzioni che lasciavano alternative isolate nella pagina successiva.
  Una prova con il comando LaTeX di gestione PDF prima del caricamento del
  backend non compilava; sostituita dai due special del driver all'inizio
  del documento. Il PDF finale è stato ricompilato e ricontrollato.
- Nessuna suite applicativa, prova di concorrenza, verifica Windows o seconda
  distribuzione LaTeX eseguita. Non è stato generato un grafo Graphify:
  assente localmente e non necessario per questo riscontro documentale mirato.
- Il conto è dimostrativo; nessuna banca esterna, invio a terzi, modifica
  applicativa, modifica SQL o commit. La revisione degli autori rimane richiesta.

La revisione indipendente Standards/Spec è **conclusa per i ticket 01 e 02**:
entrambi conformi, senza rilievi aperti. I due rapporti e il nuovo controllo
degli artefatti sono collegati in [revisione.md](revisione.md).
Il precedente tentativo interrotto per crediti resta distinto dalle revisioni
ora completate. Le impronte finali sono in [SHA256SUMS](SHA256SUMS).
