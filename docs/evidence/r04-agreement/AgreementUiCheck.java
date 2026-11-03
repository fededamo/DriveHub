package it.unifi.drivehub;

import it.unifi.drivehub.dao.DatabaseManager;
import it.unifi.drivehub.domain.operation.Request;
import it.unifi.drivehub.domain.user.Client;
import it.unifi.drivehub.domain.user.Salesman;
import it.unifi.drivehub.domain.vehicle.VehicleState;
import it.unifi.drivehub.presentation.view.*;
import javafx.application.Platform;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.robot.Robot;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.math.BigDecimal;
import java.time.Year;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/** Riusa Robot, screenshot e attese del ticket 05; seed e check sono JVM distinte. */
public class AgreementUiCheck extends PriceUiCheck {
    static void seed() throws Exception {
        ctx = new AppContext(TestDatabase.fromEnvironment().reset());
        try (var conn = ctx.getDatabaseManager().getConnection(); var sql = conn.createStatement()) {
            sql.execute("DELETE FROM sale; DELETE FROM rent; DELETE FROM test_drive; DELETE FROM request;"
                    + "DELETE FROM vehicle_discount; DELETE FROM vehicle; UPDATE bank_account SET balance = 50000 WHERE client = 1");
        }
        int year = Year.now().getValue();
        var model = ctx.getCatalogService().addModel("Fiat", "UI Agreement06", new BigDecimal("20000"), year);
        for (boolean manual : new boolean[]{false, true}) {
            var vehicle = ctx.getCatalogService().addVehicle(model.getId(), manual ? "T0600000000000098" : "T0600000000000099",
                    null, 0, manual ? new BigDecimal("20000") : null, year, VehicleState.SALE);
            ctx.getPurchaseService().initiatePurchase(vehicle.getId(), 1L);
            if (manual) ctx.getCatalogService().updateVehiclePricing(vehicle.getId(), new BigDecimal("10000"));
        }
        ctx.getCatalogService().addVehicle(model.getId(), "T0600000000000097", null, 0, null, year, VehicleState.SALE);
        ctx.getSaleToDealershipService().submitSaleProposal(2L, model.getId(), "T0600000000000096", "AB123CD", 0, year);
        try (var conn = ctx.getDatabaseManager().getConnection(); var sql = conn.createStatement()) {
            sql.executeUpdate("UPDATE model SET release_price = 30000 WHERE id = " + model.getId());
        }
        ctx.getCatalogService().setModelDiscount(model.getId(), 10);
        System.out.println("SEED SUCCESS: accordi 20000/4000/16000; listino modificato; fine prima JVM");
    }

    @SuppressWarnings("unchecked")
    static TableView<Request> requests(Parent root) {
        return (TableView<Request>) root.lookupAll(".table-view").stream()
                .filter(n -> ((TableView<?>) n).getColumns().stream().anyMatch(c -> c.getText().equals("Totale concordato")))
                .findFirst().orElseThrow();
    }

    static String cell(TableView<Request> table, Request request, String column) {
        return table.getColumns().stream().filter(c -> c.getText().equals(column)).findFirst().orElseThrow()
                .getCellData(request).toString();
    }

    static void amounts(TableView<Request> table) throws Exception {
        fx(() -> {
            for (Request request : table.getItems()) {
                if (request.getAgreedTotal() == null) {
                    check(cell(table, request, "Totale concordato").equals("—"), "cessione senza totale acquisto");
                    check(cell(table, request, "Saldo da pagare").equals("—"), "cessione senza saldo acquisto");
                    check(cell(table, request, "Stima cessione").equals(UiUtils.formatCurrency(new BigDecimal("16000"))), "stima cessione conservata");
                } else {
                    check(cell(table, request, "Totale concordato").equals(UiUtils.formatCurrency(new BigDecimal("20000"))), "totale riletto 20000");
                    check(cell(table, request, "Acconto versato").equals(UiUtils.formatCurrency(new BigDecimal("4000"))), "acconto riletto 4000");
                    check(cell(table, request, "Saldo da pagare").equals(UiUtils.formatCurrency(new BigDecimal("16000"))), "saldo riletto 16000");
                }
            }
            return null;
        });
    }

