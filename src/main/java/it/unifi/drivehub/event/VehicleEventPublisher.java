package it.unifi.drivehub.event;

import java.sql.Connection;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Soggetto osservabile (Subject nel pattern Observer GoF).
 * Notifica gli osservatori registrati quando si verificano eventi sui veicoli.
 * Notifica sincrona nella transazione del chiamante: ogni errore deve impedirne il commit.
 */
public class VehicleEventPublisher {

    private final List<VehicleCommitmentObserver> observers = new CopyOnWriteArrayList<>();

    public void attach(VehicleCommitmentObserver observer) {
        if (observer != null && !observers.contains(observer)) {
            observers.add(observer);
        }
    }

    public void detach(VehicleCommitmentObserver observer) {
        if (observer != null) {
            observers.remove(observer);
        }
    }

    public void publishCommitment(VehicleCommitmentEvent event, Connection conn) {
        for (VehicleCommitmentObserver observer : observers) {
            observer.onVehicleCommitted(event, conn);
        }
    }
}
