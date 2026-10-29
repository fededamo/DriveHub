package it.unifi.drivehub.presentation.view;

import it.unifi.drivehub.AppContext;
import it.unifi.drivehub.domain.operation.Rent;
import it.unifi.drivehub.domain.operation.Request;
import it.unifi.drivehub.domain.operation.RequestType;
import it.unifi.drivehub.domain.user.Salesman;
import it.unifi.drivehub.domain.vehicle.Vehicle;
import it.unifi.drivehub.domain.vehicle.VehicleState;
import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.List;

/**
 * Dashboard per il Venditore (Salesman).
 */
public final class SalesmanDashboardView extends BorderPane {

    private final Salesman salesman;
    private final Runnable onLogout;
    private TableView<Request> pendingTable;
    private TableView<Vehicle> inventoryTable;

    public SalesmanDashboardView(Salesman salesman, Runnable onLogout) {
        this.salesman = salesman;
        this.onLogout = onLogout;
        buildUi();
    }

    private void buildUi() {
        HBox header = UiUtils.createHeader("DriveHub", "Area Venditore - " + salesman.getName() + " " + salesman.getSurname());
        HBox.setHgrow(header.getChildren().get(0), Priority.ALWAYS);

        Button btnAccount = new Button("Gestione Account");
        btnAccount.getStyleClass().add("button-secondary");
        btnAccount.setOnAction(e -> AccountManagementDialog.show((Stage) getScene().getWindow(), salesman, onLogout, null));

        Button btnLogout = new Button("Disconnetti");
        btnLogout.getStyleClass().add("button-danger");
        btnLogout.setOnAction(e -> {
            AppContext.getInstance().logout();
            onLogout.run();
        });

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
        pendingTable.getItems().setAll(AppContext.getInstance().getOperationQueryService().getPendingRequests());
    }

    private void handleApproveRequest() {
        Request req = pendingTable.getSelectionModel().getSelectedItem();
        if (req == null) {
            UiUtils.showError("Selezione mancante", "Seleziona una richiesta dalla tabella.");
            return;
        }

        try {
            if (req.getType() == RequestType.PURCHASE) {
                AppContext.getInstance().getPurchaseService().approvePurchase(req.getId(), salesman.getId());
                UiUtils.showInfo("Richiesta Approvata", "Richiesta d'acquisto approvata! Il cliente ha ora 1 mese di tempo per saldare il restante 80%.");
            } else {
                AppContext.getInstance().getSaleToDealershipService().approveSaleProposal(req.getId(), salesman.getId());
                UiUtils.showInfo("Proposta Accettata", "Proposta di vendita accettata: importo accreditato al cliente e veicolo aggiunto al catalogo in stato SALE.");
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
                AppContext.getInstance().getPurchaseService().rejectPurchase(req.getId());
                UiUtils.showInfo("Richiesta Rifiutata", "Richiesta d'acquisto rifiutata: caparra rimborsata integralmente al cliente e veicolo tornato SALE.");
            } else {
                AppContext.getInstance().getSaleToDealershipService().rejectSaleProposal(req.getId());
                UiUtils.showInfo("Proposta Rifiutata", "Proposta di vendita rifiutata.");
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

        box.getChildren().addAll(title, inventoryTable, btnToggle);
        VBox.setVgrow(inventoryTable, Priority.ALWAYS);

        loadInventoryVehicles();
        return box;
    }

    private void loadInventoryVehicles() {
        List<Vehicle> list = AppContext.getInstance().getCatalogService().getAllVehicles().stream()
                .filter(v -> v.getState() == VehicleState.SALE || v.getState() == VehicleState.RENT)
                .toList();
        inventoryTable.getItems().setAll(list);
    }

    private void handleToggleState() {
        Vehicle v = inventoryTable.getSelectionModel().getSelectedItem();
        if (v == null) {
            UiUtils.showError("Selezione mancante", "Seleziona un veicolo da modificare.");
            return;
        }

        VehicleState target = v.getState() == VehicleState.SALE ? VehicleState.RENT : VehicleState.SALE;

        // Se da RENT a SALE, verifica prenotazioni future di noleggio (Pagina 3)
        if (v.getState() == VehicleState.RENT && target == VehicleState.SALE) {
            List<Rent> futureRents = AppContext.getInstance().getRentalService().getFutureRentsForVehicle(v.getId());
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
            int refunded = AppContext.getInstance().getRentalService().changeVehicleRentalState(v.getId(), target);
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

        TableColumn<Request, String> colAmount = new TableColumn<>("Importo / Acconto");
        colAmount.setCellValueFactory(data -> {
            if (data == null || data.getValue() == null || data.getValue().getDeposit() == null) return new SimpleStringProperty("");
            return new SimpleStringProperty(UiUtils.formatCurrency(data.getValue().getDeposit().abs()));
        });

        pendingTable.getColumns().addAll(colId, colType, colClient, colVeh, colAmount);
        pendingTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }

    private void configureInventoryTable() {
        TableColumn<Vehicle, String> colId = new TableColumn<>("ID");
        colId.setCellValueFactory(data -> {
            if (data == null || data.getValue() == null) return new SimpleStringProperty("");
            return new SimpleStringProperty("#" + data.getValue().getId());
        });

        TableColumn<Vehicle, String> colModel = new TableColumn<>("Modello");
        colModel.setCellValueFactory(data -> {
            if (data == null || data.getValue() == null) return new SimpleStringProperty("");
            return new SimpleStringProperty(
                    AppContext.getInstance().getCatalogService().getModelById(data.getValue().getModelId())
                            .map(m -> m.getBrand() + " " + m.getName()).orElse("N/D")
            );
        });

        TableColumn<Vehicle, String> colVin = new TableColumn<>("VIN");
        colVin.setCellValueFactory(data -> {
            if (data == null || data.getValue() == null) return new SimpleStringProperty("");
            return new SimpleStringProperty(data.getValue().getVin());
        });

        TableColumn<Vehicle, String> colPlate = new TableColumn<>("Targa");
        colPlate.setCellValueFactory(data -> {
            if (data == null || data.getValue() == null) return new SimpleStringProperty("");
            return new SimpleStringProperty(data.getValue().getLicencePlate() != null ? data.getValue().getLicencePlate() : "N/D");
        });

        TableColumn<Vehicle, String> colState = new TableColumn<>("Stato Attuale");
        colState.setCellValueFactory(data -> {
            if (data == null || data.getValue() == null || data.getValue().getState() == null) return new SimpleStringProperty("");
            return new SimpleStringProperty(data.getValue().getState().name());
        });

        inventoryTable.getColumns().addAll(colId, colModel, colVin, colPlate, colState);
        inventoryTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }
}