    static void focusTable(TableView<?> table) throws Exception {
        fx(() -> {
            table.requestFocus();
            var bounds = table.localToScreen(table.getBoundsInLocal());
            robot.mouseMove(bounds.getMinX() + 25, bounds.getMinY() + 45);
            robot.mouseClick(MouseButton.PRIMARY);
            return null;
        });
        key(KeyCode.HOME); key(KeyCode.DOWN); key(KeyCode.UP);
        check(fx(() -> table.getSelectionModel().getSelectedIndex()) == 0, "selezione righe con tastiera e focus visibile");
        if (table.getStyleClass().contains("agreement-requests")) {
            check(fx(table::isFocused), "tabella richieste raggiunta con focus tastiera");
            check(fx(() -> table.lookupAll(".table-cell").stream().filter(n -> n instanceof TableCell<?, ?> cell
                            && cell.getTableRow().isSelected() && cell.getText() != null && !cell.getText().isBlank())
                    .allMatch(n -> ((TableCell<?, ?>) n).getTextFill().equals(javafx.scene.paint.Color.web("#1e1b4b")))),
                    "importi selezionati leggibili con testo scuro");
        }
    }

    static String dialog() throws Exception {
        for (int i = 0; i < 100; i++) {
            String content = fx(() -> Window.getWindows().stream().filter(w -> w != stage && w.isShowing())
                    .map(w -> (DialogPane) w.getScene().getRoot()).map(DialogPane::getContentText).findFirst().orElse(null));
            if (content != null) return content;
            Thread.sleep(100);
        }
        throw new AssertionError("Dialogo assente");
    }

    static void checkDialog(String total, String deposit, String remaining) throws Exception {
        String text = dialog();
        check(text.contains("Totale concordato: " + UiUtils.formatCurrency(new BigDecimal(total))), "dialogo: totale concordato");
        check(text.contains("Acconto versato: " + UiUtils.formatCurrency(new BigDecimal(deposit))), "dialogo: acconto versato");
        check(text.contains("Saldo da pagare: " + UiUtils.formatCurrency(new BigDecimal(remaining))), "dialogo: saldo da pagare");
    }

