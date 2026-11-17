# Indice dettagliato del riallineamento

Proposta operativa del 16 novembre 2026, successiva alla prima stesura completa. Il ticket 09 ha verificato il capitolo 1 con il solo riordino dei due ultimi punti di 1.3; il ticket 10 ha riallineato il capitolo 2 e le appendici A–B, rispettando tutte le 47 voci. Il ticket 11 ha riallineato il capitolo 3 il 17 novembre: 35 voci, quattordici
frammenti originali coperti e 27 estratti correnti verificati; [confronto e
review 11](verifiche/ticket-11/README.md). Il ticket 12 ha riallineato il testing nelle quindici voci originali:
[confronto, nove frammenti, fonti e review](verifiche/ticket-12/README.md).
Il PDF completo ha 151 pagine; il ticket 13 ha eseguito compilazione integrata,
osservazione di tutte le pagine e ricompilazione dello ZIP estratto, con
review finali Standards/Spec chiuse senza rilievi: ticket **done**,
[verifiche 13](verifiche/ticket-13/README.md). Nessuna approvazione degli autori presunta.
La [corrispondenza consolidata](verifiche/ticket-13/corrispondenza-102.md)
riscontra livelli, ordine, contenuto e fonti di ogni voce.
[Confronto e verifiche 09](verifiche/ticket-09/README.md), [confronto e verifiche 10](verifiche/ticket-10/README.md). Gli autori chiedono di rifare la relazione vecchia mantenendo tutti i contenuti e la loro scansione, riadattati esattamente all'implementazione attuale: spiegazioni, schede, esempi, frammenti, figure e tabelle, oltre a capitoli e titoli.

La [matrice di corrispondenza](allineamento.md) collega tutte le 102 voci originali alle fonti attuali, alle destinazioni e agli adattamenti. L’ordine resta quello degli input di `Relazione_SWE/main.tex`; un titolo cambia solo per descrivere correttamente l’implementazione attuale. Il capitolo 5 resta l’aggiunta sull’AI.

## Frontespizio e indice

Federico D’Amodio e Matteo Orlandi; Ingegneria del Software, prof. Enrico Vicario, A.A. 2025/2026. Conservare le convenzioni LaTeX attuali e i riferimenti interni; le sottosottosezioni vanno presenti nel testo anche quando non compaiono nell’indice del PDF.

## 1. Introduzione

Contesto, obiettivi e tre ruoli; introduzione autonoma, senza anticipare la discussione tecnica dei capitoli successivi.

### 1.1 Descrizione

Catalogo comune, responsabilità di cliente, venditore e manager, distinzione fra richieste e operazioni concluse.

### 1.2 Stack tecnologico

Java 21, JavaFX costruito in Java, CSS, JDBC, PostgreSQL e Maven; strumenti di verifica reali, senza FXML, H2, Mockito o JaCoCo non più presenti.

### 1.3 Semplificazioni adottate

Conto simulato, registrazione dei soli clienti, personale predisposto, processi aziendali ridotti e inizializzazione esterna del DB; preservare i limiti attuali.

## 2. Analisi dei requisiti e progettazione

Stesse otto sezioni e stesso percorso dall'obiettivo utente a interfaccia, progetto e dati; riportare nel corpo il contenuto principale oggi in appendice.

### 2.1 Diagramma dei casi d'uso

Tavola degli autori corretta e dettagli per ruolo; attori, obiettivi e supporto, registrazione del solo cliente e assenza della panoramica prenotazioni del venditore.

### 2.2 Template dei casi principali

Sei sottosezioni con schede complete nel corpo, nello stesso ordine originale; campi, flusso numerato, alternative e riscontri delle prove. Le schede aggiuntive restano in A.

- **2.2.1 Prenotare un test drive.** Scheda completa del test drive: attore, condizioni, fascia oraria effettiva, limite di tre prenotazioni, conflitti, cancellazione e prove pertinenti.

- **2.2.2 Noleggiare un veicolo.** Scheda completa del noleggio: estremi inclusi, costo ricalcolato, saldo, conflitti, annullamento dei test drive futuri e rollback.

- **2.2.3 Richiedere e completare una vendita al cliente.** UC-03: acconto del 20%, totale fissato, autorizzazione, saldo, rifiuto e scadenza con penale; sostituire le vecchie anomalie di prezzo con il comportamento corretto attuale.

