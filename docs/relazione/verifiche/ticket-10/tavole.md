# Le undici tavole originali — basi, fonti e verifiche

Skill drawio applicata a ogni intervento ed esportazione. Originali intatti;
per quattro classi e tre sequenze il nativo originale non esiste, perciò
sono ricostruzioni dai PDF e dai relativi PlantUML di riferimento.
Le copie corrette precedenti dei tre autori sono confrontate con gli
originali e con fonti attuali; non sono considerate complete solo perché presenti.

## 2.1 — casi-uso-autori

Base: [Users SWE.drawio](<../../../../docs/Users SWE.drawio>).

Attori, ellissi, generalizzazioni, include e disposizione conservati; solo cliente registrabile, livelli O/F/S e significato UC-03/04 chiariti.

Fonti tecniche: [App.java](../../../../src/main/java/it/unifi/drivehub/App.java), [LoginView.java](../../../../src/main/java/it/unifi/drivehub/presentation/view/LoginView.java), [ClientDashboardView.java](../../../../src/main/java/it/unifi/drivehub/presentation/view/ClientDashboardView.java), [SalesmanDashboardView.java](../../../../src/main/java/it/unifi/drivehub/presentation/view/SalesmanDashboardView.java), [ManagerDashboardView.java](../../../../src/main/java/it/unifi/drivehub/presentation/view/ManagerDashboardView.java).

[Sorgente nativo](../../diagrammi/casi-uso-autori.drawio), [PDF vettoriale A3](../../diagrammi/casi-uso-autori.drawio.pdf). Figura 2.1, pagine fisiche 8 del PDF completo; riscontri e osservazioni nel [verbale visivo](controllo-visivo.md).

## 2.4 — navigazione-autori

Base: [Page Navigation Diagram.drawio](<../../../../docs/Page Navigation Diagram.drawio>).

Tre diramazioni, forme e struttura conservate; LoginView iniziale, dispatch senza selezione ruolo, 4/2/5 tab e dialoghi comuni.

Fonti tecniche: [App.java](../../../../src/main/java/it/unifi/drivehub/App.java), [LoginView.java](../../../../src/main/java/it/unifi/drivehub/presentation/view/LoginView.java), [AccountManagementDialog.java](../../../../src/main/java/it/unifi/drivehub/presentation/view/AccountManagementDialog.java).

[Sorgente nativo](../../diagrammi/navigazione-autori.drawio), [PDF vettoriale A3](../../diagrammi/navigazione-autori.drawio.pdf). Figura 2.21, pagine fisiche 36 del PDF completo; riscontri e osservazioni nel [verbale visivo](controllo-visivo.md).

## 2.5 — package-autori

Base: [Package diagram.drawio](<../../../../docs/Package diagram.drawio>).

Copia nativa con stessi gruppi, colori e forme; undici package, nomi e dipendenze attuali; nota sui collegamenti ripetuti. Archi instradati fuori dai package estranei e nel contenitore comune corretto.

Fonti tecniche: [App.java](../../../../src/main/java/it/unifi/drivehub/App.java), [AppContext.java](../../../../src/main/java/it/unifi/drivehub/AppContext.java), [DatabaseManager.java](../../../../src/main/java/it/unifi/drivehub/dao/DatabaseManager.java), [VehicleEventPublisher.java](../../../../src/main/java/it/unifi/drivehub/event/VehicleEventPublisher.java).

[Sorgente nativo](../../diagrammi/package-autori.drawio), [PDF vettoriale A3](../../diagrammi/package-autori.drawio.pdf). Figura 2.25, pagine fisiche 41 del PDF completo; riscontri e osservazioni nel [verbale visivo](controllo-visivo.md).

## 2.6.1 — classi-dominio-insieme

Base: [class-domain-drivehub.pdf](../../../../Relazione_SWE/images/class-domain-drivehub.pdf), [class-domain-drivehub.puml](../../../../Relazione_SWE/diagrams/class-domain-drivehub.puml), [drivehub-class-theme.puml](../../../../Relazione_SWE/diagrams/drivehub-class-theme.puml).

Ricostruzione nativa dal riferimento visivo: gruppi interni rossi, classi e UML; ruoli reali, conto, ID, enum e accordo. Eventi ricollocati nel gruppo business.

