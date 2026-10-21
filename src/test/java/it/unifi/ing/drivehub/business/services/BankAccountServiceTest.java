package it.unifi.ing.drivehub.business.services;

import it.unifi.ing.drivehub.dao.interfaces.BankAccountDao;
import it.unifi.ing.drivehub.dao.interfaces.DaoFactory;
import it.unifi.ing.drivehub.dao.interfaces.UnitOfWork;
import it.unifi.ing.drivehub.domain.users.BankAccount;
import it.unifi.ing.drivehub.business.banking.IbanGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BankAccountServiceTest {
    private UnitOfWork unit;
    private BankAccountDao bankAccounts;
    private BankAccountService service;

    @BeforeEach
    void setUp() {
        DaoFactory factory = mock(DaoFactory.class);
        unit = mock(UnitOfWork.class);
        bankAccounts = mock(BankAccountDao.class);
        when(factory.begin()).thenReturn(unit);
        when(unit.bankAccounts()).thenReturn(bankAccounts);
        service = new BankAccountService(factory);
    }

    @Test
    @DisplayName("Deposit increases balance")
    void depositIncreasesBalance() {
        String iban = IbanGenerator.generate();
        BankAccount account = new BankAccount(iban, 1L, new BigDecimal("100.00"));
        when(bankAccounts.findByIbanForUpdate(iban)).thenReturn(Optional.of(account));

        service.deposit(iban, new BigDecimal("50.00"));

        assertEquals(new BigDecimal("150.00"), account.balance());
        verify(bankAccounts).update(account);
        verify(unit).commit();
    }

    @Test
    @DisplayName("Withdraw decreases balance")
    void withdrawDecreasesBalance() {
        String iban = IbanGenerator.generate();
        BankAccount account = new BankAccount(iban, 1L, new BigDecimal("100.00"));
        when(bankAccounts.findByIbanForUpdate(iban)).thenReturn(Optional.of(account));

        service.withdraw(iban, new BigDecimal("40.00"));

        assertEquals(new BigDecimal("60.00"), account.balance());
        verify(bankAccounts).update(account);
        verify(unit).commit();
    }

    @Test
    @DisplayName("Withdraw fails if insufficient funds")
    void withdrawFailsIfInsufficient() {
        String iban = IbanGenerator.generate();
        BankAccount account = new BankAccount(iban, 1L, new BigDecimal("100.00"));
        when(bankAccounts.findByIbanForUpdate(iban)).thenReturn(Optional.of(account));

        assertThrows(IllegalStateException.class, () -> service.withdraw(iban, new BigDecimal("150.00")));

        verify(bankAccounts, never()).update(any());
        verify(unit).rollback();
    }
}
