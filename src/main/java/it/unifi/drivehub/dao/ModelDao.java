package it.unifi.drivehub.dao;

import it.unifi.drivehub.domain.vehicle.Model;

import java.sql.Connection;
import java.util.List;
import java.util.Optional;

/**
 * Interfaccia DAO per i Modelli.
 */
public interface ModelDao {

    List<Model> findAll();

    List<Model> findByBrand(String brandName);

    Optional<Model> findById(Long id);

    Model save(Model model, Connection conn);

    void delete(Long id, Connection conn);

    boolean hasVehicles(Long modelId);
}

