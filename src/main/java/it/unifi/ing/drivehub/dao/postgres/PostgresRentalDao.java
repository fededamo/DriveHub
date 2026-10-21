package it.unifi.ing.drivehub.dao.postgres;

import it.unifi.ing.drivehub.dao.interfaces.RentalDao;
import it.unifi.ing.drivehub.domain.rentals.Rent;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

final class PostgresRentalDao extends AbstractPostgresDao implements RentalDao {

    PostgresRentalDao(Connection connection, JdbcEntityLoader loader) {
        super(connection, loader);
    }

    @Override
    public Rent save(Rent rent) {
        if (rent.id() != null) {
            throw new IllegalArgumentException("rent is already persistent");
        }
        long id = execute("Could not save rent", () -> JdbcSupport.insert(connection,
                "INSERT INTO \"rent\" (vehicle, client, amount, start, \"end\") VALUES (?, ?, ?, ?, ?)",
                stmt -> {
                    stmt.setLong(1, rent.vehicleId());
                    stmt.setLong(2, rent.clientId());
                    stmt.setBigDecimal(3, rent.amount());
                    stmt.setDate(4, Date.valueOf(rent.start()));
                    stmt.setDate(5, Date.valueOf(rent.end()));
                }));
        rent.assignId(id);
        return rent;
    }

    @Override
    public Optional<Rent> findById(long id) {
        return loader.rent(id);
    }

    @Override
    public List<Rent> findByClientId(long clientId) {
        return execute("Could not find rents by client", () -> {
            try (PreparedStatement stmt = connection.prepareStatement(
                    "SELECT id, vehicle, client, amount, start, \"end\" FROM \"rent\" WHERE client = ? ORDER BY start DESC")) {
                stmt.setLong(1, clientId);
                try (ResultSet rs = stmt.executeQuery()) {
                    List<Rent> list = new ArrayList<>();
                    while (rs.next()) {
                        list.add(mapRow(rs));
                    }
                    return List.copyOf(list);
                }
            }
        });
    }

    @Override
    public List<Rent> findByVehicleId(long vehicleId) {
        return execute("Could not find rents by vehicle", () -> {
            try (PreparedStatement stmt = connection.prepareStatement(
                    "SELECT id, vehicle, client, amount, start, \"end\" FROM \"rent\" WHERE vehicle = ? ORDER BY start DESC")) {
                stmt.setLong(1, vehicleId);
                try (ResultSet rs = stmt.executeQuery()) {
                    List<Rent> list = new ArrayList<>();
                    while (rs.next()) {
                        list.add(mapRow(rs));
                    }
                    return List.copyOf(list);
                }
            }
        });
    }

    @Override
    public List<Rent> findAll() {
        return execute("Could not find all rents", () -> {
            try (PreparedStatement stmt = connection.prepareStatement(
                    "SELECT id, vehicle, client, amount, start, \"end\" FROM \"rent\" ORDER BY start DESC")) {
                try (ResultSet rs = stmt.executeQuery()) {
                    List<Rent> list = new ArrayList<>();
                    while (rs.next()) {
                        list.add(mapRow(rs));
                    }
                    return List.copyOf(list);
                }
            }
        });
    }

    @Override
    public boolean hasOverlappingRentals(long vehicleId, LocalDate start, LocalDate end) {
        return execute("Could not check overlapping rentals", () -> {
            try (PreparedStatement stmt = connection.prepareStatement(
                    "SELECT 1 FROM \"rent\" WHERE vehicle = ? AND NOT (\"end\" < ? OR start > ?) LIMIT 1")) {
                stmt.setLong(1, vehicleId);
                stmt.setDate(2, Date.valueOf(start));
                stmt.setDate(3, Date.valueOf(end));
                try (ResultSet rs = stmt.executeQuery()) {
                    return rs.next();
                }
            }
        });
    }

    @Override
    public boolean isVehicleRentedOnDate(long vehicleId, LocalDate date) {
        return execute("Could not check if vehicle is rented on date", () -> {
            try (PreparedStatement stmt = connection.prepareStatement(
                    "SELECT 1 FROM \"rent\" WHERE vehicle = ? AND ? BETWEEN start AND \"end\" LIMIT 1")) {
                stmt.setLong(1, vehicleId);
                stmt.setDate(2, Date.valueOf(date));
                try (ResultSet rs = stmt.executeQuery()) {
                    return rs.next();
                }
            }
        });
    }

    @Override
    public List<Rent> findFutureRentalsForVehicle(long vehicleId, LocalDate asOfDate) {
        return execute("Could not find future rentals for vehicle", () -> {
            try (PreparedStatement stmt = connection.prepareStatement(
                    "SELECT id, vehicle, client, amount, start, \"end\" FROM \"rent\" WHERE vehicle = ? AND \"end\" >= ?")) {
                stmt.setLong(1, vehicleId);
                stmt.setDate(2, Date.valueOf(asOfDate));
                try (ResultSet rs = stmt.executeQuery()) {
                    List<Rent> list = new ArrayList<>();
                    while (rs.next()) {
                        list.add(mapRow(rs));
                    }
                    return List.copyOf(list);
                }
            }
        });
    }

    @Override
    public void delete(long rentId) {
        execute("Could not delete rental", () -> {
            try (PreparedStatement stmt = connection.prepareStatement(
                    "DELETE FROM \"rent\" WHERE id = ?")) {
                stmt.setLong(1, rentId);
                stmt.executeUpdate();
                return null;
            }
        });
    }

    @Override
    public boolean hasOperationsForVehicle(long vehicleId) {
        return execute("Could not check rent operations for vehicle", () -> {
            try (PreparedStatement stmt = connection.prepareStatement(
                    "SELECT 1 FROM \"rent\" WHERE vehicle = ? LIMIT 1")) {
                stmt.setLong(1, vehicleId);
                try (ResultSet rs = stmt.executeQuery()) {
                    return rs.next();
                }
            }
        });
    }

    @Override
    public boolean hasOperationsForClient(long clientId) {
        return execute("Could not check rent operations for client", () -> {
            try (PreparedStatement stmt = connection.prepareStatement(
                    "SELECT 1 FROM \"rent\" WHERE client = ? LIMIT 1")) {
                stmt.setLong(1, clientId);
                try (ResultSet rs = stmt.executeQuery()) {
                    return rs.next();
                }
            }
        });
    }

    private static Rent mapRow(ResultSet rs) throws SQLException {
        return new Rent(
                rs.getLong("id"),
                rs.getLong("vehicle"),
                rs.getLong("client"),
                rs.getBigDecimal("amount"),
                rs.getObject("start", LocalDate.class),
                rs.getObject("end", LocalDate.class));
    }
}
