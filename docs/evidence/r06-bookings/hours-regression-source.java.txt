package it.unifi.drivehub;

import it.unifi.drivehub.domain.operation.Rent;
import it.unifi.drivehub.domain.operation.TestDrive;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class BookingRulesTest {
    @Test
    void testDriveCannotStartAfterSixteenEvenByOneMicrosecond() {
        var day = LocalDate.of(2030, 1, 10);
        assertThrows(IllegalArgumentException.class,
                () -> TestDrive.create(null, 1L, 1L, day.atTime(16, 0).plusNanos(1000)));
    }

    @Test
    void rentalOverlapIncludesBothEndpoints() {
        var day = LocalDate.of(2030, 1, 10);
        var rent = Rent.create(null, 1L, 1L, new BigDecimal("20.00"), day, day.plusDays(2));
        assertTrue(rent.overlapsWith(day.minusDays(1), day));
        assertTrue(rent.overlapsWith(day.plusDays(2), day.plusDays(3)));
        assertTrue(rent.overlapsWith(day.plusDays(1), day.plusDays(1)));
        assertFalse(rent.overlapsWith(day.minusDays(2), day.minusDays(1)));
        assertFalse(rent.overlapsWith(day.plusDays(3), day.plusDays(3)));
    }
}
