# Controllo visivo — 16 novembre 2026

Osservati i rendering del PDF finale ricompilato, con PyMuPDF a 108 dpi.
Le pagine sono identificate con numero fisico (da 1) ed etichetta stampata.
Non si deduce la qualità visiva dal solo successo della compilazione.

| Pagine | Oggetto osservato | Esito |
| --- | --- | --- |
| 2 / i | Prima pagina dell'indice | Introduzione a p. 1; 1.1 a p. 1, 1.2 a p. 2, 1.3 a p. 3. Titoli, rientri, puntinature e numeri leggibili. Nessuna variazione rispetto alla baseline. |
| 3 / ii | Seconda pagina dell'indice | Prosecuzione regolare fino alle appendici. Nessuna variazione indotta dal ticket; i capitoli successivi mantengono la struttura della prima stesura, da riallineare nei propri ticket. |
| 4 / 1 | Introduzione e Descrizione | Contesto, punto di vista della concessionaria, tre ruoli ed esempio sulla disponibilità leggibili; elenco completo nella pagina, margini rispettati, nessuna sovrapposizione o troncatura. |
| 5 / 2 | Stack tecnologico | Cinque punti e paragrafo sulla composizione leggibili. Java, JavaFX, JDBC/PostgreSQL, Maven e JUnit distinti; corretti caratteri e versioni. Lo spazio inferiore deriva dalla separazione di sezione già presente. |
| 6 / 3 | Semplificazioni adottate | Tutti i cinque punti e la chiusura leggibili. Ambiente di esecuzione prima dei processi aziendali, come richiesto dal confronto; nessun testo perso o spostato su una quarta pagina. |
| 107 / 104 | Appendice C, inizio tabella C.1 | Titolo, intestazione, tre righe dei casi e rinvio di continuazione leggibili; messaggio di glue shrinkage residuo. |
| 108 / 105 | Tabella C.1, continuazione | Intestazione ripetuta, righe e richiamo della pagina seguente leggibili; messaggio residuo. |
| 109 / 106 | Tabella C.1, continuazione | Controllati anche intestazione e bordo superiore con rendering di dettaglio: nessuna troncatura. Le colonne e il richiamo di continuazione rimangono leggibili; messaggio residuo. |
| 110 / 107 | Tabella C.1, continuazione | Righe, intestazione e piè di pagina leggibili; nessun effetto del riordino del capitolo 1. Messaggio residuo. |
| 111 / 108 | Fine C.1 e avvio C.3 / C.2 | Ultima riga della matrice, nuovo titolo, didascalia e righe della seconda tabella leggibili; messaggio residuo. |

Il PDF mantiene A4 nel capitolo 1 e nell'indice; le pagine A3 delle tavole
esistenti restano immutate. L'estratto contiene esattamente le tre pagine
del capitolo con numerazione stampata 1–3 e segnalibri dedicati.

Il log finale `latex-log.txt` conserva cinque messaggi identici per tipo a
quelli già documentati nella prima stesura. Il loro numero tra parentesi è
la pagina stampata, non quella fisica: 104–108 corrisponde a 107–111.
Nessun warning di riferimento irrisolto o di box pertinente al capitolo 1.
Non sono stati corretti gli altri capitoli o le tabelle fuori perimetro.

Il confronto di tutte le 113 pagine in `confronto-pdf.json` attesta identità
del testo e del rendering per le 112 pagine diverse dalla 6; non equivale a
una nuova revisione semantica o visiva integrale degli altri capitoli.
