# Controllo visivo effettivo

17 novembre 2026. PDF completo151 pagine, estratto4/C22 pagine. Osservati i
rendering individuali a 108 dpi generati dal nuovo checker: [elenco](pagine.json).
Numeri sempre fisici, contando frontespizio/indice; il numero stampato è4inmeno.

| Pagine osservate | Riscontro |
| --- | --- |
| 2–4 | Indice resta3 pagine. Quindici voci Testing nei livelli/ordine previsti; cinque sezioni, sei temi BusinessLogic, tecnologie e due sottosezioniDAO. Rinvii4.3.2, capitoli/appendici aggiornati, nessuna sovrapposizione. |
| 106–108 | Strategia/tecnologie, classificazione e invarianti/identità; estratto duplicato completo, rientri e didascalia leggibili. Formula/sconti/prezzi distinguono massimo e cascata; nomi lunghi vanno a capo entro margini. |
| 109–110 | Tariffa57,05 e accordo conservato, limitiPBKDF2 e tipi/zero; scadenza0,03 con notifica€0,02, menoSQL e riga lunga leggibili. Nuovo font OpenType soltanto in quel listato; nessun glifo mancante nel log finale. |
| 111–112 | RiletturaRent/date/saldo; contrattoDatabaseManager completo con graffe/causa/soppressa e didascalia sulla stessa pagina; nuovi assertDAO/contesto leggibili. |
| 113–114 | Tipi/mapping/enum, schema/demo13 tabelle/sequenze e reset; listato demo e tre estratti iniziali conservati, rollback dopo eliminazioni e sequenza diagnostica leggibili. |
| 115–116 | GatePID/timeout e limiti, FullWorkflow distinto e due stati BLOCKED/SOLD nello stesso scenario; entrambi listati e didascalie completi. Provenienza/incompatibilitàharness esplicite. |
| 117–118 | UI/tabelle e limiti di ciclo di vita; comandi correnti, launcher e comandoE1 distinti. Numerazione/listati/caption senza tagli; risultati storici e limiti presenti nel finale. Il bianco finale precede il capitolo5 e non omette contenuto. |
| 143–144 | ProvenienzaS0/E1/E2/E3, versioni/ambienti/comandi/fonti/limiti e fallimenti; testo e inizioC.2 completi. |
| 145–149 | Tutte le pagine della matrice funzionale osservate, con intestazioni ripetute, regole, righe e “segue”; nessuna cella o riga tagliata/sovrapposta. Le pagine147/149 sono ricontrollate, anche confrontando la147 con la139della baseline. La notaSaleType ampliata rimane nella cella. |
| 150–151 | Matrice trasversale completa, chiusura sui limiti e nuova provenienza12; sezioneC.4 e collegamenti ai controlli documentali leggibili. |

Tutti i 15 blocchi hanno codice/comandi e didascalia sulla stessa pagina.
Il confronto lessicale col PDF normalizza spazi/legature e il meno tipografico;
il confronto degli estratti LaTeX con i sorgenti verifica esattamente ogni riga.
Le immagini non sono dichiarate un test del software o una verifica di accessibilità.

## Cinque messaggi TeX iniziali

La baseline conserva i messaggi in `verifiche/ticket-11/latex-log.txt`, copiati
senza riscriverli in [latex-baseline.log](latex-baseline.log): cinque
`ignored error: Infinite glue shrinkage found in box being split`, stampate 133–137,
fisiche 137–141. [Riscontro nuovo](messaggi-tex.json): nella composizione conclusiva
persistono tutti e cinque, stampate 141–145, fisiche 145–149, tutte inC.
Nessuno rimosso, nessun sesto messaggio finale; non attribuiti al ticket 12.
Le tabelle restano osservabili senza tagli o sovrapposizioni nelle pagine coinvolte.

Zero Overfull/Underfull, caratteri mancanti, riferimenti irrisolti o richieste
di rerun nel log conclusivo. I primi passaggi avevano riferimenti da stabilizzare,
spaziatura e glifo € mancanti; corretti prima dei rendering finali. Il limite
TeX preesistente rimane esplicito, non dichiarato eliminato.

Capitoli1–3: tutte le 101 pagine identiche a testo/pixel. Capitolo5 e A/B:
24 pagine con testo equivalente tolto il numero del piè di pagina, slittamento+8.
Nessuna tavola o screenshot modificato o riesportato. La revisione degli autori
rimane da svolgere; questo verbale non ne attribuisce l'approvazione.

## Ricontrollo della precisazione heap

Pagina fisica 107 riaperta dopo `compilazione-heap-3.txt`: si legge ora
«l'heap della JVM Surefire ha un massimo di 192 MB», con impaginazione
regolare. Il confronto dei pixel in `confronto-correzione-heap.json` trova
identiche le altre 150 pagine, comprese tutte le altre già osservate di4/C
ed indice. Estratto e rendering rigenerati dal candidato identificato nel
manifest aggiornato. Restano gli stessi cinque messaggi preesistenti inC.
