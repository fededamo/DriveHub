# Comandi, risultati e limiti — ticket 08

14 novembre 2026. Directory del repository:
`/home/federico/dev/Drivehub/DriveHub`. Solo documentazione.
Applicate `implement` fornita dall'utente, ponytail e code-review a due assi.
TDD, typecheck, suite software e commit della skill generica non si applicano:
la richiesta operativa li esclude esplicitamente.

## Letture e baseline

Letture con `cat`, `sed`, `rg`, Python/Path e PyMuPDF. Letti docs/AGENTS,
tre docs/agents, specifica generale, indice, piano, glossario, README,
ADR 0001–0002, rapporti 07 e verifiche/revisioni pertinenti 01–06.
Letti i sorgenti LaTeX completi e osservato l'intero PDF; riscontri statici
mirati su prezzi/accordo, movimenti zero, enum, schema, presentazione e
frammenti. Nessun grafo Graphify locale; nessuno generato per questo ticket.

Prima di correggere il testo: `git rev-parse HEAD`,
`git status --short --untracked-files=all`, `git ls-files -z` e
`git ls-files --others --exclude-standard -z`. Aggiunti al censimento anche
i file eventualmente ignorati di docs/evidence, verifiche pregresse e vecchia
relazione. Acquisite 2.333 impronte in `baseline-conservazione.json` e
104 copie editoriali, PDF iniziale incluso, in `baseline-editoriale.tar.gz`;
esclusi build e verifiche dalla copia editoriale, inclusi nel censimento gli
storici da proteggere. Nessuna rigenerazione della baseline dopo le modifiche.
Il PDF iniziale aveva realmente 107 pagine e hash
`1fcc18bb426426cdc9809a69ac04993c3ffeb384a5014e515719a12643e71f4e`.
La specifica operativa è registrata qui e non proviene da /tmp.

Consultate le note locali
`../Slides and notes - academic year 202526-20261020/NotesOnTheProjectWork_Jan2025.pdf`:
47 pagine, versione 1.1 gennaio 2025. Estratte e lette le pagine pertinenti
in `note-corso.txt`; nessuna ricerca di nuove prescrizioni online.

## Predisposizione effettiva degli strumenti

Inizialmente nessun tectonic/pdflatex/xelatex/latexmk, renderer Poppler o
draw.io nel PATH; assenti anche gli ambienti /tmp dei ticket precedenti.
Non dedotta l'esistenza degli strumenti dai loro vecchi log.
Python di sistema senza PyMuPDF/Pillow; disponibile Python 3.13.16 in
`/home/federico/.local/bin/python3.13`.

Creato un venv in `/tmp/drivehub-ticket08-tools/venv`; installati PyMuPDF 2.1
e Pillow 13.0.0 tramite pip. Il primo tentativo con rete ordinaria falliva;
quello autorizzato fuori sandbox è riuscito. Download ufficiale Tectonic
0.17.0 da GitHub, ripetuto con escalation e controllo API, fallito per DNS
(`curl: (28) Resolving timed out`); anche il tentativo con risoluzione esplicita
non ha completato la connessione. `pip index versions tectonic` non trova una
distribuzione. Questi tentativi non sono compilazioni riuscite.

Il mirror Ubuntu rispondeva. Dopo simulazione read-only con apt, eseguito
`apt-get download` dei pacchetti seguenti e `dpkg-deb -x` in
`/tmp/drivehub-ticket08-tools/texlive`, senza installazione di sistema:

```
texlive-binaries texlive-base texlive-latex-base texlive-latex-recommended
texlive-latex-extra texlive-lang-italian texlive-fonts-recommended
fonts-lmodern lmodern libkpathsea6 libptexenc1 libsynctex2 libtexlua53-5
libteckit0 libmpfi0 libpaper2 libpotrace0 libzzip-0-13t64
texlive-xetex
```

Download registrato in `predisposizione-texlive.txt`; quello separato di
texlive-xetex ha ottenuto `2025.20260124-1` (10,8 MB, exit 0).
Versioni reali in `ambiente.txt`, hash di tutti i .deb e dei motori in
`strumenti-sha256.json`. Nessuno strumento o risorsa editoriale preesistente
in /tmp è stato usato come specifica.

