package it.unifi.ing.drivehub.dao.postgres;

import it.unifi.ing.drivehub.domain.rentals.Rent;
import it.unifi.ing.drivehub.domain.rentals.TestDrive;
import it.unifi.ing.drivehub.domain.sales.Request;
import it.unifi.ing.drivehub.domain.sales.Sale;
import it.unifi.ing.drivehub.domain.users.BankAccount;
import it.unifi.ing.drivehub.domain.users.Role;
import it.unifi.ing.drivehub.domain.users.User;
import it.unifi.ing.drivehub.domain.vehicles.Brand;
import it.unifi.ing.drivehub.domain.vehicles.ModelDiscount;
import it.unifi.ing.drivehub.domain.vehicles.Vehicle;
import it.unifi.ing.drivehub.domain.vehicles.VehicleDiscount;
import it.unifi.ing.drivehub.domain.vehicles.VehicleModel;
import it.unifi.ing.drivehub.domain.vehicles.VehicleState;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

final class JdbcEntityLoader {

    private final Connection connection;

    JdbcEntityLoader(Connection connection) {
        this.connection = connection;
    }

    Optional<User> user(long id) {
        return load("SELECT id, name, surname, email, hash_password, role FROM \"user\" WHERE id = ?", id,
                row -> new User(
                        row.getLong("id"),
                        row.getString("name"),
                        row.getString("surname"),
                        row.getString("email"),
                        row.getString("hash_password"),
                        Role.fromString(row.getString("role"))));
    }

    Optional<BankAccount> bankAccount(String iban) {
        try (PreparedStatement stmt = connection.prepareStatement(
                "SELECT iban, client, balance FROM \"bank_account\" WHERE iban = ?")) {
            stmt.setString(1, iban);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(new BankAccount(
                            rs.getString("iban"),
                            rs.getLong("client"),
                            rs.getBigDecimal("balance")));
                }
                return Optional.empty();
            }
        } catch (SQLException e) {
            throw new DatabaseException("Could not load BankAccount by iban " + iban, e);
        }
    }

    Optional<BankAccount> bankAccountByClient(long clientId) {
        return load("SELECT iban, client, balance FROM \"bank_account\" WHERE client = ?", clientId,
                row -> new BankAccount(
                        row.getString("iban"),
                        row.getLong("client"),
                        row.getBigDecimal("balance")));
    }

    Optional<Brand> brand(String name) {
        try (PreparedStatement stmt = connection.prepareStatement(
                "SELECT name FROM \"brand\" WHERE LOWER(name) = LOWER(?)")) {
            stmt.setString(1, name);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(new Brand(rs.getString("name")));
                }
                return Optional.empty();
            }
        } catch (SQLException e) {
            throw new DatabaseException("Could not load Brand " + name, e);
        }
    }

    Optional<VehicleModel> vehicleModel(long id) {
        return load("SELECT id, brand, name, release_price, release_year FROM \"model\" WHERE id = ?", id,
                row -> new VehicleModel(
                        row.getLong("id"),
                        row.getString("brand"),
                        row.getString("name"),
                        row.getBigDecimal("release_price"),
                        row.getInt("release_year")));
    }

    Optional<Vehicle> vehicle(long id) {
        return load("SELECT id, model, vin, licence_plate, kilometers, price, \"year\", state FROM \"vehicle\" WHERE id = ?", id,
                row -> new Vehicle(
                        row.getLong("id"),
                        row.getLong("model"),
                        row.getString("vin"),
                        row.getString("licence_plate"),
                        row.getInt("kilometers"),
                        row.getBigDecimal("price"),
                        row.getInt("year"),
                        VehicleState.fromString(row.getString("state"))));
    }

    Optional<ModelDiscount> modelDiscount(long modelId) {
        return load("SELECT model, \"value\" FROM \"model_discount\" WHERE model = ?", modelId,
                row -> new ModelDiscount(row.getLong("model"), row.getInt("value")));
    }

    Optional<VehicleDiscount> vehicleDiscount(long vehicleId) {
        return load("SELECT vehicle, \"value\" FROM \"vehicle_discount\" WHERE vehicle = ?", vehicleId,
                row -> new VehicleDiscount(row.getLong("vehicle"), row.getInt("value")));
    }

    Optional<Request> request(long id) {
        return load("SELECT id, vehicle, client, salesman, deposit, exp_date FROM \"request\" WHERE id = ?", id,
                row -> new Request(
                        row.getLong("id"),
                        row.getLong("vehicle"),
                        row.getLong("client"),
                        JdbcSupport.nullableLong(row, "salesman"),
                        row.getBigDecimal("deposit"),
                        row.getObject("exp_date", LocalDate.class)));
    }

    Optional<Sale> sale(long id) {
        return load("SELECT id, vehicle, salesman, client, amount, date FROM \"sale\" WHERE id = ?", id,
                row -> {
                    Timestamp ts = row.getTimestamp("date");
                    LocalDateTime dt = ts != null ? ts.toLocalDateTime() : null;
                    return new Sale(
                            row.getLong("id"),
                            JdbcSupport.nullableLong(row, "vehicle"),
                            JdbcSupport.nullableLong(row, "salesman"),
                            row.getLong("client"),
                            row.getBigDecimal("amount"),
                            dt);
                });
    }

    Optional<Rent> rent(long id) {
        return load("SELECT id, vehicle, client, amount, start, \"end\" FROM \"rent\" WHERE id = ?", id,
                row -> new Rent(
                        row.getLong("id"),
                        row.getLong("vehicle"),
                        row.getLong("client"),
                        row.getBigDecimal("amount"),
                        row.getObject("start", LocalDate.class),
                        row.getObject("end", LocalDate.class)));
    }

    Optional<TestDrive> testDrive(long id) {
        return load("SELECT id, vehicle, client, date FROM \"test_drive\" WHERE id = ?", id,
                row -> {
                    Timestamp ts = row.getTimestamp("date");
                    LocalDateTime dt = ts != null ? ts.toLocalDateTime() : null;
                    return new TestDrive(
                            row.getLong("id"),
                            row.getLong("vehicle"),
                            row.getLong("client"),
                            dt);
                });
    }

    private <T> Optional<T> load(String sql, long id, RowMapper<T> mapper) {
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return Optional.empty();
                }
                return Optional.of(mapper.map(resultSet));
            }
        } catch (SQLException exception) {
            throw new DatabaseException("Could not execute load query: " + sql, exception);
        }
    }

    @FunctionalInterface
    interface RowMapper<T> {
        T map(ResultSet row) throws SQLException;
    }
}
