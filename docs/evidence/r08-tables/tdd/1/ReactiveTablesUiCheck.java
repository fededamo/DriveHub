package it.unifi.drivehub;

import it.unifi.drivehub.domain.user.*;
import it.unifi.drivehub.presentation.view.*;
import javafx.application.Platform;
import javafx.animation.AnimationTimer;
import javafx.scene.control.*;
import java.sql.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/** Viste pubbliche e JDBC reale: misura connessioni/query durante le celle. */
public class ReactiveTablesUiCheck extends PriceUiCheck {
    static final AtomicInteger connections = new AtomicInteger();
    static final AtomicInteger queries = new AtomicInteger();

    static void probe() throws Exception {
        Driver delegate = new org.postgresql.Driver();
        for (Driver driver : Collections.list(DriverManager.getDrivers())) DriverManager.deregisterDriver(driver);
        DriverManager.registerDriver(new Driver() {
            public boolean acceptsURL(String url) throws SQLException { return delegate.acceptsURL(url); }
            public Connection connect(String url, Properties info) throws SQLException {
                if (!acceptsURL(url)) return null;
                connections.incrementAndGet();
                Connection connection = delegate.connect(url, info);
                return (Connection) java.lang.reflect.Proxy.newProxyInstance(getClass().getClassLoader(), new Class<?>[]{Connection.class}, (p,m,a) -> {
                    try {
                        Object value = m.invoke(connection, a);
                        if (value instanceof Statement statement) {
                            Class<?> type = value instanceof PreparedStatement ? PreparedStatement.class : Statement.class;
                            return java.lang.reflect.Proxy.newProxyInstance(getClass().getClassLoader(), new Class<?>[]{type}, (sp,sm,sa) -> {
                                if (sm.getName().startsWith("execute")) queries.incrementAndGet();
                                try { return sm.invoke(statement,sa); }
                                catch (java.lang.reflect.InvocationTargetException e) { throw e.getCause(); }
                            });
                        }
                        return value;
                    } catch (java.lang.reflect.InvocationTargetException e) { throw e.getCause(); }
                });
            }
            public DriverPropertyInfo[] getPropertyInfo(String url, Properties p) throws SQLException { return delegate.getPropertyInfo(url,p); }
            public int getMajorVersion() { return delegate.getMajorVersion(); }
            public int getMinorVersion() { return delegate.getMinorVersion(); }
            public boolean jdbcCompliant() { return delegate.jdbcCompliant(); }
            public java.util.logging.Logger getParentLogger() { return java.util.logging.Logger.getGlobal(); }
        });
    }

    static void frames() throws Exception {
        CountDownLatch rendered = new CountDownLatch(1);
        fx(() -> { new AnimationTimer() {
            int frames;
            public void handle(long now) { if (++frames == 3) { stop(); rendered.countDown(); } }
        }.start(); return null; });
        check(rendered.await(10, TimeUnit.SECONDS), "tre frame renderizzati");
    }

    static void loaded(TableView<?> table) throws Exception {
        CompletableFuture<Void> ready = new CompletableFuture<>();
        fx(() -> {
            if (!table.isDisabled()) ready.complete(null);
            else table.disabledProperty().addListener((obs,old,value) -> { if (!value) ready.complete(null); });
            return null;
        });
        ready.get(15, TimeUnit.SECONDS);
    }

    static void cells(TableView<?> table) throws Exception {
        loaded(table);
        int beforeConnections = connections.get(), beforeQueries = queries.get();
        fx(() -> {
            for (Object item : table.getItems()) for (TableColumn<?,?> column : table.getColumns()) cell(column,item);
            table.refresh(); table.scrollTo(table.getItems().size()-1);
            return null;
        });
        frames();
        check(connections.get() == beforeConnections && queries.get() == beforeQueries,
                "celle e scorrimento: zero connessioni/query, connessioni=" + (connections.get()-beforeConnections) + ", query=" + (queries.get()-beforeQueries));
    }
    @SuppressWarnings({"rawtypes", "unchecked"})
    static void cell(TableColumn column, Object item) { column.getCellObservableValue(item).getValue(); }

    public static void main(String[] args) throws Exception {
        Thread.setDefaultUncaughtExceptionHandler((t,e) -> { e.printStackTrace(); System.exit(2); });
        try {
            ctx = new AppContext(TestDatabase.fromEnvironment().reset());
            probe();
            Manager manager = (Manager) ctx.getAuthService().login("vittorio.manager@drivehub.it", "password");
            CountDownLatch ready = new CountDownLatch(1); Platform.startup(ready::countDown);
            check(ready.await(10,TimeUnit.SECONDS), "JavaFX avviato");
            fx(() -> { Platform.setImplicitExit(false); stage = new javafx.stage.Stage(); robot = new javafx.scene.robot.Robot(); return null; });
            var view = fx(() -> new ManagerDashboardView(manager,ctx.getAuthService(),ctx.getCatalogService(),ctx.getOperationQueryService(), () -> {}));
            TabPane tabs = fx(() -> (TabPane) view.getCenter());
            TableView<?> vehicles = fx(() -> (TableView<?>) tabs.getTabs().get(2).getContent().lookup(".table-view"));
            loaded(vehicles);
            show(view);
            fx(() -> { tabs.getSelectionModel().select(2); return null; }); frames();
            cells(vehicles);
            fx(() -> { tabs.getSelectionModel().select(3); return null; }); frames();
            for (var node : fx(() -> tabs.getTabs().get(3).getContent().lookupAll(".table-view"))) cells((TableView<?>)node);
            System.out.println("UI CHECK SUCCESS");
        } catch (Throwable error) { error.printStackTrace(); System.exit(1); }
        Platform.exit();
    }
}
