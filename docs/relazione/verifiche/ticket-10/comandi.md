# Comandi, strumenti e limiti — ticket 10

Repository: `/home/federico/dev/Drivehub/DriveHub`. Lavoro documentale del
16 novembre 2026, HEAD iniziale riportato in `baseline.json`. Skill applicate:
implement fornita dall'utente, ponytail full, drawio e code-review. Per il
registro tecnico si segue la prosa della relazione e delle verifiche fornite;
non serve ricercare esempi personali esterni. Nessun grafo Graphify locale:
non ne viene generato uno per questo confronto mirato.

## Baseline e letture

Prima delle modifiche, stato Git completo, elenco dei tracciati e impronte
di 3.053 file di `src`, `docs`, `Relazione_SWE`, compresi i non tracciati e
gli storici ignorati. Archivio di 210 materiali editoriali: originale,
LaTeX, figure, screenshot, PDF precedente. Manifest e archivio non vengono
rigenerati. Il diff iniziale Git non avrebbe rappresentato la relazione.
Letture prescritte: istruzioni docs e agents, piano, indice, matrice e
vincoli diagrammi, spec/ticket 10, glossario, due ADR, README/istruzioni di
compilazione e README/revisione ticket 09. Confrontati i tredici input attivi
del capitolo originale e quelli attuali, A/B, diagrammi e schermate.
`fonti.json` identifica le fonti tecniche e il contesto immutato; non afferma
una lettura integrale o una nuova esecuzione di ciascun test censito.

## Diagrammi

Letta la skill drawio e le regole XML ufficiali. XML nativo per conservare
forme, notazione, palette e geometrie degli autori; per le sette ricostruzioni
UML selezione manuale dai riferimenti visivi e confronto con codice corrente,
senza generazione automatica di UML dai Java. `riadatta-diagrammi.py` registra
le scelte, valida unicità degli ID, riferimenti e geometrie degli archi.
Non rilanciarlo dopo correzioni manuali senza conservarle: rigenera sette
ricostruzioni e package; per tre tavole lavora sulle copie corrette esistenti.

Scaricati da GitHub ufficiale ed estratti con `dpkg-deb -x` in `/tmp` draw.io
Desktop 29.0.3 e poi **32.6.10**, versione usata per le undici esportazioni.
La prima versione non riconosceva correttamente l'input con le opzioni usate;
32.6.10 ha completato gli export. L'avvio ordinario di Electron è fallito
per la sandbox; esecuzione con escalation autorizzata. Nessuna installazione
di sistema o caricamento dei diagrammi su servizi pubblici.

La revisione automatica ha rifiutato il primo comando dell'esportatore
perché includeva il repair pass in-place sui sorgenti, interpretandolo come
rischio per gli originali. Il comando non è stato eseguito. Verificate le
impronte dei quattro originali: intatti. Alternativa approvata ed eseguita:
esportatore in sola lettura dei sorgenti, con parsing XML e verifica che
l'impronta prima/dopo l'esportazione sia identica. Gli originali `docs/*.drawio`
non sono input in scrittura del generatore né dell'esportatore.

```bash
/tmp/drivehub-ticket09-tools/venv/bin/python docs/relazione/verifiche/ticket-10/esporta-diagrammi.py
```

Ogni comando invoca draw.io con `-x -f pdf -e -a --crop -b 20`; percorsi
assoluti e log separato per tavola. Conservati gli export grezzi, poi composti
su A3 con PyMuPDF, mantenendo il contenuto vettoriale e allegando il nativo
identico. Sequenze in A3 verticale, altre tavole A3 orizzontali; due pagine ER.
La composizione non cambia grafi o etichette. Hash delle undici coppie in
`esportazioni.json`; l'estratto viene dal PDF completo effettivo.

## Compilazione e controllo documentale

Riusati XeLaTeX/TeX Live 2025 e venv Python predisposti nel ticket 09 in `/tmp`.
Le variabili sono in `../ticket-09/ambiente-tex.sh`; nessun file storico
modificato. Tre passaggi finali dalla directory `docs/relazione`:

```bash
source /tmp/drivehub-ticket09-tools/env.sh
xelatex -no-shell-escape -halt-on-error -interaction=nonstopmode -recorder -output-directory=build/ticket10 main.tex
xelatex -no-shell-escape -halt-on-error -interaction=nonstopmode -recorder -output-directory=build/ticket10 main.tex
xelatex -no-shell-escape -halt-on-error -interaction=nonstopmode -recorder -output-directory=build/ticket10 main.tex
cp build/ticket10/main.pdf relazione-drivehub.pdf
```

Conservati i primi due passaggi esplorativi e i tre log del candidato finale.
Durante la preparazione un avvio dalla radice, invece che dalla directory
LaTeX, non trovava gli input relativi: corretto il working directory. Alcuni
passaggi intermedi sono stati ripetuti dopo correzioni di impaginazione;
non costituiscono nuove prove applicative. Gli avvisi di riferimenti dei
primi passaggi non sono esiti del log finale.

