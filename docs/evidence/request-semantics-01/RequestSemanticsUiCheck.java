package it.unifi.drivehub;

import it.unifi.drivehub.dao.DatabaseManager;
import it.unifi.drivehub.domain.operation.Request;
import it.unifi.drivehub.domain.operation.RequestType;
import it.unifi.drivehub.domain.user.Client;
import it.unifi.drivehub.domain.user.Salesman;
import it.unifi.drivehub.presentation.view.ClientDashboardView;
import javafx.application.Platform;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableView;
import javafx.scene.input.KeyCode;
import javafx.scene.robot.Robot;
import javafx.stage.Stage;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/** Verifica i nuovi tipi nelle viste reali, riusando fixture e gesti JavaFX esistenti. */
public class RequestSemanticsUiCheck extends SaleOutcomeUiCheck {
    public static void main(String[] args) throws Exception {
        if (args[0].equals("seed")) {
            AgreementUiCheck.seed();
            try (var conn = ctx.getDatabaseManager().getConnection(); var sql = conn.createStatement()) {
                sql.executeUpdate("UPDATE request SET client = 1 WHERE type = 'PURCHASE'");
            }
            return;
        }
        out = args[1] + "/";
        TestDatabase.fromEnvironment();
        ctx = new AppContext(new DatabaseManager(System.getenv("DRIVEHUB_TEST_DB_URL"),
                System.getenv("DRIVEHUB_TEST_DB_USER"), System.getenv("DRIVEHUB_TEST_DB_PASSWORD")));
        var ready = new CountDownLatch(1);
        Platform.startup(ready::countDown);
        check(ready.await(10, TimeUnit.SECONDS), "JavaFX avviato");
        Platform.setImplicitExit(false);
        try {
            fx(() -> { stage = new Stage(); robot = new Robot(); return null; });
            var salesman = (Salesman) ctx.getAuthService().login("paolo.costa@drivehub.it", "password");
            show(fx(() -> seller(salesman)));
            TableView<Request> pending = fx(() -> requests(stage.getScene().getRoot()));
            fx(() -> {
                check(pending.getItems().size() == 3, "due vendite e un acquisto della concessionaria");
                for (Request request : pending.getItems()) {
                    boolean sale = request.getType() == RequestType.SALE;
                    check(cell(pending, request, "Tipologia").equals(sale
                            ? "VENDITA AL CLIENTE" : "ACQUISTO DAL CLIENTE"), "etichetta " + request.getType());
                    check(request.getDeposit().signum() >= 0, "acconto/stima non negativo");
                }
                return null;
            });
            amounts(pending);
            focusTable(pending);
            screenshot("seller-request-types");

            var client = (Client) ctx.getAuthService().login("mario.rossi@email.it", "password");
            show(fx(() -> new ClientDashboardView(client, ctx.getAuthService(), ctx.getPurchaseService(),
                    ctx.getSaleToDealershipService(), ctx.getRentalService(), ctx.getTestDriveService(),
                    ctx.getCatalogService(), ctx.getOperationQueryService(), ctx.getPriceCalculatorService(), () -> {})));
            TabPane tabs = fx(() -> (TabPane) ((ClientDashboardView) stage.getScene().getRoot()).getCenter());
            fx(() -> { tabs.requestFocus(); return null; });
            key(KeyCode.RIGHT); key(KeyCode.RIGHT); key(KeyCode.RIGHT);
            check(fx(() -> tabs.getSelectionModel().getSelectedIndex()) == 3, "operazioni raggiunte da tastiera");
            TableView<Request> mine = fx(() -> requests(stage.getScene().getRoot()));
            fx(() -> {
                check(mine.getItems().size() == 2, "cliente vede entrambi i suoi acquisti da saldare");
                check(mine.getItems().stream().allMatch(r -> r.getType() == RequestType.SALE),
                        "la cessione PURCHASE non compare tra gli acquisti del cliente");
                return null;
            });
            amounts(mine);
            focusTable(mine);
            screenshot("client-request-filter");
            System.out.println("UI CHECK SUCCESS");
        } finally {
            fx(() -> { if (stage != null) stage.close(); return null; });
            Platform.exit();
        }
    }
}
