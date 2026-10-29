package it.unifi.drivehub.dao.postgres;

import it.unifi.drivehub.dao.DatabaseManager;
import it.unifi.drivehub.dao.RentDao;
import it.unifi.drivehub.domain.operation.Rent;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public final class PostgresRentDao implements RentDao {

    private final DatabaseManager db;

    public PostgresRentDao(DatabaseManager db) {
        this.db = db;
    }

    @Override
    public List<Rent> findAll() {
        List<Rent> list = new ArrayList<>();
        String sql = "SELECT id, vehicle, client, amount, start, \"end\" FROM rent ORDER BY start DESC";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Errore durante il recupero dei noleggi.", e);
        }
        return list;
    }

    @Override
    public List<Rent> findByClientId(Long clientId) {
        List<Rent> list = new ArrayList<>();
        String sql = "SELECT id, vehicle, client, amount, start, \"end\" FROM rent WHERE client = ? ORDER BY start DESC";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, clientId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Errore durante il recupero dei noleggi per cliente: " + clientId, e);
        }
        return list;
    }

    @Override
    public List<Rent> findByVehicleId(Long vehicleId) {
        List<Rent> list = new ArrayList<>();
        String sql = "SELECT id, vehicle, client, amount, start, \"end\" FROM rent WHERE vehicle = ? ORDER BY start ASC";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, vehicleId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Errore durante la ricerca dei noleggi per veicolo: " + vehicleId, e);
        }
        return list;
    }

    @Override
    public List<Rent> findFutureByVehicleId(Long vehicleId) {
        List<Rent> list = new ArrayList<>();
        String sql = "SELECT id, vehicle, client, amount, start, \"end\" FROM rent WHERE vehicle = ? AND start >= CURRENT_DATE ORDER BY start ASC";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, vehicleId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Errore durante la ricerca dei noleggi futuri per veicolo: " + vehicleId, e);
        }
        return list;
    }

    @Override
    public boolean isVehicleAvailableForRent(Long vehicleId, LocalDate start, LocalDate end, Connection conn) {
        // Controllo che non esistano prenotazioni sovrapposte:
        // Sovrapposizione se: start <= existing.end AND end >= existing.start
        String sql = "SELECT NOT EXISTS (" +
                "SELECT 1 FROM rent WHERE vehicle = ? AND start <= ? AND \"end\" >= ?" +
                ") AS is_available";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, vehicleId);
            ps.setDate(2, Date.valueOf(end));
            ps.setDate(3, Date.valueOf(start));
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getBoolean("is_available");
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Errore durante la verifica di disponibilità del noleggio.", e);
        }
        return false;
    }

    @Override
    public Rent save(Rent rent, Connection conn) {
        String sql = "INSERT INTO rent (vehicle, client, amount, start, \"end\") VALUES (?, ?, ?, ?, ?) RETURNING id";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, rent.getVehicleId());
            ps.setLong(2, rent.getClientId());
            ps.setBigDecimal(3, rent.getAmount());
            ps.setDate(4, Date.valueOf(rent.getStartDate()));
            ps.setDate(5, Date.valueOf(rent.getEndDate()));

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    long id = rs.getLong(1);
                    return Rent.create(id, rent.getVehicleId(), rent.getClientId(), rent.getAmount(), rent.getStartDate(), rent.getEndDate());
                }
                throw new SQLException("Inserimento noleggio non ha restituito l'ID generato.");
            }
        } catch (SQLException e) {
            throw new RuntimeException("Errore durante il salvataggio del noleggio.", e);
        }
    }

    @Override
    public void delete(Long id, Connection conn) {
        String sql = "DELETE FROM rent WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Errore durante l'eliminazione del noleggio id: " + id, e);
        }
    }

    @Override
    public List<Rent> deleteFutureByVehicleId(Long vehicleId, Connection conn) {
        // Seleziona prima i noleggi futuri per poter rimborsare i clienti
        List<Rent> futureRents = new ArrayList<>();
        String selectSql = "SELECT id, vehicle, client, amount, start, \"end\" FROM rent WHERE vehicle = ? AND start >= CURRENT_DATE";
        try (PreparedStatement ps = conn.prepareStatement(selectSql)) {
            ps.setLong(1, vehicleId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    futureRents.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Errore durante la selezione dei noleggi futuri da annullare.", e);
        }

        String deleteSql = "DELETE FROM rent WHERE vehicle = ? AND start >= CURRENT_DATE";
        try (PreparedStatement ps = conn.prepareStatement(deleteSql)) {
            ps.setLong(1, vehicleId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Errore durante la cancellazione dei noleggi futuri per il veicolo: " + vehicleId, e);
        }

        return futureRents;
    }

    private static Rent mapRow(ResultSet rs) throws SQLException {
        return Rent.create(
                rs.getLong("id"),
                rs.getLong("vehicle"),
                rs.getLong("client"),
                rs.getBigDecimal("amount"),
                rs.getDate("start").toLocalDate(),
                rs.getDate("end").toLocalDate()
        );
    }
}

