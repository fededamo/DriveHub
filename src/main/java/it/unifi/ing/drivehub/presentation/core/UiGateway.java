package it.unifi.ing.drivehub.presentation.core;

import it.unifi.ing.drivehub.domain.users.Role;
import it.unifi.ing.drivehub.domain.vehicles.VehicleState;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * Confine applicativo consumato dai controller JavaFX.
 */
public interface UiGateway extends AutoCloseable {
    UiModels.Session login(String email, char[] password);

    UiModels.Session register(String firstName, String lastName, String email, char[] password, Role role);

    void processExpiredRequests();

    // Gestione Profilo e Account
    void updateEmail(long userId, Role role, char[] currentPassword, String newEmail);
    void updatePassword(long userId, Role role, char[] currentPassword, char[] newPassword);
    void deleteAccount(long userId, Role role, char[] currentPassword);

    // Conto corrente
    UiModels.BankAccountItem getBankAccount(long clientId);
    void depositFunds(long clientId, BigDecimal amount);
    void withdrawFunds(long clientId, BigDecimal amount);

    // Catalogo e visualizzazione veicoli
    List<UiModels.VehicleItem> searchCatalog(String query, VehicleState state);
    List<UiModels.VehicleItem> waitingVehicles();
    List<UiModels.VehicleItem> allVehicles();
    BigDecimal quoteValuation(String brand, String model, String plate, int km, int year);
    BigDecimal quoteRentalPrice(long vehicleId, long days);

    // Azioni cliente
    void submitVehicleForSale(long clientId, UiModels.VehicleSaleSubmission submission);
    void requestPurchase(long clientId, long vehicleId, BigDecimal deposit);
    void completePurchase(long clientId, long requestId, long salesmanId);
    void cancelPurchase(long clientId, long requestId);
    void bookRental(long clientId, long vehicleId, LocalDate startDate, LocalDate endDate);
    void bookTestDrive(long clientId, long vehicleId, LocalDate date, LocalTime start);
    List<UiModels.RequestItem> clientRequests(long clientId);
    List<UiModels.RentItem> clientRentals(long clientId);
    List<UiModels.TestDriveItem> clientTestDrives(long clientId);
    List<UiModels.SaleItem> clientPurchases(long clientId);

    // Azioni venditore
    void approveVehicle(long vehicleId);
    void rejectVehicle(long vehicleId);
    void approveClientSale(long requestId, long salesmanId);
    void rejectClientSale(long requestId, long salesmanId);
    List<UiModels.RequestItem> allPendingRequests();
    void unlockFullPayment(long requestId);
    void authorizePurchase(long requestId, long salesmanId);
    void switchVehicleState(long vehicleId, VehicleState newState);
    List<UiModels.RentItem> allRentals();
    List<UiModels.TestDriveItem> allTestDrives();

    // Azioni manager
    void setModelDiscount(long modelId, int percentage);
    void setVehicleDiscount(long vehicleId, int percentage);
    void removeModelDiscount(long modelId);
    void removeVehicleDiscount(long vehicleId);
    List<UiModels.SaleItem> allSales();
    List<UiModels.BrandItem> allBrands();
    void addBrand(String name);
    void deleteBrand(String name);
    List<UiModels.ModelItem> allModels();
    void addModel(String brand, String name, BigDecimal releasePrice, int releaseYear);
    void deleteModel(long modelId);
    void addVehicle(long modelId, String vin, String plate, int km, BigDecimal price, int year, VehicleState state);
    void deleteVehicle(long vehicleId);

    @Override
    default void close() {
    }
}
