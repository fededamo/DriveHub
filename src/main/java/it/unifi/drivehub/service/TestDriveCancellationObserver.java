package it.unifi.drivehub.service;

import it.unifi.drivehub.dao.DatabaseManager;
import it.unifi.drivehub.dao.TestDriveDao;
import it.unifi.drivehub.domain.vehicle.VehicleState;
import it.unifi.drivehub.event.VehicleStateChangeEvent;
import it.unifi.drivehub.event.VehicleStateObserver;

/**
 * Observer che garantisce la priorità commerciale di vendita e noleggio rispetto ai Test Drive.
 * Come specificato negli appunti (Pagina 3):
 * "Se un veicolo prenotato per un test drive viene venduto o noleggiato, tramite un observer
 * questo evento deve essere comunicato al sistema, in modo che le prenotazioni future vengano annullate.
 * Quelli già avvenuti devono rimanere come storico."
 */
public final class TestDriveCancellationObserver implements VehicleStateObserver {

    private final DatabaseManager db;
    private final TestDriveDao testDriveDao;

    public TestDriveCancellationObserver(DatabaseManager db, TestDriveDao testDriveDao) {
        this.db = db;
        this.testDriveDao = testDriveDao;
    }

    @Override
    public void onVehicleStateChanged(VehicleStateChangeEvent event) {
        if (event.newState() == VehicleState.SOLD || event.newState() == VehicleState.RENT) {
            db.executeInTransaction(conn -> {
                int cancelled = testDriveDao.deleteFutureByVehicleId(event.vehicleId(), conn);
                if (cancelled > 0) {
                    System.out.println("[Observer] Annullati " + cancelled + " test drive futuri per il veicolo " + event.vehicleId() + " a seguito di passaggio a stato " + event.newState());
                }
            });
        }
    }
}

