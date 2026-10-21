package it.unifi.ing.drivehub.functional;

import it.unifi.ing.drivehub.business.exceptions.AuthenticationException;
import it.unifi.ing.drivehub.business.exceptions.ConflictException;
import it.unifi.ing.drivehub.business.services.RegistrationRequest;
import it.unifi.ing.drivehub.domain.rentals.TestDrive;
import it.unifi.ing.drivehub.domain.users.Role;
import it.unifi.ing.drivehub.domain.users.User;
import it.unifi.ing.drivehub.presentation.core.SessionContext;
import it.unifi.ing.drivehub.presentation.core.UiModels;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class UserGoalsFunctionalTest extends FunctionalTestSupport {

    @ParameterizedTest
    @EnumSource(value = Role.class, names = {"CLIENT", "SALESMAN", "MANAGER"})
    @DisplayName("FT-AUTH-01: registration and login for each user role")
    void registerAndLoginAllRoles(Role role) {
        String email = "test_" + role.name().toLowerCase() + "@example.com";
        User u = register("Test", "User", email, role);
        assertNotNull(u.id());

        User logged = auth.login(email.toUpperCase(), "pass-123".toCharArray());
        assertEquals(u.id(), logged.id());
        assertEquals(role, logged.role());
    }

    @Test
    @DisplayName("FT-AUTH-02: duplicate email is rejected")
    void duplicateEmailRejected() {
        assertThrows(ConflictException.class, () ->
                auth.register(new RegistrationRequest("Mario", "Rossi", customer.email(), Role.CLIENT),
                        "pass-123".toCharArray()));
    }

    @Test
    @DisplayName("FT-AUTH-03: invalid password or unknown user denies login")
    void loginFailures() {
        assertThrows(AuthenticationException.class, () ->
                auth.login(customer.email(), "wrong-password".toCharArray()));
        assertThrows(AuthenticationException.class, () ->
                auth.login("nonexistent@example.com", "pass-123".toCharArray()));
    }

    @Test
    @DisplayName("FT-SESSION-01: session context open and clear")
    void sessionLifecycle() {
        SessionContext session = new SessionContext();
        session.open(new UiModels.Session(customer.requireId(), customer.displayName(), customer.email(), customer.role()));
        assertTrue(session.isLoggedIn());
        assertEquals(customer.displayName(), session.requireCurrent().displayName());

        session.clear();
        assertFalse(session.isLoggedIn());
        assertThrows(IllegalStateException.class, session::requireCurrent);
    }

    @Test
    @DisplayName("FT-CAT-01: catalog lists available vehicles")
    void catalogListings() {
        List<UiModels.VehicleItem> items = gateway.searchCatalog("", null);
        assertFalse(items.isEmpty());
        assertTrue(items.stream().anyMatch(v -> v.plate() != null && v.plate().equals(saleVehicle.plate())));
    }

    @Test
    @DisplayName("FT-TD-01: customer books test drive")
    void bookTestDrive() {
        LocalDate date = LocalDate.now().plusDays(2);
        LocalTime start = LocalTime.of(15, 0);

        TestDrive td = testDrives.book(customer.requireId(), saleVehicle.requireId(), date, start);
        assertNotNull(td.id());
        assertEquals(customer.requireId(), td.clientId());
        assertEquals(saleVehicle.requireId(), td.vehicleId());
    }

    @Test
    @DisplayName("FT-DISC-01: manager sets vehicle discount and quote reflects discounted price")
    void managerVehicleDiscount() {
        BigDecimal initialQuote = pricing.quotePrice(saleVehicle.requireId());

        // Manager imposta 10% di sconto
        pricing.setVehicleDiscount(saleVehicle.requireId(), 10);

        BigDecimal discountedQuote = pricing.quotePrice(saleVehicle.requireId());
        assertTrue(discountedQuote.compareTo(initialQuote) < 0);

        // Rimuove lo sconto
        pricing.removeVehicleDiscount(saleVehicle.requireId());
        assertEquals(initialQuote, pricing.quotePrice(saleVehicle.requireId()));
    }
}
