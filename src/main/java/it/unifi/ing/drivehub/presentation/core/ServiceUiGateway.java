package it.unifi.ing.drivehub.presentation.core;

import it.unifi.ing.drivehub.business.services.AuthService;
import it.unifi.ing.drivehub.business.services.BankAccountService;
import it.unifi.ing.drivehub.business.services.CatalogService;
import it.unifi.ing.drivehub.business.services.PricingService;
import it.unifi.ing.drivehub.business.services.RegistrationRequest;
import it.unifi.ing.drivehub.business.services.RentalService;
import it.unifi.ing.drivehub.business.services.SalesService;
import it.unifi.ing.drivehub.business.services.TestDriveService;
import it.unifi.ing.drivehub.domain.rentals.Rent;
import it.unifi.ing.drivehub.domain.rentals.TestDrive;
import it.unifi.ing.drivehub.domain.sales.Request;
import it.unifi.ing.drivehub.domain.sales.Sale;
import it.unifi.ing.drivehub.domain.users.BankAccount;
import it.unifi.ing.drivehub.domain.users.Role;
import it.unifi.ing.drivehub.domain.users.User;
import it.unifi.ing.drivehub.domain.vehicles.Vehicle;
import it.unifi.ing.drivehub.domain.vehicles.VehicleModel;
import it.unifi.ing.drivehub.domain.vehicles.VehicleState;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class ServiceUiGateway implements UiGateway {

    private final AuthService authService;
    private final BankAccountService bankAccountService;
    private final CatalogService catalogService;
    private final SalesService salesService;
    private final RentalService rentalService;
    private final TestDriveService testDriveService;
    private final PricingService pricingService;

    public ServiceUiGateway(AuthService authService,
                            BankAccountService bankAccountService,
                            CatalogService catalogService,
                            SalesService salesService,
                            RentalService rentalService,
                            TestDriveService testDriveService,
                            PricingService pricingService) {
        this.authService = Objects.requireNonNull(authService, "authService");
        this.bankAccountService = Objects.requireNonNull(bankAccountService, "bankAccountService");
        this.catalogService = Objects.requireNonNull(catalogService, "catalogService");
        this.salesService = Objects.requireNonNull(salesService, "salesService");
        this.rentalService = Objects.requireNonNull(rentalService, "rentalService");
        this.testDriveService = Objects.requireNonNull(testDriveService, "testDriveService");
        this.pricingService = Objects.requireNonNull(pricingService, "pricingService");
    }

    @Override
    public UiModels.Session login(String email, char[] password) {
        processExpiredRequests();
        User user = authService.login(email, password);
        return new UiModels.Session(user.requireId(), user.fullName(), user.email(), user.role());
    }

    @Override
    public UiModels.Session register(String firstName, String lastName, String email, char[] password, Role role) {
        RegistrationRequest req = new RegistrationRequest(firstName, lastName, email, role);
        User user = authService.register(req, password);
        return new UiModels.Session(user.requireId(), user.fullName(), user.email(), user.role());
    }

    @Override
    public void processExpiredRequests() {
        salesService.processExpiredRequests(LocalDate.now());
    }

    @Override
    public void updateEmail(long userId, Role role, char[] currentPassword, String newEmail) {
        authService.updateEmail(userId, role, currentPassword, newEmail);
    }

    @Override
    public void updatePassword(long userId, Role role, char[] currentPassword, char[] newPassword) {
        authService.updatePassword(userId, role, currentPassword, newPassword);
    }

    @Override
    public void deleteAccount(long userId, Role role, char[] currentPassword) {
        authService.deleteAccount(userId, role, currentPassword);
    }

    @Override
    public UiModels.BankAccountItem getBankAccount(long clientId) {
        BankAccount acc = bankAccountService.getAccountForClient(clientId);
        return new UiModels.BankAccountItem(acc.iban(), acc.clientId(), acc.balance());
    }

    @Override
    public void depositFunds(long clientId, BigDecimal amount) {
        bankAccountService.depositFunds(clientId, amount);
    }

    @Override
    public void withdrawFunds(long clientId, BigDecimal amount) {
        bankAccountService.withdrawFunds(clientId, amount);
    }

    @Override
    public List<UiModels.VehicleItem> searchCatalog(String query, VehicleState state) {
        processExpiredRequests();
        List<Vehicle> list = (state == VehicleState.SALE)
                ? catalogService.availableForSale()
                : (state == VehicleState.RENT)
                ? catalogService.availableForRental()
                : catalogService.allVehicles();

        String q = query == null ? "" : query.trim().toLowerCase();
        List<UiModels.VehicleItem> items = new ArrayList<>();
        for (Vehicle v : list) {
            VehicleModel m = catalogService.model(v.modelId());
            String desc = m.brandName() + " " + m.name();
            if (q.isEmpty() || desc.toLowerCase().contains(q) || v.vin().toLowerCase().contains(q)
                    || (v.licencePlate() != null && v.licencePlate().toLowerCase().contains(q))) {
                BigDecimal effectivePrice = pricingService.quotePrice(v.requireId());
                items.add(new UiModels.VehicleItem(
                        v.requireId(), v.vin(), v.licencePlate(), desc,
                        v.kilometers(), effectivePrice, v.year(), v.state(), null
                ));
            }
        }
        return List.copyOf(items);
    }

    @Override
    public List<UiModels.VehicleItem> waitingVehicles() {
        processExpiredRequests();
        List<Vehicle> list = catalogService.waitingVehicles();
        List<UiModels.VehicleItem> items = new ArrayList<>();
        for (Vehicle v : list) {
            VehicleModel m = catalogService.model(v.modelId());
            String desc = m.brandName() + " " + m.name();
            items.add(new UiModels.VehicleItem(
                    v.requireId(), v.vin(), v.licencePlate(), desc,
                    v.kilometers(), v.price(), v.year(), v.state(), null
            ));
        }
        return List.copyOf(items);
    }

    @Override
    public List<UiModels.VehicleItem> allVehicles() {
        processExpiredRequests();
        List<Vehicle> list = catalogService.allVehicles();
        List<UiModels.VehicleItem> items = new ArrayList<>();
        for (Vehicle v : list) {
            VehicleModel m = catalogService.model(v.modelId());
            String desc = m.brandName() + " " + m.name();
            items.add(new UiModels.VehicleItem(
                    v.requireId(), v.vin(), v.licencePlate(), desc,
                    v.kilometers(), v.price(), v.year(), v.state(), null
            ));
        }
        return List.copyOf(items);
    }

    @Override
    public BigDecimal quoteValuation(String brand, String model, String plate, int km, int year) {
        return pricingService.quoteValuation(brand, model, plate, km, year);
    }

    @Override
    public BigDecimal quoteRentalPrice(long vehicleId, long days) {
        return pricingService.quoteRentalPrice(vehicleId, days);
    }

    @Override
    public void submitVehicleForSale(long clientId, UiModels.VehicleSaleSubmission sub) {
        salesService.submitVehicleForSale(
                sub.brand(), sub.model(), sub.modelYear(),
                sub.vin(), sub.plate(), sub.kilometers(),
                sub.price(), sub.vehicleYear(), clientId
        );
    }

    @Override
    public void requestPurchase(long clientId, long vehicleId, BigDecimal deposit) {
        salesService.requestPurchase(clientId, vehicleId, deposit);
    }

    @Override
    public void completePurchase(long clientId, long requestId, long salesmanId) {
        salesService.completePurchase(requestId, salesmanId);
    }

    @Override
    public void cancelPurchase(long clientId, long requestId) {
        salesService.cancelRequestWithPenalty(requestId);
    }

    @Override
    public void bookRental(long clientId, long vehicleId, LocalDate startDate, LocalDate endDate) {
        rentalService.bookRental(clientId, vehicleId, startDate, endDate);
    }

    @Override
    public void bookTestDrive(long clientId, long vehicleId, LocalDate date, LocalTime start) {
        testDriveService.book(clientId, vehicleId, date, start);
    }

    @Override
    public List<UiModels.RequestItem> clientRequests(long clientId) {
        processExpiredRequests();
        List<Request> requests = salesService.findRequestsByClient(clientId);
        List<UiModels.RequestItem> items = new ArrayList<>();
        for (Request r : requests) {
            Vehicle v = catalogService.vehicle(r.vehicleId());
            VehicleModel m = catalogService.model(v.modelId());
            items.add(new UiModels.RequestItem(
                    r.requireId(), r.vehicleId(), m.brandName() + " " + m.name(),
                    r.clientId(), r.salesmanId(), r.deposit(), r.expDate(),
                    r.isPurchase(), r.isSaleProposal()
            ));
        }
        return List.copyOf(items);
    }

    @Override
    public List<UiModels.RentItem> clientRentals(long clientId) {
        List<Rent> list = rentalService.findRentalsByClient(clientId);
        List<UiModels.RentItem> items = new ArrayList<>();
        for (Rent r : list) {
            Vehicle v = catalogService.vehicle(r.vehicleId());
            VehicleModel m = catalogService.model(v.modelId());
            items.add(new UiModels.RentItem(
                    r.requireId(), r.vehicleId(), m.brandName() + " " + m.name(),
                    r.clientId(), r.amount(), r.start(), r.end()
            ));
        }
        return List.copyOf(items);
    }

    @Override
    public List<UiModels.TestDriveItem> clientTestDrives(long clientId) {
        List<TestDrive> list = testDriveService.findTestDrivesByClient(clientId);
        List<UiModels.TestDriveItem> items = new ArrayList<>();
        for (TestDrive td : list) {
            Vehicle v = catalogService.vehicle(td.vehicleId());
            VehicleModel m = catalogService.model(v.modelId());
            items.add(new UiModels.TestDriveItem(
                    td.requireId(), td.vehicleId(), m.brandName() + " " + m.name(),
                    td.clientId(), td.date(), td.start()
            ));
        }
        return List.copyOf(items);
    }

    @Override
    public List<UiModels.SaleItem> clientPurchases(long clientId) {
        List<Sale> list = salesService.salesByClient(clientId);
        List<UiModels.SaleItem> items = new ArrayList<>();
        for (Sale s : list) {
            String desc = s.isPenalty() ? "Penale mancato saldo" : "Veicolo ritirato";
            if (s.vehicleId() != null) {
                try {
                    Vehicle v = catalogService.vehicle(s.vehicleId());
                    VehicleModel m = catalogService.model(v.modelId());
                    desc = m.brandName() + " " + m.name();
                } catch (Exception ignored) {
                }
            }
            items.add(new UiModels.SaleItem(
                    s.requireId(), s.vehicleId(), desc, s.salesmanId(),
                    s.clientId(), s.amount(), s.localDate(),
                    s.isDefaultFailure(), s.isAcquisition()
            ));
        }
        return List.copyOf(items);
    }

    @Override
    public void approveVehicle(long vehicleId) {
        salesService.approveVehicle(vehicleId);
    }

    @Override
    public void rejectVehicle(long vehicleId) {
        salesService.rejectVehicle(vehicleId);
    }

    @Override
    public void approveClientSale(long requestId, long salesmanId) {
        salesService.approveClientSale(requestId, salesmanId);
    }

    @Override
    public void rejectClientSale(long requestId, long salesmanId) {
        salesService.rejectClientSale(requestId, salesmanId);
    }

    @Override
    public List<UiModels.RequestItem> allPendingRequests() {
        processExpiredRequests();
        List<Request> list = salesService.allPendingRequests();
        List<UiModels.RequestItem> items = new ArrayList<>();
        for (Request r : list) {
            Vehicle v = catalogService.vehicle(r.vehicleId());
            VehicleModel m = catalogService.model(v.modelId());
            items.add(new UiModels.RequestItem(
                    r.requireId(), r.vehicleId(), m.brandName() + " " + m.name(),
                    r.clientId(), r.salesmanId(), r.deposit(), r.expDate(),
                    r.isPurchase(), r.isSaleProposal()
            ));
        }
        return List.copyOf(items);
    }

    @Override
    public void unlockFullPayment(long requestId) {
        salesService.unlockFullPayment(requestId);
    }

    @Override
    public void authorizePurchase(long requestId, long salesmanId) {
        salesService.authorizePurchase(requestId, salesmanId);
    }

    @Override
    public void switchVehicleState(long vehicleId, VehicleState newState) {
        rentalService.switchVehicleState(vehicleId, newState);
    }

    @Override
    public List<UiModels.RentItem> allRentals() {
        List<Rent> list = rentalService.allRentals();
        List<UiModels.RentItem> items = new ArrayList<>();
        for (Rent r : list) {
            Vehicle v = catalogService.vehicle(r.vehicleId());
            VehicleModel m = catalogService.model(v.modelId());
            items.add(new UiModels.RentItem(
                    r.requireId(), r.vehicleId(), m.brandName() + " " + m.name(),
                    r.clientId(), r.amount(), r.start(), r.end()
            ));
        }
        return List.copyOf(items);
    }

    @Override
    public List<UiModels.TestDriveItem> allTestDrives() {
        List<TestDrive> list = testDriveService.allTestDrives();
        List<UiModels.TestDriveItem> items = new ArrayList<>();
        for (TestDrive td : list) {
            Vehicle v = catalogService.vehicle(td.vehicleId());
            VehicleModel m = catalogService.model(v.modelId());
            items.add(new UiModels.TestDriveItem(
                    td.requireId(), td.vehicleId(), m.brandName() + " " + m.name(),
                    td.clientId(), td.date(), td.start()
            ));
        }
        return List.copyOf(items);
    }

    @Override
    public void setModelDiscount(long modelId, int percentage) {
        pricingService.setModelDiscount(modelId, percentage);
    }

    @Override
    public void setVehicleDiscount(long vehicleId, int percentage) {
        pricingService.setVehicleDiscount(vehicleId, percentage);
    }

    @Override
    public void removeModelDiscount(long modelId) {
        pricingService.removeModelDiscount(modelId);
    }

    @Override
    public void removeVehicleDiscount(long vehicleId) {
        pricingService.removeVehicleDiscount(vehicleId);
    }

    @Override
    public List<UiModels.SaleItem> allSales() {
        List<Sale> list = salesService.allSales();
        List<UiModels.SaleItem> items = new ArrayList<>();
        for (Sale s : list) {
            String desc = s.isPenalty() ? "Penale mancato saldo" : "Veicolo ritirato";
            if (s.vehicleId() != null) {
                try {
                    Vehicle v = catalogService.vehicle(s.vehicleId());
                    VehicleModel m = catalogService.model(v.modelId());
                    desc = m.brandName() + " " + m.name();
                } catch (Exception ignored) {
                }
            }
            items.add(new UiModels.SaleItem(
                    s.requireId(), s.vehicleId(), desc, s.salesmanId(),
                    s.clientId(), s.amount(), s.localDate(),
                    s.isDefaultFailure(), s.isAcquisition()
            ));
        }
        return List.copyOf(items);
    }

    @Override
    public List<UiModels.BrandItem> allBrands() {
        return catalogService.brands().stream()
                .map(b -> new UiModels.BrandItem(b.name()))
                .toList();
    }

    @Override
    public void addBrand(String name) {
        catalogService.addBrand(name);
    }

    @Override
    public void deleteBrand(String name) {
        catalogService.deleteBrand(name);
    }

    @Override
    public List<UiModels.ModelItem> allModels() {
        return catalogService.models().stream()
                .map(m -> new UiModels.ModelItem(m.requireId(), m.brandName(), m.name(), m.releasePrice(), m.releaseYear()))
                .toList();
    }

    @Override
    public void addModel(String brand, String name, BigDecimal releasePrice, int releaseYear) {
        catalogService.addModel(brand, name, releasePrice, releaseYear);
    }

    @Override
    public void deleteModel(long modelId) {
        catalogService.deleteModel(modelId);
    }

    @Override
    public void addVehicle(long modelId, String vin, String plate, int km, BigDecimal price, int year, VehicleState state) {
        catalogService.addVehicle(modelId, vin, plate, km, price, year, state);
    }

    @Override
    public void deleteVehicle(long vehicleId) {
        catalogService.deleteVehicle(vehicleId);
    }
}
