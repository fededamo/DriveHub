package it.unifi.ing.drivehub.domain.vehicles;

public enum VehicleState {
    WAITING,
    SALE,
    RENT,
    BLOCKED,
    SOLD;

    public static VehicleState fromString(String value) {
        if (value == null) return null;
        return VehicleState.valueOf(value.trim().toUpperCase(java.util.Locale.ROOT));
    }
}