- **2.2.4 Proporre un veicolo alla concessionaria.** UC-05 prima di UC-04 nell'esposizione, conservando gli identificativi: proposta, stima persistita, esito del venditore, storico e limite dell'accredito zero.

- **2.2.5 Autorizzare una richiesta di vendita al cliente.** UC-04: approvazione o rifiuto del venditore, scadenza e rimborso; distinguere autorizzazione e saldo senza rinumerare il caso d'uso.

- **2.2.6 Gestire lo sconto su un veicolo.** Scheda UC-06: sconti su modello e veicolo, intervallo 0–50, massimo non cumulato, inserimento/rimozione e accordi già fissati.

### 2.3 Mockups

Stessi quattro gruppi e dettagli per funzione; screenshot correnti con spiegazione di input, azione, risultato e limiti, evitando immagini storiche presentate come attuali.

- **2.3.1 Autenticazione.** Distinguere ingresso, accesso e registrazione pur condividendo LoginView; riusare le immagini 01–02 senza duplicazioni inutili.

  - **2.3.1.1 Schermata iniziale.** La vista iniziale è LoginView; dichiarare l'assenza di una Welcome Page separata e richiamare lo screenshot dell'accesso.

  - **2.3.1.2 Accesso.** Email, password, esito ed ingresso nella dashboard del ruolo; screenshot e gestione dell'errore, senza scelta pubblica del ruolo.

  - **2.3.1.3 Registrazione.** Anagrafica e credenziali del solo cliente, creazione del conto e risultato; personale già nel DB, senza simulare un form staff.

- **2.3.2 Area Customer.** Separare le sei funzioni originali, descrivendo tab e dialoghi attuali; la stessa schermata può essere richiamata da più argomenti.

  - **2.3.2.1 Catalogo veicoli.** Cataloghi vendita/noleggio, dati mostrati, prezzi, selezione del veicolo e comandi disponibili.

  - **2.3.2.2 Vendite al cliente e richieste.** Richieste aperte, totale/acconto/residuo, saldo dopo autorizzazione e consultazione delle proprie operazioni; distinguere il registro economico.

  - **2.3.2.3 Noleggi e test drive.** Dialoghi di test drive e noleggio, data/ora o intervallo, costo, prenotazioni del cliente e annullamento del test drive.

  - **2.3.2.4 Proposta di cessione del proprio veicolo.** Form, valutazione e invio della proposta; la concessionaria acquista dal cliente e il venditore decide l'esito.

  - **2.3.2.5 Conto dimostrativo.** Saldo e ricarica demo dal comando disponibile; non descrivere una scheda bancaria o un prelievo autonomo ereditati dalla vecchia UI.

  - **2.3.2.6 Profilo.** Gestione Account: email, password ed eliminazione con conferma; dialogo comune invece di una scheda Profilo separata.

- **2.3.3 Area Salesman.** Riprendere le cinque funzioni originali distinguendo richieste, inventario, gestione account e consultazione non implementata.

  - **2.3.3.1 Proposte di cessione in attesa.** Le richieste PURCHASE sono nella stessa tabella delle SALE; mostrare filtro/tipologia e approvazione/rifiuto senza inventare una tab separata.

  - **2.3.3.2 Richieste di vendita al cliente.** Autorizzazione/rifiuto delle SALE, dati economici e successivo saldo; richiamare la stessa figura delle richieste.

  - **2.3.3.3 Stato dei veicoli.** Cambio SALE/RENT, rifiuto con noleggio in corso e rimborsi dei futuri; distinguere la destinazione dalle altre transizioni di stato.

  - **2.3.3.4 Panoramica prenotazioni: limite attuale.** Conservare il punto del percorso originale spiegando che il venditore non dispone della vecchia panoramica; niente screenshot storico o nuova funzionalità.

  - **2.3.3.5 Profilo.** Descrivere il dialogo comune per il venditore e i vincoli dell'identità; riscontrare l'apertura dalla sua dashboard, senza presumere uno screenshot dedicato già disponibile.

