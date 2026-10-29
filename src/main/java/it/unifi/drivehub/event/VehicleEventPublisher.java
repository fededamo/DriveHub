package it.unifi.drivehub.event;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Soggetto osservabile (Subject nel pattern Observer GoF).
 * Notifica gli osservatori registrati quando si verificano eventi sui veicoli.
 * Thread-safe tramite CopyOnWriteArrayList e resiliente alle eccezioni degli handler.
 */
public class VehicleEventPublisher {

    private final List<VehicleStateObserver> observers = new CopyOnWriteArrayList<>();

    public void attach(VehicleStateObserver observer) {
        if (observer != null && !observers.contains(observer)) {
            observers.add(observer);
        }
    }

    public void detach(VehicleStateObserver observer) {
        if (observer != null) {
            observers.remove(observer);
        }
    }

    public void publishStateChange(VehicleStateChangeEvent event) {
        for (VehicleStateObserver observer : observers) {
            try {
                observer.onVehicleStateChanged(event);
            } catch (Exception e) {
                System.err.println("Avviso: Errore durante la notifica dell'osservatore: " + e.getMessage());
            }
        }
    }
}

