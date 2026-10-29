package it.unifi.drivehub.domain.discount;

import java.util.Objects;

/**
 * Entità Sconto Veicolo.
 * Invariante: percentuale compresa tra 0 e 50 (ereditata da Discount).
 */
public final class VehicleDiscount extends Discount {

    private final Long vehicleId;

    private VehicleDiscount(Long vehicleId, int value) {
        super(value);
        this.vehicleId = vehicleId;
    }

    public static VehicleDiscount create(Long vehicleId, int value) {
        if (vehicleId == null) {
            throw new IllegalArgumentException("L'ID del veicolo non può essere nullo.");
        }
        return new VehicleDiscount(vehicleId, value);
    }

    public Long getVehicleId() {
        return vehicleId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof VehicleDiscount that)) return false;
        return Objects.equals(vehicleId, that.vehicleId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(vehicleId);
    }

    @Override
    public String toString() {
        return "VehicleDiscount{" + "vehicleId=" + vehicleId + ", value=" + value + "%}";
    }
}
