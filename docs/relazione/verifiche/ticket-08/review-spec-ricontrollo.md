# Ricontrollo Spec — ticket 08

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
