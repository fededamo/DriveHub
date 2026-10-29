package it.unifi.drivehub.domain.operation;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Entità Sale (registro contabile di acquisti, vendite e penali).
 * Regole di dominio:
 * - Acquisti: saldo negativo (denaro in uscita per il cliente/entrata per l'azienda)
 * - Vendite auto cliente all'azienda: importo positivo accreditato al cliente
 * - Penali: importo trattenuto dall'azienda per mancato pagamento entro scadenza (senza veicolo né venditore)
 * - La data non può essere nel futuro.
 */
public final class Sale {

    private final Long id;
    private final Long vehicleId;
    private final Long salesmanId;
    private final Long clientId;
    private final BigDecimal amount;
    private final LocalDateTime date;

    private Sale(Long id, Long vehicleId, Long salesmanId, Long clientId, BigDecimal amount, LocalDateTime date) {
        this.id = id;
        this.vehicleId = vehicleId;
        this.salesmanId = salesmanId;
        this.clientId = clientId;
        this.amount = amount;
        this.date = date;
    }

    public static Sale create(Long id, Long vehicleId, Long salesmanId, Long clientId, BigDecimal amount, LocalDateTime date) {
        if (clientId == null) {
            throw new IllegalArgumentException("L'ID del cliente non può essere nullo.");
        }
        if (amount == null) {
            throw new IllegalArgumentException("L'importo non può essere nullo.");
        }
        if (date == null) {
            throw new IllegalArgumentException("La data della transazione non può essere nulla.");
        }
        if (date.isAfter(LocalDateTime.now().plusMinutes(2))) {
            throw new IllegalArgumentException("La data di vendita o acquisto di un veicolo non può essere nel futuro.");
        }
        return new Sale(id, vehicleId, salesmanId, clientId, amount, date);
    }

    public static Sale createPurchaseRecord(Long vehicleId, Long salesmanId, Long clientId, BigDecimal vehiclePrice) {
        // Acquisto veicolo da parte del cliente
        return create(null, vehicleId, salesmanId, clientId, vehiclePrice, LocalDateTime.now());
    }

    public static Sale createAcquisitionRecord(Long vehicleId, Long salesmanId, Long clientId, BigDecimal pricePaidToClient) {
        // Concessionaria acquista auto dal cliente (saldo positivo per il cliente)
        return create(null, vehicleId, salesmanId, clientId, pricePaidToClient, LocalDateTime.now());
    }

    public static Sale createPenaltyRecord(Long clientId, BigDecimal penaltyAmount) {
        // Penale: nessun veicolo né venditore associato
        return create(null, null, null, clientId, penaltyAmount, LocalDateTime.now());
    }

    public Long getId() {
        return id;
    }

    public Long getVehicleId() {
        return vehicleId;
    }

    public Long getSalesmanId() {
        return salesmanId;
    }

    public Long getClientId() {
        return clientId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public LocalDateTime getDate() {
        return date;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Sale sale)) return false;
        return id != null && Objects.equals(id, sale.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Sale{" +
                "id=" + id +
                ", vehicleId=" + vehicleId +
                ", salesmanId=" + salesmanId +
                ", clientId=" + clientId +
                ", amount=" + amount +
                ", date=" + date +
                '}';
    }
}

