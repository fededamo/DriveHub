package it.unifi.drivehub.dao.postgres;

import it.unifi.drivehub.dao.BrandDao;
import it.unifi.drivehub.dao.DatabaseManager;
import it.unifi.drivehub.domain.vehicle.Brand;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class PostgresBrandDao implements BrandDao {

    private final DatabaseManager db;

    public PostgresBrandDao(DatabaseManager db) {
        this.db = db;
    }

    @Override
    public List<Brand> findAll() {
        List<Brand> brands = new ArrayList<>();
        String sql = "SELECT name FROM brand ORDER BY name ASC";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                brands.add(Brand.create(rs.getString("name")));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Errore durante il recupero dei marchi.", e);
        }
        return brands;
    }

    @Override
    public Optional<Brand> findByName(String name) {
        String sql = "SELECT name FROM brand WHERE LOWER(name) = LOWER(?)";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, name.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(Brand.create(rs.getString("name")));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Errore durante la ricerca del marchio: " + name, e);
        }
        return Optional.empty();
    }

    @Override
    public void save(Brand brand, Connection conn) {
        String sql = "INSERT INTO brand (name) VALUES (?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, brand.getName());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Errore durante l'inserimento del marchio: " + brand.getName(), e);
        }
    }

    @Override
    public void delete(String name, Connection conn) {
        String sql = "DELETE FROM brand WHERE name = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, name);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Errore durante la cancellazione del marchio: " + name, e);
        }
    }

    @Override
    public boolean hasModels(String brandName) {
        String sql = "SELECT EXISTS(SELECT 1 FROM model WHERE brand = ?) AS has_models";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, brandName);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getBoolean("has_models");
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Errore durante la verifica dei modelli per il brand: " + brandName, e);
        }
        return false;
    }
}

