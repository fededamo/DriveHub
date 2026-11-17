# Comandi e strumenti del ticket 13

Operazioni esclusivamente documentali, 17 novembre 2026. Directory iniziale
`/home/federico/dev/Drivehub/DriveHub`. Nessun checker storico rieseguito o
modificato. Nessuna installazione di dipendenze o modifica alla toolchain
storica è stata necessaria.

## Acquisizione

`git status --porcelain=v1 --untracked-files=all` e `git rev-parse HEAD`
sono acquisiti in `stato-iniziale.txt`. La baseline usa `git ls-files`
per tracciati/non ignorati e include anche i file ignorati nelle directory
storiche pertinenti (`Relazione_SWE`, `docs/evidence`, `docs/relazione`,
escluso `build`). SHA-256 e dimensioni di 2.899 file in `baseline.json`.
L’archivio editoriale conserva i 227 file attivi/originali con i percorsi
dalla radice; il checker nuovo riscontra ogni membro contro la baseline.
Gli altri storici restano in posizione: nessun log o ZIP precedente sostituito.

`git diff 61ce18ae1d4e0458b63e970b793b0b3a634a3adf...HEAD` e
`git log 61ce18ae1d4e0458b63e970b793b0b3a634a3adf..HEAD --oneline` sono
vuoti, perché il lavoro editoriale è già non tracciato. Non costituiscono
il delta della review; valgono la baseline, il tar e il diff prodotto da
`verifica.py`, comprensivi delle nuove evidenze.

## Ambiente realmente usato

Toolchain persistente verificata in `/home/federico/dev/Drivehub/.ticket11-tools`:
XeTeX 3.141592653-2.6-0.999997, TeX Live 2025/Debian; pacchetti Debian
2025.20260124-1 e Latin Modern 2.005. Risoluzione reale di classe, pacchetti,
lingua italiana e font OpenType tramite `kpsewhich`; versione completa,
librerie runtime e percorsi in `strumenti-versioni.txt` e `strumenti.json`.
Le compilazioni attestano la disponibilità effettiva. Python del venv
persistente con PyMuPDF/Pillow, versioni registrate in `strumenti.json`.

Le vecchie directory temporanee non sono assunte disponibili. È stata
creata `/tmp/drivehub-ticket13-tex`, copiando le risorse di cache/config/font
dalla toolchain persistente; nessuno script storico adattato. Le variabili
effettive sono in `ambiente-tex.sh`. Questi percorsi sono esterni allo ZIP
e riguardano solo la toolchain, non contenuti editoriali.

## Compilazioni reali

Dalla directory `docs/relazione`, dopo `source verifiche/ticket-13/ambiente-tex.sh`,
comando eseguito tre volte per la baseline e tre per il documento aggiornato:

```sh
xelatex -no-shell-escape -halt-on-error -interaction=nonstopmode -recorder -output-directory=build/ticket13-baseline main.tex
xelatex -no-shell-escape -halt-on-error -interaction=nonstopmode -recorder -output-directory=build/ticket13 main.tex
```

Ogni invocazione ha il proprio `compilazione-baseline-N.txt` o
`compilazione-N.txt`. La prima composizione ricostruisce l’indice, i passaggi
successivi raggiungono 151 pagine. Gli hash di `.toc/.aux/.out` sono stabili
fra secondo e terzo (`stabilizzazione-N.sha256`). `latex-log.txt` e
`input-compilazione.fls` conservano l’ultimo passaggio. Il PDF è copiato da
`build/ticket13/main.pdf` a `relazione-drivehub.pdf`; il checker ne verifica
l’identità binaria.

## Controlli nuovi ed estrazione

Con `/home/federico/dev/Drivehub/.ticket11-tools/venv/bin/python` sono stati
eseguiti, dalla radice, i cinque script sotto `docs/relazione/verifiche/ticket-13/`:
`consolida.py`, `raccogli-fonti.py`, `riscontro-storico.py`, `verifica.py` e,
con l’ambiente TeX caricato, `pacchetto.py`. Output conservati nei file
`consolidamento.txt` e `controlli-{fonti,storici,documentali,pacchetto}.txt`.
Gli script includono assert che interrompono l’esecuzione su discrepanze;
non avviano il software. `riscontro-storico.py` usa soltanto letture e Git.

`pacchetto.py` enumera gli input distribuiti, usa ZIP con percorsi relativi,
estrae in `tempfile.mkdtemp`, compila lì tre volte con il comando registrato
in `pacchetto-1-compilazione-N.txt` e analizza il recorder finale.
`autonomia-pacchetto.json` conserva directory concreta, impronte, elenco
dei 73 input editoriali e delle sole risorse TeX esterne; il PDF ricompilato
è conservato qui, quindi la prova non dipende dalla sopravvivenza di `/tmp`.
Confronto di tutti i testi/formati/pixel a 96 dpi; log e stabilizzazione
propri dell’estrazione, non esiti di un vecchio ZIP. Il confronto aggiuntivo
degli oggetti PDF e degli stream decodificati è in `confronto-binario-pdf.json`:
le differenze non sono solo metadati. Date/ID e prefissi dei subset Latin
Modern producono anche lunghezze compresse e offset xref differenti;
normalizzando date, soli prefissi di sei maiuscole e Length degli stream,
gli oggetti/stream di contenuto coincidono. La tabella xref resta distinta
perché descrive gli offset fisici dei byte.

Lettura degli XML nativi e verifica degli allegati/Subject nei 43 PDF;
confronto delle undici coppie con `ticket-10/esportazioni.json` come dati.
Rendering PyMuPDF delle 151 pagine a 108 dpi e dei riferimenti visivi
originali; provini Pillow senza cambiare contenuto editoriale. Apertura
diretta delle immagini con `view_image`, separata dagli assert automatici.
Nessuna acquisizione di schermate applicative.

## Tentativi e limiti dei controlli

Durante la predisposizione sono state corrette assunzioni dei **nuovi**
checker: macro dei titoli/`scheda`, manifest fonti in forma lista o mappa,
intervalli AsyncLoad e SaleType fino all’ultima riga reale. Una ricerca
troppo ampia di `Rerun` intercettava l’informazione del pacchetto rerunfilecheck:
ora distingue `Rerun to`/`Warning:` dai messaggi informativi. I due intervalli
delle tabelle sono stati verificati sugli effettivi begin/end. Sono errori
di acquisizione/checker, non regressioni del documento o risultati di test.
Un lotto di sei provini superava il contesto dello strumento: le pagine
127–151 sono state riaperte in lotti minori prima di redigere il verbale.

Non si è rigenerato alcun diagramma: gli undici rifacimenti del ticket 10
sono invariati, coerenti col codice e osservati nel nuovo PDF; Desktop
32.6.10 resta una provenienza storica verificata per hash. I cinque messaggi
TeX persistono senza soppressione e sono valutati nel verbale, non trattati
come exit failure. Non sono state eseguite nuove prove applicative.
