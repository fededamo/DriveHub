package it.unifi.drivehub.service;

import it.unifi.drivehub.dao.RentDao;
import it.unifi.drivehub.dao.RequestDao;
import it.unifi.drivehub.dao.SaleDao;
import it.unifi.drivehub.dao.TestDriveDao;
import it.unifi.drivehub.domain.operation.Rent;
import it.unifi.drivehub.domain.operation.Request;
import it.unifi.drivehub.domain.operation.Sale;
import it.unifi.drivehub.domain.operation.TestDrive;

import java.util.List;

/**
 * Servizio di sola lettura per consultare le operazioni di sistema on-demand.
 */
public final class OperationQueryService {

    private final RequestDao requestDao;
    private final TestDriveDao testDriveDao;
    private final RentDao rentDao;
    private final SaleDao saleDao;

    public OperationQueryService(RequestDao requestDao, TestDriveDao testDriveDao, RentDao rentDao, SaleDao saleDao) {
        this.requestDao = requestDao;
        this.testDriveDao = testDriveDao;
        this.rentDao = rentDao;
        this.saleDao = saleDao;
    }

    public List<Request> getPendingRequests() {
        return requestDao.findPending();
    }

    public List<Request> getClientRequests(Long clientId) {
        return requestDao.findByClientId(clientId);
    }

    public List<TestDrive> getClientTestDrives(Long clientId) {
        return testDriveDao.findByClientId(clientId);
    }

    public List<Rent> getClientRents(Long clientId) {
        return rentDao.findByClientId(clientId);
    }

    public List<Sale> getClientSales(Long clientId) {
        return saleDao.findByClientId(clientId);
    }

    public List<Sale> getAllSales() {
        return saleDao.findAll();
    }
}

