# Comandi e tentativi — solo documentazione

Directory di lavoro `/home/federico/dev/Drivehub/DriveHub`, salvo compilazione
in `docs/relazione`. Applicate implement, ponytail e code-review; mantenuto lo
stile dei capitoli esistenti. Nessun test applicativo/TDD/commit: esclusi dalla
richiesta esplicita, che prevale sulle indicazioni generiche di implement.

## Letture e baseline

Letture mirate con `cat`, `sed`, `rg`/`rg --files` e Python (Path/ZipFile).
Istruzioni: docs/AGENTS e tre docs/agents; specifica generale, indice, piano,
glossario e README; verifiche e tre rapporti05–06; ADR0001–0002.
Fonti effettive, impronte ed estratti nei file fonti.json/registri-zip.json.
Nessun grafo Graphify locale; nessuno generato per questa ricostruzione documentale.
Non usati browser, strumenti GUI, Maven, Java, SQL o servizi esterni.

Baseline creata prima degli interventi con Python: unione ordinata dei percorsi
ottenuti da `git ls-files -z` e `git ls-files --others --exclude-standard -z`;
SHA256 dei2291file, HEAD e timestamp UTC in baseline-conservazione.json.
`git status --short --untracked-files=all` conservato in stato-iniziale.txt.
Copiati in prima/ main, README, piano, capitolo5, PDF; aggiunti capitoli3–4 prima
di modificarne le sole frasi di stato. Nessun file originale sovrascritto dalla
baseline. `git ls-files --others --ignored --exclude-standard docs/evidence
docs/relazione/verifiche` non ha restituito file; build/cache ignorati esclusi.

Cronologia conservata in cronologia-git.txt:

```bash
git log --all --format='%h %ad %s' --date=iso-strict
git show --format=fuller --stat 6bd8c69 -- src pom.xml docs/evidence/revision-records.zip
git diff --stat 841d4bc HEAD -- src pom.xml
git diff d4d82c1815e1fe2d107331fb4f2211b2bc3ca850 HEAD -- src pom.xml
```

Ultimo confronto vuoto, usato soltanto per il rapporto tree di review enum→HEAD.
ZIP letto senza estrarre o eseguire contenuti:24hash interni corrispondenti,
18ticket;6membri selezionati con righe e testi in registri-zip.json.
Una prima raccolta di fonti tentava un `r03-prices/spec-review.md` inesistente:
non ha prodotto fonti.json. Usata la review incorporata in verification.md;
nessun file inventato. Una lettura ADR iniziale usava un nome abbreviato inesatto:
ricercato il percorso reale con `rg --files docs/adr`, poi letto ADR0001.

## Strumenti e compilazione reale

`uname -a`, Tectonic `--version`, Python e PyMuPDF importati effettivamente:
output/exit in ambiente.txt. Tectonic0.17.0GNU e Python3.14.4/PyMuPDF2.1
(MuPDF1.28.6) già esistenti sotto `/tmp/drivehub-ticket06-tools/`; verificati
binario e modulo con SHA256. /tmp contiene solo strumenti/cache, non specifiche
o input editoriali. Nessuna installazione di sistema o dipendenza applicativa.

Primo comando dalla directory LaTeX:

```bash
XDG_CACHE_HOME=/tmp/drivehub-ticket06-tools/cache \
  /tmp/drivehub-ticket06-tools/tectonic --only-cached --untrusted \
  --keep-intermediates --keep-logs --outdir build main.tex
```

Fallito exit1: metrica `ec-lmr8` mancante per le note del nuovo capitolo.
Log: compilazione-cache-font-mancante.txt. Nessun successo attribuito.
Ripetuto senza `--only-cached`, con accesso rete fuori sandbox per scaricare
i font LaTeX, auto-review autorizzata e nessun rifiuto. Log integrale:
compilazione-risorse.txt, exit0. Il log riportava quattro Underfull nelle note;
impostato il solo contenuto delle note di capitolo5 a raggedright.

