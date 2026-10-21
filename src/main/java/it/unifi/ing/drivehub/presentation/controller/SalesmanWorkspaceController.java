package it.unifi.ing.drivehub.presentation.controller;

import it.unifi.ing.drivehub.domain.users.Role;
import it.unifi.ing.drivehub.domain.vehicles.VehicleState;
import it.unifi.ing.drivehub.presentation.core.UiModels;
import it.unifi.ing.drivehub.presentation.navigation.NavigationManager;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.time.format.DateTimeFormatter;
import java.util.List;

public final class SalesmanWorkspaceController extends AbstractController {
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm");

    @FXML private Label userLabel;
    @FXML private Label messageLabel;

    // Tabella veicoli in attesa (WAITING)
    @FXML private TableView<UiModels.VehicleItem> waitingVehiclesTable;
    @FXML private TableColumn<UiModels.VehicleItem, String> waitingPlateColumn;
    @FXML private TableColumn<UiModels.VehicleItem, String> waitingVinColumn;
    @FXML private TableColumn<UiModels.VehicleItem, String> waitingModelColumn;
    @FXML private TableColumn<UiModels.VehicleItem, String> waitingYearColumn;
    @FXML private TableColumn<UiModels.VehicleItem, String> waitingKmColumn;
    @FXML private TableColumn<UiModels.VehicleItem, String> waitingPriceColumn;

    // Tabella richieste
    @FXML private TableView<UiModels.RequestItem> requestsTable;
    @FXML private TableColumn<UiModels.RequestItem, String> reqIdColumn;
    @FXML private TableColumn<UiModels.RequestItem, String> reqVehicleColumn;
    @FXML private TableColumn<UiModels.RequestItem, String> reqTypeColumn;
    @FXML private TableColumn<UiModels.RequestItem, String> reqClientColumn;
    @FXML private TableColumn<UiModels.RequestItem, String> reqDepositColumn;
    @FXML private TableColumn<UiModels.RequestItem, String> reqExpDateColumn;

    // Tabella cambio stato veicoli (SALE <-> RENT)
    @FXML private TableView<UiModels.VehicleItem> availableVehiclesTable;
    @FXML private TableColumn<UiModels.VehicleItem, String> availPlateColumn;
    @FXML private TableColumn<UiModels.VehicleItem, String> availVinColumn;
    @FXML private TableColumn<UiModels.VehicleItem, String> availModelColumn;
    @FXML private TableColumn<UiModels.VehicleItem, String> availPriceColumn;
    @FXML private TableColumn<UiModels.VehicleItem, String> availStateColumn;

    // Tabella noleggi
    @FXML private TableView<UiModels.RentItem> rentalsTable;
    @FXML private TableColumn<UiModels.RentItem, String> rentalVehicleColumn;
    @FXML private TableColumn<UiModels.RentItem, String> rentalPeriodColumn;
    @FXML private TableColumn<UiModels.RentItem, String> rentalAmountColumn;

    // Tabella test drive
    @FXML private TableView<UiModels.TestDriveItem> testDrivesTable;
    @FXML private TableColumn<UiModels.TestDriveItem, String> tdVehicleColumn;
    @FXML private TableColumn<UiModels.TestDriveItem, String> tdClientColumn;
    @FXML private TableColumn<UiModels.TestDriveItem, String> tdDateColumn;
    @FXML private TableColumn<UiModels.TestDriveItem, String> tdTimeColumn;

    // Profilo e Account
    @FXML private PasswordField emailCurrentPasswordField;
    @FXML private TextField newEmailField;
    @FXML private PasswordField pwdCurrentPasswordField;
    @FXML private PasswordField newPasswordField;
    @FXML private PasswordField deleteAccountPasswordField;

