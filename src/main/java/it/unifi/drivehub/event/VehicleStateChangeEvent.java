package it.unifi.drivehub.event;

import it.unifi.drivehub.domain.vehicle.VehicleState;

/**
 * Evento emesso quando lo stato di un veicolo subisce una transizione.
 */
public record VehicleStateChangeEvent(
        Long vehicleId,
        VehicleState oldState,
        VehicleState newState
) {}

