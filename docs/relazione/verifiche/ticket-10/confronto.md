# Ticket 10 — corrispondenza originale / nuova stesura

16 novembre 2026. Riferimento editoriale: input effettivi di
`Relazione_SWE/capitoli/2_progettazione.tex`. Riferimento tecnico: sorgenti
correnti, schema PostgreSQL e ADR 0001–0002, identificati in `fonti.json`.
Il confronto comprende contenuti e funzione delle figure, non solo titoli.
Le fonti applicative sono lette; nessuna prova software è stata eseguita.

## Input e scansione

L'originale include `2a_casi_uso`, i sei template, `2b_interfaccia`,
`2c_package_diagram`, `2c_class_diagram`, `2c_sequence_diagram`,
`2c_database`: tredici file attivi con il capitolo radice. I due file
`2c_architettura` e `2d_dati_flussi` non sono input effettivi.
La nuova composizione mantiene `2_funzioni`, `2_architettura`, `2_sequenze`
e `2_database`; `2_template` raccoglie il testo valido trasferito da A.
Sono presenti le stesse 47 voci: otto sezioni, venti sottosezioni numerate,
una sottosezione non numerata sul dominio e diciotto sottosottosezioni UI.
Il controllo ripetibile confronta livelli e ordine, espandendo input e schede.

## Requisiti e sei template

| Blocco originale | Destinazione e contenuto conservato | Adattamento motivato e fonte |
| --- | --- | --- |
| 2a: descrizione attori, funzioni comuni, sei obiettivi | 2.1: ruoli, obiettivi, supporto, tavola autori e tre dettagli | Registrazione del solo cliente; UC-04 è sottofunzione, UC-05 coinvolge cliente e venditore. Nessuna panoramica del venditore. App/LoginView/dashboard e servizi. |
| 2a: spiegazione dei campi e numerazione delle alternative | Apertura 2.2, sei tabelle e relativi flussi | Identificativi UC stabili; alternative n.a/n.b con il passo di origine. Livelli O/F/S descrittivi; attore UI non implica autorizzazione generale nel servizio. |
| templates/test_drive: nome, livello, descrizione, condizioni, attori, test, cinque passi e A1–A4 | 2.2.1 UC-01, tutti i campi, prenotazione, limite di tre e conflitti; aggiunte cancellazione e rollback | Dialogo invece di scheda dedicata; UI a ore intere, dominio senza arrotondamento storico dei minuti. Lock veicolo/cliente e UQ SQL attuali. BookingRules/BookingConcurrency/TestDriveCancellation; prove storiche distinte dal riscontro statico. |
| templates/noleggio: campi, costo, addebito, prenotazione e alternative | 2.2.2 UC-02, periodo incluso, preventivo e conferma separati, costo ricalcolato, addebito completo | EXCLUDE oltre ai lock; Observer cancella tutti i test drive futuri, anche fuori dal periodo. La UI non offre annullamento autonomo del noleggio; UC-08 rimborsa i futuri. RentalService e TestDriveCancellationObserver. |
| templates/acquisto: campi, acconto, autorizzazione, saldo, annullamento/scadenza e anomalia prezzo | 2.2.3 UC-03, transazioni separate, riserva, totale e residuo; alternative 2.a/3.a/4.a/5.a/6.a/6.b | L'anomalia del prezzo corrente è risolta dall'accordo persistito (ADR 0001); il servizio verifica approvazione e termine. Non si ripropone l'annullamento autonomo assente dalla UI. Rifiuto rimborsa tutto; scadenza trattiene il 20% dell'acconto, innescata dalla dashboard. PurchaseService/Request/PurchaseAgreementIntegrationTest. |
| templates/cessione: campi, anagrafica, preventivo, proposta, approvazione, VIN e rifiuto | 2.2.4 UC-05, prima di UC-04 senza rinumerarlo; due transazioni e nessun saldo successivo | Modello già censito, preventivo facoltativo, stima persistita; VIN noto richiede ultima SALE al proponente. Tipo PURCHASE e importo non negativo sostituiscono il segno. Storico conservato; accredito zero fallisce con rollback. SaleToDealershipService e test proposta/esito. |
| templates/autorizzazione_acquisto: campi, selezione, autorizzazione, scadenza e errori | 2.2.5 UC-04, venditore, richiesta SALE, esito e termine di un mese | La stessa tabella contiene PURCHASE: quel comando svolge UC-05. Autorizzazione distinta dal saldo e dall'abilitazione staff. Query UI seleziona pendenti; il comando non certifica l'assenza di riapprovazione diretta. PurchaseService/OperationQueryService. |
| templates/sconto_veicolo: campi, selezione, percentuale, salvataggio e alternative | 2.2.6 UC-06: sconto modello e veicolo, salvataggio/rimozione, massimo 0–50 | Tab Sconti separata, ID veicolo o selezione modello; nessuna somma, base manuale nella formula, accordi esistenti invariati; refresh esplicito delle altre tabelle. CatalogService/Discount/PriceCalculatorService. |

