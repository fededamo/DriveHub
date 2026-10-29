package it.unifi.drivehub.domain.operation;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

/**
 * Entità Noleggio (Rent).
 * Invarianti:
 * - Importo non negativo
 * - Data fine >= data inizio
 */
public final class Rent {

    private final Long id;
    private final Long vehicleId;
    private final Long clientId;
    private final BigDecimal amount;
    private final LocalDate startDate;
    private final LocalDate endDate;

    private Rent(Long id, Long vehicleId, Long clientId, BigDecimal amount, LocalDate startDate, LocalDate endDate) {
        this.id = id;
        this.vehicleId = vehicleId;
        this.clientId = clientId;
        this.amount = amount;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public static Rent create(Long id, Long vehicleId, Long clientId, BigDecimal amount, LocalDate startDate, LocalDate endDate) {
        if (vehicleId == null) {
            throw new IllegalArgumentException("L'ID del veicolo non può essere nullo.");
        }
        if (clientId == null) {
            throw new IllegalArgumentException("L'ID del cliente non può essere nullo.");
        }
        if (amount == null || amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Il pagamento del noleggio non può essere negativo.");
        }
        if (startDate == null || endDate == null) {
            throw new IllegalArgumentException("Le date di inizio e fine noleggio sono obbligatorie.");
        }
        if (endDate.isBefore(startDate)) {
            throw new IllegalArgumentException("La data di fine noleggio non può essere precedente alla data di inizio.");
        }
        return new Rent(id, vehicleId, clientId, amount, startDate, endDate);
    }

    public Long getId() {
        return id;
    }

    public Long getVehicleId() {
        return vehicleId;
    }

    public Long getClientId() {
        return clientId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public boolean overlapsWith(LocalDate otherStart, LocalDate otherEnd) {
        return !this.startDate.isAfter(otherEnd) && !this.endDate.isBefore(otherStart);
    }

    public boolean isFuture() {
        return startDate.isAfter(LocalDate.now());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Rent rent)) return false;
        return Objects.equals(id, rent.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Rent{" +
                "id=" + id +
                ", vehicleId=" + vehicleId +
                ", clientId=" + clientId +
                ", amount=" + amount +
                ", start=" + startDate +
                ", end=" + endDate +
                '}';
    }
}

