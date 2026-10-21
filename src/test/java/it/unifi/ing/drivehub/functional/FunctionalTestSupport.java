package it.unifi.ing.drivehub.functional;

import it.unifi.ing.drivehub.business.core.TransactionRunner;
import it.unifi.ing.drivehub.business.security.Pbkdf2PasswordHasher;
import it.unifi.ing.drivehub.business.services.*;
import it.unifi.ing.drivehub.dao.interfaces.DaoFactory;
import it.unifi.ing.drivehub.dao.postgres.DatabaseBootstrap;
import it.unifi.ing.drivehub.dao.postgres.PostgresDaoFactory;
import it.unifi.ing.drivehub.domain.users.BankAccount;
import it.unifi.ing.drivehub.domain.users.Role;
import it.unifi.ing.drivehub.domain.users.User;
import it.unifi.ing.drivehub.domain.vehicles.*;
import it.unifi.ing.drivehub.presentation.core.ServiceUiGateway;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.BeforeEach;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.util.UUID;

abstract class FunctionalTestSupport {
    protected DaoFactory factory;
    protected TransactionRunner transactions;
    protected AuthService auth;
    protected BankAccountService bankAccounts;
    protected CatalogService catalog;
    protected PricingService pricing;
    protected SalesService sales;
    protected RentalService rentals;
    protected TestDriveService testDrives;
    protected ServiceUiGateway gateway;

    protected User customer;
    protected User salesman;
    protected User manager;
    protected Brand brand;
    protected VehicleModel model;
    protected Vehicle saleVehicle;
    protected Vehicle rentalVehicle;
    protected Vehicle waitingVehicle;

    @BeforeEach
    void setUpIsolatedEnvironment() {
        JdbcDataSource source = new JdbcDataSource();
        source.setURL("jdbc:h2:mem:ft_" + UUID.randomUUID().toString().replace("-", "")
                + ";MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DEFAULT_NULL_ORDERING=HIGH;DB_CLOSE_DELAY=-1");
        source.setUser("sa");
        source.setPassword("");
        new DatabaseBootstrap(source, false).initialize();

        factory = new PostgresDaoFactory(source);
        transactions = new TransactionRunner(factory);

        auth = new AuthService(factory, new Pbkdf2PasswordHasher(10_000, 16, 128, new SecureRandom()));
        bankAccounts = new BankAccountService(factory);
        catalog = new CatalogService(factory);
        pricing = new PricingService(factory);
        sales = new SalesService(factory, pricing);
        rentals = new RentalService(factory);
        testDrives = new TestDriveService(factory);

        gateway = new ServiceUiGateway(auth, bankAccounts, catalog, sales, rentals, testDrives, pricing);

        customer = register("Mario", "Rossi", "mario@example.com", Role.CLIENT);
        salesman = register("Luigi", "Verdi", "luigi@example.com", Role.SALESMAN);
        manager = register("Luca", "Bianchi", "luca@example.com", Role.MANAGER);

        // Pre-popola brand e model
        brand = transactions.execute(u -> u.brands().save(new Brand("Alfa Romeo")));
        model = transactions.execute(u -> u.vehicleModels().save(new VehicleModel(null, "Alfa Romeo", "Giulia", new BigDecimal("40000.00"), 2023)));

        saleVehicle = transactions.execute(u -> u.vehicles().save(new Vehicle(null, model.requireId(),
                "VIN-SALE-1", "AA100AA", 15000, new BigDecimal("40000.00"), 2023, VehicleState.SALE)));

        rentalVehicle = transactions.execute(u -> u.vehicles().save(new Vehicle(null, model.requireId(),
                "VIN-RENT-1", "BB200BB", 30000, new BigDecimal("100.00"), 2022, VehicleState.RENT)));

        waitingVehicle = transactions.execute(u -> u.vehicles().save(new Vehicle(null, model.requireId(),
                "VIN-WAIT-1", "CC300CC", 95000, new BigDecimal("12000.00"), 2019, VehicleState.WAITING)));

        // Ricarica saldo iniziale sul conto del cliente (es. 50000)
        BankAccount acc = bankAccounts.getAccountByClientId(customer.requireId());
        bankAccounts.deposit(acc.iban(), new BigDecimal("50000.00"));
    }

    protected User register(String name, String surname, String email, Role role) {
        return auth.register(new RegistrationRequest(name, surname, email, role), "pass-123".toCharArray());
    }
}
