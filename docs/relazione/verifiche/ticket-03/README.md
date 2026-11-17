# Ticket 03 — Package, classi e architettura

Verifica documentale del **12 novembre 2026**, repository
`/home/federico/dev/Drivehub/DriveHub`, HEAD iniziale e attuale
`61ce18ae1d4e0458b63e970b793b0b3a634a3adf`.
La specifica è quella ricevuta nella conversazione, conservata per i revisori
in [specifica-ticket.md](specifica-ticket.md): non dipende da un ticket già
pubblicato o da materiali temporanei. Nessun commit creato.

## Risultato e stato editoriale

- [PDF completo](../../relazione-drivehub.pdf): 63 pagine A4.
- [Estratto per gli autori](ticket-03-per-revisione.pdf): 2.5–2.6 e B.
- [Sorgente principale](../../main.tex),
  [integrazione capitolo 2](../../capitoli/2_progettazione.tex),
  [sezioni 2.5–2.6](../../capitoli/2_architettura.tex),
  [appendice B](../../appendici/b_classi.tex).
- [Indice dei diagrammi](../../diagrammi/README.md): PKG e CL-01–CL-11,
  con CL-08a/CL-08b per il package DAO; 13 nuovi sorgenti draw.io e
  altrettante esportazioni su singola pagina.
- [Matrice package → tavola → tipi → sorgenti](matrice-copertura.md),
  [controllo delle relazioni](relazioni.md),
  [inventario e impronte dei 65 sorgenti](inventario-sorgenti.json),
  [dipendenze applicative per import](dipendenze-import.json).

2.5–2.6 occupano le pagine fisiche **25–31** (numerate 22–28);
l'appendice B le **50–62** (numerate 47–59). La copertura comprende tutti
gli undici package con tipi propri e tutti i 65 tipi principali; due record
annidati del catalogo sono riassunti in nota e le due callback annidate del
gestore transazionale sono esplicite. Le omissioni di membri, tipi anonimi
e archi ripetuti sono motivate nella matrice.

Capitolo 1, 2.1–2.4 e A sono conservati; questi contenuti e i nuovi 2.5–2.6/B
restano **proposti per revisione degli autori**, non già approvati.
2.7–2.8, capitoli 3–5 e appendice C restano esplicitamente provvisori.
Le verifiche 01/02 e i loro manifest sono fotografie storiche intatte:
non sono stati adattati al nuovo PDF né rieseguiti con assunzioni superate.

## Fonti, confini e notazione

Lette `docs/AGENTS.md`, le tre istruzioni in `docs/agents/`, specifica,
indice, piano, glossario, README della relazione, verifiche 01/02 e
revisione indipendente 02; riscontrati `docs/architecture.md` e ADR 0001/0002.
I sorgenti correnti in `src/main/java` sono l'autorità tecnica.
La struttura preesistente della relazione fornisce titoli e ordine;
nessun diagramma storico è stato riproposto come descrizione corrente.