Fonti tecniche: [User.java](../../../../src/main/java/it/unifi/drivehub/domain/user/User.java), [AbstractUser.java](../../../../src/main/java/it/unifi/drivehub/domain/user/AbstractUser.java), [Client.java](../../../../src/main/java/it/unifi/drivehub/domain/user/Client.java), [BankAccount.java](../../../../src/main/java/it/unifi/drivehub/domain/user/BankAccount.java), [Vehicle.java](../../../../src/main/java/it/unifi/drivehub/domain/vehicle/Vehicle.java), [Model.java](../../../../src/main/java/it/unifi/drivehub/domain/vehicle/Model.java), [Discount.java](../../../../src/main/java/it/unifi/drivehub/domain/discount/Discount.java), [Request.java](../../../../src/main/java/it/unifi/drivehub/domain/operation/Request.java), [Sale.java](../../../../src/main/java/it/unifi/drivehub/domain/operation/Sale.java).

[Sorgente nativo](../../diagrammi/classi-dominio-insieme.drawio), [PDF vettoriale A3](../../diagrammi/classi-dominio-insieme.drawio.pdf). Figura 2.26, pagine fisiche 43 del PDF completo; riscontri e osservazioni nel [verbale visivo](controllo-visivo.md).

## 2.6.2 — classi-business-insieme

Base: [class-business-drivehub.pdf](../../../../Relazione_SWE/images/class-business-drivehub.pdf), [class-business-drivehub.puml](../../../../Relazione_SWE/diagrams/class-business-drivehub.puml), [drivehub-class-theme.puml](../../../../Relazione_SWE/diagrams/drivehub-class-theme.puml).

Stesso gruppo servizi giallo e collaboratori DAO/eventi; calcolatore concreto, DatabaseManager, record evento distinto dal contratto e Observer. Rimossi Strategy e UnitOfWork.

Fonti tecniche: [AppContext.java](../../../../src/main/java/it/unifi/drivehub/AppContext.java), [PurchaseService.java](../../../../src/main/java/it/unifi/drivehub/service/PurchaseService.java), [RentalService.java](../../../../src/main/java/it/unifi/drivehub/service/RentalService.java), [SaleToDealershipService.java](../../../../src/main/java/it/unifi/drivehub/service/SaleToDealershipService.java), [PriceCalculatorService.java](../../../../src/main/java/it/unifi/drivehub/service/PriceCalculatorService.java), [AuthService.java](../../../../src/main/java/it/unifi/drivehub/service/AuthService.java), [TestDriveCancellationObserver.java](../../../../src/main/java/it/unifi/drivehub/service/TestDriveCancellationObserver.java), [VehicleCommitmentEvent.java](../../../../src/main/java/it/unifi/drivehub/event/VehicleCommitmentEvent.java), [VehicleCommitmentObserver.java](../../../../src/main/java/it/unifi/drivehub/event/VehicleCommitmentObserver.java), [PasswordHasher.java](../../../../src/main/java/it/unifi/drivehub/security/PasswordHasher.java).

[Sorgente nativo](../../diagrammi/classi-business-insieme.drawio), [PDF vettoriale A3](../../diagrammi/classi-business-insieme.drawio.pdf). Figura 2.27, pagine fisiche 45 del PDF completo; riscontri e osservazioni nel [verbale visivo](controllo-visivo.md).

## 2.6.3 — classi-dao-insieme

Base: [class-dao-drivehub.pdf](../../../../Relazione_SWE/images/class-dao-drivehub.pdf), [class-dao-drivehub.puml](../../../../Relazione_SWE/diagrams/class-dao-drivehub.puml), [drivehub-class-theme.puml](../../../../Relazione_SWE/diagrams/drivehub-class-theme.puml).

Due contenitori blu, quattro coppie rappresentative e nota delle altre sei; callback del gestore e Connection al posto di factory/UnitOfWork/loader. TransactionConsumer e TransactionFunction<T> sono due interfacce distinte, con execute : void e execute : T.