- **2.3.4 Area Manager.** Riprendere parco veicoli, marche/modelli, registro e profilo; gli sconti della UI attuale sono una scheda distinta ma restano spiegati presso le rispettive funzioni.

  - **2.3.4.1 Parco veicoli.** Veicoli, base automatica/manuale, prezzi, inserimento/rimozione e sconto individuale con rinvio alla scheda Sconti.

  - **2.3.4.2 Marche e modelli.** Due tab attuali per marchi e modelli; base modello, anno, manutenzione e sconto modello, senza confonderli con lo sconto sul veicolo.

  - **2.3.4.3 Registro delle operazioni.** SALE/PURCHASE/PENALTY con importi non negativi e punto di vista della concessionaria; i noleggi non sono automaticamente righe di Sale.

  - **2.3.4.4 Profilo.** Email, password ed eliminazione del manager nel dialogo condiviso; verificare la vista corrente e riusare le figure solo se rappresentative.

### 2.4 Page Navigation Diagram

Tavola degli autori corretta e percorsi dei tre ruoli; distinguere Scene, tab, dialoghi, annullamento, logout e rientro dopo eliminazione dell'identità.

### 2.5 Package Diagram

Undici package di produzione, composizione e dipendenze reali; motivazione dei confini e limiti della separazione, con tavola PKG nel corpo.

- **Il dominio non dipende dagli altri livelli (sottosezione non numerata).** Riprendere la sottosezione non numerata: nessun import verso gli altri livelli applicativi, ma contratto di navigazione degli utenti; non attribuire indipendenza concettuale assoluta.

### 2.6 Class Diagram

Mantenere i quattro gruppi originali; i quattro diagrammi originali rifatti/riadattati nel corpo, uno per gruppo e tutte le tavole di dettaglio in B, coprendo gli undici package.

- **2.6.1 Domain Model.** Gerarchia utenti, conto, catalogo, sconti e operazioni; relazioni per ID distinte da riferimenti reali e invarianti spiegate.

- **2.6.2 Business Layer.** Servizi, composizione, prezzi, sicurezza ed eventi; rifare/riadattare la tavola originale dei servizi nel corpo oltre ai dettagli dell'Observer.

- **2.6.3 DAO.** Contratti DAO, implementazioni e DatabaseManager; tavola originale rifatta/riadattata nel corpo, connessione esplicita e assenza delle vecchie factory/UnitOfWork.

- **2.6.4 Presentation Layer.** Viste JavaFX, dialoghi, AsyncLoad e collegamento con App; tavola corrente nel corpo al posto dei controller FXML.

### 2.7 Sequence Diagram

Stessi tre flussi originali; rifare/riadattare le tre sequenze originali con la skill draw.io, usando i dettagli correnti come approfondimento, spiegando transazioni e alternative senza moltiplicare i casi principali.

- **2.7.1 Vendita di un veicolo al cliente.** Acconto, esito, saldo e scadenza; totale persistito, ruoli e lock attuali, senza ripetere le vecchie anomalie come comportamento corrente.

- **2.7.2 Prenotazione di un noleggio.** Disponibilità, periodo incluso, costo, saldo, cancellazione Observer e confine transazionale del noleggio.

- **2.7.3 Acquisto di un veicolo dal cliente.** Proposta e decisione del venditore, stima persistita e storico, attribuzione corretta senza vecchia UnitOfWork annidata o venditore costante.

### 2.8 Database

Conservare ER, modello relazionale e utenti/vincoli; mantenere distinta la spiegazione del dato dal mapping implementativo di 3.4.

- **2.8.1 Schema ER.** Tavola degli autori corretta, entità e cardinalità nei due versi, generalizzazione concettuale User distinta dal DDL.

- **2.8.2 Modello relazionale.** Tredici relazioni e tutti gli attributi, PK/FK, unicità, nullabilità e legenda; recuperare una lettura testuale dello schema accanto alle tavole correnti.

- **2.8.3 Utenti, vincoli e semplificazioni.** Tre tabelle e vista user, sequenza condivisa con PK locali, enum nativi, vincoli temporali/economici, limiti email e predisposizione separata del DB.

## 3. Implementazione

Stesse cinque sezioni e stessa progressione interna; riusare gli estratti validi e recuperare spiegazioni e frammenti significativi oggi accorpati.

### 3.1 Domain Model

Passare dall'organizzazione per invarianti/gerarchie ai cinque argomenti originali; invarianti e identità si spiegano nelle entità cui appartengono.

- **3.1.1 Users.** User, AbstractUser, Client/Salesman/Manager e BankAccount: gerarchia reale, conto opzionalmente caricato, invarianti e identità per ID/ruolo.

- **3.1.2 Vehicles.** Brand, Model, Vehicle, PriceMode/VehicleState e gerarchia Discount; validazione prima della mutazione, ID e dati modificabili, stato locale distinto dai controlli concorrenti.

