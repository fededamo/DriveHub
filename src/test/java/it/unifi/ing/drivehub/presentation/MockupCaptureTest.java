package it.unifi.ing.drivehub.presentation;

import it.unifi.ing.drivehub.DriveHubApplication;
import it.unifi.ing.drivehub.domain.users.Role;
import it.unifi.ing.drivehub.presentation.core.ApplicationContext;
import it.unifi.ing.drivehub.presentation.core.UiModels;
import it.unifi.ing.drivehub.presentation.navigation.NavigationManager;
import it.unifi.ing.drivehub.presentation.navigation.Route;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.TabPane;
import javafx.scene.image.PixelFormat;
import javafx.scene.image.WritableImage;
import javafx.stage.Stage;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/** Acquisisce le viste FXML dell'applicazione avviata con dati demo in memoria. */
@Tag("gui")
class MockupCaptureTest {
    private static final Path OUTPUT = Path.of("Relazione_SWE", "images", "mockups");

    @Test
    void captureCurrentApplication() throws Exception {
        System.setProperty("drivehub.db.url", "jdbc:postgresql://127.0.0.1:1/drivehub");
        CompletableFuture<Void> started = new CompletableFuture<>();
        Platform.startup(() -> {
            Platform.setImplicitExit(false);
            started.complete(null);
        });
        started.get(20, TimeUnit.SECONDS);

        Files.createDirectories(OUTPUT);
        CaptureContext capture = onFx(() -> {
            Stage stage = new Stage();
            DriveHubApplication app = new DriveHubApplication();
            app.start(stage);
            Field contextField = DriveHubApplication.class.getDeclaredField("context");
            contextField.setAccessible(true);
            ApplicationContext context = (ApplicationContext) contextField.get(app);
            if (context == null) throw new IllegalStateException("Avvio demo non riuscito");
            return new CaptureContext(stage, context);
        });
        Stage stage = capture.stage();
        ApplicationContext context = capture.context();
        captureRoute(stage, Route.WELCOME, "00-benvenuto.png");
        captureRoute(stage, Route.LOGIN, "01-accesso.png");
        captureRoute(stage, Route.REGISTER, "02-registrazione.png");

        // demo.sql contains realistic data for all three roles.
        UiModels.Session customer = new UiModels.Session(1L, "Mario Rossi", "mario.rossi@email.it", Role.CLIENT);
        UiModels.Session salesman = new UiModels.Session(1L, "Paolo Costa", "paolo.costa@drivehub.it", Role.SALESMAN);
        UiModels.Session manager = new UiModels.Session(1L, "Vittorio Brambilla", "vittorio.manager@drivehub.it", Role.MANAGER);

        onFx(() -> { context.session().open(customer); NavigationManager.get().showHomeForCurrentRole(); resize(stage); return null; });
        captureTabs(stage, "customer", List.of("catalogo", "acquisti", "noleggi-test-drive",
                "cessione", "conto", "profilo"));
        onFx(() -> { context.session().open(salesman); NavigationManager.get().showHomeForCurrentRole(); resize(stage); return null; });
        captureTabs(stage, "salesman", List.of("proposte", "richieste", "stato-veicoli",
                "panoramica", "profilo"));
        onFx(() -> { context.session().open(manager); NavigationManager.get().showHomeForCurrentRole(); resize(stage); return null; });
        captureTabs(stage, "manager", List.of("parco-veicoli", "marche-modelli", "vendite", "profilo"));

        onFx(() -> { context.close(); stage.close(); return null; });
    }

    private static void captureRoute(Stage stage, Route route, String name) throws Exception {
        onFx(() -> { NavigationManager.get().show(route); resize(stage); return null; });
        pauseForLayout();
        onFx(() -> { save(stage, name); return null; });
    }

    private static void resize(Stage stage) {
        stage.setWidth(1450);
        stage.setHeight(930);
    }

    private static void captureTabs(Stage stage, String role, List<String> names) throws Exception {
        int count = onFx(() -> ((TabPane) stage.getScene().getRoot().lookup(".tab-pane")).getTabs().size());
        if (count != names.size()) throw new IllegalStateException("Numero schede inatteso per " + role);
        for (int i = 0; i < names.size(); i++) {
            final int index = i;
            onFx(() -> { ((TabPane) stage.getScene().getRoot().lookup(".tab-pane"))
                    .getSelectionModel().select(index); return null; });
            pauseForLayout();
            String name = role + "-" + names.get(i) + ".png";
            onFx(() -> { save(stage, name); return null; });
        }
    }

    private static void pauseForLayout() throws InterruptedException {
        Thread.sleep(180);
    }

    private static <T> T onFx(Callable<T> action) throws Exception {
        CompletableFuture<T> result = new CompletableFuture<>();
        Platform.runLater(() -> {
            try { result.complete(action.call()); }
            catch (Throwable failure) { result.completeExceptionally(failure); }
        });
        return result.get(40, TimeUnit.SECONDS);
    }

    private record CaptureContext(Stage stage, ApplicationContext context) {}

    private static void save(Stage stage, String fileName) throws Exception {
        Scene scene = stage.getScene();
        scene.getRoot().applyCss();
        scene.getRoot().layout();
        WritableImage image = scene.snapshot(null);
        int width = (int) image.getWidth();
        int height = (int) image.getHeight();
        int[] pixels = new int[width * height];
        image.getPixelReader().getPixels(0, 0, width, height,
                PixelFormat.getIntArgbInstance(), pixels, 0, width);
        BufferedImage png = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        png.setRGB(0, 0, width, height, pixels, 0, width);
        ImageIO.write(png, "png", OUTPUT.resolve(fileName).toFile());
    }
}
