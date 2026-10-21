package it.unifi.ing.drivehub.dao.postgres;

import it.unifi.ing.drivehub.dao.interfaces.SaleDao;
import it.unifi.ing.drivehub.domain.sales.Sale;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

final class PostgresSaleDao extends AbstractPostgresDao implements SaleDao {

    PostgresSaleDao(Connection connection, JdbcEntityLoader loader) {
        super(connection, loader);
    }

    @Override
    public Sale save(Sale sale) {
        if (sale.id() != null) {
            throw new IllegalArgumentException("sale is already persistent");
        }
        long id = execute("Could not save sale", () -> JdbcSupport.insert(connection,
                "INSERT INTO \"sale\" (vehicle, salesman, client, amount, date) VALUES (?, ?, ?, ?, ?)",
                stmt -> {
                    JdbcSupport.setNullableLong(stmt, 1, sale.vehicleId());
                    JdbcSupport.setNullableLong(stmt, 2, sale.salesmanId());
                    stmt.setLong(3, sale.clientId());
                    stmt.setBigDecimal(4, sale.amount());
                    stmt.setTimestamp(5, Timestamp.valueOf(sale.date()));
                }));
        sale.assignId(id);
        return sale;
    }

    @Override
    public Optional<Sale> findById(long id) {
        return loader.sale(id);
    }

    @Override
    public List<Sale> findByClientId(long clientId) {
        return execute("Could not find sales by client", () -> {
            try (PreparedStatement stmt = connection.prepareStatement(
                    "SELECT id, vehicle, salesman, client, amount, date FROM \"sale\" WHERE client = ? ORDER BY date DESC")) {
                stmt.setLong(1, clientId);
                try (ResultSet rs = stmt.executeQuery()) {
                    List<Sale> list = new ArrayList<>();
                    while (rs.next()) {
                        list.add(mapRow(rs));
                    }
                    return List.copyOf(list);
                }
            }
        });
    }

    @Override
    public List<Sale> findBySalesmanId(long salesmanId) {
        return execute("Could not find sales by salesman", () -> {
            try (PreparedStatement stmt = connection.prepareStatement(
                    "SELECT id, vehicle, salesman, client, amount, date FROM \"sale\" WHERE salesman = ? ORDER BY date DESC")) {
                stmt.setLong(1, salesmanId);
                try (ResultSet rs = stmt.executeQuery()) {
                    List<Sale> list = new ArrayList<>();
                    while (rs.next()) {
                        list.add(mapRow(rs));
                    }
                    return List.copyOf(list);
                }
            }
        });
    }

    @Override
    public List<Sale> findByVehicleId(long vehicleId) {
        return execute("Could not find sales by vehicle", () -> {
            try (PreparedStatement stmt = connection.prepareStatement(
                    "SELECT id, vehicle, salesman, client, amount, date FROM \"sale\" WHERE vehicle = ? ORDER BY date DESC")) {
                stmt.setLong(1, vehicleId);
                try (ResultSet rs = stmt.executeQuery()) {
                    List<Sale> list = new ArrayList<>();
                    while (rs.next()) {
                        list.add(mapRow(rs));
                    }
                    return List.copyOf(list);
                }
            }
        });
    }

    @Override
    public List<Sale> findAll() {
        return execute("Could not find all sales", () -> {
            try (PreparedStatement stmt = connection.prepareStatement(
                    "SELECT id, vehicle, salesman, client, amount, date FROM \"sale\" ORDER BY date DESC")) {
                try (ResultSet rs = stmt.executeQuery()) {
                    List<Sale> list = new ArrayList<>();
                    while (rs.next()) {
                        list.add(mapRow(rs));
                    }
                    return List.copyOf(list);
                }
            }
        });
    }

    @Override
    public boolean hasOperationsForVehicle(long vehicleId) {
        return execute("Could not check sale operations for vehicle", () -> {
            try (PreparedStatement stmt = connection.prepareStatement(
                    "SELECT 1 FROM \"sale\" WHERE vehicle = ? LIMIT 1")) {
                stmt.setLong(1, vehicleId);
                try (ResultSet rs = stmt.executeQuery()) {
                    return rs.next();
                }
            }
        });
    }

    @Override
    public boolean hasOperationsForClient(long clientId) {
        return execute("Could not check sale operations for client", () -> {
            try (PreparedStatement stmt = connection.prepareStatement(
                    "SELECT 1 FROM \"sale\" WHERE client = ? LIMIT 1")) {
                stmt.setLong(1, clientId);
                try (ResultSet rs = stmt.executeQuery()) {
                    return rs.next();
                }
            }
        });
    }

    @Override
    public boolean hasOperationsForSalesman(long salesmanId) {
        return execute("Could not check sale operations for salesman", () -> {
            try (PreparedStatement stmt = connection.prepareStatement(
                    "SELECT 1 FROM \"sale\" WHERE salesman = ? LIMIT 1")) {
                stmt.setLong(1, salesmanId);
                try (ResultSet rs = stmt.executeQuery()) {
                    return rs.next();
                }
            }
        });
    }

    private static Sale mapRow(ResultSet rs) throws SQLException {
        Timestamp ts = rs.getTimestamp("date");
        LocalDateTime dt = ts != null ? ts.toLocalDateTime() : null;
        return new Sale(
                rs.getLong("id"),
                JdbcSupport.nullableLong(rs, "vehicle"),
                JdbcSupport.nullableLong(rs, "salesman"),
                rs.getLong("client"),
                rs.getBigDecimal("amount"),
                dt);
    }
}
