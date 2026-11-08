package it.unifi.drivehub;

import it.unifi.drivehub.domain.operation.Sale;
import it.unifi.drivehub.domain.operation.SaleType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class SaleTypeTest {
    @Test
    void registerUsesOnlyDealershipSalePurchaseAndPenalty() {
        assertEquals(java.util.Set.of("SALE", "PURCHASE", "PENALTY"),
                java.util.Arrays.stream(SaleType.values()).map(Enum::name)
                        .collect(java.util.stream.Collectors.toSet()));
    }

    @Test
    void factoriesUseTheDealershipPerspectiveAndExplicitTypes() {
        BigDecimal amount = new BigDecimal("123.45");
        var sale = Sale.createSaleRecord(1L, 16L, 1L, amount);
        var purchase = Sale.createPurchaseRecord(1L, 16L, 1L, amount);
        var penalty = Sale.createPenaltyRecord(1L, amount);
        assertEquals(SaleType.SALE, sale.getType());
        assertEquals(SaleType.PURCHASE, purchase.getType());
        assertEquals(SaleType.PENALTY, penalty.getType());
        assertEquals(amount, sale.getAmount());
        assertEquals(amount, purchase.getAmount());
        assertEquals(amount, penalty.getAmount());
        assertNull(penalty.getVehicleId());
        assertThrows(IllegalArgumentException.class,
                () -> Sale.create(1L, 1L, 16L, 1L, amount, LocalDateTime.now(), null));
    }

    @ParameterizedTest
    @CsvSource({"SALE,-0.01", "PURCHASE,-0.01", "PENALTY,-0.01"})
    void reconstructionRejectsNegativeAmountsForEveryType(SaleType type, BigDecimal amount) {
        assertThrows(IllegalArgumentException.class,
                () -> Sale.create(1L, 1L, 16L, 1L, amount, LocalDateTime.now(), type));
    }

    @Test
    void zeroAmountsKeepTheirExplicitType() {
        assertEquals(SaleType.PURCHASE, Sale.createPurchaseRecord(1L, 16L, 1L, BigDecimal.ZERO).getType());
        assertEquals(SaleType.SALE, Sale.createSaleRecord(1L, 16L, 1L, BigDecimal.ZERO).getType());
        assertEquals(SaleType.PENALTY, Sale.createPenaltyRecord(1L, BigDecimal.ZERO).getType());
    }

    @ParameterizedTest
    @CsvSource({"-0.01", "-16000"})
    void factoriesRejectNegativeInputsWithoutNormalizingThem(BigDecimal amount) {
        assertThrows(IllegalArgumentException.class, () -> Sale.createSaleRecord(1L, 16L, 1L, amount));
        assertThrows(IllegalArgumentException.class, () -> Sale.createPurchaseRecord(1L, 16L, 1L, amount));
        assertThrows(IllegalArgumentException.class, () -> Sale.createPenaltyRecord(1L, amount));
    }

    @ParameterizedTest
    @EnumSource(SaleType.class)
    void reconstructionKeepsNonNegativeAmountsAndExplicitTypes(SaleType type) {
        for (BigDecimal amount : java.util.List.of(BigDecimal.ZERO, new BigDecimal("16000.00"))) {
            Sale sale = Sale.create(1L, 1L, 16L, 1L, amount, LocalDateTime.now(), type);
            assertEquals(type, sale.getType());
            assertEquals(amount, sale.getAmount());
        }
    }
}
