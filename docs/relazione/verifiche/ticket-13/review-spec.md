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
