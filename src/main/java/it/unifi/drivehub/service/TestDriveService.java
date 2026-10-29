package it.unifi.drivehub.service;

import it.unifi.drivehub.dao.DatabaseManager;
import it.unifi.drivehub.dao.TestDriveDao;
import it.unifi.drivehub.dao.VehicleDao;
import it.unifi.drivehub.domain.operation.TestDrive;
import it.unifi.drivehub.domain.vehicle.Vehicle;
import it.unifi.drivehub.domain.vehicle.VehicleState;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Servizio per la prenotazione e gestione dei Test Drive (Pagina 3 degli appunti).
 */
public final class TestDriveService {

    private final DatabaseManager db;
    private final TestDriveDao testDriveDao;
    private final VehicleDao vehicleDao;

    public TestDriveService(DatabaseManager db, TestDriveDao testDriveDao, VehicleDao vehicleDao) {
        this.db = db;
        this.testDriveDao = testDriveDao;
        this.vehicleDao = vehicleDao;
    }

    /**
     * Prenota un giro di prova.
     * Regole di business:
     * - Il veicolo deve essere in stato SALE o RENT
     * - Il cliente non può avere più di 3 test drive prenotati/attivi
     * - Orario compreso nella fascia 9:00 - 16:00
     * - Controllo disponibilità oraria e assenza di noleggio nello stesso giorno
     * - Controllo ed esecuzione nella stessa transazione per garantire l'accesso concorrente
     */
    public TestDrive bookTestDrive(Long vehicleId, Long clientId, LocalDateTime dateTime) {
        if (dateTime.isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Impossibile prenotare un test drive nel passato.");
        }

        return db.executeInTransactionWithResult(conn -> {
            // Controllo concorrente atomico: massimo 3 test drive attivi per il cliente
            int activeCount = testDriveDao.countActiveByClientId(clientId, conn);
            if (activeCount >= 3) {
                throw new IllegalStateException("Hai già raggiunto il limite massimo di 3 test drive prenotati.");
            }

            Vehicle vehicle = vehicleDao.findById(vehicleId, conn)
                    .orElseThrow(() -> new IllegalStateException("Veicolo non trovato: " + vehicleId));
            if (vehicle.getState() != VehicleState.SALE && vehicle.getState() != VehicleState.RENT) {
                throw new IllegalStateException("I test drive possono essere prenotati solo su veicoli in vendita o a noleggio.");
            }

            // Verifica disponibilità concorrente
            boolean available = testDriveDao.isVehicleAvailableForTestDrive(vehicleId, dateTime, conn);
            if (!available) {
                throw new IllegalStateException("Fascia oraria non disponibile: il veicolo è già prenotato o noleggiato per questa data.");
            }

            TestDrive td = TestDrive.create(null, vehicleId, clientId, dateTime);
            return testDriveDao.save(td, conn);
        });
    }

    public List<TestDrive> getClientTestDrives(Long clientId) {
        return testDriveDao.findByClientId(clientId);
    }

    public void cancelTestDrive(Long testDriveId, Long clientId) {
        db.executeInTransaction(conn -> {
            TestDrive td = testDriveDao.findById(testDriveId, conn)
                    .filter(t -> t.getClientId().equals(clientId))
                    .orElseThrow(() -> new IllegalArgumentException("Test drive non trovato per l'utente specificato."));
            testDriveDao.delete(td.getId(), conn);
        });
    }
}

