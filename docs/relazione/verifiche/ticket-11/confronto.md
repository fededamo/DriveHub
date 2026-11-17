# Confronto originale / nuovo — ticket 11

16 novembre 2026. Confronto degli otto file originali attivi (capitolo e sette input), della stesura iniziale conservata nella baseline e del codice corrente. Non è una verifica limitata ai titoli. Il capitolo nuovo non usa input esterni; tutti gli estratti sono incorporati nel LaTeX.

## Contenuti e gerarchia

35 voci, stessi livelli e ordine; le quattro sottosottosezioni dei flussi sono numerate fino a 3.2.3.4 senza cambiare la profondità dell’indice PDF. I titoli dei componenti rimossi sono adattati. [Confronto strutturale ripetibile](copertura-strutturale.json).

| Voce | Originale → attuale | Contenuto, riuso e adattamento |
| --- | --- | --- |
| 3 | Implementazione → Implementazione | Conservati introduzione, rinvii e testo valido; ricomposizione nelle cinque sezioni, estratti letterali e limiti dichiarati. |
| 3.1 | Domain Model → Domain Model | Recuperati i cinque argomenti originali; package attuali distinti dai gruppi espositivi. |
| 3.1.1 | Users → Users | Recuperati utenti e conto: sottotipi, identità, normalizzazione, caricamento opzionale, invarianti locali e persistenza. |
| 3.1.2 | Vehicles → Vehicles | Riusati invariante Vehicle e distinzione ID/mutazioni; recuperati Brand, Model, Discount e stati. Adattate le vecchie transizioni alla validazione locale e UPDATE condizionale. |
| 3.1.3 | Rentals → Rentals | Recuperati Rent/TestDrive: giorni inclusi, fascia reale, istante anziché intervallo di un’ora, riferimenti ID e conflitti persistenti. |
| 3.1.4 | Sales → Sales | Riusato accordo Request; recuperate due factory e Sale con tipi espliciti, importi non negativi, FK opzionali e tolleranza temporale. |
| 3.1.5 | Observer → Eventi e contratto Observer | Ricollocato il contratto: evento/publisher/observer nel package event separato, dati effettivi e rinvio all’effetto concreto. |
| 3.2 | Business Layer → Business Layer | Ripristinato ordine coordinamento, responsabilità, quattro flussi, prezzi, identità/conto e Observer. |
| 3.2.1 | Core: unità di lavoro e transazioni → Coordinamento e transazioni | Riusato wrapper reale DatabaseManager e recuperate due callback distinte; Connection, commit, rollback tentato, cause, soppressa e close. |
| 3.2.2 | Services: responsabilità → Services: responsabilità | Recuperate responsabilità e collaboratori degli otto servizi, composizione AppContext e assenze motivate dei vecchi componenti. |
| 3.2.3 | Services: flussi applicativi → Services: flussi applicativi | Recuperati quattro flussi completi nel corpo; diagrammi come complemento. |
| 3.2.3.1 | Catalogo e test drive → Catalogo e test drive | Catalogo a letture collettive; test drive con lock, limite tre, fascia, conflitti, save, cancellazione del proprio e rollback. |
| 3.2.3.2 | Noleggio con saldo del conto → Noleggio con saldo del conto | Preventivo/conferma, query inclusiva e parametri reali; modello/sconti con conn, addebito, Rent, Observer, commit; destinazione e rimborsi distinti. |
| 3.2.3.3 | Richiesta di acquisto, saldo e penale → Richiesta di vendita al cliente, saldo e penale | Avvio con UPDATE condizionale e letture listino autonome; totale persistito, approvazione, saldo, rifiuto e scadenza separati, ordine delle scritture e transazioni. |
| 3.2.3.4 | Proposta di cessione del veicolo → Proposta di cessione del veicolo | Modello esistente, preventivo e stima salvata; VIN noto e ultima SALE al proponente; proposta/esito separati, accredito, storico, rifiuto e zero. |
| 3.2.4 | Strategies: prezzi e sconti → Calcolo dei prezzi e sconti | Riusati base selezionata e limiti precisione; recuperate formule, esempi ricalcolati e tabella origine importi. Nessuna Strategy; corretto esempio numerico prima della review. |
| 3.2.5 | Security e Banking: autenticazione e conto → Autenticazione e conto dimostrativo | Riusati PBKDF2, registrazione/conto, ricarica, identità/password/cancellazione, FK e limiti ID/email/autorizzazione. |
| 3.2.6 | Observer e gestione dell'inventario → Observer e gestione dell'inventario | Riusati publisher e spiegazione di TestDriveCancellationObserver, stessa conn prima del commit e errore propagato. |
| 3.3 | Presentation Layer → Presentation Layer | Ripristinate tre responsabilità narrative della presentazione attuale. |
| 3.3.1 | Controller e aree applicative → Viste e aree applicative | Recuperate aree, Login e dashboard, AccountManagementDialog e UiUtils; responsabilità UI rispetto ai servizi. |
| 3.3.2 | Creazione dei controller e navigazione → Creazione delle viste e navigazione | Riusato dispatch Client; recuperata costruzione reale in App al posto di FXMLLoader/ContextAware; contesto senza utente globale e callback logout. |
| 3.3.3 | Gateway, dati della UI e aggiornamento → Servizi, dati della UI e aggiornamento | Riusati integralmente VehicleRow, letture collettive, AsyncLoad, Task superato, errore/Riprova, Scene e aggiornamenti dopo commit; scritture sincrone distinte. |
| 3.4 | Persistenza: livello DAO e mapping oggetti/tabelle → Persistenza: livello DAO e mapping oggetti/tabelle | Ripristinati cinque argomenti DAO; dieci contratti e adapter effettivi. |
| 3.4.1 | Porte DAO e implementazioni JDBC → Porte DAO e implementazioni JDBC | Riusato UPDATE condizionale; recuperate firme UserDao con ruolo/Connection, PreparedStatement, ritorni, conteggi protetti e limiti. |
| 3.4.2 | Mapping degli utenti e vista unificata → Mapping degli utenti e vista unificata | Recuperati UNION ALL, mapper dei sottotipi e selezione chiusa della tabella; sequenza comune diversa da PK globale e limiti email; recuperata la traduzione di SQLSTATE23503 e il limite della tassonomia degli errori. |
| 3.4.3 | Mapping delle operazioni e valori opzionali → Mapping delle operazioni e valori opzionali | Riusati mapper Request e tabella enum/cast; recuperato mapper Sale completo e simmetria lettura/scrittura NULL, BigDecimal, date/timestamp. |
| 3.4.4 | Vincoli SQL e controlli dei servizi → Vincoli SQL e controlli dei servizi | Recuperati estratti SQL correnti, EXCLUDE e tabella garanzie/limiti; distinzione SQL/servizi/connessioni. |
| 3.4.5 | Connessione condivisa e Unit of Work → Connessione condivisa e confine transazionale | Riusati overload Vehicle con/senza conn; proprietà close/commit, letture autonome e assenza identity map/UnitOfWork. |
| 3.5 | Idiomi e Design Pattern → Idiomi e Design Pattern | Ripristinati sei temi numerati, separando GoF da tecniche e idiomi. |
| 3.5.1 | Observer → Observer | Observer GoF: problema, contratto corrente, collaboratori, esempio/rinvio ed errori/limiti. |
| 3.5.2 | Strategy → Calcolo centralizzato e assenza di Strategy | Adattata vecchia Strategy: calcolo concreto centralizzato e selezione base, senza algoritmo intercambiabile inventato. |
| 3.5.3 | Metodi statici di creazione → Metodi statici di creazione | Recuperato esempio factory Vehicle corrente, ID nullo e nuova istanza dopo INSERT; distinta da Factory Method GoF. |
| 3.5.4 | Istanza centrale per la navigazione → Navigazione e contesto applicativo | Riusato Singleton AppContext, recuperato problema del dispatch; contesto alternativo e limiti, senza NavigationManager. |
| 3.5.5 | DAO e Unit of Work → DAO e coordinamento transazionale | Riusati DAO/DI e confini JDBC, recuperato coordinamento con callback reali; vecchia UnitOfWork assente. |
| 3.5.6 | Idiomi Java usati nei confini tra livelli → Idiomi Java usati nei confini tra livelli | Recuperati Optional, record reali, enum, final, lambda/callback, try-with-resources e Task; nessun UiModels/RegistrationRequest rimosso. |

