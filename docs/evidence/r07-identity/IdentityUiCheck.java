package it.unifi.drivehub;

import it.unifi.drivehub.dao.DatabaseManager;
import it.unifi.drivehub.domain.user.User;
import it.unifi.drivehub.presentation.view.AccountManagementDialog;
import javafx.application.Platform;
import javafx.animation.AnimationTimer;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.VBox;
import javafx.scene.robot.Robot;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/** Costruttore pubblico del dialogo e Robot05: nessuna automazione interna del servizio. */
public class IdentityUiCheck extends PriceUiCheck {
    static Stage owner;
    static final AtomicInteger deleted = new AtomicInteger();

    static void seed() throws Exception {
        ctx = new AppContext(TestDatabase.fromEnvironment().reset());
        try (var conn = ctx.getDatabaseManager().getConnection(); var sql = conn.createStatement()) {
            sql.execute("""
                    DELETE FROM sale; DELETE FROM rent; DELETE FROM test_drive; DELETE FROM request;
                    INSERT INTO request(vehicle,client,deposit,agreed_total,type)
                    VALUES (1,1,100,500,'PURCHASE');
                    INSERT INTO sale(vehicle,salesman,client,amount,date,type)
                    VALUES (2,16,2,20000,CURRENT_TIMESTAMP,'PURCHASE');
                    """);
        }
        System.out.println("SEED SUCCESS: richiesta cliente, sola vendita conclusa venditore, identità senza vincoli");
    }

    static void open(User user) throws Exception {
        Platform.runLater(() -> AccountManagementDialog.show(owner, user, ctx.getAuthService(), deleted::incrementAndGet, null));
        for (int i = 0; i < 100; i++) {
            Stage found = fx(() -> Window.getWindows().stream().filter(w -> w.isShowing() && w instanceof Stage s
                            && s.getTitle() != null && s.getTitle().startsWith("Gestione Identità"))
                    .map(w -> (Stage) w).findFirst().orElse(null));
            if (found != null) { stage = found; break; }
            Thread.sleep(50); // Solo attesa rendering Robot, mai sincronizzazione delle gare PostgreSQL.
        }
        check(stage != owner && fx(() -> stage.isShowing()), "dialogo pubblico aperto");
        TabPane tabs = fx(() -> (TabPane) stage.getScene().getRoot());
        fx(() -> { tabs.requestFocus(); return null; });
        key(KeyCode.HOME);
        for (int i = 1; i < fx(() -> tabs.getTabs().size()); i++) key(KeyCode.RIGHT);
        check(fx(() -> tabs.getSelectionModel().getSelectedItem().getText().equals("Elimina Identità")),
                "scheda eliminazione raggiunta con frecce");
        check(fx(() -> tabs.getSelectionModel().getSelectedItem().getContent().lookupAll(".label").stream()
                .anyMatch(n -> ((Label) n).getText().contains("conto dimostrativo"))), "identità distinta dal conto dimostrativo");
    }

    static void password(String value) throws Exception {
        reach(n -> n instanceof PasswordField);
        fx(() -> { robot.keyPress(KeyCode.CONTROL); robot.keyType(KeyCode.A); robot.keyRelease(KeyCode.CONTROL); return null; });
        for (char c : value.toCharArray()) key(KeyCode.valueOf(String.valueOf(c).toUpperCase(java.util.Locale.ROOT)));
        reach(n -> n instanceof Button b && b.getText().equals("Elimina Definitivamente Identità"));
        key(KeyCode.SPACE);
    }

    static DialogPane alert() throws Exception {
        for (int i = 0; i < 100; i++) {
            DialogPane pane = fx(() -> Window.getWindows().stream()
                    .filter(w -> w.isShowing() && w.getScene().getRoot() instanceof DialogPane)
                    .map(w -> (DialogPane) w.getScene().getRoot()).findFirst().orElse(null));
            if (pane != null) return pane;
            Thread.sleep(50);
        }
        throw new AssertionError("Alert assente");
    }

    static String message() throws Exception {
        var pane = alert();
        return fx(() -> pane.getHeaderText() + " " + pane.getContentText());
    }

    static void accept() throws Exception {
        var pane = alert();
        fx(() -> { pane.lookupButton(ButtonType.OK).requestFocus(); return null; });
        key(KeyCode.ENTER);
    }

