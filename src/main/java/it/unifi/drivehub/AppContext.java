package it.unifi.drivehub;

import it.unifi.drivehub.dao.BankAccountDao;
import it.unifi.drivehub.dao.BrandDao;
import it.unifi.drivehub.dao.DatabaseManager;
import it.unifi.drivehub.dao.DiscountDao;
import it.unifi.drivehub.dao.ModelDao;
import it.unifi.drivehub.dao.RentDao;
import it.unifi.drivehub.dao.RequestDao;
import it.unifi.drivehub.dao.SaleDao;
import it.unifi.drivehub.dao.TestDriveDao;
import it.unifi.drivehub.dao.UserDao;
import it.unifi.drivehub.dao.VehicleDao;
import it.unifi.drivehub.dao.postgres.PostgresBankAccountDao;
import it.unifi.drivehub.dao.postgres.PostgresBrandDao;
import it.unifi.drivehub.dao.postgres.PostgresDiscountDao;
import it.unifi.drivehub.dao.postgres.PostgresModelDao;
import it.unifi.drivehub.dao.postgres.PostgresRentDao;
import it.unifi.drivehub.dao.postgres.PostgresRequestDao;
import it.unifi.drivehub.dao.postgres.PostgresSaleDao;
import it.unifi.drivehub.dao.postgres.PostgresTestDriveDao;
import it.unifi.drivehub.dao.postgres.PostgresUserDao;
import it.unifi.drivehub.dao.postgres.PostgresVehicleDao;
import it.unifi.drivehub.domain.user.User;
import it.unifi.drivehub.event.VehicleEventPublisher;
import it.unifi.drivehub.security.PasswordHasher;
import it.unifi.drivehub.security.Pbkdf2PasswordHasher;
import it.unifi.drivehub.service.AuthService;
import it.unifi.drivehub.service.CatalogService;
import it.unifi.drivehub.service.OperationQueryService;
import it.unifi.drivehub.service.PriceCalculatorService;
import it.unifi.drivehub.service.PurchaseService;
import it.unifi.drivehub.service.RentalService;
import it.unifi.drivehub.service.SaleToDealershipService;
import it.unifi.drivehub.service.TestDriveCancellationObserver;
import it.unifi.drivehub.service.TestDriveService;

/**
 * Composition Root del sistema (Pagina 5 degli appunti).
 * Punto unico e centralizzato in cui vengono istanziati i componenti
 * e configurate le dipendenze (Inversion of Control & Dependency Injection).
 */
public final class AppContext {

    private static AppContext instance;

    private final DatabaseManager databaseManager;
    private final UserDao userDao;
    private final BankAccountDao bankAccountDao;
    private final BrandDao brandDao;
    private final ModelDao modelDao;
    private final DiscountDao discountDao;
    private final VehicleDao vehicleDao;
    private final RequestDao requestDao;
    private final TestDriveDao testDriveDao;
    private final RentDao rentDao;
    private final SaleDao saleDao;

    private final PasswordHasher passwordHasher;
    private final VehicleEventPublisher vehicleEventPublisher;

    private final PriceCalculatorService priceCalculatorService;
    private final AuthService authService;
    private final PurchaseService purchaseService;
    private final SaleToDealershipService saleToDealershipService;
    private final RentalService rentalService;
    private final TestDriveService testDriveService;
    private final CatalogService catalogService;
    private final OperationQueryService operationQueryService;

    private User currentUser;

    private AppContext() {
        // 1. Persistenza & Database Manager
        this.databaseManager = new DatabaseManager();

        // 2. Data Access Objects (PostgreSQL implementations)
        this.userDao = new PostgresUserDao(databaseManager);
        this.bankAccountDao = new PostgresBankAccountDao(databaseManager);
        this.brandDao = new PostgresBrandDao(databaseManager);
        this.modelDao = new PostgresModelDao(databaseManager);
        this.discountDao = new PostgresDiscountDao(databaseManager);
        this.vehicleDao = new PostgresVehicleDao(databaseManager);
        this.requestDao = new PostgresRequestDao(databaseManager);
        this.testDriveDao = new PostgresTestDriveDao(databaseManager);
        this.rentDao = new PostgresRentDao(databaseManager);
        this.saleDao = new PostgresSaleDao(databaseManager);

        // 3. Sicurezza & Event Publisher
        this.passwordHasher = new Pbkdf2PasswordHasher();
        this.vehicleEventPublisher = new VehicleEventPublisher();

        // Registrazione Observer per cancellazione automatica Test Drive futuri
        TestDriveCancellationObserver tdObserver = new TestDriveCancellationObserver(databaseManager, testDriveDao);
        this.vehicleEventPublisher.attach(tdObserver);

        // 4. Servizi di Dominio & Business
        this.priceCalculatorService = new PriceCalculatorService();
        this.authService = new AuthService(databaseManager, userDao, bankAccountDao, passwordHasher);
        this.purchaseService = new PurchaseService(databaseManager, vehicleDao, modelDao, discountDao,
                requestDao, bankAccountDao, saleDao, priceCalculatorService, vehicleEventPublisher);
        this.saleToDealershipService = new SaleToDealershipService(databaseManager, vehicleDao, modelDao,
                discountDao, requestDao, bankAccountDao, saleDao, priceCalculatorService);
        this.rentalService = new RentalService(databaseManager, vehicleDao, modelDao, discountDao,
                rentDao, bankAccountDao, priceCalculatorService, vehicleEventPublisher);
        this.testDriveService = new TestDriveService(databaseManager, testDriveDao, vehicleDao);
        this.catalogService = new CatalogService(databaseManager, brandDao, modelDao, vehicleDao, discountDao, priceCalculatorService);
        this.operationQueryService = new OperationQueryService(requestDao, testDriveDao, rentDao, saleDao);
    }

    public static synchronized AppContext getInstance() {
        if (instance == null) {
            instance = new AppContext();
        }
        return instance;
    }

    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }

    public AuthService getAuthService() {
        return authService;
    }

    public PurchaseService getPurchaseService() {
        return purchaseService;
    }

    public SaleToDealershipService getSaleToDealershipService() {
        return saleToDealershipService;
    }

    public RentalService getRentalService() {
        return rentalService;
    }

    public TestDriveService getTestDriveService() {
        return testDriveService;
    }

    public CatalogService getCatalogService() {
        return catalogService;
    }

    public OperationQueryService getOperationQueryService() {
        return operationQueryService;
    }

    public PriceCalculatorService getPriceCalculatorService() {
        return priceCalculatorService;
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public void setCurrentUser(User currentUser) {
        this.currentUser = currentUser;
    }

    public void logout() {
        this.currentUser = null;
    }
}

