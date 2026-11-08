# Diagrammi della versione locale corrente

I cinque diagrammi che descrivono il registro sono aggiornati al contratto del
[ticket 02](../evidence/sale-semantics-02/verification.md): `SALE`, `PURCHASE` e
`PENALTY`, con importi non negativi. Le versioni delle evidenze precedenti restano storiche.

Sono usati soltanto i sorgenti e i render elencati qui, sotto **`current/`**.
Rappresentano `it.unifi.drivehub` della baseline locale del ticket17, inclusi
modificati/non tracciati, non il solo HEAD. Versione, hash, diff della sessione,
strumenti, prove effettive e limiti: [evidenze17](../evidence/r09-diagrams/verification.md).
I diagrammi sono viste selettive: non inventano membri per completare un modello
ideale e non sostituiscono i sorgenti o le garanzie specifiche dei test.

## Classi e package

| Vista / render SVG | Sorgente modificabile | Riscontro nel codice e nei test |
|---|---|---|
| [Package](current/package-diagram.svg) | [PlantUML](current/package-diagram.puml) | [AppContext](../../src/main/java/it/unifi/drivehub/AppContext.java), import in [viste](../../src/main/java/it/unifi/drivehub/presentation/view/), [servizi](../../src/main/java/it/unifi/drivehub/service/), [DAO](../../src/main/java/it/unifi/drivehub/dao/) e [dominio](../../src/main/java/it/unifi/drivehub/domain/). Dipendenze statiche verificate sui sorgenti; nessun LayeringTest corrente |
| [Catalogo e prezzi](current/class-domain.svg) | [PlantUML](current/class-domain.puml) | [Vehicle](../../src/main/java/it/unifi/drivehub/domain/vehicle/Vehicle.java), [sconti](../../src/main/java/it/unifi/drivehub/domain/discount/), [DomainInvariantsTest](../../src/test/java/it/unifi/drivehub/DomainInvariantsTest.java), [PriceSelectionIntegrationTest](../../src/test/java/it/unifi/drivehub/PriceSelectionIntegrationTest.java) |
| [Identità e operazioni](current/class-domain-workflows.svg) | [PlantUML](current/class-domain-workflows.puml) | [Utenti](../../src/main/java/it/unifi/drivehub/domain/user/), [operazioni](../../src/main/java/it/unifi/drivehub/domain/operation/), [RequestAgreementTest](../../src/test/java/it/unifi/drivehub/RequestAgreementTest.java), [SaleTypeTest](../../src/test/java/it/unifi/drivehub/SaleTypeTest.java) |
| [Servizi e Observer](current/class-business.svg) | [PlantUML](current/class-business.puml) | [AppContext](../../src/main/java/it/unifi/drivehub/AppContext.java), [VehicleEventPublisher](../../src/main/java/it/unifi/drivehub/event/VehicleEventPublisher.java), [TestDriveCancellationObserver](../../src/main/java/it/unifi/drivehub/service/TestDriveCancellationObserver.java), [TestDriveCancellationIntegrationTest](../../src/test/java/it/unifi/drivehub/TestDriveCancellationIntegrationTest.java) |
| [DAO e transazioni JDBC](current/class-dao.svg) | [PlantUML](current/class-dao.puml) | [DatabaseManager](../../src/main/java/it/unifi/drivehub/dao/DatabaseManager.java), [contratti/implementazioni](../../src/main/java/it/unifi/drivehub/dao/), [DatabaseIsolationIntegrationTest](../../src/test/java/it/unifi/drivehub/DatabaseIsolationIntegrationTest.java), [PurchaseConcurrencyIntegrationTest](../../src/test/java/it/unifi/drivehub/PurchaseConcurrencyIntegrationTest.java) |
| [Presentazione](current/class-presentation.svg) | [PlantUML](current/class-presentation.puml) | [App](../../src/main/java/it/unifi/drivehub/App.java), [AsyncLoad](../../src/main/java/it/unifi/drivehub/presentation/view/AsyncLoad.java), [TableLifecycleUiCheck](../evidence/r08-tables/TableLifecycleUiCheck.java) |

`create(...)` indica factory statiche, non Factory Method GoF; DAO, DI manuale
e composition root non sono GoF. Observer è sincrono sulla connessione del
chiamante prima del commit. La navigazione usa double dispatch nello stile
Visitor; un nuovo ruolo modifica navigator e composizione. Compromessi SOLID,
DDD e accessi globali residui restano quelli di [architecture](../architecture.md).
Le frecce di package sono dipendenze del codice, quelle delle sequenze messaggi
e risultati: il dominio non legge il DB. Relazioni per ID sono indicate come
tali, senza trasformarle in riferimenti a oggetti o composizioni UML.

