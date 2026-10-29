package it.unifi.drivehub.domain.operation;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

/**
 * Entità Richiesta (acquisto o vendita veicolo).
 * Invarianti:
 * - Tipo obbligatorio (PURCHASE o SALE)
 * - La data di scadenza (se valorizzata) non può essere nel passato rispetto alla creazione
 */
public final class Request {

    private final Long id;
    private final Long vehicleId;
    private final Long clientId;
    private Long salesmanId;
    private BigDecimal deposit;
    private LocalDate expDate;
    private final RequestType type;

    private Request(Long id, Long vehicleId, Long clientId, Long salesmanId, BigDecimal deposit, LocalDate expDate, RequestType type) {
        this.id = id;
        this.vehicleId = vehicleId;
        this.clientId = clientId;
        this.salesmanId = salesmanId;
        this.deposit = deposit;
        this.expDate = expDate;
        this.type = type;
    }

    public static Request create(Long id, Long vehicleId, Long clientId, Long salesmanId, BigDecimal deposit, LocalDate expDate, RequestType type) {
        if (vehicleId == null) {
            throw new IllegalArgumentException("L'ID del veicolo non può essere nullo.");
        }
        if (clientId == null) {
            throw new IllegalArgumentException("L'ID del cliente non può essere nullo.");
        }
        if (deposit == null) {
            throw new IllegalArgumentException("L'acconto/stima non può essere nullo.");
        }
        if (type == null) {
            throw new IllegalArgumentException("Il tipo di richiesta non può essere nullo.");
        }
        return new Request(id, vehicleId, clientId, salesmanId, deposit, expDate, type);
    }

    public static Request createNewPurchaseRequest(Long vehicleId, Long clientId, BigDecimal deposit) {
        return create(null, vehicleId, clientId, null, deposit, null, RequestType.PURCHASE);
    }

    public static Request createNewSaleRequest(Long vehicleId, Long clientId, BigDecimal estimatedValue) {
        // Valore negativo per indicare la proposta di vendita da parte del cliente
        BigDecimal signedDeposit = estimatedValue.compareTo(BigDecimal.ZERO) > 0 ? estimatedValue.negate() : estimatedValue;
        return create(null, vehicleId, clientId, null, signedDeposit, null, RequestType.SALE);
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

    public Long getSalesmanId() {
        return salesmanId;
    }

    public void setSalesmanId(Long salesmanId) {
        this.salesmanId = salesmanId;
    }

    public BigDecimal getDeposit() {
        return deposit;
    }

    public void setDeposit(BigDecimal deposit) {
        this.deposit = deposit;
    }

    public LocalDate getExpDate() {
        return expDate;
    }

    public void setExpDate(LocalDate expDate) {
        if (expDate != null && expDate.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("La data di scadenza della richiesta non può essere nel passato.");
        }
        this.expDate = expDate;
    }

    public RequestType getType() {
        return type;
    }

    public boolean isExpired() {
        return expDate != null && expDate.isBefore(LocalDate.now());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Request request)) return false;
        return Objects.equals(id, request.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Request{" +
                "id=" + id +
                ", vehicleId=" + vehicleId +
                ", clientId=" + clientId +
                ", salesmanId=" + salesmanId +
                ", deposit=" + deposit +
                ", expDate=" + expDate +
                ", type=" + type +
                '}';
    }
}

