package it.unifi.ing.drivehub.presentation.navigation;

import it.unifi.ing.drivehub.presentation.core.ApplicationContext;
import it.unifi.ing.drivehub.presentation.core.ContextAware;
import it.unifi.ing.drivehub.presentation.core.UiModels;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.Objects;

public final class NavigationManager {
    private static NavigationManager instance;

    private final Stage stage;
    private final ApplicationContext context;

    private NavigationManager(Stage stage, ApplicationContext context) {
        this.stage = Objects.requireNonNull(stage);
        this.context = Objects.requireNonNull(context);
    }

    public static void initialize(Stage stage, ApplicationContext context) {
        instance = new NavigationManager(stage, context);
    }

    public static NavigationManager get() {
        if (instance == null) {
            throw new IllegalStateException("NavigationManager non inizializzato");
        }
        return instance;
    }

    public void show(Route route) {
        Objects.requireNonNull(route);
        enforceGuard(route);
        URL resource = NavigationManager.class.getResource("../view/" + route.fxml());
        if (resource == null) {
            throw new IllegalStateException("Vista non trovata: " + route.fxml());
        }
        try {
            FXMLLoader loader = new FXMLLoader(resource);
            Parent root = loader.load();
            Object controller = loader.getController();
            if (controller instanceof ContextAware aware) {
                aware.setApplicationContext(context);
            }
            Scene scene = stage.getScene();
            if (scene == null) {
                stage.setScene(new Scene(root));
            } else {
                scene.setRoot(root);
            }
            stage.setTitle("DriveHub — " + route.name());
            stage.setMinWidth(960);
            stage.setMinHeight(640);
            stage.show();
        } catch (IOException exception) {
            throw new IllegalStateException("Impossibile caricare " + route.fxml(), exception);
        }
    }

    public void showHomeForCurrentRole() {
        UiModels.Session current = context.session().requireCurrent();
        switch (current.role()) {
            case CLIENT, CUSTOMER -> show(Route.CUSTOMER);
            case SALESMAN -> show(Route.SALESMAN);
            case MANAGER -> show(Route.MANAGER);
        }
    }

    public void logout() {
        context.session().clear();
        show(Route.WELCOME);
    }

    private void enforceGuard(Route route) {
        if (route.isPublic()) {
            return;
        }
        UiModels.Session current = context.session().requireCurrent();
        if (!route.allows(current.role())) {
            throw new SecurityException("Il ruolo " + current.role() + " non può aprire " + route);
        }
    }
}
