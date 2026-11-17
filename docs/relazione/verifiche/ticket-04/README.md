# Ticket 04 — Sequenze e database

Riscontro statico iniziato il **12 novembre 2026**, chiusura documentale
del **13 novembre 2026**, repository `/home/federico/dev/Drivehub/DriveHub`.
HEAD iniziale e attuale: `61ce18ae1d4e0458b63e970b793b0b3a634a3adf`.
La [specifica operativa](specifica-ticket.md) deriva dalla richiesta in
conversazione; nessun ticket già pubblicato o materiale temporaneo ne
costituisce il prerequisito. Nessun commit creato.

## Risultato e stato editoriale

- [PDF completo](../../relazione-drivehub.pdf),
  [estratto 2.7–2.8](ticket-04-per-revisione.pdf) e
  [capitolo 2 completo per gli autori](capitolo-2-per-revisione.pdf).
- [Sorgente principale](../../main.tex),
  [integrazione capitolo 2](../../capitoli/2_progettazione.tex),
  [sequenze](../../capitoli/2_sequenze.tex),
  [database](../../capitoli/2_database.tex).
- [Indice delle 13 nuove tavole draw.io](../../diagrammi/README.md):
  SQ-01a–d, SQ-02, SQ-OB, SQ-03a–b; ER-01/02; REL-01–03.
  Otto tavole coordinate mantengono riconoscibili i tre flussi, due viste
  ER coprono le relazioni e tre viste relazionali tutti gli attributi.
- [Matrice sequenze → messaggi → sorgenti](matrice-sequenze.md),
  [matrice dati → SQL](matrice-dati.md), con omissioni motivate;
  [fonti e impronte](fonti.json),
  [segnalazioni applicative separate](difetti-applicativi.md).
- [Controlli finali](controlli-documento.txt),
  [osservazione visiva](controllo-visivo.md),
  [revisioni indipendenti](revisione.md).

Il capitolo 2 è completo e proposto per revisione degli autori insieme
alle appendici A–B; il capitolo 1 resta conservato e proposto per revisione.
Nessuna approvazione degli autori è attribuita a questi controlli.
Capitoli 3–5 e appendice C restano esplicitamente provvisori.
Il PDF è una stesura progressiva, non la relazione finale da consegnare.
Numeri di pagina, collegamenti, font e hash della versione effettiva sono
nel verbale automatico e nel rapporto visivo collegati sopra.

PDF finale: **84 pagine A4, 108 collegamenti interni risolti, 71 font
incorporati**; 2.7–2.8 alle pagine fisiche **32–53**, capitolo 2 completo
alle **7–53**. Compilazione e controlli finali conclusi con **exit0**.
Revisioni indipendenti **Standards e Spec concluse e conformi**, nessun
rilievo aperto: [esiti, correzioni e limiti](revisione.md).

## Riscontro e selezione progettuale

Lette istruzioni documentali e documenti preliminari richiesti, compresi
README/revisioni 02–03 e relazioni del ticket 03, architettura e ADR 0001/0002.
Le fonti tecniche sono Java e DDL correnti, improntati; la vecchia relazione
ha fornito soltanto struttura e riconoscimento dei flussi, senza trasferirne
classi, gateway o regole superate. Usati casi d'uso e identificativi esistenti.

Le matrici documentano il riscontro statico, non una nuova esecuzione
funzionale. I test applicativi esistenti e i risultati storici non sono
usati per provare ogni messaggio o garanzia delle tavole. Nessun test,
database operativo, inizializzazione o acquisizione JavaFX è stato avviato.
Nessun grafo Graphify locale disponibile: non ne è stato generato uno per
sostituire il riscontro mirato.

Le tavole sono disposizioni manuali nel [generatore](crea-diagrammi.py),
che non legge Java o SQL. Gli assert verificano involucro XML, ID,
terminali e geometrie. Le viste ER sono decomposte per relazione, con
entità ripetute esplicitamente identificate come la stessa entità: questa
scelta evita archi e cardinalità sovrapposti. Le viste relazionali non sono
un'esportazione automatica dello schema. Le legende distinguono metodi,
sintesi, attori, oggetti, ID, connessioni, alternative e ritorni.

