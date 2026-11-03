package it.unifi.drivehub.presentation.view;

import it.unifi.drivehub.AppContext;
import it.unifi.drivehub.domain.operation.Rent;
import it.unifi.drivehub.domain.operation.Request;
import it.unifi.drivehub.domain.operation.RequestType;
import it.unifi.drivehub.domain.user.Salesman;
import it.unifi.drivehub.domain.vehicle.Vehicle;
import it.unifi.drivehub.domain.vehicle.VehicleState;
import it.unifi.drivehub.service.AuthService;
import it.unifi.drivehub.service.CatalogService;
import it.unifi.drivehub.service.CatalogService.VehicleRow;
import it.unifi.drivehub.service.OperationQueryService;
import it.unifi.drivehub.service.PurchaseService;
import it.unifi.drivehub.service.RentalService;
import it.unifi.drivehub.service.SaleToDealershipService;
import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.util.List;

/**
 * Dashboard per il Venditore (Salesman).
 * Adotta la Dependency Injection ricevendo tutti i servizi via costruttore.
 */
public final class SalesmanDashboardView extends BorderPane {

    private final Salesman salesman;
    private final AuthService authService;
    private final PurchaseService purchaseService;
    private final SaleToDealershipService saleToDealershipService;
    private final RentalService rentalService;
    private final CatalogService catalogService;
    private final OperationQueryService operationQueryService;
    private final Runnable onLogout;

    private TableView<Request> pendingTable;
    private AsyncLoad<List<VehicleRow>> inventoryLoad;
    private TableView<VehicleRow> inventoryTable;

    public SalesmanDashboardView(Salesman salesman, Runnable onLogout) {
        this(salesman,
                AppContext.getInstance().getAuthService(),
                AppContext.getInstance().getPurchaseService(),
                AppContext.getInstance().getSaleToDealershipService(),
                AppContext.getInstance().getRentalService(),
                AppContext.getInstance().getCatalogService(),
                AppContext.getInstance().getOperationQueryService(),
                onLogout);
    }

    public SalesmanDashboardView(Salesman salesman,
                                 AuthService authService,
                                 PurchaseService purchaseService,
                                 SaleToDealershipService saleToDealershipService,
                                 RentalService rentalService,
                                 CatalogService catalogService,
                                 OperationQueryService operationQueryService,
                                 Runnable onLogout) {
        this.salesman = salesman;
        this.authService = authService;
        this.purchaseService = purchaseService;
        this.saleToDealershipService = saleToDealershipService;
        this.rentalService = rentalService;
        this.catalogService = catalogService;
        this.operationQueryService = operationQueryService;
        this.onLogout = onLogout;
        buildUi();
    }

    private void buildUi() {
        HBox header = UiUtils.createHeader("DriveHub", "Area Venditore - " + salesman.getName() + " " + salesman.getSurname());
        HBox.setHgrow(header.getChildren().get(0), Priority.ALWAYS);

        Button btnAccount = new Button("Gestione Account");
        btnAccount.getStyleClass().add("button-secondary");
        btnAccount.setOnAction(e -> AccountManagementDialog.show((Stage) getScene().getWindow(), salesman, authService, onLogout, null));

        Button btnLogout = new Button("Disconnetti");
        btnLogout.getStyleClass().add("button-danger");
        btnLogout.setOnAction(e -> onLogout.run());

        HBox controls = new HBox(10, btnAccount, btnLogout);
        controls.setAlignment(Pos.CENTER_RIGHT);
        header.getChildren().add(controls);

        TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        Tab tabRequests = new Tab("Richieste in Sospeso", buildPendingRequestsTab());
        Tab tabInventory = new Tab("Gestione Stato Inventario (SALE / RENT)", buildInventoryTab());

        tabPane.getTabs().addAll(tabRequests, tabInventory);

        setTop(header);
        setCenter(tabPane);
    }

    private VBox buildPendingRequestsTab() {
        VBox box = new VBox(15);
        box.setPadding(new Insets(20));

        Label title = new Label("Richieste di Acquisto e Proposte di Vendita da Valutare");
        title.getStyleClass().add("card-title");

        pendingTable = new TableView<>();
        pendingTable.getStyleClass().add("agreement-requests");
        configurePendingTable();

        Button btnApprove = new Button("Approva Richiesta");
        btnApprove.getStyleClass().add("button-success");
        btnApprove.setOnAction(e -> handleApproveRequest());

        Button btnReject = new Button("Rifiuta Richiesta");
        btnReject.getStyleClass().add("button-danger");
        btnReject.setOnAction(e -> handleRejectRequest());

        HBox actions = new HBox(10, btnApprove, btnReject);
        box.getChildren().addAll(title, pendingTable, actions);
        VBox.setVgrow(pendingTable, Priority.ALWAYS);

        loadPendingRequests();
        return box;
    }

