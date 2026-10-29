package it.unifi.drivehub.dao;

import it.unifi.drivehub.domain.operation.Request;

import java.sql.Connection;
import java.util.List;
import java.util.Optional;

/**
 * Interfaccia DAO per le richieste (Request).
 */
public interface RequestDao {

    List<Request> findAll();

    List<Request> findPending();

    List<Request> findByClientId(Long clientId);

    Optional<Request> findById(Long id);

    default Optional<Request> findById(Long id, Connection conn) {
        return findById(id);
    }

    Optional<Request> findActivePurchaseRequestForVehicle(Long vehicleId);

    Request save(Request request, Connection conn);

    void update(Request request, Connection conn);

    void delete(Long id, Connection conn);
}

