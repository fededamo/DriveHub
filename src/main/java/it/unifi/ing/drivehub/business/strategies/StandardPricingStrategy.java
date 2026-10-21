package it.unifi.ing.drivehub.business.strategies;

import it.unifi.ing.drivehub.domain.DomainRuleViolationException;
import it.unifi.ing.drivehub.domain.vehicles.Vehicle;
import it.unifi.ing.drivehub.domain.vehicles.VehicleModel;
import it.unifi.ing.drivehub.domain.vehicles.VehicleState;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Objects;

public final class StandardPricingStrategy implements PricingStrategy {

    @Override
    public BigDecimal quoteVehiclePrice(Vehicle vehicle, VehicleModel model, int promotionalDiscount) {
        Objects.requireNonNull(vehicle, "vehicle");
        Objects.requireNonNull(model, "model");

        BigDecimal current = model.releasePrice();

        // 1. Sconto usato: se la targa non è null, perde il 20%
        if (vehicle.licencePlate() != null && !vehicle.licencePlate().isBlank()) {
            current = current.multiply(new BigDecimal("0.80"));
        }

        // 2. Sconto promozionale impostato dal manager (max tra veicolo e modello)
        if (promotionalDiscount > 0) {
            int discount = Math.min(promotionalDiscount, 50);
            BigDecimal promoFactor = BigDecimal.valueOf(100 - discount)
                    .divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP);
            current = current.multiply(promoFactor);
        }

        // 3. Sconto sull'anno di produzione: ogni anno perde il 5% rispetto all'anno precedente
        int currentYear = LocalDate.now().getYear();
        int yearsOld = Math.max(0, currentYear - vehicle.year());
        if (yearsOld > 0) {
            double yearFactor = Math.pow(0.95, yearsOld);
            current = current.multiply(BigDecimal.valueOf(yearFactor));
        }

        // 4. Sconto sui chilometri totali: ogni 10000 km viene applicato il 5% di sconto (scaglioni interi)
        int kmSteps = vehicle.kilometers() / 10000;
        if (kmSteps > 0) {
            double kmFactor = Math.pow(0.95, kmSteps);
            current = current.multiply(BigDecimal.valueOf(kmFactor));
        }

        return current.setScale(2, RoundingMode.HALF_UP).max(BigDecimal.ZERO);
    }

    @Override
    public BigDecimal rentalPrice(Vehicle vehicle, long days) {
        Objects.requireNonNull(vehicle, "vehicle");
        if (vehicle.state() != VehicleState.RENT) {
            throw new DomainRuleViolationException("Il veicolo non è configurato per il noleggio (stato: " + vehicle.state() + ")");
        }
        if (days <= 0) {
            throw new IllegalArgumentException("I giorni di noleggio devono essere positivi");
        }

        // Costo giornaliero: 0.1% del valore attuale del veicolo = vehicle.price / 1000
        BigDecimal dailyRate = vehicle.price().divide(new BigDecimal("1000"), 4, RoundingMode.HALF_UP);
        BigDecimal baseTotal = dailyRate.multiply(BigDecimal.valueOf(days));

        // Sconto progressivo sui giorni: (giorni * tariffa_base) * (0.95)^(giorni - 1)
        if (days > 1) {
            double discountFactor = Math.pow(0.95, days - 1);
            baseTotal = baseTotal.multiply(BigDecimal.valueOf(discountFactor));
        }

        return baseTotal.setScale(2, RoundingMode.HALF_UP).max(new BigDecimal("0.01"));
    }

    @Override
    public BigDecimal calculateDeposit(BigDecimal price) {
        Objects.requireNonNull(price, "price");
        if (price.signum() <= 0) {
            throw new IllegalArgumentException("Il prezzo deve essere positivo per calcolare l'acconto");
        }
        // Acconto pari esattamente al 20% del prezzo attuale
        return price.multiply(new BigDecimal("0.20")).setScale(2, RoundingMode.HALF_UP);
    }

    @Override
    public BigDecimal calculateValuation(BigDecimal modelReleasePrice, String licencePlate, int kilometers, int vehicleYear) {
        Objects.requireNonNull(modelReleasePrice, "modelReleasePrice");

        BigDecimal current = modelReleasePrice;

        // 1. Sconto usato (targa != null -> -20%)
        if (licencePlate != null && !licencePlate.isBlank()) {
            current = current.multiply(new BigDecimal("0.80"));
        }

        // 2. Nessun promo discount manager su veicolo cliente

        // 3. Sconto anno produzione
        int currentYear = LocalDate.now().getYear();
        int yearsOld = Math.max(0, currentYear - vehicleYear);
        if (yearsOld > 0) {
            double yearFactor = Math.pow(0.95, yearsOld);
            current = current.multiply(BigDecimal.valueOf(yearFactor));
        }

        // 4. Sconto chilometri (interi km / 10000)
        int kmSteps = kilometers / 10000;
        if (kmSteps > 0) {
            double kmFactor = Math.pow(0.95, kmSteps);
            current = current.multiply(BigDecimal.valueOf(kmFactor));
        }

        return current.setScale(2, RoundingMode.HALF_UP).max(BigDecimal.ZERO);
    }
}
