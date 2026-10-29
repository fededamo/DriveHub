package it.unifi.drivehub.dao;

import it.unifi.drivehub.domain.operation.Rent;

import java.sql.Connection;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Interfaccia DAO per i Noleggi.
 */
public interface RentDao {

    List<Rent> findAll();

    List<Rent> findByClientId(Long clientId);

    List<Rent> findByVehicleId(Long vehicleId);

    List<Rent> findFutureByVehicleId(Long vehicleId);

    boolean isVehicleAvailableForRent(Long vehicleId, LocalDate start, LocalDate end, Connection conn);

    Rent save(Rent rent, Connection conn);

    void delete(Long id, Connection conn);

    List<Rent> deleteFutureByVehicleId(Long vehicleId, Connection conn);
}