- **3.1.3 Rentals.** Rent e TestDrive nel package operation: date, importi e fascia oraria effettiva; nessuna pretesa che le sole entità impediscano conflitti persistenti.

- **3.1.4 Sales.** Request/RequestType e Sale/SaleType: accordo totale/acconto, stima, attribuzioni opzionali e importi non negativi; le tipologie non dipendono più dal segno.

- **3.1.5 Eventi e contratto Observer.** Presentare evento, observer e publisher nel package event, senza dichiararli parte di domain; l'effetto concreto e la transazione si approfondiscono in 3.2.6.

### 3.2 Business Layer

Ordine originale: coordinamento transazionale, responsabilità, quattro flussi, prezzi, identità/conto e Observer; estratti legati al caso spiegato.

- **3.2.1 Coordinamento e transazioni.** DatabaseManager e callback Connection: commit, rollback, causa ed eccezioni soppresse; niente TransactionRunner o contratto UnitOfWork assenti.

- **3.2.2 Services: responsabilità.** Responsabilità di Auth, Catalog, OperationQuery, PriceCalculator, Purchase, Rental, SaleToDealership e TestDrive; composizione esplicita e dipendenze reali.

- **3.2.3 Services: flussi applicativi.** Quattro sottosottosezioni con sequenza applicativa, controlli, scritture, alternative ed esito; i rinvii ai diagrammi completano la spiegazione e non la sostituiscono.

  - **3.2.3.1 Catalogo e test drive.** Letture catalogo e dati preparati; prenotazione test drive con fascia reale, limite attivi, idoneità e conflitti. Recuperare un frammento corrente del controllo significativo.

  - **3.2.3.2 Noleggio con saldo del conto.** Costo geometrico sui giorni inclusi, lock veicolo/cliente/conto, conflitti, addebito, salvataggio e Observer; distinguere prenotazione e cambio destinazione con rimborsi.

  - **3.2.3.3 Richiesta di vendita al cliente, saldo e penale.** Acconto del 20%, totale fissato, approvazione, saldo, rifiuto con rimborso e scadenza con penale del 20% dell'acconto; controlli nel servizio e limiti delle letture.

  - **3.2.3.4 Proposta di cessione del veicolo.** Proposta, preventivo/stima salvata, verifica VIN e ultima vendita al proponente, accettazione o rifiuto, accredito e storico; dichiarare il limite degli importi zero.

- **3.2.4 Calcolo dei prezzi e sconti.** PriceCalculatorService concreto; base automatica/manuale, massimo sconti, deprezzamenti, acconto, noleggio e stima con formule/esempi attuali. Nessuna strategia intercambiabile inventata.

- **3.2.5 Autenticazione e conto dimostrativo.** AuthService e PasswordHasher/PBKDF2, registrazione del cliente e conto, ricarica e gestione dell'identità; saldo protetto e limiti dei controlli autorizzativi.

- **3.2.6 Observer e gestione dell'inventario.** TestDriveCancellationObserver sulla stessa Connection prima del commit: cancella tutti i test drive futuri, propaga errori e può causare rollback; differenza dal vecchio listener che ignorava eccezioni.

### 3.3 Presentation Layer

Stesse tre responsabilità narrative, aggiornate dalle classi FXML/gateway a viste Java, servizi diretti e AsyncLoad.

- **3.3.1 Viste e aree applicative.** LoginView e tre dashboard, AccountManagementDialog e UiUtils; raccolta input, controlli e comandi per ruolo, senza AbstractController o FXML.

- **3.3.2 Creazione delle viste e navigazione.** App e AppContext passano collaboratori e logout; openDashboard/UserDashboardNavigator selezionano il ruolo con double dispatch, senza Route o sessione globale.

- **3.3.3 Servizi, dati della UI e aggiornamento.** Invocazioni dirette, VehicleRow e letture collettive; AsyncLoad, risposta superata, uscita dalla Scene, errore/Riprova, scritture ancora sincrone e refresh dopo commit.

### 3.4 Persistenza: livello DAO e mapping oggetti/tabelle

Recuperare cinque sottosezioni: porte, utenti, operazioni, vincoli e connessione; riusare gli estratti JDBC e aggiungere spiegazioni mirate dove mancanti.

- **3.4.1 Porte DAO e implementazioni JDBC.** Dieci contratti DAO e Postgres*Dao, PreparedStatement, overload con/senza Connection, ritorni e numero di righe modificate; esempio attuale di contratto.

