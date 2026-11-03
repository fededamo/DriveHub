package it.unifi.drivehub.event;

/** Conferma di una vendita o di un noleggio, pubblicata prima del commit. */
public record VehicleCommitmentEvent(Long vehicleId) {}
