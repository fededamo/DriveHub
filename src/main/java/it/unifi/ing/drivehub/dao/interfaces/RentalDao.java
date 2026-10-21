package it.unifi.ing.drivehub.dao.interfaces;

import it.unifi.ing.drivehub.domain.rentals.Rent;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface RentalDao {
    Rent save(Rent rent);
    Optional<Rent> findById(long id);
    List<Rent> findByClientId(long clientId);
    List<Rent> findByVehicleId(long vehicleId);
    List<Rent> findAll();
    boolean hasOverlappingRentals(long vehicleId, LocalDate start, LocalDate end);
    boolean isVehicleRentedOnDate(long vehicleId, LocalDate date);
    List<Rent> findFutureRentalsForVehicle(long vehicleId, LocalDate asOfDate);
    void delete(long rentId);
    boolean hasOperationsForVehicle(long vehicleId);
    boolean hasOperationsForClient(long clientId);
}
