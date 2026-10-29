package it.unifi.drivehub.domain.discount;

import java.util.Objects;

/**
 * Entità Sconto Veicolo.
 * Invariante: percentuale compresa tra 0 e 50.
 */
public final class VehicleDiscount {

    private final Long vehicleId;
    private final int value;

    private VehicleDiscount(Long vehicleId, int value) {
        this.vehicleId = vehicleId;
        this.value = value;
    }

    public static VehicleDiscount create(Long vehicleId, int value) {
        if (vehicleId == null) {
            throw new IllegalArgumentException("L'ID del veicolo non può essere nullo.");
        }
        if (value < 0 || value > 50) {
            throw new IllegalArgumentException("Lo sconto deve essere compreso tra 0 e 50. Valore inserito: " + value);
        }
        return new VehicleDiscount(vehicleId, value);
    }

    public Long getVehicleId() {
        return vehicleId;
    }

    public int getValue() {
        return value;
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

