package it.unifi.drivehub.domain.operation;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Objects;

/**
 * Entità Richiesta (acquisto o vendita veicolo).
 * Invarianti:
 * - Tipo obbligatorio (PURCHASE o SALE)
 * - SALE: totale concordato e acconto immutabili, non negativi e in centesimi
 * - PURCHASE: stima non negativa in deposit, senza totale concordato
 * - La data di scadenza (se valorizzata) non può essere nel passato rispetto alla creazione
 */
public final class Request {

    private final Long id;
    private final Long vehicleId;
    private final Long clientId;
    private Long salesmanId;
    private final BigDecimal deposit;
    private final BigDecimal agreedTotal;
    private LocalDate expDate;
    private final RequestType type;

    private Request(Long id, Long vehicleId, Long clientId, Long salesmanId, BigDecimal deposit, BigDecimal agreedTotal, LocalDate expDate, RequestType type) {
        this.id = id;
        this.vehicleId = vehicleId;
        this.clientId = clientId;
        this.salesmanId = salesmanId;
        this.deposit = deposit;
        this.agreedTotal = agreedTotal;
        this.expDate = expDate;
        this.type = type;
    }

    public static Request create(Long id, Long vehicleId, Long clientId, Long salesmanId, BigDecimal deposit, BigDecimal agreedTotal, LocalDate expDate, RequestType type) {
        if (vehicleId == null) {
            throw new IllegalArgumentException("L'ID del veicolo non può essere nullo.");
        }
        if (clientId == null) {
            throw new IllegalArgumentException("L'ID del cliente non può essere nullo.");
        }
        if (deposit == null) {
            throw new IllegalArgumentException("L'acconto/stima non può essere nullo.");
        }
        if (deposit.signum() < 0) {
            throw new IllegalArgumentException("L'acconto/stima non può essere negativo.");
        }
        if (type == null) {
            throw new IllegalArgumentException("Il tipo di richiesta non può essere nullo.");
        }
        if (type == RequestType.SALE) {
            if (agreedTotal == null || agreedTotal.signum() < 0
                    || deposit.compareTo(agreedTotal) > 0) {
                throw new IllegalArgumentException("Totale concordato e acconto non validi per l'acquisto.");
            }
            // Gli importi dell'accordo devono essere già espressi in centesimi, come l'addebito.
            agreedTotal = agreedTotal.setScale(2, RoundingMode.UNNECESSARY);
            deposit = deposit.setScale(2, RoundingMode.UNNECESSARY);
        } else if (agreedTotal != null) {
            throw new IllegalArgumentException("Il totale concordato riguarda solo gli acquisti del cliente.");
        }
        return new Request(id, vehicleId, clientId, salesmanId, deposit, agreedTotal, expDate, type);
    }

    public static Request createNewSaleRequest(Long vehicleId, Long clientId, BigDecimal deposit, BigDecimal agreedTotal) {
        return create(null, vehicleId, clientId, null, deposit, agreedTotal, null, RequestType.SALE);
    }

    public static Request createNewPurchaseRequest(Long vehicleId, Long clientId, BigDecimal estimatedValue) {
        return create(null, vehicleId, clientId, null, estimatedValue, null, null, RequestType.PURCHASE);
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

    public BigDecimal getAgreedTotal() {
        return agreedTotal;
    }

    public BigDecimal getRemainingAmount() {
        if (type != RequestType.SALE) {
            throw new IllegalStateException("Il saldo da pagare riguarda solo gli acquisti del cliente.");
        }
        return agreedTotal.subtract(deposit);
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
                ", agreedTotal=" + agreedTotal +
                ", expDate=" + expDate +
                ", type=" + type +
                '}';
    }
}

