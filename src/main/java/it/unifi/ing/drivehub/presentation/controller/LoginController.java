package it.unifi.ing.drivehub.presentation.controller;

import it.unifi.ing.drivehub.presentation.core.UiModels;
import it.unifi.ing.drivehub.presentation.navigation.NavigationManager;
import it.unifi.ing.drivehub.presentation.navigation.Route;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

import java.util.Arrays;

public final class LoginController extends AbstractController {
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private Label errorLabel;

    @FXML
    private void login() {
        char[] password = passwordField.getText().toCharArray();
        try {
            UiModels.Session authenticated = gateway().login(emailField.getText().trim(), password);
            context.session().open(authenticated);
            NavigationManager.get().showHomeForCurrentRole();
        } catch (RuntimeException exception) {
            showError(errorLabel, exception);
            passwordField.clear();
        } finally {
            Arrays.fill(password, '\0');
        }
    }

    @FXML
    private void back() {
        NavigationManager.get().show(Route.WELCOME);
    }

    @FXML
    private void openRegistration() {
        NavigationManager.get().show(Route.REGISTER);
    }
}