    private void loadPendingRequests() {
        pendingTable.getItems().setAll(operationQueryService.getPendingRequests());
    }

    private void handleApproveRequest() {
        Request req = pendingTable.getSelectionModel().getSelectedItem();
        if (req == null) {
            UiUtils.showError("Selezione mancante", "Seleziona una richiesta dalla tabella.");
            return;
        }

        try {
            if (req.getType() == RequestType.PURCHASE) {
                purchaseService.approvePurchase(req.getId(), salesman.getId());
                UiUtils.showInfo("Richiesta Approvata", "Il cliente ha 1 mese di tempo per pagare il saldo concordato: "
                        + UiUtils.formatCurrency(req.getRemainingAmount()));
            } else {
                saleToDealershipService.approveSaleProposal(req.getId(), salesman.getId());
                UiUtils.showInfo("Proposta Accettata", "Cessione accettata: pagamento accreditato al cliente e veicolo disponibile nel catalogo.");
            }
            loadPendingRequests();
            loadInventoryVehicles();
        } catch (Exception ex) {
            UiUtils.showError("Errore Approvazione", ex.getMessage());
        }
    }

    private void handleRejectRequest() {
        Request req = pendingTable.getSelectionModel().getSelectedItem();
        if (req == null) {
            UiUtils.showError("Selezione mancante", "Seleziona una richiesta dalla tabella.");
            return;
        }

        try {
            if (req.getType() == RequestType.PURCHASE) {
                purchaseService.rejectPurchase(req.getId());
                UiUtils.showInfo("Richiesta Rifiutata", "Richiesta d'acquisto rifiutata: caparra rimborsata integralmente al cliente e veicolo tornato SALE.");
            } else {
                saleToDealershipService.rejectSaleProposal(req.getId());
                UiUtils.showInfo("Proposta Rifiutata", "Cessione rifiutata senza pagamento. Lo storico del veicolo è conservato; un veicolo senza storico viene rimosso dal catalogo.");
            }
            loadPendingRequests();
            loadInventoryVehicles();
        } catch (Exception ex) {
            UiUtils.showError("Errore Rifiuto", ex.getMessage());
        }
    }

    private VBox buildInventoryTab() {
        VBox box = new VBox(15);
        box.setPadding(new Insets(20));

        Label title = new Label("Transizione Stato Veicoli (SALE <--> RENT)");
        title.getStyleClass().add("card-title");

        inventoryTable = new TableView<>();
        configureInventoryTable();

        Button btnToggle = new Button("Modifica Stato (Da RENT a SALE o viceversa)");
        btnToggle.setOnAction(e -> handleToggleState());

        inventoryLoad = new AsyncLoad<>("inventario", inventoryTable,
                () -> catalogService.getVehicleRows(null).stream().filter(row -> row.vehicle().getState() == VehicleState.SALE || row.vehicle().getState() == VehicleState.RENT).toList(),
                rows -> inventoryTable.getItems().setAll(rows));
        box.getChildren().addAll(title, inventoryLoad, inventoryTable, btnToggle);
        VBox.setVgrow(inventoryTable, Priority.ALWAYS);

        loadInventoryVehicles();
        return box;
    }

    private void loadInventoryVehicles() {
        inventoryLoad.load();
    }

    private void handleToggleState() {
        VehicleRow selected = inventoryTable.getSelectionModel().getSelectedItem();
        Vehicle v = selected == null ? null : selected.vehicle();
        if (v == null) {
            UiUtils.showError("Selezione mancante", "Seleziona un veicolo da modificare.");
            return;
        }

        VehicleState target = v.getState() == VehicleState.SALE ? VehicleState.RENT : VehicleState.SALE;

        // Se da RENT a SALE, verifica prenotazioni future di noleggio (Pagina 3)
        if (v.getState() == VehicleState.RENT && target == VehicleState.SALE) {
            List<Rent> futureRents = rentalService.getFutureRentsForVehicle(v.getId());
            String warningMsg = "Stai passando il veicolo #" + v.getId() + " da RENT a SALE.";
            if (!futureRents.isEmpty()) {
                warningMsg += "\nATTENZIONE: Sono presenti " + futureRents.size() + " prenotazioni future di noleggio su questo veicolo!\n"
                        + "Confermando il passaggio, tutte le prenotazioni future verranno annullate e i clienti verranno rimborsati del 100% dell'addebito.";
            }

            boolean confirm = UiUtils.showConfirmation("Conferma Cambio Stato", "Confermi la modifica dello stato?", warningMsg);
            if (!confirm) return;
        } else {
            boolean confirm = UiUtils.showConfirmation("Conferma Cambio Stato",
                    "Passare il veicolo #" + v.getId() + " a " + target + "?", "Il veicolo sarà immediatamente disponibile per il " + target + ".");
            if (!confirm) return;
        }

        try {
            int refunded = rentalService.changeVehicleRentalState(v.getId(), target);
            if (refunded > 0) {
                UiUtils.showInfo("Stato Aggiornato", "Stato modificato in " + target + ". Rimborsati con successo " + refunded + " noleggi futuri.");
            } else {
                UiUtils.showInfo("Stato Aggiornato", "Stato modificato in " + target + " con successo.");
            }
            loadInventoryVehicles();
        } catch (Exception ex) {
            UiUtils.showError("Errore", ex.getMessage());
        }
    }

