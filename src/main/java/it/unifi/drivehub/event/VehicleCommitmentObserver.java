package it.unifi.drivehub.event;

import java.sql.Connection;

/** Observer sincrono: partecipa alla transazione del chiamante e propaga gli errori. */
public interface VehicleCommitmentObserver {
    void onVehicleCommitted(VehicleCommitmentEvent event, Connection conn);
}
