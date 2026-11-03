package it.unifi.drivehub.service;

import it.unifi.drivehub.dao.TestDriveDao;
import it.unifi.drivehub.event.VehicleCommitmentEvent;
import it.unifi.drivehub.event.VehicleCommitmentObserver;

import java.sql.Connection;

/**
 * Applica la priorità commerciale di vendita e noleggio rispetto ai test drive.
 * Conserva lo storico e cancella tutti i futuri nella transazione dell'operazione,
 * anche fuori dal periodo noleggiato. Un errore impedisce il commit del chiamante.
 */
public final class TestDriveCancellationObserver implements VehicleCommitmentObserver {

    private final TestDriveDao testDriveDao;

    public TestDriveCancellationObserver(TestDriveDao testDriveDao) {
        this.testDriveDao = testDriveDao;
    }

    @Override
    public void onVehicleCommitted(VehicleCommitmentEvent event, Connection conn) {
        testDriveDao.deleteFutureByVehicleId(event.vehicleId(), conn);
    }
}