    private void configurePendingTable() {
        TableColumn<Request, String> colId = new TableColumn<>("ID");
        colId.setCellValueFactory(data -> {
            if (data == null || data.getValue() == null) return new SimpleStringProperty("");
            return new SimpleStringProperty("#" + data.getValue().getId());
        });

        TableColumn<Request, String> colType = new TableColumn<>("Tipologia");
        colType.setCellValueFactory(data -> {
            if (data == null || data.getValue() == null) return new SimpleStringProperty("");
            return new SimpleStringProperty(
                    data.getValue().getType() == RequestType.PURCHASE ? "ACQUISTO CLIENTE" : "VENDITA DA CLIENTE"
            );
        });

        TableColumn<Request, String> colClient = new TableColumn<>("Cliente ID");
        colClient.setCellValueFactory(data -> {
            if (data == null || data.getValue() == null) return new SimpleStringProperty("");
            return new SimpleStringProperty("#" + data.getValue().getClientId());
        });

        TableColumn<Request, String> colVeh = new TableColumn<>("Veicolo ID");
        colVeh.setCellValueFactory(data -> {
            if (data == null || data.getValue() == null) return new SimpleStringProperty("");
            return new SimpleStringProperty("#" + data.getValue().getVehicleId());
        });

        TableColumn<Request, String> colTotal = new TableColumn<>("Totale concordato");
        colTotal.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getType() == RequestType.PURCHASE
                ? UiUtils.formatCurrency(data.getValue().getAgreedTotal()) : "—"));

        TableColumn<Request, String> colDeposit = new TableColumn<>("Acconto versato");
        colDeposit.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getType() == RequestType.PURCHASE
                ? UiUtils.formatCurrency(data.getValue().getDeposit()) : "—"));

        TableColumn<Request, String> colRemaining = new TableColumn<>("Saldo da pagare");
        colRemaining.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getType() == RequestType.PURCHASE
                ? UiUtils.formatCurrency(data.getValue().getRemainingAmount()) : "—"));

        TableColumn<Request, String> colAmount = new TableColumn<>("Stima cessione");
        colAmount.setCellValueFactory(data -> {
            if (data == null || data.getValue() == null || data.getValue().getDeposit() == null) return new SimpleStringProperty("");
            return new SimpleStringProperty(data.getValue().getType() == RequestType.SALE
                    ? UiUtils.formatCurrency(data.getValue().getDeposit().abs()) : "—");
        });

        pendingTable.getColumns().addAll(List.of(colId, colType, colClient, colVeh, colTotal, colDeposit, colRemaining, colAmount));
        pendingTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
    }

    private void configureInventoryTable() {
        TableColumn<VehicleRow, String> colId = new TableColumn<>("ID");
        colId.setCellValueFactory(data -> {
            if (data == null || data.getValue() == null) return new SimpleStringProperty("");
            return new SimpleStringProperty("#" + data.getValue().vehicle().getId());
        });

        TableColumn<VehicleRow, String> colModel = new TableColumn<>("Modello");
        colModel.setCellValueFactory(data -> {
            if (data == null || data.getValue() == null) return new SimpleStringProperty("");
            return new SimpleStringProperty(
                    data.getValue().modelName()
            );
        });

        TableColumn<VehicleRow, String> colVin = new TableColumn<>("VIN");
        colVin.setCellValueFactory(data -> {
            if (data == null || data.getValue() == null) return new SimpleStringProperty("");
            return new SimpleStringProperty(data.getValue().vehicle().getVin());
        });

        TableColumn<VehicleRow, String> colPlate = new TableColumn<>("Targa");
        colPlate.setCellValueFactory(data -> {
            if (data == null || data.getValue() == null) return new SimpleStringProperty("");
            return new SimpleStringProperty(data.getValue().vehicle().getLicencePlate() != null ? data.getValue().vehicle().getLicencePlate() : "N/D");
        });

        TableColumn<VehicleRow, String> colState = new TableColumn<>("Stato Attuale");
        colState.setCellValueFactory(data -> {
            if (data == null || data.getValue() == null || data.getValue().vehicle().getState() == null) return new SimpleStringProperty("");
            return new SimpleStringProperty(data.getValue().vehicle().getState().name());
        });

        inventoryTable.getColumns().addAll(List.of(colId, colModel, colVin, colPlate, colState));
        inventoryTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
    }
}
