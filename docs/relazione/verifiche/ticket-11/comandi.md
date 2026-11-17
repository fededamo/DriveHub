# Comandi e ambiente effettivo — ticket 11

16 novembre 2026, repository `/home/federico/dev/Drivehub/DriveHub`.
Applicate implement fornita dall'utente, ponytail full e code-review.
Le indicazioni generiche della skill su test applicativi e commit cedono
al perimetro documentale esplicito. Nessun grafo Graphify locale presente;
non ne è stato generato uno per questo confronto mirato. Stile e convenzioni
sono quelli della relazione e delle evidenze fornite.

## Baseline

Prima delle modifiche: stato Git completo, HEAD, SHA-256 e dimensioni di
3.277 file di src/docs/Relazione_SWE e file di radice, inclusi non tracciati
e ignorati. Archiviate 223 copie editoriali: originale, sorgenti correnti,
tavole/screenshot e PDF precedente. La baseline non viene rigenerata.
`baseline-editoriale.sha256` consente di verificare l'archivio autonomamente.
Il diff Git non comprendeva la relazione, già non tracciata.

Letti docs/AGENTS e tre istruzioni richiamate, piano, indice, matrice con
capitolo 3/censimento/vincoli, spec/ticket 11, glossario, ADR 0001/0002,
README e LEGGIMI della relazione; README/revisione 05 e
README/revisione/controllo-visivo/comandi 10. Confrontati gli otto file
originali attivi, tutto il capitolo corrente e i suoi tredici frammenti,
capitolo 2/A/B e riferimenti alle tavole già riallineati, sorgenti tecnici,
schema, configurazione, POM, README e architettura.

## Strumenti disponibili e predisposti

Il PATH iniziale non conteneva xelatex, pdflatex, tectonic, latexmk,
pdftoppm, pdftotext o pdfinfo. Presenti Python 3.14.4, uv, curl, wget e
apt/dpkg-deb; PyMuPDF e Pillow inizialmente assenti. Le toolchain dei
ticket 09/10 erano state rimosse: non si è dedotto il funzionamento da
vecchi log.

Scaricati con `apt-get download` i diciannove pacchetti Ubuntu ufficiali
elencati con versione/impronta in [strumenti.json](strumenti.json), poi
estratti con `dpkg-deb -x` sotto
`/home/federico/dev/Drivehub/.ticket11-tools/texlive`, fuori dal repository
DriveHub. Nessuna installazione di sistema. Il download iniziale nella
sandbox non risolveva archive.ubuntu.com; ripetuto con rete autorizzata.
Venv documentale nello stesso albero; PyMuPDF e Pillow installati nel solo
venv dopo il fallimento iniziale della rete nella sandbox.

XeTeX 0.999997 / TeX Live 2025 Debian; lingua inglese/italiana e font Latin
Modern. L'ambiente isola TEXMFROOT/DIST/DEBIAN, VAR, CONFIG, HOME, LOCAL,
SYSVAR e SYSCONFIG; non cambia HOME dell'utente. Copia delle variabili in
[ambiente-tex.sh](ambiente-tex.sh). Indici locali con mktexlsr, mappe dai
cinque file ufficiali sotto var/lib/tex-common/fontmap-cfg e
`updmap-user --nohash`; formato con:

```bash
xelatex -ini -etex -jobname=xelatex -output-directory="$TEXMFVAR/web2c/xetex" '\input xelatex.ini'
```

Nella sola configurazione estratta dvipdfmx-paper.cfg, `p a4` è sostituito
da `p 210mm,297mm`: libpaper estratta non disponeva della configurazione
di sistema. Non cambia main.tex né le dimensioni delle tavole incluse con
fitpaper. Impronte dei pacchetti, eseguibili e versioni Python in strumenti.

## Compilazioni effettive

Dalla directory `docs/relazione`, ambiente della toolchain appena descritta.
L'ultima serie valida, dopo il controllo numerico e le precisazioni di identità/PBKDF2/configurazione, è:

```bash
source /home/federico/dev/Drivehub/.ticket11-tools/env.sh
xelatex -no-shell-escape -halt-on-error -interaction=nonstopmode -recorder -output-directory=build/ticket11 main.tex
xelatex -no-shell-escape -halt-on-error -interaction=nonstopmode -recorder -output-directory=build/ticket11 main.tex
xelatex -no-shell-escape -halt-on-error -interaction=nonstopmode -recorder -output-directory=build/ticket11 main.tex
cp build/ticket11/main.pdf relazione-drivehub.pdf
cp build/ticket11/main.log verifiche/ticket-11/latex-log.txt
```

Tre exit 0, log `compilazione-review-1..3.txt` del 17 novembre. PDF finale di 143 pagine,
rinvii stabilizzati. I file `compilazione-finale-*` conservano un candidato
precedente, non il PDF conclusivo. Sono conservati anche questi tentativi:

- `compilazione-1.txt`: interrotta dalla configurazione carta, broken pipe.
- `compilazione-2.txt`: interrotta da operatorname non disponibile con i
  pacchetti del documento. Formula riscritta con comandi standard già
  disponibili, senza aggiungere pacchetti o modificare main.tex.
