package it.unifi.ing.drivehub.business.services;

import it.unifi.ing.drivehub.business.core.TransactionRunner;
import it.unifi.ing.drivehub.business.exceptions.EntityNotFoundException;
import it.unifi.ing.drivehub.business.strategies.PricingStrategy;
import it.unifi.ing.drivehub.business.strategies.StandardPricingStrategy;
import it.unifi.ing.drivehub.dao.interfaces.DaoFactory;
import it.unifi.ing.drivehub.domain.vehicles.ModelDiscount;
import it.unifi.ing.drivehub.domain.vehicles.Vehicle;
import it.unifi.ing.drivehub.domain.vehicles.VehicleDiscount;
import it.unifi.ing.drivehub.domain.vehicles.VehicleModel;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.Optional;

public final class PricingService {
    private final TransactionRunner transactions;
    private final PricingStrategy pricingStrategy;

    public PricingService(DaoFactory daoFactory) {
        this(daoFactory, new StandardPricingStrategy());
    }

    public PricingService(DaoFactory daoFactory, PricingStrategy pricingStrategy) {
        this.transactions = new TransactionRunner(daoFactory);
        this.pricingStrategy = Objects.requireNonNull(pricingStrategy, "pricingStrategy");
    }

    public BigDecimal quotePrice(long vehicleId) {
        return transactions.execute(unit -> {
            Vehicle vehicle = unit.vehicles().findById(vehicleId)
                    .orElseThrow(() -> new EntityNotFoundException("vehicle", vehicleId));
            VehicleModel model = unit.vehicleModels().findById(vehicle.modelId())
                    .orElseThrow(() -> new EntityNotFoundException("model", vehicle.modelId()));

            Optional<ModelDiscount> md = unit.discounts().findModelDiscount(vehicle.modelId());
            Optional<VehicleDiscount> vd = unit.discounts().findVehicleDiscount(vehicle.id());

            // Se presente sia sconto veicolo che modello, si applica solo il maggiore
            int promoDiscount = Math.max(
                    md.map(ModelDiscount::value).orElse(0),
                    vd.map(VehicleDiscount::value).orElse(0)
            );

            return pricingStrategy.quoteVehiclePrice(vehicle, model, promoDiscount);
        });
    }

    public BigDecimal calculateDeposit(BigDecimal price) {
        return pricingStrategy.calculateDeposit(price);
    }

    public static BigDecimal calculateDefaultDeposit(BigDecimal total) {
        return new StandardPricingStrategy().calculateDeposit(total);
    }

    public BigDecimal calculateRentalPrice(Vehicle vehicle, long days) {
        return pricingStrategy.rentalPrice(vehicle, days);
    }

    public BigDecimal calculateValuation(BigDecimal modelReleasePrice, String plate, int km, int year) {
        return pricingStrategy.calculateValuation(modelReleasePrice, plate, km, year);
    }

    public BigDecimal quoteRentalPrice(long vehicleId, long days) {
        return transactions.execute(unit -> {
            Vehicle vehicle = unit.vehicles().findById(vehicleId)
                    .orElseThrow(() -> new EntityNotFoundException("vehicle", vehicleId));
            return calculateRentalPrice(vehicle, days);
        });
    }

    public BigDecimal quoteValuation(String brandName, String modelName, String plate, int km, int year) {
        return transactions.execute(unit -> {
            Optional<VehicleModel> modelOpt = unit.vehicleModels().findByName(modelName);
            BigDecimal releasePrice = modelOpt.map(VehicleModel::releasePrice).orElse(new BigDecimal("25000.00"));
            return calculateValuation(releasePrice, plate, km, year);
        });
    }

    public void setModelDiscount(long modelId, int percentage) {
        transactions.execute(unit -> {
            unit.discounts().saveModelDiscount(new ModelDiscount(modelId, percentage));
            return null;
        });
    }

    public void setVehicleDiscount(long vehicleId, int percentage) {
        transactions.execute(unit -> {
            unit.discounts().saveVehicleDiscount(new VehicleDiscount(vehicleId, percentage));
            return null;
        });
    }

    public void removeModelDiscount(long modelId) {
        transactions.execute(unit -> {
            unit.discounts().removeModelDiscount(modelId);
            return null;
        });
    }

    public void removeVehicleDiscount(long vehicleId) {
        transactions.execute(unit -> {
            unit.discounts().removeVehicleDiscount(vehicleId);
            return null;
        });
    }
}
