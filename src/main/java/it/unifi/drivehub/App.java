package it.unifi.drivehub;

import it.unifi.drivehub.domain.user.Client;
import it.unifi.drivehub.domain.user.Manager;
import it.unifi.drivehub.domain.user.Salesman;
import it.unifi.drivehub.domain.user.User;
import it.unifi.drivehub.domain.user.UserDashboardNavigator;
import it.unifi.drivehub.presentation.view.ClientDashboardView;
import it.unifi.drivehub.presentation.view.LoginView;
import it.unifi.drivehub.presentation.view.ManagerDashboardView;
import it.unifi.drivehub.presentation.view.SalesmanDashboardView;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Parent;
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
 * Agisce come Composition Root assemblando e iniettando le dipendenze nelle View.
 * Utilizza il pattern Double Dispatch / Visitor tramite UserDashboardNavigator
 * per la navigazione polimorfica delle dashboard, eliminando l'anti-pattern instanceof.
 */
public final class App extends Application {

    private Stage primaryStage;
    private AppContext ctx;

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
        this.ctx = AppContext.getInstance();

        showLoginView();
        primaryStage.show();
    }

    private void showLoginView() {
        LoginView loginView = new LoginView(ctx.getAuthService(), this::showDashboardForUser);
        Scene scene = new Scene(loginView, 900, 650);
        applyTheme(scene);
        primaryStage.setScene(scene);
        primaryStage.centerOnScreen();
    }

    private void showDashboardForUser(User user) {
        user.openDashboard(new UserDashboardNavigator() {
            @Override
            public void showClientDashboard(Client client) {
                ClientDashboardView dashboard = new ClientDashboardView(
                        client,
                        ctx.getAuthService(),
                        ctx.getPurchaseService(),
                        ctx.getSaleToDealershipService(),
                        ctx.getRentalService(),
                        ctx.getTestDriveService(),
                        ctx.getCatalogService(),
                        ctx.getOperationQueryService(),
                        ctx.getPriceCalculatorService(),
                        App.this::showLoginView
                );
                displayScene(dashboard, 1100, 750);
            }

            @Override
            public void showSalesmanDashboard(Salesman salesman) {
                SalesmanDashboardView dashboard = new SalesmanDashboardView(
                        salesman,
                        ctx.getAuthService(),
                        ctx.getPurchaseService(),
                        ctx.getSaleToDealershipService(),
                        ctx.getRentalService(),
                        ctx.getCatalogService(),
                        ctx.getOperationQueryService(),
                        App.this::showLoginView
                );
                displayScene(dashboard, 1050, 720);
            }

            @Override
            public void showManagerDashboard(Manager manager) {
                ManagerDashboardView dashboard = new ManagerDashboardView(
                        manager,
                        ctx.getAuthService(),
                        ctx.getCatalogService(),
                        ctx.getOperationQueryService(),
                        App.this::showLoginView
                );
                displayScene(dashboard, 1150, 750);
            }
        });
    }

    private void displayScene(Parent root, double width, double height) {
        Scene scene = new Scene(root, width, height);
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
