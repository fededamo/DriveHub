package it.unifi.drivehub.service;

import it.unifi.drivehub.dao.BankAccountDao;
import it.unifi.drivehub.dao.DatabaseManager;
import it.unifi.drivehub.dao.DiscountDao;
import it.unifi.drivehub.dao.ModelDao;
import it.unifi.drivehub.dao.RentDao;
import it.unifi.drivehub.dao.VehicleDao;
import it.unifi.drivehub.dao.UserDao;
import it.unifi.drivehub.domain.discount.ModelDiscount;
import it.unifi.drivehub.domain.discount.VehicleDiscount;
import it.unifi.drivehub.domain.operation.Rent;
import it.unifi.drivehub.domain.user.BankAccount;
import it.unifi.drivehub.domain.vehicle.Model;
import it.unifi.drivehub.domain.vehicle.Vehicle;
import it.unifi.drivehub.domain.vehicle.VehicleState;
import it.unifi.drivehub.event.VehicleEventPublisher;
import it.unifi.drivehub.event.VehicleCommitmentEvent;

import java.math.BigDecimal;
import java.sql.Connection;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

/**
 * Servizio per la gestione del noleggio auto e della concorrenza sulle date (Pagina 1 & 3 degli appunti).
 */
public final class RentalService {

    private final DatabaseManager db;
    private final VehicleDao vehicleDao;
    private final UserDao userDao;
    private final ModelDao modelDao;
    private final DiscountDao discountDao;
    private final RentDao rentDao;
    private final BankAccountDao bankAccountDao;
    private final PriceCalculatorService priceCalculator;
    private final VehicleEventPublisher eventPublisher;

    public RentalService(DatabaseManager db, VehicleDao vehicleDao, UserDao userDao, ModelDao modelDao,
                         DiscountDao discountDao, RentDao rentDao,
                         BankAccountDao bankAccountDao, PriceCalculatorService priceCalculator,
                         VehicleEventPublisher eventPublisher) {
        this.db = db;
        this.vehicleDao = vehicleDao;
        this.userDao = userDao;
        this.modelDao = modelDao;
        this.discountDao = discountDao;
        this.rentDao = rentDao;
        this.bankAccountDao = bankAccountDao;
        this.priceCalculator = priceCalculator;
        this.eventPublisher = eventPublisher;
    }

    /**
     * Calcola il preventivo del noleggio per le date selezionate.
     */
    public BigDecimal calculateRentalCost(Long vehicleId, LocalDate start, LocalDate end) {
        return db.executeInTransactionWithResult(conn -> {
            Vehicle vehicle = vehicleDao.findById(vehicleId, conn)
                    .orElseThrow(() -> new IllegalArgumentException("Veicolo non trovato: " + vehicleId));
            return calculateRentalCost(vehicle, start, end, conn);
        });
    }

    private BigDecimal calculateRentalCost(Vehicle vehicle, LocalDate start, LocalDate end, Connection conn) {
        Model model = modelDao.findById(vehicle.getModelId(), conn)
                .orElseThrow(() -> new IllegalArgumentException("Modello non trovato: " + vehicle.getModelId()));

        Integer mDisc = discountDao.findModelDiscount(model.getId(), conn).map(ModelDiscount::getValue).orElse(0);
        Integer vDisc = discountDao.findVehicleDiscount(vehicle.getId(), conn).map(VehicleDiscount::getValue).orElse(0);
        BigDecimal currentPrice = priceCalculator.calculateVehiclePrice(model, vehicle, mDisc, vDisc);

        return priceCalculator.calculateTotalRentalCost(currentPrice, start, end);
    }

