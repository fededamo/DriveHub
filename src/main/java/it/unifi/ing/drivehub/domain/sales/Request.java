package it.unifi.ing.drivehub.domain.sales;

import it.unifi.ing.drivehub.domain.BaseEntity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

/**
 * Richiesta di acquisto (deposit > 0) o di vendita veicolo del cliente (deposit < 0).
 */
public final class Request extends BaseEntity {
    private final long vehicleId;
    private final long clientId;
    private Long salesmanId;
    private final BigDecimal deposit;
    private LocalDate expDate;

    public Request(Long id, long vehicleId, long clientId, Long salesmanId, BigDecimal deposit, LocalDate expDate) {
        super(id);
        if (vehicleId <= 0) {
            throw new IllegalArgumentException("vehicleId must be positive");
        }
        if (clientId <= 0) {
            throw new IllegalArgumentException("clientId must be positive");
        }
        this.vehicleId = vehicleId;
        this.clientId = clientId;
        this.salesmanId = salesmanId;
        this.deposit = Objects.requireNonNull(deposit, "deposit");
        if (this.deposit.compareTo(BigDecimal.ZERO) == 0) {
            throw new IllegalArgumentException("deposit cannot be zero");
        }
        this.expDate = expDate;
    }

    public static Request createPurchase(long vehicleId, long clientId, BigDecimal deposit) {
        if (deposit.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Purchase deposit must be strictly positive");
        }
        return new Request(null, vehicleId, clientId, null, deposit, null);
    }

    public static Request createSaleProposal(long vehicleId, long clientId, BigDecimal estimatedValue) {
        if (estimatedValue.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Estimated value must be strictly positive");
        }
        return new Request(null, vehicleId, clientId, null, estimatedValue.negate(), null);
    }

    public static Request create(long vehicleId, long clientId, BigDecimal deposit) {
        return new Request(null, vehicleId, clientId, null, deposit, null);
    }

    public long vehicleId() {
        return vehicleId;
    }

    public long clientId() {
        return clientId;
    }

    public Long salesmanId() {
        return salesmanId;
    }

    public BigDecimal deposit() {
        return deposit;
    }

    public LocalDate expDate() {
        return expDate;
    }

    public boolean isPurchase() {
        return deposit.compareTo(BigDecimal.ZERO) > 0;
    }

    public boolean isSaleProposal() {
        return deposit.compareTo(BigDecimal.ZERO) < 0;
    }

    public void authorize(long salesmanId, LocalDate expDate) {
        this.salesmanId = salesmanId;
        this.expDate = Objects.requireNonNull(expDate, "expDate");
    }

    public void setExpDate(LocalDate expDate) {
        this.expDate = expDate;
    }

    public void setSalesmanId(Long salesmanId) {
        this.salesmanId = salesmanId;
    }

    public void unlock(LocalDate expDate) {
        this.expDate = Objects.requireNonNull(expDate, "expDate");
    }

    public boolean isUnlocked() {
        return expDate != null;
    }
}
