package it.unifi.drivehub;

import it.unifi.drivehub.domain.discount.ModelDiscount;
import it.unifi.drivehub.domain.discount.VehicleDiscount;
import it.unifi.drivehub.domain.operation.Rent;
import it.unifi.drivehub.domain.operation.TestDrive;
import it.unifi.drivehub.domain.user.BankAccount;
import it.unifi.drivehub.domain.user.Client;
import it.unifi.drivehub.domain.vehicle.Brand;
import it.unifi.drivehub.domain.vehicle.Model;
import it.unifi.drivehub.domain.vehicle.Vehicle;
import it.unifi.drivehub.domain.vehicle.VehicleState;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Year;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Verifica degli Invarianti di Dominio (DDD)")
class DomainInvariantsTest {

    @Test
    @DisplayName("Regola: se un veicolo non ha targa, obbligatoriamente i km devono essere 0")
    void testVehiclePlateAndKmInvariant() {
        // Valido: senza targa e km = 0
        assertDoesNotThrow(() -> Vehicle.create(
                1L, 1L, "12345678901234567", null, 0,
                BigDecimal.valueOf(20000), 2023, VehicleState.SALE));

        // Violazione: senza targa ma con km > 0
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> Vehicle.create(
                1L, 1L, "12345678901234567", null, 15000,
                BigDecimal.valueOf(20000), 2023, VehicleState.SALE));
        assertTrue(ex.getMessage().contains("km") || ex.getMessage().contains("chilometri"));

        // Valido: con targa e km = 0 (il contrario non deve valere)
        assertDoesNotThrow(() -> Vehicle.create(
                1L, 1L, "12345678901234567", "AB123CD", 0,
                BigDecimal.valueOf(20000), 2023, VehicleState.SALE));
    }

    @Test
    @DisplayName("Regola: sconti compresi tra 0 e 50%")
    void testDiscountRange() {
        assertDoesNotThrow(() -> ModelDiscount.create(1L, 0));
        assertDoesNotThrow(() -> ModelDiscount.create(1L, 50));
        assertThrows(IllegalArgumentException.class, () -> ModelDiscount.create(1L, -1));
        assertThrows(IllegalArgumentException.class, () -> ModelDiscount.create(1L, 51));

        assertDoesNotThrow(() -> VehicleDiscount.create(1L, 25));
        assertThrows(IllegalArgumentException.class, () -> VehicleDiscount.create(1L, 60));
    }

    @Test
    @DisplayName("Regola: anno tra 1900 e anno attuale dinamico")
    void testYearRange() {
        int currentYear = Year.now().getValue();
        assertDoesNotThrow(() -> Model.create(1L, "Fiat", "Panda", BigDecimal.valueOf(15000), 1900));
        assertDoesNotThrow(() -> Model.create(1L, "Fiat", "Panda", BigDecimal.valueOf(15000), currentYear));
        assertThrows(IllegalArgumentException.class, () -> Model.create(1L, "Fiat", "Panda", BigDecimal.valueOf(15000), 1899));
        assertThrows(IllegalArgumentException.class, () -> Model.create(1L, "Fiat", "Panda", BigDecimal.valueOf(15000), currentYear + 1));
    }

    @Test
    @DisplayName("Regola: orario Test Drive compreso tra 9:00 e 16:00")
    void testTestDriveTimeSlot() {
        LocalDate tomorrow = LocalDate.now().plusDays(1);

        // Valido: ore 9:00 e ore 16:00
        assertDoesNotThrow(() -> TestDrive.create(1L, 1L, 1L, LocalDateTime.of(tomorrow.getYear(), tomorrow.getMonth(), tomorrow.getDayOfMonth(), 9, 0)));
        assertDoesNotThrow(() -> TestDrive.create(1L, 1L, 1L, LocalDateTime.of(tomorrow.getYear(), tomorrow.getMonth(), tomorrow.getDayOfMonth(), 16, 0)));

        // Non valido: ore 8:59 e ore 16:01
        assertThrows(IllegalArgumentException.class, () -> TestDrive.create(1L, 1L, 1L, LocalDateTime.of(tomorrow.getYear(), tomorrow.getMonth(), tomorrow.getDayOfMonth(), 8, 59)));
        assertThrows(IllegalArgumentException.class, () -> TestDrive.create(1L, 1L, 1L, LocalDateTime.of(tomorrow.getYear(), tomorrow.getMonth(), tomorrow.getDayOfMonth(), 16, 1)));
    }

    @Test
    @DisplayName("Regola: conto corrente con saldo non negativo e prelievo protetto")
    void testBankAccountInvariants() {
        BankAccount acc = BankAccount.createInitial("IT19W0503401600000793728296", 1L);
        assertEquals(BigDecimal.ZERO, acc.getBalance());

        acc.deposit(BigDecimal.valueOf(500));
        assertEquals(BigDecimal.valueOf(500), acc.getBalance());

        acc.withdraw(BigDecimal.valueOf(300));
        assertEquals(BigDecimal.valueOf(200), acc.getBalance());

        // Saldo insufficiente
        assertThrows(IllegalStateException.class, () -> acc.withdraw(BigDecimal.valueOf(250)));
    }

    @Test
    @DisplayName("Regola: date di noleggio valide (fine >= inizio)")
    void testRentDates() {
        LocalDate today = LocalDate.now();
        assertDoesNotThrow(() -> Rent.create(1L, 1L, 1L, BigDecimal.valueOf(100), today, today));
        assertDoesNotThrow(() -> Rent.create(1L, 1L, 1L, BigDecimal.valueOf(100), today, today.plusDays(3)));
        assertThrows(IllegalArgumentException.class, () -> Rent.create(1L, 1L, 1L, BigDecimal.valueOf(100), today.plusDays(3), today));
    }
}