- `compilazione-3/4.txt`: riuscite, ma con riferimenti da stabilizzare e
  box di nuova impaginazione da risolvere; non sono la verifica finale.
- `compilazione-finale-1..3.txt`: candidate con un sesto messaggio Infinite
  glue, nella nuova tabella degli importi. Sostituite le sole due nuove
  longtable con tabelle su una pagina; serie `compilazione-tabelle-1..3.txt`
  riuscita con soli cinque messaggi preesistenti in C.
- [Controllo numerico iniziale](controllo-esempi-prima.txt): corretto
  l'esempio prezzo/stima da 25.334,19 a 25.333,87, derivato con gli stessi
  fattori della formula corrente. Serie `compilazione-esempi-*`
  e nuovi rendering/verifiche dopo la correzione. Seguono le precisazioni
  su identità Model/Vehicle, parametri PBKDF2 e configurazione, con tre
  passaggi `compilazione-precisazioni-*` e nuovi controlli. Nessun Java eseguito.

## Verifiche e artefatti

```bash
/home/federico/dev/Drivehub/.ticket11-tools/venv/bin/python docs/relazione/verifiche/ticket-11/verifica.py
```

Lo script scrive soltanto nuove evidenze del ticket 11. Verifica baseline,
delta anche dei non tracciati, 35 voci, 14 corrispondenze, tutti i 27
estratti esatti nei Java/SQL e nel PDF, esempi numerici, rinvii, font e
link interni, identità a testo/pixel di capitoli 1/2, estratto e rendering.
Non esegue checker storici contro assunzioni della vecchia composizione.
Il controllo visivo è separato e non viene dedotto dagli assert.

Nessuna tavola modificata o riesportata: nativi ed export esistenti sono
input della compilazione, intatti come gli originali. Non è stato necessario
un intervento draw.io nel ticket 11. I nove frammenti del testing e il
pacchetto finale restano rispettivamente ai ticket 12/13.

Nessun Maven, test applicativo, build Java, avvio UI, DB, Docker o harness;
nessun commit, invio o pubblicazione. Tectonic, Windows e macOS non
verificati. I cinque messaggi TeX di C restano dichiarati e osservati.
La toolchain estratta è temporanea e non fa parte della consegna; gli input
editoriali e gli estratti non dipendono da essa per il proprio contenuto.

## Ripresa e congelamento del 17 novembre

La prima review Spec del 16 novembre si è interrotta per esaurimento dei
crediti del workspace, senza rapporto conclusivo. Alla richiesta di
completamento, riavviate entrambe le review su contesti separati, conservando
la stessa baseline del ticket 11; nessun esito è stato dedotto dai tentativi
interrotti. La toolchain predisposta è ancora disponibile e ricontrollata.

Ripristinata a 2 la profondità della numerazione dopo il gruppo del capitolo
3, per confinare la numerazione a quattro livelli al suo corpo. Eseguiti
tre passaggi `compilazione-chiusura-1..3.txt`, tutti exit 0, poi il checker
aggiornato. Il PDF resta di 143 pagine: confronto di tutte le pagine col
candidato osservato, identità di testo e pixel
([risultato](confronto-candidato-finale.json)). Nessuna modifica narrativa.

Il controllo ora rende ripetibile anche il riuso degli estratti iniziali
(11 identici e 2 estesi solo con la graffa finale) e il confronto delle 38
pagine di capitoli 4/5 e appendici A/B/C, rimossa soltanto l'ultima riga
numerica del piè di pagina. La baseline e gli storici restano immutati.

## Correzioni dopo la prima review

La review Standards ha riscontrato una frase errata sul modificatore di
AbstractUser: il sorgente è `public abstract class`, con costruttore protetto.
Corretta la frase in 3.1.1. La review Spec ha riscontrato l'omissione di una
spiegazione valida della stesura iniziale: SQLSTATE23503 nella cancellazione
dell'identità tradotto in errore di dominio, con causa SQL conservata e
tassonomia non uniforme dei DAO. Recuperata in 3.4.2, riscontrando
PostgresUserDao236–241. Il revisore Spec ha poi confermato anche il primo
rilievo, distinguendone la provenienza. I rapporti iniziali restano intatti.

Tre nuovi passaggi `compilazione-review-1..3.txt`, tutti exit0, producono
ancora 143 pagine e i soli cinque messaggi preesistenti in C. Il checker
aggiornato passa. Rispetto al candidato della prima review cambiano solo
le pagine fisiche83 e99; le altre141 restano identiche nei pixel
([confronto](confronto-correzioni-review.json)). Riaperte entrambe dal PDF
conclusivo e affidate ai due ricontrolli indipendenti. Nessun software,
schema, dato o storico modificato.

Dopo i due PASS sono aggiornati soltanto ticket11, README della relazione
e riepiloghi in piano/indice/matrice. Il checker viene rieseguito sulla
chiusura amministrativa: conserva il PDF estratto già revisionato quando
testo, pixel e titolo coincidono, evitando di cambiarne inutilmente
l'identificativo PDF. Restano stabili le impronte degli artefatti esaminati
dai revisori. Il manifest finale comprende i nuovi rapporti e gli stati
aggiornati; nessun manifest storico è rigenerato.
