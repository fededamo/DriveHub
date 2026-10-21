package it.unifi.ing.drivehub.presentation.controller;

import it.unifi.ing.drivehub.domain.users.Role;
import it.unifi.ing.drivehub.domain.vehicles.VehicleState;
import it.unifi.ing.drivehub.presentation.core.UiModels;
import it.unifi.ing.drivehub.presentation.navigation.NavigationManager;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.List;

public final class ManagerWorkspaceController extends AbstractController {
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @FXML private Label userLabel;
    @FXML private Label messageLabel;

    // --- Tab Veicoli e Sconti ---
    @FXML private TableView<UiModels.VehicleItem> vehicleTable;
    @FXML private TableColumn<UiModels.VehicleItem, String> plateColumn;
    @FXML private TableColumn<UiModels.VehicleItem, String> vinColumn;
    @FXML private TableColumn<UiModels.VehicleItem, String> modelColumn;
    @FXML private TableColumn<UiModels.VehicleItem, String> yearColumn;
    @FXML private TableColumn<UiModels.VehicleItem, String> kmColumn;
    @FXML private TableColumn<UiModels.VehicleItem, String> priceColumn;
    @FXML private TableColumn<UiModels.VehicleItem, String> stateColumn;

    @FXML private TextField discountPercentField;

    // Aggiunta veicolo
    @FXML private ComboBox<String> vehicleModelCombo;
    @FXML private TextField newVehicleVinField;
    @FXML private TextField newVehiclePlateField;
    @FXML private TextField newVehicleKmField;
    @FXML private TextField newVehiclePriceField;
    @FXML private TextField newVehicleYearField;
    @FXML private ComboBox<VehicleState> newVehicleStateCombo;

    // --- Tab Marche e Modelli ---
    @FXML private TableView<UiModels.BrandItem> brandTable;
    @FXML private TableColumn<UiModels.BrandItem, String> brandNameColumn;
    @FXML private TextField newBrandNameField;

    @FXML private TableView<UiModels.ModelItem> modelTable;
    @FXML private TableColumn<UiModels.ModelItem, String> modelIdColumn;
    @FXML private TableColumn<UiModels.ModelItem, String> modelBrandColumn;
    @FXML private TableColumn<UiModels.ModelItem, String> modelNameCol;
    @FXML private TableColumn<UiModels.ModelItem, String> modelPriceColumn;
    @FXML private TableColumn<UiModels.ModelItem, String> modelYearColumn;

    @FXML private TextField modelDiscountPercentField;
    @FXML private ComboBox<String> newModelBrandCombo;
    @FXML private TextField newModelNameField;
    @FXML private TextField newModelPriceField;
    @FXML private TextField newModelYearField;

    // --- Tab Vendite e Transazioni ---
    @FXML private TableView<UiModels.SaleItem> salesTable;
    @FXML private TableColumn<UiModels.SaleItem, String> saleIdColumn;
    @FXML private TableColumn<UiModels.SaleItem, String> saleVehicleColumn;
    @FXML private TableColumn<UiModels.SaleItem, String> saleClientColumn;
    @FXML private TableColumn<UiModels.SaleItem, String> saleSalesmanColumn;
    @FXML private TableColumn<UiModels.SaleItem, String> saleAmountColumn;
    @FXML private TableColumn<UiModels.SaleItem, String> saleDateColumn;
    @FXML private TableColumn<UiModels.SaleItem, String> saleStatusColumn;

    // --- Tab Profilo ---
    @FXML private PasswordField emailCurrentPasswordField;
    @FXML private TextField newEmailField;
    @FXML private PasswordField pwdCurrentPasswordField;
    @FXML private PasswordField newPasswordField;

