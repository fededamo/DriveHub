package it.unifi.drivehub.presentation.view;

import it.unifi.drivehub.AppContext;
import it.unifi.drivehub.domain.discount.ModelDiscount;
import it.unifi.drivehub.domain.discount.VehicleDiscount;
import it.unifi.drivehub.domain.operation.Sale;
import it.unifi.drivehub.domain.user.Manager;
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

/**
 * Dashboard per il Dirigente (Manager).
 * Include gestione completa e protetta di Marchi, Modelli, Veicoli, Sconti e Registro Vendite.
 */
public final class ManagerDashboardView extends BorderPane {

    private final Manager manager;
    private final Runnable onLogout;

    private TableView<Brand> brandTable;
    private TableView<Model> modelTable;
    private TableView<Vehicle> vehicleTable;
    private TableView<ModelDiscount> modelDiscTable;
    private TableView<VehicleDiscount> vehicleDiscTable;
    private TableView<Sale> saleTable;

    public ManagerDashboardView(Manager manager, Runnable onLogout) {
        this.manager = manager;
        this.onLogout = onLogout;
        buildUi();
    }

    private void buildUi() {
        HBox header = UiUtils.createHeader("DriveHub", "Area Direzione (Manager) - " + manager.getName() + " " + manager.getSurname());
        HBox.setHgrow(header.getChildren().get(0), Priority.ALWAYS);

        Button btnAccount = new Button("Gestione Account");
        btnAccount.getStyleClass().add("button-secondary");
        btnAccount.setOnAction(e -> AccountManagementDialog.show((Stage) getScene().getWindow(), manager, onLogout, null));

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

        Tab tabBrands = new Tab("Marchi", buildBrandsTab());
        Tab tabModels = new Tab("Modelli", buildModelsTab());
        Tab tabVehicles = new Tab("Veicoli", buildVehiclesTab());
        Tab tabDiscounts = new Tab("Sconti", buildDiscountsTab());
        Tab tabSales = new Tab("Registro Vendite & Penali", buildSalesTab());

        tabPane.getTabs().addAll(tabBrands, tabModels, tabVehicles, tabDiscounts, tabSales);

        setTop(header);
        setCenter(tabPane);
    }

    // --- TAB MARCHI ---
    private VBox buildBrandsTab() {
        VBox box = new VBox(15);
        box.setPadding(new Insets(20));

        brandTable = new TableView<>();
        TableColumn<Brand, String> colName = new TableColumn<>("Nome Brand");
        colName.setCellValueFactory(d -> new SimpleStringProperty(d != null && d.getValue() != null ? d.getValue().getName() : ""));
        brandTable.getColumns().add(colName);
        brandTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        HBox addBox = new HBox(10);
        TextField nameField = new TextField();
        nameField.setPromptText("Nome nuovo brand");
        Button btnAdd = new Button("Aggiungi Brand");
        btnAdd.setOnAction(e -> {
            try {
                AppContext.getInstance().getCatalogService().addBrand(nameField.getText());
                nameField.clear();
                loadBrands();
                UiUtils.showInfo("Riuscito", "Brand aggiunto con successo.");
            } catch (Exception ex) {
                UiUtils.showError("Errore", ex.getMessage());
            }
        });

        Button btnDelete = new Button("Elimina Selezionato");
        btnDelete.getStyleClass().add("button-danger");
        btnDelete.setOnAction(e -> {
            Brand b = brandTable.getSelectionModel().getSelectedItem();
            if (b != null) {
                try {
                    AppContext.getInstance().getCatalogService().deleteBrand(b.getName());
                    loadBrands();
                    UiUtils.showInfo("Riuscito", "Brand eliminato.");
                } catch (Exception ex) {
                    UiUtils.showError("Errore Eliminazione", ex.getMessage());
                }
            }
        });

        addBox.getChildren().addAll(nameField, btnAdd, btnDelete);
        box.getChildren().addAll(brandTable, addBox);
        VBox.setVgrow(brandTable, Priority.ALWAYS);

        loadBrands();
        return box;
    }

    private void loadBrands() {
        brandTable.getItems().setAll(AppContext.getInstance().getCatalogService().getAllBrands());
    }

