package it.unifi.ing.drivehub.dao.interfaces;

import it.unifi.ing.drivehub.domain.sales.Request;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface RequestDao {
    Request save(Request request);
    void updateExpDate(long requestId, LocalDate expDate);
    void updateAuthorization(long requestId, long salesmanId, LocalDate expDate);
    void delete(long requestId);
    Optional<Request> findById(long id);
    default Optional<Request> findByIdForUpdate(long id) { return findById(id); }
    Optional<Request> findByVehicleId(long vehicleId);
    List<Request> findByClientId(long clientId);
    List<Request> findAll();
    List<Request> findExpiredRequests(LocalDate asOfDate);
}
