# Controllo semantico delle relazioni

Riscontro statico del 12 novembre 2026. Le fonti sono i sorgenti correnti
indicizzati e improntati in `inventario-sorgenti.json`, non i diagrammi storici.
Ogni riga riguarda tutte le relazioni esplicitate nella relativa tavola;
attributi tipizzati e note completano gli archi selettivi.

| Tavola | Relazioni e giustificazione nel sorgente | Limite della notazione |
| --- | --- | --- |
| PKG | Import correnti di App/AppContext, viste, servizi, DAO e PostgreSQL: direzione utilizzatore → fornitore. Root → viste/servizi/DAO concreti; viste → servizi e root; servizi → DAO/event/security; contratti e PostgreSQL → dominio. | Gli archi principali sono grafici; tutti i trasversali sono esplicitati in calce. `event` usa JDBC ma non importa `service`. Nessuna pipeline inversa DAO → servizi. |
| CL-01 | `App extends Application`; campo `ctx`. `showDashboardForUser` costruisce `new UserDashboardNavigator() {...}`. `AppContext(DatabaseManager)` costruisce tutti i collaboratori dichiarati come campi. | App non implementa il navigator. `[1]` sul campo ctx riguarda l'app avviata, non la fase precedente a `start`. Il riquadro giallo dei collaboratori è una nota, non una classe aggiunta. |
| CL-02 | `AbstractUser implements User`; Client/Salesman/Manager estendono AbstractUser. `User.openDashboard` riceve il navigator. Client conserva `BankAccount`, inizialmente anche nullo (`createWithoutAccount`, setter). | `Client → BankAccount [0..1]` è la sola associazione fra queste due classi. Nessuna cardinalità inversa o composizione. `BankAccount.clientId` è Long. Il navigator riceve i tre sottotipi come parametri; uso richiamato nelle firme e in nota. |
| CL-03 | Campi `Vehicle.state: VehicleState`, `priceMode: PriceMode`, entrambi non nulli nei percorsi di creazione/aggiornamento. `getSelectedBasePrice(Model)` usa il modello passato. | Associazioni navigabili agli enum, molteplicità bersaglio 1. Dipendenza di metodo verso Model; `modelId` e `Model.brand` restano scalari. Nessuna associazione Vehicle–Model/Model–Brand dedotta dalle chiavi. |
| CL-04 | `ModelDiscount extends Discount`, `VehicleDiscount extends Discount`; costruttore base valida 0–50 e factory specializzate richiedono l'ID. | Nessuna associazione ai tipi Model/Vehicle, assenti dai campi. |
| CL-05 | Campi Request.type/Sale.type con i rispettivi enum, validati non nulli. Vehicle/client/salesman sono Long; alcune chiavi sono nulle per penale o richiesta non approvata. | Nessuna associazione alle entità esterne. Vincoli SALE/PURCHASE e importi riscontrati nelle factory. Il 20% appartiene al servizio, non alla classe Request. |
| CL-06 | Campi priceCalculator in PurchaseService, SaleToDealershipService, RentalService e CatalogService; campo db nei sei servizi transazionali; DAO e hasher dai costruttori; publisher in Purchase/Rental. | Collaborazioni espresse come attributi, senza archi ripetuti. Non viene introdotta una gerarchia fra servizi. I due record annidati sono riassunti in nota. |
| CL-07 | Publisher conserva `List<VehicleCommitmentObserver>`; attach evita duplicati/null e detach rimuove. `publishCommitment` invoca sincronicamente il contratto. Observer concreto in service realizza il contratto e chiama `deleteFutureByVehicleId(event.vehicleId(), conn)`. Purchase/Rental conservano il publisher. | Lista 0..* al bersaglio; nessuna molteplicità inversa. `VehicleCommitmentEvent` è un record di solo ID. Connection è un parametro, non un oggetto posseduto dall'evento. I due chiamanti in grigio sono una selezione dichiarata, non un nuovo tipo Java. |
| CL-08a/b | Dieci interfacce DAO indipendenti; `DatabaseManager` usa come parametri `TransactionConsumer`/`TransactionFunction<T>`, dichiarate annidate. | Nessuna generalizzazione dei DAO verso il gestore. Connessione esplicita nei contratti; default di alcune letture non garantiscono da soli il riuso. Callback prive di molteplicità perché non conservate come associazioni. |
| CL-09 | Ogni `PostgresXDao implements XDao`; tutti hanno `private final DatabaseManager db` ricevuto dal costruttore. Verificate tutte le dieci dichiarazioni. | Dieci realizzazioni con triangolo vuoto tratteggiato. Il gestore condiviso è un campo tipizzato, non una superclasse e non una parte esclusiva. |
| CL-10 | Login e tre dashboard estendono BorderPane; AsyncLoad<T> estende HBox. Tre dashboard usano AsyncLoad, AccountManagementDialog e UiUtils. Il dialogo chiama UiUtils. | AccountManagementDialog non estende Dialog. Gli archi verso servizi, utente/callback sono selezionati come campi; firme e note segnalano gli overload con AppContext. Task può essere nullo; non si promette cancellazione immediata della query JDBC. |
| CL-11 | `Pbkdf2PasswordHasher implements PasswordHasher`; `AuthService.hasher` ha tipo PasswordHasher; AppContext ne sceglie la realizzazione. | Realizzazione, non ereditarietà di classe. Nessuna garanzia generale di autorizzazione dedotta da hash/verify. |

`+`, `−`, `#`, `~` indicano visibilità; le firme sono deliberatamente
abbreviate. `abstract` è una qualifica editoriale della classe astratta,
non un nuovo stereotipo di dominio. Gli enum sono oggetti tipizzati Java;
le cardinalità non sono cardinalità delle FK SQL.

Non sono stati riscontrati conflitti con gli ADR 0001/0002. Non sono stati
identificati nuovi difetti applicativi da correggere in questo ticket:
accoppiamento alla navigazione, accessi globali, ampiezza delle dashboard e
dipendenza JDBC sono limiti architetturali già documentati, qui descritti
senza modificarli. Non è stata eseguita una ricerca esaustiva di bug.