## Quattordici frammenti originali

I tredici estratti validi della stesura precedente sono tutti conservati: undici identici, due estesi con la graffa finale del metodo (`lst:update-condizionale`, `lst:mapping`). Sono aggiunti quattordici estratti per recuperare le funzioni originali mancanti o renderne verificabile l’adattamento. Non è richiesto un rapporto uno a uno fra vecchio e nuovo listato. Le fonti e gli intervalli di tutti i 27 sono in [frammenti.json](frammenti.json), con testo letterale e SHA-256; [pagine-frammenti.json](pagine-frammenti.json) registra la pagina PDF di ciascuno.

Il checker confronta ogni carattere con l’intervallo del sorgente e il testo PDF normalizzato solo per spazi, legature e soft hyphen. Numeri originali conservati; nessuna stringa di errore riscritta dentro un estratto. Le omissioni riguardano il contesto esterno dichiarato in apertura e presso gli esempi; selezioni UserDao e SQL sono intervalli separati, non un blocco falsamente consecutivo. Non c’è pseudocodice presentato come sorgente.

| Originale (intervallo del blocco) | Destinazione ed estratti correnti | Adattamento |
| --- | --- | --- |
| `Relazione_SWE/capitoli/3a_domain_model.tex` 24–40 | 3.1.2; `lst:vehicle-invariante`, `lst:update-condizionale` | Recuperata funzione di transizione: invariante locale distinta dal controllo atomico nel DAO; approve/block rimossi. |
| `Relazione_SWE/capitoli/3a_domain_model.tex` 50–69 | 3.1.4; `lst:request-accordo`, `lst:request-factory` | Due factory con RequestType esplicito, totale/acconto in centesimi; nessuna stima negativa. |
| `Relazione_SWE/capitoli/3b_business_layer.tex` 17–32 | 3.2.1; `lst:transazione`, `lst:callback` | DatabaseManager e due callback reali sostituiscono TransactionRunner/UnitOfWork, con causa e rollback tentato. |
| `Relazione_SWE/capitoli/3b_flussi.tex` 10–19 | 3.2.3.1; `lst:test-drive` | Lock, conteggio, disponibilità, factory e save effettivi; fascia non troncata, messaggi letterali. |
| `Relazione_SWE/capitoli/3b_flussi.tex` 27–31 | 3.2.3.2 e 3.4.4; `lst:overlap`, `lst:vincolo-rent` | Predicato inclusivo e ordine dei parametri del DAO, insieme al vincolo EXCLUDE corrente. |
| `Relazione_SWE/capitoli/3a_presentazione_dominio.tex` 19–26 | 3.3.2; `lst:app-login`, `lst:navigazione` | Costruzione Java con servizi/callback e dispatch sostituiscono FXMLLoader/ContextAware. |
| `Relazione_SWE/capitoli/3d_dao.tex` 7–13 | 3.4.1; `lst:user-dao-letture`, `lst:user-dao-comandi` | Due intervalli esplicitamente selezionati dal contratto UserDao, incluso ruolo e connessione nelle scritture. |
| `Relazione_SWE/capitoli/3d_dao.tex` 20–30 | 3.4.2; `lst:user-mapping` | Switch sui tre sottotipi reali dopo lettura dalla vista; conto caricato separatamente. |
| `Relazione_SWE/capitoli/3d_dao.tex` 37–40 | 3.4.3; `lst:sale-mapping`, `lst:mapping` | Mapper Sale completo e Request con getObject, date e BigDecimal: NULL non diventa zero. |
| `Relazione_SWE/capitoli/3d_dao.tex` 47–51 | 3.4.4; `lst:vincolo-vin`, `lst:vincolo-saldo`, `lst:vincolo-rent` | Tre intervalli distinti di schema.sql sostituiscono init.sql: UNIQUE, CHECK, EXCLUDE; enum/cast spiegati accanto. |
| `Relazione_SWE/capitoli/3e_pattern.tex` 7–12 | 3.5.1 (contratto introdotto in 3.1.5, effetto in 3.2.6); `lst:contratto-observer`, `lst:observer` | Contratto corrente con Connection; errori propagati invece di ignorati. |
| `Relazione_SWE/capitoli/3e_pattern.tex` 19–28 | 3.5.2 con formule in 3.2.4; `lst:base` | Adattamento motivato: scelta della base nello stesso calcolatore concreto; PricingStrategy non esiste. |
| `Relazione_SWE/capitoli/3e_pattern.tex` 35–42 | 3.5.3; `lst:vehicle-factory` | Overload corrente completo, ID passato dal chiamante e nuova istanza dopo INSERT; costruttore privato e nessun assignId. |
| `Relazione_SWE/capitoli/3e_pattern.tex` 49–55 | 3.5.4 con costruzione in 3.3.2; `lst:singleton`, `lst:navigazione`, `lst:app-login` | Contesto predefinito sincronizzato e double dispatch sostituiscono NavigationManager globale; costruttore alternativo esplicito. |