Fonti tecniche: [DatabaseManager.java](../../../../src/main/java/it/unifi/drivehub/dao/DatabaseManager.java), [UserDao.java](../../../../src/main/java/it/unifi/drivehub/dao/UserDao.java), [VehicleDao.java](../../../../src/main/java/it/unifi/drivehub/dao/VehicleDao.java), [RentDao.java](../../../../src/main/java/it/unifi/drivehub/dao/RentDao.java), [RequestDao.java](../../../../src/main/java/it/unifi/drivehub/dao/RequestDao.java), [PostgresUserDao.java](../../../../src/main/java/it/unifi/drivehub/dao/postgres/PostgresUserDao.java), [PostgresVehicleDao.java](../../../../src/main/java/it/unifi/drivehub/dao/postgres/PostgresVehicleDao.java), [PostgresRentDao.java](../../../../src/main/java/it/unifi/drivehub/dao/postgres/PostgresRentDao.java), [PostgresRequestDao.java](../../../../src/main/java/it/unifi/drivehub/dao/postgres/PostgresRequestDao.java).

[Sorgente nativo](../../diagrammi/classi-dao-insieme.drawio), [PDF vettoriale A3](../../diagrammi/classi-dao-insieme.drawio.pdf). Figura 2.28, pagine fisiche 47 del PDF completo; riscontri e osservazioni nel [verbale visivo](controllo-visivo.md).

## 2.6.4 — classi-presentazione-insieme

Base: [class-presentation-drivehub.pdf](../../../../Relazione_SWE/images/class-presentation-drivehub.pdf), [class-presentation-drivehub.puml](../../../../Relazione_SWE/diagrams/class-presentation-drivehub.puml), [drivehub-class-theme.puml](../../../../Relazione_SWE/diagrams/drivehub-class-theme.puml).

Gruppo verde e collaboratori, classi UML; viste Java al posto dei controller, dialogo utility, AsyncLoad e App con navigator anonimo.

Fonti tecniche: [App.java](../../../../src/main/java/it/unifi/drivehub/App.java), [UserDashboardNavigator.java](../../../../src/main/java/it/unifi/drivehub/domain/user/UserDashboardNavigator.java), [AccountManagementDialog.java](../../../../src/main/java/it/unifi/drivehub/presentation/view/AccountManagementDialog.java), [AsyncLoad.java](../../../../src/main/java/it/unifi/drivehub/presentation/view/AsyncLoad.java).

[Sorgente nativo](../../diagrammi/classi-presentazione-insieme.drawio), [PDF vettoriale A3](../../diagrammi/classi-presentazione-insieme.drawio.pdf). Figura 2.29, pagine fisiche 49 del PDF completo; riscontri e osservazioni nel [verbale visivo](controllo-visivo.md).

## 2.7.1 — sequenza-vendita-insieme

Base: [sequence-purchase-drivehub.pdf](../../../../Relazione_SWE/images/sequence-purchase-drivehub.pdf), [sequence-purchase-drivehub.puml](../../../../Relazione_SWE/diagrams/sequence-purchase-drivehub.puml), [drivehub-sequence-theme.puml](../../../../Relazione_SWE/diagrams/drivehub-sequence-theme.puml).

Attori, lifeline, frecce, divisori acconto/autorizzazione/saldo e palette originali; T1–T3, accordo persistito, conto e Observer. Letture autonome dichiarate.

Fonti tecniche: [PurchaseService.java](../../../../src/main/java/it/unifi/drivehub/service/PurchaseService.java), [DatabaseManager.java](../../../../src/main/java/it/unifi/drivehub/dao/DatabaseManager.java), [PostgresVehicleDao.java](../../../../src/main/java/it/unifi/drivehub/dao/postgres/PostgresVehicleDao.java), [PostgresRequestDao.java](../../../../src/main/java/it/unifi/drivehub/dao/postgres/PostgresRequestDao.java), [TestDriveCancellationObserver.java](../../../../src/main/java/it/unifi/drivehub/service/TestDriveCancellationObserver.java).

[Sorgente nativo](../../diagrammi/sequenza-vendita-insieme.drawio), [PDF vettoriale A3](../../diagrammi/sequenza-vendita-insieme.drawio.pdf). Figura 2.30, pagine fisiche 51 del PDF completo; riscontri e osservazioni nel [verbale visivo](controllo-visivo.md).

## 2.7.2 — sequenza-noleggio-insieme

