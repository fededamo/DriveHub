package it.unifi.drivehub.dao;

import it.unifi.drivehub.domain.operation.Sale;

import java.sql.Connection;
import java.util.List;
import java.util.Optional;

/**
 * Interfaccia DAO per il registro di vendite, acquisti e penali.
 */
public interface SaleDao {

    List<Sale> findAll();

    List<Sale> findByClientId(Long clientId);

    Optional<Sale> findById(Long id);

    Sale save(Sale sale, Connection conn);

    boolean hasSaleOperationsForVehicle(Long vehicleId);
}

