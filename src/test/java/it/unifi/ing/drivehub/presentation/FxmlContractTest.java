package it.unifi.ing.drivehub.presentation;

import org.junit.jupiter.api.Test;

import javax.xml.parsers.DocumentBuilderFactory;
import java.lang.reflect.Method;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import javafx.fxml.FXML;
import javafx.event.ActionEvent;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;

class FxmlContractTest {
    private static final Path VIEW_ROOT = Path.of(
            "src/main/resources/it/unifi/ing/drivehub/presentation/view");
    private static final Pattern CONTROLLER = Pattern.compile("fx:controller=\"([^\"]+)\"");
    private static final Pattern HANDLER = Pattern.compile("onAction=\"#([^\"]+)\"");

    @Test
    void allViewsAreWellFormedAndHandlersExist() throws Exception {
        var factory = DocumentBuilderFactory.newInstance();
        factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        try (var paths = Files.list(VIEW_ROOT)) {
            var views = paths.filter(path -> path.toString().endsWith(".fxml")).sorted().toList();
            assertEquals(6, views.size(), "unexpected number of FXML views");
            for (Path view : views) {
                factory.newDocumentBuilder().parse(view.toFile());
                String source = Files.readString(view);
                Matcher controllerMatch = CONTROLLER.matcher(source);
                assertTrue(controllerMatch.find(), () -> view + " has no fx:controller");
                Class<?> controller = Class.forName(controllerMatch.group(1));
                Set<String> ids = new HashSet<>();
                Matcher idMatches = Pattern.compile("fx:id=\"([^\"]+)\"").matcher(source);
                while (idMatches.find()) {
                    assertTrue(ids.add(idMatches.group(1)), "Duplicate fx:id in " + view);
                }
                for (Class<?> type = controller; type != null; type = type.getSuperclass()) {
                    for (Field field : type.getDeclaredFields()) {
                        if (field.isAnnotationPresent(FXML.class)) {
                            assertTrue(ids.contains(field.getName()),
                                    () -> view + " does not inject @FXML field " + field.getName());
                        }
                    }
                }
                Set<String> methods = allMethodNames(controller);
                Matcher handlers = HANDLER.matcher(source);
                while (handlers.find()) {
                    String handler = handlers.group(1);
                    assertTrue(methods.contains(handler),
                            () -> view + " references missing handler #" + handler);
                }
            }
        }
    }

    @Test
    void sharedStylesheetExists() {
        assertTrue(Files.isRegularFile(VIEW_ROOT.resolve("../css/style.css").normalize()));
    }

    private static Set<String> allMethodNames(Class<?> type) {
        Set<String> names = new HashSet<>();
        for (Class<?> current = type; current != null; current = current.getSuperclass()) {
            for (Method method : current.getDeclaredMethods()) {
                if ((Modifier.isPublic(method.getModifiers()) || method.isAnnotationPresent(FXML.class))
                        && (method.getParameterCount() == 0
                        || (method.getParameterCount() == 1
                        && method.getParameterTypes()[0].isAssignableFrom(ActionEvent.class)))) {
                    names.add(method.getName());
                }
            }
        }
        return names;
    }
}
