# Review Standards — ticket 10

16 novembre 2026. Revisione indipendente, limitata all'asse Standards.
Punto fisso: working tree iniziale di `baseline.json`, 3.053 file,
HEAD `61ce18ae1d4e0458b63e970b793b0b3a634a3adf`. Il diff tra commit è
vuoto; sono stati esaminati il delta documentale dalla baseline e i nuovi
materiali del ticket, inclusi script, nativi, esportazioni ed evidenze.

## Regole applicate

- `docs/AGENTS.md:3`: piano e indice come riferimenti editoriali;
  `docs/relazione/piano.md:8`: materiali nell'area della relazione;
  righe 15–16: intervento soltanto documentale.
- `docs/agents/domain.md:7` e `:17`: glossario, ADR pertinenti,
  riscontro corrente e attribuzione delle verifiche alla versione esaminata.
  Consultati GLOSSARY, ADR 0001–0002, README, piano, indice e matrice.
- `docs/relazione/allineamento.md:29`: skill drawio, conservazione dei
  nativi e degli export; `:32`: basi degli autori; `:47`: verifica semantica,
  esportazione e osservazione del PDF documentate per ogni tavola.
- `docs/agents/issue-tracker.md:14`: chiusura dopo verifica dei criteri;
  `docs/relazione/LEGGIMI-consegna.md:5`: revisione degli autori ancora
  necessaria. Consultate anche le etichette triage. Nessun
  `CODING_STANDARDS.md` o `CONTRIBUTING.md` reperito; assente
  `docs/adr/README.md`, senza introdurre convenzioni sostitutive.

## Riscontri e rilievi

**Nessuna violazione documentata rilevata.** Il controllo indipendente
in sola lettura trova zero file iniziali mancanti e 18 modificati, tutti
nell'area editoriale autorizzata: originali, capitolo 1, software, schema,
dati e vecchie evidenze conservati. Archivio iniziale di 210 copie con
SHA-256 coerente; corrispondono anche gli hash delle undici coppie native/PDF.

`tavole.md`, `confronto.md` e `controllo-visivo.md` registrano basi,
adattamenti e limiti; `comandi.md` distingue tentativi, compilazione finale
e verifiche documentali dalle prove applicative storiche. La review precede
l'aggiornamento degli stati: nessuna falsa approvazione degli autori.
Gli ZIP e le istruzioni precedenti restano esplicitamente storici.

Euristica Fowler, distinta dagli standard: **nessun code smell concreto
da segnalare**, proporzionando il giudizio agli script documentali locali.
Sintassi dei quattro Python verificata mediante parsing AST; nessun
formatter prescritto, nessuna astrazione ulteriore necessaria.

**Sintesi: 0 rilievi Standards, 0 criticità aperte su questo asse.**

Non rieseguiti esportazione, compilazione, checker che scrive report o test
applicativi; esaminate le evidenze finali disponibili. Restano i cinque
messaggi TeX preesistenti in C, dichiarati e valutati nel verbale visivo.