Applicate le skill implement, draw.io e code-review. La skill draw.io
era in un percorso diverso da quello del catalogo: trovata e letta in
`/home/federico/.codex/plugins/cache/drawio/drawio/1.2.6/skills/drawio/SKILL.md`.
Consultate le [regole XML ufficiali](https://raw.githubusercontent.com/jgraph/drawio-mcp/main/shared/xml-essentials.md).
Nessun grafo Graphify locale disponibile: non è stato generato un nuovo
grafo per sostituire il riscontro mirato dei sorgenti. TDD, typecheck e suite
applicativa non si applicano a modifiche solo documentali; sono stati usati
controlli degli artefatti e osservazione reale, come richiesto dal ticket.

Le tavole sono disposizioni manuali e selezioni progettuali nel
[generatore](crea-diagrammi.py), non un'esportazione UML automatica del codice.
Il generatore non legge sorgenti Java. I suoi assert controllano ID,
terminali degli archi, geometrie e assenza di sovrapposizioni dei riquadri;
il repair pass della skill ha normalizzato l'involucro XML, senza riparazioni
strutturali. Frecce e simboli sono spiegati nel testo, nell'appendice e
nell'indice dei diagrammi. Non si deducono associazioni da chiavi scalari o
composizioni da dipendenze iniettate.

## Ambiente e comandi effettivi

[Ambiente, versioni e data](ambiente.txt),
[impronte dei pacchetti portabili](strumenti-sha256.txt).
Ubuntu 26.04.1 LTS in WSL2; Node 24.22.1; Tectonic 0.17.0 e draw.io Desktop
32.6.10. Strumenti LaTeX/draw.io assenti dal PATH e copie temporanee precedenti
non più disponibili. Sono stati scaricati dai progetti ufficiali ed estratti
soltanto in `/tmp/drivehub-ticket03-tools`, senza dipendenze applicative o
installazioni di sistema. Riutilizzato, dopo verifica, l'ambiente Python
`/tmp/drivehub-review-tools/venv`: Python 3.13 e PyMuPDF 2.1/MuPDF 1.28.6.

Preparazione (fuori sandbox per rete; directory già predisposta in `/tmp`):

```bash
curl -fL --retry 2 -o /tmp/drivehub-ticket03-tools/tectonic.tar.gz \
  https://github.com/tectonic-typesetting/tectonic/releases/download/tectonic%400.17.0/tectonic-0.17.0-x86_64-unknown-linux-musl.tar.gz
tar -xzf /tmp/drivehub-ticket03-tools/tectonic.tar.gz -C /tmp/drivehub-ticket03-tools
curl -fL --retry 2 -o /tmp/drivehub-ticket03-tools/drawio.deb \
  https://github.com/jgraph/drawio-desktop/releases/download/v32.6.10/drawio-amd64-32.6.10.deb
dpkg-deb -x /tmp/drivehub-ticket03-tools/drawio.deb /tmp/drivehub-ticket03-tools/drawio
```

Generazione dalla radice del repository:

```bash
python3 docs/relazione/verifiche/ticket-03/crea-diagrammi.py
```

Esportazione reale, ripetuta solo per le tavole interessate dalle correzioni;
ogni file `*-esportazione.txt` conserva comando assoluto, output ed exit 0:

```bash
/tmp/drivehub-ticket03-tools/drawio/opt/drawio/drawio --disable-gpu \
  --user-data-dir=/tmp/drivehub-ticket03-tools/profile -x -f pdf -e --crop -b 10 \
  -o docs/relazione/diagrammi/classi-utenti.drawio.pdf \
  docs/relazione/diagrammi/classi-utenti.drawio
```

Esportazione fuori sandbox, mantenendo la sandbox interna di Electron.
`--crop` include la tavola intera anziché suddividerla sui fogli del canvas.
Tutti i 13 PDF sono stati aperti/renderizzati; l'XML recuperato dal Subject
è stato confrontato canonicamente con il sorgente corrispondente.

Prima compilazione con download nella nuova cache, dalla directory
`docs/relazione` (comando uguale al seguente senza `--only-cached`);
[output iniziale](compilazione-iniziale.txt), non attribuito alla stesura finale.
Compilazione finale reale usando soltanto la cache:

```bash
XDG_CACHE_HOME=/tmp/drivehub-ticket03-tools/cache \
  /tmp/drivehub-ticket03-tools/tectonic --only-cached --untrusted \
  --keep-intermediates --keep-logs --outdir build main.tex \
  > verifiche/ticket-03/compilazione.txt 2>&1
cp build/main.pdf relazione-drivehub.pdf
cp build/main.log verifiche/ticket-03/latex-log.txt
/tmp/drivehub-review-tools/venv/bin/python verifiche/ticket-03/controlla-documento.py \
  > verifiche/ticket-03/controlli-documento.txt 2>&1
```

[Compilazione finale](compilazione.txt), [log LaTeX](latex-log.txt),
[controlli aggiornati](controlli-documento.txt), [script](controlla-documento.py).
Entrambi i comandi conclusi con exit 0. Indice e riferimenti stabilizzati
dai passaggi automatici di Tectonic. PDF distribuito identico a `build/main.pdf`,
95 collegamenti interni risolti e 45 font incorporati. Nessun box difettoso,
carattere mancante o riferimento irrisolto nel log finale. Restano 57 avvisi
noti di Tectonic per la lettura delle figure PDF 1.7 con impostazione interna
1.5; il documento effettivo è PDF 1.7. Gli avvisi non sono soppressi.

## Conservazione, osservazione e revisioni

La [baseline iniziale](baseline-conservazione.json) protegge 2.046 file:
software tracciato, SQL, dati, vecchia relazione/evidenze e tutti i materiali
precedenti della nuova relazione, salvo i soli file necessari a integrazione
e stato (main, capitolo 2 contenitore, contenitore appendici, due README,
PDF corrente). Il controllo verifica tutte le impronte e confronta il testo
del capitolo 1 con l'estratto storico. Il testo e le figure di 2.1–2.4/A
sono preservati a livello sorgente; l'avviso di stato del capitolo 2 e i
numeri di pagina successivi sono aggiornati.

[Controllo visivo del documento](controllo-visivo.md).
Le immagini a 108 dpi di tutte le pagine e a 108 dpi dei diagrammi sono
riproducibili in `build/visual-ticket03/` e non sostituiscono il PDF distribuito.
La validità degli XML e gli assert non sono presentati come giudizi visivi.

Revisioni indipendenti Standards e Spec **concluse e conformi**, con contesti
separati e nessun rilievo aperto. Standards ha segnalato un rilievo
terminologico minore in tre formulazioni, corretto e verificato nuovamente;
Spec non ha rilevato mancanze o errori nel perimetro richiesto.
[Esiti e limiti](revisione.md), [Standards](revisione-standards.md),
[Spec](revisione-spec.md). La revisione degli autori è un passaggio distinto.
Le impronte aggiornate sono in [SHA256SUMS](SHA256SUMS); i manifest dei
ticket precedenti restano intatti. Il controllo Git esterno al perimetro
è concluso con exit 0 ([verbale](conservazione.txt)).

## Impedimenti, correzioni e limiti

- DNS di GitHub non disponibile nella sandbox: download riusciti fuori
  sandbox. L'avvio Electron nella sandbox falliva con
  `sandbox_host_linux.cc:41 ... Operation not permitted`; usata esecuzione
  fuori sandbox con profilo temporaneo. Nessun rifiuto dell'auto-review.
- Prime esportazioni con archi che attraversavano riquadri e firme strette:
  corrette le disposizioni, abbreviate firme non essenziali e usati archi
  rettilinei per la generalizzazione dei tre ruoli. Separati i DAO dalle
  callback per mantenere leggibilità. Una nota dei record di lettura si
  sovrapponeva alla legenda: corretta e aggiunto assert sulle geometrie.
- Primi log LaTeX con Overfull/Underfull sui nomi tecnici lunghi: riscritti
  i periodi e ricompilato. Corretto il simbolo di visibilità di package
  dopo averne osservato la resa nel PDF. Un repair pass era partito dalla
  directory errata e non aveva trovato i file: rieseguito dalla radice.
- Nessuna suite applicativa, nuova acquisizione UI, inizializzazione/query DB,
  verifica Windows o seconda distribuzione LaTeX. I percorsi temporanei non
  sono prerequisiti garantiti su un altro PC. Nessuna dipendenza aggiunta.
- Riscontro statico e controllo del documento, senza certificazione generale
  della sicurezza o della correttezza applicativa. Nessun nuovo difetto
  applicativo identificato; limiti architetturali in [relazioni.md](relazioni.md).
- La stesura resta progressiva e richiede la revisione degli autori.