Per l'estrazione portabile impostate solo variabili di toolchain:
LD_LIBRARY_PATH, PATH e TEXMFROOT/TEXMFDIST/TEXMFDEBIAN/TEXMFCNF verso gli
alberi estratti; TEXMFVAR/TEXMFCONFIG/TEXMFHOME/VARTEXFONTS e XDG_CONFIG_HOME
verso sottodirectory temporanee. Nessuna variabile HOME modificata.
PERL5LIB punta a `texlive/usr/share/texlive/tlpkg`.
Configurati `language.dat` con english/italian, mappe font da
`var/lib/tex-common/fontmap-cfg/`, `updmap --user --nohash --cnffile=...`,
e formato con `xetex -ini -etex -jobname=xelatex -progname=xelatex xelatex.ini`.
Log `fontmap.txt` e `formato-xelatex.txt`: exit 0. Anche la preparazione
iniziale del formato pdflatex è conservata; il documento finale usa XeLaTeX.
L'avviso iniziale di updmap-user riguarda il nuovo albero temporaneo,
non modifica la configurazione TeX dell'utente fuori /tmp.

## Compilazione e correzioni

Dalla directory `docs/relazione`, con l'ambiente temporaneo sopra descritto:

```bash
xelatex -no-shell-escape -halt-on-error -interaction=nonstopmode -recorder -output-directory=build/ticket08 main.tex
```

Primo tentativo interrotto dal driver con
`xdvipdfmx:fatal: Unrecognized paper format: a4`: nell'estrazione mancava la
tabella di libpaper. Copiato il `paperspecs` del pacchetto in XDG_CONFIG_HOME,
senza alterare il documento. Log `compilazione-paperspecs-mancante.txt`.
Tre passaggi effettivi successivi: `compilazione-passaggio-1/2/3.txt`.
Primo passaggio con riferimenti/indice da stabilizzare. Nei passaggi finali
restano cinque messaggi `ignored error: Infinite glue shrinkage`, valutati
separatamente in [valutazione dei messaggi TeX](valutazione-messaggi-tex.md).

Correzioni documentali minime: rinvio 2.6 a Implementazione completo;
macro provvisoria inutilizzata rimossa; scheda UC-05 con stima zero e
rollback già descritti in 2.7/3/4/C. Nessuna modifica di formule o risultati.
Altri tre passaggi dopo le correzioni:
`compilazione-correzioni-1/2/3.txt`, tutti exit 0. Copiati main.pdf nel PDF
distribuito e main.log in `latex-log.txt`. Output 1.7, 107 pagine.
Nessun Warning/Underfull/Overfull, carattere mancante o riferimento indefinito
nel passaggio finale; rimangono i cinque `ignored error` sopra indicati.
I 96 avvisi di versione delle figure del vecchio
compilatore Tectonic appartengono ai ticket precedenti: con questo XeLaTeX
non si ripresentano. Nessuna soppressione di avvisi introdotta nei sorgenti.

## ZIP estratto, controlli e osservazione

ZIP creato con Python/ZipFile da main, capitoli, appendici, diagrammi/PDF,
screenshot e LEGGIMI; esclusi build, vecchie verifiche e percorsi esterni.
95 membri confrontati byte per byte con il repository.
Prima prova estratta in `/tmp/drivehub-ticket08-estratto`, tre passaggi exit 0.
Dopo la correzione dei soli conteggi e della versione nelle istruzioni,
ZIP finale estratto da zero in `/tmp/drivehub-ticket08-consegna`.
Il processo di orchestrazione di questa seconda prova è terminato con
exit 143 dopo il primo passaggio (106 pagine, indice ancora da stabilizzare):
non dichiarato riuscito. Ripresi esplicitamente passaggi 2 e 3, entrambi
exit 0; output finale 107 pagine, con gli stessi cinque `ignored error`.
Conservati tutti i log
`compilazione-pacchetto*.txt`; il pacchetto è stato controllato soltanto
dopo la stabilizzazione.

```bash
/tmp/drivehub-ticket08-tools/venv/bin/python docs/relazione/verifiche/ticket-08/controlla-finale.py
/tmp/drivehub-ticket08-tools/venv/bin/python docs/relazione/verifiche/ticket-08/verifica-pacchetto.py /tmp/drivehub-ticket08-consegna /tmp/drivehub-ticket08-tools
```

