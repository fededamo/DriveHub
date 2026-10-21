package it.unifi.ing.drivehub.business.services;

import it.unifi.ing.drivehub.dao.interfaces.BankAccountDao;
import it.unifi.ing.drivehub.dao.interfaces.DaoFactory;
import it.unifi.ing.drivehub.dao.interfaces.RentalDao;
import it.unifi.ing.drivehub.dao.interfaces.UnitOfWork;
import it.unifi.ing.drivehub.dao.interfaces.VehicleDao;
import it.unifi.ing.drivehub.domain.users.BankAccount;
import it.unifi.ing.drivehub.business.banking.IbanGenerator;
import it.unifi.ing.drivehub.domain.rentals.Rent;
import it.unifi.ing.drivehub.domain.vehicles.Vehicle;
import it.unifi.ing.drivehub.support.TestFixtures;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class RentalServiceTest {
    private UnitOfWork unit;
    private VehicleDao vehicles;
    private RentalDao rentals;
    private BankAccountDao bankAccounts;
    private it.unifi.ing.drivehub.dao.interfaces.TestDriveDao testDrives;
    private RentalService service;

    @BeforeEach
    void setUp() {
        DaoFactory factory = mock(DaoFactory.class);
        unit = mock(UnitOfWork.class);
        vehicles = mock(VehicleDao.class);
        rentals = mock(RentalDao.class);
        bankAccounts = mock(BankAccountDao.class);
        testDrives = mock(it.unifi.ing.drivehub.dao.interfaces.TestDriveDao.class);
        when(factory.begin()).thenReturn(unit);
        when(unit.vehicles()).thenReturn(vehicles);
        when(unit.rentals()).thenReturn(rentals);
        when(unit.bankAccounts()).thenReturn(bankAccounts);
        when(unit.testDrives()).thenReturn(testDrives);
        service = new RentalService(factory);
    }

    @Test
    @DisplayName("Book rental debits client bank account and saves Rent")
    void bookRentalDebitsAccount() {
        // Veicolo con prezzo 100.000 € -> tariffa giornaliera (0.1%) = 100 €
        Vehicle vehicle = new Vehicle(30L, 20L, "VIN30", "AB123CD", 22_000, new BigDecimal("100000.00"), 2022, it.unifi.ing.drivehub.domain.vehicles.VehicleState.RENT);
        when(vehicles.findById(30L)).thenReturn(Optional.of(vehicle));

        String iban = IbanGenerator.generate();
        BankAccount account = new BankAccount(iban, 1L, new BigDecimal("500.00"));
        when(bankAccounts.findByClientId(1L)).thenReturn(Optional.of(account));

        when(rentals.save(any())).thenAnswer(inv -> {
            Rent r = inv.getArgument(0);
            r.assignId(10L);
            return r;
        });

        LocalDate start = LocalDate.now();
        LocalDate end = start; // 1 giorno -> costo = 100.00 €

        Rent rent = service.bookRental(1L, 30L, start, end);

        assertNotNull(rent);
        assertEquals(new BigDecimal("400.00"), account.balance());
        verify(bankAccounts).updateBalance(account.iban(), account.balance());
        verify(rentals).save(any(Rent.class));
        verify(unit).commit();
    }
}