    // --- TAB MODELLI ---
    private VBox buildModelsTab() {
        VBox box = new VBox(15);
        box.setPadding(new Insets(20));

        modelTable = new TableView<>();
        TableColumn<Model, String> colId = new TableColumn<>("ID");
        colId.setCellValueFactory(d -> new SimpleStringProperty(d != null && d.getValue() != null ? "#" + d.getValue().getId() : ""));
        TableColumn<Model, String> colBrand = new TableColumn<>("Brand");
        colBrand.setCellValueFactory(d -> new SimpleStringProperty(d != null && d.getValue() != null ? d.getValue().getBrand() : ""));
        TableColumn<Model, String> colName = new TableColumn<>("Modello");
        colName.setCellValueFactory(d -> new SimpleStringProperty(d != null && d.getValue() != null ? d.getValue().getName() : ""));
        TableColumn<Model, String> colPrice = new TableColumn<>("Prezzo Listino");
        colPrice.setCellValueFactory(d -> new SimpleStringProperty(d != null && d.getValue() != null ? UiUtils.formatCurrency(d.getValue().getReleasePrice()) : ""));
        TableColumn<Model, String> colYear = new TableColumn<>("Anno Rilascio");
        colYear.setCellValueFactory(d -> new SimpleStringProperty(d != null && d.getValue() != null ? String.valueOf(d.getValue().getReleaseYear()) : ""));

        modelTable.getColumns().addAll(colId, colBrand, colName, colPrice, colYear);
        modelTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        HBox addBox = new HBox(10);
        ComboBox<Brand> brandCombo = new ComboBox<>();
        brandCombo.getItems().setAll(AppContext.getInstance().getCatalogService().getAllBrands());
        TextField nameField = new TextField();
        nameField.setPromptText("Nome modello");
        TextField priceField = new TextField();
        priceField.setPromptText("Prezzo (es. 25000)");
        TextField yearField = new TextField(String.valueOf(LocalDate.now().getYear()));
        yearField.setPromptText("Anno");

        Button btnAdd = new Button("Aggiungi Modello");
        btnAdd.setOnAction(e -> {
            try {
                if (brandCombo.getValue() == null) throw new IllegalArgumentException("Seleziona il brand.");
                BigDecimal price = new BigDecimal(priceField.getText().trim().replace(",", "."));
                int year = Integer.parseInt(yearField.getText().trim());
                AppContext.getInstance().getCatalogService().addModel(brandCombo.getValue().getName(), nameField.getText(), price, year);
                nameField.clear(); priceField.clear();
                loadModels();
                UiUtils.showInfo("Riuscito", "Modello inserito con successo.");
            } catch (Exception ex) {
                UiUtils.showError("Errore", ex.getMessage());
            }
        });

        Button btnDelete = new Button("Elimina Selezionato");
        btnDelete.getStyleClass().add("button-danger");
        btnDelete.setOnAction(e -> {
            Model m = modelTable.getSelectionModel().getSelectedItem();
            if (m != null) {
                try {
                    AppContext.getInstance().getCatalogService().deleteModel(m.getId());
                    loadModels();
                    UiUtils.showInfo("Riuscito", "Modello eliminato.");
                } catch (Exception ex) {
                    UiUtils.showError("Errore Eliminazione", ex.getMessage());
                }
            }
        });

        addBox.getChildren().addAll(brandCombo, nameField, priceField, yearField, btnAdd, btnDelete);
        box.getChildren().addAll(modelTable, addBox);
        VBox.setVgrow(modelTable, Priority.ALWAYS);

        loadModels();
        return box;
    }

    private void loadModels() {
        modelTable.getItems().setAll(AppContext.getInstance().getCatalogService().getAllModels());
    }