## Sequenze e stati

| Flusso / render SVG | Sorgente modificabile | Implementazione e prove pertinenti |
|---|---|---|
| [Acconto, approvazione e saldo](current/sequence-sale-payment.svg) | [PlantUML](current/sequence-sale-payment.puml) | [PurchaseService](../../src/main/java/it/unifi/drivehub/service/PurchaseService.java); [PurchaseAgreementIntegrationTest](../../src/test/java/it/unifi/drivehub/PurchaseAgreementIntegrationTest.java), [PurchaseConcurrencyIntegrationTest](../../src/test/java/it/unifi/drivehub/PurchaseConcurrencyIntegrationTest.java) |
| [Rifiuto e scadenza acquisto](current/sequence-purchase-outcomes.svg) | [PlantUML](current/sequence-purchase-outcomes.puml) | Stesso servizio; gare/esiti/rollback in [PurchaseConcurrencyIntegrationTest](../../src/test/java/it/unifi/drivehub/PurchaseConcurrencyIntegrationTest.java) |
| [Cessione del cliente](current/sequence-purchase-proposal-approval.svg) | [PlantUML](current/sequence-purchase-proposal-approval.puml) | [SaleToDealershipService](../../src/main/java/it/unifi/drivehub/service/SaleToDealershipService.java); [SaleProposalIntegrationTest](../../src/test/java/it/unifi/drivehub/SaleProposalIntegrationTest.java), [SaleOutcomeIntegrationTest](../../src/test/java/it/unifi/drivehub/SaleOutcomeIntegrationTest.java) |
| [Noleggio e cancellazioni atomiche](current/sequence-rent-payment.svg) | [PlantUML](current/sequence-rent-payment.puml) | [RentalService](../../src/main/java/it/unifi/drivehub/service/RentalService.java); [BookingConcurrencyIntegrationTest](../../src/test/java/it/unifi/drivehub/BookingConcurrencyIntegrationTest.java), [TestDriveCancellationIntegrationTest](../../src/test/java/it/unifi/drivehub/TestDriveCancellationIntegrationTest.java) |
| [Test drive](current/sequence-test-drive.svg) | [PlantUML](current/sequence-test-drive.puml) | [TestDriveService](../../src/main/java/it/unifi/drivehub/service/TestDriveService.java); [BookingRulesTest](../../src/test/java/it/unifi/drivehub/BookingRulesTest.java), [BookingConcurrencyIntegrationTest](../../src/test/java/it/unifi/drivehub/BookingConcurrencyIntegrationTest.java) |
| [Caricamento e rendering tabelle](current/sequence-table-load.svg) | [PlantUML](current/sequence-table-load.puml) | [AsyncLoad](../../src/main/java/it/unifi/drivehub/presentation/view/AsyncLoad.java), [CatalogService](../../src/main/java/it/unifi/drivehub/service/CatalogService.java); [CatalogListingIntegrationTest](../../src/test/java/it/unifi/drivehub/CatalogListingIntegrationTest.java), [harness grafici14](../evidence/r08-tables/verification.md) |
| [Stati veicolo e cambio destinazione](current/vehicle-states.svg) | [PlantUML](current/vehicle-states.puml) | [VehicleState](../../src/main/java/it/unifi/drivehub/domain/vehicle/VehicleState.java), servizi Purchase/SaleToDealership/Rental; [VehicleDestinationIntegrationTest](../../src/test/java/it/unifi/drivehub/VehicleDestinationIntegrationTest.java) |

I messaggi che riassumono un oggetto aggiornato omettono gli argomenti ripetitivi;
`callback` indica il corpo del servizio eseguito da DatabaseManager. Ogni gruppo
transazionale apre/disabilita autocommit e tenta commit, oppure rollback su
eccezione, poi chiude. Non si promette recupero da ogni guasto o assenza globale
di deadlock. Le letture modello/sconti all'avvio acquisto e nella stima cessione
aprono anche connessioni proprie. Nessuno snapshot unico del listino.

La macchina a stati descrive i percorsi dei servizi, non ogni uso pubblico di
`Vehicle.setState`. Request non ha un enum di stato della pratica; Rent e
TestDrive non hanno ciclo REQUESTED/COMPLETED. Vincoli effettivi in [schema.sql](../../src/main/resources/db/schema.sql).
Il totale concordato non cambia al saldo; prezzo manuale significa base della
stessa formula ([ADR0001](../adr/0001-base-manuale-e-prezzo-concordato.md)).

## Casi d'uso e navigazione

