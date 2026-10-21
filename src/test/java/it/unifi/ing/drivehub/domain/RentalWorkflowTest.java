package it.unifi.ing.drivehub.domain;

import it.unifi.ing.drivehub.domain.rentals.Rent;
import it.unifi.ing.drivehub.domain.rentals.TestDrive;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.*;

class RentalWorkflowTest {
    @Test
    @DisplayName("Create Rent record validates required fields and dates")
    void rentCreationValidatesFields() {
        LocalDate start = LocalDate.now();
        LocalDate end = start.plusDays(3);
        Rent rent = Rent.create(1L, 2L, new BigDecimal("150.00"), start, end);

        assertNotNull(rent);
        assertEquals(1L, rent.vehicleId());
        assertEquals(2L, rent.clientId());
        assertEquals(new BigDecimal("150.00"), rent.amount());
        assertEquals(start, rent.startDate());
        assertEquals(end, rent.endDate());
    }

    @Test
    @DisplayName("Create TestDrive validates date and times (1 hour slot)")
    void testDriveCreationValidates() {
        LocalDate date = LocalDate.now().plusDays(1);
        LocalTime start = LocalTime.of(10, 0);

        TestDrive td = TestDrive.create(1L, 2L, date, start);

        assertNotNull(td);
        assertEquals(1L, td.vehicleId());
        assertEquals(2L, td.clientId());
        assertEquals(date, td.date());
        assertEquals(start, td.start());
        assertEquals(LocalTime.of(11, 0), td.end());
    }
}
