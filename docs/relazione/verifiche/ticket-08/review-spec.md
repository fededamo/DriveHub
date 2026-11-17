# Review Spec — ticket 08

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
