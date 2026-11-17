# Comandi e ambiente — ticket 09

16 novembre 2026, repository `/home/federico/dev/Drivehub/DriveHub`.
Applicate la skill `implement` fornita, ponytail full e code-review a due
assi. Il perimetro esplicito dell'utente prevale su test software e commit
previsti dalla skill generica. Nessuna nuova prova applicativa.

## Letture e baseline

Letti `docs/AGENTS.md`, le tre istruzioni in `docs/agents/`, piano, indice,
matrice, specifica e ticket 09, GLOSSARY, ADR 0001–0002, README della relazione
e istruzioni di consegna. Confrontati i due sorgenti dell'introduzione e
le fonti in `fonti.json`; consultate le istruzioni tecniche di avvio e testing.
Nessun grafo Graphify locale: nessuno generato per il confronto mirato.

Prima delle modifiche: `git rev-parse HEAD`,
`git status --short --untracked-files=all`, `git ls-files -z` e
`git ls-files --others --exclude-standard -z`. Alle impronte aggiunti i file
presenti nelle aree storiche anche se ignorati; archiviati i materiali
editoriali senza build e verifiche annidate. Nessuna baseline da HEAD usata
al posto dei 468 file non tracciati. Nessuna rigenerazione dopo il riordino.

## Strumenti effettivamente predisposti

All'inizio assenti XeLaTeX, Tectonic, renderer PDF e PyMuPDF nel PATH/ambiente
Python; assente anche il vecchio `/tmp/drivehub-ticket08-tools`.
Creato venv con Python 3.13.16 in `/tmp/drivehub-ticket09-tools/venv` e
installati PyMuPDF 2.1 / MuPDF 1.28.6 e Pillow 13.0.0.
I primi tentativi ordinari di pip e apt fallivano per risoluzione DNS;
download con escalation autorizzata riusciti. Nessun rifiuto di auto-review.

Pacchetti ufficiali Ubuntu scaricati con `apt-get download` ed estratti
con `dpkg-deb -x` sotto `/tmp/drivehub-ticket09-tools/texlive`:

```
texlive-binaries texlive-base texlive-latex-base texlive-latex-recommended
texlive-latex-extra texlive-lang-italian texlive-fonts-recommended
fonts-lmodern lmodern libkpathsea6 libptexenc1 libsynctex2 libtexlua53-5
libteckit0 libmpfi0 libpaper2 libpotrace0 libzzip-0-13t64 texlive-xetex
```

Versioni e impronte dei 19 pacchetti in `strumenti.json`; motore e Python
in `ambiente.txt`. Nessun `apt install`, cambiamento al sistema o a HOME.
Le variabili della toolchain sono riprodotte in `ambiente-tex.sh`.
Gli eseguibili e le cache in `/tmp` restano prerequisiti temporanei,
non materiali editoriali indispensabili alla compilazione su un altro PC.

Preparati `language.dat` con inglese/italiano, mappe font e formato XeLaTeX.
I primi tentativi di updmap e formato fallivano: percorsi sys/user coincidenti,
indice ls-R del pacchetto collegato a un percorso di sistema assente e
`paperspecs` inizialmente cercato nella directory sbagliata. Conservati
`fontmap.txt`, `fontmap-tentativo2.txt`, `formato-xelatex.txt` e
`formato-xelatex-tentativo2.txt`. Corrette soltanto le directory temporanee:
ls-R locale rigenerato, TEXMFSYSVAR distinto da TEXMFVAR, paperspecs copiato
da `texlive/etc/`. Generazioni finali in `fontmap-finale.txt` e
`formato-xelatex-finale.txt`; le compilazioni successive sono riuscite.

## Compilazione reale

Dalla directory `docs/relazione`, dopo aver caricato l'ambiente temporaneo:

```bash
source /tmp/drivehub-ticket09-tools/env.sh
mkdir -p build/ticket09
xelatex -no-shell-escape -halt-on-error -interaction=nonstopmode -recorder -output-directory=build/ticket09 main.tex
xelatex -no-shell-escape -halt-on-error -interaction=nonstopmode -recorder -output-directory=build/ticket09 main.tex
xelatex -no-shell-escape -halt-on-error -interaction=nonstopmode -recorder -output-directory=build/ticket09 main.tex
cp build/ticket09/main.pdf relazione-drivehub.pdf
```

Tre passaggi prima del riordino, conservati con prefisso `prima-`, e tre
passaggi finali dopo il riordino, tutti riusciti. Log finali in
`compilazione-1.txt`–`compilazione-3.txt`; copie del log TeX, TOC, AUX e FLS
qui conservate. Il primo passaggio della prima compilazione richiedeva la
stabilizzazione di indice e riferimenti; quello finale non la richiede.
Il PDF finale ha 113 pagine. I cinque messaggi residui sono valutati nel
[controllo visivo](controllo-visivo.md), senza dichiararli eliminati.

Estratto e rendering prodotti direttamente dal PDF finale con PyMuPDF:
`insert_pdf` sulle pagine zero-based 3–5; rendering delle pagine fisiche
2–6 e 107–111 con matrice 1.5, equivalente a 108 dpi. Il confronto con il
PDF nella baseline usa tutte le pagine a 72 dpi, testo e pixel, e segnala
la sola pagina 6 modificata. Il JSON `prima-confronto-pdf.json` si riferisce
alla compilazione prima del riordino, allora identica alla baseline.

## Controlli ripetibili

Dalla radice del repository:

```bash
/tmp/drivehub-ticket09-tools/venv/bin/python docs/relazione/verifiche/ticket-09/verifica-documenti.py
git diff --check
```

Il controllo verifica conservazione, ambito del delta inclusi i nuovi file,
assenza di righe perse/aggiunte nel capitolo, PDF, estratto, indice, link
interni, etichette e messaggi TeX. Il primo controllo dei rinvii ha rilevato
il parametro formale `#3` della macro `tavolaautori`: corretta la verifica
documentale per controllare i tre argomenti concreti, senza modificare LaTeX.
Il risultato è in `controlli-documentali.txt` / `esito-documentale.json`.

La review usa `baseline.json`, `baseline-editoriale.tar.gz`, il delta
testuale e l'elenco dei file aggiunti: un diff fra commit sarebbe vuoto
e ometterebbe la relazione non tracciata. Nessuna suite Maven, SQL, JavaFX,
Docker o test applicativo eseguito. Nessun commit.
