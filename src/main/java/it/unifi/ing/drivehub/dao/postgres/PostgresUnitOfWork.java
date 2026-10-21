package it.unifi.ing.drivehub.dao.postgres;

import it.unifi.ing.drivehub.dao.interfaces.BankAccountDao;
import it.unifi.ing.drivehub.dao.interfaces.BrandDao;
import it.unifi.ing.drivehub.dao.interfaces.DiscountDao;
import it.unifi.ing.drivehub.dao.interfaces.RentalDao;
import it.unifi.ing.drivehub.dao.interfaces.RequestDao;
import it.unifi.ing.drivehub.dao.interfaces.SaleDao;
import it.unifi.ing.drivehub.dao.interfaces.TestDriveDao;
import it.unifi.ing.drivehub.dao.interfaces.UnitOfWork;
import it.unifi.ing.drivehub.dao.interfaces.UserDao;
import it.unifi.ing.drivehub.dao.interfaces.VehicleDao;
import it.unifi.ing.drivehub.dao.interfaces.VehicleModelDao;

import java.sql.Connection;
import java.sql.SQLException;

final class PostgresUnitOfWork implements UnitOfWork {

    private final Connection connection;
    private final UserDao users;
    private final BankAccountDao bankAccounts;
    private final BrandDao brands;
    private final VehicleModelDao vehicleModels;
    private final VehicleDao vehicles;
    private final DiscountDao discounts;
    private final RentalDao rentals;
    private final TestDriveDao testDrives;
    private final RequestDao requests;
    private final SaleDao sales;
    private boolean completed;
    private boolean closed;

    PostgresUnitOfWork(Connection connection) {
        this.connection = connection;
        JdbcEntityLoader loader = new JdbcEntityLoader(connection);
        users = new PostgresUserDao(connection, loader);
        bankAccounts = new PostgresBankAccountDao(connection, loader);
        brands = new PostgresBrandDao(connection, loader);
        vehicleModels = new PostgresVehicleModelDao(connection, loader);
        vehicles = new PostgresVehicleDao(connection, loader);
        discounts = new PostgresDiscountDao(connection, loader);
        rentals = new PostgresRentalDao(connection, loader);
        testDrives = new PostgresTestDriveDao(connection, loader);
        requests = new PostgresRequestDao(connection, loader);
        sales = new PostgresSaleDao(connection, loader);
    }

    @Override public UserDao users() { ensureOpen(); return users; }
    @Override public BankAccountDao bankAccounts() { ensureOpen(); return bankAccounts; }
    @Override public BrandDao brands() { ensureOpen(); return brands; }
    @Override public VehicleModelDao vehicleModels() { ensureOpen(); return vehicleModels; }
    @Override public VehicleDao vehicles() { ensureOpen(); return vehicles; }
    @Override public DiscountDao discounts() { ensureOpen(); return discounts; }
    @Override public RentalDao rentals() { ensureOpen(); return rentals; }
    @Override public TestDriveDao testDrives() { ensureOpen(); return testDrives; }
    @Override public RequestDao requests() { ensureOpen(); return requests; }
    @Override public SaleDao sales() { ensureOpen(); return sales; }

    @Override
    public void commit() {
        ensureActive();
        try {
            connection.commit();
            completed = true;
        } catch (SQLException exception) {
            rollbackAfterFailure(exception);
            throw new DatabaseException("Could not commit unit of work", exception);
        }
    }

    @Override
    public void rollback() {
        ensureActive();
        try {
            connection.rollback();
            completed = true;
        } catch (SQLException exception) {
            throw new DatabaseException("Could not roll back unit of work", exception);
        }
    }

    @Override
    public void close() {
        if (closed) {
            return;
        }
        DatabaseException pending = null;
        if (!completed) {
            try {
                connection.rollback();
            } catch (SQLException exception) {
                pending = new DatabaseException("Could not roll back uncommitted unit of work", exception);
            }
        }
        try {
            connection.close();
        } catch (SQLException exception) {
            if (pending != null) {
                pending.addSuppressed(exception);
            } else {
                pending = new DatabaseException("Could not close database connection", exception);
            }
        } finally {
            closed = true;
        }
        if (pending != null) {
            throw pending;
        }
    }

    private void ensureOpen() {
        if (closed) {
            throw new IllegalStateException("Unit of work has already been closed");
        }
    }

    private void ensureActive() {
        ensureOpen();
        if (completed) {
            throw new IllegalStateException("Unit of work has already been completed");
        }
    }

    private void rollbackAfterFailure(SQLException failure) {
        try {
            connection.rollback();
        } catch (SQLException rollbackException) {
            failure.addSuppressed(rollbackException);
        }
    }
}
