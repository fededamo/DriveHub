package it.unifi.ing.drivehub.domain.rentals;

import it.unifi.ing.drivehub.domain.BaseEntity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Objects;

/**
 * Prenotazione di una prova su strada (Test Drive).
 * Durata fissa: 1 ora. Fascia oraria permessa: 9:00 - 16:00 (slot orari a inizio ora).
 */
public final class TestDrive extends BaseEntity {
    private final long vehicleId;
    private final long clientId;
    private final LocalDateTime date;

    public TestDrive(Long id, long vehicleId, long clientId, LocalDateTime date) {
        super(id);
        if (vehicleId <= 0) {
            throw new IllegalArgumentException("vehicleId must be positive");
        }
        if (clientId <= 0) {
            throw new IllegalArgumentException("clientId must be positive");
        }
        this.vehicleId = vehicleId;
        this.clientId = clientId;
        this.date = Objects.requireNonNull(date, "date");

        int hour = date.getHour();
        if (hour < 9 || hour > 16 || date.getMinute() != 0) {
            throw new IllegalArgumentException("L'orario del test drive deve essere compreso nella fascia 09:00 - 16:00 a inizio ora (slot di 1 ora)");
        }
    }

    public static TestDrive create(long vehicleId, long clientId, LocalDateTime date) {
        return new TestDrive(null, vehicleId, clientId, date);
    }

    public static TestDrive create(long vehicleId, long clientId, LocalDate date, LocalTime start) {
        Objects.requireNonNull(date, "date");
        Objects.requireNonNull(start, "start");
        return new TestDrive(null, vehicleId, clientId, date.atTime(start.getHour(), 0));
    }

    public static TestDrive book(long vehicleId, long clientId, LocalDate date, LocalTime start) {
        return create(vehicleId, clientId, date, start);
    }

    public long vehicleId() {
        return vehicleId;
    }

    public long clientId() {
        return clientId;
    }

    public LocalDateTime dateTime() {
        return date;
    }

    public LocalDate date() {
        return date.toLocalDate();
    }

    public LocalTime start() {
        return date.toLocalTime();
    }

    public LocalTime end() {
        return date.toLocalTime().plusHours(1);
    }
}
