package it.unifi.ing.drivehub.dao.postgres;

import it.unifi.ing.drivehub.dao.interfaces.BrandDao;
import it.unifi.ing.drivehub.domain.vehicles.Brand;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

final class PostgresBrandDao extends AbstractPostgresDao implements BrandDao {

    PostgresBrandDao(Connection connection, JdbcEntityLoader loader) {
        super(connection, loader);
    }

    @Override
    public Brand save(Brand brand) {
        execute("Could not save brand", () -> {
            try (PreparedStatement check = connection.prepareStatement(
                    "SELECT 1 FROM \"brand\" WHERE LOWER(name) = LOWER(?)")) {
                check.setString(1, brand.name());
                try (ResultSet rs = check.executeQuery()) {
                    if (rs.next()) {
                        return null;
                    }
                }
            }
            try (PreparedStatement stmt = connection.prepareStatement(
                    "INSERT INTO \"brand\" (name) VALUES (?)")) {
                stmt.setString(1, brand.name());
                stmt.executeUpdate();
                return null;
            }
        });
        return brand;
    }

    @Override
    public Optional<Brand> findByName(String name) {
        return loader.brand(name);
    }

    @Override
    public List<Brand> findAll() {
        return execute("Could not find all brands", () -> {
            try (PreparedStatement stmt = connection.prepareStatement(
                    "SELECT name FROM \"brand\" ORDER BY name ASC")) {
                try (ResultSet rs = stmt.executeQuery()) {
                    List<Brand> brands = new ArrayList<>();
                    while (rs.next()) {
                        brands.add(new Brand(rs.getString("name")));
                    }
                    return List.copyOf(brands);
                }
            }
        });
    }

    @Override
    public void delete(String brandName) {
        execute("Could not delete brand", () -> {
            try (PreparedStatement stmt = connection.prepareStatement(
                    "DELETE FROM \"brand\" WHERE LOWER(name) = LOWER(?)")) {
                stmt.setString(1, brandName);
                stmt.executeUpdate();
                return null;
            }
        });
    }

    @Override
    public boolean hasModels(String brandName) {
        return execute("Could not check models for brand", () -> {
            try (PreparedStatement stmt = connection.prepareStatement(
                    "SELECT 1 FROM \"model\" WHERE LOWER(brand) = LOWER(?) LIMIT 1")) {
                stmt.setString(1, brandName);
                try (ResultSet rs = stmt.executeQuery()) {
                    return rs.next();
                }
            }
        });
    }
}
