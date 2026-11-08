# Responsabilità, dipendenze e flussi

Questa descrizione riguarda i sorgenti correnti. I [diagrammi usati](diagrams/README.md)
collegano classi, dipendenze, sequenze, stati, casi d'uso e navigazione ai sorgenti
e ai test; gli originali precedenti sono identificati come storici e conservati.
Glossario e [ADR0001](adr/0001-base-manuale-e-prezzo-concordato.md) fissano i
termini economici. Le prove e i loro limiti sono in [testing](testing.md).

## Costruzione e direzione delle dipendenze

| Parte | Responsabilità ed esempio verificabile | Dipende da |
|---|---|---|
| [App](../src/main/java/it/unifi/drivehub/App.java) | Avvia JavaFX, sceglie la dashboard e passa servizi/utente/callback alle viste | AppContext, dominio utenti, viste JavaFX |
| [AppContext](../src/main/java/it/unifi/drivehub/AppContext.java) | Composition root: costruisce DAO PostgreSQL, hasher, publisher/observer e servizi | Implementazioni DAO, DatabaseManager, servizi, sicurezza, eventi |
| [Viste](../src/main/java/it/unifi/drivehub/presentation/view/) | Raccolgono input, invocano servizi, presentano esiti e righe di lettura | Servizi concreti, dominio, JavaFX; alcuni overload anche AppContext |
| [Servizi](../src/main/java/it/unifi/drivehub/service/) | Coordinano casi d'uso, transazioni e regole fra entità; PriceCalculatorService centralizza le formule | Interfacce DAO, dominio; servizi transazionali anche DatabaseManager e java.sql.Connection |
| [Dominio](../src/main/java/it/unifi/drivehub/domain/) | Entità/enum, controlli locali e comportamenti: Vehicle, Request, BankAccount | Libreria Java; User conosce il contratto UserDashboardNavigator dello stesso package |
| [DAO](../src/main/java/it/unifi/drivehub/dao/) e [PostgreSQL](../src/main/java/it/unifi/drivehub/dao/postgres/) | Contratti di accesso, mapping righe/entità, SQL, lock e conteggi di scrittura | Dominio, JDBC/Connection; implementazioni anche DatabaseManager |

Questa è la **direzione delle dipendenze del codice**, non una pipeline
DAO → dominio → servizi → UI. Una richiesta dati parte, per esempio, da
`ClientDashboardView` → `CatalogService.getVehicleRows` → DAO → PostgreSQL;
il risultato torna come righe preparate dal servizio alla vista. Il dominio
non chiama i DAO. Il DB conserva i dati persistenti, mentre viste, liste ed
entità ne trattengono copie in memoria che possono diventare obsolete.

`AppContext(DatabaseManager)` costruisce esplicitamente tutti i servizi e
condivide lo stesso gestore fra DAO e servizi; i costruttori dei servizi
ricevono le dipendenze. È **dependency injection manuale**, senza container.
`App` usa `AppContext.getInstance()`, Singleton inizializzato in modo
sincronizzato, e passa i servizi alle viste. Restano accessi globali negli
overload di comodo di LoginView, delle tre dashboard e AccountManagementDialog.
Non c'è DI uniforme né sostituibilità di ogni dipendenza dal solo costruttore
del contesto: questo riceve il gestore, ma sceglie internamente i PostgresDAO,
Pbkdf2PasswordHasher e servizi concreti. I test PostgreSQL costruiscono invece
un nuovo contesto con il gestore del solo DB dedicato.

## Pattern: nomi e confini

| Meccanismo | Esempio nel codice | Classificazione e limite |
|---|---|---|
| Factory statiche | `Vehicle.create`, `Client.createWithoutAccount`, `Request.createNewPurchaseRequest`, `Sale.createAcquisitionRecord` | Metodi statici nominati, con costruttori non pubblici e controlli propri dell'entità. **Non Factory Method GoF**: manca un metodo di creazione ridefinito dai sottotipi di un Creator |
| Observer | `VehicleEventPublisher.attach/publishCommitment` e `TestDriveCancellationObserver.onVehicleCommitted` | Observer GoF, notifica sincrona sulla connessione del chiamante prima del commit; non coda di eventi, outbox o consegna asincrona |
| Navigazione polimorfica | `Client.openDashboard` chiama `UserDashboardNavigator.showClientDashboard(this)`, implementato in App anche per Salesman/Manager | Double dispatch nello stile Visitor; il contratto enumera i tre tipi. Un nuovo ruolo richiede di aggiornare il navigator e la composizione, non soltanto una nuova classe |
| Singleton | `AppContext.getInstance` | Accesso globale al contesto dell'applicazione, distinto dalla DI dei costruttori; non è stato conservato uno stato globale dell'utente corrente |
| DAO | `VehicleDao` / `PostgresVehicleDao` | Pattern di accesso ai dati, **non uno dei GoF**. Restituisce entità e accetta Connection nei percorsi transazionali; non un ORM né un repository DDD indipendente da JDBC |
| Composition root e DI | AppContext assembla, App collega le viste | Tecniche di composizione/inversione del controllo, **non pattern GoF** |

