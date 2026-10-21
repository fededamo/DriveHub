package it.unifi.ing.drivehub.dao.interfaces;

/** A transaction-scoped set of DAO ports. Closing without commit must roll back. */
public interface UnitOfWork extends AutoCloseable {
    UserDao users();
    BankAccountDao bankAccounts();
    BrandDao brands();
    VehicleModelDao vehicleModels();
    default VehicleModelDao models() { return vehicleModels(); }
    VehicleDao vehicles();
    DiscountDao discounts();
    RentalDao rentals();
    TestDriveDao testDrives();
    RequestDao requests();
    SaleDao sales();

    void commit();
    void rollback();

    @Override
    void close();
}
