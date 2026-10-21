package it.unifi.ing.drivehub.domain;

import it.unifi.ing.drivehub.domain.users.Role;
import it.unifi.ing.drivehub.domain.users.User;
import it.unifi.ing.drivehub.domain.vehicles.Vehicle;
import it.unifi.ing.drivehub.domain.vehicles.VehicleState;
import it.unifi.ing.drivehub.support.TestFixtures;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class UserAndVehicleTest {
    @Test
    @DisplayName("User creation trims names and normalizes email")
    void userCreationNormalizesFields() {
        User user = new User(1L, " Maria ", " Rossi ", "MARIA@EXAMPLE.IT", "hash", Role.CLIENT);

        assertEquals("Maria", user.name());
        assertEquals("Rossi", user.surname());
        assertEquals("Maria Rossi", user.displayName());
        assertEquals("maria@example.it", user.email());
    }

    @Test
    @DisplayName("Vehicle approval transitions state from WAITING to SALE")
    void vehicleApproval() {
        Vehicle vehicle = TestFixtures.waitingVehicle(1L, "AA111AA");
        assertEquals(VehicleState.WAITING, vehicle.state());

        vehicle.approve();
        assertEquals(VehicleState.SALE, vehicle.state());
    }

    @Test
    @DisplayName("Vehicle block and unblock transitions work correctly")
    void vehicleBlockAndUnblock() {
        Vehicle vehicle = TestFixtures.saleVehicle(1L, "AA111AA");
        assertEquals(VehicleState.SALE, vehicle.state());

        vehicle.block();
        assertEquals(VehicleState.BLOCKED, vehicle.state());

        vehicle.unblock();
        assertEquals(VehicleState.SALE, vehicle.state());
    }

    @Test
    @DisplayName("Illegal transition throws DomainRuleViolationException")
    void illegalTransitionThrows() {
        Vehicle vehicle = TestFixtures.rentalVehicle(1L, "AA111AA");
        assertThrows(DomainRuleViolationException.class, vehicle::approve);
        assertThrows(DomainRuleViolationException.class, vehicle::block);
    }

    @Test
    @DisplayName("Se il veicolo non ha targa i chilometri devono essere 0")
    void vehicleWithoutPlateRequiresZeroKm() {
        // Senza targa e 0 km: valido
        assertDoesNotThrow(() -> Vehicle.create(1L, "VIN123", null, 0, new BigDecimal("20000.00"), 2023, VehicleState.SALE));

        // Senza targa e km > 0: eccezione di dominio
        assertThrows(DomainRuleViolationException.class, () ->
                Vehicle.create(1L, "VIN123", null, 1500, new BigDecimal("20000.00"), 2023, VehicleState.SALE));
    }

    @Test
    @DisplayName("Anno veicolo deve essere compreso tra 1900 e anno attuale")
    void vehicleYearValidation() {
        int currentYear = java.time.LocalDate.now().getYear();
        assertDoesNotThrow(() -> Vehicle.create(1L, "VIN123", "AA111BB", 1000, new BigDecimal("20000.00"), currentYear, VehicleState.SALE));
        assertThrows(IllegalArgumentException.class, () ->
                Vehicle.create(1L, "VIN123", "AA111BB", 1000, new BigDecimal("20000.00"), 1899, VehicleState.SALE));
        assertThrows(IllegalArgumentException.class, () ->
                Vehicle.create(1L, "VIN123", "AA111BB", 1000, new BigDecimal("20000.00"), currentYear + 1, VehicleState.SALE));
    }
}
