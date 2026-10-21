package it.unifi.ing.drivehub;

import it.unifi.ing.drivehub.business.security.Pbkdf2PasswordHasher;
import it.unifi.ing.drivehub.business.services.AuthService;
import it.unifi.ing.drivehub.business.services.BankAccountService;
import it.unifi.ing.drivehub.business.services.CatalogService;
import it.unifi.ing.drivehub.business.services.PricingService;
import it.unifi.ing.drivehub.business.services.RentalService;
import it.unifi.ing.drivehub.business.services.SalesService;
import it.unifi.ing.drivehub.business.services.TestDriveService;
import it.unifi.ing.drivehub.dao.interfaces.DaoFactory;
import it.unifi.ing.drivehub.dao.postgres.DatabaseBootstrap;
import it.unifi.ing.drivehub.dao.postgres.DatabaseConfig;
import it.unifi.ing.drivehub.dao.postgres.DriverManagerDataSource;
import it.unifi.ing.drivehub.dao.postgres.PostgresDaoFactory;
import it.unifi.ing.drivehub.presentation.core.ApplicationContext;
import it.unifi.ing.drivehub.presentation.core.ServiceUiGateway;
import it.unifi.ing.drivehub.presentation.core.UiGateway;
import it.unifi.ing.drivehub.presentation.navigation.NavigationManager;
import it.unifi.ing.drivehub.presentation.navigation.Route;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import javax.sql.DataSource;

/** JavaFX composition root. No controller knows how services or JDBC are built. */
public final class DriveHubApplication extends Application {
    private ApplicationContext context;

    @Override
    public void start(Stage stage) {
        stage.setOnCloseRequest(e -> {
            try {
                stop();
            } finally {
                javafx.application.Platform.exit();
                System.exit(0);
            }
        });

        try {
            launchWithGateway(stage, createGateway());
        } catch (Exception failure) {
            System.err.println("Connessione PostgreSQL non disponibile (" + failure.getMessage() + "). Avvio automatico in modalità Demo In-Memory...");
            try {
                launchWithGateway(stage, createInMemoryDemoGateway());
            } catch (Exception fallbackFailure) {
                showStartupFailure(stage, new RuntimeException(fallbackFailure));
            }
        }
    }

    private void launchWithGateway(Stage stage, UiGateway gateway) {
        context = new ApplicationContext(gateway);
        NavigationManager.initialize(stage, context);
        NavigationManager.get().show(Route.WELCOME);
    }

    @Override
    public void stop() {
        if (context != null) {
            try {
                context.close();
            } catch (Exception ignored) {
            }
        }
        javafx.application.Platform.exit();
        System.exit(0);
    }

    private static UiGateway createGateway() {
        DatabaseConfig config = DatabaseConfig.fromEnvironment();
        DataSource dataSource = new DriverManagerDataSource(config);
        new DatabaseBootstrap(dataSource, config.seedDemoData()).initialize();
        DaoFactory daoFactory = new PostgresDaoFactory(dataSource);

        AuthService auth = new AuthService(daoFactory, new Pbkdf2PasswordHasher());
        BankAccountService bankAccounts = new BankAccountService(daoFactory);
        CatalogService catalog = new CatalogService(daoFactory);
        SalesService sales = new SalesService(daoFactory);
        RentalService rentals = new RentalService(daoFactory);
        TestDriveService testDrives = new TestDriveService(daoFactory);
        PricingService pricing = new PricingService(daoFactory);

        return new ServiceUiGateway(auth, bankAccounts, catalog, sales, rentals, testDrives, pricing);
    }

    private static UiGateway createInMemoryDemoGateway() {
        org.h2.jdbcx.JdbcDataSource h2 = new org.h2.jdbcx.JdbcDataSource();
        h2.setURL("jdbc:h2:mem:drivehub_demo;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DEFAULT_NULL_ORDERING=HIGH;DB_CLOSE_DELAY=-1");
        h2.setUser("sa");
        h2.setPassword("");
        new DatabaseBootstrap(h2, true).initialize();
        DaoFactory daoFactory = new PostgresDaoFactory(h2);

        AuthService auth = new AuthService(daoFactory, new Pbkdf2PasswordHasher());
        BankAccountService bankAccounts = new BankAccountService(daoFactory);
        CatalogService catalog = new CatalogService(daoFactory);
        SalesService sales = new SalesService(daoFactory);
        RentalService rentals = new RentalService(daoFactory);
        TestDriveService testDrives = new TestDriveService(daoFactory);
        PricingService pricing = new PricingService(daoFactory);

        return new ServiceUiGateway(auth, bankAccounts, catalog, sales, rentals, testDrives, pricing);
    }

    private void showStartupFailure(Stage stage, RuntimeException failure) {
        Label title = new Label("DriveHub non può collegarsi al database PostgreSQL");
        title.getStyleClass().add("startup-error-title");
        Label guidance = new Label("""
                Verifica che PostgreSQL sia avviato e che le credenziali siano corrette.
                Configurazione rilevata / predefinita:
                  - host: localhost:5432
                  - database: drivehub
                  - utente: drivehub
                
                Oppure avvia subito l'applicazione in modalità dimostrativa in-memory con i dati demo precaricati.
                """);
        guidance.getStyleClass().add("startup-error-body");
        Label details = new Label(failure.getMessage());
        details.getStyleClass().add("startup-error-details");
        details.setWrapText(true);

        Button demoButton = new Button("▶ Avvia in modalità Demo (Database In-Memory)");
        demoButton.getStyleClass().add("primary-button");
        demoButton.setOnAction(e -> {
            try {
                launchWithGateway(stage, createInMemoryDemoGateway());
            } catch (Exception ex) {
                details.setText("Errore avvio demo: " + ex.getMessage());
            }
        });

        VBox root = new VBox(14, title, guidance, details, demoButton);
        root.setPadding(new Insets(24));
        root.getStyleClass().add("startup-error-root");
        Scene scene = new Scene(root, 680, 420);
        scene.getStylesheets().add(DriveHubApplication.class.getResource("/it/unifi/ing/drivehub/presentation/css/style.css").toExternalForm());
        stage.setTitle("DriveHub - Avvio applicazione");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
