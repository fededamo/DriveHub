package it.unifi.drivehub.dao.postgres;

import it.unifi.drivehub.dao.DatabaseManager;
import it.unifi.drivehub.dao.RequestDao;
import it.unifi.drivehub.domain.operation.Request;
import it.unifi.drivehub.domain.operation.RequestType;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class PostgresRequestDao implements RequestDao {

    private final DatabaseManager db;

    public PostgresRequestDao(DatabaseManager db) {
        this.db = db;
    }

    @Override
    public List<Request> findAll() {
        List<Request> list = new ArrayList<>();
        String sql = "SELECT id, vehicle, client, salesman, deposit, exp_date, type::text as type_str FROM request ORDER BY id DESC";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Errore durante il recupero delle richieste.", e);
        }
        return list;
    }

    @Override
    public List<Request> findPending() {
        List<Request> list = new ArrayList<>();
        String sql = "SELECT id, vehicle, client, salesman, deposit, exp_date, type::text as type_str FROM request WHERE salesman IS NULL ORDER BY id ASC";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Errore durante il recupero delle richieste in sospeso.", e);
        }
        return list;
    }

    @Override
    public List<Request> findByClientId(Long clientId) {
        List<Request> list = new ArrayList<>();
        String sql = "SELECT id, vehicle, client, salesman, deposit, exp_date, type::text as type_str FROM request WHERE client = ? ORDER BY id DESC";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, clientId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Errore durante il recupero delle richieste del cliente: " + clientId, e);
        }
        return list;
    }

    @Override
    public Optional<Request> findById(Long id) {
        String sql = "SELECT id, vehicle, client, salesman, deposit, exp_date, type::text as type_str FROM request WHERE id = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Errore durante la ricerca della richiesta id: " + id, e);
        }
        return Optional.empty();
    }

    @Override
    public Optional<Request> findActivePurchaseRequestForVehicle(Long vehicleId) {
        String sql = "SELECT id, vehicle, client, salesman, deposit, exp_date, type::text as type_str " +
                "FROM request WHERE vehicle = ? AND type = 'PURCHASE'::request_type";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, vehicleId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Errore durante la ricerca della richiesta d'acquisto per il veicolo: " + vehicleId, e);
        }
        return Optional.empty();
    }

    @Override
    public Request save(Request request, Connection conn) {
        String sql = "INSERT INTO request (vehicle, client, salesman, deposit, exp_date, type) " +
                "VALUES (?, ?, ?, ?, ?, ?::request_type) RETURNING id";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, request.getVehicleId());
            ps.setLong(2, request.getClientId());
            if (request.getSalesmanId() != null) {
                ps.setLong(3, request.getSalesmanId());
            } else {
                ps.setNull(3, Types.BIGINT);
            }
            ps.setBigDecimal(4, request.getDeposit());
            if (request.getExpDate() != null) {
                ps.setDate(5, Date.valueOf(request.getExpDate()));
            } else {
                ps.setNull(5, Types.DATE);
            }
            ps.setString(6, request.getType().name());

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    long id = rs.getLong(1);
                    return Request.create(id, request.getVehicleId(), request.getClientId(),
                            request.getSalesmanId(), request.getDeposit(), request.getExpDate(), request.getType());
                }
                throw new SQLException("Inserimento richiesta non ha restituito l'ID generato.");
            }
        } catch (SQLException e) {
            throw new RuntimeException("Errore durante il salvataggio della richiesta.", e);
        }
    }

    @Override
    public void update(Request request, Connection conn) {
        String sql = "UPDATE request SET salesman = ?, deposit = ?, exp_date = ?, type = ?::request_type WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            if (request.getSalesmanId() != null) {
                ps.setLong(1, request.getSalesmanId());
            } else {
                ps.setNull(1, Types.BIGINT);
            }
            ps.setBigDecimal(2, request.getDeposit());
            if (request.getExpDate() != null) {
                ps.setDate(3, Date.valueOf(request.getExpDate()));
            } else {
                ps.setNull(3, Types.DATE);
            }
            ps.setString(4, request.getType().name());
            ps.setLong(5, request.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Errore durante l'aggiornamento della richiesta id: " + request.getId(), e);
        }
    }

    @Override
    public void delete(Long id, Connection conn) {
        String sql = "DELETE FROM request WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Errore durante l'eliminazione della richiesta id: " + id, e);
        }
    }

    private static Request mapRow(ResultSet rs) throws SQLException {
        Long salesman = rs.getObject("salesman") != null ? rs.getLong("salesman") : null;
        Date expDate = rs.getDate("exp_date");
        return Request.create(
                rs.getLong("id"),
                rs.getLong("vehicle"),
                rs.getLong("client"),
                salesman,
                rs.getBigDecimal("deposit"),
                expDate != null ? expDate.toLocalDate() : null,
                RequestType.valueOf(rs.getString("type_str"))
        );
    }
}

