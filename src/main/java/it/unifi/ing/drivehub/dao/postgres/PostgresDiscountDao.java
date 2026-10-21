package it.unifi.ing.drivehub.dao.postgres;

import it.unifi.ing.drivehub.dao.interfaces.DiscountDao;
import it.unifi.ing.drivehub.domain.vehicles.ModelDiscount;
import it.unifi.ing.drivehub.domain.vehicles.VehicleDiscount;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.Optional;

final class PostgresDiscountDao extends AbstractPostgresDao implements DiscountDao {

    PostgresDiscountDao(Connection connection, JdbcEntityLoader loader) {
        super(connection, loader);
    }

    @Override
    public Optional<ModelDiscount> findModelDiscount(long modelId) {
        return loader.modelDiscount(modelId);
    }

    @Override
    public Optional<VehicleDiscount> findVehicleDiscount(long vehicleId) {
        return loader.vehicleDiscount(vehicleId);
    }

    @Override
    public void saveModelDiscount(ModelDiscount discount) {
        execute("Could not save model discount", () -> {
            try (PreparedStatement del = connection.prepareStatement(
                    "DELETE FROM \"model_discount\" WHERE model = ?")) {
                del.setLong(1, discount.modelId());
                del.executeUpdate();
            }
            try (PreparedStatement stmt = connection.prepareStatement(
                    "INSERT INTO \"model_discount\" (model, \"value\") VALUES (?, ?)")) {
                stmt.setLong(1, discount.modelId());
                stmt.setInt(2, discount.value());
                stmt.executeUpdate();
                return null;
            }
        });
    }

    @Override
    public void saveVehicleDiscount(VehicleDiscount discount) {
        execute("Could not save vehicle discount", () -> {
            try (PreparedStatement del = connection.prepareStatement(
                    "DELETE FROM \"vehicle_discount\" WHERE vehicle = ?")) {
                del.setLong(1, discount.vehicleId());
                del.executeUpdate();
            }
            try (PreparedStatement stmt = connection.prepareStatement(
                    "INSERT INTO \"vehicle_discount\" (vehicle, \"value\") VALUES (?, ?)")) {
                stmt.setLong(1, discount.vehicleId());
                stmt.setInt(2, discount.value());
                stmt.executeUpdate();
                return null;
            }
        });
    }

    @Override
    public void removeModelDiscount(long modelId) {
        execute("Could not remove model discount", () -> {
            try (PreparedStatement stmt = connection.prepareStatement(
                    "DELETE FROM \"model_discount\" WHERE model = ?")) {
                stmt.setLong(1, modelId);
                stmt.executeUpdate();
                return null;
            }
        });
    }

    @Override
    public void removeVehicleDiscount(long vehicleId) {
        execute("Could not remove vehicle discount", () -> {
            try (PreparedStatement stmt = connection.prepareStatement(
                    "DELETE FROM \"vehicle_discount\" WHERE vehicle = ?")) {
                stmt.setLong(1, vehicleId);
                stmt.executeUpdate();
                return null;
            }
        });
    }
}
