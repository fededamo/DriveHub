package it.unifi.drivehub.domain.user;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Entità Conto Bancario associata a un cliente.
 * Invarianti di dominio:
 * - Saldo non negativo (balance >= 0)
 * - IBAN non nullo e conforme al formato italiano standard
 */
public final class BankAccount {

    private static final Pattern IBAN_PATTERN = Pattern.compile("^[A-Z]{2}\\d{2}[A-Z0-9]{11,30}$");

    private final String iban;
    private final Long clientId;
    private BigDecimal balance;

    private BankAccount(String iban, Long clientId, BigDecimal balance) {
        this.iban = iban;
        this.clientId = clientId;
        this.balance = balance;
    }

    /**
     * Factory Method per la creazione con validazione degli invarianti.
     */
    public static BankAccount create(String iban, Long clientId, BigDecimal balance) {
        if (iban == null || iban.trim().isEmpty()) {
            throw new IllegalArgumentException("L'IBAN non può essere vuoto.");
        }
        String cleanIban = iban.trim().toUpperCase();
        if (!IBAN_PATTERN.matcher(cleanIban).matches()) {
            throw new IllegalArgumentException("Formato IBAN non valido: " + iban);
        }
        if (clientId == null) {
            throw new IllegalArgumentException("L'ID del cliente associato non può essere nullo.");
        }
        if (balance == null || balance.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Il saldo del conto non può essere negativo.");
        }
        return new BankAccount(cleanIban, clientId, balance);
    }

    public static BankAccount createInitial(String iban, Long clientId) {
        return create(iban, clientId, BigDecimal.ZERO);
    }

    public String getIban() {
        return iban;
    }

    public Long getClientId() {
        return clientId;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void deposit(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("L'importo da depositare deve essere positivo.");
        }
        this.balance = this.balance.add(amount);
    }

    public void withdraw(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("L'importo da prelevare deve essere positivo.");
        }
        if (this.balance.compareTo(amount) < 0) {
            throw new IllegalStateException("Saldo insufficiente sul conto per completare l'operazione.");
        }
        this.balance = this.balance.subtract(amount);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BankAccount that)) return false;
        return Objects.equals(iban, that.iban);
    }

    @Override
    public int hashCode() {
        return Objects.hash(iban);
    }

    @Override
    public String toString() {
        return "BankAccount{" +
                "iban='" + iban + '\'' +
                ", clientId=" + clientId +
                ", balance=" + balance +
                '}';
    }
}