    @FXML
    private void initialize() {
        // Setup veicoli WAITING
        waitingPlateColumn.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().plate() != null ? c.getValue().plate() : "Da immatricolare"));
        waitingVinColumn.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().vin()));
        waitingModelColumn.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().displayName()));
        waitingYearColumn.setCellValueFactory(c -> new SimpleStringProperty(String.valueOf(c.getValue().year())));
        waitingKmColumn.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().kilometers() + " km"));
        waitingPriceColumn.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().price() + " €"));

        // Setup richieste
        reqIdColumn.setCellValueFactory(c -> new SimpleStringProperty(String.valueOf(c.getValue().id())));
        reqVehicleColumn.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().vehicleDesc()));
        reqTypeColumn.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().isPurchase() ? "Acquisto Cliente (Acconto)" : "Proposta Vendita Cliente"));
        reqClientColumn.setCellValueFactory(c -> new SimpleStringProperty("Cliente #" + c.getValue().clientId()));
        reqDepositColumn.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().deposit() + " €"));
        reqExpDateColumn.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().expDate() != null ? c.getValue().expDate().format(DATE_FMT) : "In attesa autorizzazione"));

        // Setup veicoli disponibili per switch stato
        availPlateColumn.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().plate() != null ? c.getValue().plate() : "Da immatricolare"));
        availVinColumn.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().vin()));
        availModelColumn.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().displayName()));
        availPriceColumn.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().price() + " €"));
        availStateColumn.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().state().name()));

        // Setup noleggi
        rentalVehicleColumn.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().vehicleDesc()));
        rentalPeriodColumn.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().startDate().format(DATE_FMT) + " - " + c.getValue().endDate().format(DATE_FMT)));
        rentalAmountColumn.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().amount() + " €"));

        // Setup test drive
        tdVehicleColumn.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().vehicleDesc()));
        tdClientColumn.setCellValueFactory(c -> new SimpleStringProperty("Cliente #" + c.getValue().clientId()));
        tdDateColumn.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().date().format(DATE_FMT)));
        tdTimeColumn.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().start().format(TIME_FMT)));
    }

    @Override
    protected void onContextReady() {
        if (context.session().isLoggedIn()) {
            userLabel.setText(session().displayName() + " (Venditore - " + session().email() + ")");
            refresh();
        }
    }

    @FXML
    public void refresh() {
        clearMessages();
        gateway().processExpiredRequests();
        waitingVehiclesTable.setItems(FXCollections.observableArrayList(gateway().waitingVehicles()));
        requestsTable.setItems(FXCollections.observableArrayList(gateway().allPendingRequests()));

        List<UiModels.VehicleItem> active = gateway().allVehicles().stream()
                .filter(v -> v.state() == VehicleState.SALE || v.state() == VehicleState.RENT)
                .toList();
        availableVehiclesTable.setItems(FXCollections.observableArrayList(active));

        rentalsTable.setItems(FXCollections.observableArrayList(gateway().allRentals()));
        testDrivesTable.setItems(FXCollections.observableArrayList(gateway().allTestDrives()));
    }

    @FXML
    public void approveWaitingVehicle() {
        clearMessages();
        UiModels.VehicleItem v = waitingVehiclesTable.getSelectionModel().getSelectedItem();
        if (v == null) {
            showError(messageLabel, "Seleziona un veicolo in attesa da approvare.");
            return;
        }

        // Cerca se esiste una richiesta associata
        List<UiModels.RequestItem> reqs = gateway().allPendingRequests();
        UiModels.RequestItem req = reqs.stream()
                .filter(r -> r.vehicleId() == v.id() && r.isSaleProposal())
                .findFirst()
                .orElse(null);

        try {
            if (req != null) {
                gateway().approveClientSale(req.id(), session().userId());
                showSuccess(messageLabel, "Proposta approvata! Liquidato il cliente per " + req.deposit().abs()
                        + " €, registrata spesa aziendale e auto messa in vendita (stato: SALE).");
            } else {
                gateway().approveVehicle(v.id());
                showSuccess(messageLabel, "Veicolo approvato e messo in vendita (stato: SALE).");
            }
            refresh();
        } catch (Exception ex) {
            showError(messageLabel, ex);
        }
    }

    @FXML
    public void rejectWaitingVehicle() {
        clearMessages();
        UiModels.VehicleItem v = waitingVehiclesTable.getSelectionModel().getSelectedItem();
        if (v == null) {
            showError(messageLabel, "Seleziona un veicolo in attesa da rifiutare.");
            return;
        }

        List<UiModels.RequestItem> reqs = gateway().allPendingRequests();
        UiModels.RequestItem req = reqs.stream()
                .filter(r -> r.vehicleId() == v.id() && r.isSaleProposal())
                .findFirst()
                .orElse(null);

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Sei sicuro di voler rifiutare la proposta per " + v.displayName() + "?",
                ButtonType.YES, ButtonType.NO);
        if (confirm.showAndWait().orElse(ButtonType.NO) == ButtonType.YES) {
            try {
                if (req != null) {
                    gateway().rejectClientSale(req.id(), session().userId());
                } else {
                    gateway().rejectVehicle(v.id());
                }
                showSuccess(messageLabel, "Proposta rifiutata.");
                refresh();
            } catch (Exception ex) {
                showError(messageLabel, ex);
            }
        }
    }

    @FXML
    public void authorizePurchaseRequest() {
        clearMessages();
        UiModels.RequestItem req = requestsTable.getSelectionModel().getSelectedItem();
        if (req == null) {
            showError(messageLabel, "Seleziona una richiesta dalla tabella.");
            return;
        }
        if (!req.isPurchase()) {
            showError(messageLabel, "Questa è una proposta di vendita da cliente. Usa 'Approva Proposta' per acquisirla.");
            return;
        }
        try {
            gateway().authorizePurchase(req.id(), session().userId());
            showSuccess(messageLabel, "Richiesta d'acquisto #" + req.id() + " autorizzata! Scadenza saldo impostata a 1 mese.");
            refresh();
        } catch (Exception ex) {
            showError(messageLabel, ex);
        }
    }

    @FXML
    public void switchToRent() {
        clearMessages();
        UiModels.VehicleItem v = availableVehiclesTable.getSelectionModel().getSelectedItem();
        if (v == null) {
            showError(messageLabel, "Seleziona un veicolo dalla tabella.");
            return;
        }
        if (v.state() == VehicleState.RENT) {
            showError(messageLabel, "Il veicolo è già in modalità NOLEGGIO (RENT).");
            return;
        }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Sei sicuro di voler cambiare lo stato del veicolo " + v.displayName() + " in NOLEGGIO (RENT)?",
                ButtonType.YES, ButtonType.NO);
        confirm.setTitle("Conferma Cambio Stato a Noleggio");
        if (confirm.showAndWait().orElse(ButtonType.NO) == ButtonType.YES) {
            try {
                gateway().switchVehicleState(v.id(), VehicleState.RENT);
                showSuccess(messageLabel, "Stato veicolo " + v.displayName() + " impostato su NOLEGGIO (RENT).");
                refresh();
            } catch (Exception ex) {
                showError(messageLabel, ex);
            }
        }
    }

    @FXML
    public void switchToSale() {
        clearMessages();
        UiModels.VehicleItem v = availableVehiclesTable.getSelectionModel().getSelectedItem();
        if (v == null) {
            showError(messageLabel, "Seleziona un veicolo dalla tabella.");
            return;
        }
        if (v.state() == VehicleState.SALE) {
            showError(messageLabel, "Il veicolo è già in modalità VENDITA (SALE).");
            return;
        }

        List<UiModels.RentItem> futureRentals = gateway().allRentals().stream()
                .filter(r -> r.vehicleId() == v.id() && !r.endDate().isBefore(java.time.LocalDate.now()))
                .toList();

        StringBuilder msg = new StringBuilder("Sei sicuro di voler impostare lo stato del veicolo " + v.displayName() + " su VENDITA (SALE)?");
        if (!futureRentals.isEmpty()) {
            msg.append("\n\nATTENZIONE: Sono presenti ").append(futureRentals.size())
                    .append(" prenotazione/i di noleggio future per questo mezzo.")
                    .append("\nConfermando, TUTTE le prenotazioni future verranno annullate e i rispettivi clienti verranno rimborsati al 100% dell'addebito sul proprio conto.");
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, msg.toString(), ButtonType.YES, ButtonType.NO);
        confirm.setTitle("Conferma Cambio Stato a Vendita");
        if (confirm.showAndWait().orElse(ButtonType.NO) == ButtonType.YES) {
            try {
                gateway().switchVehicleState(v.id(), VehicleState.SALE);
                showSuccess(messageLabel, "Stato veicolo " + v.displayName() + " impostato su VENDITA (SALE). Eventuali noleggi futuri sono stati annullati e rimborsati al 100%.");
                refresh();
            } catch (Exception ex) {
                showError(messageLabel, ex);
            }
        }
    }

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
            gateway().updateEmail(session().userId(), Role.SALESMAN, currentPwd.toCharArray(), newEmail);
            showSuccess(messageLabel, "Email aggiornata con successo in: " + newEmail);
            emailCurrentPasswordField.clear();
            newEmailField.clear();
            userLabel.setText(session().displayName() + " (Venditore - " + newEmail + ")");
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
            gateway().updatePassword(session().userId(), Role.SALESMAN, currentPwd.toCharArray(), newPwd.toCharArray());
            showSuccess(messageLabel, "Password aggiornata con successo!");
            pwdCurrentPasswordField.clear();
            newPasswordField.clear();
        } catch (Exception ex) {
            showError(messageLabel, ex);
        }
    }

    @FXML
    public void deleteAccount() {
        clearMessages();
        String currentPwd = deleteAccountPasswordField.getText();
        if (currentPwd == null || currentPwd.isBlank()) {
            showError(messageLabel, "Inserisci la tua password attuale per confermare l'eliminazione dell'account.");
            return;
        }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Sei sicuro di voler eliminare definitivamente il tuo account?\nL'operazione è permessa solo se non hai vendite o operazioni associate nel registro.",
                ButtonType.YES, ButtonType.NO);
        confirm.setTitle("Elimina Account Venditore");
        if (confirm.showAndWait().orElse(ButtonType.NO) == ButtonType.YES) {
            try {
                gateway().deleteAccount(session().userId(), Role.SALESMAN, currentPwd.toCharArray());
                Alert info = new Alert(Alert.AlertType.INFORMATION, "Account eliminato con successo.");
                info.showAndWait();
                logout();
            } catch (Exception ex) {
                showError(messageLabel, ex);
            }
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