Alcuni Javadoc preesistenti chiamano `create` “Factory Method”: qui la
classificazione è quella della struttura effettiva, non di quell'etichetta.
Non vengono introdotti pattern o refactor per far coincidere il codice con la teoria.

## SOLID: esempi e compromessi

- **SRP:** PriceCalculatorService calcola prezzi/tariffe senza SQL; i DAO
  mappano e persistono; PurchaseService coordina l'acquisto. La separazione è
  utile, ma le dashboard costruiscono controlli, gestiscono eventi e caricamenti
  in classi ampie; AuthService riunisce login, identità e conto dimostrativo.
- **OCP:** un nuovo observer può essere registrato senza cambiare il ciclo
  di pubblicazione. Non vale per ogni variazione: un nuovo ruolo modifica
  UserDashboardNavigator/App e il mapping SQL; una nuova formula richiede oggi
  la modifica del calcolatore concreto.
- **LSP:** Client, Salesman e Manager implementano il contratto User e App li
  usa attraverso `openDashboard`. Questo è un esempio di polimorfismo, non una
  dimostrazione formale della sostituibilità di ogni sottotipo in ogni contesto.
  AuthService distingue ancora Client con `instanceof` per caricare il conto.
- **ISP:** PasswordHasher espone hash/verify; i DAO sono distinti per risorsa.
  Non significa “un DAO per aggregato DDD”: UserDao copre più ruoli, VehicleDao
  comprende query, scritture e lock, e i contratti espongono JDBC.
- **DIP:** AuthService riceve UserDao, BankAccountDao e PasswordHasher come
  astrazioni; la scelta PostgreSQL/PBKDF2 sta nel contesto. Restano dipendenze
  da DatabaseManager, calcolatore/publisher concreti e servizi concreti nelle
  viste. I contratti DAO con Connection legano anche i servizi alla tecnologia
  JDBC: non è un'architettura esagonale interamente disaccoppiata.

## Uso di DDD

Il lessico condiviso distingue acquisto del cliente, cessione, base manuale,
totale concordato e conto dimostrativo. Vehicle protegge proprietà locali
(ad esempio chilometri non negativi e coerenza targa/chilometri);
BankAccount.withdraw controlla disponibilità e importo; Request conserva
totale/acconto dell'accordo e ne ricava il saldo. I controlli che richiedono
più record o concorrenza appartengono ai servizi e ai vincoli PostgreSQL:
l'unicità del VIN, per esempio, non è dimostrabile dalla factory in memoria.

`RequestType` usa il punto di vista della concessionaria: `SALE` quando vende al
cliente, `PURCHASE` quando acquista dal cliente. `createNewSaleRequest` riceve
acconto e `agreed_total`, non negativi e con acconto non superiore al totale;
`createNewPurchaseRequest` riceve una stima non negativa in `deposit` e conserva
`agreed_total` nullo. I nuovi input negativi sono rifiutati, senza cambiarne il
segno. I nomi dei servizi e i testi cliente descrivono ancora l'azione del cliente.
Il registro `Sale` mantiene il contratto precedente fino al ticket 02:
`PURCHASE` per la vendita al cliente, `ACQUISITION` negativa per la cessione,
`PENALTY` per la penale. La stima positiva della richiesta viene accreditata
direttamente al cliente; solo la factory del registro rappresenta l'uscita negativa.

È un modello di dominio con ispirazione DDD e servizi applicativi, in un solo
contesto documentato. Non sono dimostrati bounded context indipendenti,
aggregate root che racchiudano tutte le scritture, repository DDD, value object
per ogni concetto o eventi persistenti. Le entità hanno riferimenti per ID e
stato mutabile; la transazione del servizio può attraversare più entità/tabelle.
Il contratto di navigazione in User introduce anche una responsabilità legata
alla presentazione, pur senza importare JavaFX nel dominio.

## Flussi economici e transazionali

`DatabaseManager.executeInTransaction*` apre una connessione, disabilita
autocommit, esegue il corpo e tenta commit oppure rollback sull'eccezione.
Le scritture del caso d'uso passano questa stessa connessione ai DAO e
all'Observer. Un errore di rollback viene conservato come eccezione soppressa:
non si promette il recupero da ogni guasto del server o della rete.

