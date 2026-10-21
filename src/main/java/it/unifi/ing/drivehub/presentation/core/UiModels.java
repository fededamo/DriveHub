package it.unifi.ing.drivehub.presentation.core;

import it.unifi.ing.drivehub.domain.users.Role;
import it.unifi.ing.drivehub.domain.vehicles.VehicleState;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Objects;

/** Proiezioni immutabili per i controller JavaFX. */
public final class UiModels {
    private UiModels() {
    }

    public record Session(long userId, String displayName, String email, Role role) {
        public Session {
            Objects.requireNonNull(displayName);
            Objects.requireNonNull(email);
            Objects.requireNonNull(role);
        }
    }

    public record BankAccountItem(String iban, long clientId, BigDecimal balance) {
    }

    public record VehicleItem(long id, String vin, String plate, String displayName,
                              int kilometers, BigDecimal price, int year,
                              VehicleState state, Integer discountPercentage) {
    }

    public record RequestItem(long id, long vehicleId, String vehicleDesc, long clientId,
                              Long salesmanId, BigDecimal deposit, LocalDate expDate,
                              boolean isPurchase, boolean isSaleProposal) {
    }

    public record RentItem(long id, long vehicleId, String vehicleDesc, long clientId,
                           BigDecimal amount, LocalDate startDate, LocalDate endDate) {
    }

    public record TestDriveItem(long id, long vehicleId, String vehicleDesc, long clientId,
                                LocalDate date, LocalTime start) {
    }

    public record SaleItem(long id, Long vehicleId, String vehicleDesc, Long salesmanId,
                           long clientId, BigDecimal amount, LocalDate date,
                           boolean isDefaultFailure, boolean isAcquisition) {
    }

    public record VehicleSaleSubmission(String brand, String model, int modelYear,
                                        String vin, String plate, int kilometers,
                                        BigDecimal price, int vehicleYear) {
    }

    public record BrandItem(String name) {
        @Override
        public String toString() {
            return name;
        }
    }

    public record ModelItem(long id, String brand, String name, BigDecimal releasePrice, int releaseYear) {
        @Override
        public String toString() {
            return brand + " " + name + " (" + releaseYear + " - " + releasePrice + " €)";
        }
    }
}
