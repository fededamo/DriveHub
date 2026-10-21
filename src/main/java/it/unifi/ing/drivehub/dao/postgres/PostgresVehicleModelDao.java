package it.unifi.ing.drivehub.dao.postgres;

import it.unifi.ing.drivehub.dao.interfaces.VehicleModelDao;
import it.unifi.ing.drivehub.domain.vehicles.VehicleModel;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

final class PostgresVehicleModelDao extends AbstractPostgresDao implements VehicleModelDao {

    PostgresVehicleModelDao(Connection connection, JdbcEntityLoader loader) {
        super(connection, loader);
    }

    @Override
    public VehicleModel save(VehicleModel model) {
        if (model.id() != null) {
            throw new IllegalArgumentException("model is already persistent");
        }
        long id = execute("Could not save vehicle model", () -> JdbcSupport.insert(connection,
                "INSERT INTO \"model\" (brand, name, release_price, release_year) VALUES (?, ?, ?, ?)",
                stmt -> {
                    stmt.setString(1, model.brandName());
                    stmt.setString(2, model.name());
                    stmt.setBigDecimal(3, model.releasePrice());
                    stmt.setInt(4, model.releaseYear());
                }));
        model.assignId(id);
        return model;
    }

    @Override
    public Optional<VehicleModel> findById(long id) {
        return loader.vehicleModel(id);
    }

    @Override
    public Optional<VehicleModel> findByName(String name) {
        return execute("Could not find model by name", () -> {
            try (PreparedStatement stmt = connection.prepareStatement(
                    "SELECT id, brand, name, release_price, release_year FROM \"model\" WHERE LOWER(name) = LOWER(?)")) {
                stmt.setString(1, name);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return Optional.of(new VehicleModel(
                                rs.getLong("id"),
                                rs.getString("brand"),
                                rs.getString("name"),
                                rs.getBigDecimal("release_price"),
                                rs.getInt("release_year")));
                    }
                    return Optional.empty();
                }
            }
        });
    }

    @Override
    public List<VehicleModel> findByBrand(String brandName) {
        return execute("Could not find models by brand", () -> {
            try (PreparedStatement stmt = connection.prepareStatement(
                    "SELECT id, brand, name, release_price, release_year FROM \"model\" WHERE LOWER(brand) = LOWER(?)")) {
                stmt.setString(1, brandName);
                try (ResultSet rs = stmt.executeQuery()) {
                    List<VehicleModel> models = new ArrayList<>();
                    while (rs.next()) {
                        models.add(new VehicleModel(
                                rs.getLong("id"),
                                rs.getString("brand"),
                                rs.getString("name"),
                                rs.getBigDecimal("release_price"),
                                rs.getInt("release_year")));
                    }
                    return List.copyOf(models);
                }
            }
        });
    }

    @Override
    public List<VehicleModel> findAll() {
        return execute("Could not find all models", () -> {
            try (PreparedStatement stmt = connection.prepareStatement(
                    "SELECT id, brand, name, release_price, release_year FROM \"model\"")) {
                try (ResultSet rs = stmt.executeQuery()) {
                    List<VehicleModel> models = new ArrayList<>();
                    while (rs.next()) {
                        models.add(new VehicleModel(
                                rs.getLong("id"),
                                rs.getString("brand"),
                                rs.getString("name"),
                                rs.getBigDecimal("release_price"),
                                rs.getInt("release_year")));
                    }
                    return List.copyOf(models);
                }
            }
        });
    }

    @Override
    public void delete(long modelId) {
        execute("Could not delete vehicle model", () -> {
            try (PreparedStatement stmt = connection.prepareStatement(
                    "DELETE FROM \"model\" WHERE id = ?")) {
                stmt.setLong(1, modelId);
                stmt.executeUpdate();
                return null;
            }
        });
    }

    @Override
    public boolean hasVehicles(long modelId) {
        return execute("Could not check vehicles for model", () -> {
            try (PreparedStatement stmt = connection.prepareStatement(
                    "SELECT 1 FROM \"vehicle\" WHERE model = ? LIMIT 1")) {
                stmt.setLong(1, modelId);
                try (ResultSet rs = stmt.executeQuery()) {
                    return rs.next();
                }
            }
        });
    }
}
