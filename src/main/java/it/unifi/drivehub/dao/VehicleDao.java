package it.unifi.drivehub.dao;

import it.unifi.drivehub.domain.vehicle.Vehicle;
import it.unifi.drivehub.domain.vehicle.VehicleState;

import java.sql.Connection;
import java.util.List;
import java.util.Optional;

/**
 * Interfaccia DAO per i Veicoli.
 * Include metodi per la gestione della concorrenza atomica sullo stato.
 */
public interface VehicleDao {

    List<Vehicle> findAll();

    List<Vehicle> findByState(VehicleState state);

    Optional<Vehicle> findById(Long id);

    Optional<Vehicle> findByVin(String vin);

    Vehicle save(Vehicle vehicle, Connection conn);

    void update(Vehicle vehicle, Connection conn);

    /**
     * Esegue una modifica atomica di stato con WHERE sullo stato atteso (ottimizzazione concorrenza).
     * Ritorna true se la riga è stata aggiornata, false se lo stato iniziale era diverso (race condition).
     */
    boolean updateStateAtomically(Long vehicleId, VehicleState expectedOldState, VehicleState newState, Connection conn);

    void delete(Long id, Connection conn);

    boolean hasAssociatedOperations(Long vehicleId);
}

