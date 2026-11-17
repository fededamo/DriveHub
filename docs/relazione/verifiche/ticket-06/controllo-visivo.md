# Osservazione visiva effettiva del PDF

13 novembre 2026, agente principale. Rendering PDF con PyMuPDF a fattore 1,5,
file aperti con view_image; nessun avvio/cattura dell'applicazione.
Versione finale: **105 pagine**, SHA-256
`05aed422b71cf85a5532ff4fab49549d4605add90ad20fe8a397c6720ef844b2`.
Pagine indicate fisicamente (dal frontespizio = 1); quelle stampate hanno
lo scarto dei tre fogli iniziali.

Sono state osservate **tutte le 14 nuove pagine**, non un campione:
64,65,66,67,68 e 97,98,99,100,101,102,103,104,105. Osservati inoltre
frontespizio/indice1–3, aggiornamento di stato54, confine finale capitolo3
pagina63, capitolo5 provvisorio69 e fine appendiceB96. I rendering sono in
`docs/relazione/build/visual-ticket06/`; quelli intermedi non costituiscono
screenshot dell'app o una nuova esecuzione di test.

| Pagine | Controllo visivo e risultato |
| --- | --- |
| 1–3 | Autori/corso/anno e proposta1–4/A–C leggibili. Indice con cinque sezioni4 e C dopoB, pagine e riferimenti coerenti; capitolo5 non dichiarato completato. |
| 54,63 | Modificata solo frase di stato a54; frammenti3 e conclusione del capitolo leggibili, tecnicamente invariati. Nessuna nuova approvazione attribuita. |
| 64 | Titolo e strategia leggibili; separazione classi in memoria, JDBC e mainJavaFX. Precisazione finale AppStartup limitato all'ingresso pubblico presente. |
| 65 | Invarianti/prezzi/accordo leggibili. Frammento4.1 completo con didascalia nella stessa pagina, righe17–24 e centesimi conservati. |
| 66 | Mapping/enum e rollback leggibili; frammento4.2 righe318–323, ritorni a capo delle chiamate e virgolette corretti. SQL **RETURN NULL** reso con spazio dopo correzione texttt. |
| 67 | Frammento4.3 righe301–308 con didascalia insieme; ultima chiamata ritorna a capo senza perdita di contenuto. Gare e presentazione non si sovrappongono. |
| 68–69 | Comandi e prerequisiti distinguono senzaDB/profilo/harness; backslash shell visibile. Sintesi storica e limiti nel corpo; pagina69 mantiene capitolo5 provvisorio. |
| 96–98 | ConfineB/C corretto; provenienzaS0/E1/E2/E3 leggibile, date/commit/ambiente/esiti e limiti non confusi con nuove esecuzioni. |
| 99–103 | TabellaC.1 su cinque pagine, intestazioni ripetute, diciture continua/segue, righe complete e colonne distinte. Metodi lunghi interrompibili senza uscire dalle celle; lacune leggibili. Osservati anche dettagli delle intestazioni ripetute tramite renderingPDF ritagliato. |
| 103–104 | TabellaC.2 su due pagine; caption, colonne e continuazione leggibili, nessuna riga tagliata. Confini enum/nullabilità e UI distinti. |
| 104–105 | Chiusura matrice, limiti noti e riproduzione coerenti; eliminata l'interruzione di un percorso in mezzo alla parola fra pagine. Ultima pagina naturalmente parziale, senza pagina vuota aggiuntiva. |

## Correzioni seguite da ricontrollo

La prima impaginazione aveva identificatori oltre colonna: aggiunte interruzioni
locali dei riferimenti e celle raggedright, ricompilato. La prima versione del
capitolo lasciava una pagina quasi vuota: sintetizzata la sola conclusione4.5,
senza spostare dettagli dal corpo se necessari, mantenendoli tracciati inC.
La visualizzazione ha poi evidenziato perdita dello spazio in `code{RETURN NULL}`:
corretto localmente in texttt. Precisata anche la distinzione AppStartup/DB.
Ricompilati realmente PDF e indice, rieseguiti gli assert e ricontrollate le
pagine interessate64–68, indice e confini della versione105p sopra identificata.

I controlli testuali confrontano frammenti/sorgenti e testoPDF, validano144link,
84font e geometria, senza sostituire questa osservazione. Il log finale non
ha box difettosi o caratteri mancanti. I96avvisi noti di versione delle figure
PDF restano dichiarati; tavole precedenti conservate. Non è stata osservata
manualmente ogni pagina tecnica già completata della relazione; ne è stato
confrontato il contenuto testuale e i sorgenti sono protetti dalla baseline.
L'osservazione non certifica accessibilità del PDF/app né approvazione degli autori.
