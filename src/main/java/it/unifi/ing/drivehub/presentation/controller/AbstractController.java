package it.unifi.ing.drivehub.presentation.controller;

import it.unifi.ing.drivehub.presentation.core.ApplicationContext;
import it.unifi.ing.drivehub.presentation.core.ContextAware;
import it.unifi.ing.drivehub.presentation.core.UiGateway;
import it.unifi.ing.drivehub.presentation.core.UiModels;
import it.unifi.ing.drivehub.presentation.navigation.NavigationManager;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;

import java.math.BigDecimal;
import java.util.Objects;

abstract class AbstractController implements ContextAware {
    protected ApplicationContext context;

    @Override
    public final void setApplicationContext(ApplicationContext context) {
        this.context = Objects.requireNonNull(context);
        onContextReady();
    }

    protected void onContextReady() {
    }

    protected UiGateway gateway() {
        return context.gateway();
    }

    protected UiModels.Session session() {
        return context.session().requireCurrent();
    }

    protected void showSuccess(Label label, String message) {
        label.getStyleClass().removeAll("danger-text", "success-text");
        label.getStyleClass().add("success-text");
        label.setText(message);
    }

    protected void showError(Label label, Throwable error) {
        label.getStyleClass().removeAll("success-text", "danger-text");
        label.getStyleClass().add("danger-text");
        String message = error.getMessage();
        label.setText(message == null || message.isBlank() ? "Operazione non riuscita" : message);
    }

    protected void showError(Label label, String message) {
        label.getStyleClass().removeAll("success-text", "danger-text");
        label.getStyleClass().add("danger-text");
        label.setText(message == null || message.isBlank() ? "Operazione non riuscita" : message);
    }

    protected BigDecimal decimal(String text, String fieldName) {
        try {
            return new BigDecimal(text.trim().replace(',', '.'));
        } catch (RuntimeException exception) {
            throw new IllegalArgumentException(fieldName + " non valido");
        }
    }

    protected long wholeNumber(String text, String fieldName) {
        try {
            return Long.parseLong(text.trim());
        } catch (RuntimeException exception) {
            throw new IllegalArgumentException(fieldName + " non valido");
        }
    }

    protected void alert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public void logout() {
        NavigationManager.get().logout();
    }
}
