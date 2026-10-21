package it.unifi.ing.drivehub.business.services;

import it.unifi.ing.drivehub.business.core.TransactionRunner;
import it.unifi.ing.drivehub.business.exceptions.EntityNotFoundException;
import it.unifi.ing.drivehub.dao.interfaces.DaoFactory;
import it.unifi.ing.drivehub.domain.users.BankAccount;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Servizio applicativo per la consultazione e gestione del conto corrente bancario del cliente.
 */
public final class BankAccountService {
    private final TransactionRunner transactions;

    public BankAccountService(DaoFactory daoFactory) {
        this.transactions = new TransactionRunner(daoFactory);
    }

    public BankAccount getAccountForClient(long clientId) {
        return transactions.execute(unit -> unit.bankAccounts().findByClientId(clientId)
                .orElseThrow(() -> new EntityNotFoundException("BankAccount for client", clientId)));
    }

    public BankAccount getAccountByClientId(long clientId) {
        return getAccountForClient(clientId);
    }

    public BankAccount depositFunds(long clientId, BigDecimal amount) {
        Objects.requireNonNull(amount, "amount");
        return transactions.execute(unit -> {
            BankAccount account = unit.bankAccounts().findByClientId(clientId)
                    .orElseThrow(() -> new EntityNotFoundException("BankAccount for client", clientId));
            account.deposit(amount);
            unit.bankAccounts().updateBalance(account.iban(), account.balance());
            return account;
        });
    }

    public void deposit(String iban, BigDecimal amount) {
        Objects.requireNonNull(amount, "amount");
        transactions.execute(unit -> {
            BankAccount account = unit.bankAccounts().findByIbanForUpdate(iban)
                    .orElseThrow(() -> new EntityNotFoundException("BankAccount with iban", iban));
            account.deposit(amount);
            unit.bankAccounts().update(account);
            return null;
        });
    }

    public BankAccount withdrawFunds(long clientId, BigDecimal amount) {
        Objects.requireNonNull(amount, "amount");
        return transactions.execute(unit -> {
            BankAccount account = unit.bankAccounts().findByClientId(clientId)
                    .orElseThrow(() -> new EntityNotFoundException("BankAccount for client", clientId));
            account.withdraw(amount);
            unit.bankAccounts().updateBalance(account.iban(), account.balance());
            return account;
        });
    }

    public void withdraw(String iban, BigDecimal amount) {
        Objects.requireNonNull(amount, "amount");
        transactions.execute(unit -> {
            BankAccount account = unit.bankAccounts().findByIbanForUpdate(iban)
                    .orElseThrow(() -> new EntityNotFoundException("BankAccount with iban", iban));
            account.withdraw(amount);
            unit.bankAccounts().update(account);
            return null;
        });
    }
}