## Figure, formule e tabelle

Riusati gli stessi identificativi di package/classi/navigazione, sequenze d’insieme e dettagli SQ-01a–d, SQ-02, SQ-03a/b, SQ-OB, ER/relazioni e B. Tutte le 43 coppie draw.io/PDF restano byte per byte uguali alla baseline, compresi gli undici rifacimenti del ticket 10. Nessuna creazione, correzione, adattamento o esportazione di diagrammi è stata necessaria in questo ticket; gli originali degli autori restano intatti.

Le formule recuperano selezione base, massimo promozioni, fattori targa/anni/km, acconto, residuo, costo geometrico e stima. Gli esempi sono ricalcolati in [esempi-importi.json](esempi-importi.json). La vecchia anomalia saldo/listino è sostituita dall’accordo persistito corrente; il vecchio noleggio non è riproposto. Tabella 3.1 distingue origine e momento vincolante degli importi, tabella 3.2 separa meccanismi e limiti delle garanzie. L’impaginazione finale delle due tabelle evita nuovi messaggi di split.

## Conservazione e composizione

Il PDF passa da 129 a 143 pagine. L’indice passa da due a tre pagine; il capitolo 1 si sposta da 4–6 a 5–7, il 2 da 7–81 a 8–82 e il 3 occupa 83–105 (numeri fisici). Il confronto delle 78 pagine dei capitoli 1/2 è identico a testo e pixel, pagina per pagina. [Risultato](conservazione-pdf.json). Nessun sorgente di questi capitoli o delle appendici è modificato.

Capitoli 4/5, appendici A/B/C, tavole, screenshot e storici sono conservati nei file; le pagine successive slittano di quattordici. Non è un riallineamento del testing o un nuovo pacchetto finale. Tutti gli storici inclusi nella baseline sono verificati per SHA-256, senza rigenerare manifest precedenti. Le nuove evidenze attestano soltanto questa versione.
