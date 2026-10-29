package it.unifi.drivehub.event;

import java.util.ArrayList;
import java.util.List;

/**
 * Soggetto osservabile (Subject nel pattern Observer GoF).
 * Notifica gli osservatori registrati quando si verificano eventi sui veicoli.
 */
public class VehicleEventPublisher {

    private final List<VehicleStateObserver> observers = new ArrayList<>();

    public void attach(VehicleStateObserver observer) {
        if (observer != null && !observers.contains(observer)) {
            observers.add(observer);
        }
    }

    public void detach(VehicleStateObserver observer) {
        observers.remove(observer);
    }

    public void publishStateChange(VehicleStateChangeEvent event) {
        for (VehicleStateObserver observer : observers) {
            observer.onVehicleStateChanged(event);
        }
    }
}