Le sei schede conservano senza riassunto le descrizioni, condizioni, flussi,
alternative e limiti validi della precedente appendice A. I sei paragrafi
aggiunti sulle prove identificano classi e portata; non certificano tutti i
rami UI. Le nove schede UC-07–11 e UC-S01–04 restano in A, senza duplicati.
Le vecchie classi di prova su H2/mock non sono spacciate per prove attuali.
I rinvii della matrice C e del capitolo 4 cambiano soltanto destinazione.

## Diciotto funzioni UI, nell'ordine originale

Tutte le immagini sono le sedici acquisizioni esistenti del 12 novembre,
riusate byte per byte. Provenienza PostgreSQL temporaneo/Xvfb conservata
nell'apertura 2.3; nessun nuovo avvio JavaFX o DB. Ogni voce descrive
schermata, input, azione, risultato e limite, oppure motiva l'assenza.

| Originale in 2b_interfaccia | Nuova voce e immagini | Conservazione / adattamento |
| --- | --- | --- |
| Schermata iniziale | 2.3.1.1, rinvio 01 | LoginView è l'ingresso; nessuna Welcome Page separata. |
| Accesso | 2.3.1.2, 01 | Email/password, errore nella vista, dashboard del ruolo autenticato; nessuna scelta pubblica. |
| Registrazione | 2.3.1.3, 02 | Nome, cognome, email e password, identità/conto atomici; solo cliente, staff predisposto. |
| Catalogo veicoli | 2.3.2.1, 03 e 05 | Due tab per destinazione, selezione e comandi; basi e prezzi correnti, caricamento/errore/Riprova. Filtri storici non inventati. |
| Acquisti e richieste | 2.3.2.2, 08 | Richieste, accordo e saldo; registro completo del manager distinto dalle operazioni personali. Nessun comando autonomo di annullamento dell'acquisto. |
| Noleggi e test drive | 2.3.2.3, 04/06 e rinvio 08 | Dialoghi nei cataloghi e tabelle personali; data/ora o periodo, preventivo, conferma/annulla, annullamento del solo test drive. |
| Proposta del proprio veicolo | 2.3.2.4, 07 | Form anagrafico, preventivo facoltativo, invio e attesa dell'esito, non pagamento immediato. |
| Conto dimostrativo | 2.3.2.5, rinvio 09 | Saldo header e ricarica con importo positivo; comando e dialogo, senza vecchia scheda bancaria/prelievo autonomo. |
| Profilo cliente | 2.3.2.6, 09 | Dialogo comune: email, password, cancellazione confermata e ritorno al login; limiti di FK e conto positivo. |
| Veicoli in attesa | 2.3.3.1, 10 | PURCHASE nella tabella comune, tipo visibile; esito, accredito, storico e errori. Non si presume un filtro dedicato. |
| Richieste di acquisto | 2.3.3.2, rinvio 10 | SALE nella medesima tabella: approvazione/rifiuto, importi e successivo saldo del cliente. |
| Stato dei veicoli | 2.3.3.3, 11 | Solo destinazione SALE/RENT, conferma UI, rilettura sotto lock, rifiuto con noleggio in corso e rimborsi dei futuri. |
| Panoramica prenotazioni | 2.3.3.4, nessuna immagine | Assente dalle due tab del venditore; esplicitata la mancata equivalenza. |
| Profilo venditore | 2.3.3.5, rinvio 09 dichiarato cliente | Pulsante buildUi → AccountManagementDialog; stesse credenziali/conferme, senza ricarica. Nessuno screenshot staff presunto. |
| Parco veicoli | 2.3.4.1, 14/15 | Inserimento/rimozione e basi, sconto individuale nella tab Sconti; ADR 0001 e accordi fissati. |
| Marche e modelli | 2.3.4.2, 12/13 e rinvio 15 | Due tab distinte, campi e dipendenze, sconto modello distinto dall'esemplare. |
| Registro operazioni | 2.3.4.3, 16 | Consultazione SALE/PURCHASE/PENALTY non negativi; noleggi distinti, caricamento e Riprova. |
| Profilo manager | 2.3.4.4, rinvio 09 dichiarato cliente | Pulsante buildUi → dialogo comune senza ricarica; credenziali/conferme, nessuna tab dedicata inventata. |

