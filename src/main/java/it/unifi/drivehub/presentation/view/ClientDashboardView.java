package it.unifi.drivehub.presentation.view;

import it.unifi.drivehub.AppContext;
import it.unifi.drivehub.domain.operation.Rent;
import it.unifi.drivehub.domain.operation.Request;
import it.unifi.drivehub.domain.operation.RequestType;
import it.unifi.drivehub.domain.operation.TestDrive;
import it.unifi.drivehub.domain.user.Client;
import it.unifi.drivehub.domain.vehicle.Brand;
import it.unifi.drivehub.domain.vehicle.Model;
import it.unifi.drivehub.domain.vehicle.Vehicle;
import it.unifi.drivehub.domain.vehicle.VehicleState;
import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

/**
 * Dashboard per il Cliente (Customer).
 */
public final class ClientDashboardView extends BorderPane {

    private final Client client;
    private final Runnable onLogout;
    private Label balanceLabel;
    private TableView<Vehicle> saleTable;
    private TableView<Vehicle> rentTable;
    private TableView<Request> requestsTable;
    private TableView<Rent> myRentsTable;
    private TableView<TestDrive> myTestDrivesTable;

    public ClientDashboardView(Client client, Runnable onLogout) {
        this.client = client;
        this.onLogout = onLogout;
        buildUi();
        javafx.application.Platform.runLater(this::checkExpirationsOnStartup);
    }

    private void checkExpirationsOnStartup() {
        try {
            List<String> notices = AppContext.getInstance().getPurchaseService().checkAndHandleExpiredRequests(client.getId());
            if (!notices.isEmpty()) {
                StringBuilder sb = new StringBuilder();
                for (String n : notices) {
                    sb.append(n).append("\n\n");
                }
                Stage stage = getScene() != null ? (Stage) getScene().getWindow() : null;
                UiUtils.showInfo(stage, "Avviso Scadenza Richieste", sb.toString().trim());
                refreshBalance();
            }
        } catch (Throwable t) {
            System.err.println("Avviso: Errore durante il controllo scadenze: " + t.getMessage());
        }
    }

    private void buildUi() {
        // Header
        HBox header = UiUtils.createHeader("DriveHub", "Area Cliente - Benvenuto " + client.getName() + " " + client.getSurname());
        HBox.setHgrow(header.getChildren().get(0), Priority.ALWAYS);

        balanceLabel = new Label("Saldo: " + UiUtils.formatCurrency(client.getBankAccount() != null ? client.getBankAccount().getBalance() : BigDecimal.ZERO));
        balanceLabel.getStyleClass().add("balance-badge");

        Button btnTopUp = new Button("Ricarica");
        btnTopUp.getStyleClass().add("button-success");
        btnTopUp.setOnAction(e -> openTopUpDialog());

        Button btnAccount = new Button("Gestione Account");
        btnAccount.getStyleClass().add("button-secondary");
        btnAccount.setOnAction(e -> AccountManagementDialog.show((Stage) getScene().getWindow(), client, onLogout, u -> refreshBalance()));

        Button btnLogout = new Button("Disconnetti");
        btnLogout.getStyleClass().add("button-danger");
        btnLogout.setOnAction(e -> {
            AppContext.getInstance().logout();
            onLogout.run();
        });

        HBox userControls = new HBox(10, balanceLabel, btnTopUp, btnAccount, btnLogout);
        userControls.setAlignment(Pos.CENTER_RIGHT);
        header.getChildren().add(userControls);

        // TabPane Principale
        TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        Tab tabSale = new Tab("Auto in Vendita", buildSaleCatalogTab());
        Tab tabRent = new Tab("Auto a Noleggio", buildRentCatalogTab());
        Tab tabSellMine = new Tab("Vendi la tua Auto", buildSellMyCarTab());
        Tab tabMyOps = new Tab("Le Mie Operazioni", buildMyOperationsTab());

        tabPane.getTabs().addAll(tabSale, tabRent, tabSellMine, tabMyOps);

        setTop(header);
        setCenter(tabPane);
    }

    private void refreshBalance() {
        client.setBankAccount(AppContext.getInstance().getAuthService().getClientBankAccount(client.getId()));
        balanceLabel.setText("Saldo: " + UiUtils.formatCurrency(client.getBankAccount().getBalance()));
    }

