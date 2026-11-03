package it.unifi.drivehub;

import it.unifi.drivehub.dao.DatabaseManager;
import it.unifi.drivehub.domain.user.Salesman;
import it.unifi.drivehub.domain.vehicle.Vehicle;
import it.unifi.drivehub.domain.vehicle.VehicleState;
import javafx.application.Platform;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.robot.Robot;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.math.BigDecimal;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/** Riusa i controlli pubblici e Robot 05/06/10, sul solo PostgreSQL dedicato. */
public class DestinationUiCheck extends SaleOutcomeUiCheck {
    static void seed() throws Exception {
        ctx = new AppContext(TestDatabase.fromEnvironment().reset());
        try (var conn = ctx.getDatabaseManager().getConnection(); var sql = conn.createStatement()) {
            sql.execute("""
                    DELETE FROM sale; DELETE FROM rent; DELETE FROM test_drive; DELETE FROM request;
                    DELETE FROM vehicle_discount; DELETE FROM vehicle;
                    UPDATE bank_account SET balance=1000 WHERE client IN (1,2);
                    INSERT INTO model(id,brand,name,release_price,release_year)
                      VALUES(1201,'Fiat','UI Destination12',100,EXTRACT(YEAR FROM CURRENT_DATE));
                    INSERT INTO vehicle(id,model,vin,kilometers,price,year,state)
                      SELECT 1200+n,1201,'T120000000000000'||n,0,100,EXTRACT(YEAR FROM CURRENT_DATE),
                        (CASE WHEN n IN (4,5) THEN 'SALE' WHEN n=6 THEN 'SOLD'
                              WHEN n=7 THEN 'WAITING' ELSE 'RENT' END)::vehicle_state
                      FROM generate_series(1,7) n;
                    INSERT INTO rent(vehicle,client,amount,start,"end") VALUES
                      (1201,1,100.25,CURRENT_DATE+2,CURRENT_DATE+2),
                      (1201,2,200.75,CURRENT_DATE+3,CURRENT_DATE+3),
                      (1202,1,50,CURRENT_DATE-1,CURRENT_DATE),
                      (1202,2,60,CURRENT_DATE+3,CURRENT_DATE+3),
                      (1203,1,100.25,CURRENT_DATE+2,CURRENT_DATE+2),
                      (1203,2,200.75,CURRENT_DATE+3,CURRENT_DATE+3);
                    """);
        }
        System.out.println("SEED SUCCESS: futuro multi-cliente, noleggio iniziato ieri, stato obsoleto e rollback");
    }

    @SuppressWarnings("unchecked")
    static TableView<Vehicle> inventory() {
        return (TableView<Vehicle>) stage.getScene().getRoot().lookupAll(".table-view").stream()
                .filter(n -> ((TableView<?>) n).getColumns().stream().anyMatch(c -> c.getText().equals("VIN")))
                .findFirst().orElseThrow();
    }

    static void openInventory(Salesman user) throws Exception {
        show(fx(() -> seller(user)));
        TabPane tabs = fx(() -> (TabPane) ((it.unifi.drivehub.presentation.view.SalesmanDashboardView)
                stage.getScene().getRoot()).getCenter());
        fx(() -> { tabs.requestFocus(); return null; });
        key(KeyCode.RIGHT);
        check(fx(() -> tabs.getSelectionModel().getSelectedIndex()) == 1, "inventario raggiunto con freccia");
    }

    static void selectVehicle(long id) throws Exception {
        var table = fx(DestinationUiCheck::inventory);
        focusTable(table);
        int index = fx(() -> {
            for (int i=0; i<table.getItems().size(); i++) if (table.getItems().get(i).getId()==id) return i;
            throw new AssertionError("Veicolo assente " + id);
        });
        for (int i=0; i<index; i++) key(KeyCode.DOWN);
        check(fx(() -> table.getSelectionModel().getSelectedItem().getId())==id, "selezione con frecce " + id);
        press("Modifica Stato (Da RENT a SALE o viceversa)");
    }

