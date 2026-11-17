package it.unifi.drivehub;

import it.unifi.drivehub.presentation.view.*;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.robot.Robot;
import javafx.stage.Stage;
import javafx.stage.Window;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/** Acquisizione documentale: App reale, DB demo esterno; nessun reset o test di dominio. */
public final class CaptureTicket02 extends PriceUiCheck {
    static TabPane tabs() { return (TabPane) stage.getScene().getRoot().lookup(".tab-pane"); }
    static Button button(String text) {
        var local = tabs().getSelectionModel().getSelectedItem().getContent().lookupAll(".button").stream()
                .filter(n -> n instanceof Button b && text.equals(b.getText()))
                .map(n -> (Button)n).findFirst();
        if (local.isPresent()) return local.get();
        return stage.getScene().getRoot().lookupAll(".button").stream()
                .filter(n -> n instanceof Button b && b.isVisible() && text.equals(b.getText()))
                .map(n -> (Button) n).findFirst().orElseThrow();
    }
    static void waitFor(java.util.concurrent.Callable<Boolean> condition, String message) throws Exception {
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(30);
        while (System.nanoTime() < deadline) {
            if (fx(condition)) return;
            Thread.sleep(100);
        }
        throw new AssertionError(message);
    }
    static void ready() throws Exception {
        waitFor(() -> stage.getScene().getRoot().lookupAll(".table-view").stream()
                .noneMatch(Node::isDisabled), "Tabelle ancora disabilitate / errore di caricamento");
        Thread.sleep(500); // Attesa del frame, non verifica funzionale del DB.
    }
    static void tab(int index, String name) throws Exception {
        fx(() -> { tabs().getSelectionModel().select(index); return null; });
        ready(); screenshot(name); System.out.println("SCREENSHOT " + name);
    }
    static void login(String email, Class<?> expected) throws Exception {
        fx(() -> {
            tabs().getSelectionModel().select(0);
            for (Node n : tabs().getTabs().get(0).getContent().lookupAll(".text-field")) {
                TextField f = (TextField)n;
                f.setText(f instanceof PasswordField ? "password" : email);
            }
            return null;
        });
        Platform.runLater(() -> button("Accedi al Sistema").fire());
        waitFor(() -> expected.isInstance(stage.getScene().getRoot()), "Dashboard attesa assente");
        fx(() -> { stage.setWidth(1500); stage.setHeight(930); stage.centerOnScreen(); return null; });
        ready();
        check(fx(() -> stage.getScene().getRoot().lookupAll(".table-view").stream()
                .anyMatch(n -> !((TableView<?>)n).getItems().isEmpty())), "Dati demo caricati in " + expected.getSimpleName());
        System.out.println("LOGIN " + expected.getSimpleName());
    }
    static void logout() throws Exception {
        fx(() -> { button("Disconnetti").fire(); return null; });
        waitFor(() -> stage.getScene().getRoot() instanceof LoginView, "Accesso non ripristinato");
    }
    static void dialog(String action, String title, String name) throws Exception {
        Platform.runLater(() -> button(action).fire());
        waitFor(() -> Window.getWindows().stream().anyMatch(w -> w != stage && w.isShowing()), "Dialogo assente: " + title);
        waitFor(() -> Window.getWindows().stream().anyMatch(w -> w instanceof Stage s && s.isShowing()
                && s.getTitle().startsWith(title)), "Dialogo corrente atteso " + title);
        Thread.sleep(400);
        Stage owner = stage;
        try {
            stage = fx(() -> (Stage)Window.getWindows().stream()
                    .filter(w -> w instanceof Stage s && s != owner && s.isShowing()
                            && s.getTitle().startsWith(title)).findFirst().orElseThrow());
            screenshot(name); // Cattura del dialogo reale, senza ritagli o ricostruzioni successive.
        } finally { stage = owner; }
        System.out.println("SCREENSHOT " + name);
        fx(() -> { for (Window w : java.util.List.copyOf(Window.getWindows())) if (w != stage) w.hide(); return null; });
        Thread.sleep(200);
    }
    static void selectFirstTableRow() throws Exception {
        fx(() -> {
            TableView<?> table = (TableView<?>)tabs().getSelectionModel().getSelectedItem().getContent().lookup(".table-view");
            check(!table.getItems().isEmpty(), "Catalogo non vuoto prima del dialogo");
            table.getSelectionModel().select(0); return null;
        });
    }
    public static void main(String[] args) throws Exception {
        Thread.setDefaultUncaughtExceptionHandler((t, e) -> { e.printStackTrace(); System.exit(2); });
        try {
            check(System.getenv("DRIVEHUB_DB_URL").equals("jdbc:postgresql://127.0.0.1:55442/drivehub_ticket02_demo"), "Destinazione demo dedicata");
            out = args[0] + "/";
            CountDownLatch started = new CountDownLatch(1);
            Platform.startup(started::countDown);
            check(started.await(10, TimeUnit.SECONDS), "JavaFX avviato");
            fx(() -> { stage = new Stage(); robot = new Robot(); new App().start(stage); return null; });
            Thread.sleep(700);
            check(fx(() -> stage.getScene().getRoot() instanceof LoginView), "App.start mostra LoginView");
            tab(0, "01-accesso"); tab(1, "02-registrazione");
            login("mario.rossi@email.it", ClientDashboardView.class);
            tab(0, "03-cliente-catalogo");
            selectFirstTableRow(); dialog("Prenota Test Drive", "Prenotazione Test Drive", "04-cliente-dialogo-test-drive");
            tab(1, "05-cliente-noleggio");
            selectFirstTableRow(); dialog("Prenota Noleggio", "Prenotazione Noleggio", "06-cliente-dialogo-noleggio");
            tab(2, "07-cliente-cessione"); tab(3, "08-cliente-operazioni");
            dialog("Gestione Account", "Gestione Identità", "09-cliente-identita");
            logout(); login("paolo.costa@drivehub.it", SalesmanDashboardView.class);
            tab(0, "10-venditore-richieste"); tab(1, "11-venditore-inventario");
            logout(); login("vittorio.manager@drivehub.it", ManagerDashboardView.class);
            tab(0, "12-manager-marchi"); tab(1, "13-manager-modelli");
            tab(2, "14-manager-veicoli"); tab(3, "15-manager-sconti"); tab(4, "16-manager-registro");
            logout(); System.out.println("ACQUISIZIONE COMPLETATA: 16 screenshot, tre login e ritorni all'accesso.");
        } catch (Throwable failure) {
            failure.printStackTrace();
            System.exit(1);
        } finally { Platform.exit(); }
    }
}
