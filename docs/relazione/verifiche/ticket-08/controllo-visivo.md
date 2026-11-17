# Osservazione completa del PDF — ticket 08

14 novembre 2026. Osservazione effettiva dell'agente principale tramite
`view_image`, distinta dagli assert e dalla successiva code-review.
PDF SHA-256 `e046f9efc5b3a7411354bfdde71b288e0d748f01ebea8c2aa7b3534c5a8990d4`.
107 pagine A4: frontespizio, i–ii e 1–104.

Rese con PyMuPDF a **120 dpi**, 993×1404 pixel circa; aperta ogni singola
pagina, in ordine, in gruppi di quattro (ultimo gruppo 105–107).
Tutte le 15 figure screenshot nel PDF sono state inoltre aperte come
ritagli del PDF a **220 dpi**. I rendering sono in
`docs/relazione/build/visual-ticket08/`, ignorata; il verbale e il manifest
dei rendering restano persistenti. Non è un controllo per miniature.

| Pagine fisiche osservate | Contenuto e valutazione |
| --- | --- |
| 1–3 | Identità, corso, docente, A.A. e stato proposto corretti. Entrambe le pagine dell'indice leggibili; capitoli 1–5, quindici schede A, B e quattro sezioni C presenti, destinazioni e numeri coerenti. |
| 4–6 | Descrizione, stack e semplificazioni: titoli, elenchi, termini economici e margini integri. |
| 7–12 | Tre ruoli, diagrammi UC e sintesi; identificativi e rimandi A.1–A.15 riconoscibili, nessun testo nelle ellissi tagliato. Gli spazi bianchi delle pagine di figure non sono lacune editoriali. |
| 13–21 | Autenticazione e aree dei tre ruoli: 15 screenshot, didascalie 2.4–2.18 e provenienza. Ingranditi tutti gli screenshot: intestazioni, valori, comandi e colonne leggibili. Le celle troncate con puntini e la porzione visibile delle tabelle sono caratteristiche delle acquisizioni UI originali, non tagli del PDF. I dialoghi raster ingranditi mostrano pixel, ma il contenuto rimane leggibile; nessuna acquisizione alterata. Per i dati minuti è appropriato lo zoom. |
| 22–24 | Tre navigazioni, legende, frecce, UC e dialoghi. Ritorni e distinzioni fra schede/azioni leggibili. |
| 25–31 | Package, catalogo ed eventi; notazioni e note leggibili, due tavole riprese coerenti con B. A pagina 31 il rinvio corretto ora mostra «capitolo 3», senza stato superato. |
| 32–44 | Tutte le otto sequenze: ingressi UI, linee di vita, chiamate, alternative, confini transazionali, note e didascalie. Testi densi ma leggibili; nessun incrocio che impedisca di seguire le frecce. Limite zero esplicito a 42 e 44. |
| 45–53 | ER-01/02 e REL-01/02/03: cardinalità, attributi, vincoli e note integri. Riferimenti e passaggio al capitolo 3 coerenti; nessuna tabella troncata. |
| 54–63 | Tutto il capitolo 3: 13 frammenti con numeri originali, simboli Java/SQL, commenti e didascalie leggibili. Le righe lunghe vanno a capo entro i margini. Il titolo DAO su due righe rimane integro. |
| 64–68 | Tutto il Testing: tre estratti, tabelle/classi citate, comandi, ambienti e conteggi storici. Nessuna riga persa al cambio pagina; nessun successo corrente attribuito alle istruzioni. |
| 69–71 | Tutto il capitolo AI e otto note: attribuzioni, percorsi lunghi spezzati, rimandi a 4/C e limiti leggibili; nessun testo delle note fuori pagina. |
| 72–85 | Tutta A: introduzione e quindici schede, flussi e alternative leggibili. UC-05 a pagina 77, dopo la ricompilazione, contiene stima nulla/rollback e rinvio a 2.7 senza uscire dal margine inferiore. Le postcondizioni restano riferite al successo. |
| 86–98 | Tutta B: legenda e dodici tavole, identificativi CL-01–11 con CL-08a/b, frecce, molteplicità e note leggibili. Nessun ritaglio di membri o intestazioni rilevato. |
| 99–107 | Tutta C: provenienza, matrici C.1/C.2 con intestazioni ripetute e continuazioni, limiti e riproduzione. Nomi lunghi e celle su più righe leggibili, righe separate; nessuna cella sovrapposta. Ultima pagina conclusa senza segnaposto. |

L'osservazione riguarda **tutte le 107 pagine del PDF aggiornato**, dopo le
correzioni a 2.6 e UC-05, non soltanto le pagine nuove del ticket precedente.
Non sono emersi altri difetti documentali che richiedessero una nuova stesura
o impaginazione. La compilazione finale non segnala Warning, Underfull,
Overfull, caratteri mancanti o riferimenti indefiniti. Restano tuttavia cinque
`ignored error: Infinite glue shrinkage`, omessi nella prima valutazione e
segnalati dalla review Spec. Il 15 novembre sono state riosservate le pagine
fisiche 101–106 (numeri stampati 98–103), comprese continuazioni e passaggio
fra C.1 e C.2: intestazioni, righe, separatori e margini integri; nessun taglio
o sovrapposizione rilevato. Valutazione e limite residuo nel
[verbale diagnostico](valutazione-messaggi-tex.md). Gli avvisi transitori
del primo passaggio restano conservati e valutati in `comandi.md`.

Il PDF ricompilato dallo ZIP finale è confrontato su tutte le pagine nel
testo e nei pixel a 96 dpi: coincide visivamente con quello osservato.
Questo confronto non sostituisce l'osservazione qui registrata. Non sono
state eseguite prove di stampa, screen reader o accessibilità PDF generale.
L'ispezione dell'agente non costituisce approvazione degli autori.