    static void confirm() throws Exception {
        fx(() -> {
            Window.getWindows().stream().filter(w -> w != stage && w.isShowing()).forEach(w ->
                    ((DialogPane) w.getScene().getRoot()).lookupButton(ButtonType.OK).requestFocus());
            return null;
        });
        key(KeyCode.ENTER);
    }

    static void balance(long id, String amount) {
        check(ctx.getAuthService().getClientBankAccount(id).getBalance().equals(new BigDecimal(amount)),
                "saldo cliente " + id + " = " + amount);
    }

    public static void main(String[] args) throws Exception {
        if (args[0].equals("seed")) { seed(); return; }
        out = args[1] + "/";
        TestDatabase.fromEnvironment();
        ctx = new AppContext(new DatabaseManager(System.getenv("DRIVEHUB_TEST_DB_URL"),
                System.getenv("DRIVEHUB_TEST_DB_USER"), System.getenv("DRIVEHUB_TEST_DB_PASSWORD")));
        Thread.setDefaultUncaughtExceptionHandler((thread,error) -> { error.printStackTrace(); System.exit(2); });
        var ready = new CountDownLatch(1);
        Platform.startup(ready::countDown);
        check(ready.await(10, TimeUnit.SECONDS), "JavaFX avviato nella seconda JVM");
        Platform.setImplicitExit(false);
        try {
            fx(() -> { stage = new Stage(); robot = new Robot(); return null; });
            var user = (Salesman) ctx.getAuthService().login("paolo.costa@drivehub.it", "password");
            openInventory(user);
            check(fx(() -> inventory().getItems().size())==5, "SOLD e WAITING esclusi dall'inventario");
            selectVehicle(1201);
            check(dialog().contains("2 prenotazioni future"), "conferma con conteggio futuro e rimborso 100%");
            screenshot("future-confirmation"); confirm();
            check(dialog().contains("Rimborsati con successo 2"), "successo soltanto dopo commit");
            screenshot("refunded"); closeAlert();
            balance(1,"1100.25"); balance(2,"1200.75");
            check(ctx.getCatalogService().getVehicleById(1201L).orElseThrow().getState()==VehicleState.SALE,
                    "destinazione SALE persistita");

            openInventory(user); selectVehicle(1202); confirm();
            check(dialog().contains("noleggio in corso"), "rifiuto leggibile per noleggio iniziato ieri");
            screenshot("ongoing-rejected"); closeAlert();
            balance(1,"1100.25"); balance(2,"1200.75");

            openInventory(user);
            ctx.getPurchaseService().initiatePurchase(1204L,1L);
            selectVehicle(1204); confirm();
            check(dialog().contains("stato attuale: BLOCKED"), "vista obsoleta non aggira la riserva");
            screenshot("stale-rejected"); closeAlert();

            openInventory(user); selectVehicle(1205); confirm();
            check(dialog().contains("Stato modificato in RENT"), "SALE verso RENT consentito"); closeAlert();

            try (var conn = ctx.getDatabaseManager().getConnection(); var sql = conn.createStatement()) {
                sql.execute("DELETE FROM bank_account WHERE client=2");
            }
            openInventory(user); selectVehicle(1203); confirm();
            check(dialog().contains("Conto cliente non trovato"), "conto mancante segnala errore");
            screenshot("refund-rollback"); closeAlert();
            balance(1,"1080.25");
            check(ctx.getCatalogService().getVehicleById(1203L).orElseThrow().getState()==VehicleState.RENT,
                    "rollback conserva destinazione");
            check(ctx.getOperationQueryService().getClientRents(1L).stream().anyMatch(r -> r.getVehicleId()==1203L)
                    && ctx.getOperationQueryService().getClientRents(2L).stream().anyMatch(r -> r.getVehicleId()==1203L),
                    "rollback conserva entrambi i noleggi");
            check(ctx.getOperationQueryService().getAllSales().isEmpty(), "nessuna nuova penale");
            System.out.println("UI CHECK SUCCESS");
        } finally {
            fx(() -> { stage.close(); return null; }); Platform.exit();
        }
    }
}
