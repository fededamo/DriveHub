# Ticket 05 — Implementazione

Status: done — capitolo 3 proposto alla revisione degli autori.

Riscontro documentale del **13 novembre 2026**, repository
`/home/federico/dev/Drivehub/DriveHub`, HEAD
`61ce18ae1d4e0458b63e970b793b0b3a634a3adf`, nessun nuovo commit.
La [specifica operativa](specifica-ticket.md) deriva dalla richiesta in
conversazione: nessun ticket pubblicato o contenuto temporaneo è necessario
per ricostruirla. Applicate implement, ponytail e code-review; mantenuto il
tono dei capitoli già forniti. Le istruzioni generiche su test e commit
cedono ai vincoli documentali espliciti dell'utente.

## Risultato

- [PDF completo](../../relazione-drivehub.pdf) e [capitolo 3 per gli autori](capitolo-3-per-revisione.pdf).
- [LaTeX principale](../../main.tex) e [capitolo 3](../../capitoli/3_implementazione.tex).
- [Matrice affermazioni/frammenti → sorgenti](matrice-fonti.md),
  [55 fonti con impronte](fonti.json), [13 estratti esatti](frammenti.json)
  e [loro collocazione nel PDF](pagine-frammenti.json).
- [Controlli riusciti](controlli-documento.txt), [log LaTeX](latex-log.txt),
  [compilazione reale](compilazione.txt), [controllo visivo](controllo-visivo.md),
  [revisioni Standards e Spec](revisione.md).
- [Difetti applicativi segnalati separatamente](difetti-applicativi.md):
  nessuna correzione al software.

Capitolo 3 completo nelle cinque sezioni concordate, proposto alla revisione
degli autori. Capitoli 1–2 e appendici A–B conservati e proposti per
revisione; nessuna di queste parti è considerata già approvata.
Capitoli 4–5 e appendice C restano provvisori. La stesura progressiva
non è ancora la relazione finale da consegnare.

Il PDF ha **93 pagine A4**, capitolo 3 alle pagine fisiche **54–63**,
**133 collegamenti interni risolti** e **81 font incorporati**.
I tredici frammenti hanno da 3 a 19 righe fisiche, sono commentati nel
contesto e mantengono i numeri dei sorgenti. La verifica confronta sia
il contenuto LaTeX esatto sia il testo estratto dal PDF (normalizzando
spazi/legature); ogni frammento e didascalia resta su una pagina.
Nessun catalogo di metodi o diagramma nuovo: riusati CL/PKG, navigazione,
sequenze e modello relazionale già presenti. Nessuna attribuzione GoF
basata solo sui nomi dei metodi.

Revisioni indipendenti concluse: **Standards conforme, zero aperti;
Spec conforme, un rilievo minore risolto e zero aperti**.
La matrice D1 ora distingue costruttore privato e factory pubblica;
il LaTeX e il PDF erano già corretti. Entrambi i revisori hanno
verificato la precisazione; non è una correzione applicativa.
PDF finale SHA-256: `2713ae306c1bc3f748ecddbfbdc0771e0b401119a8cd41af005ebd7856b742bb`.
[Manifest finale](SHA256SUMS) e [verifica di chiusura](finalizzazione.txt).

## Fonti e confini

Lette docs/AGENTS e istruzioni richiamate, specifica, indice, piano,
glossario e README; architecture, ADR 0001/0002, verifiche e revisioni
03–04. Le fonti tecniche effettive sono Java e DDL correnti.
La matrice esplicita campi finali/mutabili, ID rispetto a riferimenti,
base nella formula rispetto ad accordo persistito, proprietà della
Connection, connessioni autonome, lock specifici, rollback tentato e
notifiche sincrone prima del commit. La UI distingue Task/worker dai
comandi sincroni, controllo dei risultati superati, errori e scene.