```bash
/tmp/drivehub-ticket09-tools/venv/bin/python docs/relazione/verifiche/ticket-10/prepara-artefatti.py
/tmp/drivehub-ticket09-tools/venv/bin/python docs/relazione/verifiche/ticket-10/verifica-documenti.py
```

Il controllo confronta baseline e nuovi file, 47 voci, sei schede e nove
aggiuntive, diciotto funzioni UI, attributi delle tredici relazioni, undici
coppie draw.io/PDF e allegati, rinvii, estratto, rendering e capitolo 1 identico.
Gli stati di avanzamento non vengono selezionati prima delle review.
Il controllo visivo e i messaggi residui sono nel verbale dedicato.
Nessun Maven, build Java, suite applicativa, JavaFX, DB, Docker o harness;
nessun commit, invio o pubblicazione. Tectonic e altri sistemi operativi
non verificati. Gli ZIP storici restano riferiti alle proprie versioni.

## Ripresa e chiusura nella stessa attività

Alla ripresa gli strumenti temporanei non erano più presenti. La prima
invocazione dell'esportatore non è partita (`python: No such file or directory`).
Ripristinati il venv sotto lo stesso percorso, gli stessi diciannove pacchetti
TeX ufficiali mediante `apt-get download` e `dpkg-deb -x`, e draw.io 32.6.10
dalla release ufficiale GitHub. Nessuna installazione di sistema. Python
della ripresa è 3.14, mentre il precedente era 3.13; PyMuPDF 2.1 e Pillow 13.0.0.
Impronte dei pacchetti e dei motori in `strumenti-ripresa.json`.

Per TeX ricreati gli indici locali dopo rimozione del solo collegamento
`ls-R` temporaneo verso `/var/lib/texmf`, `language.dat` inglese/italiano,
formato con `xelatex -ini -etex ... '\input xelatex.ini'` collocato in
`$TEXMFVAR/web2c/xetex`, mappe riunendo i file ufficiali sotto
`var/lib/tex-common/fontmap-cfg/{texmf,texlive}` e `updmap-user --nohash`.
Nella sola configurazione temporanea `dvipdfmx-paper.cfg` è esplicitata
la carta `p 210mm,297mm`. Non cambia `main.tex` né le dimensioni delle
tavole incluse con `fitpaper`.

I log `ripresa-compilazione-1..5.txt` conservano i tentativi falliti per
formato, carta e mappe font; il sesto completa un primo PDF con riferimenti
da stabilizzare. Ripetuti poi i **tre passaggi finali riusciti** nei file
`compilazione-finale-1..3.txt`, aggiornato il PDF completo e rigenerati
estratto, rendering e controllo documentale. Il confronto a pixel del
capitolo 1 con la baseline passa anche con la toolchain ripristinata.

Un precedente tentativo opzionale di routing automatico draw.io non era
partito perché la revisione automatica segnalava crediti esauriti nel
workspace. Le correzioni sono state realizzate nel nativo XML con la skill
drawio, mediante punti di raccordo espliciti; le successive esportazioni
ordinarie sono state approvate ed eseguite. Risolti i collegamenti che
attraversavano membri, le dimensioni di Client/BankAccount, i nomi delle
callback e la visibilità dei campi. Tutti gli undici export aggiornati sono
stati controllati; le quattro pagine delle classi riaperte nel PDF finale.

Durante le correzioni richieste dalla review Spec due export sono andati
in timeout dopo 55 secondi; un successivo renderer è stato terminato
(`Renderer process gone (killed)`). Il contatore cgroup riportava un
`oom_kill` e la memoria disponibile era circa 275 MiB. Rimossi soltanto
i download temporanei già estratti e la documentazione della toolchain
in `/tmp`, dopo averne conservato le impronte. Spostati gli strumenti
estratti dalla tmpfs al disco del workspace, fuori dal repository, con
symlink sotto i medesimi percorsi `/tmp`; memoria disponibile oltre 1 GiB.
L'esportatore usa profili separati per tavola e `--disable-dev-shm-usage`.
Nessun originale o documento storico rimosso e nessuna modifica al sistema.
Con questa configurazione riescono tutte le undici esportazioni; seguono
tre passaggi XeLaTeX riusciti e rigenerazione di estratto/rendering/checker.
Le tre correzioni Spec riguardano callback distinte, metodo del calcolatore
e instradamento dei package. Il rapporto iniziale è conservato e i due
assi ricevono un ricontrollo indipendente delle correzioni.

Dopo il controllo conclusivo di PDF, ZIP e 303 impronte, rimosse le sole
copie temporanee degli strumenti fuori dal repository e i relativi symlink.
I pacchetti binari non fanno parte della consegna; versioni e impronte
restano in `strumenti-ripresa.json`. Nessun materiale editoriale rimosso.
