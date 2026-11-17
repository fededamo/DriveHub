# Revisione finale indipendente — ticket 08

15 novembre 2026. Due revisori in contesti nuovi, incaricati separatamente
secondo la skill `mattpocock-skills:code-review`; nessuna lettura reciproca
prima dei rapporti indipendenti. Le prime esecuzioni interrotte non valgono
come esito: questi rapporti sono stati completati nella ripresa del 15 novembre.

Confine effettivo: baseline di 2.333 file e archivio editoriale iniziale,
inclusi non tracciati; `delta-documentale.diff`, `delta-files.json` e
`input-review.json` per la prima review; correzione diagnostica e chiusura
nei corrispondenti file `*-finale.*`. Il diff Git vuoto, senza commit, non è
usato come prova sufficiente. HEAD invariato:
`61ce18ae1d4e0458b63e970b793b0b3a634a3adf`.

PDF revisionato, 107 pagine, SHA-256:
`e046f9efc5b3a7411354bfdde71b288e0d748f01ebea8c2aa7b3534c5a8990d4`.
ZIP revisionato, SHA-256:
`9b450b53f3bf623b919f81edf63f7b288728bea92818f25b8da8011f6c93837c`.
Entrambi invariati durante correzione del verbale e ricontrolli.

I rapporti iniziali restano qui riportati, compreso il rilievo Spec e la frase
Standards sugli avvisi rettificata nel ricontrollo. L'esito attuale deriva
dai ricontrolli, senza cancellare le valutazioni precedenti.

## Standards

### Rapporto iniziale

Fonte distinta: [review-standards.md](review-standards.md).

15 novembre 2026. Revisore indipendente in contesto nuovo, senza lettura
del rapporto Spec. Esito: **conforme; nessun rilievo aperto**.

## Riscontri effettuati

Letti `docs/AGENTS.md`, le tre istruzioni `docs/agents/`, specifica generale
e operativa, indice, piano, glossario, README e ADR 0001–0002; esaminati
delta, istruzioni di consegna, matrice finale, nuovi controllori 08,
verbali, manifest e log pertinenti.

HEAD risolto a `61ce18ae1d4e0458b63e970b793b0b3a634a3adf`;
diff e log Git vuoti. Verificato indipendentemente il confine effettivo:
2.333 file iniziali, sei modificati, 2.327 invariati, nessuna eliminazione;
104 copie archiviate coerenti con la baseline. I cinque delta testuali
corrispondono all'archivio; tutti i 152 input congelati coincidono.
I 95 membri ZIP coincidono byte per byte con i sorgenti correnti.

Controlli statici nuovi sul PDF: 107 pagine, 158 collegamenti interni
risolti, 86 font incorporati; delta testuale rispetto al PDF iniziale
limitato alle pagine 31 e 77. Verificate tutte le 122 impronte dei rendering;
osservate personalmente le pagine 2, 31 e 77 e confrontate coi rendering
del PDF attuale. Nessun avviso rilevato nel log finale conservato.

## Norme documentate

Nessuna violazione rilevata rispetto a `docs/agents/domain.md`
(termini del glossario, ADR e attribuzione dei risultati alla versione),
`docs/relazione/piano.md` (perimetro documentale e conservazione dello
storico), `docs/relazione/indice.md` (struttura e ordine),
`docs/agents/issue-tracker.md` (ticket, stato, dipendenze e commenti).
La precisazione UC-05 è riscontrabile in `SaleToDealershipService`
e `BankAccount.deposit`; documenta il limite senza dichiararlo corretto.
Lo stato in attesa delle review è coerente con la fase corrente.

## Euristiche

Nessuno smell della baseline richiede interventi nel delta documentale.
Non proposti refactoring applicativi o astrazioni aggiuntive.

## Artefatti e limiti

PDF SHA-256: `e046f9efc5b3a7411354bfdde71b288e0d748f01ebea8c2aa7b3534c5a8990d4`.
ZIP SHA-256: `9b450b53f3bf623b919f81edf63f7b288728bea92818f25b8da8011f6c93837c`.

Non ricompilato né osservate personalmente tutte le pagine: compilazione,
autonomia e osservazione integrale sono evidenze del principale del
14 novembre. Nessuna prova applicativa eseguita. Restano i limiti dichiarati
e la revisione degli autori da svolgere.


### Ricontrollo della correzione

Fonte distinta: [review-standards-ricontrollo.md](review-standards-ricontrollo.md).

15 novembre 2026. Ricontrollo indipendente senza lettura del rapporto Spec.
**Esito: conforme con limite diagnostico esplicito; nessun rilievo Standards aperto.**

## Rettifica e riscontri

La frase «Nessun avviso rilevato nel log finale conservato» del rapporto
Standards iniziale era troppo ampia: nei due log sono presenti cinque
`ignored error: Infinite glue shrinkage found in box being split`.
Il presente ricontrollo rettifica quella conclusione; il rapporto iniziale
rimane conservato.