    // --- TAB VEICOLI ---
    private VBox buildVehiclesTab() {
        VBox box = new VBox(15);
        box.setPadding(new Insets(20));

        vehicleTable = new TableView<>();
        TableColumn<Vehicle, String> colId = new TableColumn<>("ID");
        colId.setCellValueFactory(d -> new SimpleStringProperty(d != null && d.getValue() != null ? "#" + d.getValue().getId() : ""));
        TableColumn<Vehicle, String> colMod = new TableColumn<>("Modello");
        colMod.setCellValueFactory(d -> {
            if (d == null || d.getValue() == null) return new SimpleStringProperty("");
            return new SimpleStringProperty(
                    AppContext.getInstance().getCatalogService().getModelById(d.getValue().getModelId())
                            .map(m -> m.getBrand() + " " + m.getName()).orElse("N/D")
            );
        });
        TableColumn<Vehicle, String> colVin = new TableColumn<>("VIN");
        colVin.setCellValueFactory(d -> new SimpleStringProperty(d != null && d.getValue() != null ? d.getValue().getVin() : ""));
        TableColumn<Vehicle, String> colPlate = new TableColumn<>("Targa");
        colPlate.setCellValueFactory(d -> new SimpleStringProperty(d != null && d.getValue() != null && d.getValue().getLicencePlate() != null ? d.getValue().getLicencePlate() : "N/D"));
        TableColumn<Vehicle, String> colKm = new TableColumn<>("Km");
        colKm.setCellValueFactory(d -> new SimpleStringProperty(d != null && d.getValue() != null ? String.valueOf(d.getValue().getKilometers()) : ""));
        TableColumn<Vehicle, String> colState = new TableColumn<>("Stato");
        colState.setCellValueFactory(d -> new SimpleStringProperty(d != null && d.getValue() != null && d.getValue().getState() != null ? d.getValue().getState().name() : ""));

        vehicleTable.getColumns().addAll(colId, colMod, colVin, colPlate, colKm, colState);
        vehicleTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        HBox addBox = new HBox(8);
        ComboBox<Model> modelCombo = new ComboBox<>();
        modelCombo.getItems().setAll(AppContext.getInstance().getCatalogService().getAllModels());
        TextField vinField = new TextField();
        vinField.setPromptText("VIN (17 car.)");
        TextField plateField = new TextField();
        plateField.setPromptText("Targa (o vuoto)");
        TextField kmField = new TextField("0");
        kmField.setPromptText("Km");
        TextField priceField = new TextField();
        priceField.setPromptText("Prezzo");
        TextField yearField = new TextField(String.valueOf(LocalDate.now().getYear()));
        ComboBox<VehicleState> stateCombo = new ComboBox<>();
        stateCombo.getItems().addAll(VehicleState.SALE, VehicleState.RENT);
        stateCombo.setValue(VehicleState.SALE);

        Button btnAdd = new Button("Aggiungi Veicolo");
        btnAdd.setOnAction(e -> {
            try {
                if (modelCombo.getValue() == null) throw new IllegalArgumentException("Seleziona il modello.");
                BigDecimal price = new BigDecimal(priceField.getText().trim().replace(",", "."));
                int km = Integer.parseInt(kmField.getText().trim());
                int year = Integer.parseInt(yearField.getText().trim());
                AppContext.getInstance().getCatalogService().addVehicle(
                        modelCombo.getValue().getId(), vinField.getText(), plateField.getText(), km, price, year, stateCombo.getValue()
                );
                loadVehicles();
                UiUtils.showInfo("Riuscito", "Veicolo inserito a catalogo.");
            } catch (Exception ex) {
                UiUtils.showError("Errore", ex.getMessage());
            }
        });

        Button btnDelete = new Button("Elimina Selezionato");
        btnDelete.getStyleClass().add("button-danger");
        btnDelete.setOnAction(e -> {
            Vehicle v = vehicleTable.getSelectionModel().getSelectedItem();
            if (v != null) {
                try {
                    AppContext.getInstance().getCatalogService().deleteVehicle(v.getId());
                    loadVehicles();
                    UiUtils.showInfo("Riuscito", "Veicolo eliminato.");
                } catch (Exception ex) {
                    UiUtils.showError("Errore Eliminazione", ex.getMessage());
                }
            }
        });

        addBox.getChildren().addAll(modelCombo, vinField, plateField, kmField, priceField, yearField, stateCombo, btnAdd, btnDelete);
        box.getChildren().addAll(vehicleTable, addBox);
        VBox.setVgrow(vehicleTable, Priority.ALWAYS);

        loadVehicles();
        return box;
    }

    private void loadVehicles() {
        vehicleTable.getItems().setAll(AppContext.getInstance().getCatalogService().getAllVehicles());
    }

