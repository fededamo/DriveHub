package it.unifi.drivehub.dao;

import it.unifi.drivehub.domain.vehicle.Brand;

import java.sql.Connection;
import java.util.List;
import java.util.Optional;

/**
 * Interfaccia DAO per i Brand.
 */
public interface BrandDao {

    List<Brand> findAll();

    Optional<Brand> findByName(String name);

    void save(Brand brand, Connection conn);

    void delete(String name, Connection conn);

    boolean hasModels(String brandName);
}

