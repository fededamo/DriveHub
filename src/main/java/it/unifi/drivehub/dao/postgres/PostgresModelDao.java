package it.unifi.drivehub.dao.postgres;

import it.unifi.drivehub.dao.DatabaseManager;
import it.unifi.drivehub.dao.ModelDao;
import it.unifi.drivehub.domain.vehicle.Model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class PostgresModelDao implements ModelDao {

    private final DatabaseManager db;

    public PostgresModelDao(DatabaseManager db) {
        this.db = db;
    }

    @Override
    public List<Model> findAll() {
        List<Model> models = new ArrayList<>();
        String sql = "SELECT id, brand, name, release_price, release_year FROM model ORDER BY brand ASC, name ASC";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                models.add(Model.create(
                        rs.getLong("id"),
                        rs.getString("brand"),
                        rs.getString("name"),
                        rs.getBigDecimal("release_price"),
                        rs.getInt("release_year")
                ));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Errore durante il recupero dei modelli.", e);
        }
        return models;
    }

    @Override
    public List<Model> findByBrand(String brandName) {
        List<Model> models = new ArrayList<>();
        String sql = "SELECT id, brand, name, release_price, release_year FROM model WHERE brand = ? ORDER BY name ASC";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, brandName);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    models.add(Model.create(
                            rs.getLong("id"),
                            rs.getString("brand"),
                            rs.getString("name"),
                            rs.getBigDecimal("release_price"),
                            rs.getInt("release_year")
                    ));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Errore durante la ricerca dei modelli per il brand: " + brandName, e);
        }
        return models;
    }

    @Override
    public Optional<Model> findById(Long id) {
        String sql = "SELECT id, brand, name, release_price, release_year FROM model WHERE id = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(Model.create(
                            rs.getLong("id"),
                            rs.getString("brand"),
                            rs.getString("name"),
                            rs.getBigDecimal("release_price"),
                            rs.getInt("release_year")
                    ));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Errore durante la ricerca del modello id: " + id, e);
        }
        return Optional.empty();
    }

    @Override
    public Model save(Model model, Connection conn) {
        String sql = "INSERT INTO model (brand, name, release_price, release_year) VALUES (?, ?, ?, ?) RETURNING id";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, model.getBrand());
            ps.setString(2, model.getName());
            ps.setBigDecimal(3, model.getReleasePrice());
            ps.setInt(4, model.getReleaseYear());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    long id = rs.getLong(1);
                    return Model.create(id, model.getBrand(), model.getName(), model.getReleasePrice(), model.getReleaseYear());
                }
                throw new SQLException("Inserimento modello non ha restituito l'ID generato.");
            }
        } catch (SQLException e) {
            throw new RuntimeException("Errore durante il salvataggio del modello: " + model.getName(), e);
        }
    }

    @Override
    public void delete(Long id, Connection conn) {
        String sql = "DELETE FROM model WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Errore durante la cancellazione del modello id: " + id, e);
        }
    }

    @Override
    public boolean hasVehicles(Long modelId) {
        String sql = "SELECT EXISTS(SELECT 1 FROM vehicle WHERE model = ?) AS has_vehicles";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, modelId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getBoolean("has_vehicles");
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Errore durante la verifica dei veicoli per il modello id: " + modelId, e);
        }
        return false;
    }
}