| Vista / render SVG | Sorgente modificabile | Riscontro |
|---|---|---|
| [Cliente e identità](current/use-cases.svg) | [PlantUML](current/use-cases.puml) | [AuthService](../../src/main/java/it/unifi/drivehub/service/AuthService.java), servizi economici; [AuthServiceIntegrationTest](../../src/test/java/it/unifi/drivehub/AuthServiceIntegrationTest.java), [IdentityDeletionIntegrationTest](../../src/test/java/it/unifi/drivehub/IdentityDeletionIntegrationTest.java) |
| [Staff](current/use-cases-staff.svg) | [PlantUML](current/use-cases-staff.puml) | [SalesmanDashboardView](../../src/main/java/it/unifi/drivehub/presentation/view/SalesmanDashboardView.java), [ManagerDashboardView](../../src/main/java/it/unifi/drivehub/presentation/view/ManagerDashboardView.java); test di esito cessione, destinazione e listino sopra |
| [Accesso, ruoli e account](current/page-navigation.svg) | [PlantUML](current/page-navigation.puml) | [App](../../src/main/java/it/unifi/drivehub/App.java), [LoginView](../../src/main/java/it/unifi/drivehub/presentation/view/LoginView.java), [AccountManagementDialog](../../src/main/java/it/unifi/drivehub/presentation/view/AccountManagementDialog.java); [AppStartupUiCheck](../evidence/r10-cleanup/AppStartupUiCheck.java) |
| [Cliente: tab e dialoghi](current/page-navigation-customer.svg) | [PlantUML](current/page-navigation-customer.puml) | [ClientDashboardView](../../src/main/java/it/unifi/drivehub/presentation/view/ClientDashboardView.java); [TableLifecycleUiCheck](../evidence/r08-tables/TableLifecycleUiCheck.java), [PurchaseAgreementIntegrationTest](../../src/test/java/it/unifi/drivehub/PurchaseAgreementIntegrationTest.java) |
| [Staff: tab e azioni](current/page-navigation-staff.svg) | [PlantUML](current/page-navigation-staff.puml) | Dashboard sopra; [TableLifecycleUiCheck](../evidence/r08-tables/TableLifecycleUiCheck.java), [SaleOutcomeUiCheck](../evidence/r05-sale-outcome/SaleOutcomeUiCheck.java) |

I casi d'uso mostrano funzionalità disponibili, non una certificazione di
autorizzazione in ogni API. I diagrammi di navigazione descrivono tab, scene,
callback e dialoghi, non stati persistenti. Le tab dello stesso ruolo sono
selezionabili in ogni ordine; le frecce ne mostrano soltanto alcuni percorsi.
La sola esistenza di un test non attesta ogni messaggio o gesto del diagramma:
nelle evidenze17 è distinto ciò che è rieseguito da ciò che è controllato staticamente.

## Artefatti storici conservati

**Non usare come prove correnti** i `.puml` direttamente in questa directory
(compreso `er-diagram.puml`), `../source-diagrams/*.drawio` e i quattro `.drawio`
direttamente in `docs/`. Sono originali precedenti, conservati byte per byte;
non vanno confusi con `current/`. Anche mockup e screenshot in `docs/mockups/`
e `docs/screenshots/` sono storici, inclusa la schermata PaymentDialog.
Nessun ER, draw.io, mockup o screenshot storico è stato rigenerato per il17.

I manifest `../evidence/full-stack/source-manifest.json` e
`../evidence/jdk21-test/source-manifest.json` si riferiscono alla vecchia struttura
`it.unifi.ing.drivehub`; i relativi XML/summary non attestano questa versione.
Le prove01–16 attestano esclusivamente le rispettive sessioni: **394/394del16
rimane storico**, anche se una nuova esecuzione17 ha lo stesso conteggio.
[Inventario con hash e confronto manifest](../evidence/r09-diagrams/historical-artifacts.json)
e [indice evidenze](../evidence/README.md) rendono esplicita questa distinzione.

## Render riproducibile

Con Java21 e il JAR PlantUML1.2026.2 verificato ([origine/hash](../evidence/r09-diagrams/renderer.json)),
dalla radice DriveHub, impostare `PLANTUML_JAR` al percorso locale effettivo:

```bash
java -XX:+PerfDisableSharedMem -Djava.awt.headless=true -jar "$PLANTUML_JAR" \
  -charset UTF-8 -failfast2 -tsvg docs/diagrams/current/*.puml
```

I diagrammi strutturali usano Smetana incluso nel JAR; Graphviz esterno non è
necessario per questi render. Il comando `-version` può segnalare l'assenza di
`dot` e terminare16: l'esito del render effettivo è registrato separatamente.
Per rivedere i PNG senza duplicarli nel progetto, usare `-tpng -o` con directory
temporanea assoluta. Verificare visivamente i render dopo modifiche: il successo
del parser non dimostra la leggibilità o la correttezza UML.
