package it.unifi.drivehub;

import it.unifi.drivehub.domain.operation.Request;
import it.unifi.drivehub.domain.operation.RequestType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class RequestAgreementTest {
    @Test
    void agreementAndPaidDepositRemainFixedDuringApproval() {
        Request request = Request.createNewSaleRequest(1L, 2L,
                new BigDecimal("4000.01"), new BigDecimal("20000.03"));
        assertEquals(RequestType.SALE, request.getType());
        request.setSalesmanId(16L);
        request.setExpDate(LocalDate.now().plusMonths(1));
        assertEquals(new BigDecimal("20000.03"), request.getAgreedTotal());
        assertEquals(new BigDecimal("4000.01"), request.getDeposit());
        assertEquals(new BigDecimal("16000.02"), request.getRemainingAmount());
    }

    @ParameterizedTest
    @CsvSource({",4000", "-1,0", "20000,-1", "20000,20001"})
    void rejectsMissingOrInconsistentPurchaseAgreement(BigDecimal total, BigDecimal deposit) {
        assertThrows(IllegalArgumentException.class,
                () -> Request.createNewSaleRequest(1L, 2L, deposit, total));
    }

    @Test
    void rejectsAmountsThatWouldChangeWhenStoredAsCents() {
        assertThrows(ArithmeticException.class, () -> Request.createNewSaleRequest(1L, 2L,
                new BigDecimal("4000"), new BigDecimal("20000.001")));
        assertThrows(ArithmeticException.class, () -> Request.createNewSaleRequest(1L, 2L,
                new BigDecimal("4000.001"), new BigDecimal("20000")));
    }

    @Test
    void cessionKeepsItsEstimateWithoutPurchaseAmounts() {
        Request request = Request.createNewPurchaseRequest(1L, 2L, new BigDecimal("13500.00"));
        assertEquals(RequestType.PURCHASE, request.getType());
        assertEquals(new BigDecimal("13500.00"), request.getDeposit());
        assertNull(request.getAgreedTotal());
        assertThrows(IllegalStateException.class, request::getRemainingAmount);
        assertThrows(IllegalArgumentException.class, () -> Request.create(null, 1L, 2L, null,
                request.getDeposit(), BigDecimal.ZERO, null, RequestType.PURCHASE));
    }

    @Test
    void rejectsNegativeEstimatesAtCreationAndReload() {
        assertThrows(IllegalArgumentException.class,
                () -> Request.createNewPurchaseRequest(1L, 2L, new BigDecimal("-16000")));
        assertThrows(IllegalArgumentException.class, () -> Request.create(3L, 1L, 2L, null,
                new BigDecimal("-16000"), null, null, RequestType.PURCHASE));
    }

    @Test
    void zeroAmountsKeepTheExplicitDealershipType() {
        Request sale = Request.createNewSaleRequest(1L, 2L, BigDecimal.ZERO, BigDecimal.ZERO);
        assertEquals(RequestType.SALE, sale.getType());
        assertEquals(new BigDecimal("0.00"), sale.getRemainingAmount());
        Request purchase = Request.createNewPurchaseRequest(1L, 2L, BigDecimal.ZERO);
        assertEquals(RequestType.PURCHASE, purchase.getType());
        assertEquals(0, purchase.getDeposit().signum());
        assertNull(purchase.getAgreedTotal());
    }
}