| Caso d'uso | Passaggi attuali e prova pertinente |
|---|---|
| Avvio acquisto | `PurchaseService.initiatePurchase`: UPDATE condizionale SALE → BLOCKED, calcolo del prezzo sulla modalità esplicita, lock conto, addebito20%, salvataggio totale/acconto. [PurchaseAgreementIntegrationTest](../src/test/java/it/unifi/drivehub/PurchaseAgreementIntegrationTest.java) |
| Saldo, rifiuto, scadenza | Rilettura della richiesta con FOR UPDATE; saldo solo dopo approvazione e prima della scadenza, per totale meno acconto persistiti; vendita PURCHASE, richiesta rimossa, BLOCKED → SOLD, Observer prima del commit. Rifiuto rimborsa l'acconto; scadenza all'accesso trattiene20% dell'acconto e rimborsa80%, una richiesta per transazione. [PurchaseConcurrencyIntegrationTest](../src/test/java/it/unifi/drivehub/PurchaseConcurrencyIntegrationTest.java) |
| Cessione del cliente | `submitSaleProposal`: VIN noto solo SOLD, ultima PURCHASE al proponente, nessuna richiesta, modello/anno coerenti. Approvazione/rifiuto bloccano richiesta e veicolo WAITING; accettazione accredita il cliente e registra ACQUISITION negativa, rifiuto conserva il VIN con storico o elimina quello nuovo senza storico. [SaleProposalIntegrationTest](../src/test/java/it/unifi/drivehub/SaleProposalIntegrationTest.java), [SaleOutcomeIntegrationTest](../src/test/java/it/unifi/drivehub/SaleOutcomeIntegrationTest.java) |
| Noleggio e test drive | Lock veicolo, cliente NO KEY UPDATE, conto se serve; disponibilità riletta, addebito noleggio integrale. EXCLUDE sui giorni inclusivi e vincoli test drive aiutano a proteggere accessi concorrenti. La conferma noleggio cancella tutti i test drive futuri, anche fuori periodo, nella propria transazione. [BookingConcurrencyIntegrationTest](../src/test/java/it/unifi/drivehub/BookingConcurrencyIntegrationTest.java), [TestDriveCancellationIntegrationTest](../src/test/java/it/unifi/drivehub/TestDriveCancellationIntegrationTest.java) |
| Cambio destinazione | Solo SALE ↔ RENT sotto lock veicolo; un noleggio che comprende oggi blocca RENT → SALE. Futuri da domani cancellati e rimborsati100% con lock conti per ID cliente crescente, prima dell'aggiornamento finale. [VehicleDestinationIntegrationTest](../src/test/java/it/unifi/drivehub/VehicleDestinationIntegrationTest.java) |
| Cancellazione identità | Password, lock identità NO KEY UPDATE e conto cliente; richieste/storico e FK RESTRICT impediscono la cancellazione. Identità senza associazioni cancellabile, con conto dimostrativo eliminato per CASCADE anche se positivo. Attribuzioni storiche preservate. [IdentityDeletionIntegrationTest](../src/test/java/it/unifi/drivehub/IdentityDeletionIntegrationTest.java) |

Il prezzo consigliato usa la base modello; quello effettivo usa la base
selezionata, automatica o manuale, **nella stessa formula**, non come totale
finale imposto. Le basi manuali sono arrotondate ai centesimi HALF_UP.
`PriceCalculatorService` usa BigDecimal ma anche fattori/potenze e calcolo
geometrico in `double`, con arrotondamento finale: non aritmetica decimale
esatta in ogni passaggio. Il totale20.000/acconto4.000/saldo16.000 resta
concordato anche se cambia il listino; questo non congela tutti i prezzi futuri.

Il lock dei conti protegge gli aggiornamenti, quello delle richieste serializza
gli esiti sullo stesso ID, e i lock veicolo/cliente coordinano le prenotazioni.
I DAO verificano conteggi/ID nei percorsi protetti. Sono protocolli specifici
verificati con gare reali e guasti SQL, non una certificazione globale di
assenza di deadlock, serializzabilità o consegna “exactly once” distribuita.
In particolare l'avvio acquisto legge modello/sconti anche con DAO che aprono
connessioni proprie: non garantisce uno snapshot unico dell'intero listino
durante modifiche concorrenti del manager. Il totale letto viene poi persistito.

## Presentazione e flusso delle letture

`CatalogService.getVehicleRows` prepara modelli, sconti, prezzi e tariffa;
`AsyncLoad` esegue le letture su worker virtuali Java21 attraverso Task JavaFX,
pubblica solo il caricamento corrente sul thread grafico e presenta errore/riprova.
Le celle delle tabelle modificate nel14 leggono righe già preparate, senza SQL
nel rendering. Le letture collettive non costituiscono una cache persistente
né uno snapshot transazionale comune alle query.

Le azioni economiche mostrano successo dopo il ritorno del servizio e il
commit; ciò non implica che ogni gesto UI sia asincrono o che tutti i controlli
rimangano sempre reattivi. Anteprime, selezioni ed entità in memoria possono
essere obsolete: i comandi rileggono i dati necessari nei percorsi protetti.
La cancellazione di un Task scarta risultati vecchi ma non interrompe
immediatamente JDBC già avviato. [Prove14](evidence/r08-tables/verification.md)
e [prove16](evidence/r09-instructions/verification.md) delimitano gli esiti
osservati; non attestano prestazioni universali o accessibilità certificata.
