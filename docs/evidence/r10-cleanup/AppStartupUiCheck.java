package it.unifi.drivehub;

import it.unifi.drivehub.presentation.view.LoginView;
import javafx.application.Platform;
import javafx.scene.control.Button;
import javafx.scene.control.TabPane;
import javafx.scene.robot.Robot;
import javafx.stage.Stage;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/** Avvio reale di App.start e schermata pubblica, riusando il Robot delle prove05. */
public class AppStartupUiCheck extends PriceUiCheck {
    public static void main(String[] args) throws Exception {
        Thread.setDefaultUncaughtExceptionHandler((t,e) -> { e.printStackTrace(); System.exit(2); });
        try {
            out = args[0] + "/";
            CountDownLatch ready = new CountDownLatch(1);
            Platform.startup(ready::countDown);
            check(ready.await(10, TimeUnit.SECONDS), "JavaFX avviato su Xvfb");
            fx(() -> { stage = new Stage(); robot = new Robot(); new App().start(stage); return null; });
            ReactiveTablesUiCheck.frames();
            check(fx(() -> stage.isShowing() && stage.getScene().getRoot() instanceof LoginView), "App.start mostra LoginView");
            check(fx(() -> "DriveHub - Piattaforma Concessionario Auto".equals(stage.getTitle())), "titolo applicativo");
            check(fx(() -> !stage.getScene().getStylesheets().isEmpty()), "tema caricato");
            reach(n -> n instanceof Button b && "Accedi al Sistema".equals(b.getText()));
            check(fx(() -> ((TabPane)stage.getScene().getRoot().lookup(".tab-pane")).getTabs().size() == 2), "accesso e registrazione disponibili");
            screenshot("startup-login");
            System.out.println("APP STARTUP SUCCESS");
        } catch (Throwable error) {
            error.printStackTrace(); System.exit(1);
        } finally {
            Platform.exit();
        }
    }
}
