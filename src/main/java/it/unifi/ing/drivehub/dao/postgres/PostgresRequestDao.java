package it.unifi.ing.drivehub.dao.postgres;

import it.unifi.ing.drivehub.dao.interfaces.RequestDao;
import it.unifi.ing.drivehub.domain.sales.Request;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Types;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

final class PostgresRequestDao extends AbstractPostgresDao implements RequestDao {

    PostgresRequestDao(Connection connection, JdbcEntityLoader loader) {
        super(connection, loader);
    }

    @Override
    public Request save(Request request) {
        if (request.id() != null) {
            throw new IllegalArgumentException("request is already persistent");
        }
        long id = execute("Could not save request", () -> JdbcSupport.insert(connection,
                "INSERT INTO \"request\" (vehicle, client, salesman, deposit, exp_date) VALUES (?, ?, ?, ?, ?)",
                stmt -> {
                    stmt.setLong(1, request.vehicleId());
                    stmt.setLong(2, request.clientId());
                    if (request.salesmanId() != null) {
                        stmt.setLong(3, request.salesmanId());
                    } else {
                        stmt.setNull(3, Types.BIGINT);
                    }
                    stmt.setBigDecimal(4, request.deposit());
                    stmt.setDate(5, request.expDate() == null ? null : Date.valueOf(request.expDate()));
                }));
        request.assignId(id);
        return request;
    }

    @Override
    public void updateExpDate(long requestId, LocalDate expDate) {
        execute("Could not update request exp_date", () -> {
            try (PreparedStatement stmt = connection.prepareStatement(
                    "UPDATE \"request\" SET exp_date = ? WHERE id = ?")) {
                stmt.setDate(1, expDate == null ? null : Date.valueOf(expDate));
                stmt.setLong(2, requestId);
                stmt.executeUpdate();
                return null;
            }
        });
    }

    @Override
    public void updateAuthorization(long requestId, long salesmanId, LocalDate expDate) {
        execute("Could not authorize request", () -> {
            try (PreparedStatement stmt = connection.prepareStatement(
                    "UPDATE \"request\" SET salesman = ?, exp_date = ? WHERE id = ?")) {
                stmt.setLong(1, salesmanId);
                stmt.setDate(2, expDate == null ? null : Date.valueOf(expDate));
                stmt.setLong(3, requestId);
                stmt.executeUpdate();
                return null;
            }
        });
    }

    @Override
    public void delete(long requestId) {
        execute("Could not delete request", () -> {
            try (PreparedStatement stmt = connection.prepareStatement(
                    "DELETE FROM \"request\" WHERE id = ?")) {
                stmt.setLong(1, requestId);
                stmt.executeUpdate();
                return null;
            }
        });
    }

    @Override
    public Optional<Request> findById(long id) {
        return loader.request(id);
    }

    @Override
    public Optional<Request> findByVehicleId(long vehicleId) {
        return execute("Could not find request by vehicle", () -> {
            try (PreparedStatement stmt = connection.prepareStatement(
                    "SELECT id, vehicle, client, salesman, deposit, exp_date FROM \"request\" WHERE vehicle = ?")) {
                stmt.setLong(1, vehicleId);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return Optional.of(mapRow(rs));
                    }
                    return Optional.empty();
                }
            }
        });
    }

    @Override
    public List<Request> findByClientId(long clientId) {
        return execute("Could not find requests by client", () -> {
            try (PreparedStatement stmt = connection.prepareStatement(
                    "SELECT id, vehicle, client, salesman, deposit, exp_date FROM \"request\" WHERE client = ?")) {
                stmt.setLong(1, clientId);
                try (ResultSet rs = stmt.executeQuery()) {
                    List<Request> list = new ArrayList<>();
                    while (rs.next()) {
                        list.add(mapRow(rs));
                    }
                    return List.copyOf(list);
                }
            }
        });
    }

    @Override
    public List<Request> findAll() {
        return execute("Could not find all requests", () -> {
            try (PreparedStatement stmt = connection.prepareStatement(
                    "SELECT id, vehicle, client, salesman, deposit, exp_date FROM \"request\"")) {
                try (ResultSet rs = stmt.executeQuery()) {
                    List<Request> list = new ArrayList<>();
                    while (rs.next()) {
                        list.add(mapRow(rs));
                    }
                    return List.copyOf(list);
                }
            }
        });
    }

    @Override
    public List<Request> findExpiredRequests(LocalDate asOfDate) {
        return execute("Could not find expired requests", () -> {
            try (PreparedStatement stmt = connection.prepareStatement(
                    "SELECT id, vehicle, client, salesman, deposit, exp_date FROM \"request\" " +
                    "WHERE deposit > 0 AND exp_date IS NOT NULL AND exp_date < ?")) {
                stmt.setDate(1, Date.valueOf(asOfDate));
                try (ResultSet rs = stmt.executeQuery()) {
                    List<Request> list = new ArrayList<>();
                    while (rs.next()) {
                        list.add(mapRow(rs));
                    }
                    return List.copyOf(list);
                }
            }
        });
    }

    private static Request mapRow(ResultSet rs) throws java.sql.SQLException {
        return new Request(
                rs.getLong("id"),
                rs.getLong("vehicle"),
                rs.getLong("client"),
                JdbcSupport.nullableLong(rs, "salesman"),
                rs.getBigDecimal("deposit"),
                rs.getObject("exp_date", LocalDate.class));
    }
}
