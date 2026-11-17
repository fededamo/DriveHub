# Review finale del ticket 13

17 novembre 2026. Due subagenti con contesto iniziale separato hanno recensito il documento integrato, il PDF conclusivo, lo ZIP e le prove di autonomia. Punto fisso: baseline iniziale 13, inclusi non tracciati e nuove aggiunte. I giudizi sono riportati separatamente, senza fusione o graduatoria tra assi. Nessuna vecchia review è usata come review13.

## Standards

# Review Standards — ticket 13

17 novembre 2026. Review indipendente in contesto separato; nessuna lettura
della review Spec 13 né riuso delle vecchie review come giudizio corrente.

**Esito: nessun rilievo.** Nessuna violazione documentata rilevata rispetto
a `docs/AGENTS.md`, `docs/agents/{domain,issue-tracker,triage-labels}.md` e
ai requisiti globali della richiesta operativa. Letti glossario, ADR 0001/0002,
piano, indice, matrice, specifica, ticket e istruzioni di consegna.

**Verifiche effettive:**

- Risolto HEAD `61ce18ae1d4e0458b63e970b793b0b3a634a3adf`; diff e log Git
  richiesti vuoti. Confrontati invece i 2.899 file della baseline e tutte
  le 227 copie nel tar con le impronte: otto modifiche documentali,
  2.891 file conservati, nessuna cancellazione. Confermato il delta al corpo
  limitato a `capitoli/5_uso_ai.tex`; originali e storici intatti.
- Esaminati tutti i cinque nuovi checker, i loro assert e gli esiti conservati.
  Riscontrati 24 input originali/15 correnti, 102 corrispondenze con intervalli,
  23 funzioni originali, 42 estratti/comandi sulle fonti reali, 281 fonti,
  78 fonti storiche e 28 intervalli aggiornati. Letture mirate di servizi,
  transazioni, schema e assert confermano la distinzione S0/E1/E2/E3 e
  l'attribuzione alla versione esaminata richiesta da `domain.md`.
- Verificati indipendentemente i 43 nativi/export e gli allegati/XML incorporati;
  ZIP integro, 118 membri uguali a manifest e sorgenti correnti. Recorder
  dell'estrazione: 73 input editoriali interni, sole risorse TeX esterne.
- Letti log e stabilizzazione delle compilazioni reali. Confrontati in memoria
  tutti i PDF: 151 pagine uguali per testo/formato/pixel a 96 dpi fra consegna
  e ricompilazione; rispetto alla baseline cambiano solo 119–121. Tutti i
  151 PNG conservati corrispondono al PDF corrente a 108 dpi.
- Osservati direttamente capitolo 5, tutte le dodici A3 e pagine 145–149:
  nessun difetto materiale rilevato. I cinque messaggi TeX sono presenti anche
  in baseline e ZIP, espliciti e non dichiarati risolti.

**Euristiche Fowler:** nessuno smell utile da segnalare; nessuna richiesta di
refactoring applicativo. Gli stati amministrativi aperti precedono, correttamente,
la chiusura delle due review.

SHA-256 recensiti:

- PDF: `79536e7bd909b03e433d16c5b2fccd0f18269e432436463e30e75cb40a954b5e`
- ZIP: `4264917500906131dabb95b21f402b89c339636e70fb74dd53c02c017b0658ff`
- PDF da ZIP: `8e97404c1885f720d6e60baf66b6139a2ce24ec65719e707bd75b5e9ba14f7d0`

Limiti: nessuna rigenerazione o esecuzione applicativa; osservazione indipendente
mirata, verbale integrale letto. Stampa, altri sistemi e accessibilità assistiva
non verificati; restano i cinque messaggi TeX dichiarati.

## Spec

# Review Spec — ticket 13

17 novembre 2026. Review indipendente sulla baseline di 2.899 file e sul
tar editoriale iniziale, HEAD `61ce18ae1d4e0458b63e970b793b0b3a634a3adf`.
Delta Git vuoto: valutati delta documentale e aggiunte 13, come prescritto
da `richiesta-operativa.md`. Non letto il rapporto Standards 13.

**Esito: conforme nel perimetro autorizzato. Nessun requisito mancante o
parziale, scope creep o implementazione apparentemente errata rilevato.**

Letti integralmente piano, indice, matrice, specifica, ticket 09–13 e
istruzioni pertinenti; consultati GLOSSARY e ADR 0001/0002. Criteri:
ticket 13, «Acceptance criteria», e specifica, «Testing Decisions».

Verifiche effettive, senza eseguire checker storici:

- Espansione indipendente dei 24 input originali e 15 correnti: tutte le
  102 voci coincidono per livello, posizione e ordine. Confrontati contenuti,
  adattamenti dei 23 frammenti, diciotto funzioni UI e due tabelle; verificati
  quaranta estratti letterali e due blocchi di comandi. Sei schede complete
  in 2.2, UC-05 prima di UC-04; A senza duplicati, B con undici package,
  C con assert, associazioni selettive e lacune esplicite.
- Baseline: nessun file mancante, soltanto gli otto cambiamenti dichiarati;
  227 membri del tar coerenti. Nativi/export delle 43 coppie coerenti,
  compresi undici allegati e 32 Subject XML; rifacimenti 10 conservati.
- Lette preparazione, chiamate, transazioni, assert e riletture decisivi.
  Riscontrati log E1 e ricalcolati XML E2; sette marcatori E3 verificati.
  S0, fallimenti, incompatibilità e risultati storici restano distinti.
  Capitolo 5 conserva attribuzioni e limiti senza approvazioni presunte.
- Confronto indipendente di tutte le pagine a 96 dpi: 151 nel PDF finale;
  soltanto 119–121 differiscono dalla baseline. ZIP ricompilato uguale per
  testo, formato e pixel. Osservate direttamente dodici A3, 119–121 e
  145–149; letto il verbale delle 151 pagine. Cinque messaggi TeX persistono,
  senza difetto visivo rilevato; 238 collegamenti interni risolti.
- ZIP: CRC, 118 membri, manifest e byte correnti verificati; recorder con
  73 input editoriali interni. Tre log reali terminano con exit 0 e indice
  stabile. Differenze binarie PDF attribuite separatamente a metadati,
  subset font e relativi offset.

SHA-256 PDF: `79536e7bd909b03e433d16c5b2fccd0f18269e432436463e30e75cb40a954b5e`.
SHA-256 ZIP: `4264917500906131dabb95b21f402b89c339636e70fb74dd53c02c017b0658ff`.

Non ripetute compilazioni o prove applicative; artefatti non rigenerati.
Restano revisione degli autori e limiti dichiarati; chiusura degli stati da ricontrollare.

## Chiusura e ricontrollo degli stati

Standards: 0 rilievi; Spec: 0 rilievi. Nessuna correzione al candidato PDF/ZIP è stata richiesta. Le precisazioni editoriali su fonti e rinvii non cambiano gli input distribuiti. I ricontrolli amministrativi separati sono in [Standards](review-standards-stati.md) e [Spec](review-spec-stati.md). Le impronte definitive e la verifica dello ZIP ancora uguale a quello ricompilato sono in [finalizzazione](finalizzazione.txt) e [artefatti](artefatti-finali.json). Chiusura tecnica e review assistite non attestano approvazione degli autori.
