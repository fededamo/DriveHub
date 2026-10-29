package it.unifi.drivehub.service;

import it.unifi.drivehub.domain.vehicle.Model;
import it.unifi.drivehub.domain.vehicle.Vehicle;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.Year;
import java.time.temporal.ChronoUnit;

/**
 * Servizio di Business per il calcolo dei prezzi e delle tariffe.
 * Implementa fedelmente le regole di calcolo degli sconti a cascata (non sommati)
 * e la formula geometrica delle tariffe di noleggio (Pagina 1 degli appunti).
 */
public final class PriceCalculatorService {

    /**
     * Calcola il prezzo effettivo di un veicolo applicando gli sconti nell'ordine specificato:
     * 1. Sconto promozionale maggiore tra mezzo e modello: prezzo = prezzo_iniziale * (1 - sconto)
     * 2. Sconto usato (20% se targa != null): prezzo = prezzo * 0.8
     * 3. Ammortamento anno (5% per anno rispetto all'anno corrente): prezzo = prezzo * (0.95)^anni
     * 4. Ammortamento chilometri (5% ogni 20.000 km): prezzo = prezzo * (0.95)^(km / 20000)
     */
    public BigDecimal calculateVehiclePrice(Model model, Vehicle vehicle, Integer modelDiscount, Integer vehicleDiscount) {
        int mDisc = modelDiscount != null ? modelDiscount : 0;
        int vDisc = vehicleDiscount != null ? vehicleDiscount : 0;
        int maxPromo = Math.max(mDisc, vDisc);
        return applyStandardDepreciation(model.getReleasePrice(), maxPromo, vehicle.getLicencePlate(), vehicle.getKilometers(), vehicle.getYear());
    }

    /**
     * Calcola il preventivo per un veicolo che un cliente vuole vendere alla concessionaria.
     */
    public BigDecimal estimateVehicleValue(BigDecimal modelBasePrice, int modelDiscount, String plate, int km, int year) {
        return applyStandardDepreciation(modelBasePrice, modelDiscount, plate, km, year);
    }

    /**
     * Algoritmo centralizzato di ammortamento e sconti a cascata (DRY).
     */
    private BigDecimal applyStandardDepreciation(BigDecimal basePrice, int promoDiscount, String licencePlate, int km, int year) {
        BigDecimal currentPrice = basePrice;

        // 1. Sconto promozionale
        if (promoDiscount > 0) {
            double promoFactor = 1.0 - (promoDiscount / 100.0);
            currentPrice = currentPrice.multiply(BigDecimal.valueOf(promoFactor));
        }

        // 2. Sconto sull'usato (20% se targa presente)
        if (licencePlate != null && !licencePlate.trim().isEmpty()) {
            currentPrice = currentPrice.multiply(BigDecimal.valueOf(0.80));
        }

        // 3. Ammortamento anno (5% all'anno rispetto all'anno corrente)
        int currentYear = Year.now().getValue();
        int yearsOld = Math.max(0, currentYear - year);
        if (yearsOld > 0) {
            double yearFactor = Math.pow(0.95, yearsOld);
            currentPrice = currentPrice.multiply(BigDecimal.valueOf(yearFactor));
        }

        // 4. Ammortamento chilometri (5% ogni 20.000 km)
        if (km > 0) {
            double kmExponent = (double) km / 20000.0;
            double kmFactor = Math.pow(0.95, kmExponent);
            currentPrice = currentPrice.multiply(BigDecimal.valueOf(kmFactor));
        }

        return currentPrice.setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Calcola l'acconto dovuto per l'acquisto (20% del prezzo attuale del veicolo).
     */
    public BigDecimal calculatePurchaseDeposit(BigDecimal currentPrice) {
        return currentPrice.multiply(BigDecimal.valueOf(0.20)).setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Calcola la tariffa giornaliera di noleggio (0.1% del valore attuale del veicolo).
     */
    public BigDecimal calculateDailyRentalRate(BigDecimal currentPrice) {
        return currentPrice.multiply(BigDecimal.valueOf(0.001)).setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Calcola il costo totale del noleggio per il periodo specificato.
     * Formula geometrica da appunti (Pagina 1):
     * totale = (prezzo_attuale * 0.1%) * ((1 - 0.95^giorni) / 0.05)
     */
    public BigDecimal calculateTotalRentalCost(BigDecimal currentPrice, LocalDate start, LocalDate end) {
        long days = ChronoUnit.DAYS.between(start, end) + 1;
        if (days <= 0) {
            throw new IllegalArgumentException("Il periodo di noleggio deve comprendere almeno 1 giorno.");
        }

        double dailyRate = currentPrice.doubleValue() * 0.001;
        double sumGeometric = (1.0 - Math.pow(0.95, days)) / 0.05;
        double totalCost = dailyRate * sumGeometric;

        return BigDecimal.valueOf(totalCost).setScale(2, RoundingMode.HALF_UP);
    }
}