Ripetuto poi il comando cached sopra, log compilazione.txt, exit0:
nessun Underfull/Overfull, carattere mancante o riferimento irrisolto.
Copiati `build/main.pdf` su `relazione-drivehub.pdf` e `build/main.log` su
verifiche/ticket-07/latex-log.txt. Gli avvisi sulle figure1.7 rispetto
all'impostazione interna1.5 rimangono dichiarati, non soppressi; PDF finale1.7.

## Controlli e osservazione

```bash
/tmp/drivehub-ticket06-tools/venv/bin/python \
  docs/relazione/verifiche/ticket-07/controlla-documento.py \
  > docs/relazione/verifiche/ticket-07/controlli-documento.txt 2>&1
```

Il primo controllo si è fermato sulla citazione del README07 ancora da creare:
log controlli-prima-readme.txt. Creato il resoconto, rieseguiti gli stessi controlli.
Il primo conteggio dei link considerava soltanto LINK_GOTO (0) e contava gli
avvisi nel log TeX anziché nell'output del compilatore (0): non era una verifica
completa dei riferimenti. Conservato in controlli-primo-conteggio-link.txt.
Esteso il controllo ai LINK_NAMED con resolve_link, come nel ticket06;
aggiunto assert sul numero minimo di link e conteggio degli avvisi nel log CLI.
Nuovo esito156link risolti/96avvisi. Nessun cambiamento al PDF necessario.
Gli assert verificano conservazione,43fonti,24hash ZIP ed estratti, conteggi
storici separati, titoli/indice/riferimenti/font e testo PDF pregresso.
Estratto e rendering vengono dal PDF completo; lo ZIP sorgenti contiene i
LaTeX e tutte le figure locali, confrontati byte per byte. Nessuna suite o
generazione di documenti storici avviata. Il rendering è seguito dall'osservazione
effettiva con view_image delle pagine elencate nel verbale visivo.

Le due review usano contesti nuovi indipendenti, punto fisso HEAD iniziale e
delta documentale effettivo. Comandi Git di riferimento:

```bash
git rev-parse 61ce18ae1d4e0458b63e970b793b0b3a634a3adf
git diff 61ce18ae1d4e0458b63e970b793b0b3a634a3adf...HEAD
git log 61ce18ae1d4e0458b63e970b793b0b3a634a3adf..HEAD --oneline
```

Gli ultimi due sono vuoti: non sostituiscono baseline/delta/copied-prima per i
non tracciati. Nessun commit/staging/push, invio a terzi o approvazione umana.

## Chiusura

Due sub-agenti avviati con `fork_turns=none`, incarichi Standards e Spec separati,
senza lettura reciproca dei rapporti. Entrambi conformi, zero rilievi aperti.
Le verifiche realmente svolte da ciascuno sono nei rispettivi rapporti; non
sono presentate come nuova compilazione o test applicativi.

Dopo le review, Python ha aggregato i rapporti senza fonderne gli assi e
ricontrollato i 22 hash congelati, la baseline e le copie iniziali, tutte le
fonti, i link locali della consegna e i manifest. `Status: done` riguarda il
ticket documentale; gli autori non sono considerati approvanti. Il PDF resta
identico al congelato, nessuna nuova compilazione necessaria.

Delta finale: confronto dei sette file preesistenti con prima/, più inventario
di ogni nuovo file, inclusi i non tracciati e i rapporti. delta-files.json non
calcola al proprio interno l'hash di sé stesso, SHA256SUMS, input-review.json o
finalizzazione.txt; questi inventari sono descritti come tali. SHA256SUMS
copre tutti i file di questa directory, escluso sé stesso, verificato riga per
riga. Le impronte degli artefatti esterni alla directory sono in input-review.json.
Controlli finali e assenza di commit registrati in finalizzazione.txt.
