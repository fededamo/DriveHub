# Osservazione del PDF integrato — ticket 13

17 novembre 2026. Osservazione diretta dell’agente delle **151 pagine fisiche**
di `docs/relazione/relazione-drivehub.pdf`, dopo la terza compilazione.
Non è una revisione degli autori. Il manifest [rendering.json](rendering.json)
lega ogni pagina al PNG osservato in [pagine/](pagine/), a 108 dpi; i
[provini](provini/) affiancano due pagine A4 senza eliminarne contenuto.
Le dodici A3 sono state inoltre aperte singolarmente alla dimensione del
rendering. Le osservazioni riguardano composizione e leggibilità, insieme
al confronto semantico delle tavole e alla lettura delle fonti.

| Pagine fisiche osservate | Osservazioni |
| --- | --- |
| 1 | Frontespizio: nomi, insegnamento, docente e anno accademico leggibili; nessuna approvazione attribuita. |
| 2–4 | Indice su tre pagine; capitoli 1–5, A–C e ordine UC-05 prima di UC-04 coerenti. Numero pagina e titolo non collidono. |
| 5–7 | Introduzione: tre ruoli, stack e semplificazioni distinti; riordino ambiente/processi già acquisito, testo e margini regolari. |
| 8–12 | Casi d’uso: panoramica A3 e tre viste di ruolo; nomi, livelli e didascalie leggibili. Gli attraversamenti delle associazioni nella base originale restano riconoscibili; nessun taglio dei contenuti. |
| 13–20 | Sei schede principali complete: pre/postcondizioni, flussi, alternative e portata delle prove. UC-05 precede UC-04. Nessuna scheda ridotta al titolo o trasferita in A. |
| 21–35 | Interfacce per funzione: sedici immagini disponibili, descrizioni e limiti. Accesso/benvenuto e richieste condividono immagini motivate; riepilogo prenotazioni venditore assente dichiarato. Tabelle e didascalie entro i margini. |
| 36–40 | Navigazione generale e tre ruoli: LoginView iniziale, dispatch dal ruolo, ritorni e dialoghi distinguibili. |
| 41–50 | Package e quattro gruppi di classi: palette riconoscibile, UML e note leggibili. Callback separate; App costruisce un navigator anonimo; nessun componente rimosso rappresentato come corrente. |
| 51–60 | Vendita al cliente: insieme A3 e dettagli SQ-01a–d; tre transazioni distinte, accordo/acconto/saldo e rifiuto/scadenza. Frecce e note non tagliate. |
| 61–65 | Noleggio e Observer: preventivo separato dalla conferma, confini Connection, rollback e cancellazioni future leggibili. |
| 66–69 | Cessione dal cliente: proposta e due esiti separati, VIN/WAITING, accredito e limite dello zero espliciti. |
| 70–82 | ER A3 e legenda, dettagli ER-01/02, relazionale REL-01–03 e tredici relazioni nel testo. Tipi enum, opzionalità e limiti dell’identità globale distinguibili. |
| 83–105 | Implementazione: cinque sezioni, responsabilità, esempi e ventisette estratti. Tabelle 3.1 (fisica 93) e 3.2 (102) leggibili; listati completi entro margini. |
| 106–118 | Testing: quindici blocchi censiti, preparazione/assert/riletture e limiti. Simbolo euro e 0,02 a pagina 110 leggibili; comandi e prerequisiti non confusi con nuove esecuzioni. |
| 119–121 | Capitolo 5 aggiornato: dichiarazioni, tracce e controlli distinti. Nuovo paragrafo sul riallineamento interamente visibile a pagina 121. La nota 6 prosegue da 120 a 121 con continuità regolare; note e corpo non si sovrappongono. |
| 122–126 | Appendice A, introduzione e UC-07/08/09/10/11/S01: schede aggiuntive, senza ripetere le sei principali. |
| 127–129 | A: UC-S02/S03/S04 completi, account/identità/conto demo e limiti leggibili. |
| 130–142 | B: introduzione e dodici fogli per undici package (dao diviso in CL-08a/b). Nomi, firme selezionate, legende e didascalie B.1–B.12 integri. |
| 143–144 | C.1 e inizio C.2: provenienza S0/E1/E2/E3, date, tentativi falliti, ambienti e incompatibilità distinti. |
| 145–148 | Matrice funzionale multipagina: intestazioni ripetute, separatori, rinvii ai passi e colonna dei limiti leggibili. Nessuna riga sovrapposta o perdita al salto pagina. |
| 149 | Ultima riga funzionale e avvio C.3: continuità regolare, due tabelle distinte e footer “Segue” al posto corretto. |
| 150–151 | Conclusione C.3 e C.4: limiti di coverage, difetti applicativi separati e riproducibilità storica espliciti. Testo finale non troncato. |

## Tavole A3 e confronto delle basi

Osservate singolarmente le pagine **9, 37, 42, 44, 46, 48, 50, 52, 61,
66, 71 e 72**: nove orizzontali e tre sequenze verticali. La figura ER
occupa due fogli, conservando lo stesso numero 2.41 e una legenda separata.
Controllati identificativi, attori, punte delle frecce, cardinalità, tipi,
nomi e didascalie; nessuna pagina A3 è stata valutata soltanto dal provino.

Confrontati inoltre i riferimenti visivi originali degli undici diagrammi:
`users-swe-current.png`, `page-navigation-original.pdf`,
`package-diagram-original.pdf`, i quattro `class-*-drivehub.pdf`, le tre
`sequence-*-drivehub.pdf` e `swe-er-original.pdf` in `Relazione_SWE/images/`.
I dieci rendering PDF originali sono conservati nei provini `base-*`; la
PNG originale è letta dal suo percorso intatto. Si riconoscono ellissi e
attori, tre diramazioni di navigazione, raggruppamenti e palette
rosso/giallo/blu/verde, sequenze con lifeline e divisori, notazione ER con
gerarchia concettuale. Gli adattamenti di nomi, collaboratori e regole sono
quelli documentati per ciascuna tavola in [tavole.md](tavole.md), verificati
sui sorgenti correnti. Le dimensioni e i caratteri sono adattati al nuovo
formato, senza pretendere identità grafica con i PDF PlantUML originali.

## Messaggi residui, correzioni e ricontrollo

I cinque `ignored error: Infinite glue shrinkage found in box being split`
rimangono alle pagine stampate 141–145, **fisiche 145–149**. Sono presenti
anche nella nuova compilazione della baseline e nella ricompilazione da
ZIP. [messaggi-tex.json](messaggi-tex.json) riporta le righe effettive.
In quelle cinque pagine ho controllato intestazioni, righe, interruzioni e
fondi pagina: nessun difetto visivo associato rilevato. Le pagine sono
anche identiche alla baseline a testo e pixel; non si dichiara rimozione,
miglioramento o regressione. Non risultano Overfull, Underfull, glifi
mancanti o rinvii irrisolti nei log finali.

Non è risultata necessaria una correzione d’impaginazione. L’unica modifica
al corpo è l’aggiornamento del capitolo 5; le sue tre pagine sono state
osservate integralmente dopo la stabilizzazione. [paginazione.json](paginazione.json)
attesta che nessun intervallo cambia: 1–3 a 5–105; 4 a 106–118; 5 a
119–121; A a 122–129; B a 130–142; C a 143–151. **Solo 119–121 cambiano
contenuto e pixel**; le altre 148 pagine restano identiche, anche frontespizio
e indice. Il confronto della ricompilazione da ZIP copre tutte le 151
pagine per testo, formato e resa, senza dedurre l’osservazione dal solo
esito automatico.
