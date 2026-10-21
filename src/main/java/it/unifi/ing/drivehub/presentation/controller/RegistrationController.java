package it.unifi.ing.drivehub.presentation.controller;

import it.unifi.ing.drivehub.domain.users.Role;
import it.unifi.ing.drivehub.presentation.core.UiModels;
import it.unifi.ing.drivehub.presentation.navigation.NavigationManager;
import it.unifi.ing.drivehub.presentation.navigation.Route;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.util.Arrays;

public final class RegistrationController extends AbstractController {
    @FXML private TextField firstNameField;
    @FXML private TextField lastNameField;
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private ComboBox<Role> roleCombo;
    @FXML private Label errorLabel;

    @FXML
    private void initialize() {
        roleCombo.setItems(FXCollections.observableArrayList(Role.CLIENT, Role.SALESMAN, Role.MANAGER));
        roleCombo.setValue(Role.CLIENT);
    }

    @FXML
    private void register() {
        char[] password = passwordField.getText().toCharArray();
        try {
            Role role = roleCombo.getValue();
            if (role == null) {
                throw new IllegalArgumentException("Seleziona un ruolo");
            }
            UiModels.Session registered = gateway().register(
                    firstNameField.getText(), lastNameField.getText(),
                    emailField.getText(), password, role);
            context.session().open(registered);
            NavigationManager.get().showHomeForCurrentRole();
        } catch (RuntimeException exception) {
            showError(errorLabel, exception);
            passwordField.clear();
        } finally {
            Arrays.fill(password, '\0');
        }
    }

    @FXML
    private void openLogin() {
        NavigationManager.get().show(Route.LOGIN);
    }
}
