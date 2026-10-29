package it.unifi.drivehub.dao.postgres;

import it.unifi.drivehub.dao.DatabaseManager;
import it.unifi.drivehub.dao.TestDriveDao;
import it.unifi.drivehub.domain.operation.TestDrive;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class PostgresTestDriveDao implements TestDriveDao {

    private final DatabaseManager db;

    public PostgresTestDriveDao(DatabaseManager db) {
        this.db = db;
    }

    @Override
    public List<TestDrive> findAll() {
        List<TestDrive> list = new ArrayList<>();
        String sql = "SELECT id, vehicle, client, date FROM test_drive ORDER BY date DESC";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Errore durante il recupero dei test drive.", e);
        }
        return list;
    }

    @Override
    public Optional<TestDrive> findById(Long id) {
        try (Connection conn = db.getConnection()) {
            return findById(id, conn);
        } catch (SQLException e) {
            throw new RuntimeException("Errore di connessione durante la ricerca del test drive per id: " + id, e);
        }
    }

    @Override
    public Optional<TestDrive> findById(Long id, Connection conn) {
        String sql = "SELECT id, vehicle, client, date FROM test_drive WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Errore durante la ricerca del test drive per id: " + id, e);
        }
        return Optional.empty();
    }

    @Override
    public List<TestDrive> findByClientId(Long clientId) {
        List<TestDrive> list = new ArrayList<>();
        String sql = "SELECT id, vehicle, client, date FROM test_drive WHERE client = ? ORDER BY date DESC";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, clientId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Errore durante la ricerca dei test drive per cliente: " + clientId, e);
        }
        return list;
    }

    @Override
    public List<TestDrive> findByVehicleId(Long vehicleId) {
        List<TestDrive> list = new ArrayList<>();
        String sql = "SELECT id, vehicle, client, date FROM test_drive WHERE vehicle = ? ORDER BY date ASC";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, vehicleId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Errore durante la ricerca dei test drive per veicolo: " + vehicleId, e);
        }
        return list;
    }

    @Override
    public List<TestDrive> findFutureByVehicleId(Long vehicleId) {
        List<TestDrive> list = new ArrayList<>();
        String sql = "SELECT id, vehicle, client, date FROM test_drive WHERE vehicle = ? AND date > CURRENT_TIMESTAMP ORDER BY date ASC";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, vehicleId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Errore durante la ricerca dei test drive futuri per veicolo: " + vehicleId, e);
        }
        return list;
    }

    @Override
    public int countActiveByClientId(Long clientId) {
        try (Connection conn = db.getConnection()) {
            return countActiveByClientId(clientId, conn);
        } catch (SQLException e) {
            throw new RuntimeException("Errore di connessione durante il conteggio dei test drive attivi: " + clientId, e);
        }
    }

    @Override
    public int countActiveByClientId(Long clientId, Connection conn) {
        String sql = "SELECT count(*) FROM test_drive WHERE client = ? AND date >= CURRENT_TIMESTAMP";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, clientId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Errore durante il conteggio dei test drive attivi per il cliente: " + clientId, e);
        }
        return 0;
    }

    @Override
    public boolean isVehicleAvailableForTestDrive(Long vehicleId, LocalDateTime date, Connection conn) {
        // 1. Controllo che il veicolo non sia già prenotato per un test drive alla stessa data/ora
        // 2. Controllo che il veicolo non sia noleggiato nel giorno specificato
        String sql = "SELECT (" +
                "NOT EXISTS(SELECT 1 FROM test_drive WHERE vehicle = ? AND date = ?) AND " +
                "NOT EXISTS(SELECT 1 FROM rent WHERE vehicle = ? AND ? BETWEEN start AND \"end\")" +
                ") AS is_available";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, vehicleId);
            ps.setTimestamp(2, Timestamp.valueOf(date));
            ps.setLong(3, vehicleId);
            ps.setDate(4, Date.valueOf(date.toLocalDate()));
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getBoolean("is_available");
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Errore durante la verifica della disponibilità del test drive.", e);
        }
        return false;
    }

    @Override
    public TestDrive save(TestDrive testDrive, Connection conn) {
        String sql = "INSERT INTO test_drive (vehicle, client, date) VALUES (?, ?, ?) RETURNING id";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, testDrive.getVehicleId());
            ps.setLong(2, testDrive.getClientId());
            ps.setTimestamp(3, Timestamp.valueOf(testDrive.getDate()));
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    long id = rs.getLong(1);
                    return TestDrive.create(id, testDrive.getVehicleId(), testDrive.getClientId(), testDrive.getDate());
                }
                throw new SQLException("Inserimento test drive non ha restituito l'ID generato.");
            }
        } catch (SQLException e) {
            throw new RuntimeException("Errore durante il salvataggio del test drive.", e);
        }
    }

    @Override
    public void delete(Long id, Connection conn) {
        String sql = "DELETE FROM test_drive WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Errore durante l'eliminazione del test drive id: " + id, e);
        }
    }

    @Override
    public int deleteFutureByVehicleId(Long vehicleId, Connection conn) {
        String sql = "DELETE FROM test_drive WHERE vehicle = ? AND date > CURRENT_TIMESTAMP";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, vehicleId);
            return ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Errore durante la cancellazione dei test drive futuri per il veicolo: " + vehicleId, e);
        }
    }

    private static TestDrive mapRow(ResultSet rs) throws SQLException {
        return TestDrive.create(
                rs.getLong("id"),
                rs.getLong("vehicle"),
                rs.getLong("client"),
                rs.getTimestamp("date").toLocalDateTime()
        );
    }
}