    private void openTopUpDialog() {
        TextInputDialog dialog = new TextInputDialog("500.00");
        dialog.setTitle("Ricarica Saldo");
        dialog.setHeaderText("Ricarica il tuo conto corrente");
        dialog.setContentText("Inserisci l'importo (€):");
        dialog.showAndWait().ifPresent(val -> {
            try {
                BigDecimal amount = new BigDecimal(val.trim().replace(",", "."));
                AppContext.getInstance().getAuthService().topUpClientBalance(client.getId(), amount);
                refreshBalance();
                UiUtils.showInfo("Ricarica Effettuata", "Accreditati con successo € " + amount);
            } catch (Exception ex) {
                UiUtils.showError("Errore", ex.getMessage());
            }
        });
    }

    // --- TAB 1: CATALOGO AUTO IN VENDITA ---
    private VBox buildSaleCatalogTab() {
        VBox box = new VBox(15);
        box.setPadding(new Insets(20));

        saleTable = new TableView<>();
        configureVehicleTable(saleTable, true);

        Button btnBuy = new Button("Procedi all'Acquisto (Acconto 20%)");
        btnBuy.setOnAction(e -> handlePurchaseAction());

        Button btnTestDrive = new Button("Prenota Test Drive");
        btnTestDrive.getStyleClass().add("button-secondary");
        btnTestDrive.setOnAction(e -> handleTestDriveAction(saleTable.getSelectionModel().getSelectedItem()));

        HBox actions = new HBox(10, btnBuy, btnTestDrive);
        box.getChildren().addAll(saleTable, actions);
        VBox.setVgrow(saleTable, Priority.ALWAYS);

        loadSaleVehicles();
        return box;
    }

    private void loadSaleVehicles() {
        saleTable.getItems().setAll(AppContext.getInstance().getCatalogService().getVehiclesByState(VehicleState.SALE));
    }

    private void handlePurchaseAction() {
        Vehicle v = saleTable.getSelectionModel().getSelectedItem();
        if (v == null) {
            UiUtils.showError("Selezione mancante", "Seleziona un veicolo dalla tabella.");
            return;
        }

        BigDecimal fullPrice = AppContext.getInstance().getCatalogService().getEffectiveVehiclePrice(v);
        BigDecimal deposit = AppContext.getInstance().getPriceCalculatorService().calculatePurchaseDeposit(fullPrice);

        boolean confirm = UiUtils.showConfirmation("Conferma Avvio Operazione",
                "Procedere alla prenotazione dell'acquisto?",
                "Prezzo stimato: " + UiUtils.formatCurrency(fullPrice) +
                        "\nAcconto da versare ora (20%): " + UiUtils.formatCurrency(deposit) +
                        "\nIl veicolo passerà in stato BLOCKED e l'operazione verrà sottoposta a un venditore.");
        if (!confirm) return;

        try {
            AppContext.getInstance().getPurchaseService().initiatePurchase(v.getId(), client.getId());
            refreshBalance();
            loadSaleVehicles();
            refreshMyOperations();
            UiUtils.showInfo("Richiesta Inviata", "Acconto scalato con successo. La richiesta è ora in attesa di approvazione dal venditore.");
        } catch (Exception ex) {
            UiUtils.showError("Operazione Fallita", ex.getMessage());
        }
    }

    // --- TAB 2: CATALOGO NOLEGGIO ---
    private VBox buildRentCatalogTab() {
        VBox box = new VBox(15);
        box.setPadding(new Insets(20));

        rentTable = new TableView<>();
        configureVehicleTable(rentTable, false);

        Button btnRent = new Button("Prenota Noleggio");
        btnRent.setOnAction(e -> handleRentAction());

        Button btnTestDrive = new Button("Prenota Test Drive");
        btnTestDrive.getStyleClass().add("button-secondary");
        btnTestDrive.setOnAction(e -> handleTestDriveAction(rentTable.getSelectionModel().getSelectedItem()));

        HBox actions = new HBox(10, btnRent, btnTestDrive);
        box.getChildren().addAll(rentTable, actions);
        VBox.setVgrow(rentTable, Priority.ALWAYS);

        loadRentVehicles();
        return box;
    }

    private void loadRentVehicles() {
        rentTable.getItems().setAll(AppContext.getInstance().getCatalogService().getVehiclesByState(VehicleState.RENT));
    }

