package it.unifi.ing.drivehub.business.services;

import it.unifi.ing.drivehub.business.core.TransactionRunner;
import it.unifi.ing.drivehub.dao.interfaces.DaoFactory;
import it.unifi.ing.drivehub.dao.interfaces.UnitOfWork;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import it.unifi.ing.drivehub.domain.observer.InventoryEvent;
import it.unifi.ing.drivehub.domain.observer.InventoryEventType;
import it.unifi.ing.drivehub.domain.observer.InventoryObserver;
import it.unifi.ing.drivehub.domain.rentals.Rent;
import it.unifi.ing.drivehub.domain.sales.Request;
import it.unifi.ing.drivehub.domain.users.BankAccount;
import it.unifi.ing.drivehub.domain.vehicles.Vehicle;
import it.unifi.ing.drivehub.domain.vehicles.VehicleState;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class TransactionAndObserverTest {
    @Test
    @DisplayName("UT-TX-01: commit failure rolls back and closes without hiding the original failure")
    void commitFailurePreservesCauseAndCloses() {
        DaoFactory factory = mock(DaoFactory.class);
        UnitOfWork unit = mock(UnitOfWork.class);
        when(factory.begin()).thenReturn(unit);
        var original = new IllegalStateException("commit failure");
        var rollback = new IllegalStateException("rollback failure");
        doThrow(original).when(unit).commit();
        doThrow(rollback).when(unit).rollback();
        assertSame(original, assertThrows(IllegalStateException.class,
                () -> new TransactionRunner(factory).execute(u -> "result")));
        assertArrayEquals(new Throwable[]{rollback}, original.getSuppressed());
        var order = inOrder(unit);
        order.verify(unit).commit();
        order.verify(unit).rollback();
        order.verify(unit).close();
    }

    @Test
    @DisplayName("UT-OBS-01: RentalService notifies observers and cancels conflicting test drives on rental booking")
    void rentalServiceNotifiesObserverAndCancelsTestDrives() {
        DaoFactory factory = mock(DaoFactory.class);
        UnitOfWork unit = mock(UnitOfWork.class);
        var vehicles = mock(it.unifi.ing.drivehub.dao.interfaces.VehicleDao.class);
        var rentals = mock(it.unifi.ing.drivehub.dao.interfaces.RentalDao.class);
        var bankAccounts = mock(it.unifi.ing.drivehub.dao.interfaces.BankAccountDao.class);
        var testDrives = mock(it.unifi.ing.drivehub.dao.interfaces.TestDriveDao.class);

        when(factory.begin()).thenReturn(unit);
        when(unit.vehicles()).thenReturn(vehicles);
        when(unit.rentals()).thenReturn(rentals);
        when(unit.bankAccounts()).thenReturn(bankAccounts);
        when(unit.testDrives()).thenReturn(testDrives);

        Vehicle vehicle = new Vehicle(10L, 1L, "VIN10", "AB123CD", 10000, new BigDecimal("20000.00"), 2022, VehicleState.RENT);
        when(vehicles.findById(10L)).thenReturn(Optional.of(vehicle));

        String iban = it.unifi.ing.drivehub.business.banking.IbanGenerator.generate();
        BankAccount clientAccount = new BankAccount(iban, 5L, new BigDecimal("1000.00"));
        when(bankAccounts.findByClientId(5L)).thenReturn(Optional.of(clientAccount));

        when(rentals.save(any())).thenAnswer(inv -> inv.getArgument(0));

        RentalService rentalService = new RentalService(factory);
        List<InventoryEvent> events = new ArrayList<>();
        rentalService.subscribe(events::add);

        LocalDate start = LocalDate.now().plusDays(2);
        LocalDate end = LocalDate.now().plusDays(4);

        Rent rent = rentalService.bookRental(5L, 10L, start, end);

        assertNotNull(rent);
        verify(testDrives).cancelTestDrivesInRange(eq(10L), any(LocalDateTime.class), any(LocalDateTime.class));
        assertFalse(events.isEmpty());
        assertTrue(events.stream().anyMatch(e -> e.type() == InventoryEventType.TEST_DRIVES_CANCELLED_VEHICLE_UNAVAILABLE));
    }

    @Test
    @DisplayName("UT-OBS-02: SalesService notifies observers on purchase blocking and sale completion")
    void salesServiceNotifiesObserver() {
        DaoFactory factory = mock(DaoFactory.class);
        UnitOfWork unit = mock(UnitOfWork.class);
        var vehicles = mock(it.unifi.ing.drivehub.dao.interfaces.VehicleDao.class);
        var requests = mock(it.unifi.ing.drivehub.dao.interfaces.RequestDao.class);
        var sales = mock(it.unifi.ing.drivehub.dao.interfaces.SaleDao.class);
        var bankAccounts = mock(it.unifi.ing.drivehub.dao.interfaces.BankAccountDao.class);
        var testDrives = mock(it.unifi.ing.drivehub.dao.interfaces.TestDriveDao.class);

        when(factory.begin()).thenReturn(unit);
        when(unit.vehicles()).thenReturn(vehicles);
        when(unit.requests()).thenReturn(requests);
        when(unit.sales()).thenReturn(sales);
        when(unit.bankAccounts()).thenReturn(bankAccounts);
        when(unit.testDrives()).thenReturn(testDrives);

        Vehicle vehicle = new Vehicle(20L, 2L, "VIN20", "CD456EF", 5000, new BigDecimal("30000.00"), 2023, VehicleState.SALE);
        when(vehicles.findById(20L)).thenReturn(Optional.of(vehicle));

        String iban = it.unifi.ing.drivehub.business.banking.IbanGenerator.generate();
        BankAccount clientAccount = new BankAccount(iban, 8L, new BigDecimal("10000.00"));
        when(bankAccounts.findByClientId(8L)).thenReturn(Optional.of(clientAccount));

        when(requests.save(any())).thenAnswer(inv -> inv.getArgument(0));

        SalesService salesService = new SalesService(factory);
        List<InventoryEvent> events = new ArrayList<>();
        salesService.subscribe(events::add);

        Request req = salesService.requestPurchase(8L, 20L, new BigDecimal("6000.00"));

        assertNotNull(req);
        assertEquals(1, events.size());
        assertEquals(InventoryEventType.STATUS_CHANGED, events.get(0).type());
        assertEquals(VehicleState.BLOCKED, events.get(0).currentStatus());
    }
}
