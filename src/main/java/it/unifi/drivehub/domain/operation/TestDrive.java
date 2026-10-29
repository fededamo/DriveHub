package it.unifi.drivehub.domain.operation;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Entità TestDrive (prenotazione giro di prova).
 * Invarianti:
 * - Durata fissa di 1 ora
 * - Orario di inizio compreso rigorosamente nella fascia 9:00 - 16:00
 */
public final class TestDrive {

    private final Long id;
    private final Long vehicleId;
    private final Long clientId;
    private final LocalDateTime date;

    private TestDrive(Long id, Long vehicleId, Long clientId, LocalDateTime date) {
        this.id = id;
        this.vehicleId = vehicleId;
        this.clientId = clientId;
        this.date = date;
    }

    public static TestDrive create(Long id, Long vehicleId, Long clientId, LocalDateTime date) {
        if (vehicleId == null) {
            throw new IllegalArgumentException("L'ID del veicolo non può essere nullo.");
        }
        if (clientId == null) {
            throw new IllegalArgumentException("L'ID del cliente non può essere nullo.");
        }
        if (date == null) {
            throw new IllegalArgumentException("La data e l'ora del test drive non possono essere nulle.");
        }
        int hour = date.getHour();
        if (hour < 9 || hour > 16 || (hour == 16 && (date.getMinute() > 0 || date.getSecond() > 0))) {
            throw new IllegalArgumentException("L'ora di inizio di un TestDrive deve essere compresa nella fascia oraria 9:00 - 16:00.");
        }
        return new TestDrive(id, vehicleId, clientId, date);
    }

    public Long getId() {
        return id;
    }

    public Long getVehicleId() {
        return vehicleId;
    }

    public Long getClientId() {
        return clientId;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public boolean isFuture() {
        return date.isAfter(LocalDateTime.now());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TestDrive testDrive)) return false;
        return id != null && Objects.equals(id, testDrive.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "TestDrive{" +
                "id=" + id +
                ", vehicleId=" + vehicleId +
                ", clientId=" + clientId +
                ", date=" + date +
                '}';
    }
}