    private void handleRentAction() {
        Vehicle v = rentTable.getSelectionModel().getSelectedItem();
        if (v == null) {
            UiUtils.showError("Selezione mancante", "Seleziona un veicolo per il noleggio.");
            return;
        }

        // Dialog selezione date
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Prenotazione Noleggio");
        dialog.setHeaderText("Seleziona il periodo di noleggio per " + getVehicleDesc(v));

        DatePicker startPicker = new DatePicker(LocalDate.now().plusDays(1));
        DatePicker endPicker = new DatePicker(LocalDate.now().plusDays(3));
        Label costLabel = new Label("Costo Totale: --");
        costLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

        Runnable updateCost = () -> {
            LocalDate s = startPicker.getValue();
            LocalDate en = endPicker.getValue();
            if (s != null && en != null && !en.isBefore(s)) {
                try {
                    BigDecimal cost = AppContext.getInstance().getRentalService().calculateRentalCost(v.getId(), s, en);
                    costLabel.setText("Costo Totale: " + UiUtils.formatCurrency(cost) + " (100% addebitato all'atto della conferma)");
                } catch (Exception ex) {
                    costLabel.setText("Date non valide.");
                }
            }
        };

        startPicker.valueProperty().addListener((obs, oldV, newV) -> updateCost.run());
        endPicker.valueProperty().addListener((obs, oldV, newV) -> updateCost.run());
        updateCost.run();

        VBox content = new VBox(10, new Label("Data Inizio:"), startPicker, new Label("Data Fine:"), endPicker, costLabel);
        content.setPadding(new Insets(15));
        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.OK) {
                try {
                    AppContext.getInstance().getRentalService().bookRental(v.getId(), client.getId(), startPicker.getValue(), endPicker.getValue());
                    refreshBalance();
                    refreshMyOperations();
                    UiUtils.showInfo("Noleggio Confermato", "Prenotazione registrata con successo!");
                } catch (Exception ex) {
                    UiUtils.showError("Errore Noleggio", ex.getMessage());
                }
            }
        });
    }

    private void handleTestDriveAction(Vehicle v) {
        if (v == null) {
            UiUtils.showError("Selezione mancante", "Seleziona un veicolo dalla tabella.");
            return;
        }

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Prenotazione Test Drive");
        dialog.setHeaderText("Prenota giro di prova per " + getVehicleDesc(v));

        DatePicker datePicker = new DatePicker(LocalDate.now().plusDays(1));
        ComboBox<Integer> hourCombo = new ComboBox<>();
        for (int h = 9; h <= 16; h++) {
            hourCombo.getItems().add(h);
        }
        hourCombo.setValue(10);

        VBox content = new VBox(10,
                new Label("Data:"), datePicker,
                new Label("Ora di inizio (durata 1 ora, fascia 9:00 - 16:00):"), hourCombo
        );
        content.setPadding(new Insets(15));
        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.OK) {
                try {
                    LocalDateTime dt = LocalDateTime.of(datePicker.getValue(), LocalTime.of(hourCombo.getValue(), 0));
                    AppContext.getInstance().getTestDriveService().bookTestDrive(v.getId(), client.getId(), dt);
                    refreshMyOperations();
                    UiUtils.showInfo("Test Drive Prenotato", "Il giro di prova è stato confermato per il giorno " + dt.format(UiUtils.DATE_TIME_FORMATTER));
                } catch (Exception ex) {
                    UiUtils.showError("Errore Test Drive", ex.getMessage());
                }
            }
        });
    }

    // --- TAB 3: VENDI LA TUA AUTO ---
    private VBox buildSellMyCarTab() {
        VBox box = new VBox(15);
        box.setPadding(new Insets(25));
        box.setMaxWidth(600);

        Label lbl = new Label("Richiedi preventivo e vendi il tuo veicolo alla concessionaria");
        lbl.getStyleClass().add("card-title");

        ComboBox<Brand> brandCombo = new ComboBox<>();
        brandCombo.getItems().setAll(AppContext.getInstance().getCatalogService().getAllBrands());
        ComboBox<Model> modelCombo = new ComboBox<>();

        brandCombo.setOnAction(e -> {
            Brand b = brandCombo.getValue();
            if (b != null) {
                modelCombo.getItems().setAll(AppContext.getInstance().getCatalogService().getModelsByBrand(b.getName()));
            }
        });

        TextField vinField = new TextField();
        vinField.setPromptText("VIN (17 caratteri alfanumerici)");
        TextField plateField = new TextField();
        plateField.setPromptText("Targa (es. AB123CD)");
        TextField kmField = new TextField();
        kmField.setPromptText("Chilometri totali percorsi");
        TextField yearField = new TextField(String.valueOf(LocalDate.now().getYear()));
        yearField.setPromptText("Anno di immatricolazione");

        Label estimateLabel = new Label("Valore di stima calcolato: --");
        estimateLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #16a34a;");

        Button btnEstimate = new Button("Calcola Preventivo");
        btnEstimate.getStyleClass().add("button-secondary");
        btnEstimate.setOnAction(e -> {
            try {
                Model m = modelCombo.getValue();
                if (m == null) throw new IllegalArgumentException("Seleziona prima marca e modello.");
                int km = Integer.parseInt(kmField.getText().trim());
                int year = Integer.parseInt(yearField.getText().trim());
                BigDecimal est = AppContext.getInstance().getSaleToDealershipService().calculateEstimate(m.getId(), plateField.getText(), km, year);
                estimateLabel.setText("Valore di stima calcolato: " + UiUtils.formatCurrency(est));
            } catch (Exception ex) {
                UiUtils.showError("Errore Calcolo", ex.getMessage());
            }
        });

        Button btnSubmit = new Button("Invia Proposta di Vendita");
        btnSubmit.getStyleClass().add("button-success");
        btnSubmit.setOnAction(e -> {
            try {
                Model m = modelCombo.getValue();
                if (m == null) throw new IllegalArgumentException("Seleziona marca e modello.");
                int km = Integer.parseInt(kmField.getText().trim());
                int year = Integer.parseInt(yearField.getText().trim());
                AppContext.getInstance().getSaleToDealershipService().submitSaleProposal(
                        client.getId(), m.getId(), vinField.getText(), plateField.getText(), km, year
                );
                refreshMyOperations();
                UiUtils.showInfo("Proposta Inviata", "La proposta di vendita è stata registrata con successo (stato WAITING). Un venditore la esaminerà.");
            } catch (Exception ex) {
                UiUtils.showError("Errore Invio Proposta", ex.getMessage());
            }
        });

        box.getChildren().addAll(
                lbl,
                new Label("Marca:"), brandCombo,
                new Label("Modello:"), modelCombo,
                new Label("VIN:"), vinField,
                new Label("Targa:"), plateField,
                new Label("Chilometri:"), kmField,
                new Label("Anno:"), yearField,
                btnEstimate, estimateLabel, btnSubmit
        );
        return box;
    }

    // --- TAB 4: LE MIE OPERAZIONI ---
    private VBox buildMyOperationsTab() {
        VBox box = new VBox(20);
        box.setPadding(new Insets(20));

        // Tabella Richieste d'Acquisto
        Label reqTitle = new Label("Richieste d'Acquisto in Corso");
        reqTitle.getStyleClass().add("card-title");
        requestsTable = new TableView<>();
        configureRequestsTable();

        Button btnCompletePurchase = new Button("Paga Saldo Restante (80%) e Completa Acquisto");
        btnCompletePurchase.getStyleClass().add("button-success");
        btnCompletePurchase.setOnAction(e -> handleCompletePurchaseAction());

        // Tabella Noleggi
        Label rentTitle = new Label("I Miei Noleggi");
        rentTitle.getStyleClass().add("card-title");
        myRentsTable = new TableView<>();
        configureMyRentsTable();

        // Tabella Test Drive
        Label tdTitle = new Label("I Miei Test Drive");
        tdTitle.getStyleClass().add("card-title");
        myTestDrivesTable = new TableView<>();
        configureMyTestDrivesTable();

        Button btnCancelTd = new Button("Annulla Test Drive Selezionato");
        btnCancelTd.getStyleClass().add("button-danger");
        btnCancelTd.setOnAction(e -> {
            TestDrive td = myTestDrivesTable.getSelectionModel().getSelectedItem();
            if (td != null) {
                AppContext.getInstance().getTestDriveService().cancelTestDrive(td.getId(), client.getId());
                refreshMyOperations();
                UiUtils.showInfo("Test Drive Annullato", "Prenotazione annullata.");
            }
        });

        box.getChildren().addAll(
                reqTitle, requestsTable, btnCompletePurchase,
                new Separator(),
                rentTitle, myRentsTable,
                new Separator(),
                tdTitle, myTestDrivesTable, btnCancelTd
        );

        refreshMyOperations();
        return box;
    }

    private void handleCompletePurchaseAction() {
        Request r = requestsTable.getSelectionModel().getSelectedItem();
        if (r == null) {
            UiUtils.showError("Selezione mancante", "Seleziona una richiesta d'acquisto.");
            return;
        }
        if (r.getSalesmanId() == null) {
            UiUtils.showError("Non autorizzata", "La richiesta deve essere prima approvata da un venditore.");
            return;
        }
        if (r.isExpired()) {
            UiUtils.showError("Richiesta scaduta", "La data di scadenza (" + r.getExpDate() + ") è stata superata.");
            return;
        }

        boolean confirm = UiUtils.showConfirmation("Conferma Saldo Finale",
                "Completare l'acquisto del veicolo #" + r.getVehicleId() + "?",
                "Verrà scalato il restante 80% dell'importo dal tuo saldo e il veicolo diventerà di tua proprietà (stato SOLD).");
        if (!confirm) return;

        try {
            AppContext.getInstance().getPurchaseService().completePurchase(r.getId());
            refreshBalance();
            refreshMyOperations();
            loadSaleVehicles();
            UiUtils.showInfo("Congratulazioni!", "Acquisto completato con successo! Il veicolo è ufficialmente venduto.");
        } catch (Exception ex) {
            UiUtils.showError("Errore", ex.getMessage());
        }
    }

    private void refreshMyOperations() {
        requestsTable.getItems().setAll(
                AppContext.getInstance().getOperationQueryService().getClientRequests(client.getId())
                        .stream().filter(r -> r.getType() == RequestType.PURCHASE).toList()
        );
        myRentsTable.getItems().setAll(AppContext.getInstance().getOperationQueryService().getClientRents(client.getId()));
        myTestDrivesTable.getItems().setAll(AppContext.getInstance().getOperationQueryService().getClientTestDrives(client.getId()));
    }

    private void configureVehicleTable(TableView<Vehicle> table, boolean isSale) {
        TableColumn<Vehicle, String> colModel = new TableColumn<>("Modello");
        colModel.setCellValueFactory(data -> {
            if (data == null || data.getValue() == null) return new SimpleStringProperty("");
            return new SimpleStringProperty(getVehicleDesc(data.getValue()));
        });

        TableColumn<Vehicle, String> colYear = new TableColumn<>("Anno");
        colYear.setCellValueFactory(data -> {
            if (data == null || data.getValue() == null) return new SimpleStringProperty("");
            return new SimpleStringProperty(String.valueOf(data.getValue().getYear()));
        });

        TableColumn<Vehicle, String> colPlate = new TableColumn<>("Targa / Usato");
        colPlate.setCellValueFactory(data -> {
            if (data == null || data.getValue() == null) return new SimpleStringProperty("");
            return new SimpleStringProperty(data.getValue().isUsed() ? data.getValue().getLicencePlate() : "Nuovo (0 km)");
        });

        TableColumn<Vehicle, String> colKm = new TableColumn<>("Km");
        colKm.setCellValueFactory(data -> {
            if (data == null || data.getValue() == null) return new SimpleStringProperty("");
            return new SimpleStringProperty(String.valueOf(data.getValue().getKilometers()));
        });

        TableColumn<Vehicle, String> colPrice = new TableColumn<>(isSale ? "Prezzo Calcolato" : "Tariffa Giornaliera");
        colPrice.setCellValueFactory(data -> {
            if (data == null || data.getValue() == null) return new SimpleStringProperty("");
            try {
                if (isSale) {
                    BigDecimal effPrice = AppContext.getInstance().getCatalogService().getEffectiveVehiclePrice(data.getValue());
                    return new SimpleStringProperty(UiUtils.formatCurrency(effPrice));
                } else {
                    BigDecimal effPrice = AppContext.getInstance().getCatalogService().getEffectiveVehiclePrice(data.getValue());
                    BigDecimal dailyRate = AppContext.getInstance().getPriceCalculatorService().calculateDailyRentalRate(effPrice);
                    return new SimpleStringProperty(UiUtils.formatCurrency(dailyRate) + "/giorno");
                }
            } catch (Exception ex) {
                return new SimpleStringProperty("N/D");
            }
        });

        table.getColumns().addAll(colModel, colYear, colPlate, colKm, colPrice);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }

    private void configureRequestsTable() {
        TableColumn<Request, String> colId = new TableColumn<>("ID");
        colId.setCellValueFactory(data -> {
            if (data == null || data.getValue() == null) return new SimpleStringProperty("");
            return new SimpleStringProperty("#" + data.getValue().getId());
        });

        TableColumn<Request, String> colVeh = new TableColumn<>("Veicolo ID");
        colVeh.setCellValueFactory(data -> {
            if (data == null || data.getValue() == null) return new SimpleStringProperty("");
            return new SimpleStringProperty("#" + data.getValue().getVehicleId());
        });

        TableColumn<Request, String> colDep = new TableColumn<>("Acconto Versato");
        colDep.setCellValueFactory(data -> {
            if (data == null || data.getValue() == null) return new SimpleStringProperty("");
            return new SimpleStringProperty(UiUtils.formatCurrency(data.getValue().getDeposit()));
        });

        TableColumn<Request, String> colStatus = new TableColumn<>("Stato / Venditore");
        colStatus.setCellValueFactory(data -> {
            if (data == null || data.getValue() == null) return new SimpleStringProperty("");
            return new SimpleStringProperty(
                    data.getValue().getSalesmanId() != null ? "Approvata da Venditore #" + data.getValue().getSalesmanId() : "In attesa di approvazione"
            );
        });

        TableColumn<Request, String> colExp = new TableColumn<>("Scadenza Saldo");
        colExp.setCellValueFactory(data -> {
            if (data == null || data.getValue() == null) return new SimpleStringProperty("");
            return new SimpleStringProperty(
                    data.getValue().getExpDate() != null ? data.getValue().getExpDate().format(UiUtils.DATE_FORMATTER) : "N/D"
            );
        });

        requestsTable.getColumns().addAll(colId, colVeh, colDep, colStatus, colExp);
        requestsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }

    private void configureMyRentsTable() {
        TableColumn<Rent, String> colVeh = new TableColumn<>("Veicolo ID");
        colVeh.setCellValueFactory(data -> {
            if (data == null || data.getValue() == null) return new SimpleStringProperty("");
            return new SimpleStringProperty("#" + data.getValue().getVehicleId());
        });

        TableColumn<Rent, String> colAmount = new TableColumn<>("Importo Pagato");
        colAmount.setCellValueFactory(data -> {
            if (data == null || data.getValue() == null) return new SimpleStringProperty("");
            return new SimpleStringProperty(UiUtils.formatCurrency(data.getValue().getAmount()));
        });

        TableColumn<Rent, String> colStart = new TableColumn<>("Inizio");
        colStart.setCellValueFactory(data -> {
            if (data == null || data.getValue() == null) return new SimpleStringProperty("");
            return new SimpleStringProperty(data.getValue().getStartDate() != null ? data.getValue().getStartDate().format(UiUtils.DATE_FORMATTER) : "");
        });

        TableColumn<Rent, String> colEnd = new TableColumn<>("Fine");
        colEnd.setCellValueFactory(data -> {
            if (data == null || data.getValue() == null) return new SimpleStringProperty("");
            return new SimpleStringProperty(data.getValue().getEndDate() != null ? data.getValue().getEndDate().format(UiUtils.DATE_FORMATTER) : "");
        });

        myRentsTable.getColumns().addAll(colVeh, colAmount, colStart, colEnd);
        myRentsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }

    private void configureMyTestDrivesTable() {
        TableColumn<TestDrive, String> colVeh = new TableColumn<>("Veicolo ID");
        colVeh.setCellValueFactory(data -> {
            if (data == null || data.getValue() == null) return new SimpleStringProperty("");
            return new SimpleStringProperty("#" + data.getValue().getVehicleId());
        });

        TableColumn<TestDrive, String> colDate = new TableColumn<>("Data e Ora");
        colDate.setCellValueFactory(data -> {
            if (data == null || data.getValue() == null) return new SimpleStringProperty("");
            return new SimpleStringProperty(data.getValue().getDate() != null ? data.getValue().getDate().format(UiUtils.DATE_TIME_FORMATTER) : "");
        });

        myTestDrivesTable.getColumns().addAll(colVeh, colDate);
        myTestDrivesTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }

    private String getVehicleDesc(Vehicle v) {
        if (v == null) return "";
        return AppContext.getInstance().getCatalogService().getModelById(v.getModelId())
                .map(m -> m.getBrand() + " " + m.getName() + " (" + v.getYear() + ")")
                .orElse("Veicolo #" + v.getId());
    }
}