Esaminati `valutazione-messaggi-tex.md`, `diagnostica-tex.json`, filtro
aggiornato, comandi, README 08, controllo visivo e F13 della matrice finale.
Estratta tramite AST ed eseguita soltanto l'espressione del filtro corrente
sui due log: cinque messaggi ciascuno, identici alle liste diagnostiche.
Verificate impronte dello script e dei log; log ed esiti originari
`autonomia-pacchetto.json`/`esito-documentale.json` coincidono con il freeze.
Le modifiche ai cinque input congelati riguardano esclusivamente verbali,
matrice e controllore 08; sorgenti LaTeX e artefatti finali sono invariati.

## Norme ed euristiche

La correzione rispetta `docs/agents/domain.md` (attribuire i risultati alla
versione esaminata) e `piano.md`, «Criteri di lavoro» (distinguere storico,
nuove verifiche e limiti). Il campo storico `warnings_last_pass: []` viene
contestualizzato come risultato del filtro limitato, senza riscriverlo.
Il filtro attuale conserva e stampa i messaggi per la valutazione manuale.
Non sono introdotte soppressioni nei sorgenti né dichiarate cause o
risoluzioni non dimostrate. Nessuno smell applicabile richiede interventi.

Il principale documenta la nuova osservazione delle pagine fisiche 101–106.
Ho campionato personalmente 101 e 106: intestazioni, continuazioni e righe
risultano leggibili, senza tagli o sovrapposizioni rilevati.

## Artefatti e limiti

PDF SHA-256: `e046f9efc5b3a7411354bfdde71b288e0d748f01ebea8c2aa7b3534c5a8990d4`.
ZIP SHA-256: `9b450b53f3bf623b919f81edf63f7b288728bea92818f25b8da8011f6c93837c`.

Nessuna nuova compilazione o riesecuzione del confronto sull'estrazione,
non più disponibile. I cinque messaggi restano non risolti e con causa
non dimostrata; altre versioni LaTeX e stampa richiedono valutazione.
Chiusura amministrativa e revisione degli autori restano distinte.


### Ricontrollo della chiusura amministrativa

Fonte distinta: [review-standards-chiusura.md](review-standards-chiusura.md).

15 novembre 2026. **Conforme; nessun rilievo Standards aperto.**

Verificati README della relazione e del ticket 08, piano, tracker e F16:
`Status: done` indica completamento tecnico, coerente con
`docs/agents/issue-tracker.md`. Revisione degli autori ancora da svolgere;
nessuna approvazione umana, invio o pubblicazione attribuita.

L'aggregazione conserva rapporti iniziali e ricontrolli, rettifica sugli
avvisi e limite residuo. Delta finale e input distinguono la chiusura dalla
prima review. Baseline e archivio iniziali invariati; 169 input finali
coerenti. `sha256sum --check --quiet SHA256SUMS`, eseguito dalla directory
del manifest prima di aggiungere questo rapporto, termina con exit 0.

PDF SHA-256: `e046f9efc5b3a7411354bfdde71b288e0d748f01ebea8c2aa7b3534c5a8990d4`.
ZIP SHA-256: `9b450b53f3bf623b919f81edf63f7b288728bea92818f25b8da8011f6c93837c`.

Nessuna nuova compilazione o prova applicativa. Restano cinque messaggi TeX
valutati, non risolti, e i limiti di riproduzione dichiarati.


## Spec

### Rapporto iniziale

Fonte distinta: [review-spec.md](review-spec.md).

15 novembre 2026. Contesto nuovo e indipendente; rapporto Standards 08 non
consultato. Fonte autoritativa: `specifica-ticket.md`, criteri 1–10.
Lette istruzioni richiamate, specifica generale, indice, piano, glossario,
README, note originali del corso e verifiche/revisioni pertinenti 01–07.

**Esito: un rilievo aperto; nessun ampliamento improprio del perimetro.**

1. **Valutazione dei messaggi finali incompleta.** La specifica, righe 47–48,
   richiede «Valutare gli avvisi effettivi senza sopprimerli». I log finali
   `latex-log.txt:805` e `pacchetto-latex-log.txt:801` riportano cinque
   `ignored error: Infinite glue shrinkage found in box being split`, sui
   fogli numerati 98–102. Non sono valutati in `comandi.md`, che alle righe
   95 e 119 dichiara invece assenza di avvisi. Il filtro di
   `verifica-pacchetto.py:39` non intercetta questi messaggi. Occorre
   registrarli e valutarne la portata rispetto all'osservazione delle pagine
   pertinenti; il solo elenco vuoto `warnings_last_pass` non li esclude.
   Il rilievo non dimostra un difetto visivo né richiede modifiche software.

Riscontri autonomi: diff/log Git vuoti al riferimento
`61ce18ae1d4e0458b63e970b793b0b3a634a3adf`; confine effettivo verificato
con 2.333 impronte, 104 copie iniziali e delta dei non tracciati. Sei
modifiche previste, 2.327 file intatti e 152 input congelati corrispondenti.
ZIP integro: 95 membri identici ai sorgenti, comprese 32 tavole modificabili.
PDF aperto: 107 pagine, 158 link risolti, 86 font incorporati; differenze
testuali soltanto alle pagine 31 e 77. Verificati 16 frammenti esatti,
32 coppie draw.io/PDF e il limite UC-05 su servizio/conto correnti.
Record FLS coerente: 77 input locali e 89 risorse della toolchain.

