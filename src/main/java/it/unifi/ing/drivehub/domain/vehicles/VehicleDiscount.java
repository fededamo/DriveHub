package it.unifi.ing.drivehub.domain.vehicles;

public final class VehicleDiscount {
    private final long vehicleId;
    private final int value;

    public VehicleDiscount(long vehicleId, int value) {
        if (vehicleId <= 0) {
            throw new IllegalArgumentException("vehicleId must be positive");
        }
        if (value < 0 || value > 50) {
            throw new IllegalArgumentException("discount value must be between 0 and 50");
        }
        this.vehicleId = vehicleId;
        this.value = value;
    }

    public long vehicleId() {
        return vehicleId;
    }

    public int value() {
        return value;
    }
}
