package it.unifi.drivehub;

import it.unifi.drivehub.domain.vehicle.Model;
import it.unifi.drivehub.domain.vehicle.Vehicle;
import it.unifi.drivehub.domain.vehicle.VehicleState;
import it.unifi.drivehub.service.PriceCalculatorService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Year;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("Verifica del Calcolo Prezzi e Tariffe (Regole di Business)")
class PriceCalculatorServiceTest {

    private PriceCalculatorService calculator;

    @BeforeEach
    void setUp() {
        calculator = new PriceCalculatorService();
    }

    @Test
    @DisplayName("Regola 1: Sconto promozionale massimo tra modello e veicolo")
    void testPromotionalDiscountMax() {
        int currentYear = Year.now().getValue();
        Model model = Model.create(1L, "Fiat", "500", BigDecimal.valueOf(20000), currentYear);
        // Nuovo (senza targa, 0 km, anno corrente)
        Vehicle vehicle = Vehicle.create(1L, 1L, "12345678901234567", null, 0, BigDecimal.valueOf(20000), currentYear, VehicleState.SALE);

        // Modello 10%, Veicolo 20% -> deve applicare 20%
        BigDecimal price = calculator.calculateVehiclePrice(model, vehicle, 10, 20);
        // 20000 * 0.8 = 16000.00
        assertEquals(new BigDecimal("16000.00"), price);
    }

    @Test
    @DisplayName("Regola 2: Sconto usato 20% se targa != null")
    void testUsedVehicleDiscount() {
        int currentYear = Year.now().getValue();
        Model model = Model.create(1L, "Fiat", "500", BigDecimal.valueOf(20000), currentYear);
        // Usato (con targa, 0 km, anno corrente, no promo)
        Vehicle vehicle = Vehicle.create(1L, 1L, "12345678901234567", "AB123CD", 0, BigDecimal.valueOf(20000), currentYear, VehicleState.SALE);

        BigDecimal price = calculator.calculateVehiclePrice(model, vehicle, 0, 0);
        // 20000 * 0.8 = 16000.00
        assertEquals(new BigDecimal("16000.00"), price);
    }

    @Test
    @DisplayName("Regola 3: Ammortamento anno 5% per anno di anzianità")
    void testYearAmortization() {
        int currentYear = Year.now().getValue();
        Model model = Model.create(1L, "Fiat", "500", BigDecimal.valueOf(20000), currentYear - 2);
        // Auto di 2 anni fa (nuova da stock, senza targa, 0 km)
        Vehicle vehicle = Vehicle.create(1L, 1L, "12345678901234567", null, 0, BigDecimal.valueOf(20000), currentYear - 2, VehicleState.SALE);

        BigDecimal price = calculator.calculateVehiclePrice(model, vehicle, 0, 0);
        // 20000 * (0.95)^2 = 20000 * 0.9025 = 18050.00
        assertEquals(new BigDecimal("18050.00"), price);
    }

    @Test
    @DisplayName("Regola 4: Ammortamento chilometri 5% ogni 20000 km")
    void testMileageAmortization() {
        int currentYear = Year.now().getValue();
        Model model = Model.create(1L, "Fiat", "500", BigDecimal.valueOf(20000), currentYear);
        // Con 40.000 km (usata)
        Vehicle vehicle = Vehicle.create(1L, 1L, "12345678901234567", "AB123CD", 40000, BigDecimal.valueOf(20000), currentYear, VehicleState.SALE);

        BigDecimal price = calculator.calculateVehiclePrice(model, vehicle, 0, 0);
        // 1. Usato: 20000 * 0.80 = 16000
        // 2. Anno: 0 anni -> 16000
        // 3. Km: 40000 / 20000 = 2 -> 16000 * (0.95)^2 = 16000 * 0.9025 = 14440.00
        assertEquals(new BigDecimal("14440.00"), price);
    }

    @Test
    @DisplayName("Calcolo acconto acquisto (20% del prezzo attuale)")
    void testPurchaseDeposit() {
        BigDecimal deposit = calculator.calculatePurchaseDeposit(BigDecimal.valueOf(15000));
        assertEquals(new BigDecimal("3000.00"), deposit);
    }

    @Test
    @DisplayName("Calcolo costo noleggio geometrico con 0.1% giornaliero")
    void testRentalCostFormula() {
        BigDecimal carPrice = BigDecimal.valueOf(20000);
        LocalDate start = LocalDate.of(2026, 11, 1);
        LocalDate end = LocalDate.of(2026, 11, 3); // 3 giorni inclusi: 1, 2, 3

        BigDecimal cost = calculator.calculateTotalRentalCost(carPrice, start, end);
        // dailyRate = 20000 * 0.001 = 20.00
        // giorno 1: 20.00
        // giorno 2: 20.00 * 0.95 = 19.00
        // giorno 3: 19.00 * 0.95 = 18.05
        // Somma = 20 + 19 + 18.05 = 57.05
        assertEquals(new BigDecimal("57.05"), cost);
    }
}