Applicate le skill implement, draw.io e code-review. La skill draw.io
effettivamente letta è in
`/home/federico/.codex/plugins/cache/drawio/drawio/1.2.6/skills/drawio/SKILL.md`;
il percorso iniziale del catalogo era assente. Consultate le
[regole XML ufficiali](https://raw.githubusercontent.com/jgraph/drawio-mcp/main/shared/xml-essentials.md).
Si mantengono formato e tono della relazione esistente.
Le istruzioni generiche di implement su commit/TDD/typecheck/suite
applicativa sono sostituite dal perimetro documentale esplicito dell'utente.

## Strumenti e riproduzione

[Ambiente, versioni e comandi di rilevamento](ambiente.txt),
[impronte degli strumenti](strumenti-sha256.txt).
Ubuntu 26.04.1 LTS/WSL2; Node 24.22.1; draw.io Desktop 32.6.10;
Tectonic 0.17.0 **variante GNU**. Gli strumenti sono separati dalle
dipendenze applicative, senza installazioni di sistema.

Il 12 novembre sono stati verificati e riusati gli strumenti temporanei
precedenti; Tectonic musl si arrestava con exit 139 nella fase xdvipdfmx
del documento completo, anche fuori sandbox. Le tredici nuove tavole
compilavano invece insieme in un documento minimo. La variante GNU
ha compilato il documento completo. Non si attribuisce una causa interna
dimostrata al crash. I tentativi sono conservati nei log `compilazione-*`
e `diagnosi-*`; non sono risultati della compilazione finale.

Il 13 novembre le directory temporanee precedenti erano state rimosse:
sono stati riscaricati dai progetti ufficiali gli stessi strumenti,
ricreato un venv Python separato e ricostruita la cache LaTeX. Preparazione
eseguita fuori sandbox per rete, dopo aver predisposto la directory `/tmp`:

```bash
mkdir -p /tmp/drivehub-ticket04-tools
curl -fL --retry 2 -o /tmp/drivehub-ticket04-tools/tectonic-gnu.tar.gz \
  https://github.com/tectonic-typesetting/tectonic/releases/download/tectonic%400.17.0/tectonic-0.17.0-x86_64-unknown-linux-gnu.tar.gz
tar -xzf /tmp/drivehub-ticket04-tools/tectonic-gnu.tar.gz -C /tmp/drivehub-ticket04-tools
curl -fL --retry 2 -o /tmp/drivehub-ticket04-tools/drawio.deb \
  https://github.com/jgraph/drawio-desktop/releases/download/v32.6.10/drawio-amd64-32.6.10.deb
dpkg-deb -x /tmp/drivehub-ticket04-tools/drawio.deb /tmp/drivehub-ticket04-tools/drawio
uv venv --python /usr/bin/python3 /tmp/drivehub-ticket04-tools/venv
uv pip install --python /tmp/drivehub-ticket04-tools/venv/bin/python \
  --cache-dir /tmp/drivehub-ticket04-tools/uv-cache pymupdf
```

Generazione dalla radice del repository:

```bash
python3 docs/relazione/verifiche/ticket-04/crea-diagrammi.py
python3 docs/relazione/verifiche/ticket-04/esporta-diagrammi.py
```

L'[esportatore](esporta-diagrammi.py) esegue il repair pass della skill e
invoca draw.io con `--disable-gpu`, profilo separato, `-x -f pdf -e --crop
-b 10`. Le tredici esportazioni conservano XML incorporato e tavola intera
su un foglio; ogni `*-esportazione.txt` contiene data, comando, output ed
exit. Electron richiede esecuzione fuori sandbox; la sua sandbox interna
resta abilitata. Non ci sono rifiuti automatici di approvazione da segnalare.
Dopo il rilievo Spec è stata riesportata soltanto SQ-03a.
L’osservazione finale ha poi richiesto la riduzione dei soli riquadri
di intestazione nelle otto sequenze, per separarne i bordi, e il colore
principale sull’UPDATE condizionale di SQ-01a; riesportate le otto tavole
e ricompilato il documento, mantenendo invariati messaggi e confini.
Corretta inoltre la resa delle virgolette angolari nei soli nuovi testi
LaTeX, usando i comandi tipografici anziché i caratteri Unicode letterali;
ricompilato e ricontrollato il corpo interessato, senza cambiare le tavole.

Compilazione effettiva dalla directory `docs/relazione`, dopo il primo
passaggio con download nella cache (`compilazione-ripristino.txt`):

```bash
XDG_CACHE_HOME=/tmp/drivehub-ticket04-tools/cache \
  /tmp/drivehub-ticket04-tools/tectonic --only-cached --untrusted \
  --keep-intermediates --keep-logs --outdir build main.tex \
  > verifiche/ticket-04/compilazione.txt 2>&1
cp build/main.pdf relazione-drivehub.pdf
cp build/main.log verifiche/ticket-04/latex-log.txt
/tmp/drivehub-ticket04-tools/venv/bin/python verifiche/ticket-04/controlla-documento.py \
  > verifiche/ticket-04/controlli-documento.txt 2>&1
```

Tectonic esegue i passaggi necessari a stabilizzare indice e riferimenti.
I log finali non sono sostituiti da un controllo storico. Gli avvisi noti
PDF 1.7/1.5 restano registrati, senza soppressioni: l'output effettivo è
PDF 1.7. Gli strumenti in `/tmp` non sono prerequisiti garantiti su un
altro computer; i sorgenti e i comandi qui sono autonomi rispetto ai dati
temporanei della sessione.

## Verifica, conservazione e revisioni

Il [controllo aggiornato](controlla-documento.py) apre e renderizza tutte
le pagine, verifica ordine e completamento delle sezioni, riferimenti e
font, confronta XML incorporati e tutte le etichette delle esportazioni,
controlla copertura lessicale di tabelle/attributi/FK e ricalcola le impronte.
Questi assert non sostituiscono il riscontro semantico o l'osservazione.
I rendering riproducibili sono in `build/visual-ticket04/`; il PDF
distribuito è identico a `build/main.pdf`. Le immagini sono osservazioni
del documento, non nuove acquisizioni dell'applicazione.

La [baseline iniziale](baseline-conservazione.json) protegge 2.116 file:
software, SQL, storico e materiali precedenti della relazione, inclusi
manifest ed evidenze 01–03, screenshot, diagrammi, capitolo 1, 2.1–2.6 e A–B.
Sono esclusi solo i cinque file necessari all'integrazione/stato
(main, contenitore capitolo 2, due README, PDF corrente). Le copie testuali
precedenti sono in [prima](prima/). Il confronto PDF conserva anche il testo
delle parti precedenti, esclusi avviso di stato e numeri di pagina.

Le due revisioni sono documentate in [revisione.md](revisione.md).
I precedenti controlli parziali avevano trovato due rilievi: spazi persi
nelle espressioni SQL con `nolinkurl` e nome inesatto `buildSellCarTab`.
Correzioni locali: `texttt` per le sole espressioni nuove con spazi e
`buildSellMyCarTab` in SQ-03a, generatore e matrice. Nessuna modifica
alla macro condivisa o ai materiali precedenti. I controlli parziali
non sono qualificati come revisioni concluse; i rapporti finali provengono
da due nuovi contesti separati e riguardano il PDF aggiornato.

[Manifest finale](SHA256SUMS) e [controllo del perimetro Git](conservazione.txt).
Le verifiche precedenti restano fotografie intatte; nessun loro manifest
è aggiornato per pretendere che 2.7–2.8 siano ancora provvisorie.

## Limiti

Nessuna suite applicativa, query o inizializzazione DB, acquisizione UI,
verifica Windows o seconda distribuzione LaTeX. Nessuna modifica al
software, SQL, dati operativi o relazione precedente; nessun commit.
Il riscontro statico non certifica assenza generale di bug, deadlock,
problemi di autorizzazione o guasti distribuiti. Gli importi zero e i
limiti dei controlli email sono [segnalati separatamente](difetti-applicativi.md),
senza correzioni applicative. L'approvazione degli autori resta richiesta.
