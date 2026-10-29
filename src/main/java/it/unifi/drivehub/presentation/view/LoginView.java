package it.unifi.drivehub.presentation.view;

import it.unifi.drivehub.AppContext;
import it.unifi.drivehub.domain.user.Client;
import it.unifi.drivehub.domain.user.User;
import it.unifi.drivehub.service.AuthService;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;

import java.util.function.Consumer;

/**
 * Schermata iniziale di Autenticazione e Registrazione.
 * Adotta la Dependency Injection ricevendo AuthService.
 */
public final class LoginView extends BorderPane {

    private final AuthService authService;
    private final Consumer<User> onLoginSuccess;

    public LoginView(Consumer<User> onLoginSuccess) {
        this(AppContext.getInstance().getAuthService(), onLoginSuccess);
    }

    public LoginView(AuthService authService, Consumer<User> onLoginSuccess) {
        this.authService = authService;
        this.onLoginSuccess = onLoginSuccess;
        buildUi();
    }

    private void buildUi() {
        // Intestazione
        VBox headerBox = new VBox(5);
        headerBox.setAlignment(Pos.CENTER);
        headerBox.setPadding(new Insets(30, 20, 20, 20));
        Label title = new Label("DriveHub");
        title.setStyle("-fx-font-size: 32px; -fx-font-weight: bold; -fx-text-fill: #1e293b;");
        Label subtitle = new Label("Piattaforma di Gestione Compravendita e Noleggio Veicoli");
        subtitle.setStyle("-fx-font-size: 14px; -fx-text-fill: #64748b;");
        headerBox.getChildren().addAll(title, subtitle);

        // Contenitore centrale con Form a Schede
        TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        tabPane.setMaxWidth(420);

        // Tab Login
        Tab loginTab = new Tab("Accedi");
        VBox loginBox = new VBox(15);
        loginBox.setPadding(new Insets(25));
        loginBox.setAlignment(Pos.CENTER_LEFT);

        TextField emailField = new TextField();
        emailField.setPromptText("Email (es. mario.rossi@email.it)");
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password");

        Button btnLogin = new Button("Accedi al Sistema");
        btnLogin.setMaxWidth(Double.MAX_VALUE);
        btnLogin.setOnAction(e -> handleLogin(emailField.getText(), passwordField.getText()));

        loginBox.getChildren().addAll(
                new Label("Email:"), emailField,
                new Label("Password:"), passwordField,
                btnLogin
        );
        loginTab.setContent(loginBox);

        // Tab Registrazione Cliente
        Tab registerTab = new Tab("Registrati");
        VBox regBox = new VBox(12);
        regBox.setPadding(new Insets(25));
        regBox.setAlignment(Pos.CENTER_LEFT);

        TextField nameField = new TextField();
        nameField.setPromptText("Nome");
        TextField surnameField = new TextField();
        surnameField.setPromptText("Cognome");
        TextField regEmailField = new TextField();
        regEmailField.setPromptText("Email");
        PasswordField regPwdField = new PasswordField();
        regPwdField.setPromptText("Password");

        Button btnRegister = new Button("Crea Account Cliente");
        btnRegister.setMaxWidth(Double.MAX_VALUE);
        btnRegister.getStyleClass().add("button-success");
        btnRegister.setOnAction(e -> handleRegister(
                nameField.getText(), surnameField.getText(),
                regEmailField.getText(), regPwdField.getText()
        ));

        regBox.getChildren().addAll(
                new Label("Nome:"), nameField,
                new Label("Cognome:"), surnameField,
                new Label("Email:"), regEmailField,
                new Label("Password:"), regPwdField,
                btnRegister
        );
        registerTab.setContent(regBox);

        tabPane.getTabs().addAll(loginTab, registerTab);

        VBox centerBox = new VBox(tabPane);
        centerBox.setAlignment(Pos.TOP_CENTER);
        centerBox.setPadding(new Insets(10, 20, 40, 20));

        setTop(headerBox);
        setCenter(centerBox);
    }

    private void handleLogin(String email, String password) {
        User user;
        try {
            user = authService.login(email, password);
        } catch (Throwable ex) {
            UiUtils.showError("Errore di Accesso", ex.getMessage());
            return;
        }

        try {
            onLoginSuccess.accept(user);
        } catch (Throwable ex) {
            System.err.println("ERRORE CARICAMENTO DASHBOARD: " + ex.getMessage());
            ex.printStackTrace();
            UiUtils.showError("Errore Caricamento Dashboard", "Impossibile caricare l'interfaccia: " + ex.getMessage());
        }
    }

    private void handleRegister(String name, String surname, String email, String password) {
        Client client;
        try {
            client = authService.registerClient(name, surname, email, password);
            UiUtils.showInfo("Registrazione Completata",
                    "Benvenuto " + client.getName() + "!\nÈ stato creato automaticamente un conto corrente con IBAN:\n"
                            + client.getBankAccount().getIban() + "\nSaldo iniziale: € 0,00");
        } catch (Throwable ex) {
            UiUtils.showError("Errore di Registrazione", ex.getMessage());
            return;
        }

        try {
            onLoginSuccess.accept(client);
        } catch (Throwable ex) {
            System.err.println("ERRORE CARICAMENTO DASHBOARD REGISTRAZIONE: " + ex.getMessage());
            ex.printStackTrace();
            UiUtils.showError("Errore Caricamento Dashboard", "Impossibile caricare l'interfaccia: " + ex.getMessage());
        }
    }
}