    public static void main(String[] args) throws Exception {
        if (args[0].equals("seed")) { seed(); return; }
        Thread.setDefaultUncaughtExceptionHandler((thread, error) -> { error.printStackTrace(); System.exit(2); });
        out = "/tmp/drivehub-ticket06-ui/";
        TestDatabase.fromEnvironment();
        ctx = new AppContext(new DatabaseManager(System.getenv("DRIVEHUB_TEST_DB_URL"),
                System.getenv("DRIVEHUB_TEST_DB_USER"), System.getenv("DRIVEHUB_TEST_DB_PASSWORD")));
        var ready = new CountDownLatch(1);
        Platform.startup(ready::countDown);
        check(ready.await(10, TimeUnit.SECONDS), "JavaFX avviato nella seconda JVM");
        Platform.setImplicitExit(false);
        try {
            fx(() -> { stage = new Stage(); robot = new Robot(); return null; });
            Salesman seller = (Salesman) ctx.getAuthService().login("paolo.costa@drivehub.it", "password");
            show(fx(() -> new SalesmanDashboardView(seller, ctx.getAuthService(), ctx.getPurchaseService(),
                    ctx.getSaleToDealershipService(), ctx.getRentalService(), ctx.getCatalogService(), ctx.getOperationQueryService(), () -> {})));
            TableView<Request> pending = fx(() -> requests(stage.getScene().getRoot()));
            check(fx(() -> pending.getItems().size()) == 3, "due accordi e una cessione riletti dopo riavvio JVM");
            amounts(pending); focusTable(pending); screenshot("seller-agreements");
            Long approved = fx(() -> pending.getSelectionModel().getSelectedItem().getId());
            reach(n -> n instanceof Button b && b.getText().equals("Approva Richiesta")); key(KeyCode.SPACE);
            check(dialog().contains(UiUtils.formatCurrency(new BigDecimal("16000"))), "approvazione mostra saldo concordato");
            closeAlert();
            check(ctx.getOperationQueryService().getClientRequests(1L).stream()
                    .anyMatch(r -> r.getId().equals(approved) && r.getSalesmanId() != null), "approvazione da tastiera persistita");

            Client client = (Client) ctx.getAuthService().login("mario.rossi@email.it", "password");
            show(fx(() -> new ClientDashboardView(client, ctx.getAuthService(), ctx.getPurchaseService(), ctx.getSaleToDealershipService(),
                    ctx.getRentalService(), ctx.getTestDriveService(), ctx.getCatalogService(), ctx.getOperationQueryService(), ctx.getPriceCalculatorService(), () -> {})));
            TabPane tabs = fx(() -> (TabPane) ((ClientDashboardView) stage.getScene().getRoot()).getCenter());
            fx(() -> { tabs.requestFocus(); return null; });
            key(KeyCode.RIGHT); key(KeyCode.RIGHT); key(KeyCode.RIGHT);
            check(fx(() -> tabs.getSelectionModel().getSelectedIndex()) == 3, "operazioni raggiunte con frecce");
            TableView<Request> mine = fx(() -> requests(stage.getScene().getRoot()));
            check(fx(() -> mine.getItems().size()) == 2, "entrambi gli accordi cliente presenti");
            amounts(mine); focusTable(mine); key(KeyCode.END);
            check(fx(() -> mine.getSelectionModel().getSelectedItem().getId()).equals(approved), "richiesta approvata selezionata da tastiera");
            screenshot("client-agreements");
            reach(n -> n instanceof Button b && b.getText().equals("Paga saldo concordato e completa acquisto")); key(KeyCode.SPACE);
            checkDialog("20000", "4000", "16000"); screenshot("client-confirmation");
            // Il dialogo di conferma può aprirsi con Annulla selezionato: scegliamo esplicitamente OK.
            fx(() -> { Window.getWindows().stream().filter(w -> w != stage && w.isShowing()).forEach(w -> {
                DialogPane pane = (DialogPane) w.getScene().getRoot();
                pane.lookupButton(ButtonType.OK).requestFocus();
            }); return null; });
            key(KeyCode.ENTER); closeAlert();
            check(ctx.getOperationQueryService().getClientSales(1L).getFirst().getAmount().equals(new BigDecimal("20000.00")), "UI registra vendita 20000");
            check(ctx.getAuthService().getClientBankAccount(1L).getBalance().equals(new BigDecimal("26000.00")), "UI addebita soltanto saldo 16000");
            screenshot("client-completed");

            fx(() -> { tabs.requestFocus(); return null; });
            key(KeyCode.LEFT); key(KeyCode.LEFT); key(KeyCode.LEFT);
            check(fx(() -> tabs.getSelectionModel().getSelectedIndex()) == 0, "ritorno al catalogo con frecce");
            TableView<?> catalog = fx(() -> (TableView<?>) tabs.getTabs().getFirst().getContent().lookup(".table-view"));
            focusTable(catalog);
            reach(n -> n instanceof Button b && b.getText().startsWith("Acquista Veicolo")); key(KeyCode.SPACE);
            check(dialog().contains("Prezzo effettivo attuale"), "preventivo avvio distinto da accordo persistito");
            fx(() -> { Window.getWindows().stream().filter(w -> w != stage && w.isShowing()).forEach(w ->
                    ((DialogPane) w.getScene().getRoot()).lookupButton(ButtonType.OK).requestFocus()); return null; });
            key(KeyCode.ENTER); checkDialog("27000", "5400", "21600");
            Thread.sleep(300); screenshot("client-new-agreement"); closeAlert();
            System.out.println("UI CHECK SUCCESS");
        } finally { Platform.exit(); }
    }
}