    // --- TAB SCONTI ---
    private HBox buildDiscountsTab() {
        HBox box = new HBox(20);
        box.setPadding(new Insets(20));

        // Sconti Modello
        VBox modelBox = new VBox(10);
        Label mTitle = new Label("Sconti su Modelli (0 - 50%)");
        mTitle.getStyleClass().add("card-title");
        modelDiscTable = new TableView<>();
        TableColumn<ModelDiscount, String> mColMod = new TableColumn<>("Modello");
        mColMod.setCellValueFactory(d -> {
            if (d == null || d.getValue() == null) return new SimpleStringProperty("");
            return new SimpleStringProperty(
                    AppContext.getInstance().getCatalogService().getModelById(d.getValue().getModelId())
                            .map(m -> m.getBrand() + " " + m.getName()).orElse("#" + d.getValue().getModelId())
            );
        });
        TableColumn<ModelDiscount, String> mColVal = new TableColumn<>("Sconto %");
        mColVal.setCellValueFactory(d -> new SimpleStringProperty(d != null && d.getValue() != null ? d.getValue().getValue() + "%" : ""));
        modelDiscTable.getColumns().addAll(mColMod, mColVal);
        modelDiscTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        HBox mControls = new HBox(8);
        ComboBox<Model> mCombo = new ComboBox<>();
        mCombo.getItems().setAll(AppContext.getInstance().getCatalogService().getAllModels());
        TextField mValField = new TextField();
        mValField.setPromptText("% (0-50)");
        Button btnSetM = new Button("Salva");
        btnSetM.setOnAction(e -> {
            try {
                if (mCombo.getValue() == null) throw new IllegalArgumentException("Seleziona modello.");
                int val = Integer.parseInt(mValField.getText().trim());
                AppContext.getInstance().getCatalogService().setModelDiscount(mCombo.getValue().getId(), val);
                loadDiscounts();
                UiUtils.showInfo("Riuscito", "Sconto modello aggiornato.");
            } catch (Exception ex) {
                UiUtils.showError("Errore", ex.getMessage());
            }
        });
        Button btnDelM = new Button("Rimuovi");
        btnDelM.getStyleClass().add("button-danger");
        btnDelM.setOnAction(e -> {
            ModelDiscount md = modelDiscTable.getSelectionModel().getSelectedItem();
            if (md != null) {
                AppContext.getInstance().getCatalogService().removeModelDiscount(md.getModelId());
                loadDiscounts();
            }
        });
        mControls.getChildren().addAll(mCombo, mValField, btnSetM, btnDelM);
        modelBox.getChildren().addAll(mTitle, modelDiscTable, mControls);
        HBox.setHgrow(modelBox, Priority.ALWAYS);

        // Sconti Veicolo
        VBox vehBox = new VBox(10);
        Label vTitle = new Label("Sconti su Singoli Veicoli (0 - 50%)");
        vTitle.getStyleClass().add("card-title");
        vehicleDiscTable = new TableView<>();
        TableColumn<VehicleDiscount, String> vColVeh = new TableColumn<>("Veicolo ID");
        vColVeh.setCellValueFactory(d -> new SimpleStringProperty(d != null && d.getValue() != null ? "#" + d.getValue().getVehicleId() : ""));
        TableColumn<VehicleDiscount, String> vColVal = new TableColumn<>("Sconto %");
        vColVal.setCellValueFactory(d -> new SimpleStringProperty(d != null && d.getValue() != null ? d.getValue().getValue() + "%" : ""));
        vehicleDiscTable.getColumns().addAll(vColVeh, vColVal);
        vehicleDiscTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        HBox vControls = new HBox(8);
        TextField vIdField = new TextField();
        vIdField.setPromptText("Veicolo ID");
        TextField vValField = new TextField();
        vValField.setPromptText("% (0-50)");
        Button btnSetV = new Button("Salva");
        btnSetV.setOnAction(e -> {
            try {
                long vid = Long.parseLong(vIdField.getText().trim());
                int val = Integer.parseInt(vValField.getText().trim());
                AppContext.getInstance().getCatalogService().setVehicleDiscount(vid, val);
                loadDiscounts();
                UiUtils.showInfo("Riuscito", "Sconto veicolo aggiornato.");
            } catch (Exception ex) {
                UiUtils.showError("Errore", ex.getMessage());
            }
        });
        Button btnDelV = new Button("Rimuovi");
        btnDelV.getStyleClass().add("button-danger");
        btnDelV.setOnAction(e -> {
            VehicleDiscount vd = vehicleDiscTable.getSelectionModel().getSelectedItem();
            if (vd != null) {
                AppContext.getInstance().getCatalogService().removeVehicleDiscount(vd.getVehicleId());
                loadDiscounts();
            }
        });
        vControls.getChildren().addAll(vIdField, vValField, btnSetV, btnDelV);
        vehBox.getChildren().addAll(vTitle, vehicleDiscTable, vControls);
        HBox.setHgrow(vehBox, Priority.ALWAYS);

        box.getChildren().addAll(modelBox, vehBox);
        loadDiscounts();
        return box;
    }

