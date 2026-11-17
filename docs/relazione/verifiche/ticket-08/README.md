# Ticket 08 — Verifica finale e pacchetto di consegna

Status: done — completamento tecnico; revisione degli autori ancora da svolgere.

Lavorazione e compilazione: 14 novembre 2026; chiusura e review: 15 novembre 2026.
HEAD invariato:
`61ce18ae1d4e0458b63e970b793b0b3a634a3adf`.
[Specifica operativa ricevuta](specifica-ticket.md), indipendente da /tmp
e dai ticket precedentemente pubblicati.

## Pacchetto proposto agli autori

- [PDF completo, 107 pagine](../../relazione-drivehub.pdf).
- [ZIP dei sorgenti LaTeX e delle figure](sorgenti-relazione.zip), con 32
  tavole draw.io modificabili e istruzioni autonome: [LEGGIMI](../../LEGGIMI-consegna.md).
- [Matrice finale criteri → parti → riscontri → esiti/lacune](matrice-finale.md).
- [Osservazione di tutte le 107 pagine](controllo-visivo.md), con ingrandimenti
  dei 15 screenshot inclusi.
- [Comandi e valutazione degli avvisi](comandi.md), [ambiente](ambiente.txt),
  [log finale TeX](latex-log.txt) e [risultato documentale](esito-documentale.json).
- [Autonomia dello ZIP estratto](autonomia-pacchetto.json),
  [registro degli input](pacchetto-input.fls) e [log separato](pacchetto-latex-log.txt).
- [Review indipendenti Standards e Spec](revisione.md), con rapporti iniziali
  distinti, correzione dell'unico rilievo Spec e ricontrolli: nessun rilievo aperto.
- [Ripresa e controllo conclusivo](ripresa-2026-11-15.md),
  [delta finale](delta-files-finale.json), [input finali](input-review-finale.json)
  e [impronte di consegna](SHA256SUMS).

PDF SHA-256:
`e046f9efc5b3a7411354bfdde71b288e0d748f01ebea8c2aa7b3534c5a8990d4`.
ZIP SHA-256:
`9b450b53f3bf623b919f81edf63f7b288728bea92818f25b8da8011f6c93837c`.

Il pacchetto è completo tecnicamente e proposto agli autori per la revisione
finale di capitoli 1–5 e appendici A–C. **La loro revisione resta da svolgere:**
nessuna approvazione, invio al docente o pubblicazione è attribuita.

## Esito tecnico e correzioni

Compilazione reale con XeLaTeX/TeX Live 2025 predisposto soltanto in /tmp;
107 pagine, 158 collegamenti interni risolti e 86 font incorporati. Nessun
Warning/Underfull/Overfull, carattere mancante o riferimento indefinito
nel passaggio finale. Restano cinque messaggi `ignored error: Infinite glue
shrinkage`, [valutati dopo il rilievo Spec](valutazione-messaggi-tex.md):
nessun difetto visivo rilevato nella nuova osservazione delle pagine 101–106;
il messaggio del motore rimane un limite dichiarato. Gli avvisi iniziali e
i tentativi interrotti sono conservati e discussi nei comandi.
Tectonic non disponibile e download
GitHub fallito; non assunto funzionante dai vecchi verbali.

Tutte le pagine sono state osservate dal principale dopo le correzioni,
a 120 dpi; screenshot anche a 220 dpi. La compilazione separata dello ZIP
finale estratto produce tutte le 107 pagine identiche nel testo e nei pixel
a 96 dpi; tutti gli input editoriali sono nell'estrazione. Prerequisiti
di toolchain, font e limiti di riproduzione sono nelle istruzioni incluse.

Conservati struttura e contenuti corretti. Tre correzioni ai sorgenti:
rinvio del capitolo 2 al capitolo 3 completo, rimozione della macro provvisoria
inutilizzata e precisazione del limite zero in UC-05. Le tre fasi
proposta/stima, accredito ed esito persistito risultano così coerenti con
il corpo e C. Nessuna nuova tavola o rigenerazione di figure.
Riscontrati nuovamente 16 frammenti e le 32 coppie XML/PDF.

## Delta e conservazione

[Baseline](baseline-conservazione.json), [stato iniziale](stato-iniziale.txt)
e [104 copie editoriali iniziali](baseline-editoriale.tar.gz) acquisiti prima
delle modifiche. Il PDF iniziale aveva effettivamente 107 pagine; il nuovo
conteggio non è stato imposto. Il confronto effettivo usa impronte e copie,
inclusi i non tracciati: il diff Git vuoto non basta.

Sei file preesistenti cambiati su 2.333: main, 2_architettura, a_casi_uso,
README, piano e PDF. Gli altri 2.327 sono intatti. Nuovi materiali soltanto
nelle verifiche 08, nel ticket locale 08 e nelle istruzioni di consegna.
Vecchia relazione, software/test/SQL/dati, evidenze e manifest 01–07
preservati; nessun vecchio verificatore eseguito sul documento aggiornato.
I file `delta-documentale.diff`, `delta-files.json` e `input-review.json`
conservano lo stato presentato alle review iniziali. `delta-documentale-finale.diff`,
`delta-files-finale.json` e `input-review-finale.json` includono correzione del
verbale e chiusura amministrativa. La baseline iniziale non è rigenerata.
Conservazione e manifest finali identificano gli artefatti revisionati.

## Limiti e prossimo passo

Le segnalazioni applicative già documentate nei ticket 04–06 restano tali:
movimenti zero, unicità email non globale, errore di refresh dopo commit e
incompatibilità degli harness storici. Nessuna nuova prova o correzione
applicativa; E1/E2/E3 rimangono attribuiti a versioni/date/ambienti originali.
Prompt/modelli/tempi/percentuali AI e modalità IDE non sono ricostruiti.

Non eseguiti Java, suite, DB, harness, commit o esportazioni nuove.
Restano cinque messaggi TeX sulla divisione dei riquadri, valutati con nuova
osservazione delle pagine interessate; nessun difetto visivo rilevato.
Non verificati Windows/macOS, stampa, seconda versione del motore,
screen reader o accessibilità PDF generale. Toolchain e cache sono
prerequisiti, non contenuti dello ZIP; i riferimenti tecnici nel PDF
richiedono il repository per consultare le fonti, non per compilare.
Il passo successivo è la revisione degli autori sul pacchetto identificato.
