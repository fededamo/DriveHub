# Follow-up indipendente Spec — ticket17

**P2 risolto; zero rilievi Spec residui individuati.** Conservato il rapporto
iniziale `spec-review.md`, riferito alla patch congelata precedente.

Verificata la correzione `review-correction.patch`, SHA256
`cc93bd7778f40c56e2e55022e05d2ebda70fecaabb3bec69fa0b86fde29d5abc`,
e i quattro file fissati da `review-correction-files.sha256.json`.
Il manifest iniziale dei130file, applicando soltanto questi quattro nuovi
hash, corrisponde ai file correnti senza discrepanze.

Il nuovo hunk `@@ -14,23 +14,28 @@` di
`docs/diagrams/current/page-navigation-customer.puml` elimina entrambi i
self-loop contestati. Le righe22–23 riportano il test drive alla tab chiamante
Vendita/Noleggio; la riga30 riporta la ricarica a Vendita nel percorso
esemplificato. Le righe34–38 distinguono annullamento, conferma e comando
successivo alla chiusura, anche in errore, precisando che la ricarica è
disponibile da tutte le tab e ne conserva la selezione.

Questa rappresentazione corrisponde a
`ClientDashboardView.java` righe165–174 e344–355: comando ed Alert sono
elaborati dopo `showAndWait`, senza selezionare un'altra tab. Risolto quindi
il disallineamento rispetto a ticket17 riga18, AC14 riga163 e R09 righe324–327.

Ispezionato direttamente il nuovo PNG: ritorni espliciti e note leggibili.
Hash di PUML, SVG e PNG coincidono con `render-inventory.json`, che registra
entrambi i render exit0; i comandi di correzione sono conservati in `render.log`.
Ricontrollati i93input runtime originali: nessuna modifica. La correzione
documentale non richiede una nuova esecuzione della suite.

Limiti: controllo statico e visivo della correzione, senza rieseguire
render/suite/DB o certificare tutti i gesti GUI. Relazione sempre esclusa.
Nessun nuovo rischio applicativo individuato; restano i limiti delle prove
documentati in `verification.md`.
