package it.unifi.ing.drivehub.dao.postgres;

import it.unifi.ing.drivehub.dao.interfaces.TestDriveDao;
import it.unifi.ing.drivehub.domain.rentals.TestDrive;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

final class PostgresTestDriveDao extends AbstractPostgresDao implements TestDriveDao {

    PostgresTestDriveDao(Connection connection, JdbcEntityLoader loader) {
        super(connection, loader);
    }

    @Override
    public TestDrive save(TestDrive testDrive) {
        if (testDrive.id() != null) {
            throw new IllegalArgumentException("test drive is already persistent");
        }
        long id = execute("Could not save test drive", () -> JdbcSupport.insert(connection,
                "INSERT INTO \"test_drive\" (vehicle, client, date) VALUES (?, ?, ?)",
                stmt -> {
                    stmt.setLong(1, testDrive.vehicleId());
                    stmt.setLong(2, testDrive.clientId());
                    stmt.setTimestamp(3, Timestamp.valueOf(testDrive.dateTime()));
                }));
        testDrive.assignId(id);
        return testDrive;
    }

    @Override
    public Optional<TestDrive> findById(long id) {
        return loader.testDrive(id);
    }

    @Override
    public List<TestDrive> findByClientId(long clientId) {
        return execute("Could not find test drives by client", () -> {
            try (PreparedStatement stmt = connection.prepareStatement(
                    "SELECT id, vehicle, client, date FROM \"test_drive\" WHERE client = ? ORDER BY date DESC")) {
                stmt.setLong(1, clientId);
                try (ResultSet rs = stmt.executeQuery()) {
                    List<TestDrive> list = new ArrayList<>();
                    while (rs.next()) {
                        list.add(mapRow(rs));
                    }
                    return List.copyOf(list);
                }
            }
        });
    }

    @Override
    public List<TestDrive> findByVehicleId(long vehicleId) {
        return execute("Could not find test drives by vehicle", () -> {
            try (PreparedStatement stmt = connection.prepareStatement(
                    "SELECT id, vehicle, client, date FROM \"test_drive\" WHERE vehicle = ? ORDER BY date DESC")) {
                stmt.setLong(1, vehicleId);
                try (ResultSet rs = stmt.executeQuery()) {
                    List<TestDrive> list = new ArrayList<>();
                    while (rs.next()) {
                        list.add(mapRow(rs));
                    }
                    return List.copyOf(list);
                }
            }
        });
    }

    @Override
    public List<TestDrive> findAll() {
        return execute("Could not find all test drives", () -> {
            try (PreparedStatement stmt = connection.prepareStatement(
                    "SELECT id, vehicle, client, date FROM \"test_drive\" ORDER BY date DESC")) {
                try (ResultSet rs = stmt.executeQuery()) {
                    List<TestDrive> list = new ArrayList<>();
                    while (rs.next()) {
                        list.add(mapRow(rs));
                    }
                    return List.copyOf(list);
                }
            }
        });
    }

    @Override
    public int countActiveByClientId(long clientId, LocalDateTime asOfDate) {
        return execute("Could not count active test drives for client", () -> {
            try (PreparedStatement stmt = connection.prepareStatement(
                    "SELECT COUNT(*) FROM \"test_drive\" WHERE client = ? AND date >= ?")) {
                stmt.setLong(1, clientId);
                stmt.setTimestamp(2, Timestamp.valueOf(asOfDate));
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return rs.getInt(1);
                    }
                    return 0;
                }
            }
        });
    }

    @Override
    public boolean hasConflictingTestDrive(long vehicleId, LocalDateTime dateTime) {
        return execute("Could not check conflicting test drive", () -> {
            try (PreparedStatement stmt = connection.prepareStatement(
                    "SELECT 1 FROM \"test_drive\" WHERE vehicle = ? AND date = ? LIMIT 1")) {
                stmt.setLong(1, vehicleId);
                stmt.setTimestamp(2, Timestamp.valueOf(dateTime));
                try (ResultSet rs = stmt.executeQuery()) {
                    return rs.next();
                }
            }
        });
    }

    @Override
    public List<TestDrive> findFutureTestDrivesForVehicle(long vehicleId, LocalDateTime asOfDate) {
        return execute("Could not find future test drives", () -> {
            try (PreparedStatement stmt = connection.prepareStatement(
                    "SELECT id, vehicle, client, date FROM \"test_drive\" WHERE vehicle = ? AND date >= ?")) {
                stmt.setLong(1, vehicleId);
                stmt.setTimestamp(2, Timestamp.valueOf(asOfDate));
                try (ResultSet rs = stmt.executeQuery()) {
                    List<TestDrive> list = new ArrayList<>();
                    while (rs.next()) {
                        list.add(mapRow(rs));
                    }
                    return List.copyOf(list);
                }
            }
        });
    }

    @Override
    public void delete(long testDriveId) {
        execute("Could not delete test drive", () -> {
            try (PreparedStatement stmt = connection.prepareStatement(
                    "DELETE FROM \"test_drive\" WHERE id = ?")) {
                stmt.setLong(1, testDriveId);
                stmt.executeUpdate();
                return null;
            }
        });
    }

    @Override
    public void cancelFutureTestDrivesForVehicle(long vehicleId, LocalDateTime asOfDate) {
        execute("Could not cancel future test drives for vehicle", () -> {
            try (PreparedStatement stmt = connection.prepareStatement(
                    "DELETE FROM \"test_drive\" WHERE vehicle = ? AND date >= ?")) {
                stmt.setLong(1, vehicleId);
                stmt.setTimestamp(2, Timestamp.valueOf(asOfDate));
                stmt.executeUpdate();
                return null;
            }
        });
    }

    @Override
    public void cancelTestDrivesInRange(long vehicleId, LocalDateTime from, LocalDateTime to) {
        execute("Could not cancel test drives in range for vehicle", () -> {
            try (PreparedStatement stmt = connection.prepareStatement(
                    "DELETE FROM \"test_drive\" WHERE vehicle = ? AND date >= ? AND date <= ?")) {
                stmt.setLong(1, vehicleId);
                stmt.setTimestamp(2, Timestamp.valueOf(from));
                stmt.setTimestamp(3, Timestamp.valueOf(to));
                stmt.executeUpdate();
                return null;
            }
        });
    }

    @Override
    public boolean hasOperationsForVehicle(long vehicleId) {
        return execute("Could not check test drive operations for vehicle", () -> {
            try (PreparedStatement stmt = connection.prepareStatement(
                    "SELECT 1 FROM \"test_drive\" WHERE vehicle = ? LIMIT 1")) {
                stmt.setLong(1, vehicleId);
                try (ResultSet rs = stmt.executeQuery()) {
                    return rs.next();
                }
            }
        });
    }

    @Override
    public boolean hasOperationsForClient(long clientId) {
        return execute("Could not check test drive operations for client", () -> {
            try (PreparedStatement stmt = connection.prepareStatement(
                    "SELECT 1 FROM \"test_drive\" WHERE client = ? LIMIT 1")) {
                stmt.setLong(1, clientId);
                try (ResultSet rs = stmt.executeQuery()) {
                    return rs.next();
                }
            }
        });
    }

    private static TestDrive mapRow(ResultSet rs) throws SQLException {
        Timestamp ts = rs.getTimestamp("date");
        LocalDateTime dt = ts != null ? ts.toLocalDateTime() : null;
        return new TestDrive(
                rs.getLong("id"),
                rs.getLong("vehicle"),
                rs.getLong("client"),
                dt);
    }
}
