package it.unifi.drivehub;

import it.unifi.drivehub.domain.user.*;
import it.unifi.drivehub.domain.vehicle.*;
import it.unifi.drivehub.presentation.view.*;
import javafx.application.Platform;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.robot.Robot;
import javafx.stage.*;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.math.BigDecimal;
import java.time.Year;
import java.util.concurrent.*;
import java.util.function.Predicate;

/** Controllo eseguibile delle viste pubbliche; solo PostgreSQL dedicato e display Xvfb. */
public class PriceUiCheck {
    static Stage stage;
    static Robot robot;
    static AppContext ctx;
    static Long vehicleId;
    static String out = "/tmp/drivehub-ticket05-ui/";
    static <T> T fx(Callable<T> work) throws Exception {
        FutureTask<T> task = new FutureTask<>(work);
        Platform.runLater(task);
        return task.get(60, TimeUnit.SECONDS);
    }
    static void check(boolean value, String message) {
        if (!value) throw new AssertionError(message);
        System.out.println("PASS " + message);
    }
    static void key(KeyCode code) throws Exception {
        fx(() -> { robot.keyType(code); return null; });
        Thread.sleep(140);
    }
    static void reach(Predicate<Node> target) throws Exception {
        for (int i = 0; i < 50; i++) {
            if (fx(() -> target.test(stage.getScene().getFocusOwner()))) return;
            key(KeyCode.TAB);
        }
        throw new AssertionError("Controllo irraggiungibile con Tab: " + fx(() -> stage.getScene().getFocusOwner()));
    }
    static void closeAlert() throws Exception {
        for (int i = 0; i < 100; i++) {
            boolean showing = fx(() -> Window.getWindows().stream().anyMatch(w -> w != stage && w.isShowing()));
            if (showing) { key(KeyCode.ENTER); Thread.sleep(150); return; }
            Thread.sleep(100);
        }
        throw new AssertionError("Dialogo atteso assente");
    }
    static void screenshot(String name) throws Exception {
        fx(() -> {
            var img = robot.getScreenCapture(null, stage.getX(), stage.getY(), stage.getWidth(), stage.getHeight());
            BufferedImage png = new BufferedImage((int) img.getWidth(), (int) img.getHeight(), BufferedImage.TYPE_INT_ARGB);
            for (int y = 0; y < png.getHeight(); y++) for (int x = 0; x < png.getWidth(); x++)
                png.setRGB(x, y, img.getPixelReader().getArgb(x, y));
            ImageIO.write(png, "png", new File(out + name + ".png"));
            return null;
        });
    }
    static void show(Parent root) throws Exception {
        fx(() -> {
            Scene scene = new Scene(root, 1280, 760);
            scene.getStylesheets().add(PriceUiCheck.class.getResource("/css/style.css").toExternalForm());
            stage.setScene(scene); stage.show(); stage.toFront(); stage.requestFocus();
            return null;
        });
        Thread.sleep(500);
    }
    static Vehicle reload() { return ctx.getCatalogService().getVehicleById(vehicleId).orElseThrow(); }
    public static void main(String[] args) throws Exception {
        Thread.setDefaultUncaughtExceptionHandler((thread, error) -> { error.printStackTrace(); System.exit(2); });
        try {
            ctx = new AppContext(TestDatabase.fromEnvironment().reset());
            int year = Year.now().getValue();
            Model model = ctx.getCatalogService().addModel("Fiat", "UI Ticket05", new BigDecimal("20000"), year);
            ctx.getCatalogService().setModelDiscount(model.getId(), 10);
            vehicleId = ctx.getCatalogService().addVehicle(model.getId(), "T0500000000000099", null, 0, null, year, VehicleState.SALE).getId();
            ctx.getCatalogService().addVehicle(model.getId(), "T0500000000000098", null, 0, new BigDecimal("10000"), year, VehicleState.RENT);
            // Fixture mirata: il caricamento asincrono e le query nelle celle restano al ticket 14.
            try (var conn = ctx.getDatabaseManager().getConnection(); var st = conn.createStatement()) {
                st.execute("DELETE FROM sale; DELETE FROM rent; DELETE FROM test_drive; DELETE FROM request; "
                        + "DELETE FROM vehicle_discount; DELETE FROM vehicle WHERE model <> " + model.getId());
            }
            Manager manager = (Manager) ctx.getAuthService().login("vittorio.manager@drivehub.it", "password");
            CountDownLatch ready = new CountDownLatch(1);
            Platform.startup(ready::countDown); check(ready.await(10, TimeUnit.SECONDS), "JavaFX avviato");
            fx(() -> { Platform.setImplicitExit(false); stage = new Stage(); robot = new Robot(); return null; });
            show(fx(() -> new ManagerDashboardView(manager, ctx.getAuthService(), ctx.getCatalogService(), ctx.getOperationQueryService(), () -> {})));
            fx(() -> { ((TabPane) ((ManagerDashboardView) stage.getScene().getRoot()).getCenter()).getSelectionModel().select(2); return null; });
            Thread.sleep(300);
            TableView<?> table = fx(() -> (TableView<?>) ((TabPane) ((ManagerDashboardView) stage.getScene().getRoot()).getCenter())
                    .getSelectionModel().getSelectedItem().getContent().lookup(".table-view"));
            fx(() -> { table.requestFocus(); return null; });
            screenshot("manager-initial");
            fx(() -> { var b = table.localToScreen(table.getBoundsInLocal());
                robot.mouseMove(b.getMinX() + 30, b.getMinY() + 38);
                robot.mouseClick(javafx.scene.input.MouseButton.PRIMARY); return null; });
            Thread.sleep(200);
            System.out.println("Stage focused=" + fx(() -> stage.isFocused()) + " focus=" + fx(() -> stage.getScene().getFocusOwner()));
            System.out.println("Selected after click=" + fx(() -> table.getSelectionModel().getSelectedItem()));
            key(KeyCode.DOWN);
            System.out.println("Selected after down=" + fx(() -> table.getSelectionModel().getSelectedItem()));
            key(KeyCode.UP);
            System.out.println("Selected after up=" + fx(() -> table.getSelectionModel().getSelectedItem()));
            check(fx(() -> ((Vehicle) table.getSelectionModel().getSelectedItem()).getId().equals(vehicleId)), "selezione veicolo da tastiera");
            reach(n -> n instanceof CheckBox);
            check(fx(() -> ((CheckBox) stage.getScene().getFocusOwner()).isSelected()), "modalità automatica visibile");
            screenshot("manager-automatic");
            key(KeyCode.SPACE);
            reach(n -> n instanceof TextField f && "Base manuale (€)".equals(f.getPromptText()));
            check(fx(() -> !stage.getScene().getFocusOwner().isDisabled()), "base manuale raggiungibile con Tab");
            fx(() -> { robot.keyPress(KeyCode.CONTROL); robot.keyType(KeyCode.A); robot.keyRelease(KeyCode.CONTROL); return null; });
            for (KeyCode digit : new KeyCode[]{KeyCode.DIGIT1, KeyCode.DIGIT0, KeyCode.DIGIT0, KeyCode.DIGIT0, KeyCode.DIGIT0}) key(digit);
            reach(n -> n instanceof Button b && b.getText().equals("Applica base alla selezione"));
            key(KeyCode.SPACE); closeAlert();
            check(reload().getPriceMode() == PriceMode.MANUAL, "manuale salvato dalla UI");
            check(ctx.getCatalogService().getEffectiveVehiclePrice(reload()).equals(new BigDecimal("9000.00")), "UI produce 9000 da base 10000");
            screenshot("manager-manual");
            fx(() -> { table.requestFocus(); return null; }); key(KeyCode.HOME); key(KeyCode.DOWN); key(KeyCode.UP);
            reach(n -> n instanceof CheckBox); key(KeyCode.SPACE);
            reach(n -> n instanceof Button b && b.getText().equals("Applica base alla selezione"));
            key(KeyCode.SPACE); closeAlert();
            check(reload().getPriceMode() == PriceMode.AUTOMATIC, "ritorno al consigliato da tastiera persistito");
            ctx.getCatalogService().updateVehiclePricing(vehicleId, new BigDecimal("10000"));
            Client client = (Client) ctx.getAuthService().login("mario.rossi@email.it", "password");
            show(fx(() -> new ClientDashboardView(client, ctx.getAuthService(), ctx.getPurchaseService(), ctx.getSaleToDealershipService(),
                    ctx.getRentalService(), ctx.getTestDriveService(), ctx.getCatalogService(), ctx.getOperationQueryService(), ctx.getPriceCalculatorService(), () -> {})));
            screenshot("client-sale");
            fx(() -> { ((TabPane) ((ClientDashboardView) stage.getScene().getRoot()).getCenter()).requestFocus(); return null; });
            key(KeyCode.RIGHT);
            check(fx(() -> ((TabPane) ((ClientDashboardView) stage.getScene().getRoot()).getCenter()).getSelectionModel().getSelectedIndex()) == 1,
                    "catalogo noleggio raggiunto con freccia destra");
            screenshot("client-rent");
            System.out.println("UI CHECK SUCCESS");
        } finally {
            Platform.exit();
        }
    }
}
