package it.unifi.drivehub.dao.postgres;

import it.unifi.drivehub.dao.DatabaseManager;
import it.unifi.drivehub.dao.SaleDao;
import it.unifi.drivehub.domain.operation.Sale;
import it.unifi.drivehub.domain.operation.SaleType;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class PostgresSaleDao implements SaleDao {

    private final DatabaseManager db;

    public PostgresSaleDao(DatabaseManager db) {
        this.db = db;
    }

    @Override
    public List<Sale> findAll() {
        List<Sale> list = new ArrayList<>();
        String sql = "SELECT id, vehicle, salesman, client, amount, date, type FROM sale ORDER BY date DESC";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Errore durante il recupero delle transazioni di vendita.", e);
        }
        return list;
    }

    @Override
    public List<Sale> findByClientId(Long clientId) {
        List<Sale> list = new ArrayList<>();
        String sql = "SELECT id, vehicle, salesman, client, amount, date, type FROM sale WHERE client = ? ORDER BY date DESC";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, clientId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Errore durante il recupero delle vendite per il cliente: " + clientId, e);
        }
        return list;
    }

    @Override
    public Optional<Sale> findById(Long id) {
        String sql = "SELECT id, vehicle, salesman, client, amount, date, type FROM sale WHERE id = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Errore durante la ricerca della vendita id: " + id, e);
        }
        return Optional.empty();
    }

    @Override
    public Optional<Sale> findLatestPurchaseForVehicle(Long vehicleId, Connection conn) {
        String sql = "SELECT id, vehicle, salesman, client, amount, date, type FROM sale " +
                "WHERE vehicle = ? AND type = 'PURCHASE' ORDER BY date DESC, id DESC LIMIT 1";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            if (conn.getAutoCommit()) throw new IllegalArgumentException("La verifica della titolarità richiede una transazione attiva.");
            ps.setLong(1, vehicleId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapRow(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Errore durante la verifica dell'ultima vendita del veicolo.", e);
        }
    }

    @Override
    public Sale save(Sale sale, Connection conn) {
        String sql = "INSERT INTO sale (vehicle, salesman, client, amount, date, type) VALUES (?, ?, ?, ?, ?, ?) RETURNING id";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            if (sale.getVehicleId() != null) {
                ps.setLong(1, sale.getVehicleId());
            } else {
                ps.setNull(1, Types.BIGINT);
            }
            if (sale.getSalesmanId() != null) {
                ps.setLong(2, sale.getSalesmanId());
            } else {
                ps.setNull(2, Types.BIGINT);
            }
            ps.setLong(3, sale.getClientId());
            ps.setBigDecimal(4, sale.getAmount());
            ps.setTimestamp(5, Timestamp.valueOf(sale.getDate()));
            ps.setString(6, sale.getType().name());

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    long id = rs.getLong(1);
                    return Sale.create(id, sale.getVehicleId(), sale.getSalesmanId(), sale.getClientId(), sale.getAmount(), sale.getDate(), sale.getType());
                }
                throw new SQLException("Inserimento transazione sale non ha restituito l'ID generato.");
            }
        } catch (SQLException e) {
            throw new RuntimeException("Errore durante il salvataggio della transazione sale.", e);
        }
    }

    private static Sale mapRow(ResultSet rs) throws SQLException {
        Long vehicle = rs.getObject("vehicle") != null ? rs.getLong("vehicle") : null;
        Long salesman = rs.getObject("salesman") != null ? rs.getLong("salesman") : null;
        return Sale.create(
                rs.getLong("id"),
                vehicle,
                salesman,
                rs.getLong("client"),
                rs.getBigDecimal("amount"),
                rs.getTimestamp("date").toLocalDateTime(),
                SaleType.valueOf(rs.getString("type"))
        );
    }
}

