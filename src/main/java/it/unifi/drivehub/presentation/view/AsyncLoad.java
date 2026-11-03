package it.unifi.drivehub.presentation.view;

import javafx.concurrent.Task;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.HBox;

import java.util.function.Consumer;
import java.util.function.Supplier;

/** Un caricamento per controllo: solo l'ultima richiesta può pubblicare il risultato. */
final class AsyncLoad<T> extends HBox {
    private final Node target;
    private final Consumer<T> apply;
    private final Label status = new Label();
    private final ProgressIndicator progress = new ProgressIndicator();
    private final Button refresh = new Button("Aggiorna");
    private Supplier<T> source;
    private Task<T> current;

    AsyncLoad(String name, Node target, Supplier<T> source, Consumer<T> apply) {
        super(8);
        this.target = target;
        this.source = source;
        this.apply = apply;
        setAlignment(Pos.CENTER_LEFT);
        progress.setMaxSize(20, 20);
        progress.setFocusTraversable(false);
        status.setWrapText(true);
        refresh.setAccessibleText("Aggiorna o riprova: " + name);
        refresh.setOnAction(event -> load());
        getChildren().addAll(refresh, progress, status);
        target.sceneProperty().addListener((obs, oldScene, scene) -> {
            if (oldScene != null && scene == null && current != null) {
                current.cancel();
                current = null;
            }
        });
    }

    void load(Supplier<T> source) {
        this.source = source;
        load();
    }

    void load() {
        if (current != null) current.cancel();
        Supplier<T> request = source;
        Task<T> task = new Task<>() {
            @Override protected T call() { return request.get(); }
        };
        current = task;
        target.setDisable(true);
        progress.setVisible(true);
        status.setText("Caricamento in corso…");
        refresh.setText("Aggiorna");
        task.setOnSucceeded(event -> {
            if (current != task) return;
            apply.accept(task.getValue());
            progress.setVisible(false);
            status.setText("Aggiornato");
            target.setDisable(false);
        });
        task.setOnFailed(event -> {
            if (current != task) return;
            progress.setVisible(false);
            status.setText("Caricamento non riuscito. Riprova.");
            refresh.setText("Riprova");
        });
        Thread.ofVirtual().name("drivehub-table-load").start(task);
    }
}
