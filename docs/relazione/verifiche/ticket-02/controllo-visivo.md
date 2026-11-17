# Controllo visivo — 12 novembre 2026

PDF realmente compilato con Tectonic, aperto con PyMuPDF e renderizzato.
Sono state aperte e ispezionate le tavole `controllo-01.png`–`controllo-17.png`
in `docs/relazione/build/visual-ticket02/`, che affiancano le pagine senza
modificare le immagini inserite. Le tavole sono riproducibili con
`controlla_documento.py`. Sono state ispezionate anche la copertina e le
pagine di transizione alle sezioni ancora provvisorie, B e C.

| Pagine fisiche | Controllo effettivo |
| --- | --- |
| 1 | Dati del frontespizio e stato di stesura progressiva; parti presentate per revisione, senza approvazione implicita del capitolo 1. |
| 2–3 | Indice in due pagine: titoli, ordine 2.1–2.4, numerazione e quindici schede dell'appendice A, senza sovrapposizioni. |
| 7–10 | Spiegazione dei livelli; tre casi d'uso draw.io; attori, identificativi, ellissi, associazioni e didascalie leggibili. Corretto l'instradamento che attraversava le ellissi. |
| 11–12 | Sintesi dei sei casi e rimandi A.1–A.15, terminologia e riferimenti ai servizi; nomi delle classi dentro i margini. |
| 13–21 | Quindici screenshot: accesso/registrazione, cliente, dialoghi di test drive/noleggio, proposta, identità, venditore e manager. Controllati corrispondenza delle didascalie, tab attive, controlli visibili e dati demo. |
| 22–24 | Tre diagrammi di navigazione, completi su una pagina ciascuno; tab, azioni e risultati, legenda e riferimenti ai casi. Nessuna porzione omessa dall'esportazione. |
| 25–26 | Confine del ticket: sezioni 2.5–2.8 ancora marcate provvisorie. |
| 30–43 | Introduzione dell'appendice e tutte le quindici schede: campi, passi e alternative numerati; riferimenti coerenti. Nessuna alternativa isolata su una pagina successiva dopo la correzione delle interruzioni. |
| 44–45 | Appendici B e C ancora provvisorie. |

I tre PDF dei casi d'uso aggiornati sono stati riesportati, reinseriti e
ispezionati nel PDF finale. Per i diagrammi di navigazione è stata verificata
la presenza di tutti i riquadri e delle legende, oltre alla validità XML.
I dialoghi sono catture dirette della propria finestra. Le tabelle nelle
schermate complete richiedono zoom per leggere le celle più piccole; i PNG
originali sono disponibili a risoluzione piena. Nessuna immagine storica è
inclusa e nessuno screenshot è una ricostruzione grafica.

I controlli automatici risolvono tutte le 91 destinazioni interne e
confrontano l'ordine dell'indice con l'indice concordato. Non ci sono `??`,
caratteri mancanti, box sovrapposti o troppo laschi nel log finale. Il solo
avviso noto sulla versione dei PDF inclusi è riportato nel verbale e nel log.

Questo è un controllo documentale e visivo dell'agente, non l'approvazione
degli autori, una prova su Windows o una nuova esecuzione della suite applicativa.
