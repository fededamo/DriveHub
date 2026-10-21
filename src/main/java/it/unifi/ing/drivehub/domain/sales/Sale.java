package it.unifi.ing.drivehub.domain.sales;

import it.unifi.ing.drivehub.domain.BaseEntity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Registro compravendite:
 * 1. Vendita al cliente: amount > 0, vehicle != null, salesman != null.
 * 2. Acquisto da cliente: amount < 0 (esborso aziendale), vehicle != null, salesman != null.
 * 3. Penale mancato saldo: amount > 0, vehicle == null, salesman == null.
 */
public final class Sale extends BaseEntity {
    private final Long vehicleId;
    private final Long salesmanId;
    private final long clientId;
    private final BigDecimal amount;
    private final LocalDateTime date;

    public Sale(Long id, Long vehicleId, Long salesmanId, long clientId, BigDecimal amount, LocalDateTime date) {
        super(id);
        if (clientId <= 0) {
            throw new IllegalArgumentException("clientId must be positive");
        }
        this.vehicleId = vehicleId;
        this.salesmanId = salesmanId;
        this.clientId = clientId;
        this.amount = Objects.requireNonNull(amount, "amount");
        this.date = Objects.requireNonNull(date, "date");
    }

    public static Sale record(Long vehicleId, Long salesmanId, long clientId, BigDecimal amount, LocalDateTime date) {
        return new Sale(null, vehicleId, salesmanId, clientId, amount, date);
    }

    public static Sale record(Long vehicleId, Long salesmanId, long clientId, BigDecimal amount, LocalDate date) {
        return new Sale(null, vehicleId, salesmanId, clientId, amount, date.atStartOfDay());
    }

    public static Sale completed(Long vehicleId, long clientId, Long salesmanId, BigDecimal amount, LocalDateTime date) {
        return new Sale(null, vehicleId, salesmanId, clientId, amount, date);
    }

    public static Sale penalty(long clientId, BigDecimal amount, LocalDateTime date) {
        return new Sale(null, null, null, clientId, amount, date);
    }

    public Long vehicleId() {
        return vehicleId;
    }

    public Long salesmanId() {
        return salesmanId;
    }

    public long clientId() {
        return clientId;
    }

    public BigDecimal amount() {
        return amount;
    }

    public LocalDateTime date() {
        return date;
    }

    public LocalDate localDate() {
        return date.toLocalDate();
    }

    public boolean isPenalty() {
        return vehicleId == null && salesmanId == null;
    }

    public boolean isDefaultFailure() {
        return isPenalty();
    }

    public boolean isAcquisition() {
        return amount.compareTo(BigDecimal.ZERO) < 0;
    }
}
