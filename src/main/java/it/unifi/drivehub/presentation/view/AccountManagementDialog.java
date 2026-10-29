package it.unifi.drivehub.presentation.view;

import it.unifi.drivehub.AppContext;
import it.unifi.drivehub.domain.user.Client;
import it.unifi.drivehub.domain.user.User;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.Separator;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.math.BigDecimal;
import java.util.function.Consumer;

/**
 * Schermata di gestione account (Pagina 4 degli appunti).
 * Permette la modifica di email, password, ricarica saldo (per Clienti)
 * e la cancellazione controllata dell'account previa conferma della password attuale.
 */
public final class AccountManagementDialog {

    public static void show(Stage owner, User user, Runnable onAccountDeleted, Consumer<User> onUserUpdated) {
        Stage dialog = new Stage();
        dialog.initOwner(owner);
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Gestione Account - " + user.getName() + " " + user.getSurname());

        TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        // Tab 1: Modifica Email
        Tab emailTab = new Tab("Modifica Email");
        VBox emailBox = new VBox(15);
        emailBox.setPadding(new Insets(20));
        TextField newEmailField = new TextField();
        newEmailField.setPromptText("Nuova email");
        PasswordField pwdForEmailField = new PasswordField();
        pwdForEmailField.setPromptText("Password attuale per conferma");
        Button btnSaveEmail = new Button("Aggiorna Email");
        btnSaveEmail.setOnAction(e -> {
            String newEmail = newEmailField.getText();
            String pwd = pwdForEmailField.getText();
            try {
                AppContext.getInstance().getAuthService().changeEmail(user.getId(), user.getRoleDisplayName(), newEmail, pwd);
                UiUtils.showInfo("Operazione Riuscita", "Email aggiornata con successo in: " + newEmail);
                if (user instanceof Client c) c.updateEmail(newEmail);
                if (onUserUpdated != null) onUserUpdated.accept(user);
                dialog.close();
            } catch (Exception ex) {
                UiUtils.showError("Errore", ex.getMessage());
            }
        });
        emailBox.getChildren().addAll(
                new Label("Email attuale: " + user.getEmail()),
                new Label("Nuova Email:"), newEmailField,
                new Label("Password Attuale:"), pwdForEmailField,
                btnSaveEmail
        );
        emailTab.setContent(emailBox);

        // Tab 2: Modifica Password
        Tab pwdTab = new Tab("Modifica Password");
        VBox pwdBox = new VBox(15);
        pwdBox.setPadding(new Insets(20));
        PasswordField currentPwdField = new PasswordField();
        currentPwdField.setPromptText("Password attuale");
        PasswordField newPwdField = new PasswordField();
        newPwdField.setPromptText("Nuova password");
        Button btnSavePwd = new Button("Aggiorna Password");
        btnSavePwd.setOnAction(e -> {
            String curPwd = currentPwdField.getText();
            String newPwd = newPwdField.getText();
            try {
                AppContext.getInstance().getAuthService().changePassword(user.getId(), user.getRoleDisplayName(), newPwd, curPwd);
                UiUtils.showInfo("Operazione Riuscita", "Password aggiornata con successo.");
                dialog.close();
            } catch (Exception ex) {
                UiUtils.showError("Errore", ex.getMessage());
            }
        });
        pwdBox.getChildren().addAll(
                new Label("Password Attuale:"), currentPwdField,
                new Label("Nuova Password:"), newPwdField,
                btnSavePwd
        );
        pwdTab.setContent(pwdBox);

        // Tab 3: Ricarica Saldo (solo se Cliente)
        if (user instanceof Client client) {
            Tab topUpTab = new Tab("Ricarica Saldo");
            VBox topUpBox = new VBox(15);
            topUpBox.setPadding(new Insets(20));
            TextField amountField = new TextField();
            amountField.setPromptText("Es. 500.00");
            Button btnTopUp = new Button("Ricarica Conto");
            btnTopUp.getStyleClass().add("button-success");
            btnTopUp.setOnAction(e -> {
                try {
                    BigDecimal amount = new BigDecimal(amountField.getText().trim().replace(",", "."));
                    AppContext.getInstance().getAuthService().topUpClientBalance(client.getId(), amount);
                    client.setBankAccount(AppContext.getInstance().getAuthService().getClientBankAccount(client.getId()));
                    UiUtils.showInfo("Ricarica Effettuata", "Accreditati con successo € " + amount + " sul conto.");
                    if (onUserUpdated != null) onUserUpdated.accept(client);
                    dialog.close();
                } catch (Exception ex) {
                    UiUtils.showError("Errore Ricarica", ex.getMessage());
                }
            });
            topUpBox.getChildren().addAll(
                    new Label("IBAN: " + (client.getBankAccount() != null ? client.getBankAccount().getIban() : "N/D")),
                    new Label("Saldo attuale: " + (client.getBankAccount() != null ? UiUtils.formatCurrency(client.getBankAccount().getBalance()) : "€ 0,00")),
                    new Label("Importo da ricaricare (> 0):"), amountField,
                    btnTopUp
            );
            topUpTab.setContent(topUpBox);
            tabPane.getTabs().add(topUpTab);
        }

        // Tab 4: Elimina Account
        Tab deleteTab = new Tab("Elimina Account");
        VBox deleteBox = new VBox(15);
        deleteBox.setPadding(new Insets(20));
        PasswordField pwdForDeleteField = new PasswordField();
        pwdForDeleteField.setPromptText("Password attuale per confermare");
        Button btnDelete = new Button("Elimina Definitivamente Account");
        btnDelete.getStyleClass().add("button-danger");
        btnDelete.setOnAction(e -> {
            boolean confirm = UiUtils.showConfirmation("Conferma Eliminazione",
                    "Sei sicuro di voler eliminare il tuo account?",
                    "L'operazione è consentita solo se non risultano operazioni storiche associate nel sistema.");
            if (!confirm) return;

            try {
                AppContext.getInstance().getAuthService().deleteAccount(user.getId(), user.getRoleDisplayName(), pwdForDeleteField.getText());
                UiUtils.showInfo("Account Eliminato", "L'account è stato cancellato con successo.");
                dialog.close();
                if (onAccountDeleted != null) onAccountDeleted.run();
            } catch (Exception ex) {
                UiUtils.showError("Eliminazione Impossibile", ex.getMessage());
            }
        });
        deleteBox.getChildren().addAll(
                new Label("Attenzione: l'eliminazione dell'account è consentita solo se non sono state effettuate operazioni (Test Drive, Noleggi, Vendite)."),
                new Label("Inserisci la password per confermare:"), pwdForDeleteField,
                btnDelete
        );
        deleteTab.setContent(deleteBox);

        tabPane.getTabs().addAll(emailTab, pwdTab, deleteTab);

        Scene scene = new Scene(tabPane, 480, 360);
        scene.getStylesheets().add(AccountManagementDialog.class.getResource("/css/style.css").toExternalForm());
        dialog.setScene(scene);
        dialog.showAndWait();
    }
}

