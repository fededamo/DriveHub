package it.unifi.ing.drivehub.presentation;

import it.unifi.ing.drivehub.business.exceptions.ConflictException;
import it.unifi.ing.drivehub.domain.users.Role;
import it.unifi.ing.drivehub.domain.vehicles.VehicleState;
import it.unifi.ing.drivehub.presentation.core.*;
import it.unifi.ing.drivehub.presentation.navigation.NavigationManager;
import it.unifi.ing.drivehub.presentation.navigation.Route;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.stage.Window;
import org.junit.jupiter.api.*;

import java.lang.reflect.Proxy;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

@Tag("gui")
class JavaFxSmokeTest {
    private static final String VIEWS = "/it/unifi/ing/drivehub/presentation/view/";
    private static final UiModels.VehicleItem RENTAL = new UiModels.VehicleItem(
            11L, "VIN-RENT", "UI111AA", "Veicolo noleggio demo", 5000, new BigDecimal("50.00"), 2022, VehicleState.RENT, null);
    private static final UiModels.VehicleItem SALE = new UiModels.VehicleItem(
            12L, "VIN-SALE", "UI222AA", "Veicolo vendita demo", 8000, new BigDecimal("20000.00"), 2023, VehicleState.SALE, null);

    @BeforeAll
    static void startToolkit() throws Exception {
        CompletableFuture<Void> ready = new CompletableFuture<>();
        Platform.startup(() -> { Platform.setImplicitExit(false); ready.complete(null); });
        ready.get(15, TimeUnit.SECONDS);
    }

    @AfterEach
    void closeWindows() throws Exception {
        onFx(() -> List.copyOf(Window.getWindows()).forEach(Window::hide));
    }

    @Test
    @DisplayName("Routing for all roles, guard and logout")
    void allRoutesLoadAndLogoutRemovesSession() throws Exception {
        onFx(() -> {
            Stub stub = new Stub();
            ApplicationContext context = new ApplicationContext(stub.gateway());
            Stage stage = new Stage();
            NavigationManager.initialize(stage, context);
            NavigationManager navigation = NavigationManager.get();
            for (Route route : List.of(Route.WELCOME, Route.LOGIN, Route.REGISTER)) {
                navigation.show(route);
            }
            assertThrows(IllegalStateException.class, () -> navigation.show(Route.CUSTOMER));
            for (Role role : List.of(Role.CUSTOMER, Role.CLIENT, Role.SALESMAN, Role.MANAGER)) {
                context.session().open(session(role));
                navigation.showHomeForCurrentRole();
                navigation.logout();
                assertTrue(context.session().current().isEmpty());
                assertThrows(IllegalStateException.class, () -> navigation.show(Route.CUSTOMER));
            }
        });
    }

    private static UiModels.Session session(Role role) {
        return new UiModels.Session(1L, "Utente dimostrativo", "ui@drivehub.test", role);
    }

    private static void onFx(FxRunnable action) throws Exception {
        CompletableFuture<Void> result = new CompletableFuture<>();
        Platform.runLater(() -> {
            try {
                action.run();
                result.complete(null);
            } catch (Throwable error) {
                result.completeExceptionally(error);
            }
        });
        result.get(25, TimeUnit.SECONDS);
    }

    @FunctionalInterface interface FxRunnable { void run() throws Exception; }

    private static class Stub {
        List<UiModels.VehicleItem> catalog = List.of(RENTAL, SALE);
        UiGateway gateway() {
            return (UiGateway) Proxy.newProxyInstance(UiGateway.class.getClassLoader(), new Class<?>[]{UiGateway.class}, (proxy, method, args) -> {
                if ("searchCatalog".equals(method.getName()) || "allVehicles".equals(method.getName())) {
                    return catalog;
                }
                return method.getReturnType() == List.class ? List.of() : null;
            });
        }
    }
}