- **3.4.2 Mapping degli utenti e vista unificata.** Vista UNION ALL, ricostruzione del sottotipo, selezione chiusa della tabella di ruolo, ID condivisi per sequenza e limiti delle PK/email.

- **3.4.3 Mapping delle operazioni e valori opzionali.** Request e Sale: tipi espliciti, agreed_total, BigDecimal, date/timestamp e FK nullable; letture e scritture simmetriche, senza codifica economica tramite segno.

- **3.4.4 Vincoli SQL e controlli dei servizi.** Estratti SQL attuali di unicità, CHECK, EXCLUDE e enum/cast; distinguere regole SQL da stato, titolarità, accordo e autorizzazione nei servizi.

- **3.4.5 Connessione condivisa e confine transazionale.** Overload che usano realmente Connection, proprietà di commit/close e letture autonome; nessuna identity map o UnitOfWork del vecchio progetto.

### 3.5 Idiomi e Design Pattern

Sei sottosezioni nello stesso ordine, con problema, collaboratori, frammento/rinvio significativo e limite; distinguere GoF, idiomi e tecniche.

- **3.5.1 Observer.** Publisher, contratto e observer concreto; notifica sincrona e condivisione della Connection, errori e assenza di consegna persistente.

- **3.5.2 Calcolo centralizzato e assenza di Strategy.** Preservare la discussione della variabilità degli algoritmi: oggi un calcolatore concreto, senza interfaccia PricingStrategy né selezione intercambiabile. Non attribuire GoF Strategy.

- **3.5.3 Metodi statici di creazione.** Factory di Vehicle, Request e Sale, validazione e ricostruzione; costruttori effettivi e distinzione da Factory Method GoF.

- **3.5.4 Navigazione e contesto applicativo.** Double dispatch nello stile Visitor per il ruolo e Singleton predefinito AppContext con costruttore alternativo; non ripristinare NavigationManager globale.

- **3.5.5 DAO e coordinamento transazionale.** Porte SQL e callback DatabaseManager sulla stessa connessione, DI manuale e dipendenze JDBC; spiegare che il vecchio contratto UnitOfWork non c'è più.

- **3.5.6 Idiomi Java usati nei confini tra livelli.** Optional dove realmente usato, record di lettura, enum, campi finali, lambda/callback, try-with-resources e Task; niente UiModels o RegistrationRequest rimossi.

## 4. Testing

Stesse cinque sezioni e stessi argomenti interni, con classi e prove attuali; motivazione, preparazione, comando, oracolo, estratto e limite per i casi significativi.

### 4.1 Strategia di verifica

Rischi economici, livelli, test funzionali e guidati dalla struttura, oracoli e attribuzione dei risultati; separare test, harness e controlli documentali.

- **4.1.1 Tecnologie e strumenti.** JUnit, Surefire, Maven, PostgreSQL dedicato e supporto TestDatabase; profilo postgres-tests, harness JavaFX esterni alla suite, niente H2/Mockito/JaCoCo presunti.

### 4.2 Livello 1: Business Logic

Recuperare i sei argomenti originali senza classificare come unitari i test PostgreSQL; dove manca un test isolato, indicarlo e rinviare alla prova di integrazione.

- **4.2.1 Invarianti del dominio.** DomainInvariantsTest e BookingRulesTest: confini, stato invariato dopo input rifiutato, conto, sconti, anni, date e fascia oraria.

- **4.2.2 Autenticazione e identità.** Conservare l'argomento della vecchia AuthServiceTest; non esiste un equivalente unitario dedicato. Spiegare il confine e rinviare ai test PostgreSQL di email, credenziali e cancellazione.

- **4.2.3 Prezzi e sicurezza.** Estratti discriminanti su formula/base/sconti/centesimi; per password e identità indicare le prove effettive e l'eventuale assenza di un test isolato PBKDF2, senza ricreare PricingAndSecurityTest.

- **4.2.4 Richieste, vendite e accordo economico.** RequestAgreementTest e SaleTypeTest per il dominio; PurchaseAgreement/SaleOutcome/SaleProposalIntegrationTest per scritture ed esiti, chiaramente classificati nel livello successivo.

- **4.2.5 Noleggio e conto dimostrativo.** BookingRulesTest e DomainInvariantsTest per regole locali; BookingConcurrency, BalanceConcurrency e VehicleDestinationIntegrationTest per integrazione, conflitti e rimborsi.

