package it.unifi.ing.drivehub.dao.interfaces;

import it.unifi.ing.drivehub.domain.sales.Sale;

import java.util.List;
import java.util.Optional;

public interface SaleDao {
    Sale save(Sale sale);
    Optional<Sale> findById(long id);
    List<Sale> findByClientId(long clientId);
    List<Sale> findBySalesmanId(long salesmanId);
    List<Sale> findByVehicleId(long vehicleId);
    List<Sale> findAll();
    boolean hasOperationsForVehicle(long vehicleId);
    boolean hasOperationsForClient(long clientId);
    boolean hasOperationsForSalesman(long salesmanId);
}
