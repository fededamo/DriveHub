package it.unifi.ing.drivehub.domain;

import it.unifi.ing.drivehub.domain.users.BankAccount;
import it.unifi.ing.drivehub.business.banking.IbanGenerator;
import it.unifi.ing.drivehub.domain.sales.Request;
import it.unifi.ing.drivehub.domain.sales.Sale;
import it.unifi.ing.drivehub.domain.vehicles.Vehicle;
import it.unifi.ing.drivehub.domain.vehicles.VehicleState;
import it.unifi.ing.drivehub.support.TestFixtures;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class SalesWorkflowTest {
    @Test
    @DisplayName("Request creation sets null expDate and positive deposit")
    void requestCreation() {
        Request request = Request.create(2L, 1L, new BigDecimal("4000.00"));

        assertNull(request.expDate());
        assertEquals(1L, request.clientId());
        assertEquals(2L, request.vehicleId());
        assertEquals(new BigDecimal("4000.00"), request.deposit());
    }

    @Test
    @DisplayName("Request unlock sets 30-day expiration date")
    void requestUnlockSetsExpDate() {
        Request request = Request.create(1L, 2L, new BigDecimal("4000.00"));
        LocalDate expiry = LocalDate.now().plusDays(30);

        request.unlock(expiry);

        assertEquals(expiry, request.expDate());
        assertTrue(request.isUnlocked());
    }

    @Test
    @DisplayName("Sale creation records completed sale or default failure")
    void saleCreation() {
        Sale completed = Sale.completed(1L, 2L, 3L, new BigDecimal("36000.00"), LocalDateTime.now());
        assertFalse(completed.isDefaultFailure());
        assertEquals(1L, completed.vehicleId());
        assertEquals(2L, completed.clientId());
        assertEquals(3L, completed.salesmanId());

        Sale failure = Sale.penalty(2L, new BigDecimal("800.00"), LocalDateTime.now());
        assertTrue(failure.isDefaultFailure());
        assertNull(failure.vehicleId());
        assertNull(failure.salesmanId());
        assertEquals(2L, failure.clientId());
    }

    @Test
    @DisplayName("BankAccount enforces non-negative balance on withdrawal")
    void bankAccountWithdrawal() {
        String iban = IbanGenerator.generate();
        BankAccount account = new BankAccount(iban, 1L, new BigDecimal("500.00"));

        account.deposit(new BigDecimal("200.00"));
        assertEquals(new BigDecimal("700.00"), account.balance());

        account.withdraw(new BigDecimal("300.00"));
        assertEquals(new BigDecimal("400.00"), account.balance());

        assertThrows(IllegalStateException.class, () -> account.withdraw(new BigDecimal("500.00")));
    }
}
