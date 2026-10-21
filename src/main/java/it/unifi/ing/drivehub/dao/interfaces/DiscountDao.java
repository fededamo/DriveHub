package it.unifi.ing.drivehub.dao.interfaces;

import it.unifi.ing.drivehub.domain.vehicles.ModelDiscount;
import it.unifi.ing.drivehub.domain.vehicles.VehicleDiscount;

import java.util.Optional;

public interface DiscountDao {
    Optional<ModelDiscount> findModelDiscount(long modelId);
    Optional<VehicleDiscount> findVehicleDiscount(long vehicleId);
    void saveModelDiscount(ModelDiscount discount);
    void saveVehicleDiscount(VehicleDiscount discount);
    void removeModelDiscount(long modelId);
    void removeVehicleDiscount(long vehicleId);
}
