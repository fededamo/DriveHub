package it.unifi.ing.drivehub.dao.interfaces;

import it.unifi.ing.drivehub.domain.vehicles.Vehicle;
import it.unifi.ing.drivehub.domain.vehicles.VehicleState;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface VehicleDao {
    Vehicle save(Vehicle vehicle);
    void updateState(long vehicleId, VehicleState state);
    void updatePrice(long vehicleId, BigDecimal price);
    void updateDetails(long vehicleId, String plate, int km, BigDecimal price, int year, VehicleState state);
    void delete(long vehicleId);
    Optional<Vehicle> findById(long id);
    default Optional<Vehicle> findByIdForUpdate(long id) { return findById(id); }
    default void update(Vehicle vehicle) {
        updateDetails(vehicle.requireId(), vehicle.licencePlate(), vehicle.kilometers(), vehicle.price(), vehicle.year(), vehicle.state());
    }
    Optional<Vehicle> findByVin(String vin);
    Optional<Vehicle> findByPlate(String plate);
    List<Vehicle> findByState(VehicleState state);
    List<Vehicle> findAll();
    boolean hasOperations(long vehicleId);
}
