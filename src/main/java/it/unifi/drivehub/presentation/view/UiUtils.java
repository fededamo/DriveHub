package it.unifi.drivehub.presentation.view;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Optional;

/**
 * Utilità grafiche per la visualizzazione di alert, formattazione valuta e date.
 */
public final class UiUtils {

    public static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    public static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final NumberFormat CURRENCY_FORMAT = NumberFormat.getCurrencyInstance(Locale.ITALY);

    private UiUtils() {}

    public static String formatCurrency(BigDecimal amount) {
        if (amount == null) return "€ 0,00";
        return CURRENCY_FORMAT.format(amount);
    }

    public static void showInfo(String title, String message) {
        showInfo(null, title, message);
    }

    public static void showInfo(javafx.stage.Window owner, String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        if (owner != null) {
            alert.initOwner(owner);
        }
        alert.showAndWait();
    }

    public static void showError(String title, String message) {
        showError(null, title, message);
    }

    public static void showError(javafx.stage.Window owner, String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText("Attenzione");
        alert.setContentText(message);
        if (owner != null) {
            alert.initOwner(owner);
        }
        alert.showAndWait();
    }

    public static boolean showConfirmation(String title, String header, String content) {
        return showConfirmation(null, title, header, content);
    }

    public static boolean showConfirmation(javafx.stage.Window owner, String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        if (owner != null) {
            alert.initOwner(owner);
        }
        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }

    public static VBox createCard(String title) {
        VBox card = new VBox(10);
        card.getStyleClass().add("card");
        card.setPadding(new Insets(15));

        if (title != null && !title.isEmpty()) {
            Label lbl = new Label(title);
            lbl.getStyleClass().add("card-title");
            card.getChildren().add(lbl);
        }
        return card;
    }

    public static HBox createHeader(String titleText, String subtitleText) {
        HBox header = new HBox(15);
        header.setAlignment(Pos.CENTER_LEFT);
        header.getStyleClass().add("dashboard-header");
        header.setPadding(new Insets(15, 20, 15, 20));

        VBox titles = new VBox(3);
        Label title = new Label(titleText);
        title.getStyleClass().add("app-title");
        Label subtitle = new Label(subtitleText);
        subtitle.getStyleClass().add("app-subtitle");
        titles.getChildren().addAll(title, subtitle);

        header.getChildren().add(titles);
        return header;
    }
}

