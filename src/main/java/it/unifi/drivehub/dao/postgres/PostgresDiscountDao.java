package it.unifi.drivehub.dao.postgres;

import it.unifi.drivehub.dao.DatabaseManager;
import it.unifi.drivehub.dao.DiscountDao;
import it.unifi.drivehub.domain.discount.ModelDiscount;
import it.unifi.drivehub.domain.discount.VehicleDiscount;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class PostgresDiscountDao implements DiscountDao {

    private final DatabaseManager db;

    public PostgresDiscountDao(DatabaseManager db) {
        this.db = db;
    }

    @Override
    public Optional<ModelDiscount> findModelDiscount(Long modelId) {
        String sql = "SELECT model, value FROM model_discount WHERE model = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, modelId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(ModelDiscount.create(rs.getLong("model"), rs.getInt("value")));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Errore durante la ricerca dello sconto modello: " + modelId, e);
        }
        return Optional.empty();
    }

    @Override
    public Optional<VehicleDiscount> findVehicleDiscount(Long vehicleId) {
        String sql = "SELECT vehicle, value FROM vehicle_discount WHERE vehicle = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, vehicleId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(VehicleDiscount.create(rs.getLong("vehicle"), rs.getInt("value")));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Errore durante la ricerca dello sconto veicolo: " + vehicleId, e);
        }
        return Optional.empty();
    }

    @Override
    public List<ModelDiscount> findAllModelDiscounts() {
        List<ModelDiscount> list = new ArrayList<>();
        String sql = "SELECT model, value FROM model_discount";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(ModelDiscount.create(rs.getLong("model"), rs.getInt("value")));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Errore durante il recupero di tutti gli sconti modello.", e);
        }
        return list;
    }

    @Override
    public List<VehicleDiscount> findAllVehicleDiscounts() {
        List<VehicleDiscount> list = new ArrayList<>();
        String sql = "SELECT vehicle, value FROM vehicle_discount";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(VehicleDiscount.create(rs.getLong("vehicle"), rs.getInt("value")));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Errore durante il recupero di tutti gli sconti veicolo.", e);
        }
        return list;
    }

    @Override
    public void saveModelDiscount(ModelDiscount discount, Connection conn) {
        String sql = "INSERT INTO model_discount (model, value) VALUES (?, ?) " +
                "ON CONFLICT (model) DO UPDATE SET value = EXCLUDED.value";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, discount.getModelId());
            ps.setInt(2, discount.getValue());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Errore durante il salvataggio dello sconto modello.", e);
        }
    }

    @Override
    public void deleteModelDiscount(Long modelId, Connection conn) {
        String sql = "DELETE FROM model_discount WHERE model = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, modelId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Errore durante l'eliminazione dello sconto modello: " + modelId, e);
        }
    }

    @Override
    public void saveVehicleDiscount(VehicleDiscount discount, Connection conn) {
        String sql = "INSERT INTO vehicle_discount (vehicle, value) VALUES (?, ?) " +
                "ON CONFLICT (vehicle) DO UPDATE SET value = EXCLUDED.value";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, discount.getVehicleId());
            ps.setInt(2, discount.getValue());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Errore durante il salvataggio dello sconto veicolo.", e);
        }
    }

    @Override
    public void deleteVehicleDiscount(Long vehicleId, Connection conn) {
        String sql = "DELETE FROM vehicle_discount WHERE vehicle = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, vehicleId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Errore durante l'eliminazione dello sconto veicolo: " + vehicleId, e);
        }
    }
}

