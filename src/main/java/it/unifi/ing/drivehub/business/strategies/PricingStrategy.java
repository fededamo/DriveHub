package it.unifi.ing.drivehub.business.strategies;

import it.unifi.ing.drivehub.domain.vehicles.Vehicle;
import it.unifi.ing.drivehub.domain.vehicles.VehicleModel;

import java.math.BigDecimal;

public interface PricingStrategy {
    BigDecimal quoteVehiclePrice(Vehicle vehicle, VehicleModel model, int promotionalDiscount);

    BigDecimal rentalPrice(Vehicle vehicle, long days);

    BigDecimal calculateDeposit(BigDecimal price);

    BigDecimal calculateValuation(BigDecimal modelReleasePrice, String licencePlate, int kilometers, int vehicleYear);
}
