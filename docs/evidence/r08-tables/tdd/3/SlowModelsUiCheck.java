package it.unifi.drivehub;

import it.unifi.drivehub.domain.user.Client;
import it.unifi.drivehub.presentation.view.ClientDashboardView;
import javafx.application.Platform;
import javafx.scene.control.*;
import java.util.concurrent.*;

/** Lock PostgreSQL osservato; nessuno sleep per simulare un caricamento lento. */
public class SlowModelsUiCheck extends ReactiveTablesUiCheck {
    static void awaitBlocked(int blocker) throws Exception {
        long deadline=System.nanoTime()+TimeUnit.SECONDS.toNanos(10);
        try(var observer=ctx.getDatabaseManager().getConnection();var sql=observer.prepareStatement("SELECT pid FROM pg_stat_activity WHERE ?=ANY(pg_blocking_pids(pid)) AND query LIKE '%brand =%'")) {
            sql.setInt(1,blocker);sql.setQueryTimeout(2);
            while(System.nanoTime()<deadline)try(var rs=sql.executeQuery()){if(rs.next()){System.out.println("OBSERVED loader="+rs.getInt(1)+" blocker="+blocker);return;}}
        }
        throw new AssertionError("Lettura modelli non osservata");
    }
    public static void main(String[] args) throws Exception {
        Thread.setDefaultUncaughtExceptionHandler((t,e) -> { e.printStackTrace(); System.exit(2); });
        try {
            ctx = new AppContext(TestDatabase.fromEnvironment().reset());
            try (var conn = ctx.getDatabaseManager().getConnection(); var sql = conn.createStatement()) {
                sql.execute("DELETE FROM request");
            }
            Client client = (Client)ctx.getAuthService().login("mario.rossi@email.it", "password");
            CountDownLatch ready = new CountDownLatch(1);Platform.startup(ready::countDown);
            check(ready.await(10,TimeUnit.SECONDS), "JavaFX avviato");
            fx(() -> { Platform.setImplicitExit(false); stage = new javafx.stage.Stage();robot = new javafx.scene.robot.Robot();return null; });
            var view = fx(() -> new ClientDashboardView(client,ctx.getAuthService(),ctx.getPurchaseService(),ctx.getSaleToDealershipService(),ctx.getRentalService(),ctx.getTestDriveService(),ctx.getCatalogService(),ctx.getOperationQueryService(),ctx.getPriceCalculatorService(), () -> {}));
            show(view);
            var tabs = fx(() -> (TabPane)view.getCenter());
            var combo = fx(() -> (ComboBox<?>)tabs.getTabs().get(2).getContent().lookupAll(".combo-box").stream().filter(n -> ((ComboBox<?>)n).getItems().stream().anyMatch(i -> i instanceof it.unifi.drivehub.domain.vehicle.Brand)).findFirst().orElseThrow());
            try (var gate = ctx.getDatabaseManager().getConnection()) {
                gate.setAutoCommit(false);
                try (var sql = gate.createStatement()) { sql.execute("LOCK TABLE model IN ACCESS EXCLUSIVE MODE"); }
                FutureTask<Void> select = new FutureTask<>(() -> { combo.getSelectionModel().selectFirst();return null; });
                Platform.runLater(select);
                try {
                    int gatePid;
                    try (var sql = gate.createStatement(); var rs = sql.executeQuery("SELECT pg_backend_pid()")) { rs.next();gatePid=rs.getInt(1); }
                    long deadline = System.nanoTime()+TimeUnit.SECONDS.toNanos(10);
                    boolean blocked=false;
                    try(var observer=ctx.getDatabaseManager().getConnection();var sql=observer.prepareStatement("SELECT pid FROM pg_stat_activity WHERE ?=ANY(pg_blocking_pids(pid)) AND query LIKE '%brand =%'") ) {
                        sql.setInt(1,gatePid);sql.setQueryTimeout(2);
                        while(System.nanoTime()<deadline && !blocked) try(var rs=sql.executeQuery()) { if(rs.next()) { blocked=true;System.out.println("OBSERVED model loader PID="+rs.getInt(1)+" blocker="+gatePid); } }
                    }
                    check(blocked,"lettura modelli realmente in attesa PostgreSQL");
                    select.get(2,TimeUnit.SECONDS);
                    check(true, "selezione modello torna mentre PostgreSQL è bloccato");
                    check(fx(() -> view.getScene().getWindow().isShowing()), "finestra reattiva durante caricamento lento");
                } finally { gate.rollback(); }
            }
            // Una richiesta vuota più recente deve superare anche il risultato di una lettura già avviata.
            ComboBox<?> models = fx(() -> (ComboBox<?>)tabs.getTabs().get(2).getContent().lookupAll(".combo-box").stream().filter(n -> n != combo).findFirst().orElseThrow());
            try(var gate=ctx.getDatabaseManager().getConnection()) {
                gate.setAutoCommit(false);
                try(var sql=gate.createStatement()) { sql.execute("LOCK TABLE model IN ACCESS EXCLUSIVE MODE"); }
                fx(() -> { combo.getSelectionModel().clearSelection();combo.getSelectionModel().selectFirst();return null; });
                int pid;
                try(var sql=gate.createStatement();var rs=sql.executeQuery("SELECT pg_backend_pid()")) {rs.next();pid=rs.getInt(1);}
                awaitBlocked(pid);
                fx(() -> {combo.getSelectionModel().clearSelection();return null;});
                gate.rollback();
                CompletableFuture<Void> done=new CompletableFuture<>();
                fx(() -> {if(!models.isDisabled())done.complete(null);else models.disabledProperty().addListener((o,a,b)->{if(!b)done.complete(null);});return null;});
                done.get(10,TimeUnit.SECONDS);
                check(fx(() -> models.getItems().isEmpty()),"risultato superato non ripopola modelli senza marca");
            }
            System.out.println("UI CHECK SUCCESS");
        } catch(Throwable error) { error.printStackTrace();System.exit(1); }
        Platform.exit();
    }
}
