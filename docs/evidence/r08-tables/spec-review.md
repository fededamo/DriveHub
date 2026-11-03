# Spec — review indipendente ticket 14

Diff congelato della sola sessione: `review.patch`, SHA256 `b7cf51719edf2beffd4c2b4e0130e989e31f1a5a928518c313992e8ddd827c5f`, 14 file; confronto con la fotografia locale, non con HEAD. Hash dei file ricontrollati. Relazione_SWE esclusa; nessuna modifica o esecuzione concorrente di suite/GUI.

**0 rilievi Spec.**

- AC16/US49, «rendering e scorrimento senza query»: i renderer dei tre cataloghi/inventario e degli sconti leggono esclusivamente righe preparate. Il caricamento collettivo riusa CatalogService e PriceCalculatorService, con quattro query per listino e due per sconti modello; nessuna cache persistente o modifica a DAO/schema/formule.
- Criteri prezzi/aggiornamenti: selezione automatica/manuale e tariffa sono calcolate fuori JavaFX. Modifiche alla base e agli sconti ricaricano i dati; selezioni dei comandi convertite al Vehicle della riga. I test PostgreSQL e la GUI verificano prezzi letterali, centesimi, svalutazioni, ritorno all'automatico e modifica del modello in una vista già aperta.
- AC16/US50, «caricamenti falliti o superati ... senza ... dati obsoleti»: Task correnti identificati e precedenti cancellati, callback JavaFX, stato e riprova. Coperti lock reali con backend/blocker osservati, errore SQL reale con ripristino finally, risultato ed errore obsoleti dopo vera query JDBC e chiusura osservata delle risorse. L'errore obsoleto iniettato è correttamente distinto dalla prova PostgreSQL.
- US51: Robot esercita frecce, Tab, inserimento base, Spazio/Invio e riprova; etichette, stato e sette schermate verificati. Il limite del titolo abbreviato e l'assenza di certificazione screen reader sono espliciti.
- Richiesta TDD: tre coppie riportano sorgenti identici RED/GREEN; ampliamenti e guasti dell'ambiente/harness sono distinti. Baseline390/390 nuova e mirati148/148 verificabili; finale GUI riuscita.

Suite completa finale, sentinella, conservazione e documenti di chiusura sono ancora in corso: correttamente non dichiarati conclusi nel diff revisionato. Attendo il delta finale prima di attestare la chiusura.

## Delta documentale di chiusura

Review indipendente della sola `closing-docs.patch`, SHA256 `3bba4a98c5f7499153bdb1154f8c7c1d19a94f907d5edb5982333242a302284d`: **0 rilievi Spec**. I soli tre file cambiati dal freeze sono piano, ticket14 e verification; runtime, test e harness conservano gli hash revisionati.

Ricontati gli XML finali: **394/394**, zero failure/errori/salti; quattro nuovi test, isolamento/sentinella e regressioni precedenti inclusi. Log finali GUI e modelli entrambi `UI CHECK SUCCESS`; conservazione1089presenti/1082identici/7aggiornamenti/zeroeliminazioni, HEAD/indice invariati; identità/privilegi dedicati, sentinella finale in sola lettura e arresto PG/Xvfb sono documentati.

La chiusura dei5criteri, di ticket14/R08/AC16/#16 e il totale14/20 corrispondono alle prove; AC13 e ticket successivi restano aperti/non avviati. Limiti grafici, JDBC e certificazioni non eseguite sono espliciti. Nessuna sovradichiarazione rilevata; nessun test o modifica al prodotto effettuati durante la review.
