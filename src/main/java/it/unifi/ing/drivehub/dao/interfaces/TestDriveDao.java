package it.unifi.ing.drivehub.dao.interfaces;

import it.unifi.ing.drivehub.domain.rentals.TestDrive;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface TestDriveDao {
    TestDrive save(TestDrive testDrive);
    Optional<TestDrive> findById(long id);
    List<TestDrive> findByClientId(long clientId);
    List<TestDrive> findByVehicleId(long vehicleId);
    List<TestDrive> findAll();
    int countActiveByClientId(long clientId, LocalDateTime asOfDate);
    boolean hasConflictingTestDrive(long vehicleId, LocalDateTime dateTime);
    List<TestDrive> findFutureTestDrivesForVehicle(long vehicleId, LocalDateTime asOfDate);
    void delete(long testDriveId);
    void cancelFutureTestDrivesForVehicle(long vehicleId, LocalDateTime asOfDate);
    void cancelTestDrivesInRange(long vehicleId, LocalDateTime from, LocalDateTime to);
    boolean hasOperationsForVehicle(long vehicleId);
    boolean hasOperationsForClient(long clientId);
}
