package it.unifi.drivehub.dao;

import it.unifi.drivehub.domain.operation.TestDrive;

import java.sql.Connection;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Interfaccia DAO per i Test Drive.
 */
public interface TestDriveDao {

    List<TestDrive> findAll();

    List<TestDrive> findByClientId(Long clientId);

    List<TestDrive> findByVehicleId(Long vehicleId);

    List<TestDrive> findFutureByVehicleId(Long vehicleId);

    int countActiveByClientId(Long clientId);

    boolean isVehicleAvailableForTestDrive(Long vehicleId, LocalDateTime date, Connection conn);

    TestDrive save(TestDrive testDrive, Connection conn);

    void delete(Long id, Connection conn);

    int deleteFutureByVehicleId(Long vehicleId, Connection conn);
}

