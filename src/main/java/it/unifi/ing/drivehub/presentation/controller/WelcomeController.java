package it.unifi.ing.drivehub.presentation.controller;

import it.unifi.ing.drivehub.presentation.navigation.NavigationManager;
import it.unifi.ing.drivehub.presentation.navigation.Route;
import javafx.fxml.FXML;

public final class WelcomeController {
    @FXML
    private void openLogin() {
        NavigationManager.get().show(Route.LOGIN);
    }

    @FXML
    private void openRegistration() {
        NavigationManager.get().show(Route.REGISTER);
    }
}
