package it.unifi.drivehub;

import it.unifi.drivehub.dao.DatabaseManager;
import it.unifi.drivehub.domain.operation.Request;
import it.unifi.drivehub.domain.operation.RequestType;
import it.unifi.drivehub.domain.operation.Sale;
import it.unifi.drivehub.domain.user.Client;
import it.unifi.drivehub.domain.user.Manager;
import it.unifi.drivehub.domain.user.Salesman;
import it.unifi.drivehub.presentation.view.*;
import javafx.application.Platform;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.robot.Robot;
import javafx.stage.Stage;
import javafx.stage.Window;
import java.math.BigDecimal;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/** Viste reali e tastiera sui dati 841d4bc convertiti, in una seconda JVM. */
public class ConvertedDataUiCheck extends AgreementUiCheck {
    public static void main(String[] args) throws Exception {
        if (args[0].equals("seed")) {
            var db = LegacyContractFixture.reset();
            LegacyContractFixture.convert(db);
            LegacyContractFixture.convert(db);
            new AppContext(db).getCatalogService().updateVehiclePricing(1L, new BigDecimal("35000"));
            LegacyContractFixture.sql(db, "UPDATE model SET release_price=30000 WHERE id=1");
            System.out.println("SEED SUCCESS: fotografia 841d4bc convertita due volte; listino modificato");
            return;
        }
        out = args[1] + "/";
        Thread.setDefaultUncaughtExceptionHandler((thread, error) -> { error.printStackTrace(); System.exit(2); });
        TestDatabase.fromEnvironment();
        ctx = new AppContext(new DatabaseManager(System.getenv("DRIVEHUB_TEST_DB_URL"),
                System.getenv("DRIVEHUB_TEST_DB_USER"), System.getenv("DRIVEHUB_TEST_DB_PASSWORD")));
        var ready = new CountDownLatch(1);
        Platform.startup(ready::countDown);
        check(ready.await(10, TimeUnit.SECONDS), "JavaFX avviato nella seconda JVM");
        Platform.setImplicitExit(false);
        try {
            fx(() -> { stage = new Stage(); robot = new Robot(); return null; });
            var seller = (Salesman) ctx.getAuthService().login("paolo.costa@drivehub.it", "password");
            show(fx(() -> new SalesmanDashboardView(seller, ctx.getAuthService(), ctx.getPurchaseService(),
                    ctx.getSaleToDealershipService(), ctx.getRentalService(), ctx.getCatalogService(),
                    ctx.getOperationQueryService(), () -> {})));
            var pending = fx(() -> requests(stage.getScene().getRoot()));
            fx(() -> {
                check(pending.getItems().size() == 3, "tre richieste non approvate convertite");
                for (Request request : pending.getItems()) {
                    boolean sale = request.getType() == RequestType.SALE;
                    check(cell(pending, request, "Tipologia").equals(sale ? "VENDITA AL CLIENTE" : "ACQUISTO DAL CLIENTE"),
                            "tipo convertito " + request.getId());
                    check(cell(pending, request, "Stima cessione").equals(sale ? "—" : UiUtils.formatCurrency(request.getDeposit())),
                            "stima positiva o zero " + request.getId());
                    if (sale) {
                        check(cell(pending, request, "Saldo da pagare").equals(UiUtils.formatCurrency(new BigDecimal("16000"))), "saldo 16000");
                    } else {
                        check(cell(pending, request, "Totale concordato").equals("—"), "cessione senza totale");
                        check(cell(pending, request, "Acconto versato").equals("—"), "cessione senza acconto");
                    }
                }
                return null;
            });
            select(pending, 105L);
            screenshot("seller-converted-requests");
            press("Approva Richiesta");
            check(dialog().contains("pagamento accreditato"), "cessione convertita accettata da tastiera");
            closeAlert();
            balance("42000.00");
            check(ctx.getOperationQueryService().getClientRequests(1L).stream().noneMatch(r -> r.getId() == 105L), "cessione elaborata una volta");

            var client = (Client) ctx.getAuthService().login("mario.rossi@email.it", "password");
            show(fx(() -> new ClientDashboardView(client, ctx.getAuthService(), ctx.getPurchaseService(),
                    ctx.getSaleToDealershipService(), ctx.getRentalService(), ctx.getTestDriveService(),
                    ctx.getCatalogService(), ctx.getOperationQueryService(), ctx.getPriceCalculatorService(), () -> {})));
            check(dialog().contains("3200.00"), "scadenza convertita: rimborso 3200");
            screenshot("client-converted-expiry");
            closeAlert();
            balance("45200.00");
            var clientTabs = fx(() -> (TabPane) ((ClientDashboardView) stage.getScene().getRoot()).getCenter());
            fx(() -> { clientTabs.requestFocus(); return null; });
            key(KeyCode.RIGHT); key(KeyCode.RIGHT); key(KeyCode.RIGHT);
            check(fx(() -> clientTabs.getSelectionModel().getSelectedIndex()) == 3, "operazioni cliente raggiunte con frecce");
            var mine = fx(() -> requests(stage.getScene().getRoot()));
            check(fx(() -> mine.getItems().stream().allMatch(r -> r.getType() == RequestType.SALE)), "cliente vede solo i propri acquisti");
            screenshot("client-converted-requests");
            select(mine, 101L);
            press("Paga saldo concordato e completa acquisto");
            checkDialog("20000", "4000", "16000");
            screenshot("client-converted-confirmation");
            fx(() -> {
                Window.getWindows().stream().filter(w -> w != stage && w.isShowing()).forEach(w ->
                        ((DialogPane) w.getScene().getRoot()).lookupButton(ButtonType.OK).requestFocus());
                return null;
            });
            key(KeyCode.ENTER);
            check(dialog().contains("Acquisto completato"), "accordo convertito completato da tastiera");
            closeAlert();
            balance("29200.00");
            screenshot("client-converted-completed");

            var manager = (Manager) ctx.getAuthService().login("vittorio.manager@drivehub.it", "password");
            show(fx(() -> new ManagerDashboardView(manager, ctx.getAuthService(), ctx.getCatalogService(), ctx.getOperationQueryService(), () -> {})));
            var tabs = fx(() -> (TabPane) ((ManagerDashboardView) stage.getScene().getRoot()).getCenter());
            fx(() -> { tabs.requestFocus(); return null; });
            for (int i = 0; i < tabs.getTabs().size() - 1; i++) key(KeyCode.RIGHT);
            check(fx(() -> tabs.getSelectionModel().getSelectedItem().getText()).equals("Registro economico"), "registro raggiunto con frecce");
            @SuppressWarnings("unchecked")
            var table = fx(() -> (TableView<Sale>) tabs.getSelectionModel().getSelectedItem().getContent().lookup(".table-view"));
            fx(() -> {
                check(table.getItems().size() == 11, "otto operazioni convertite e tre nuovi esiti");
                for (Sale sale : table.getItems()) {
                    String label = switch (sale.getType()) {
                        case SALE -> "VENDITA (Entrata)";
                        case PURCHASE -> "ACQUISTO (Uscita)";
                        case PENALTY -> "PENALE (Entrata)";
                    };
                    check(table.getColumns().get(1).getCellData(sale).equals(label), "registro: " + sale.getId() + " " + label);
                    check(sale.getAmount().signum() >= 0, "importo non negativo " + sale.getId());
                    check(table.getColumns().get(6).getCellData(sale).equals(UiUtils.formatCurrency(sale.getAmount())), "importo leggibile " + sale.getId());
                }
                return null;
            });
            focusTable(table); key(KeyCode.END);
            check(fx(() -> table.getSelectionModel().getSelectedIndex()) == 10, "registro navigabile fino all'ultima riga");
            screenshot("manager-converted-ledger");
            System.out.println("UI CHECK SUCCESS");
        } finally {
            fx(() -> {
                java.util.List.copyOf(Window.getWindows()).stream().filter(w -> w != stage).forEach(Window::hide);
                stage.close();
                return null;
            });
            Platform.exit();
        }
    }

    static void select(TableView<Request> table, long id) throws Exception {
        int index = fx(() -> {
            for (int i = 0; i < table.getItems().size(); i++) if (table.getItems().get(i).getId() == id) return i;
            throw new AssertionError("Richiesta assente " + id);
        });
        fx(() -> { stage.toFront(); stage.requestFocus(); return null; });
        reach(n -> n == table);
        key(KeyCode.HOME);
        for (int i = 0; i < index; i++) key(KeyCode.DOWN);
        check(fx(() -> table.getSelectionModel().getSelectedItem().getId()) == id, "richiesta selezionata con frecce " + id);
    }

    static void press(String text) throws Exception {
        reach(n -> n instanceof Button b && b.getText().equals(text));
        key(KeyCode.SPACE);
    }

    static void balance(String expected) {
        check(ctx.getAuthService().getClientBankAccount(1L).getBalance().equals(new BigDecimal(expected)), "saldo conto " + expected);
    }
}
