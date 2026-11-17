# Osservazione del PDF — ticket 03

Controllo del 12 novembre 2026 sul PDF finale con SHA-256
`568d76ead85fa06d6d3942c25ab0505722e10b1b4365aa5455fbbfa1a18e2d95`.
Il PDF è stato aperto con PyMuPDF e renderizzato; le immagini delle pagine
sono state effettivamente aperte con `view_image`, non soltanto generate.
Il PDF autonomo e l'estratto per gli autori restano gli artefatti di consegna.

## Pagine osservate

Numeri fisici, contando il frontespizio come pagina 1.

| Pagine | Contenuto e controllo | Esito finale |
| --- | --- | --- |
| 1–3 | Frontespizio e due pagine dell'indice: stato progressivo aggiornato, autori/corso/anno conservati, ordine 2.5/2.6 e quattro sottosezioni, numeri di pagina e appendici. | Leggibili; nessun titolo mancante o riferimento visibilmente irrisolto. |
| 25 | Testo di 2.5: responsabilità, undici package, direzione delle dipendenze e composizione. | Testo entro i margini; spazio libero dovuto alla tavola sulla pagina seguente. |
| 26 | PKG, figura 2.22: undici package distinti, frecce principali e nota con tutti i trasversali. | Tavola intera, note e frecce visibili; etichetta root abbreviata per tenerla nel riquadro. |
| 27–28 | 2.6/Domain Model e CL-03, figura 2.23: ID, parametro Model, enum e legenda. | Nessun arco sulle classi estranee; didascalia e rimando coerenti. |
| 29–30 | Domain Model/Business Layer e CL-07, figura 2.24: Observer, cardinalità, connessione e limiti. | Terminologia corretta dopo rilievo Standards; frecce, triangolo e nota leggibili; didascalia su due righe entro i margini. |
| 31 | DAO e Presentation Layer: confini, callback, navigator anonimo, credenziali e autorizzazione. | Testo completo; nessun overflow; ordine delle sottosezioni conservato. |
| 50 | Inizio B: legenda, visibilità e copertura. | Simbolo `~` corretto; nessuna marcatura provvisoria. |
| 51 | B.1 / CL-01: App, contesto, JavaFX, navigator anonimo. | Tipi esterni distinti; costruzione e riferimento ctx leggibili. |
| 52 | B.2 / CL-02: User, AbstractUser, tre ruoli, conto. | Generalizzazioni rettilinee senza attraversare i riquadri; riferimento al conto opzionale separato dagli ID. |
| 53 | B.3 / CL-03: catalogo. | Tavola completa e identica alla vista del corpo; dipendenza verso Model separata dagli enum. |
| 54 | B.4 / CL-04: sconti. | Entrambe le generalizzazioni e vincolo 0–50 leggibili; nessuna falsa associazione alle entità. |
| 55 | B.5 / CL-05: richieste, registro, prenotazioni. | Due enum e quattro classi riconoscibili; importi, nullabilità e chiavi visibili. |
| 56 | B.6 / CL-06: nove tipi del servizio e nota sui record annidati. | Record di lettura e legenda separati, nessuna sovrapposizione finale; campi dei collaboratori leggibili. |
| 57 | B.7 / CL-07: eventi. | Stessa vista del corpo; contratto/implementazione, parametri e lista degli observer leggibili. |
| 58 | B.8 / CL-08a: dieci contratti DAO. | Tutti i contratti e le firme selezionate presenti; rinvio a CL-08b leggibile. |
| 59 | B.9 / CL-08b: gestore e callback. | Qualifiche `DatabaseManager.Transaction*` su due righe, senza sbordare; dipendenze di parametro visibili. |
| 60 | B.10 / CL-09: dieci DAO PostgreSQL. | Tutte le dieci realizzazioni con triangolo vuoto; nessun foglio o riga tagliato. |
| 61 | B.11 / CL-10: sette tipi di presentazione e JavaFX. | Ereditarietà nello spazio libero; dialogo separato da Dialog; AsyncLoad e nota presenti. |
| 62 | B.12 / CL-11: contratto hasher e PBKDF2. | Realizzazione corretta e leggibile; nota non attribuisce autorizzazione generale. |
| 32–35, 63 | Confini di 2.7–2.8, capitoli 3–5 e appendice C. | Titoli e marcature “Provvisorio” conservati e riconoscibili. |

Tutte le nuove tavole sono state osservate **nel documento compilato**.
Sono state anche aperte le esportazioni singole per diagnosticare il primo
layout. Dopo le correzioni sono state ricontrollate soltanto le pagine
interessate: PKG, CL-02, CL-06, CL-08b, legenda B e testo/didascalia
segnalati dalla revisione Standards. I controlli automatici finali hanno
riaperto anche tutte le altre esportazioni e verificato XML incorporati,
indice, font e collegamenti.

## Correzioni emerse dall'osservazione

La prima disposizione aveva archi sulle classi intermedie e alcune firme
strette. Le generalizzazioni dei ruoli sono state rese rettilinee, gli
altri collaboratori riposizionati e le firme abbreviate quando non serviva
l'elenco completo dei parametri. Una nota sui record del catalogo era
coperta dalla legenda: riposizionata e aggiunto un assert di non
sovrapposizione dei riquadri al generatore. Spezzati i due nomi qualificati
lunghi delle callback e corretto il simbolo di visibilità di package nel
testo LaTeX. Non si attribuisce esito positivo alle prime disposizioni.

## Portata e riproduzione

Rendering riproducibile con `controlla-documento.py` nell'ambiente Python
indicato nel README. Immagini delle pagine in
`docs/relazione/build/visual-ticket03/pagina-NN.png`, scala 1,5 (108 dpi);
esportazioni singole nella stessa directory. I PDF sono vettoriali e
consentono zoom; le tavole più dense usano corpi inferiori al testo
principale, ma conservano etichette distinte e non sovrapposte.

Il controllo riguarda questa stesura e queste esportazioni. Non certifica
una stampa fisica, un diverso visualizzatore, la leggibilità degli screenshot
storici a ogni zoom o l'approvazione degli autori. I contenuti dei ticket
01/02 sono conservati e verificati con impronte e confronto del testo,
senza attribuire loro una nuova acquisizione UI.