Esiti finali exit 0: conservazione, 16 estratti esatti, 32 XML/PDF coerenti,
158 link risolti, 86 font incorporati e ZIP coerente. Le prime versioni del
controllo corrente non gestivano label generate dalle macro, URL encoding
del Subject draw.io e prefisso `#` per resolve_link: assert falliti su questi
parser, corretti senza modificare il PDF o gli artefatti storici.

La prova del pacchetto confronta tutti i testi e i pixel delle 107 pagine a
96 dpi e il recorder degli input: 77 input locali, 89 risorse di toolchain,
nessun contenuto editoriale preso dal repository esterno all'estrazione.
`autonomia-pacchetto.json` fissa anche lo SHA dello ZIP finale; registrati
`pacchetto-input.fls` e `pacchetto-latex-log.txt`.

Tutte le pagine renderizzate a 120 dpi e aperte con view_image; i 15 screenshot
inclusi osservati anche come ritagli PDF a 220 dpi. Verbale distinto in
`controllo-visivo.md`. Rendering e confronti automatici non sono usati come
sostituti dell'osservazione. Non rieseguito alcun verificatore 01–07.

## Confine della revisione

Il punto fisso Git è HEAD iniziale, ma il confronto effettivo comprende i
non tracciati: baseline, archivio iniziale, `delta-documentale.diff`,
`delta-files.json` e `input-review.json`. Due revisori in contesti nuovi
separati ricevono gli stessi input congelati, con incarichi Standards e Spec,
senza lettura reciproca dei rapporti. La chiusura amministrativa e gli
eventuali riesami sono registrati nel verbale finale.

Nessuna compilazione Java, suite, DB, harness, nuovo diagramma, aggiornamento
di evidenze storiche o commit. Toolchain e cache non sono distribuite nello ZIP.
Non verificati stampa, altre versioni LaTeX, Windows/macOS o screen reader.
I difetti applicativi noti restano separati; revisione umana ancora da svolgere.

## Ripresa e correzione del verbale — 15 novembre 2026

[Ripresa](ripresa-2026-11-15.md) con nuovo lettore PDF temporaneo e controllo
08 ripetuto, exit 0; [registro](controlli-chiusura-2026-11-15.txt).
La review Spec rileva che il filtro iniziale ignorava i messaggi `ignored error`.
Corretti verbali, matrice e filtro corrente, senza cambiare log o risultati
originari: `warnings_last_pass: []` in `autonomia-pacchetto.json` descrive
soltanto quel filtro limitato, non l'assenza di tutti i messaggi diagnostici.
Lo script aggiornato elenca anche questi messaggi e ne richiede la valutazione;
non è rieseguito sull'estrazione precedente, che non esiste più.
Riosservate personalmente le pagine fisiche 101–106, con riscontro delle
righe delle tabelle C.1/C.2 nei sorgenti. Esito e limite residuo sono nel
[verbale diagnostico](valutazione-messaggi-tex.md); PDF e ZIP invariati.

## Chiusura e impronte

Rapporti iniziali, ricontrolli del rilievo e ricontrolli amministrativi restano
distinti; `revisione.md` li riporta separatamente per Standards e Spec.
Il delta finale è ricalcolato dalla baseline originale, non dal solo Git;
le copie e le impronte iniziali non sono sovrascritte. Gli input finali
sono congelati in `input-review-finale.json`, senza includere il file stesso
o `SHA256SUMS`, per evitare dipendenze circolari.

Controlli conclusivi: impronte della baseline e delle copie; diff testuale
da archivio; inventario dei nuovi file; identità PDF/ZIP; collegamenti locali
dei documenti di consegna; HEAD invariato. `stato-finale.txt` conserva lo stato
Git e `finalizzazione.txt` l'esito di questi controlli. `SHA256SUMS` comprende
gli artefatti di verifica, il PDF e i sorgenti attivi, escluso se stesso.
La verifica standard si esegue dalla presente directory:

```bash
sha256sum --check SHA256SUMS
```

Il manifest è generato e verificato dopo l'ultimo ricontrollo; non comporta
nuove compilazioni o modifiche al PDF/ZIP. Nessun vecchio verificatore è
stato applicato al documento corrente.
