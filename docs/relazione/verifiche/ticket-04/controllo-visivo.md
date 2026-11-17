# Osservazione del PDF — ticket 04

Controllo effettivo dell'agente principale il **13 novembre 2026**, con
apertura del PDF tramite PyMuPDF 2.1/MuPDF 1.28.6 e osservazione dei
rendering a 108 dpi mediante `view_image`. I rendering provengono dal
PDF compilato, non dal solo XML o da schermate dell'editor draw.io.
File: [relazione-drivehub.pdf](../../relazione-drivehub.pdf), SHA-256
`0b25ad25962fb0886e408ac23d4a97a68eafe87e8fd8533b5647897fc7441a37`.

Il documento ha 84 pagine A4. Le nuove sezioni sono alle pagine fisiche
32–53 (numerate 29–50); il capitolo 2 completo occupa le fisiche 7–53.
Le pagine renderizzate sono riproducibili con
`controlla-documento.py` in `build/visual-ticket04/pagina-NN.png`.
Sono state renderizzate tutte le pagine e osservate tutte le tredici
nuove tavole nel documento. Gli assert, la riuscita dell'export e questa
osservazione sono verifiche distinte.

| Tavola / figura | Pagina fisica | Osservazione |
| --- | --- | --- |
| SQ-01a / 2.25 | 33 | Attore, handler, servizio e DAO distinguibili; UPDATE condizionale nel principale, alternativa zero-righe esplicita; letture autonome a1/a2/a3 separate nelle note da c1; accordo/acconto leggibili. |
| SQ-01b / 2.26 | 35 | Approvazione e rifiuto mutuamente alternativi, rimborso e rimozione riconoscibili; frecce rosse e guardie leggibili. |
| SQ-01c / 2.27 | 36 | Rilettura richiesta, residuo, registro, stato e rinvio Observer in ordine; sette intestazioni separate; totale e acconto non ricalcolati. |
| SQ-01d / 2.28 | 38 | Innesco all'apertura, lettura autonoma, ciclo per candidato, skip e scadenza espliciti; penale sull'acconto, non sul totale. |
| SQ-02 / 2.29 | 40 | Date, lock veicolo/cliente, controllo periodi, addebito e rinvio Observer distinguibili; stato RENT e connessione c5 leggibili. |
| SQ-OB / 2.30 | 41 | Chiamata sincrona, DELETE futuro con CURRENT_TIMESTAMP, ritorni tratteggiati e propagazione errore leggibili; nessuna freccia asincrona. |
| SQ-03a / 2.31 | 43 | Nome reale buildSellMyCarTab visibile; VIN noto/nuovo, controlli e letture autonome distinguibili; proposta senza pagamento. |
| SQ-03b / 2.32 | 44 | Accettazione e rifiuto riconoscibili, stima persistita e registro PURCHASE espliciti; rifiuto con/senza storico e limite importo zero leggibili. |
| ER-01 / 2.33 | 46 | Rettangoli, rombi, estremi 1/0..1/0..* separati; ripetizioni e rinvii ER-02 spiegati, sale→vehicle opzionale. |
| ER-02 / 2.34 | 47 | Cardinalità cliente/conto/operazioni/venditore leggibili; manager isolato e user come vista dichiarati; nessun arco intrecciato. |
| REL-01 / 2.35 | 49 | Cinque tabelle del catalogo, attributi/chiavi/tipi e note DELETE/UPDATE/trigger completi; targa nullable distinta da CK. |
| REL-02 / 2.36 | 50 | Tre ruoli, conto e vista distinti; sequenza comune, nullabilità, email e limiti dei trigger leggibili. |
| REL-03 / 2.37 | 51 | Quattro tabelle operative, accordo, intervallo EXCLUDE e riferimenti storici leggibili; nessuna riga tagliata. |

Tutte le esportazioni contengono un'intera tavola su una sola pagina;
nessuna suddivisione accidentale o etichetta troncata osservata.
Frecce, cardinalità, simboli, didascalie e numeri delle figure restano
distinguibili nella pagina A4. Le tavole sono vettoriali: lo zoom consente
di approfondire le note tecniche più fitte senza perdita di definizione.
La leggibilità non è dedotta dal solo valore numerico dei font.

Osservati anche frontespizio, indice alle pagine fisiche 2–3, apertura
2.7, trattazione finale degli enum/vincoli/utenti e stato dell'appendice C.
Indice, titoli e ordine 2.7/2.8 e delle sei sottosezioni sono corretti;
didascalie 2.25–2.37 e rimandi interni risolti. Le espressioni SQL con
spazi e le virgolette angolari nei nuovi testi hanno ora resa corretta.
I capitoli 3–5 e C mantengono i segnaposto Provvisorio; il frontespizio
propone capitoli 1–2 e A–B per revisione, senza dichiarare approvazione.

La conservazione del capitolo 1 e di 2.1–2.6/A–B è verificata anche con
hash dei sorgenti/figure e confronto del testo PDF con gli estratti
storici, esclusi avviso di stato e numeri di pagina. Non si attribuisce
all'osservazione delle nuove tavole un nuovo collaudo dell'applicazione.

## Correzioni e limiti del controllo

Le prime viste ER avevano archi/cardinalità sovrapposti: disposte per
relazione, senza alterare entità o significato. Ingranditi i caratteri
delle sequenze e corrette spaziature e intestazioni; la riduzione a128px
separa i bordi anche con sette partecipanti. Riesportate le sole tavole
interessate e riosservate dopo la compilazione.

I rilievi preliminari su spazi SQL e nome dell'handler sono corretti e
verificati nel PDF aggiornato. Corretta infine la resa T1 dei guillemets
nei soli due nuovi testi e riosservate le pagine32/52; le tredici tavole
sono rimaste invariate in quest'ultimo passaggio.

La compilazione finale e lo script di controllo sono conclusi con exit0;
rimangono 96 avvisi noti di Tectonic sulla lettura dei PDF1.7 con
impostazione interna1.5. Il documento prodotto è PDF1.7: nessun riferimento
irrisolto, carattere mancante o box difettoso nel log finale. Nessuna
verifica su stampante, seconda distribuzione LaTeX o altro sistema
operativo; nessuna prova applicativa o certificazione di accessibilità.
La revisione degli autori resta un passaggio distinto.