    /**
     * Prenota un noleggio. Controllo concorrente delle date e creazione eseguite nella stessa transazione.
     * Addebito immediato al 100% dell'importo sul conto bancario.
     */
    public Rent bookRental(Long vehicleId, Long clientId, LocalDate start, LocalDate end) {
        if (start.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("La data di inizio noleggio non può essere nel passato.");
        }
        if (end.isBefore(start)) {
            throw new IllegalArgumentException("La data di fine noleggio non può precedere la data di inizio.");
        }

        try {
            return db.executeInTransactionWithResult(conn -> {
                Vehicle vehicle = vehicleDao.findByIdForUpdate(vehicleId, conn)
                        .orElseThrow(() -> new IllegalStateException("Veicolo non trovato: " + vehicleId));
                if (vehicle.getState() != VehicleState.RENT) {
                    throw new IllegalStateException("Il veicolo non è abilitato per il noleggio.");
                }
                userDao.lockClient(clientId, conn);

                // Verifica concorrente delle date nella stessa transazione
                boolean available = rentDao.isVehicleAvailableForRent(vehicleId, start, end, conn);
                if (!available) {
                    throw new IllegalStateException("Il veicolo risulta già prenotato per il periodo o per alcune date selezionate.");
                }

                BigDecimal totalCost = calculateRentalCost(vehicle, start, end, conn);

                // Addebito al 100% sul conto del cliente
                BankAccount account = bankAccountDao.findByClientIdForUpdate(clientId, conn)
                        .orElseThrow(() -> new IllegalStateException("Conto cliente non trovato."));
                account.withdraw(totalCost);
                bankAccountDao.updateBalance(account.getIban(), account.getBalance(), conn);

                // Salvataggio tupla Rent
                Rent rent = Rent.create(null, vehicleId, clientId, totalCost, start, end);
                Rent createdRent = rentDao.save(rent, conn);
                eventPublisher.publishCommitment(new VehicleCommitmentEvent(vehicleId), conn);
                return createdRent;
            });
        } catch (RuntimeException failure) {
            if (failure.getCause() instanceof RuntimeException reason
                    && (reason instanceof IllegalArgumentException || reason instanceof IllegalStateException)) {
                throw reason;
            }
            throw failure;
        }

    }

    /**
     * Cambio stato veicolo da RENT a SALE (o viceversa) da parte del venditore.
     * Come da appunti (Pagina 3):
     * "Se un veicolo adibito al noleggio con prenotazioni future viene cambiato di stato,
     * i clienti devono essere rimborsati dell'addebito precedentemente versato.
     * Le prenotazioni future vengono annullate."
     */
    public int changeVehicleRentalState(Long vehicleId, VehicleState targetState) {
        if (targetState != VehicleState.SALE && targetState != VehicleState.RENT) {
            throw new IllegalArgumentException("La transizione ammessa è solo tra RENT e SALE.");
        }

        try {
            return db.executeInTransactionWithResult(conn -> {
                Vehicle vehicle = vehicleDao.findByIdForUpdate(vehicleId, conn)
                        .orElseThrow(() -> new IllegalStateException("Veicolo non trovato: " + vehicleId));
                VehicleState oldState = vehicle.getState();
                if ((oldState != VehicleState.SALE && oldState != VehicleState.RENT) || oldState == targetState) {
                    throw new IllegalStateException("La transizione è ammessa solo tra vendita e noleggio; stato attuale: " + oldState + ".");
                }

                int refundedCount = 0;
                if (oldState == VehicleState.RENT && targetState == VehicleState.SALE) {
                    LocalDate today = LocalDate.now();
                    if (!rentDao.isVehicleAvailableForRent(vehicleId, today, today, conn)) {
                        throw new IllegalStateException("Impossibile destinare alla vendita un veicolo con un noleggio in corso.");
                    }
                    // Elimina prenotazioni future e rimborsa i clienti
                    List<Rent> cancelledRents = rentDao.deleteFutureByVehicleId(vehicleId, conn);
                    cancelledRents.sort(Comparator.comparing(Rent::getClientId));
                    for (Rent cancelled : cancelledRents) {
                        BankAccount account = bankAccountDao.findByClientIdForUpdate(cancelled.getClientId(), conn)
                                .orElseThrow(() -> new IllegalStateException("Conto cliente non trovato."));
                        account.deposit(cancelled.getAmount());
                        bankAccountDao.updateBalance(account.getIban(), account.getBalance(), conn);
                        refundedCount++;
                    }
                }

                vehicle.setState(targetState);
                vehicleDao.update(vehicle, conn);
                return refundedCount;
            });
        } catch (RuntimeException failure) {
            if (failure.getCause() instanceof RuntimeException reason
                    && (reason instanceof IllegalArgumentException || reason instanceof IllegalStateException)) {
                throw reason;
            }
            throw failure;
        }
    }

    public List<Rent> getFutureRentsForVehicle(Long vehicleId) {
        return rentDao.findFutureByVehicleId(vehicleId);
    }
}

