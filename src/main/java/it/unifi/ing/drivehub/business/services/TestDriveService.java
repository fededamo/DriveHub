package it.unifi.ing.drivehub.business.services;

import it.unifi.ing.drivehub.business.core.TransactionRunner;
import it.unifi.ing.drivehub.business.exceptions.ConflictException;
import it.unifi.ing.drivehub.business.exceptions.EntityNotFoundException;
import it.unifi.ing.drivehub.dao.interfaces.DaoFactory;
import it.unifi.ing.drivehub.domain.DomainRuleViolationException;
import it.unifi.ing.drivehub.domain.rentals.TestDrive;
import it.unifi.ing.drivehub.domain.vehicles.Vehicle;
import it.unifi.ing.drivehub.domain.vehicles.VehicleState;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Objects;

public final class TestDriveService {
    private final TransactionRunner transactions;

    public TestDriveService(DaoFactory daoFactory) {
        this.transactions = new TransactionRunner(daoFactory);
    }

    /**
     * Prenotazione test di guida:
     * 1. Permessa su veicoli sia in vendita (SALE) che a noleggio (RENT).
     * 2. Massimo 3 test di guida prenotati per cliente.
     * 3. Slot durata 1 ora, orari disponibili 9:00 - 16:00 a inizio ora.
     * 4. Nessuna sovrapposizione d'orario per lo stesso veicolo.
     * 5. Nessuna disponibilità se il veicolo è noleggiato per quel giorno.
     */
    public TestDrive book(long clientId, long vehicleId, LocalDate date, LocalTime start) {
        Objects.requireNonNull(date, "date");
        Objects.requireNonNull(start, "start");

        if (date.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("La data del test drive non può essere nel passato");
        }
        int hour = start.getHour();
        if (hour < 9 || hour > 16) {
            throw new IllegalArgumentException("L'orario di inizio del test drive deve essere compreso nella fascia 09:00 - 16:00");
        }

        LocalDateTime dateTime = date.atTime(hour, 0);
        if (dateTime.isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("L'orario del test drive non può essere nel passato");
        }

        return transactions.execute(unit -> {
            Vehicle vehicle = unit.vehicles().findById(vehicleId)
                    .orElseThrow(() -> new EntityNotFoundException("vehicle", vehicleId));
            if (vehicle.state() != VehicleState.SALE && vehicle.state() != VehicleState.RENT) {
                throw new DomainRuleViolationException("I test drive possono essere prenotati solo su veicoli in vendita (SALE) o da noleggiare (RENT).");
            }

            // Massimo 3 test drive prenotati per cliente
            int activeCount = unit.testDrives().countActiveByClientId(clientId, LocalDateTime.now());
            if (activeCount >= 3) {
                throw new DomainRuleViolationException("Hai già raggiunto il limite massimo di 3 test di guida prenotati.");
            }

            // Verifica che il veicolo non sia noleggiato per quel giorno
            if (unit.rentals().isVehicleRentedOnDate(vehicleId, date)) {
                throw new ConflictException("Il veicolo è noleggiato per il giorno selezionato: nessuna fascia oraria è disponibile.");
            }

            // Verifica che l'orario non sia già occupato per lo stesso veicolo
            if (unit.testDrives().hasConflictingTestDrive(vehicleId, dateTime)) {
                throw new ConflictException("La fascia oraria delle " + String.format("%02d:00", hour) + " per questo veicolo è già occupata.");
            }

            TestDrive testDrive = TestDrive.create(vehicleId, clientId, dateTime);
            return unit.testDrives().save(testDrive);
        });
    }

    public List<TestDrive> findTestDrivesByClient(long clientId) {
        return transactions.execute(unit -> unit.testDrives().findByClientId(clientId));
    }

    public List<TestDrive> allTestDrives() {
        return transactions.execute(unit -> unit.testDrives().findAll());
    }
}
