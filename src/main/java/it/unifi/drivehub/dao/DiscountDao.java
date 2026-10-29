package it.unifi.drivehub.dao;

import it.unifi.drivehub.domain.discount.ModelDiscount;
import it.unifi.drivehub.domain.discount.VehicleDiscount;

import java.sql.Connection;
import java.util.List;
import java.util.Optional;

/**
 * Interfaccia DAO per la gestione degli sconti su modello e veicolo.
 */
public interface DiscountDao {

    Optional<ModelDiscount> findModelDiscount(Long modelId);

    Optional<VehicleDiscount> findVehicleDiscount(Long vehicleId);

    List<ModelDiscount> findAllModelDiscounts();

    List<VehicleDiscount> findAllVehicleDiscounts();

    void saveModelDiscount(ModelDiscount discount, Connection conn);

    void deleteModelDiscount(Long modelId, Connection conn);

    void saveVehicleDiscount(VehicleDiscount discount, Connection conn);

    void deleteVehicleDiscount(Long vehicleId, Connection conn);
}

