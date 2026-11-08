package it.unifi.drivehub;

import it.unifi.drivehub.dao.DatabaseManager;
import it.unifi.drivehub.domain.operation.Sale;
import it.unifi.drivehub.domain.user.Manager;
import it.unifi.drivehub.presentation.view.ManagerDashboardView;
import it.unifi.drivehub.presentation.view.UiUtils;
import javafx.application.Platform;
import javafx.scene.control.Label;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableView;
import javafx.scene.input.KeyCode;
import javafx.scene.robot.Robot;
import javafx.stage.Stage;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/** Registro manager riletto da PostgreSQL, con tipi espliciti e navigazione da tastiera. */
public class SaleRegisterUiCheck extends AgreementUiCheck {
    public static void main(String[] args) throws Exception {
        if (args[0].equals("seed")) {
            var db = TestDatabase.fromEnvironment().reset();
            try (var conn = db.getConnection(); var sql = conn.createStatement()) {
                sql.execute("""
                        DELETE FROM sale;
                        INSERT INTO sale (client,amount,date,type) VALUES
                        (1,20000,CURRENT_TIMESTAMP,'SALE'),
                        (1,16000,CURRENT_TIMESTAMP,'PURCHASE'),
                        (1,800,CURRENT_TIMESTAMP,'PENALTY'),
                        (2,0,CURRENT_TIMESTAMP,'SALE'),
                        (2,0,CURRENT_TIMESTAMP,'PURCHASE'),
                        (2,0,CURRENT_TIMESTAMP,'PENALTY');
                        """);
            }
            System.out.println("SEED SUCCESS: sei operazioni, tre tipi anche a zero");
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
            var user = (Manager) ctx.getAuthService().login("vittorio.manager@drivehub.it", "password");
            show(fx(() -> new ManagerDashboardView(user, ctx.getAuthService(), ctx.getCatalogService(),
                    ctx.getOperationQueryService(), () -> {})));
            var tabs = fx(() -> (TabPane) ((ManagerDashboardView) stage.getScene().getRoot()).getCenter());
            fx(() -> { tabs.requestFocus(); return null; });
            for (int i = 0; i < tabs.getTabs().size() - 1; i++) key(KeyCode.RIGHT);
            check(fx(() -> tabs.getSelectionModel().getSelectedItem().getText()).equals("Registro economico"),
                    "registro raggiunto con frecce");
            @SuppressWarnings("unchecked")
            var table = fx(() -> (TableView<Sale>) tabs.getSelectionModel().getSelectedItem().getContent().lookup(".table-view"));
            fx(() -> {
                check(stage.getScene().getRoot().lookupAll(".label").stream().anyMatch(n -> n instanceof Label label
                        && label.getText().equals("Registro della concessionaria: vendite e penali in entrata, acquisti in uscita")),
                        "titolo descrive il verso economico attraverso il tipo");
                check(table.getItems().size() == 6, "sei operazioni rilette dopo riavvio");
                for (Sale sale : table.getItems()) {
                    String expected = switch (sale.getType()) {
                        case SALE -> "VENDITA (Entrata)";
                        case PURCHASE -> "ACQUISTO (Uscita)";
                        case PENALTY -> "PENALE (Entrata)";
                    };
                    check(table.getColumns().get(1).getCellData(sale).equals(expected), "tipo esplicito " + sale.getType());
                    check(sale.getAmount().signum() >= 0, "importo non negativo " + sale.getAmount());
                    check(table.getColumns().get(6).getCellData(sale).equals(UiUtils.formatCurrency(sale.getAmount())),
                            "importo formattato " + sale.getAmount());
                }
                return null;
            });
            focusTable(table);
            key(KeyCode.END);
            check(fx(() -> table.getSelectionModel().getSelectedIndex()) == 5, "registro navigabile fino all'ultima riga");
            screenshot("manager-ledger");
            System.out.println("UI CHECK SUCCESS");
        } finally {
            fx(() -> { stage.close(); return null; });
            Platform.exit();
        }
    }
}