Per le sole proprietà delle API Task/Platform sono consultate le
[API ufficiali Task 21](https://openjfx.io/javadoc/21/javafx.graphics/javafx/concurrent/Task.html)
e [Platform 21](https://openjfx.io/javadoc/21/javafx.graphics/javafx/application/Platform.html#runLater(java.lang.Runnable)),
anche tramite Context7. Il comportamento applicativo è riscontrato sul
codice, senza trasferire garanzie dalle API al resto del progetto.
Nessun grafo Graphify locale trovato e nessuno generato per questo lavoro.

## Strumenti e comandi riproducibili

[Ambiente e rilevamento](ambiente.txt), [impronte strumenti](strumenti-sha256.txt).
Linux WSL2 x86_64; Python di sistema 3.14.4; Tectonic **0.17.0 GNU**,
Python 3.14.4/PyMuPDF 2.1 (MuPDF 1.28.6) nel venv documentale verificato.
Non sono presenti motori LaTeX nel PATH. Verificati gli eseguibili già
esistenti nell'ambiente temporaneo, senza installazioni di sistema o
nuove dipendenze applicative. Le copie temporanee sono strumenti, non
fonti o prerequisiti documentali garantiti su un altro computer.

Comando effettivo, dalla directory `docs/relazione`:

```bash
XDG_CACHE_HOME=/tmp/drivehub-ticket04-tools/cache \
  /tmp/drivehub-ticket04-tools/tectonic --only-cached --untrusted \
  --keep-intermediates --keep-logs --outdir build main.tex \
  > verifiche/ticket-05/compilazione.txt 2>&1
cp build/main.pdf relazione-drivehub.pdf
cp build/main.log verifiche/ticket-05/latex-log.txt
/tmp/drivehub-ticket04-tools/venv/bin/python verifiche/ticket-05/controlla-documento.py \
  > verifiche/ticket-05/controlli-documento.txt 2>&1
```

I comandi finali sono conclusi con exit 0; Tectonic esegue i passaggi
necessari per indice e riferimenti. Su un altro computer si possono
usare Tectonic 0.17.0 e Python con PyMuPDF disponibili nel proprio ambiente,
adattando i percorsi degli eseguibili e della cache. Per la prima
compilazione va omesso `--only-cached`, consentendo il download dei comuni
pacchetti LaTeX. Il contenuto dei frammenti è nel LaTeX, quindi il PDF
si ricompila senza leggere codice o materiali da `/tmp`.

Il log finale non contiene riferimenti irrisolti, box difettosi o caratteri
mancanti. Restano **96 avvisi noti** di Tectonic sulla versione PDF 1.7 delle
figure rispetto all'impostazione interna 1.5, nei passaggi eseguiti.
Non sono soppressi: l'output effettivo è PDF 1.7 e le figure sono leggibili.

## Conservazione e correzioni di lavorazione

La [baseline iniziale](baseline-conservazione.json) protegge **2.199 file**:
software, SQL, dati, relazione precedente, tutti i contenuti completati,
diagrammi, screenshot ed evidenze/manifest 01–04. Sono esclusi soltanto
main, capitolo 3, README della relazione e PDF corrente. Le copie iniziali
dei quattro sono in [prima](prima/); il [delta testuale](delta-documentale.diff)
e il [controllo Git](conservazione.txt) rendono isolabile il lavoro anche
se docs/relazione era già non tracciata. La baseline non viene rigenerata
per adattarla alle modifiche.

Le impronte sono intatte; il testo PDF di capitoli 1–2 e appendici A–B è
confrontato con la copia iniziale, esclusi piè di pagina e sillabazione a
fine riga. È cambiata una sola sillabazione di «approvazione/rifiuto» in
2.3, senza modifica al sorgente o al contenuto. I vecchi controlli 01–04
non sono rieseguiti contro assunzioni divenute storiche.

Tentativi e correzioni effettivi, conservati senza presentarli come successi:

- `compilazione-iniziale.txt`: cache priva di listings.sty, exit 1.
  `compilazione-risorse.txt`: download LaTeX e prima compilazione, exit 0.
- Le inclusioni con intervalli direttamente nei Java mostravano una
  numerazione sfalsata dopo un carattere Unicode precedente all'intervallo.
  Sostituite con estratti esatti incorporati nel LaTeX, verificati contro
  sorgenti/impronte; nessuna modifica ai Java. Ridotti gli intervalli ai
  soli blocchi utili e mantenuti insieme frammento e didascalia.
- `compilazione-font-mancante.txt`: font delle virgolette letterali non
  ancora in cache, exit 1. `compilazione-font.txt`: recupero e compilazione,
  exit 0. Aggiustati due paragrafi per eliminare gli Underfull; le
  virgolette nei frammenti sono letterali con upquote.
- `controlli-primo-confronto.txt`: differenza di sillabazione rilevata nel
  PDF precedente; il confronto ora rimuove solo sillabazioni a fine riga.
- `controlli-frammento-transazione.txt`: rilevata una graffa esterna al
  metodo, nascosta dal gobble del rientro. Ristretto l'estratto al metodo
  completo (88–106); il confronto PDF/sorgente ora passa.
- `compilazione-prima-impaginazione.txt` e `controlli-prima-impaginazione.txt`:
  versione intermedia da 94 pagine. Sintetizzata la sola 3.5 per tenere
  spiegazione Singleton e frammento nella stessa pagina; versione finale
  da 93 pagine, nuovamente compilata, controllata e osservata.

I download dei pacchetti/font hanno richiesto esecuzione fuori sandbox per
rete, con cache temporanea. Nessun rifiuto dell'approvazione automatica.
Il [controllo aggiornato](controlla-documento.py) è documentale e non avvia
Java, database o suite applicative. I rendering riproducibili di tutte le
pagine sono in `build/visual-ticket05/`; non sono screenshot dell'applicazione.

## Limiti

Nessun test applicativo, accesso DB, acquisizione UI, verifica Windows o
seconda distribuzione LaTeX; nessun commit. Nessun audit esaustivo o nuova
prova di concorrenza/sicurezza. Il rollback con guasto, le conseguenze degli
importi zero e l'errore di refresh dopo commit sono riscontri statici,
non guasti riprodotti in questa attività. I controlli documentali non
sostituiscono l'approvazione degli autori.
