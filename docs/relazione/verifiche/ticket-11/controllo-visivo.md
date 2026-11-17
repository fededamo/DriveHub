# Controllo visivo — ticket 11

16 novembre 2026. PDF completo realmente ricompilato: **143 pagine**;
estratto del capitolo 3: **23 pagine**. Numeri sotto sempre fisici, contando
frontespizio e tre pagine di indice; il numero stampato è inferiore di quattro.
Nessuna appendice ha ricevuto modifiche di contenuto in questo ticket.

Osservate tutte le pagine **83–105** del capitolo 3, **2–4** dell'indice
e **135–143** di C nei nove [provini](provini/) ottenuti dai
[rendering individuali a 108 dpi](pagine/). [Elenco](pagine.json).
Riaperte singolarmente 92/93 per formule ed esempio numerico, e 102 per
la tabella delle garanzie. Dopo la correzione dell'esempio, rigenerati i
rendering e riaperta la pagina 93 conclusiva.

| Pagine fisiche | Osservazione effettiva |
| --- | --- |
| 2–4 | Indice su tre pagine; cinque sezioni e venticinque sottosezioni del capitolo 3 fino a 3.5.6. Le quattro sottosottosezioni sono numerate nel corpo, senza essere aggiunte al livello dell'indice. |
| 83–86 | Apertura, cinque gruppi del dominio, distinzione package event, invarianti e factory; nessun titolo isolato, listati e didascalie leggibili e senza tagli. |
| 87–88 | Metodo transazionale e due callback distinti, causa/rollback/close; responsabilità degli otto servizi e avvio dei flussi. Metodo e didascalia restano sulla stessa pagina. |
| 89–91 | Test drive, query di overlap con ordine parametri, noleggio e vendita al cliente: righe dei listati complete, quattro livelli numerati, rinvii risolti. |
| 92–94 | Cessione, base selezionata, formula targa/anni/km, acconto, somma geometrica ed esempi; tabella 3.1 interamente su pagina 93. Prezzo/stima finale 25.333,87 e noleggio 57,05 leggibili; nessun simbolo mancante. |
| 95–97 | Observer, viste e navigazione, App e dispatch Client, AsyncLoad ed errori/Riprova; nessun listato separato dalla didascalia e nessuna sovrapposizione fra titoli e testo. |
| 98–100 | Firme UserDao, UPDATE condizionale, switch utente, mapper Request/Sale; numeri dei sorgenti e parti opzionali leggibili. Gli estratti che prima omettevano la graffa di chiusura ora la mostrano. |
| 101–102 | Enum/cast, tre intervalli SQL separati, tabella 3.2 interamente a inizio pagina 102 e overload Connection. Tabella completa, senza righe spezzate o tagli; il testo riprende sotto il float. |
| 103–105 | Sei temi dei pattern nello stesso ordine, contratto Observer, factory e Singleton; finale 3.5.6 presente. Il bianco residuo dell'ultima pagina precede il nuovo capitolo, senza testo omesso. |
| 135–143 | Appendice C conservata: intestazioni ripetute, righe, continuazioni e piè di tabella completi; nessuna sovrapposizione visibile nelle pagine interessate dai cinque messaggi storici. |

Tutti i 27 estratti e le rispettive didascalie sono sulla stessa pagina;
il confronto lessicale con i sorgenti verifica anche che il PDF non ne
ometta righe. Le formule e le due tabelle recuperano la funzione espositiva
originale, con valori correnti. Nessuna figura è stata adattata o riesportata.

## Messaggi TeX nella nuova composizione

L'ultimo passaggio `compilazione-review-3.txt` registra **cinque** messaggi
preesistenti `Infinite glue shrinkage found in box being split`, sulle
pagine stampate 133–137, fisiche **137–141**, tutte in C. Nell'intero C
osservato non si rilevano tagli o sovrapposizioni. Non vengono dichiarati
eliminati né attribuiti al ticket 11: il sorgente di C è identico alla baseline.

Zero Overfull, Underfull, caratteri mancanti, riferimenti indefiniti,
richieste di rerun o labelwidth negativi nel log conclusivo. Il messaggio
aggiuntivo sorto durante la composizione della nuova tabella è registrato
nei tentativi intermedi e non compare dopo la ricomposizione su una pagina.

Capitoli 1/2: identità del testo e dei pixel verificata su tutte le 78 pagine
rispetto al PDF iniziale, con solo spostamento fisico di una pagina per
l'indice più lungo. Tutti i loro file e tutte le tavole sono immutati.
La chiusura tecnica e questo controllo assistito non sono approvazione degli autori.

## Ricontrollo del 17 novembre

Riosservati tutti i nove provini aggiornati: indice 2–4, capitolo 3
83–105 e C 135–143. Le precisazioni sull'identità Model/Vehicle e i
parametri PBKDF2/configurazione sono presenti e leggibili. Dopo il solo
ripristino del contatore a fine capitolo, la compilazione conclusiva
conserva testo e pixel su tutte le 143 pagine rispetto al candidato
osservato; restano validi anche i rendering aperti. Il nuovo log
`compilazione-review-3.txt` conferma gli stessi cinque messaggi in C,
con zero Overfull/Underfull o riferimenti irrisolti.

## Correzioni della review e PDF conclusivo

Riosservate singolarmente le pagine83 e99 dopo la nuova compilazione
`compilazione-review-1..3.txt`. Pagina83 descrive ora AbstractUser come
classe astratta con costruttore protetto; il paragrafo resta leggibile
senza incidere sulla gerarchia o sui margini. Pagina99 recupera la
traduzione di SQLSTATE23503 e la distinzione degli errori DAO; il testo
aggiunto rientra nella pagina, senza spostare mapper o didascalie. Il
confronto delle altre141 pagine dà identità dei pixel al candidato già
osservato; tutte le23 pagine di capitolo3 e le accessorie risultano quindi
osservate nella composizione conclusiva. Cinque messaggi TeX di C ancora
presenti, nessun nuovo box difettoso o rinvio irrisolto.
