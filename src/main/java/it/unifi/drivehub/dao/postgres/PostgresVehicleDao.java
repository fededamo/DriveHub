package it.unifi.drivehub.dao.postgres;

import it.unifi.drivehub.dao.DatabaseManager;
import it.unifi.drivehub.dao.VehicleDao;
import it.unifi.drivehub.domain.vehicle.Vehicle;
import it.unifi.drivehub.domain.vehicle.PriceMode;
import it.unifi.drivehub.domain.vehicle.VehicleState;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class PostgresVehicleDao implements VehicleDao {

    private final DatabaseManager db;

    public PostgresVehicleDao(DatabaseManager db) {
        this.db = db;
    }

    @Override
    public List<Vehicle> findAll() {
        List<Vehicle> list = new ArrayList<>();
        String sql = "SELECT id, model, vin, licence_plate, kilometers, price, price_mode, year, state::text as state_str FROM vehicle ORDER BY id ASC";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Errore durante il recupero dei veicoli.", e);
        }
        return list;
    }

    @Override
    public List<Vehicle> findByState(VehicleState state) {
        List<Vehicle> list = new ArrayList<>();
        String sql = "SELECT id, model, vin, licence_plate, kilometers, price, price_mode, year, state::text as state_str FROM vehicle WHERE state = ?::vehicle_state ORDER BY id ASC";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, state.name());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Errore durante la ricerca dei veicoli per stato: " + state, e);
        }
        return list;
    }

    @Override
    public Optional<Vehicle> findById(Long id) {
        try (Connection conn = db.getConnection()) {
            return findById(id, conn);
        } catch (SQLException e) {
            throw new RuntimeException("Errore di connessione durante la ricerca del veicolo id: " + id, e);
        }
    }

    @Override
    public Optional<Vehicle> findById(Long id, Connection conn) {
        return findById(id, conn, false);
    }

    @Override
    public Optional<Vehicle> findByIdForUpdate(Long id, Connection conn) {
        return findById(id, conn, true);
    }

    private Optional<Vehicle> findById(Long id, Connection conn, boolean forUpdate) {
        String sql = "SELECT id, model, vin, licence_plate, kilometers, price, price_mode, year, state::text as state_str FROM vehicle WHERE id = ?" + (forUpdate ? " FOR UPDATE" : "");
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            if (forUpdate && conn.getAutoCommit()) throw new IllegalArgumentException("Il lock del veicolo richiede una transazione attiva.");
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Errore durante la ricerca del veicolo id: " + id, e);
        }
        return Optional.empty();
    }

    @Override
    public Optional<Vehicle> findByVin(String vin) {
        try (Connection conn = db.getConnection()) {
            return findByVin(vin, conn);
        } catch (SQLException e) {
            throw new RuntimeException("Errore di connessione durante la ricerca del veicolo per VIN: " + vin, e);
        }
    }

    @Override
    public Optional<Vehicle> findByVin(String vin, Connection conn) {
        return findByVin(vin, conn, false);
    }
    @Override
    public Optional<Vehicle> findByVinForUpdate(String vin, Connection conn) {
        return findByVin(vin, conn, true);
    }
    private Optional<Vehicle> findByVin(String vin, Connection conn, boolean forUpdate) {
        String sql = "SELECT id, model, vin, licence_plate, kilometers, price, price_mode, year, state::text as state_str FROM vehicle WHERE UPPER(vin) = UPPER(?)" + (forUpdate ? " FOR UPDATE" : "");
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            if (forUpdate && conn.getAutoCommit()) throw new IllegalArgumentException("Il lock del veicolo richiede una transazione attiva.");
            ps.setString(1, vin.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Errore durante la ricerca del veicolo per VIN: " + vin, e);
        }
        return Optional.empty();
    }

    @Override
    public Vehicle save(Vehicle vehicle, Connection conn) {
        String sql = "INSERT INTO vehicle (model, vin, licence_plate, kilometers, price, year, state, price_mode) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?::vehicle_state, ?::price_mode) RETURNING id";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, vehicle.getModelId());
            ps.setString(2, vehicle.getVin());
            if (vehicle.getLicencePlate() != null) {
                ps.setString(3, vehicle.getLicencePlate());
            } else {
                ps.setNull(3, Types.VARCHAR);
            }
            ps.setInt(4, vehicle.getKilometers());
            ps.setBigDecimal(5, vehicle.getPrice());
            ps.setInt(6, vehicle.getYear());
            ps.setString(7, vehicle.getState().name());
            ps.setString(8, vehicle.getPriceMode().name());

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    long id = rs.getLong(1);
                    return Vehicle.create(id, vehicle.getModelId(), vehicle.getVin(), vehicle.getLicencePlate(),
                            vehicle.getKilometers(), vehicle.getPrice(), vehicle.getYear(), vehicle.getState(), vehicle.getPriceMode());
                }
                throw new SQLException("Inserimento veicolo non ha restituito l'ID generato.");
            }
        } catch (SQLException e) {
            throw new RuntimeException("Errore durante il salvataggio del veicolo.", e);
        }
    }

    @Override
    public void update(Vehicle vehicle, Connection conn) {
        String sql = "UPDATE vehicle SET model = ?, licence_plate = ?, kilometers = ?, price = ?, state = ?::vehicle_state, price_mode = ?::price_mode " +
                "WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, vehicle.getModelId());
            if (vehicle.getLicencePlate() != null) {
                ps.setString(2, vehicle.getLicencePlate());
            } else {
                ps.setNull(2, Types.VARCHAR);
            }
            ps.setInt(3, vehicle.getKilometers());
            ps.setBigDecimal(4, vehicle.getPrice());
            ps.setString(5, vehicle.getState().name());
            ps.setString(6, vehicle.getPriceMode().name());
            ps.setLong(7, vehicle.getId());
            if (ps.executeUpdate() != 1) throw new SQLException("Veicolo da aggiornare non trovato: " + vehicle.getId());
        } catch (SQLException e) {
            throw new RuntimeException("Errore durante l'aggiornamento del veicolo id: " + vehicle.getId(), e);
        }
    }

    @Override
    public void updatePricing(Vehicle vehicle, Connection conn) {
        String sql = "UPDATE vehicle SET price = ?, price_mode = ?::price_mode WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setBigDecimal(1, vehicle.getPrice());
            ps.setString(2, vehicle.getPriceMode().name());
            ps.setLong(3, vehicle.getId());
            if (ps.executeUpdate() != 1) throw new SQLException("Veicolo non trovato durante l'aggiornamento della base.");
        } catch (SQLException e) {
            throw new RuntimeException("Errore durante l'aggiornamento della base del veicolo.", e);
        }
    }

    @Override
    public boolean updateStateAtomically(Long vehicleId, VehicleState expectedOldState, VehicleState newState, Connection conn) {
        String sql = "UPDATE vehicle SET state = ?::vehicle_state WHERE id = ? AND state = ?::vehicle_state";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, newState.name());
            ps.setLong(2, vehicleId);
            ps.setString(3, expectedOldState.name());
            int affected = ps.executeUpdate();
            return affected > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Errore durante l'aggiornamento atomico dello stato del veicolo.", e);
        }
    }

    @Override
    public void delete(Long id, Connection conn) {
        String sql = "DELETE FROM vehicle WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            if (ps.executeUpdate() != 1) throw new SQLException("Veicolo da eliminare non trovato: " + id);
        } catch (SQLException e) {
            throw new RuntimeException("Errore durante l'eliminazione del veicolo id: " + id, e);
        }
    }

    @Override
    public boolean hasAssociatedOperations(Long vehicleId) {
        try (Connection conn = db.getConnection()) {
            return hasAssociatedOperations(vehicleId, conn);
        } catch (SQLException e) {
            throw new RuntimeException("Errore di connessione durante la verifica dello storico del veicolo.", e);
        }
    }

    @Override
    public boolean hasAssociatedOperations(Long vehicleId, Connection conn) {
        String sql = "SELECT (" +
                "EXISTS(SELECT 1 FROM test_drive WHERE vehicle = ?) OR " +
                "EXISTS(SELECT 1 FROM rent WHERE vehicle = ?) OR " +
                "EXISTS(SELECT 1 FROM sale WHERE vehicle = ?)" +
                ") AS has_ops";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, vehicleId);
            ps.setLong(2, vehicleId);
            ps.setLong(3, vehicleId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getBoolean("has_ops");
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Errore durante la verifica delle operazioni per il veicolo id: " + vehicleId, e);
        }
        return false;
    }

    private static Vehicle mapRow(ResultSet rs) throws SQLException {
        return Vehicle.create(
                rs.getLong("id"),
                rs.getLong("model"),
                rs.getString("vin"),
                rs.getString("licence_plate"),
                rs.getInt("kilometers"),
                rs.getBigDecimal("price"),
                rs.getInt("year"),
                VehicleState.valueOf(rs.getString("state_str")),
                PriceMode.valueOf(rs.getString("price_mode"))
        );
    }
}