## Architettura, classi e navigazione

| Blocco originale | Nuova trattazione | Ragione degli adattamenti |
| --- | --- | --- |
| 2.4: ingresso, autenticazione, ruoli, schede, logout | Tavola da Page Navigation Diagram e dettagli per ruolo; Scene/tab/dialoghi, annullamento e ritorno dopo cancellazione | Welcome, selettore ruolo e schede obsolete sostituiti con LoginView, dispatch per sottotipo e tab effettive. Le tre diramazioni e le forme restano. |
| 2.5: quattro aree, dipendenze, paragrafo sul dominio | Copia nativa di Package diagram con undici package concreti, composizione e dipendenze; sottosezione non numerata conservata | service/event/security e presentazione Java, non business.strategies/core/controller. Note distinguono raggruppamenti da package. Contratti JDBC e navigazione nel dominio limitano la separazione ideale. |
| 2.6: legenda e quattro gruppi | Quattro tavole d'insieme nel corpo; B conserva CL-01–11, incluse CL-08a/b | Stessa funzione espositiva, palette rossa/gialla/blu/verde e notazione UML. Non è UML generato dal codice; selezione manuale di membri e collaborazioni. |
| Domain Model: catalogo, utenti/conto, operazioni, sconti, eventi | 2.6.1, tavola dominio e spiegazioni valide di identità, base/prezzo, ID, sconti e tipi | BaseEntity, Role e nomi plurali rimossi; User/AbstractUser e tre ruoli reali. BankAccount è riferimento effettivo; gli altri legami sono ID. Eventi ricollocati in business: non persi. |
| Business Layer: servizi, prezzi, sicurezza, coordinamento, Observer | 2.6.2, tavola servizi/calcolatore/DAO/events/security e spiegazione Observer | PurchaseService e SaleToDealershipService sostituiscono SalesService; AuthService include conto. Calcolatore concreto, senza Strategy. DatabaseManager e callback al posto di TransactionRunner/UnitOfWork. |
| DAO: porte, quattro coppie rappresentative, altre sei e coordinamento | 2.6.3, tavola nativa DAO e spiegazione Connection/overload/mapping | Dieci contratti e Postgres*Dao; nessuna factory, UnitOfWork o loader separato. Connessioni autonome distinte da quelle passate dai servizi. B conserva tutte le realizzazioni. |
| Presentazione: viste, contesto, gateway, navigazione e permessi | 2.6.4, tavola viste/App/callback/servizi e spiegazioni AsyncLoad/identità | Viste BorderPane costruite in Java; dialogo utility, non Dialog; Task per le letture. Nessun FXML/controller/gateway/Route/sessione separata. App costruisce un navigator anonimo, non lo implementa direttamente. |

