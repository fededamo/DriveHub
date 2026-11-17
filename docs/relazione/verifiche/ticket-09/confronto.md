# Confronto originale / stesura attuale — ticket 09

16 novembre 2026. Riscontro documentale sul working tree acquisito in
`baseline.json`, HEAD `61ce18ae1d4e0458b63e970b793b0b3a634a3adf`.
La relazione e il tracker erano non tracciati: il confronto usa le copie
del working tree, non il solo `git diff HEAD`.

## Esito editoriale

Conservato il testo di `docs/relazione/capitoli/1_introduzione.tex`, già
corretto nei contenuti e nelle affermazioni tecniche. Unica modifica: il punto
«Ambiente di esecuzione» precede ora «Processi aziendali ridotti», come
nell’originale la modalità dimostrativa precedeva i processi esclusi.
Il ticket 09 verifica gli adattamenti già presenti senza attribuirli a una
nuova riscrittura. Titoli, etichette e tre sezioni restano gli stessi.
Il confronto con la baseline verifica che nessuna riga del capitolo sia
stata aggiunta o rimossa: cambia soltanto la posizione dei due punti.

Riferimenti confrontati:

- [Originale](../../../../Relazione_SWE/capitoli/1_introduzione.tex), 29 righe.
- [Stesura verificata](../../capitoli/1_introduzione.tex), 41 righe.
- [Matrice, capitolo 1](../../allineamento.md#capitolo-1) e
  [indice concordato](../../indice.md#1-introduzione).

Le righe nella tabella si riferiscono ai sorgenti, non al testo estratto
dal PDF. «Conservato» riguarda il contenuto espositivo; «adattato» descrive
la differenza dall'originale già risolta nella prima stesura.

## Corrispondenza di tutti i blocchi

| Blocco originale | Stesura attuale | Trattamento e motivazione | Fonti attuali |
| --- | --- | --- | --- |
| Titolo Introduzione, r. 1 | r. 1 | Conservati titolo, ordine e `ch:introduzione`. | Indice e matrice, voce 1. |
| Descrizione, r. 3 | r. 3 | Conservata la sezione 1.1. | Indice e matrice, voce 1.1. |
| Applicazione desktop, attività e catalogo comune, r. 4 | rr. 4, 6, 15 | Conservati contesto e collegamento tra disponibilità e operazioni. Precisato il punto di vista della concessionaria e distinta la richiesta dalla registrazione economica; mantenuta la funzione esplicativa con l'esempio acconto → blocco → vendita conclusa. | F01, F03, F06, F07, F08, F09, F12, F13, F15. |
| Tre figure operative, r. 6 | r. 8 | Conservati i tre ruoli e le aree dedicate; i nomi leggibili sostituiscono i codici nell'elenco, senza perdere alcun ruolo. | F03, F04, F05, F06. |
| Cliente, r. 8 | r. 10 | Conservate registrazione, catalogo, test drive, noleggio, acconto, proposta del proprio veicolo, consultazione e conto. Esplicitati saldo dopo approvazione e gestione profilo. | F04, F07, F08, F09, F10, F11. |
| Venditore, r. 9 | r. 11 | Conservati esito della proposta, autorizzazione della vendita e cambio destinazione. Adattata la consultazione: la dashboard espone richieste in sospeso e inventario; non offre la vecchia panoramica di noleggi e test drive. Il dettaglio resta al ticket 10, senza inventare una funzione. | F05, F07, F10, F11. |
| Manager, r. 10 | r. 12 | Conservati marche, modelli, veicoli, sconti e consultazione delle operazioni. Precisati scelta della base e registro economico con vendite, acquisti e penali. | F06, F12, F13, F14, F15, F20. |
| Informazioni condivise e operazioni per ruolo, r. 13 | rr. 8–15 | Conservato il significato nell'introduzione ai ruoli e nel paragrafo sulla disponibilità, con un esempio concreto; non eliminato per la sola assenza della frase letterale. | F02, F03, F04, F05, F06, F07. |
| Stack tecnologico, r. 15 | r. 18 | Conservata la sezione 1.2. | Indice e matrice, voce 1.2. |
| Linguaggio e interfaccia, r. 17 | rr. 21–22, 28 | Conservati Java 21, JavaFX 21.0.2 e CSS. FXML e controller sostituiti dalle viste costruite in Java; spiegati servizi, caricamenti asincroni e composizione esplicita senza anticipare i dettagli del capitolo 3. | F02, F03, F04, F05, F06, F16, F17. |
| Persistenza, r. 18 | r. 23 e r. 37 | Conservato JDBC/PostgreSQL; sostituito il fallback H2 con PostgreSQL 16 e predisposizione separata. Precisati driver 42.7.3, SQL nei DAO, commit/rollback e quattro enum nativi. | F02, F11, F15, F18, F19, F21, F22. |
| Build e verifica, r. 19 | rr. 24–25 | Conservati Maven e JUnit, con versione 5.10.2. Mockito e JaCoCo non sono più dipendenze; il testo descrive la distinzione reale tra prove senza DB e integrazione PostgreSQL, senza attribuire una misura di copertura. Avvio Windows Docker e server separato entrambi riscontrati. | F02, F18, F19, F23. |
| Semplificazioni e premessa, rr. 22–23 | rr. 31–32, 41 | Conservati sezione 1.3, `sec:semplificazioni` e obiettivo didattico. La chiusura chiarisce il valore dimostrativo dei flussi. | Indice e matrice, voce 1.3; F18. |
| Pagamenti interni, r. 25 | r. 34 | Conservati conto simulato, acconti, saldi, noleggi e rimborsi. Precisati saldo persistito e IBAN dimostrativo; nessuna integrazione bancaria attribuita. | F07, F08, F10, F11, F15, F21. |
| Account dello staff, r. 26 | r. 35 | Adattato il limite: UI e servizio registrano soltanto clienti e relativo conto. Staff predisposto nel DB; manca un percorso applicativo di amministrazione del personale. I dati demo comprendono i tre ruoli. | F04, F11, F22. |
| Modalità dimostrativa e durata dei dati, r. 27 | r. 37 | Recuperata interamente la funzione dell'argomento: spiegare prerequisiti, inizializzazione e persistenza tra sessioni. Il fallback H2 e la perdita dei dati alla chiusura non sono attuali: il Java richiede PostgreSQL già predisposto, lo script Windows inizializza soltanto il proprio nuovo DB e mantiene il volume; i volumi esistenti non sono reinizializzati automaticamente. | F02, F18, F19, F21. |
| Processi aziendali esclusi, r. 28 | r. 38 | Conservati tutti gli ambiti: sedi, fornitori, officina, consegna fisica, contratti, fatturazione e assistenza successiva. «Gestione documentale dei contratti» evita di negare l'accordo economico effettivamente modellato. | F02, F15, F18. |

Nessun blocco originale è privo di corrispondenza. Nell'introduzione originale
non ci sono figure, tabelle, schede, `lstlisting` o `input` da recuperare.
Gli esempi e i frammenti in linea (`CLIENT`, `SALESMAN`, `MANAGER`,
`BankAccount`) hanno la stessa funzione di riconoscimento nel testo attuale;
non erano esempi di codice eseguibile. I nomi dei ruoli sono resi in italiano,
il conto è spiegato in termini applicativi. Le sezioni e i blocchi rimangono
nell'ordine originale; il conto precede il personale e la predisposizione DB
resta tra le semplificazioni, con i processi ridotti esplicitamente presenti.

## Approfondimenti già validi conservati

Il testo corrente aggiunge chiarimenti pertinenti all'implementazione:
terminologia vendita/cessione, separazione tra richiesta e registro,
esempio di disponibilità, base manuale nella stessa formula e totale
concordato all'acconto del 20%. Sono riscontrati sugli ADR 0001–0002 e
sui servizi correnti; non costituiscono nuove funzionalità di questo ticket.
L'introduzione resta autonoma e rimanda la discussione dettagliata di
architettura, algoritmi e prove ai capitoli successivi.

## Fonti e riscontri

I percorsi sono relativi alla radice del repository. Impronte e dimensioni
delle fonti sono in `fonti.json`; i numeri seguenti identificano intervalli
letti staticamente, senza eseguire applicazione, SQL o test.

| ID | Fonte | Riscontro |
| --- | --- | --- |
| F01 | `docs/relazione/GLOSSARY.md` | Punto di vista della concessionaria, vendita e cessione. |
| F02 | `pom.xml` | Java source/target 21, JavaFX 21.0.2, JDBC 42.7.3, JUnit 5.10.2, plugin JavaFX e Surefire; profilo `postgres-tests`; assenza di FXML/H2/Mockito/JaCoCo/ORM tra le dipendenze. |
| F03 | `src/main/java/it/unifi/drivehub/App.java:34–122`, `AppContext.java:76–120` | Login iniziale, dashboard dopo autenticazione, CSS; costruzione di DAO PostgreSQL e servizi con dipendenze esplicite. |
| F04 | `presentation/view/ClientDashboardView.java:110–150,178–250,359–475` e `LoginView.java:51–109,134–149` sotto il package Java principale | Operazioni del cliente, tab, dialoghi, conto e profilo; form del solo cliente. |
| F05 | `presentation/view/SalesmanDashboardView.java:94–172,179–240,247–292` | Due tab, esiti di richieste SALE/PURCHASE e cambio destinazione; nessuna panoramica prenotazioni. |
| F06 | `presentation/view/ManagerDashboardView.java:78–100,162–350,366–490` | Catalogo, base, sconti e registro economico; gestione account comune. |
| F07 | `service/PurchaseService.java:53–96,129–183` | Acconto, blocco, autorizzazione e completamento con residuo dal totale persistito. |
| F08 | `service/RentalService.java` | Prenotazione per periodo, disponibilità, addebito e cambio destinazione con rimborsi. |
| F09 | `service/TestDriveService.java` | Disponibilità e conflitti temporali delle prenotazioni. |
| F10 | `service/SaleToDealershipService.java` | Proposta, stima, esito del venditore e registrazione della cessione. |
| F11 | `service/AuthService.java:53–64,96–110,122–133` | Registrazione cliente/conto nella stessa transazione, saldo persistito e IBAN generato. |
| F12 | `service/PriceCalculatorService.java:26–85` e `domain/vehicle/Vehicle.java` | Stessa formula per base selezionata e modello; sconti e svalutazione; acconto 20%. |
| F13 | `domain/operation/Request.java` e `SaleType.java` | Accordo e residuo, distinzione tra richiesta e registrazione SALE/PURCHASE/PENALTY. |
| F14 | `docs/adr/0001-base-manuale-e-prezzo-concordato.md` | Decisione sulla base manuale e sul totale fissato all'acconto. |
| F15 | `src/main/resources/db/schema.sql:36–39,150–195,240–308` | Enum, utenti, conto, richieste, prenotazioni e registro; vincoli economici e temporali. |
| F16 | `presentation/view/AsyncLoad.java:49–77` | Task in thread virtuale e callback JavaFX sui controlli. |
| F17 | `src/main/resources/css/style.css` | Stile condiviso caricato da App. |
| F18 | `README.md:5–16,24–33,50–111,121–139` | Avvio Windows o PostgreSQL separato; predisposizione una tantum, persistenza, build e test. |
| F19 | `run.bat` e `run.ps1:1–4,27–73` | Avvio con immagine `postgres:16`; schema/demo solo sul nuovo contenitore con volume nuovo, protezione del volume esistente. |
| F20 | `docs/adr/0002-enumerazioni-native-postgresql.md` | Quattro domini nativi; conversione storica e script ritirato. |
| F21 | `dao/DatabaseManager.java:24–51,66–105` e `dao/postgres/PostgresBankAccountDao.java` | JDBC diretto, configurazione, commit/rollback e salvataggio del saldo; nessun bootstrap alternativo nel gestore o in AppContext. |
| F22 | `src/main/resources/db/demo.sql:153,171,189,196` | Inserimenti distinti per clienti, conti, venditori e manager. |
| F23 | `docs/testing.md:33–53,57–106` | Classificazione delle prove, profilo e credenziali di test dedicate. Solo lettura delle istruzioni, nessuna nuova esecuzione. |

Per i percorsi abbreviati F04–F13, F16 e F21, il prefisso Java è
`src/main/java/it/unifi/drivehub/`. Le conclusioni di assenza si basano su
dipendenze, composizione, viste e schema correnti, non su una presunta
esecuzione completa dei flussi.
