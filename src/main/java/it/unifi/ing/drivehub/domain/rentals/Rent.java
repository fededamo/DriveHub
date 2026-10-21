package it.unifi.ing.drivehub.domain.rentals;

import it.unifi.ing.drivehub.domain.BaseEntity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

/**
 * Noleggio di un veicolo da parte di un cliente.
 */
public final class Rent extends BaseEntity {
    private final long vehicleId;
    private final long clientId;
    private final BigDecimal amount;
    private final LocalDate start;
    private final LocalDate end;

    public Rent(Long id, long vehicleId, long clientId, BigDecimal amount,
                LocalDate start, LocalDate end) {
        super(id);
        if (vehicleId <= 0) {
            throw new IllegalArgumentException("vehicleId must be positive");
        }
        if (clientId <= 0) {
            throw new IllegalArgumentException("clientId must be positive");
        }
        this.vehicleId = vehicleId;
        this.clientId = clientId;
        this.amount = Objects.requireNonNull(amount, "amount");
        if (this.amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("amount cannot be negative");
        }
        this.start = Objects.requireNonNull(start, "start");
        this.end = Objects.requireNonNull(end, "end");
        if (end.isBefore(start)) {
            throw new IllegalArgumentException("end date cannot be before start date");
        }
    }

    public static Rent create(long vehicleId, long clientId, BigDecimal amount,
                              LocalDate start, LocalDate end) {
        return new Rent(null, vehicleId, clientId, amount, start, end);
    }

    public long vehicleId() {
        return vehicleId;
    }

    public long clientId() {
        return clientId;
    }

    public BigDecimal amount() {
        return amount;
    }

    public LocalDate start() {
        return start;
    }

    public LocalDate startDate() {
        return start;
    }

    public LocalDate end() {
        return end;
    }

    public LocalDate endDate() {
        return end;
    }
}
