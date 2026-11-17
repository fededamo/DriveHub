# Controllo visivo — ticket 10

16 novembre 2026. Osservazione del PDF compilato realmente, non soltanto
delle figure autonome. PDF completo: 129 pagine; estratto: 96 pagine.
I numeri seguenti sono **pagine fisiche del PDF**, comprensive di frontespizio
e indice; il numero stampato nel corpo è inferiore di tre.

## Copertura osservata

Tutte le pagine 7–81 (capitolo 2) e 100–120 (appendici A–B), mediante
i [24 provini](provini/) e i [rendering individuali](pagine/) a 108 dpi.
L'elenco completo, con indice PDF, è in [pagine.json](pagine.json).
I provini servono a osservare impaginazione e sequenza; le tavole corrette
sono state riaperte anche come pagina singola. Le figure sono vettoriali
nel PDF, salvo gli screenshot originali già acquisiti nel ticket 02.

| Pagine fisiche | Osservazione |
| --- | --- |
| 7–11 | Apertura, casi d'uso d'insieme e dettagli per ruolo: attori, ellissi, rinvii e legende riconoscibili, senza tagli. |
| 12–19 | Sei schede complete, una pagina ciascuna, nell'ordine UC-01/02/03/05/04/06; flussi, alternative e riscontri leggibili. |
| 20–34 | Diciotto funzioni UI e sedici screenshot: conservate proporzioni e contenuti; didascalie distinguono tab, dialoghi e viste condivise. Welcome e panoramica prenotazioni assenti esplicitamente. |
| 35–39 | Navigazione: tavola d'insieme A3 e tre dettagli; dispatch dal ruolo autenticato, nessuna schermata di scelta del ruolo. |
| 40–42 | Package: quattro aree degli autori, undici package correnti e dipendenze selezionate; testo non coperto dagli archi. |
| 43–49 | Quattro class diagram d'insieme e spiegazioni; compartimenti completi, connettori all'esterno dei membri, colori e triangoli UML riconoscibili. |
| 50–68 | Tre sequenze principali e otto dettagli: partecipanti, fasi, connessioni, chiamate e note visibili; accordo persistito e transazioni distinte. |
| 69–81 | ER su due fogli A3, due dettagli ER, tre tavole relazionali e tredici relazioni testuali; chiavi, nullabilità e legende leggibili. |
| 100–107 | Appendice A: nove schede aggiuntive, senza duplicare le sei del corpo; nessuna riga troncata ai cambi pagina. |
| 108–120 | Appendice B: introduzione e dodici tavole di dettaglio, con undici package; titoli, membri, note e didascalie completi. |

Le undici corrispondenze, con base, adattamenti, fonti e collegamenti a ogni
pagina, sono nel [registro delle tavole](tavole.md). I fogli A3 osservati
sono 8, 36, 41, 43, 45, 47, 49, 51, 60, 65 e 70–71: dodici pagine per
undici diagrammi, poiché l'ER conserva la seconda pagina di note.
La stampa delle tavole va mantenuta in A3; un'anteprima ridotta non ne
conserva le dimensioni dei caratteri. Non sono stati ingranditi artificialmente
gli screenshot né sostituiti con immagini storiche.

## Correzioni verificate

Nel controllo iniziale i class diagram mostravano alcuni archi sopra i
membri. Corrette con la skill drawio geometrie, punti di raccordo e dimensioni
dei riquadri; eliminati archi ridondanti quando il riferimento è già esplicito
negli attributi o nella nota. Corrette anche le callback annidate in
`TransactionConsumer` e `TransactionFunction<T>`, la visibilità protetta di
`AbstractUser` e i nomi `startDate/endDate`. Riesportati tutti gli undici
nativi e ricompilata l'intera relazione. Riaperte nel PDF finale le quattro
pagine 43/45/47/49: nessun membro coperto, fuori riquadro o tagliato.
Riosservata pagina 35 dopo la correzione di «scelta del ruolo» in
«smistamento per ruolo».

Dopo la review Spec, ulteriori tre correzioni: due callback distinte nella
tavola DAO; chiamata `estimateVehicleValue` al calcolatore nella cessione;
instradamento delle dipendenze del package fuori da `domain.user`,
`dao.postgres` e dagli altri package intermedi. Riesportate le undici tavole,
ricompilato il PDF in tre passaggi e rigenerati estratto e rendering.
Riaperte le pagine finali 41, 47 e 65: nomi e firme delle callback separati,
freccia al metodo corretto e connettori senza attraversare i nomi dei package
estranei. I tre rilievi iniziali restano nel rapporto Spec; il ricontrollo
indipendente è registrato separatamente nel verbale di chiusura.

L'ultima ricompilazione mantiene 129 pagine e la stessa collocazione delle
figure. Il controllo del capitolo 1 confronta sia testo sia pixel con il
PDF della baseline; le tavole di dettaglio conservate restano identiche
nei loro file nativi e nelle esportazioni.

## Indice, rinvii e messaggi TeX

Osservate anche le pagine fisiche 2–3 dell'indice: otto sezioni, sei schede
principali correttamente ordinate e nove aggiuntive in A; pagine di avvio e
numerazione coerenti. I 60 riferimenti presenti negli input risultano
risolti nel file ausiliario finale. Zero `Overfull`, `Underfull`, riferimenti
indefiniti, richieste di rerun, caratteri mancanti o labelwidth negativi.

Restano **cinque** messaggi preesistenti `Infinite glue shrinkage found in
box being split`, nell'appendice C: pagine stampate 120–124, fisiche 123–127.
Sono nel [terzo log finale](compilazione-finale-3.txt). Osservate tutte le
pagine 121–129 di C nella nuova composizione: intestazioni ripetute, righe,
continuazioni e piè di tabella sono completi, senza sovrapposizioni visibili.
I messaggi non vengono dichiarati eliminati; il contenuto delle tabelle è
fuori dal riallineamento del ticket 10 e resta conservato.

Questo controllo riguarda il documento e le figure; non è un nuovo test
applicativo né un'approvazione degli autori.
