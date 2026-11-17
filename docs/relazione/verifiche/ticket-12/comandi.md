# Comandi, strumenti e tentativi effettivi

17 novembre 2026. Applicate skill implement fornita dall'utente, ponytailfull e
code-review; i divieti espliciti sostituiscono TDD/suite/commit generici.
Serena initial_instructions letta; Graphify non generato per questo confronto
mirato. Non usati draw.io/export perché nessuna tavola richiede modifiche.
Le fonti decisive sono state lette nei loro intervalli originali, recuperando
anche gli output compressi dal proxyHeadroom prima delle conclusioni.

## Baseline

Working tree iniziale con relazione/docsAGENTS/agents non tracciati. SHA-256 di
2804 file sotto src/docs/Relazione_SWE e README/POM, inclusi PDF e storici;
intermedi di compilazione rigenerabili esclusi. Archiviate 223 copie editoriali.
[Manifest immutato](baseline.json), [archivio](baseline-editoriale.tar.gz),
[impronta](baseline-editoriale.sha256), [statoGit](stato-iniziale.txt).
HEAD 61ce18a immutato. Nessun vecchio manifest rigenerato.
La review confronta questa fotografia, non il solo diffGit vuoto.

## Strumenti ricontrollati all'avvio

Nel PATH iniziale soltanto Python tra gli strumenti cercati: assenti xelatex,
pdflatex/latexmk e gli eseguibiliPoppler. La directory `.ticket11-tools` esisteva:
verificati xelatex--version, kpsewhichlistings, importPyMuPDF e apertura reale
delPDFiniziale143 pagine. XeTeX0.999997/TeXLive2025Debian, Python3.14.4,
PyMuPDF2.1/MuPDF1.28.6. [Versioni](strumenti-versioni.txt).
Risorse/eseguibili letti dalla toolchain11; VAR/CONFIG/HOME/LOCAL/SYSVAR/
SYSCONFIG/XDG separati in `/tmp/drivehub-ticket12-tex`, copiando soltanto
le risorse documentali utili. [Variabili effettive](ambiente-tex.sh).
Nessuna installazione o download; nessun uso di percorsi temporanei presunto
prima della verifica. Le risorse temporanee restano prerequisiti esterni.

## Compilazione

Dalla directory `docs/relazione`, ambiente esportato dal nuovo script:

```bash
source verifiche/ticket-12/ambiente-tex.sh
xelatex -no-shell-escape -halt-on-error -interaction=nonstopmode -recorder -output-directory=build/ticket12 main.tex
cp build/ticket12/main.pdf relazione-drivehub.pdf
cp build/ticket12/main.log verifiche/ticket-12/latex-log.txt
```

Comandoxelatex eseguito sette volte, log `compilazione-1.txt` fino a7:

- Tentativo precedente dalla directorybuild con `../main.tex`: exit1,
  inputrelativi non trovati; [log](compilazione-primo-tentativo.txt).
- Passaggi 1/2 riusciti ma non definitivi: nuovi rinvii/indice, glifo € e
  spaziature da sistemare. Il tentativo di modifica prima di2 usava il
  percorso dal cwd errato e si è fermato su FileNotFoundError, senza scritture.
- Passaggi 3/4 con ulteriori correzioni/stabilizzazione; il mappingliterate
  del simbolo€ non era efficace in questa distribuzione. Una provaTeX minima
  in/tmp ha verificato Latin Modern Mono OpenType; adottato nel solo listato
  penale, senza alterare gli estratti letterali o i capitoli1–3.
- Passaggi 5/6/7: exit0; rinvii stabilizzati in6/7,151 pagine. Il finale è7;
  cinque messaggi TeX preesistenti inC, nessun box/glifo/rinvio difettoso nuovo.

Il log build/main preesistente era un intermedio diverso: conservato come
`latex-build-main-preesistente.log`, non usato per attribuire i cinque
messaggi delPDF di 143 pagine. La baseline pertinente è il log finale 11.
Il recorder `.fls` contiene soltanto input editoriali dal repository e risorse
TeX esterne; nessun frammento o contenuto editoriale in/tmp richiesto.

## Controlli nuovi

```bash
/home/federico/dev/Drivehub/.ticket11-tools/venv/bin/python docs/relazione/verifiche/ticket-12/verifica.py
```

Script esclusivamente documentale: baseline/delta, struttura15 voci,
censimento9 funzioni e15 blocchi (tre iniziali identici), confronto UTF8esatto
sorgenti/LaTeX, ricalcolo esemplificativo importi, conteggi da log/XML storici,
inputmanifest/harness, PDF/rinvii/link/font/coordinate, conservazione1–3a
pixel/testo e5/A/Bsenza piè di pagina, estratto e rendering.
Gli assert non dimostrano automaticamente la corrispondenza semantica o la
visibilità: letture e [verbalevisivo](controllo-visivo.md) separati.
Due prime verifiche del confrontoPDF si erano fermate su menoU2212 e
spezzatura shellconbackslash; il confronto permette ora le sole differenze
tipografiche documentate, conservando il confrontoletterale dei sorgenti.
Nessun checker storico eseguito contro il documento avanzato.

ParsingXMLfinaliE2 e mirati separati, logE1full/targeted/RED/ZIP e manifestE3;
nessuno scriptstorico importato/eseguito. Il confronto Git treeE1/HEAD su
src/POM è vuoto; E2 ha 28inputdiversi. Nessuna consultazione di backupprivati.
Tutti i risultati applicativi restano storici; le nuove prove sono TeX/PDF.

Non eseguiti Java/Maven/JUnit/GUI/harness/DB/Docker, commit/invii/pubblicazioni,
Windows/macOS/Tectonic o nuovoZIPfinale. Nessuna approvazioneautori presunta.

## Precisazione heap durante la review

Correzione autonoma del principale: 192 MB è il massimo heap configurato da
-Xmx192m, non tutta la memoria JVM. Conservati candidato, sorgente, estratto
e manifest iniziali; nuovo confronto in `confronto-correzione-heap.json`.
`compilazione-heap-1.txt` si ferma prima di leggere il documento perché
`xelatex.fmt` non era più presente nella cache temporanea. Ricopiati soltanto
var/config/home documentali dalla toolchain verificata; nessun prerequisito
applicativo o database coinvolto. Passaggi `compilazione-heap-2.txt` e
`compilazione-heap-3.txt` exit 0, rinvii stabili, 151 pagine. Il log conclusivo
è quello della serie heap, copiato in `latex-log.txt`.

Il checker aggiornato passa: solo pagina fisica 107 diversa nei pixel rispetto
al candidato iniziale, altre 150 identiche. Pagina 107 osservata nuovamente;
restano cinque messaggi TeX in C, nessun nuovo box/glifo/rinvio irrisolto.
`candidato-review.json` identifica questo candidato per il ricontrollo dei revisori.