    @FXML
    private void initialize() {
        // Setup veicoli
        plateColumn.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().plate() != null ? c.getValue().plate() : "Da immatricolare"));
        vinColumn.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().vin()));
        modelColumn.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().displayName()));
        yearColumn.setCellValueFactory(c -> new SimpleStringProperty(String.valueOf(c.getValue().year())));
        kmColumn.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().kilometers() + " km"));
        priceColumn.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().price() + " €"));
        stateColumn.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().state().name()));

        newVehicleStateCombo.setItems(FXCollections.observableArrayList(VehicleState.SALE, VehicleState.RENT));
        newVehicleStateCombo.setValue(VehicleState.SALE);

        // Setup marchi
        brandNameColumn.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().name()));

        // Setup modelli
        modelIdColumn.setCellValueFactory(c -> new SimpleStringProperty(String.valueOf(c.getValue().id())));
        modelBrandColumn.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().brand()));
        modelNameCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().name()));
        modelPriceColumn.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().releasePrice() + " €"));
        modelYearColumn.setCellValueFactory(c -> new SimpleStringProperty(String.valueOf(c.getValue().releaseYear())));

        // Setup vendite
        saleIdColumn.setCellValueFactory(c -> new SimpleStringProperty(String.valueOf(c.getValue().id())));
        saleVehicleColumn.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().vehicleDesc()));
        saleClientColumn.setCellValueFactory(c -> new SimpleStringProperty("#" + c.getValue().clientId()));
        saleSalesmanColumn.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().salesmanId() != null ? "#" + c.getValue().salesmanId() : "—"));
        saleAmountColumn.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().amount() + " €"));
        saleDateColumn.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().date().format(DATE_FMT)));
        saleStatusColumn.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().isDefaultFailure() ? "Mancato Saldo (Penale 20% trattenuta)"
                        : (c.getValue().isAcquisition() ? "Acquisto da Cliente" : "Vendita a Cliente")));
    }

    @Override
    protected void onContextReady() {
        if (context.session().isLoggedIn()) {
            userLabel.setText(session().displayName() + " (Manager - " + session().email() + ")");
            refresh();
        }
    }

    @FXML
    public void refresh() {
        clearMessages();
        gateway().processExpiredRequests();
        vehicleTable.setItems(FXCollections.observableArrayList(gateway().allVehicles()));
        salesTable.setItems(FXCollections.observableArrayList(gateway().allSales()));

        List<UiModels.BrandItem> brands = gateway().allBrands();
        brandTable.setItems(FXCollections.observableArrayList(brands));
        newModelBrandCombo.setItems(FXCollections.observableArrayList(brands.stream().map(UiModels.BrandItem::name).toList()));

        List<UiModels.ModelItem> models = gateway().allModels();
        modelTable.setItems(FXCollections.observableArrayList(models));
        vehicleModelCombo.setItems(FXCollections.observableArrayList(
                models.stream().map(m -> m.id() + ": " + m.brand() + " " + m.name()).toList()
        ));
    }

    // --- Gestione Sconti Veicolo ---
    @FXML
    public void applyVehicleDiscount() {
        clearMessages();
        UiModels.VehicleItem v = vehicleTable.getSelectionModel().getSelectedItem();
        if (v == null) {
            showError(messageLabel, "Seleziona un veicolo dalla tabella.");
            return;
        }
        String pStr = discountPercentField.getText();
        if (pStr == null || pStr.isBlank()) {
            showError(messageLabel, "Inserisci la percentuale di sconto (0 - 50%).");
            return;
        }
        try {
            int percentage = Integer.parseInt(pStr.trim());
            gateway().setVehicleDiscount(v.id(), percentage);
            showSuccess(messageLabel, "Sconto del " + percentage + "% applicato al veicolo #" + v.id() + " (" + v.displayName() + ").");
            discountPercentField.clear();
            refresh();
        } catch (Exception ex) {
            showError(messageLabel, ex);
        }
    }

    @FXML
    public void removeVehicleDiscount() {
        clearMessages();
        UiModels.VehicleItem v = vehicleTable.getSelectionModel().getSelectedItem();
        if (v == null) {
            showError(messageLabel, "Seleziona un veicolo dalla tabella.");
            return;
        }
        try {
            gateway().removeVehicleDiscount(v.id());
            showSuccess(messageLabel, "Sconto veicolo rimosso per #" + v.id() + ".");
            refresh();
        } catch (Exception ex) {
            showError(messageLabel, ex);
        }
    }

    // --- Gestione Aggiunta / Eliminazione Veicolo ---
    @FXML
    public void addVehicle() {
        clearMessages();
        String selectedModel = vehicleModelCombo.getValue();
        if (selectedModel == null) {
            showError(messageLabel, "Seleziona un modello dalla lista.");
            return;
        }
        try {
            long modelId = Long.parseLong(selectedModel.split(":")[0].trim());
            String vin = newVehicleVinField.getText().trim();
            String plate = newVehiclePlateField.getText().trim();
            if (plate.isBlank()) plate = null;
            int km = Integer.parseInt(newVehicleKmField.getText().trim());
            BigDecimal price = new BigDecimal(newVehiclePriceField.getText().trim());
            int year = Integer.parseInt(newVehicleYearField.getText().trim());
            VehicleState state = newVehicleStateCombo.getValue();

            gateway().addVehicle(modelId, vin, plate, km, price, year, state);
            showSuccess(messageLabel, "Veicolo inserito con successo nel parco auto!");

            newVehicleVinField.clear();
            newVehiclePlateField.clear();
            newVehicleKmField.clear();
            newVehiclePriceField.clear();
            newVehicleYearField.clear();
            refresh();
        } catch (Exception ex) {
            showError(messageLabel, ex);
        }
    }

    @FXML
    public void deleteVehicle() {
        clearMessages();
        UiModels.VehicleItem v = vehicleTable.getSelectionModel().getSelectedItem();
        if (v == null) {
            showError(messageLabel, "Seleziona un veicolo da eliminare.");
            return;
        }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Sei sicuro di voler eliminare il veicolo #" + v.id() + " (" + v.displayName() + ")?\nL'operazione è consentita solo se non ha test drive, noleggi o vendite collegate.",
                ButtonType.YES, ButtonType.NO);
        if (confirm.showAndWait().orElse(ButtonType.NO) == ButtonType.YES) {
            try {
                gateway().deleteVehicle(v.id());
                showSuccess(messageLabel, "Veicolo eliminato con successo.");
                refresh();
            } catch (Exception ex) {
                showError(messageLabel, ex);
            }
        }
    }

    // --- Gestione Marche e Modelli ---
    @FXML
    public void addBrand() {
        clearMessages();
        String name = newBrandNameField.getText();
        if (name == null || name.isBlank()) {
            showError(messageLabel, "Inserisci il nome del marchio.");
            return;
        }
        try {
            gateway().addBrand(name.trim());
            showSuccess(messageLabel, "Marchio '" + name + "' aggiunto con successo!");
            newBrandNameField.clear();
            refresh();
        } catch (Exception ex) {
            showError(messageLabel, ex);
        }
    }

    @FXML
    public void deleteBrand() {
        clearMessages();
        UiModels.BrandItem b = brandTable.getSelectionModel().getSelectedItem();
        if (b == null) {
            showError(messageLabel, "Seleziona un marchio dalla tabella da eliminare.");
            return;
        }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Sei sicuro di voler eliminare il marchio '" + b.name() + "'?\nL'operazione è permessa solo se non ci sono modelli associati.",
                ButtonType.YES, ButtonType.NO);
        if (confirm.showAndWait().orElse(ButtonType.NO) == ButtonType.YES) {
            try {
                gateway().deleteBrand(b.name());
                showSuccess(messageLabel, "Marchio eliminato con successo.");
                refresh();
            } catch (Exception ex) {
                showError(messageLabel, ex);
            }
        }
    }

    @FXML
    public void addModel() {
        clearMessages();
        String brand = newModelBrandCombo.getValue();
        String name = newModelNameField.getText();
        if (brand == null || name == null || name.isBlank()) {
            showError(messageLabel, "Seleziona un marchio e inserisci il nome del modello.");
            return;
        }
        try {
            BigDecimal price = new BigDecimal(newModelPriceField.getText().trim());
            int year = Integer.parseInt(newModelYearField.getText().trim());
            gateway().addModel(brand, name.trim(), price, year);
            showSuccess(messageLabel, "Modello '" + brand + " " + name + "' aggiunto con successo!");
            newModelNameField.clear();
            newModelPriceField.clear();
            newModelYearField.clear();
            refresh();
        } catch (Exception ex) {
            showError(messageLabel, ex);
        }
    }

    @FXML
    public void deleteModel() {
        clearMessages();
        UiModels.ModelItem m = modelTable.getSelectionModel().getSelectedItem();
        if (m == null) {
            showError(messageLabel, "Seleziona un modello dalla tabella da eliminare.");
            return;
        }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Sei sicuro di voler eliminare il modello '" + m.brand() + " " + m.name() + "'?\nL'operazione è permessa solo se non ci sono veicoli a catalogo associati.",
                ButtonType.YES, ButtonType.NO);
        if (confirm.showAndWait().orElse(ButtonType.NO) == ButtonType.YES) {
            try {
                gateway().deleteModel(m.id());
                showSuccess(messageLabel, "Modello eliminato con successo.");
                refresh();
            } catch (Exception ex) {
                showError(messageLabel, ex);
            }
        }
    }

    @FXML
    public void applyModelDiscount() {
        clearMessages();
        UiModels.ModelItem m = modelTable.getSelectionModel().getSelectedItem();
        if (m == null) {
            showError(messageLabel, "Seleziona un modello dalla tabella.");
            return;
        }
        String pStr = modelDiscountPercentField.getText();
        if (pStr == null || pStr.isBlank()) {
            showError(messageLabel, "Inserisci la percentuale di sconto (0 - 50%).");
            return;
        }
        try {
            int percentage = Integer.parseInt(pStr.trim());
            gateway().setModelDiscount(m.id(), percentage);
            showSuccess(messageLabel, "Sconto del " + percentage + "% applicato a tutti i veicoli del modello " + m.brand() + " " + m.name());
            modelDiscountPercentField.clear();
            refresh();
        } catch (Exception ex) {
            showError(messageLabel, ex);
        }
    }

    @FXML
    public void removeModelDiscount() {
        clearMessages();
        UiModels.ModelItem m = modelTable.getSelectionModel().getSelectedItem();
        if (m == null) {
            showError(messageLabel, "Seleziona un modello dalla tabella.");
            return;
        }
        try {
            gateway().removeModelDiscount(m.id());
            showSuccess(messageLabel, "Sconto modello rimosso per " + m.brand() + " " + m.name());
            refresh();
        } catch (Exception ex) {
            showError(messageLabel, ex);
        }
    }

    // --- Profilo ---
    @FXML
    public void updateEmail() {
        clearMessages();
        String currentPwd = emailCurrentPasswordField.getText();
        String newEmail = newEmailField.getText();
        if (currentPwd == null || currentPwd.isBlank() || newEmail == null || newEmail.isBlank()) {
            showError(messageLabel, "Inserisci la password attuale e la nuova email.");
            return;
        }
        try {
            gateway().updateEmail(session().userId(), Role.MANAGER, currentPwd.toCharArray(), newEmail);
            showSuccess(messageLabel, "Email aggiornata con successo in: " + newEmail);
            emailCurrentPasswordField.clear();
            newEmailField.clear();
            userLabel.setText(session().displayName() + " (Manager - " + newEmail + ")");
        } catch (Exception ex) {
            showError(messageLabel, ex);
        }
    }

    @FXML
    public void updatePassword() {
        clearMessages();
        String currentPwd = pwdCurrentPasswordField.getText();
        String newPwd = newPasswordField.getText();
        if (currentPwd == null || currentPwd.isBlank() || newPwd == null || newPwd.isBlank()) {
            showError(messageLabel, "Inserisci la password attuale e la nuova password.");
            return;
        }
        try {
            gateway().updatePassword(session().userId(), Role.MANAGER, currentPwd.toCharArray(), newPwd.toCharArray());
            showSuccess(messageLabel, "Password aggiornata con successo!");
            pwdCurrentPasswordField.clear();
            newPasswordField.clear();
        } catch (Exception ex) {
            showError(messageLabel, ex);
        }
    }

    @FXML
    public void logout() {
        NavigationManager.get().logout();
    }

    private void clearMessages() {
        messageLabel.setText("");
        messageLabel.getStyleClass().removeAll("danger-text", "success-text");
    }
}