    private void loadDiscounts() {
        modelDiscTable.getItems().setAll(AppContext.getInstance().getCatalogService().getAllModelDiscounts());
        vehicleDiscTable.getItems().setAll(AppContext.getInstance().getCatalogService().getAllVehicleDiscounts());
    }

    // --- TAB REGISTRO VENDITE ---
    private VBox buildSalesTab() {
        VBox box = new VBox(15);
        box.setPadding(new Insets(20));

        Label title = new Label("Registro Contabile Operazioni (Vendite, Acquisti, Penali)");
        title.getStyleClass().add("card-title");

        saleTable = new TableView<>();
        TableColumn<Sale, String> colId = new TableColumn<>("ID");
        colId.setCellValueFactory(d -> new SimpleStringProperty(d != null && d.getValue() != null ? "#" + d.getValue().getId() : ""));

        TableColumn<Sale, String> colType = new TableColumn<>("Tipologia");
        colType.setCellValueFactory(d -> {
            if (d == null || d.getValue() == null) return new SimpleStringProperty("");
            Sale s = d.getValue();
            if (s.getSalesmanId() == null && s.getVehicleId() == null) {
                return new SimpleStringProperty("PENALE SCADENZA (Quota trattenuta)");
            } else if (s.getAmount() != null && s.getAmount().compareTo(BigDecimal.ZERO) < 0) {
                return new SimpleStringProperty("ACQUISTO DA CLIENTE");
            } else {
                return new SimpleStringProperty("VENDITA A CLIENTE");
            }
        });

        TableColumn<Sale, String> colDate = new TableColumn<>("Data Transazione");
        colDate.setCellValueFactory(d -> new SimpleStringProperty(
                d != null && d.getValue() != null && d.getValue().getDate() != null ? d.getValue().getDate().format(UiUtils.DATE_TIME_FORMATTER) : ""
        ));

        TableColumn<Sale, String> colClient = new TableColumn<>("Cliente ID");
        colClient.setCellValueFactory(d -> new SimpleStringProperty(
                d != null && d.getValue() != null ? "#" + d.getValue().getClientId() : ""
        ));

        TableColumn<Sale, String> colSalesman = new TableColumn<>("Venditore");
        colSalesman.setCellValueFactory(d -> new SimpleStringProperty(
                d != null && d.getValue() != null && d.getValue().getSalesmanId() != null ? "#" + d.getValue().getSalesmanId() : "N/D"
        ));

        TableColumn<Sale, String> colVehicle = new TableColumn<>("Veicolo");
        colVehicle.setCellValueFactory(d -> new SimpleStringProperty(
                d != null && d.getValue() != null && d.getValue().getVehicleId() != null ? "#" + d.getValue().getVehicleId() : "N/D"
        ));

        TableColumn<Sale, String> colAmount = new TableColumn<>("Importo");
        colAmount.setCellValueFactory(d -> new SimpleStringProperty(
                d != null && d.getValue() != null && d.getValue().getAmount() != null ? UiUtils.formatCurrency(d.getValue().getAmount()) : ""
        ));

        saleTable.getColumns().addAll(colId, colType, colDate, colClient, colSalesman, colVehicle, colAmount);
        saleTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        box.getChildren().addAll(title, saleTable);
        VBox.setVgrow(saleTable, Priority.ALWAYS);

        loadSales();
        return box;
    }

    private void loadSales() {
        saleTable.getItems().setAll(AppContext.getInstance().getOperationQueryService().getAllSales());
    }
}

