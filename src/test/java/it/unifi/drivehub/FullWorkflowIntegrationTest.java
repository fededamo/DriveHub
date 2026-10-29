package it.unifi.drivehub;

import it.unifi.drivehub.domain.operation.Request;
import it.unifi.drivehub.domain.user.Client;
import it.unifi.drivehub.domain.user.Manager;
import it.unifi.drivehub.domain.user.Salesman;
import it.unifi.drivehub.domain.user.User;
import it.unifi.drivehub.domain.vehicle.Vehicle;
import it.unifi.drivehub.domain.vehicle.VehicleState;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Verifica Completa dei Flussi Applicativi e Login sui 3 Ruoli")
class FullWorkflowIntegrationTest {

    private static AppContext ctx;

    @BeforeAll
    static void setUp() {
        ctx = AppContext.getInstance();
    }

    @Test
    @DisplayName("Flusso 1: Login Cliente (mario.rossi@email.it) e caricamento dati")
    void testClientLoginAndDataLoading() {
        User user = ctx.getAuthService().login("mario.rossi@email.it", "password");
        assertNotNull(user);
        assertInstanceOf(Client.class, user);
        Client client = (Client) user;
        assertNotNull(client.getBankAccount());
        assertNotNull(client.getBankAccount().getIban());

        // Controllo scadenze
        assertDoesNotThrow(() -> ctx.getPurchaseService().checkAndHandleExpiredRequests(client.getId()));

        // Caricamento catalogo vendita
        List<Vehicle> saleVehicles = ctx.getCatalogService().getVehiclesByState(VehicleState.SALE);
        assertNotNull(saleVehicles);
        assertFalse(saleVehicles.isEmpty());

        // Verifica calcolo prezzo effettivo su tutti i veicoli in vendita
        for (Vehicle v : saleVehicles) {
            BigDecimal price = ctx.getCatalogService().getEffectiveVehiclePrice(v);
            assertNotNull(price);
            assertTrue(price.compareTo(BigDecimal.ZERO) >= 0);
        }

        // Caricamento catalogo noleggio (inclusi veicoli 41 e 42 con VIN 16 caratteri)
        List<Vehicle> rentVehicles = ctx.getCatalogService().getVehiclesByState(VehicleState.RENT);
        assertNotNull(rentVehicles);
        assertFalse(rentVehicles.isEmpty());
        assertTrue(rentVehicles.stream().anyMatch(v -> v.getVin().length() == 16),
                "I veicoli a 16 caratteri devono essere gestiti correttamente senza errori sul VIN");

        // Caricamento operazioni cliente
        assertDoesNotThrow(() -> ctx.getOperationQueryService().getClientRequests(client.getId()));
        assertDoesNotThrow(() -> ctx.getOperationQueryService().getClientRents(client.getId()));
        assertDoesNotThrow(() -> ctx.getOperationQueryService().getClientTestDrives(client.getId()));
    }

    @Test
    @DisplayName("Flusso 2: Login Venditore (paolo.costa@drivehub.it) e caricamento dati")
    void testSalesmanLoginAndDataLoading() {
        User user = ctx.getAuthService().login("paolo.costa@drivehub.it", "password");
        assertNotNull(user);
        assertInstanceOf(Salesman.class, user);

        // Richieste in sospeso
        List<Request> pending = ctx.getOperationQueryService().getPendingRequests();
        assertNotNull(pending);

        // Inventario veicoli
        List<Vehicle> allVehicles = ctx.getCatalogService().getAllVehicles();
        assertNotNull(allVehicles);
        assertFalse(allVehicles.isEmpty());
    }

    @Test
    @DisplayName("Flusso 3: Login Manager (vittorio.manager@drivehub.it) e caricamento dati")
    void testManagerLoginAndDataLoading() {
        User user = ctx.getAuthService().login("vittorio.manager@drivehub.it", "password");
        assertNotNull(user);
        assertInstanceOf(Manager.class, user);

        // Marchi, Modelli, Veicoli, Sconti, Vendite
        assertFalse(ctx.getCatalogService().getAllBrands().isEmpty());
        assertFalse(ctx.getCatalogService().getAllModels().isEmpty());
        assertFalse(ctx.getCatalogService().getAllVehicles().isEmpty());
        assertNotNull(ctx.getCatalogService().getAllModelDiscounts());
        assertNotNull(ctx.getCatalogService().getAllVehicleDiscounts());
        assertFalse(ctx.getOperationQueryService().getAllSales().isEmpty());
    }
}

