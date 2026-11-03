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

    default Optional<Vehicle> findById(Long id, Connection conn) {
        return findById(id);
    }

    /** Rilegge e blocca il veicolo nella transazione attiva del chiamante. */
    Optional<Vehicle> findByIdForUpdate(Long id, Connection conn);

    Optional<Vehicle> findByVin(String vin);

    default Optional<Vehicle> findByVin(String vin, Connection conn) {
        return findByVin(vin);
    }

    /** Rilegge il VIN noto con lock fino al commit/rollback nella transazione attiva. */
    Optional<Vehicle> findByVinForUpdate(String vin, Connection conn);

    Vehicle save(Vehicle vehicle, Connection conn);

    void update(Vehicle vehicle, Connection conn);

    /** Aggiorna la sola scelta commerciale senza sovrascrivere stato o caratteristiche. */
    void updatePricing(Vehicle vehicle, Connection conn);

    /**
     * Esegue una modifica atomica di stato con WHERE sullo stato atteso (ottimizzazione concorrenza).
     * Ritorna true se la riga è stata aggiornata, false se lo stato iniziale era diverso (race condition).
     */
    boolean updateStateAtomically(Long vehicleId, VehicleState expectedOldState, VehicleState newState, Connection conn);

    void delete(Long id, Connection conn);

    boolean hasAssociatedOperations(Long vehicleId);

    /** Presenza di qualsiasi storico sulla connessione transazionale del chiamante. */
    boolean hasAssociatedOperations(Long vehicleId, Connection conn);
}

