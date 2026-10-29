package it.unifi.drivehub;

import it.unifi.drivehub.domain.user.Client;
import it.unifi.drivehub.domain.user.Manager;
import it.unifi.drivehub.domain.user.Salesman;
import it.unifi.drivehub.domain.user.User;
import it.unifi.drivehub.presentation.view.ClientDashboardView;
import it.unifi.drivehub.presentation.view.LoginView;
import it.unifi.drivehub.presentation.view.ManagerDashboardView;
import it.unifi.drivehub.presentation.view.SalesmanDashboardView;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;

/**
 * Entry point dell'applicazione desktop JavaFX DriveHub.
 */
public final class App extends Application {

    private Stage primaryStage;

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        primaryStage.setTitle("DriveHub - Piattaforma Concessionario Auto");

        Platform.setImplicitExit(false);
        primaryStage.setOnCloseRequest(e -> {
            Platform.exit();
            System.exit(0);
        });

        // Inizializzazione Composition Root
        AppContext.getInstance();

        showLoginView();
        primaryStage.show();
    }

    private void showLoginView() {
        LoginView loginView = new LoginView(this::showDashboardForUser);
        Scene scene = new Scene(loginView, 900, 650);
        applyTheme(scene);
        primaryStage.setScene(scene);
        primaryStage.centerOnScreen();
    }

    private void showDashboardForUser(User user) {
        Scene scene;
        if (user instanceof Client client) {
            ClientDashboardView dashboard = new ClientDashboardView(client, this::showLoginView);
            scene = new Scene(dashboard, 1100, 750);
        } else if (user instanceof Salesman salesman) {
            SalesmanDashboardView dashboard = new SalesmanDashboardView(salesman, this::showLoginView);
            scene = new Scene(dashboard, 1050, 720);
        } else if (user instanceof Manager manager) {
            ManagerDashboardView dashboard = new ManagerDashboardView(manager, this::showLoginView);
            scene = new Scene(dashboard, 1150, 750);
        } else {
            throw new IllegalStateException("Tipo utente non gestito: " + user.getClass().getName());
        }

        applyTheme(scene);
        primaryStage.setScene(scene);
        primaryStage.centerOnScreen();
    }

    private void applyTheme(Scene scene) {
        String css = getClass().getResource("/css/style.css") != null
                ? getClass().getResource("/css/style.css").toExternalForm()
                : null;
        if (css != null) {
            scene.getStylesheets().add(css);
        }
    }

    public static void main(String[] args) {
        installCrashHandler();
        launch(args);
    }

    private static void installCrashHandler() {
        Thread.setDefaultUncaughtExceptionHandler((thread, throwable) -> {
            System.err.println("=== CRASH FATALE APPLICAZIONE (Thread: " + thread.getName() + ") ===");
            throwable.printStackTrace(System.err);
            try {
                StringWriter sw = new StringWriter();
                PrintWriter pw = new PrintWriter(sw);
                throwable.printStackTrace(pw);
                String msg = "Data: " + LocalDateTime.now() + "\nThread: " + thread.getName() + "\n" + sw + "\n----------------------------------------\n";
                Files.writeString(Path.of("app_error.log"), msg, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
            } catch (Exception ignored) {}
        });
    }
}

