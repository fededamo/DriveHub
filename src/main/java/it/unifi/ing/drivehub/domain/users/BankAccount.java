package it.unifi.ing.drivehub.domain.users;

import it.unifi.ing.drivehub.domain.DomainRuleViolationException;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Conto corrente bancario intestato a un Cliente.
 */
public final class BankAccount {
    private final String iban;
    private final long clientId;
    private BigDecimal balance;

    public BankAccount(String iban, long clientId, BigDecimal balance) {
        if (iban == null || iban.isBlank() || iban.length() != 27) {
            throw new IllegalArgumentException("IBAN must be exactly 27 characters");
        }
        if (clientId <= 0) {
            throw new IllegalArgumentException("clientId must be positive");
        }
        this.iban = iban.trim().toUpperCase(java.util.Locale.ROOT);
        this.clientId = clientId;
        this.balance = Objects.requireNonNull(balance, "balance");
        if (this.balance.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("balance cannot be negative");
        }
    }

    public static BankAccount create(String iban, long clientId, BigDecimal initialBalance) {
        return new BankAccount(iban, clientId, initialBalance);
    }

    public String iban() {
        return iban;
    }

    public long clientId() {
        return clientId;
    }

    public BigDecimal balance() {
        return balance;
    }

    public void deposit(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new DomainRuleViolationException("Deposit amount must be strictly positive");
        }
        this.balance = this.balance.add(amount);
    }

    public void withdraw(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new DomainRuleViolationException("Withdrawal amount must be strictly positive");
        }
        if (this.balance.compareTo(amount) < 0) {
            throw new DomainRuleViolationException("Fondi insufficienti sul conto corrente: saldo attuale "
                    + balance + " €, importo richiesto " + amount + " €");
        }
        this.balance = this.balance.subtract(amount);
    }
}