    static void capture(String name) throws Exception {
        Stage original = stage;
        var pane = alert();
        var painted = new CountDownLatch(1);
        fx(() -> {
            new AnimationTimer() {
                private int frames;
                @Override public void handle(long now) {
                    if (++frames == 3) { stop(); painted.countDown(); }
                }
            }.start();
            return null;
        });
        check(painted.await(3, TimeUnit.SECONDS), "alert renderizzato prima della cattura");
        stage = fx(() -> (Stage) pane.getScene().getWindow());
        screenshot(name);
        stage = original;
    }

    static void closeIdentity() throws Exception {
        fx(() -> { stage.close(); stage = owner; return null; });
    }

    public static void main(String[] args) throws Exception {
        if (args[0].equals("seed")) { seed(); return; }
        out = args[1] + "/";
        TestDatabase.fromEnvironment();
        ctx = new AppContext(new DatabaseManager(System.getenv("DRIVEHUB_TEST_DB_URL"),
                System.getenv("DRIVEHUB_TEST_DB_USER"), System.getenv("DRIVEHUB_TEST_DB_PASSWORD")));
        Thread.setDefaultUncaughtExceptionHandler((thread, error) -> { error.printStackTrace(); System.exit(2); });
        var ready = new CountDownLatch(1);
        Platform.startup(ready::countDown);
        check(ready.await(10, TimeUnit.SECONDS), "JavaFX avviato nella JVM distinta dal seed");
        Platform.setImplicitExit(false);
        try {
            fx(() -> {
                owner = new Stage(); owner.setScene(new Scene(new VBox(new Label("DriveHub — verifica identità")), 600, 400));
                owner.show(); stage = owner; robot = new Robot(); return null;
            });
            open(ctx.getAuthService().login("mario.rossi@email.it", "password"));
            screenshot("client-rules"); password("password");
            check(message().toLowerCase(java.util.Locale.ROOT).contains("richieste") && message().contains("conto dimostrativo"), "conferma esplicita prima della cancellazione");
            capture("confirmation"); key(KeyCode.ESCAPE);
            check(deleted.get() == 0 && ctx.getAuthService().login("mario.rossi@email.it", "password") != null,
                    "annullamento da tastiera conserva identità e sessione");
            key(KeyCode.SPACE); accept();
            check(message().contains("richieste o operazioni storiche associate"), "richiesta cliente impedisce eliminazione con motivo leggibile");
            capture("client-request-rejected"); accept();
            check(deleted.get() == 0 && fx(() -> stage.isShowing()), "rifiuto conserva dialogo e sessione"); closeIdentity();

            open(ctx.getAuthService().login("paolo.costa@drivehub.it", "password"));
            password("password"); accept();
            check(message().contains("richieste o operazioni storiche associate"), "sola vendita conclusa impedisce eliminazione venditore");
            capture("salesman-history-rejected"); accept(); closeIdentity();
            check(ctx.getOperationQueryService().getAllSales().getFirst().getSalesmanId() == 16L,
                    "attribuzione venditore persistita");

            open(ctx.getAuthService().login("giovanni.bianchi@email.it", "password"));
            password("errata"); accept();
            check(message().contains("Password attuale errata"), "password errata impedisce cancellazione");
            capture("wrong-password"); accept(); closeIdentity();

            open(ctx.getAuthService().login("giovanni.bianchi@email.it", "password"));
            password("password"); accept();
            check(message().contains("cancellata con successo"), "identità senza vincoli eliminata con successo dopo commit");
            capture("client-deleted"); accept();
            check(deleted.get() == 1, "callback di uscita invocata soltanto dopo successo");
            try { ctx.getAuthService().login("giovanni.bianchi@email.it", "password"); throw new AssertionError("Identità ancora presente"); }
            catch (IllegalArgumentException expected) { System.out.println("PASS login eliminato"); }
            try { ctx.getAuthService().getClientBankAccount(3L); throw new AssertionError("Conto dimostrativo ancora presente"); }
            catch (IllegalStateException expected) { System.out.println("PASS conto dimostrativo rimosso"); }
            stage = owner;

            open(ctx.getAuthService().login("laura.gatti@drivehub.it", "password"));
            password("password"); accept(); check(message().contains("cancellata con successo"), "venditore senza vincoli eliminabile"); accept();
            stage = owner;
            open(ctx.getAuthService().login("vittorio.manager@drivehub.it", "password"));
            password("password"); accept(); check(message().contains("cancellata con successo"), "manager senza vincoli eliminabile"); accept();
            check(deleted.get() == 3, "tre cancellazioni effettive e nessun callback sui rifiuti");
            System.out.println("UI CHECK SUCCESS");
        } finally {
            fx(() -> { for (var window : java.util.List.copyOf(Window.getWindows())) window.hide(); return null; });
            Platform.exit();
        }
    }
}