## Sequenze e dati

| Blocco originale | Nuova trattazione e profondità conservata | Fonte / limite |
| --- | --- | --- |
| Apertura 2.7: collaboratori, messaggi, transazioni | Tre sequenze d'insieme nello stesso ordine, più gli otto dettagli esistenti; azioni/SQL/sintesi distinti da metodi | Diagrammi selezionati a mano sui servizi e DAO, non estratti automaticamente. Connection e callback, commit/rollback e limiti di apertura/chiusura rimangono spiegati. |
| Vendita: acconto, autorizzazione, saldo | 2.7.1, insieme + SQ-01a–d; totale fissato, quattro fasi con rifiuto/scadenza, lock, Observer | Modello e sconti autonomi a T1; saldo su agreed_total, controllo termine anche nel servizio. Rimossa l'anomalia del vecchio listino al saldo e l'idea che il solo controller autorizzi. |
| Noleggio: disponibilità, costo, conto, annullamenti | 2.7.2, insieme + SQ-02/SQ-OB; preventivo separato, formula geometrica, intervallo incluso, addebito completo | Lock veicolo/cliente/conto e EXCLUDE; cancellazioni future tutte, sulla stessa connessione. Non si conserva la vecchia descrizione di Observer non atomico o controllo soltanto applicativo. |
| Cessione: proposta, accettazione, storico | 2.7.3, insieme + SQ-03a/b; stima fissata, VIN noto e ultima vendita, decisione e accredito | Tipo esplicito con importi non negativi; nessuna UnitOfWork annidata o venditore costante. Rifiuto senza movimenti, storico conservato; zero ammesso nel dato ma non nel conto. |
| 2.8.1: ER e spiegazione delle cardinalità | SWE ER riadattato, legenda, ER-01/02 e testo nei due versi | User concettuale distinto dalle tre tabelle e dalla vista. Request.vehicle unico, conto opzionale per il cliente, registro nullable e ON DELETE specifici. |
| 2.8.2: tredici relazioni testuali, tutti gli attributi, FK e legenda | Recuperate tredici voci testuali accanto a REL-01–03, con PK/FK/U/? e tutti gli attributi; candidata vs UNIQUE nullable | price_mode, request.type/agreed_total e sale.type attuali; nessuna codifica tramite segno. DDL verificato staticamente per attributi, nullabilità e 15 FK. |
| 2.8.2–3: tipi e vincoli economici/temporali | Quattro enum SQL, centesimi, accordo, anni, targa, sconti, EXCLUDE, UQ test drive e fascia | SQL non impone 20%, immutabilità dell'accordo, date future o tutte le combinazioni economiche. Le regole applicative sono indicate come tali. |
| 2.8.3: utenti, mapping, identità, conto, bootstrap | Tre tabelle e UNION ALL, user_id_seq, PK/email locali, trigger e limiti, conto simulato | PostgreSQL predisposto esternamente, senza H2/seed/bootstrap automatico Java. ID espliciti, concorrenza email e confronto LOWER non diventano un'unicità globale garantita. |

## Materiali conservati e perimetro

Il capitolo 1 è intatto, come originali, screenshot, dettagli draw.io preesistenti,
software, test, schema, dati, glossario, ADR e tutte le verifiche precedenti.
B riceve soltanto una frase che distingue insieme/dettagli. L'appendice C
e il capitolo 4 ricevono il solo rinvio necessario alle sei schede spostate.
Capitoli 3 e 5 invariati. Nessun frammento applicativo dell'originale appartiene
al capitolo 2: i 23 listati originali dei capitoli 3–4 restano ai loro ticket.
I PDF e ZIP storici non vengono aggiornati né sottoposti a nuovi vecchi manifest.
La revisione degli autori resta da svolgere.