Osservate personalmente pagine fisiche 1–3, 31, 33, 37, 69, 77, 94, 107
e uno screenshot ingrandito: nessun difetto rilevato nel campione.
L'osservazione integrale resta attribuita al principale e al suo verbale.
Compilazione ed equivalenza del pacchetto valutate dalle prove conservate,
senza ripeterle; nessun Java, test applicativo, DB, harness o commit.
Restano revisione degli autori e limiti applicativi dichiarati; nessuna
approvazione umana è attribuita. Chiusura amministrativa da ricontrollare.

PDF SHA-256: `e046f9efc5b3a7411354bfdde71b288e0d748f01ebea8c2aa7b3534c5a8990d4`.
ZIP SHA-256: `9b450b53f3bf623b919f81edf63f7b288728bea92818f25b8da8011f6c93837c`.


### Ricontrollo della correzione

Fonte distinta: [review-spec-ricontrollo.md](review-spec-ricontrollo.md).

15 novembre 2026. Stesso revisore Spec indipendente; rapporto Standards non
consultato. Il rapporto iniziale `review-spec.md` resta conservato intatto.

**CONFORME: rilievo iniziale chiuso, zero rilievi Spec aperti.**

Il requisito di `specifica-ticket.md:47` («Valutare gli avvisi effettivi»)
risulta soddisfatto dalla correzione documentale:

- `valutazione-messaggi-tex.md` identifica i cinque messaggi in entrambi i
  log finali, spiega il precedente filtro incompleto e mantiene distinta
  l'assenza di difetti visivi osservati dalla causa interna non dimostrata.
  I messaggi rimangono un limite esplicito, senza dichiararli risolti.
- `comandi.md`, README 08, `controllo-visivo.md` e F13 della matrice finale
  riportano coerentemente tale limite. Il campo storico
  `warnings_last_pass: []` non è riscritto né presentato come assenza di
  ogni diagnostica. Non viene attribuita una nuova compilazione.
- Ho valutato autonomamente tramite AST l'espressione corrente di
  `verifica-pacchetto.py` sui due log: cinque rilevamenti esatti ciascuno,
  numeri stampati 98–102. Risultati e impronte di script/log coincidono con
  `diagnostica-tex.json`. Rimosso l'assert di assenza, il filtro conserva e
  stampa i messaggi per la valutazione manuale.

Ho aperto personalmente con `view_image` tutte le pagine fisiche 101–106
a 120 dpi, campionando anche il sorgente delle tabelle C.1/C.2:
intestazioni, continuazioni, righe finali e transizione alla prosa leggibili;
nessun taglio o sovrapposizione rilevato. La decisione di conservare il PDF
è motivata dai riscontri, senza sopprimere i messaggi o inventarne la causa.

Impronte ricalcolate e invariate:

PDF SHA-256: `e046f9efc5b3a7411354bfdde71b288e0d748f01ebea8c2aa7b3534c5a8990d4`.
ZIP SHA-256: `9b450b53f3bf623b919f81edf63f7b288728bea92818f25b8da8011f6c93837c`.

Non ripetuti compilazione, confronto dell'estrazione ormai assente o prove
applicative. Restano i cinque messaggi del motore, i limiti di riproduzione
e la revisione degli autori ancora da svolgere; nessuna approvazione umana.
Il successivo aggiornamento amministrativo finale resta da ricontrollare.


### Ricontrollo della chiusura amministrativa

Fonte distinta: [review-spec-chiusura.md](review-spec-chiusura.md).

15 novembre 2026. **CONFORME: zero rilievi aperti.** Indipendenza mantenuta;
rapporto dell'altro revisore di chiusura non consultato.

README, piano, tracker 08 `done`, README 08 e F16 soddisfano i criteri 9–10:
chiusura tecnica distinta dalla revisione degli autori ancora da svolgere,
senza approvazione umana, invio o pubblicazione. L'aggregazione conserva
rilievo iniziale e ricontrollo; cinque messaggi TeX restano limiti valutati.

Riscontrati delta finale sulla baseline immutata (sei modifiche, 2.327 file
conservati), 169 input finali, stato e finalizzazione. Verifica autonoma
`sha256sum --check --quiet SHA256SUMS`: exit 0.

Artefatti invariati, SHA-256:

PDF: `e046f9efc5b3a7411354bfdde71b288e0d748f01ebea8c2aa7b3534c5a8990d4`.
ZIP: `9b450b53f3bf623b919f81edf63f7b288728bea92818f25b8da8011f6c93837c`.

Non ripetute compilazioni o prove applicative; restano i limiti tecnici
dichiarati e il controllo finale degli autori.


Standard documentati: zero rilievi aperti; euristiche: zero interventi richiesti.
Spec: un rilievo iniziale sulla valutazione dei messaggi TeX, chiuso; zero rilievi aperti.

Restano i cinque messaggi TeX valutati, i limiti applicativi storici e la revisione
degli autori ancora da svolgere. Nessuna approvazione umana, invio o pubblicazione.
