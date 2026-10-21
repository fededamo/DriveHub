package it.unifi.ing.drivehub.business.services;

import it.unifi.ing.drivehub.business.core.TransactionRunner;
import it.unifi.ing.drivehub.business.exceptions.ConflictException;
import it.unifi.ing.drivehub.business.exceptions.EntityNotFoundException;
import it.unifi.ing.drivehub.dao.interfaces.DaoFactory;
import it.unifi.ing.drivehub.domain.DomainRuleViolationException;
import it.unifi.ing.drivehub.domain.observer.InventoryEvent;
import it.unifi.ing.drivehub.domain.observer.InventoryEventType;
import it.unifi.ing.drivehub.domain.observer.InventoryObserver;
import it.unifi.ing.drivehub.domain.rentals.Rent;
import it.unifi.ing.drivehub.domain.users.BankAccount;
import it.unifi.ing.drivehub.domain.vehicles.Vehicle;
import it.unifi.ing.drivehub.domain.vehicles.VehicleState;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import it.unifi.ing.drivehub.domain.observer.InventorySubject;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;

public final class RentalService implements InventorySubject {
    private final TransactionRunner transactions;
    private final PricingService pricingService;
    private final List<InventoryObserver> observers = new CopyOnWriteArrayList<>();

    public RentalService(DaoFactory daoFactory) {
        this(daoFactory, new PricingService(daoFactory));
    }

    public RentalService(DaoFactory daoFactory, PricingService pricingService) {
        this.transactions = new TransactionRunner(daoFactory);
        this.pricingService = Objects.requireNonNull(pricingService, "pricingService");
    }

    @Override
    public void subscribe(InventoryObserver observer) {
        observers.add(Objects.requireNonNull(observer, "observer"));
    }

    @Override
    public void unsubscribe(InventoryObserver observer) {
        observers.remove(observer);
    }

    public void addObserver(InventoryObserver observer) {
        subscribe(observer);
    }

    public void removeObserver(InventoryObserver observer) {
        unsubscribe(observer);
    }

    private void notifyObservers(InventoryEvent event) {
        for (InventoryObserver observer : observers) {
            try {
                observer.onInventoryEvent(event);
            } catch (RuntimeException ignored) {
            }
        }
    }

    /**
     * Il cliente noleggia un'auto:
     * 1. Solo veicoli con stato 'RENT'.
     * 2. Verifica che nessun giorno dell'intervallo si sovrapponga a noleggi già esistenti.
     * 3. Calcolo del costo totale (tariffa giornaliera 0.1% + sconto 5% a scaglioni ogni 5 giorni).
     * 4. Pagamento anticipato al 100% scalato dal conto corrente bancario.
     * 5. Salvataggio tupla in Rent e rollback atomico in caso di qualsiasi errore.
     */
    public Rent bookRental(long clientId, long vehicleId, LocalDate start, LocalDate end) {
        Objects.requireNonNull(start, "start");
        Objects.requireNonNull(end, "end");
        if (end.isBefore(start)) {
            throw new IllegalArgumentException("La data di fine noleggio non può precedere la data di inizio");
        }
        if (start.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("La data di inizio noleggio non può essere nel passato");
        }

        return transactions.execute(unit -> {
            Vehicle vehicle = unit.vehicles().findById(vehicleId)
                    .orElseThrow(() -> new EntityNotFoundException("vehicle", vehicleId));
            if (vehicle.state() != VehicleState.RENT) {
                throw new DomainRuleViolationException("Il veicolo non è configurato per il noleggio (stato: " + vehicle.state() + ")");
            }

            // Se anche un solo giorno si sovrappone a una prenotazione già esistente, l'intervallo non è legale
            if (unit.rentals().hasOverlappingRentals(vehicleId, start, end)) {
                throw new ConflictException("Le date selezionate si sovrappongono a un noleggio già esistente per questo veicolo.");
            }

            long days = ChronoUnit.DAYS.between(start, end) + 1;
            BigDecimal amount = pricingService.calculateRentalPrice(vehicle, days);

            // Addebito anticipato al 100% sul conto corrente del cliente
            BankAccount account = unit.bankAccounts().findByClientId(clientId)
                    .orElseThrow(() -> new EntityNotFoundException("BankAccount for client", clientId));
            account.withdraw(amount);
            unit.bankAccounts().updateBalance(account.iban(), account.balance());

            Rent rent = Rent.create(vehicleId, clientId, amount, start, end);
            Rent savedRent = unit.rentals().save(rent);

            // Annulla test drive futuri su questo veicolo compresi nelle date di noleggio (il noleggio ha priorità)
            unit.testDrives().cancelTestDrivesInRange(vehicleId, start.atStartOfDay(), end.atTime(23, 59, 59));

            notifyObservers(InventoryEvent.of(InventoryEventType.TEST_DRIVES_CANCELLED_VEHICLE_UNAVAILABLE, vehicleId, vehicle.licencePlate()));

            return savedRent;
        });
    }

    /**
     * Un venditore può modificare lo stato di un veicolo da RENT a SALE e viceversa:
     * Se un veicolo passa da RENT a SALE, tutte le prenotazioni future di noleggio vengono annullate,
     * i clienti vengono rimborsati al 100% dell'addebito sul loro conto, e le tuple eliminate da Rent.
     */
    public void switchVehicleState(long vehicleId, VehicleState newState) {
        Objects.requireNonNull(newState, "newState");
        transactions.execute(unit -> {
            Vehicle vehicle = unit.vehicles().findById(vehicleId)
                    .orElseThrow(() -> new EntityNotFoundException("vehicle", vehicleId));

            if (vehicle.state() == VehicleState.RENT && newState == VehicleState.SALE) {
                List<Rent> futureRentals = unit.rentals().findFutureRentalsForVehicle(vehicleId, LocalDate.now());
                for (Rent r : futureRentals) {
                    BankAccount clientAccount = unit.bankAccounts().findByClientId(r.clientId())
                            .orElseThrow(() -> new EntityNotFoundException("BankAccount for client", r.clientId()));
                    clientAccount.deposit(r.amount());
                    unit.bankAccounts().updateBalance(clientAccount.iban(), clientAccount.balance());
                    unit.rentals().delete(r.requireId());
                }
                notifyObservers(InventoryEvent.of(InventoryEventType.RENTALS_CANCELLED_STATE_CHANGE, vehicleId, vehicle.licencePlate()));
            }

            unit.vehicles().updateState(vehicleId, newState);
            notifyObservers(InventoryEvent.statusChanged(vehicleId, vehicle.licencePlate(), vehicle.state(), newState));
            return null;
        });
    }

    public List<Rent> findRentalsByClient(long clientId) {
        return transactions.execute(unit -> unit.rentals().findByClientId(clientId));
    }

    public List<Rent> findRentalsByVehicle(long vehicleId) {
        return transactions.execute(unit -> unit.rentals().findByVehicleId(vehicleId));
    }

    public List<Rent> allRentals() {
        return transactions.execute(unit -> unit.rentals().findAll());
    }
}
