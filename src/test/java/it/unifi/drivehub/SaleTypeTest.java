package it.unifi.drivehub;

import it.unifi.drivehub.domain.operation.Sale;
import it.unifi.drivehub.domain.operation.SaleType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class SaleTypeTest {
    @Test
    void factoriesUseTheDealershipPerspectiveAndExplicitTypes() {
        BigDecimal amount = new BigDecimal("123.45");
        var purchase = Sale.createPurchaseRecord(1L, 16L, 1L, amount);
        var acquisition = Sale.createAcquisitionRecord(1L, 16L, 1L, amount);
        var penalty = Sale.createPenaltyRecord(1L, amount);
        assertEquals(SaleType.PURCHASE, purchase.getType());
        assertEquals(SaleType.ACQUISITION, acquisition.getType());
        assertEquals(SaleType.PENALTY, penalty.getType());
        assertEquals(amount, purchase.getAmount());
        assertEquals(amount.negate(), acquisition.getAmount());
        assertEquals(amount, penalty.getAmount());
        assertNull(penalty.getVehicleId());
        assertThrows(IllegalArgumentException.class,
                () -> Sale.create(1L, 1L, 16L, 1L, amount, LocalDateTime.now(), null));
    }

    @ParameterizedTest
    @CsvSource({"PURCHASE,-0.01", "ACQUISITION,0.01", "PENALTY,-0.01"})
    void reconstructionRejectsAmountsWithTheWrongEconomicSign(SaleType type, BigDecimal amount) {
        assertThrows(IllegalArgumentException.class,
                () -> Sale.create(1L, 1L, 16L, 1L, amount, LocalDateTime.now(), type));
    }

    @Test
    void zeroHistoricalAmountsKeepTheirExplicitType() {
        assertEquals(SaleType.ACQUISITION, Sale.createAcquisitionRecord(1L, 16L, 1L, BigDecimal.ZERO).getType());
        assertEquals(SaleType.PURCHASE, Sale.createPurchaseRecord(1L, 16L, 1L, BigDecimal.ZERO).getType());
        assertEquals(SaleType.PENALTY, Sale.createPenaltyRecord(1L, BigDecimal.ZERO).getType());
    }
}
