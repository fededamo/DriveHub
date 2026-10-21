package it.unifi.ing.drivehub.domain.observer;

import it.unifi.ing.drivehub.domain.vehicles.VehicleState;

import java.time.Instant;
import java.util.Objects;

public record InventoryEvent(
        InventoryEventType type,
        Long vehicleId,
        String plate,
        VehicleState previousStatus,
        VehicleState currentStatus,
        Instant occurredAt
) {
    public InventoryEvent {
        Objects.requireNonNull(type, "type");
        Objects.requireNonNull(occurredAt, "occurredAt");
    }

    public static InventoryEvent statusChanged(Long vehicleId, String plate, VehicleState prev, VehicleState curr) {
        return new InventoryEvent(InventoryEventType.STATUS_CHANGED, vehicleId, plate, prev, curr, Instant.now());
    }

    public static InventoryEvent requestExpired(Long vehicleId, String plate) {
        return new InventoryEvent(InventoryEventType.REQUEST_EXPIRED, vehicleId, plate, VehicleState.BLOCKED, VehicleState.SALE, Instant.now());
    }

    public static InventoryEvent of(InventoryEventType type, Long vehicleId, String plate) {
        return new InventoryEvent(type, vehicleId, plate, null, null, Instant.now());
    }
}
