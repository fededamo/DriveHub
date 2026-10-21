package it.unifi.ing.drivehub.dao.postgres;

import it.unifi.ing.drivehub.dao.interfaces.VehicleDao;
import it.unifi.ing.drivehub.domain.vehicles.Vehicle;
import it.unifi.ing.drivehub.domain.vehicles.VehicleState;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

final class PostgresVehicleDao extends AbstractPostgresDao implements VehicleDao {

    PostgresVehicleDao(Connection connection, JdbcEntityLoader loader) {
        super(connection, loader);
    }

    @Override
    public Vehicle save(Vehicle vehicle) {
        if (vehicle.id() != null) {
            throw new IllegalArgumentException("vehicle is already persistent");
        }
        long id = execute("Could not save vehicle", () -> JdbcSupport.insert(connection,
                    "INSERT INTO \"vehicle\" (model, vin, licence_plate, kilometers, price, \"year\", state) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?)",
                stmt -> {
                    stmt.setLong(1, vehicle.modelId());
                    stmt.setString(2, vehicle.vin());
                    stmt.setString(3, vehicle.licencePlate());
                    stmt.setInt(4, vehicle.kilometers());
                    stmt.setBigDecimal(5, vehicle.price());
                    stmt.setInt(6, vehicle.year());
                    stmt.setString(7, vehicle.state().name());
                }));
        vehicle.assignId(id);
        return vehicle;
    }

    @Override
    public void updateState(long vehicleId, VehicleState state) {
        execute("Could not update vehicle state", () -> {
            try (PreparedStatement stmt = connection.prepareStatement(
                    "UPDATE \"vehicle\" SET state = ? WHERE id = ?")) {
                stmt.setString(1, state.name());
                stmt.setLong(2, vehicleId);
                stmt.executeUpdate();
                return null;
            }
        });
    }

    @Override
    public void updatePrice(long vehicleId, BigDecimal price) {
        execute("Could not update vehicle price", () -> {
            try (PreparedStatement stmt = connection.prepareStatement(
                    "UPDATE \"vehicle\" SET price = ? WHERE id = ?")) {
                stmt.setBigDecimal(1, price);
                stmt.setLong(2, vehicleId);
                stmt.executeUpdate();
                return null;
            }
        });
    }

    @Override
    public void updateDetails(long vehicleId, String plate, int km, BigDecimal price, int year, VehicleState state) {
        execute("Could not update vehicle details", () -> {
            try (PreparedStatement stmt = connection.prepareStatement(
                    "UPDATE \"vehicle\" SET licence_plate = ?, kilometers = ?, price = ?, \"year\" = ?, state = ? WHERE id = ?")) {
                stmt.setString(1, plate);
                stmt.setInt(2, km);
                stmt.setBigDecimal(3, price);
                stmt.setInt(4, year);
                stmt.setString(5, state.name());
                stmt.setLong(6, vehicleId);
                stmt.executeUpdate();
                return null;
            }
        });
    }

    @Override
    public void delete(long vehicleId) {
        execute("Could not delete vehicle", () -> {
            try (PreparedStatement stmt = connection.prepareStatement(
                    "DELETE FROM \"vehicle\" WHERE id = ?")) {
                stmt.setLong(1, vehicleId);
                stmt.executeUpdate();
                return null;
            }
        });
    }

    @Override
    public Optional<Vehicle> findById(long id) {
        return loader.vehicle(id);
    }

    @Override
    public Optional<Vehicle> findByVin(String vin) {
        return execute("Could not find vehicle by VIN", () -> {
            try (PreparedStatement stmt = connection.prepareStatement(
                    "SELECT id, model, vin, licence_plate, kilometers, price, \"year\", state FROM \"vehicle\" WHERE vin = ?")) {
                stmt.setString(1, vin);
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
    public Optional<Vehicle> findByPlate(String plate) {
        return execute("Could not find vehicle by plate", () -> {
            try (PreparedStatement stmt = connection.prepareStatement(
                    "SELECT id, model, vin, licence_plate, kilometers, price, \"year\", state FROM \"vehicle\" WHERE licence_plate = ?")) {
                stmt.setString(1, plate);
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
    public List<Vehicle> findByState(VehicleState state) {
        return execute("Could not find vehicles by state", () -> {
            try (PreparedStatement stmt = connection.prepareStatement(
                    "SELECT id, model, vin, licence_plate, kilometers, price, \"year\", state FROM \"vehicle\" WHERE state = ?")) {
                stmt.setString(1, state.name());
                try (ResultSet rs = stmt.executeQuery()) {
                    List<Vehicle> list = new ArrayList<>();
                    while (rs.next()) {
                        list.add(mapRow(rs));
                    }
                    return List.copyOf(list);
                }
            }
        });
    }

    @Override
    public List<Vehicle> findAll() {
        return execute("Could not find all vehicles", () -> {
            try (PreparedStatement stmt = connection.prepareStatement(
                    "SELECT id, model, vin, licence_plate, kilometers, price, \"year\", state FROM \"vehicle\"")) {
                try (ResultSet rs = stmt.executeQuery()) {
                    List<Vehicle> list = new ArrayList<>();
                    while (rs.next()) {
                        list.add(mapRow(rs));
                    }
                    return List.copyOf(list);
                }
            }
        });
    }

    @Override
    public boolean hasOperations(long vehicleId) {
        return execute("Could not check operations for vehicle", () -> {
            String sql = "SELECT 1 FROM (" +
                    "SELECT vehicle FROM \"test_drive\" WHERE vehicle = ? UNION ALL " +
                    "SELECT vehicle FROM \"rent\" WHERE vehicle = ? UNION ALL " +
                    "SELECT vehicle FROM \"sale\" WHERE vehicle = ?" +
                    ") ops LIMIT 1";
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setLong(1, vehicleId);
                stmt.setLong(2, vehicleId);
                stmt.setLong(3, vehicleId);
                try (ResultSet rs = stmt.executeQuery()) {
                    return rs.next();
                }
            }
        });
    }

    private static Vehicle mapRow(ResultSet rs) throws SQLException {
        return new Vehicle(
                rs.getLong("id"),
                rs.getLong("model"),
                rs.getString("vin"),
                rs.getString("licence_plate"),
                rs.getInt("kilometers"),
                rs.getBigDecimal("price"),
                rs.getInt("year"),
                VehicleState.fromString(rs.getString("state")));
    }
}