- **4.2.6 Transazioni e Observer.** Dichiarare l'assenza della vecchia TransactionAndObserverTest; mostrare il contratto e rinviare alle riletture PostgreSQL di TestDriveCancellationIntegrationTest e ai guasti dopo scritture effettive.

### 4.3 Livello 2: Persistence Layer (DAO)

Restituire visibilità a mapping/DAO e preparazione schema/demo, mantenendo il contenuto aggiuntivo su rollback, concorrenza e isolamento del DB di test.

- **4.3.1 DAO e mapping su PostgreSQL.** PriceSelection, PurchaseAgreement, SaleOutcome e PostgresEnumIntegrationTest: salvataggio e rilettura, tipi e null; non descrivere la vecchia PostgresDaoIntegrationTest su H2.

- **4.3.2 Schema e dati dimostrativi.** DemoSqlValidationTest e TestDatabase: tredici tabelle, righe e sequenze dopo ricaricamenti; DatabaseIsolationIntegrationTest e configurazione dedicata. Nessun bootstrap automatico dell'applicazione.

### 4.4 Verifiche funzionali e della presentazione

Esplicitare scenari di registrazione/accesso, catalogo/sconti, flussi commerciali, GUI e tabelle; estratto funzionale attuale e fonti degli harness. Non rinominare FullWorkflow come prova di ogni workflow.

### 4.5 Esecuzione della suite e portata dei risultati

Comandi e ambienti correnti, risultati storici con versione/data, suite standard distinta dal profilo PostgreSQL e dagli harness; nessuna percentuale di coverage o nuova esecuzione dedotta dalla riscrittura.

## 5. Uso dell’AI

Conservare il capitolo attuale e l’attribuzione delle attività, aggiornando i riferimenti coinvolti e descrivendo questo riallineamento senza inventare approvazioni, prompt, metriche o nuove prove applicative.

- **5.1 Progettazione e sviluppo iniziale manuali.** Dichiarazione degli autori sulla progettazione e sulle prime classi.
- **5.2 Revisione assistita da Codex.** Attività ricostruibili dalle tracce, con versione e provenienza.
- **5.3 Verifica e controllo umano.** Distinguere contributi, decisioni e risultati storici dalla revisione degli autori.
- **5.4 Documentazione, diagrammi e limiti.** Includere la relazione e il riallineamento; dichiarare i limiti delle fonti.

## Appendici

- **A — Schede aggiuntive dei casi d’uso.** UC-07–11 e UC-S01–04. Le sei schede principali tornano integralmente in 2.2; gli identificativi e i rimandi si conservano, senza copie divergenti.
- **B — Tavole delle classi.** Copertura completa degli undici package e dettagli delle viste significative già spiegate in 2.6.
- **C — Matrice delle verifiche.** Requisiti, flussi, test, risultati, provenienza e limiti. Il capitolo 4 mantiene esempi argomentati e istruzioni di esecuzione.

## Figure ed esempi

Usare sempre la skill `drawio:drawio` per tutti i diagrammi. Rifare/riadattare gli undici diagrammi originali nelle rispettive sezioni, partendo da SWE ER, Users SWE, Page Navigation Diagram e Package diagram degli autori, secondo i [vincoli operativi](allineamento.md#diagrammi-vincoli-operativi). Le copie corrette e le viste attuali sono basi da confrontare e adattare; gli screenshot rappresentano l'interfaccia corrente. Il [censimento di figure, tabelle e 23 frammenti originali](allineamento.md#frammenti-ed-esempi-da-conservare-per-funzione) fissa tutti i contenuti da conservare e aggiornare.

Ogni gruppo di classi ha una tavola originale rifatta/riadattata nel corpo. Le sequenze conservano tre flussi principali. Schema ER e modello relazionale restano accompagnati da cardinalità, attributi, chiavi e legenda. Le formule dei prezzi, la tabella sull’origine degli importi e quella sulle garanzie recuperano la funzione esplicativa dell’originale con dati e limiti correnti.

Ogni tavola, rifatta, adattata o aggiuntiva, conserva un sorgente `.drawio` modificabile e l'esportazione verificata nel PDF; si applica sempre la relativa skill. Le acquisizioni UI rappresentano solo funzioni implementate; l’assenza di una funzione è spiegata nel punto corrispondente.
