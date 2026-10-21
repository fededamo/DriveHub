package it.unifi.ing.drivehub.dao.interfaces;

import it.unifi.ing.drivehub.domain.vehicles.VehicleModel;

import java.util.List;
import java.util.Optional;

public interface VehicleModelDao {
    VehicleModel save(VehicleModel model);
    Optional<VehicleModel> findById(long id);
    Optional<VehicleModel> findByName(String name);
    List<VehicleModel> findByBrand(String brandName);
    List<VehicleModel> findAll();
    void delete(long modelId);
    boolean hasVehicles(long modelId);
}
