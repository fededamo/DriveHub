package it.unifi.drivehub.event;

/**
 * Observer per intercettare i cambiamenti di stato di un veicolo.
 * Pattern Observer (GoF).
 */
public interface VehicleStateObserver {
    void onVehicleStateChanged(VehicleStateChangeEvent event);
}

