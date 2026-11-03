# Coerenza di istruzioni, diagrammi ed evidenze — riscontro18

Ispezione dei documenti correnti rispetto alla baseline locale18 e ai sorgenti
che descrivono. Non sono test JUnit del testo. Nessun accesso a `Relazione_SWE`.
Le evidenze16–17 sono state lette come **storiche**; le esecuzioni18 sono
registrate separatamente in [verification.md](verification.md).

| Documento / vista | Riscontro corrente e confine della verifica |
|---|---|
| `installation.md`, README, POM | Target21 e JavaFX21.0.2/JDBC42.7.3; Maven usa lo stesso JDK21. `DatabaseManager()` legge `db.url`, `db.user`, `db.password` dalla risorsa, con fallback se assente: le variabili TEST/SENTINEL non configurano l'applicazione. La sola copia privata18 sostituisce questa risorsa. Nessun reset all'avvio in `App.start`. |
| Installazione: schema/demo | `schema.sql` ricrea strutture e richiede btree_gist; `demo.sql` cancella/ricrea dati e sequenze. Setup18 ha eseguito schema in transazione sul nuovo DB sacrificabile, osservato client=0/estensione, poi demo facoltativa e sentinella. Procedure distruttive distinte da avvio ordinario; nessuna migrazione completa o installer promessi. |
| `testing.md` e isolamento | Test con tag `postgres`, profilo Maven, configurazioni esplicite validate da `TestDatabase`; ruoli/DB distinti e non amministrativi. Probe READ ONLY su copia sentinella e hash tabelle/sequenze prima/dopo. I precedenti numeri sono storici; suite e GUI18 non condividono le fixture contemporaneamente. |
| Glossario / ADR0001 / `class-domain.puml` | `Vehicle` mantiene `PriceMode` AUTOMATIC/MANUAL e base selezionata; `PriceCalculatorService` applica stessa formula, massimo sconto e svalutazioni; `Request` conserva totale/acconto. Base manuale non è prezzo finale. Test prezzo/accordo18 e importi GUI coerenti. |
| `class-domain-workflows.puml` | User/AbstractUser/tre ruoli e navigator esistenti; associazioni per ID, Client con conto opzionale. `RequestType` PURCHASE/SALE e campi totale/acconto/scadenza; esito elimina la richiesta. `SaleType` PURCHASE/ACQUISITION/PENALTY e segni vincolati nello schema. Nessuno stato REQUESTED/COMPLETED per Rent/TestDrive. |
| `class-business.puml`, `class-dao.puml` | `AppContext(DatabaseManager)` costruisce dieci PostgresDAO, servizi, hasher e registra l'Observer. DAO ricevono Connection nei percorsi transazionali; `DatabaseManager` tenta rollback e conserva errore soppresso. `VehicleEventPublisher.publishCommitment` invoca Observer sincroni prima del commit, non una coda. Firme abbreviate esplicitamente dichiarate. |
| `class-presentation.puml`, `package-diagram.puml` | App costruisce navigator anonimo e passa utente/servizi/callback; overload residui usano Singleton. Dipendenze UI→servizi/dominio, servizi→DAO/dominio/eventi/security/JDBC, implementazioni→contratti/JDBC; dominio senza import DAO/servizi/JavaFX. `AsyncLoad` e record delle righe esistono; niente controller FXML. Sono viste selettive, non una promessa d'indipendenza da JDBC. |
| `sequence-sale-payment.puml`, `sequence-purchase-outcomes.puml` | `PurchaseService`: blocco SALE→BLOCKED, acconto20% e accordo; approvazione con venditore/scadenza; saldo sul persistito, richiesta consumata, PURCHASE e SOLD/Observer. Rifiuto rimborsa acconto; scadenza all'ingresso, una transazione per candidata, penale20% dell'acconto. Letture prezzo all'avvio anche su connessioni proprie: nessuno snapshot unico inventato. |
| `sequence-purchase-proposal-approval.puml` | `SaleToDealershipService`: VIN noto bloccato, SOLD/ultima PURCHASE/nessuna richiesta/modello-anno; VIN nuovo inserito WAITING. Esito su richiesta e veicolo riletti: accredito/ACQUISITION negativa/SALE o rifiuto con storico SOLD/rimozione VIN nuovo. UI venditore, non approvazione manager né PaymentGateway. |
| `sequence-rent-payment.puml`, `sequence-test-drive.puml` | Lock veicolo→cliente→conto, dove necessario; noleggio giorni inclusivi e addebito100%, Observer elimina tutti i futuri. Test drive limita tre attivi, disponibilità e09–16; annullamento manuale controlla proprietario senza aggiungere un vincolo futuro. I relativi test18 esercitano conferme, gare e rollback; non ogni gesto del dialogo grafico. |
| `sequence-table-load.puml` | `CatalogService.getVehicleRows` prepara righe con quattro letture collettive; `AsyncLoad.load` annulla il Task precedente, pubblica solo current nei callback FX e usa thread virtuali. Scena rimossa cancella current; errore mantiene dati disabilitati e offre Riprova. Query JDBC già avviate possono terminare. Harness18 osservano query bloccate, celle, obsoleti ed errore/riprova. |
| `vehicle-states.puml` | Stati reali SALE/RENT/SOLD/WAITING/BLOCKED; frecce riferite ai servizi. `RentalService.changeVehicleRentalState` permette solo opposti SALE/RENT, protegge noleggio comprendente oggi e rimborsa futuri nella transazione. La factory/il setter pubblico non impongono l'intera macchina. Le regressioni18 verificano i confini senza generalizzare a ogni assegnazione. |
| `page-navigation*.puml` (tre viste) | `App`/LoginView/AccountManagementDialog e tre dashboard correnti. Quattro tab cliente, due venditore e cinque manager, dialoghi e callback; nessuna rotta/controller inesistente. Test drive ritorna alla tab chiamante e ricarica non la cambia, come corretto17; dialogo chiude prima del comando/esito. Riscontro statico nei gestori; GUI18 verifica solo i percorsi/assert dei sette harness elencati nella matrice. |
| `use-cases*.puml` (due viste) | Funzioni correnti cliente/staff: venditore approva/rifiuta, manager gestisce catalogo/prezzi/sconti/registro. Partecipazione dell'attore non equivale ad autorizzazione centralizzata di ogni servizio. Scadenze e Observer sono effetti interni; nessun gateway o ordini/manutenzione inesistenti. |
| `architecture.md`: teoria | Factory statiche distinte da Factory Method GoF; Observer sincrono e navigazione nello stile Visitor; DAO/DI/composition root distinti dai GoF. Esempi SOLID e DDD con limiti espliciti, senza refactor per imitare la teoria. `double` nei fattori, dipendenze JDBC, accessi globali e snapshot parziale non occultati. |
| Pulizia15 e riferimenti | Ricerca mirata in `src`/POM di `javafx-fxml`, `FXML`, `getCurrentUser`, `setCurrentUser`, `logout(`: nessun riferimento residuo ai componenti rimossi. Nessuna rimozione18. Compilazione finale e vero App.start18 verificano i chiamanti attuali. |
| Evidenze e storico | Manifest delle versioni `it.unifi.ing.drivehub` non usati come prove del package corrente. File iniziali, incluse evidenze16–17 e originali UML/draw.io/mockup/screenshot, confrontati con fotografia18; nessuna riscrittura. Nuovi log, XML, screenshot e input sono soltanto in `r11-integrated/`. |

Il nuovo render18 dei **18 PlantUML correnti** è riuscito, ed è identico byte per
byte a ciascuno dei18SVG consegnati: [log](render.log), [hash](render-results.json).
Non sono stati rigenerati gli originali storici. La leggibilità dei render era
stata ispezionata nel17;18 verifica identità e coerenza delle stesse viste sui
sorgenti ancora identici, senza attribuirsi una nuova ispezione di18immagini.
Nessun grafo Graphify presente: letture mirate sufficienti, nessuna generazione.

Non sono emerse discrepanze che richiedano modifiche a installazione,
architettura o diagrammi. Aggiornati soltanto indice delle evidenze, rinvio alla
matrice in testing, piano e ticket18. Restano i limiti dichiarati: viste
selettive, copertura GUI parziale, Linux/WSL/Xvfb, niente screen reader/altroOS,
migrazione completa, installer o garanzia globale di assenza di deadlock.
