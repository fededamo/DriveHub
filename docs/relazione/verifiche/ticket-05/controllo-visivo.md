# Controllo visivo — ticket 05

Osservazione del 13 novembre 2026. PDF distribuito: 93 pagine A4,
SHA-256 `2713ae306c1bc3f748ecddbfbdc0771e0b401119a8cd41af005ebd7856b742bb`.
Renderizzazioni prodotte con PyMuPDF a 100,8 dpi in
`docs/relazione/build/visual-ticket05/`; aperte con view_image.
Non sono schermate dell'applicazione e gli assert non sono presentati
come prova visiva.

- Frontespizio e indice (pagine fisiche 1–3): stato capitoli 1–3/A–B per
  revisione; cinque sezioni del capitolo 3 nell'ordine concordato, titolo
  3.4 intero e numeri coerenti. Nessuna approvazione degli autori attribuita.
- Capitolo 3 (54–63): osservati testo, tutti i tredici frammenti e le loro
  didascalie; numeri di riga, rientri, stringhe, operatori e commenti
  leggibili. I ritorni a capo delle righe lunghe non tagliano il codice;
  i frammenti restano interi con la didascalia. La tabella dei quattro
  cast enum è leggibile, i titoli non si sovrappongono al testo.
- Riferimenti visibili: B/2.7/2.8 nell'apertura, 2.30 per Observer,
  2.36/2.37 per il modello relazionale, 2.22 e 3.6/3.8/3.13 in 3.5.
  Le destinazioni interne sono controllate anche dal verificatore PDF.
- Confini: pagina 53 conclude il capitolo 2 conservato; 64–65 e 93
  mostrano ancora i provvisori 4–5/C. Nessuna modifica a quelle sezioni.

Correzioni dopo la prima osservazione: frammenti in blocchi indivisibili,
virgolette letterali con upquote, intervalli senza righe estranee,
3.5 sintetizzata per conservare spiegazione e frammento Singleton su una
pagina. Riosservate pagina 63 dopo la sintesi e 54–55 dopo la precisazione
sugli ID finali di Request. Riosservate infine tutte le pagine 54–63 della versione definitiva
identificata dall'impronta sopra riportata.

Non osservate manualmente tutte le 93 pagine: i contenuti precedenti sono
protetti da hash e dal confronto testuale; ogni pagina è stata aperta e
renderizzata dal controllo. Nessun nuovo diagramma, quindi nessuna nuova
esportazione draw.io da qualificare come prova. I soli avvisi finali sono
quelli noti di versione delle figure PDF, non difetti visivi osservati.
