package it.unifi.ing.drivehub.presentation.controller;

import it.unifi.ing.drivehub.domain.users.Role;
import it.unifi.ing.drivehub.domain.vehicles.VehicleState;
import it.unifi.ing.drivehub.presentation.core.UiModels;
import it.unifi.ing.drivehub.presentation.navigation.NavigationManager;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.util.Callback;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public final class CustomerWorkspaceController extends AbstractController {
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm");

    @FXML private Label userLabel;
    @FXML private TabPane workspaceTabs;

    // Catalogo
    @FXML private TextField catalogSearchField;
    @FXML private ComboBox<VehicleState> vehicleStateCombo;
    @FXML private TableView<UiModels.VehicleItem> catalogTable;
    @FXML private TableColumn<UiModels.VehicleItem, String> catalogPlateColumn;
    @FXML private TableColumn<UiModels.VehicleItem, String> catalogVinColumn;
    @FXML private TableColumn<UiModels.VehicleItem, String> catalogModelColumn;
    @FXML private TableColumn<UiModels.VehicleItem, String> catalogYearColumn;
    @FXML private TableColumn<UiModels.VehicleItem, String> catalogMileageColumn;
    @FXML private TableColumn<UiModels.VehicleItem, String> catalogPriceColumn;
    @FXML private TableColumn<UiModels.VehicleItem, String> catalogStateColumn;

    // Richieste e Acquisti
    @FXML private TableView<UiModels.RequestItem> requestsTable;
    @FXML private TableColumn<UiModels.RequestItem, String> requestVehicleColumn;
    @FXML private TableColumn<UiModels.RequestItem, String> requestTypeColumn;
    @FXML private TableColumn<UiModels.RequestItem, String> requestDepositColumn;
    @FXML private TableColumn<UiModels.RequestItem, String> requestExpDateColumn;

    @FXML private TableView<UiModels.SaleItem> salesTable;
    @FXML private TableColumn<UiModels.SaleItem, String> saleVehicleColumn;
    @FXML private TableColumn<UiModels.SaleItem, String> saleAmountColumn;
    @FXML private TableColumn<UiModels.SaleItem, String> saleDateColumn;
    @FXML private TableColumn<UiModels.SaleItem, String> saleStatusColumn;

    // Noleggi
    @FXML private Label rentalSelectedVehicleLabel;
    @FXML private DatePicker rentalStartDatePicker;
    @FXML private DatePicker rentalEndDatePicker;
    @FXML private Label rentalQuoteLabel;
    @FXML private TableView<UiModels.RentItem> rentalsTable;
    @FXML private TableColumn<UiModels.RentItem, String> rentalVehicleColumn;
    @FXML private TableColumn<UiModels.RentItem, String> rentalPeriodColumn;
    @FXML private TableColumn<UiModels.RentItem, String> rentalAmountColumn;

    // Test Drive
    @FXML private Label testDriveSelectedVehicleLabel;
    @FXML private DatePicker testDriveDatePicker;
    @FXML private ComboBox<String> testDriveHourCombo;
    @FXML private TableView<UiModels.TestDriveItem> testDrivesTable;
    @FXML private TableColumn<UiModels.TestDriveItem, String> testDriveVehicleColumn;
    @FXML private TableColumn<UiModels.TestDriveItem, String> testDriveDateColumn;
    @FXML private TableColumn<UiModels.TestDriveItem, String> testDriveTimeColumn;

    // Vendita veicolo da cliente
    @FXML private ComboBox<UiModels.BrandItem> brandComboBox;
    @FXML private ComboBox<UiModels.ModelItem> modelComboBox;
    @FXML private TextField newVinField;
    @FXML private TextField newPlateField;
    @FXML private TextField newKmField;
    @FXML private TextField newVehicleYearField;
    @FXML private TextField newPriceField;
    @FXML private Label valuationQuoteLabel;

    // Conto Bancario
    @FXML private Label ibanLabel;
    @FXML private Label balanceLabel;
    @FXML private TextField depositAmountField;

    // Profilo e Account
    @FXML private PasswordField emailCurrentPasswordField;
    @FXML private TextField newEmailField;
    @FXML private PasswordField pwdCurrentPasswordField;
    @FXML private PasswordField newPasswordField;
    @FXML private PasswordField deleteAccountPasswordField;

    @FXML private Label globalMessageLabel;

    private UiModels.VehicleItem selectedCatalogVehicle;

    @FXML
    private void initialize() {
        vehicleStateCombo.setItems(FXCollections.observableArrayList(VehicleState.SALE, VehicleState.RENT));
        vehicleStateCombo.setValue(VehicleState.SALE);

        testDriveHourCombo.setItems(FXCollections.observableArrayList(
                "09:00", "10:00", "11:00", "12:00", "13:00", "14:00", "15:00", "16:00"
        ));
        testDriveHourCombo.setValue("10:00");

        // Colonne catalogo
        catalogPlateColumn.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().plate() != null ? c.getValue().plate() : "Da immatricolare"));
        catalogVinColumn.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().vin()));
        catalogModelColumn.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().displayName()));
        catalogYearColumn.setCellValueFactory(c -> new SimpleStringProperty(String.valueOf(c.getValue().year())));
        catalogMileageColumn.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().kilometers() + " km"));
        catalogPriceColumn.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().price() + " €"));
        catalogStateColumn.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().state().name()));

        // Colonne richieste
        requestVehicleColumn.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().vehicleDesc()));
        requestTypeColumn.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().isPurchase() ? "Acquisto (Acconto 20%)" : "Proposta vendita auto"));
        requestDepositColumn.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().deposit() + " €"));
        requestExpDateColumn.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().expDate() != null ? c.getValue().expDate().format(DATE_FMT) : "In attesa sblocco"));

        // Colonne vendite
        saleVehicleColumn.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().vehicleDesc()));
        saleAmountColumn.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().amount() + " €"));
        saleDateColumn.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().date().format(DATE_FMT)));
        saleStatusColumn.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().isDefaultFailure() ? "Mancato saldo (Penale 20% trattenuta)"
                        : (c.getValue().isAcquisition() ? "Vendita a concessionaria" : "Acquisto Concluso")));

        // Colonne noleggi
        rentalVehicleColumn.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().vehicleDesc()));
        rentalPeriodColumn.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().startDate().format(DATE_FMT) + " - " + c.getValue().endDate().format(DATE_FMT)));
        rentalAmountColumn.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().amount() + " €"));

        // Colonne test drive
        testDriveVehicleColumn.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().vehicleDesc()));
        testDriveDateColumn.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().date().format(DATE_FMT)));
        testDriveTimeColumn.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().start().format(TIME_FMT)));

        catalogTable.getSelectionModel().selectedItemProperty().addListener((obs, oldV, newV) -> {
            selectedCatalogVehicle = newV;
            if (newV != null) {
                rentalSelectedVehicleLabel.setText("Veicolo selezionato: " + newV.displayName() + " (" + newV.price() + " €)");
                testDriveSelectedVehicleLabel.setText("Veicolo selezionato: " + newV.displayName());
                updateRentalQuotePreview();
                updateRentalCalendarRestrictions();
                updateTestDriveCalendarRestrictions();
            }
        });
    }

    @Override
    protected void onContextReady() {
        if (context.session().isLoggedIn()) {
            userLabel.setText(session().displayName() + " (" + session().email() + ")");
            refresh();
        }
    }

    @FXML
    public void refresh() {
        clearMessages();
        long clientId = session().userId();
        searchCatalog();
        requestsTable.setItems(FXCollections.observableArrayList(gateway().clientRequests(clientId)));
        salesTable.setItems(FXCollections.observableArrayList(gateway().clientPurchases(clientId)));
        rentalsTable.setItems(FXCollections.observableArrayList(gateway().clientRentals(clientId)));
        testDrivesTable.setItems(FXCollections.observableArrayList(gateway().clientTestDrives(clientId)));

        if (brandComboBox != null) {
            brandComboBox.setItems(FXCollections.observableArrayList(gateway().allBrands()));
        }

        try {
            UiModels.BankAccountItem acc = gateway().getBankAccount(clientId);
            ibanLabel.setText(acc.iban());
            balanceLabel.setText(acc.balance().toPlainString() + " €");
        } catch (Exception ex) {
            ibanLabel.setText("Non disponibile");
            balanceLabel.setText("0.00 €");
        }
    }

    @FXML
    public void searchCatalog() {
        clearMessages();
        VehicleState state = vehicleStateCombo.getValue();
        List<UiModels.VehicleItem> items = gateway().searchCatalog(catalogSearchField.getText(), state);
        catalogTable.setItems(FXCollections.observableArrayList(items));
    }

    @FXML
    public void selectForPurchase() {
        clearMessages();
        if (selectedCatalogVehicle == null) {
            showError(globalMessageLabel, "Seleziona un veicolo dal catalogo per procedere con l'acquisto.");
            return;
        }
        if (selectedCatalogVehicle.state() != VehicleState.SALE) {
            showError(globalMessageLabel, "Solo i veicoli con stato 'SALE' possono essere acquistati.");
            return;
        }

        // Calcolo acconto esatto: 20% del prezzo attuale
        BigDecimal deposit = selectedCatalogVehicle.price().multiply(new BigDecimal("0.20")).setScale(2, RoundingMode.HALF_UP);

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Confermi l'avvio della procedura di acquisto per " + selectedCatalogVehicle.displayName() + "?\n\n"
                        + "Prezzo totale del veicolo: " + selectedCatalogVehicle.price() + " €\n"
                        + "Acconto richiesto (20%): " + deposit + " €\n\n"
                        + "L'importo di " + deposit + " € verrà scalato dal tuo conto corrente bancario e il veicolo verrà riservato (stato 'BLOCKED') in attesa dell'autorizzazione di un venditore.",
                ButtonType.YES, ButtonType.NO);
        confirm.setTitle("Conferma Acquisto e Versamento Acconto (20%)");
        if (confirm.showAndWait().orElse(ButtonType.NO) == ButtonType.YES) {
            try {
                gateway().requestPurchase(session().userId(), selectedCatalogVehicle.id(), deposit);
                showSuccess(globalMessageLabel, "Acconto di " + deposit + " € versato con successo! Veicolo riservato (BLOCKED) e richiesta registrata.");
                refresh();
                workspaceTabs.getSelectionModel().select(1);
            } catch (Exception ex) {
                showError(globalMessageLabel, ex);
            }
        }
    }

    @FXML
    public void payFullBalance() {
        clearMessages();
        UiModels.RequestItem req = requestsTable.getSelectionModel().getSelectedItem();
        if (req == null) {
            showError(globalMessageLabel, "Seleziona una richiesta dalla tabella.");
            return;
        }
        if (!req.isPurchase()) {
            showError(globalMessageLabel, "Questa richiesta è una proposta di vendita da te effettuata, non un acquisto.");
            return;
        }
        if (req.expDate() == null) {
            showError(globalMessageLabel, "La richiesta non è ancora stata autorizzata da un venditore. Attendi che venga autorizzata.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Sei sicuro di voler saldare l'importo rimanente per " + req.vehicleDesc() + "?\nIl saldo verrà scalato dal tuo conto corrente bancario.",
                ButtonType.YES, ButtonType.NO);
        confirm.setTitle("Conferma Saldo Acquisto");
        if (confirm.showAndWait().orElse(ButtonType.NO) == ButtonType.YES) {
            try {
                gateway().completePurchase(session().userId(), req.id(), req.salesmanId() != null ? req.salesmanId() : 1L);
                showSuccess(globalMessageLabel, "Acquisto completato con successo! Il veicolo è tuo.");
                refresh();
            } catch (Exception ex) {
                showError(globalMessageLabel, ex);
            }
        }
    }

    @FXML
    public void cancelRequestWithPenalty() {
        clearMessages();
        UiModels.RequestItem req = requestsTable.getSelectionModel().getSelectedItem();
        if (req == null) {
            showError(globalMessageLabel, "Seleziona una richiesta dalla tabella.");
            return;
        }
        if (!req.isPurchase()) {
            showError(globalMessageLabel, "Solo le richieste di acquisto possono essere annullate qui.");
            return;
        }

        BigDecimal deposit = req.deposit();
        BigDecimal penalty = deposit.multiply(new BigDecimal("0.20")).setScale(2, RoundingMode.HALF_UP);
        BigDecimal refund = deposit.subtract(penalty);

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Attenzione: annullando la richiesta d'acquisto verrà trattenuto il 20% della caparra ("
                        + penalty + " €) come penale per la concessionaria,\ne ti verrà rimborsato il restante 80% ("
                        + refund + " €) sul tuo conto corrente.\n\nVuoi procedere?",
                ButtonType.YES, ButtonType.NO);
        confirm.setTitle("Conferma Annullamento con Penale");
        if (confirm.showAndWait().orElse(ButtonType.NO) == ButtonType.YES) {
            try {
                gateway().cancelPurchase(session().userId(), req.id());
                showSuccess(globalMessageLabel, "Richiesta annullata: 20% trattenuto e " + refund + " € riaccreditati sul conto.");
                refresh();
            } catch (Exception ex) {
                showError(globalMessageLabel, ex);
            }
        }
    }

    @FXML
    public void selectForRental() {
        workspaceTabs.getSelectionModel().select(2);
    }

    @FXML
    public void updateRentalQuotePreview() {
        if (selectedCatalogVehicle == null || selectedCatalogVehicle.state() != VehicleState.RENT) {
            if (rentalQuoteLabel != null) rentalQuoteLabel.setText("Seleziona un veicolo RENT");
            return;
        }
        LocalDate start = rentalStartDatePicker.getValue();
        LocalDate end = rentalEndDatePicker.getValue();
        if (start != null && end != null && !end.isBefore(start)) {
            long days = ChronoUnit.DAYS.between(start, end) + 1;
            try {
                BigDecimal quote = gateway().quoteRentalPrice(selectedCatalogVehicle.id(), days);
                if (rentalQuoteLabel != null) {
                    rentalQuoteLabel.setText("Preventivo totale per " + days + " gg: " + quote + " € (Sconto 5% ogni 5 gg applicato)");
                }
            } catch (Exception ignored) {
            }
        }
    }

    @FXML
    public void confirmRental() {
        clearMessages();
        if (selectedCatalogVehicle == null || selectedCatalogVehicle.state() != VehicleState.RENT) {
            showError(globalMessageLabel, "Seleziona dal catalogo un veicolo con modalità 'RENT'.");
            return;
        }
        LocalDate start = rentalStartDatePicker.getValue();
        LocalDate end = rentalEndDatePicker.getValue();
        if (start == null || end == null) {
            showError(globalMessageLabel, "Seleziona sia la data di inizio che di fine noleggio.");
            return;
        }
        if (end.isBefore(start)) {
            showError(globalMessageLabel, "La data di fine non può precedere la data di inizio.");
            return;
        }
        try {
            long days = ChronoUnit.DAYS.between(start, end) + 1;
            BigDecimal quote = gateway().quoteRentalPrice(selectedCatalogVehicle.id(), days);
            gateway().bookRental(session().userId(), selectedCatalogVehicle.id(), start, end);
            showSuccess(globalMessageLabel, "Noleggio confermato per " + days + " giorni! Importo di " + quote + " € addebitato sul tuo conto corrente.");
            refresh();
        } catch (Exception ex) {
            showError(globalMessageLabel, ex);
        }
    }

    @FXML
    public void selectForTestDrive() {
        workspaceTabs.getSelectionModel().select(2);
    }

    private void updateRentalCalendarRestrictions() {
        if (selectedCatalogVehicle == null || selectedCatalogVehicle.state() != VehicleState.RENT) {
            rentalStartDatePicker.setDayCellFactory(null);
            rentalEndDatePicker.setDayCellFactory(null);
            return;
        }
        long vid = selectedCatalogVehicle.id();
        List<UiModels.RentItem> rentals = gateway().allRentals().stream()
                .filter(r -> r.vehicleId() == vid)
                .toList();

        Callback<DatePicker, DateCell> dayCellFactory = picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty) return;
                if (item.isBefore(LocalDate.now())) {
                    setDisable(true);
                    return;
                }
                for (UiModels.RentItem r : rentals) {
                    if (!item.isBefore(r.startDate()) && !item.isAfter(r.endDate())) {
                        setDisable(true);
                        setStyle("-fx-background-color: #fee2e2; -fx-text-fill: #991b1b;");
                        setTooltip(new Tooltip("Veicolo già noleggiato per questo giorno"));
                        return;
                    }
                }
            }
        };
        rentalStartDatePicker.setDayCellFactory(dayCellFactory);
        rentalEndDatePicker.setDayCellFactory(dayCellFactory);
    }

    private void updateTestDriveCalendarRestrictions() {
        if (selectedCatalogVehicle == null) {
            testDriveDatePicker.setDayCellFactory(null);
            return;
        }
        long vid = selectedCatalogVehicle.id();
        List<UiModels.RentItem> rentals = gateway().allRentals().stream()
                .filter(r -> r.vehicleId() == vid)
                .toList();

        Callback<DatePicker, DateCell> dayCellFactory = picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty) return;
                if (item.isBefore(LocalDate.now())) {
                    setDisable(true);
                    return;
                }
                for (UiModels.RentItem r : rentals) {
                    if (!item.isBefore(r.startDate()) && !item.isAfter(r.endDate())) {
                        setDisable(true);
                        setStyle("-fx-background-color: #fee2e2; -fx-text-fill: #991b1b;");
                        setTooltip(new Tooltip("Veicolo a noleggio: nessuna fascia oraria disponibile"));
                        return;
                    }
                }
            }
        };
        testDriveDatePicker.setDayCellFactory(dayCellFactory);
        onTestDriveDateSelected();
    }

    @FXML
    public void onTestDriveDateSelected() {
        if (selectedCatalogVehicle == null) {
            testDriveHourCombo.getItems().clear();
            return;
        }
        LocalDate date = testDriveDatePicker.getValue();
        if (date == null || date.isBefore(LocalDate.now())) {
            testDriveHourCombo.getItems().clear();
            return;
        }

        long vid = selectedCatalogVehicle.id();
        // Se il veicolo è noleggiato per quel giorno, nessuna fascia oraria è disponibile
        boolean isRented = gateway().allRentals().stream()
                .anyMatch(r -> r.vehicleId() == vid && !date.isBefore(r.startDate()) && !date.isAfter(r.endDate()));
        if (isRented) {
            testDriveHourCombo.getItems().clear();
            showError(globalMessageLabel, "Il veicolo è noleggiato per il giorno selezionato: nessuna fascia oraria disponibile.");
            return;
        }

        // Se lo stesso veicolo ha già prenotazioni per lo stesso giorno, gli orari occupati non sono selezionabili
        Set<Integer> bookedHours = gateway().allTestDrives().stream()
                .filter(td -> td.vehicleId() == vid && td.date().equals(date))
                .map(td -> td.start().getHour())
                .collect(Collectors.toSet());

        List<String> available = new ArrayList<>();
        LocalTime now = LocalTime.now();
        boolean isToday = date.equals(LocalDate.now());

        for (int h = 9; h <= 16; h++) {
            if (bookedHours.contains(h)) {
                continue;
            }
            if (isToday && h <= now.getHour()) {
                continue;
            }
            available.add(String.format("%02d:00", h));
        }

        testDriveHourCombo.setItems(FXCollections.observableArrayList(available));
        if (!available.isEmpty()) {
            testDriveHourCombo.setValue(available.get(0));
        } else {
            testDriveHourCombo.setValue(null);
            showError(globalMessageLabel, "Tutti gli orari per la data selezionata sono già occupati.");
        }
    }

    @FXML
    public void confirmTestDrive() {
        clearMessages();
        if (selectedCatalogVehicle == null) {
            showError(globalMessageLabel, "Seleziona un veicolo dal catalogo per il test drive.");
            return;
        }
        LocalDate date = testDriveDatePicker.getValue();
        String hourStr = testDriveHourCombo.getValue();
        if (date == null || hourStr == null || hourStr.isBlank()) {
            showError(globalMessageLabel, "Seleziona una data valida e una fascia oraria disponibile per il test drive.");
            return;
        }
        try {
            LocalTime time = LocalTime.parse(hourStr, TIME_FMT);
            gateway().bookTestDrive(session().userId(), selectedCatalogVehicle.id(), date, time);
            showSuccess(globalMessageLabel, "Test drive prenotato con successo per il " + date.format(DATE_FMT) + " alle ore " + time + " (durata 1 ora).");
            refresh();
            updateTestDriveCalendarRestrictions();
        } catch (Exception ex) {
            showError(globalMessageLabel, ex);
        }
    }

    @FXML
    public void onBrandSelected() {
        clearMessages();
        UiModels.BrandItem brand = brandComboBox.getValue();
        if (brand == null) {
            modelComboBox.getItems().clear();
            return;
        }
        List<UiModels.ModelItem> models = gateway().allModels().stream()
                .filter(m -> m.brand().equalsIgnoreCase(brand.name()))
                .toList();
        modelComboBox.setItems(FXCollections.observableArrayList(models));
        modelComboBox.getSelectionModel().clearSelection();
        newPriceField.clear();
        if (valuationQuoteLabel != null) valuationQuoteLabel.setText("");
    }

    @FXML
    public void onModelSelected() {
        newPriceField.clear();
        if (valuationQuoteLabel != null) valuationQuoteLabel.setText("");
    }

    @FXML
    public void calculateValuationQuote() {
        clearMessages();
        UiModels.BrandItem brand = brandComboBox.getValue();
        UiModels.ModelItem model = modelComboBox.getValue();
        if (brand == null || model == null) {
            showError(globalMessageLabel, "Seleziona Marchio e Modello dal catalogo prima di calcolare la valutazione.");
            return;
        }

        String vin = newVinField.getText();
        if (vin == null || vin.isBlank()) {
            showError(globalMessageLabel, "Inserisci il numero di telaio (VIN).");
            return;
        }
        if (vin.trim().length() > 17) {
            showError(globalMessageLabel, "Il numero di telaio (VIN) non può superare 17 caratteri.");
            return;
        }

        String plate = newPlateField.getText() != null ? newPlateField.getText().trim() : "";
        if (plate.isBlank()) {
            plate = null;
        }

        String kmStr = newKmField.getText();
        if (kmStr == null || kmStr.isBlank()) {
            showError(globalMessageLabel, "Inserisci i chilometri percorsi.");
            return;
        }
        int km;
        try {
            km = Integer.parseInt(kmStr.trim());
            if (km < 0) {
                showError(globalMessageLabel, "I chilometri non possono essere negativi.");
                return;
            }
        } catch (NumberFormatException e) {
            showError(globalMessageLabel, "I chilometri devono essere un numero intero valido.");
            return;
        }

        if (plate == null && km != 0) {
            showError(globalMessageLabel, "Se il veicolo non ha targa, obbligatoriamente i chilometri devono essere a 0.");
            return;
        }

        String yearStr = newVehicleYearField.getText();
        if (yearStr == null || yearStr.isBlank()) {
            showError(globalMessageLabel, "Inserisci l'anno di immatricolazione del veicolo.");
            return;
        }
        int vehicleYear;
        int currentYear = LocalDate.now().getYear();
        try {
            vehicleYear = Integer.parseInt(yearStr.trim());
            if (vehicleYear < 1900 || vehicleYear > currentYear) {
                showError(globalMessageLabel, "L'anno del veicolo deve essere compreso tra 1900 e " + currentYear + ".");
                return;
            }
        } catch (NumberFormatException e) {
            showError(globalMessageLabel, "Inserisci un anno valido.");
            return;
        }

        try {
            BigDecimal estimated = gateway().quoteValuation(brand.name(), model.name(), plate, km, vehicleYear);
            newPriceField.setText(estimated.toPlainString());
            if (valuationQuoteLabel != null) {
                valuationQuoteLabel.setText("Valutazione calcolata dal sistema: " + estimated + " € (-20% usato se targata, -5%/anno, -5%/10.000 km)");
            }
            showSuccess(globalMessageLabel, "Valutazione automatica completata con successo: " + estimated + " €");
        } catch (Exception ex) {
            showError(globalMessageLabel, ex);
        }
    }

    @FXML
    public void submitVehicleForSale() {
        clearMessages();
        calculateValuationQuote();
        String priceText = newPriceField.getText();
        if (priceText == null || priceText.isBlank()) {
            return;
        }

        BigDecimal price = new BigDecimal(priceText);
        UiModels.BrandItem brand = brandComboBox.getValue();
        UiModels.ModelItem model = modelComboBox.getValue();
        String vin = newVinField.getText().trim();
        String plate = newPlateField.getText() != null ? newPlateField.getText().trim() : null;
        if (plate != null && plate.isBlank()) plate = null;
        int km = Integer.parseInt(newKmField.getText().trim());
        int vehicleYear = Integer.parseInt(newVehicleYearField.getText().trim());

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Confermi l'invio della proposta di vendita alla concessionaria?\n\n"
                        + "Veicolo: " + brand.name() + " " + model.name() + "\n"
                        + "VIN: " + vin + "\n"
                        + "Targa: " + (plate != null ? plate : "Da immatricolare") + "\n"
                        + "Chilometri: " + km + " km\n"
                        + "Anno immatricolazione: " + vehicleYear + "\n"
                        + "Valutazione stimata (accredito previsto se approvato): " + price + " €\n\n"
                        + "Il veicolo sarà registrato con stato 'WAITING' in attesa dell'approvazione di un venditore.",
                ButtonType.YES, ButtonType.NO);
        confirm.setTitle("Conferma Proposta di Vendita");
        if (confirm.showAndWait().orElse(ButtonType.NO) == ButtonType.YES) {
            try {
                UiModels.VehicleSaleSubmission sub = new UiModels.VehicleSaleSubmission(
                        brand.name(), model.name(), model.releaseYear(), vin, plate, km, price, vehicleYear
                );
                gateway().submitVehicleForSale(session().userId(), sub);
                showSuccess(globalMessageLabel, "Proposta inviata con successo! Il veicolo è in attesa di approvazione ('WAITING').");
                newVinField.clear();
                newPlateField.clear();
                newKmField.clear();
                newVehicleYearField.clear();
                newPriceField.clear();
                if (valuationQuoteLabel != null) valuationQuoteLabel.setText("");
                refresh();
                workspaceTabs.getSelectionModel().select(1);
            } catch (Exception ex) {
                showError(globalMessageLabel, ex);
            }
        }
    }

    @FXML
    public void depositFunds() {
        clearMessages();
        String amountStr = depositAmountField.getText();
        if (amountStr == null || amountStr.isBlank()) {
            showError(globalMessageLabel, "Inserisci l'importo da versare.");
            return;
        }
        try {
            BigDecimal amount = new BigDecimal(amountStr.trim());
            gateway().depositFunds(session().userId(), amount);
            showSuccess(globalMessageLabel, "Versamento di " + amount + " € completato con successo!");
            depositAmountField.clear();
            refresh();
        } catch (Exception ex) {
            showError(globalMessageLabel, ex);
        }
    }

    @FXML
    public void updateEmail() {
        clearMessages();
        String currentPwd = emailCurrentPasswordField.getText();
        String newEmail = newEmailField.getText();
        if (currentPwd == null || currentPwd.isBlank() || newEmail == null || newEmail.isBlank()) {
            showError(globalMessageLabel, "Inserisci la password attuale e il nuovo indirizzo email.");
            return;
        }
        try {
            gateway().updateEmail(session().userId(), Role.CLIENT, currentPwd.toCharArray(), newEmail);
            showSuccess(globalMessageLabel, "Email aggiornata con successo in: " + newEmail);
            emailCurrentPasswordField.clear();
            newEmailField.clear();
            userLabel.setText(session().displayName() + " (" + newEmail + ")");
        } catch (Exception ex) {
            showError(globalMessageLabel, ex);
        }
    }

    @FXML
    public void updatePassword() {
        clearMessages();
        String currentPwd = pwdCurrentPasswordField.getText();
        String newPwd = newPasswordField.getText();
        if (currentPwd == null || currentPwd.isBlank() || newPwd == null || newPwd.isBlank()) {
            showError(globalMessageLabel, "Inserisci la password attuale e la nuova password.");
            return;
        }
        try {
            gateway().updatePassword(session().userId(), Role.CLIENT, currentPwd.toCharArray(), newPwd.toCharArray());
            showSuccess(globalMessageLabel, "Password aggiornata con successo!");
            pwdCurrentPasswordField.clear();
            newPasswordField.clear();
        } catch (Exception ex) {
            showError(globalMessageLabel, ex);
        }
    }

    @FXML
    public void deleteAccount() {
        clearMessages();
        String currentPwd = deleteAccountPasswordField.getText();
        if (currentPwd == null || currentPwd.isBlank()) {
            showError(globalMessageLabel, "Inserisci la tua password attuale per confermare l'eliminazione dell'account.");
            return;
        }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Sei sicuro di voler eliminare definitivamente il tuo account?\nL'operazione è consentita solo se non hai storico di operazioni (Test Drive, Noleggi o Acquisti).",
                ButtonType.YES, ButtonType.NO);
        confirm.setTitle("Elimina Account");
        if (confirm.showAndWait().orElse(ButtonType.NO) == ButtonType.YES) {
            try {
                gateway().deleteAccount(session().userId(), Role.CLIENT, currentPwd.toCharArray());
                Alert info = new Alert(Alert.AlertType.INFORMATION, "Account eliminato con successo.");
                info.showAndWait();
                logout();
            } catch (Exception ex) {
                showError(globalMessageLabel, ex);
            }
        }
    }

    private void clearMessages() {
        globalMessageLabel.setText("");
        globalMessageLabel.getStyleClass().removeAll("danger-text", "success-text");
    }
}
