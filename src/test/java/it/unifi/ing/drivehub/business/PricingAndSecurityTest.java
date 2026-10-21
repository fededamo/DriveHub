package it.unifi.ing.drivehub.business;

import it.unifi.ing.drivehub.business.security.Pbkdf2PasswordHasher;
import it.unifi.ing.drivehub.business.strategies.StandardPricingStrategy;
import it.unifi.ing.drivehub.domain.vehicles.Vehicle;
import it.unifi.ing.drivehub.domain.vehicles.VehicleModel;
import it.unifi.ing.drivehub.domain.vehicles.VehicleState;
import it.unifi.ing.drivehub.support.TestFixtures;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.time.Year;

import static org.junit.jupiter.api.Assertions.*;

class PricingAndSecurityTest {
    @Test
    @DisplayName("Pricing strategy applies discount cascade to sale price")
    void discountAppliesToSalePrice() {
        int currentYear = Year.now().getValue();
        VehicleModel model = new VehicleModel(20L, "Alfa Romeo", "Giulia", new BigDecimal("40000.00"), currentYear);
        // Nuovo veicolo senza targa, 0 km, anno corrente
        Vehicle newCar = Vehicle.create(20L, "VIN123", null, 0, new BigDecimal("40000.00"), currentYear, VehicleState.SALE);
        StandardPricingStrategy strategy = new StandardPricingStrategy();

        // 40000 con 10% promo = 36000
        assertEquals(new BigDecimal("36000.00"), strategy.quoteVehiclePrice(newCar, model, 10));
        // 0% promo = 40000
        assertEquals(new BigDecimal("40000.00"), strategy.quoteVehiclePrice(newCar, model, 0));
    }

    @Test
    @DisplayName("Pricing strategy applies rental formula (giorni * tariffa_base) * 0.95^(giorni - 1)")
    void discountAppliesToRental() {
        // Veicolo con prezzo 40000 -> tariffa giornaliera = 40000 / 1000 = 40.00 €
        Vehicle target = new Vehicle(30L, 20L, "VIN30", "AB123CD", 22_000, new BigDecimal("40000.00"), 2022, VehicleState.RENT);
        StandardPricingStrategy strategy = new StandardPricingStrategy();

        // 1 giorno: 1 * 40 * (0.95)^0 = 40.00 €
        assertEquals(new BigDecimal("40.00"), strategy.rentalPrice(target, 1));

        // 2 giorni: 2 * 40 * (0.95)^1 = 76.00 €
        assertEquals(new BigDecimal("76.00"), strategy.rentalPrice(target, 2));

        // 3 giorni: 3 * 40 * (0.95)^2 = 108.30 €
        assertEquals(new BigDecimal("108.30"), strategy.rentalPrice(target, 3));

        // 4 giorni: 4 * 40 * (0.95)^3 = 137.18 €
        assertEquals(new BigDecimal("137.18"), strategy.rentalPrice(target, 4));
    }

    @Test
    @DisplayName("SEC-AUTH-01: PBKDF2 stores salt and verifies without retaining plaintext")
    void pbkdf2RoundTrip() {
        Pbkdf2PasswordHasher hasher = new Pbkdf2PasswordHasher(10_000, 16, 128, new SecureRandom());

        String first = hasher.hash("correct horse".toCharArray());
        String second = hasher.hash("correct horse".toCharArray());

        assertNotEquals(first, second, "fresh random salts must produce different encodings");
        assertTrue(hasher.verify("correct horse".toCharArray(), first));
        assertFalse(hasher.verify("wrong password".toCharArray(), first));
        assertFalse(hasher.verify("correct horse".toCharArray(), "malformed"));
    }
}