Base: [sequence-rental-drivehub.pdf](../../../../Relazione_SWE/images/sequence-rental-drivehub.pdf), [sequence-rental-drivehub.puml](../../../../Relazione_SWE/diagrams/sequence-rental-drivehub.puml), [drivehub-sequence-theme.puml](../../../../Relazione_SWE/diagrams/drivehub-sequence-theme.puml).

Stessa sequenza del noleggio con attore e collaboratori colorati; preventivo separato, lock, formula geometrica, addebito e Observer sulla stessa Connection. Alternative nei dettagli SQ-02/SQ-OB.

Fonti tecniche: [RentalService.java](../../../../src/main/java/it/unifi/drivehub/service/RentalService.java), [PriceCalculatorService.java](../../../../src/main/java/it/unifi/drivehub/service/PriceCalculatorService.java), [PostgresRentDao.java](../../../../src/main/java/it/unifi/drivehub/dao/postgres/PostgresRentDao.java), [PostgresUserDao.java](../../../../src/main/java/it/unifi/drivehub/dao/postgres/PostgresUserDao.java), [TestDriveCancellationObserver.java](../../../../src/main/java/it/unifi/drivehub/service/TestDriveCancellationObserver.java), [schema.sql](../../../../src/main/resources/db/schema.sql).

[Sorgente nativo](../../diagrammi/sequenza-noleggio-insieme.drawio), [PDF vettoriale A3](../../diagrammi/sequenza-noleggio-insieme.drawio.pdf). Figura 2.35, pagine fisiche 60 del PDF completo; riscontri e osservazioni nel [verbale visivo](controllo-visivo.md).

## 2.7.3 — sequenza-cessione-insieme

Base: [sequence-acquisition-drivehub.pdf](../../../../Relazione_SWE/images/sequence-acquisition-drivehub.pdf), [sequence-acquisition-drivehub.puml](../../../../Relazione_SWE/diagrams/sequence-acquisition-drivehub.puml), [drivehub-sequence-theme.puml](../../../../Relazione_SWE/diagrams/drivehub-sequence-theme.puml).

Due attori, lifeline e divisori proposta/approvazione; stima persistita, VIN e storico, accettazione e rifiuto. Nessun gateway o UnitOfWork annidato. La chiamata al calcolatore è estimateVehicleValue; calculateEstimate resta nel servizio.

Fonti tecniche: [SaleToDealershipService.java](../../../../src/main/java/it/unifi/drivehub/service/SaleToDealershipService.java), [PostgresSaleDao.java](../../../../src/main/java/it/unifi/drivehub/dao/postgres/PostgresSaleDao.java), [PostgresVehicleDao.java](../../../../src/main/java/it/unifi/drivehub/dao/postgres/PostgresVehicleDao.java), [PostgresRequestDao.java](../../../../src/main/java/it/unifi/drivehub/dao/postgres/PostgresRequestDao.java), [BankAccount.java](../../../../src/main/java/it/unifi/drivehub/domain/user/BankAccount.java).

[Sorgente nativo](../../diagrammi/sequenza-cessione-insieme.drawio), [PDF vettoriale A3](../../diagrammi/sequenza-cessione-insieme.drawio.pdf). Figura 2.38, pagine fisiche 65 del PDF completo; riscontri e osservazioni nel [verbale visivo](controllo-visivo.md).

## 2.8.1 — database-er-autori

Base: [SWE ER.drawio](<../../../../docs/SWE ER.drawio>).

Tabelle a tre colonne, generalizzazione concettuale, cardinalità a zampa di corvo e seconda pagina di legenda conservate; 13 tabelle, 62 attributi e 15 FK verificati, legenda NULL e rinvio testuale aggiornati.

Fonti tecniche: [PostgresUserDao.java](../../../../src/main/java/it/unifi/drivehub/dao/postgres/PostgresUserDao.java), [Request.java](../../../../src/main/java/it/unifi/drivehub/domain/operation/Request.java), [PriceMode.java](../../../../src/main/java/it/unifi/drivehub/domain/vehicle/PriceMode.java), [schema.sql](../../../../src/main/resources/db/schema.sql).

[Sorgente nativo](../../diagrammi/database-er-autori.drawio), [PDF vettoriale A3](../../diagrammi/database-er-autori.drawio.pdf). Figura 2.41, pagine fisiche 70, 71 del PDF completo; riscontri e osservazioni nel [verbale visivo](controllo-visivo.md).

