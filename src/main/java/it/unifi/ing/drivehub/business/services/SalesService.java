package it.unifi.ing.drivehub.business.services;

import it.unifi.ing.drivehub.business.core.TransactionRunner;
import it.unifi.ing.drivehub.business.exceptions.ConflictException;
import it.unifi.ing.drivehub.business.exceptions.EntityNotFoundException;
import it.unifi.ing.drivehub.dao.interfaces.DaoFactory;
import it.unifi.ing.drivehub.domain.DomainRuleViolationException;
import it.unifi.ing.drivehub.domain.observer.InventoryEvent;
import it.unifi.ing.drivehub.domain.observer.InventoryEventType;
import it.unifi.ing.drivehub.domain.observer.InventoryObserver;
import it.unifi.ing.drivehub.domain.observer.InventorySubject;
import it.unifi.ing.drivehub.domain.sales.Request;
import it.unifi.ing.drivehub.domain.sales.Sale;
import it.unifi.ing.drivehub.domain.users.BankAccount;
import it.unifi.ing.drivehub.domain.vehicles.Vehicle;
import it.unifi.ing.drivehub.domain.vehicles.VehicleModel;
import it.unifi.ing.drivehub.domain.vehicles.VehicleState;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Servizio applicativo per la compravendita dei veicoli e la gestione delle richieste.
 */
public final class SalesService implements InventorySubject {
    private final TransactionRunner transactions;
    private final PricingService pricingService;
    private final List<InventoryObserver> observers = new CopyOnWriteArrayList<>();

    public SalesService(DaoFactory daoFactory) {
        this(daoFactory, new PricingService(daoFactory));
    }

    public SalesService(DaoFactory daoFactory, PricingService pricingService) {
        this.transactions = new TransactionRunner(daoFactory);
        this.pricingService = Objects.requireNonNull(pricingService, "pricingService");
    }

    @Override
    public void subscribe(InventoryObserver observer) {
        if (observer != null && !observers.contains(observer)) {
            observers.add(observer);
        }
    }

    @Override
    public void unsubscribe(InventoryObserver observer) {
        observers.remove(observer);
    }

    private void notifyObservers(InventoryEvent event) {
        for (InventoryObserver observer : observers) {
            try {
                observer.onInventoryEvent(event);
            } catch (Exception ignored) {
            }
        }
    }

    /**
     * Il cliente richiede l'acquisto di un veicolo pagando l'acconto (pari al 20% del prezzo calcolato):
     * 1. Verifica disponibilità veicolo (SALE).
     * 2. Addebita l'acconto sul conto bancario del cliente.
     * 3. Cambia stato veicolo in BLOCKED.
     * 4. Registra la tupla in Request con deposit > 0, salesman = null, exp_date = null.
     */
    public Request requestPurchase(long clientId, long vehicleId, BigDecimal deposit) {
        Objects.requireNonNull(deposit, "deposit");
        return transactions.execute(unit -> {
            Vehicle vehicle = unit.vehicles().findById(vehicleId)
                    .orElseThrow(() -> new EntityNotFoundException("vehicle", vehicleId));
            if (vehicle.state() != VehicleState.SALE) {
                throw new DomainRuleViolationException("Il veicolo selezionato non è disponibile per la vendita (stato: " + vehicle.state() + ")");
            }

            BankAccount account = unit.bankAccounts().findByClientId(clientId)
                    .orElseThrow(() -> new EntityNotFoundException("BankAccount for client", clientId));
            account.withdraw(deposit);
            unit.bankAccounts().updateBalance(account.iban(), account.balance());

            unit.vehicles().updateState(vehicleId, VehicleState.BLOCKED);

            Request request = Request.createPurchase(vehicleId, clientId, deposit);
            Request saved = unit.requests().save(request);

            notifyObservers(InventoryEvent.statusChanged(vehicleId, vehicle.licencePlate(), VehicleState.SALE, VehicleState.BLOCKED));
            return saved;
        });
    }

    public Request initiatePurchase(long clientId, long vehicleId, BigDecimal deposit) {
        return requestPurchase(clientId, vehicleId, deposit);
    }

    /**
     * Un venditore autorizza l'operazione di acquisto:
     * Assegna l'ID del venditore e imposta exp_date esattamente a 1 mese dal giorno di accettazione.
     */
    public void authorizePurchase(long requestId, long salesmanId) {
        transactions.execute(unit -> {
            Request request = unit.requests().findById(requestId)
                    .orElseThrow(() -> new EntityNotFoundException("request", requestId));
            if (!request.isPurchase()) {
                throw new DomainRuleViolationException("Solo le richieste d'acquisto possono essere autorizzate con scadenza.");
            }
            LocalDate expDate = LocalDate.now().plusMonths(1);
            unit.requests().updateAuthorization(requestId, salesmanId, expDate);
            return null;
        });
    }

    public void unlockFullPayment(long requestId) {
        authorizePurchase(requestId, 1L);
    }

    /**
     * Il cliente paga il resto della somma entro exp_date:
     * 1. Saldo residuo (prezzo - acconto) sottratto dal conto corrente del cliente.
     * 2. Stato veicolo modificato in SOLD (il veicolo NON viene eliminato dal DB).
     * 3. Creata tupla in Sale con amount = prezzo veicolo e date = data odierna.
     * 4. Tupla Request eliminata.
     * 5. Annullamento automatico tramite Observer di tutti i test drive futuri per quel veicolo.
     */
    public Sale completePurchase(long requestId, long salesmanId) {
        return transactions.execute(unit -> {
            Request request = unit.requests().findById(requestId)
                    .orElseThrow(() -> new EntityNotFoundException("request", requestId));
            Vehicle vehicle = unit.vehicles().findById(request.vehicleId())
                    .orElseThrow(() -> new EntityNotFoundException("vehicle", request.vehicleId()));

            BigDecimal totalAmount = vehicle.price();
            BigDecimal remaining = totalAmount.subtract(request.deposit());
            if (remaining.compareTo(BigDecimal.ZERO) > 0) {
                BankAccount account = unit.bankAccounts().findByClientId(request.clientId())
                        .orElseThrow(() -> new EntityNotFoundException("BankAccount for client", request.clientId()));
                account.withdraw(remaining);
                unit.bankAccounts().updateBalance(account.iban(), account.balance());
            }

            // Il veicolo passa a SOLD e rimane nella base dati
            unit.vehicles().updateState(vehicle.requireId(), VehicleState.SOLD);
            unit.requests().delete(requestId);

            LocalDateTime now = LocalDateTime.now();
            Sale sale = Sale.record(vehicle.requireId(), salesmanId, request.clientId(), totalAmount, now);
            Sale savedSale = unit.sales().save(sale);

            // Annulla eventuali test drive futuri prenotati su questo veicolo
            unit.testDrives().cancelFutureTestDrivesForVehicle(vehicle.requireId(), now);

            notifyObservers(InventoryEvent.statusChanged(vehicle.requireId(), vehicle.licencePlate(), VehicleState.BLOCKED, VehicleState.SOLD));
            notifyObservers(InventoryEvent.of(InventoryEventType.TEST_DRIVES_CANCELLED_VEHICLE_UNAVAILABLE, vehicle.requireId(), vehicle.licencePlate()));

            return savedSale;
        });
    }

    /**
     * Mancato pagamento entro exp_date o annullamento con penale:
     * 1. Stato veicolo torna SALE.
     * 2. Creata tupla in Sale con vehicle = null e salesman = null (penale pari al 20% del deposit).
     * 3. Rimborso al cliente del rimanente 80% del deposit sul conto corrente.
     * 4. Tupla in Request eliminata.
     */
    public Sale cancelRequestWithPenalty(long requestId) {
        return transactions.execute(unit -> {
            Request request = unit.requests().findById(requestId)
                    .orElseThrow(() -> new EntityNotFoundException("request", requestId));

            BigDecimal deposit = request.deposit();
            BigDecimal penalty = deposit.multiply(new BigDecimal("0.20")).setScale(2, RoundingMode.HALF_UP);
            BigDecimal refund = deposit.subtract(penalty);

            BankAccount account = unit.bankAccounts().findByClientId(request.clientId())
                    .orElseThrow(() -> new EntityNotFoundException("BankAccount for client", request.clientId()));
            account.deposit(refund);
            unit.bankAccounts().updateBalance(account.iban(), account.balance());

            unit.requests().delete(requestId);
            unit.vehicles().updateState(request.vehicleId(), VehicleState.SALE);

            LocalDateTime penaltyDate = (request.expDate() != null)
                    ? request.expDate().atStartOfDay()
                    : LocalDateTime.now();

            Sale penaltySale = Sale.penalty(request.clientId(), penalty, penaltyDate);
            Sale saved = unit.sales().save(penaltySale);

            notifyObservers(InventoryEvent.requestExpired(request.vehicleId(), null));
            return saved;
        });
    }

    public Sale cancelPurchase(long requestId) {
        return cancelRequestWithPenalty(requestId);
    }

    /**
     * Controllo ed elaborazione automatica delle richieste di acquisto scadute:
     * Eseguito all'avvio e ad ogni consultazione.
     */
    public List<Sale> processExpiredRequests(LocalDate asOfDate) {
        LocalDate date = asOfDate != null ? asOfDate : LocalDate.now();
        List<Request> expired = transactions.execute(unit -> unit.requests().findExpiredRequests(date));
        List<Sale> penalties = new ArrayList<>();
        for (Request req : expired) {
            try {
                penalties.add(cancelRequestWithPenalty(req.requireId()));
            } catch (Exception ignored) {
            }
        }
        return List.copyOf(penalties);
    }

    /**
     * Il cliente propone la vendita del proprio veicolo alla concessionaria:
     * 1. Calcola il preventivo con lo sconto a cascata.
     * 2. Se il VIN esiste già ed è in stato SOLD (macchina precedentemente venduta dall'azienda),
     *    aggiorna la tupla esistente (targa, km, prezzo, anno, stato WAITING).
     * 3. Altrimenti crea una nuova tupla Vehicle con stato WAITING.
     * 4. Crea la tupla in Request con deposit = Vehicle.price * (-1).
     */
    public Vehicle submitVehicleForSale(String brandName, String modelName, int modelYear,
                                        String vin, String plate, int kilometers,
                                        BigDecimal price, int vehicleYear, long clientId) {
        return transactions.execute(unit -> {
            VehicleModel model = CatalogModels.findOrCreate(unit, brandName, modelName, price, modelYear);
            BigDecimal estimatedPrice = pricingService.calculateValuation(model.releasePrice(), plate, kilometers, vehicleYear);

            Optional<Vehicle> existing = unit.vehicles().findByVin(vin);
            Vehicle vehicle;
            if (existing.isPresent()) {
                Vehicle v = existing.get();
                if (v.state() != VehicleState.SOLD) {
                    throw new ConflictException("Un veicolo attivo con questo VIN è già presente a catalogo (stato: " + v.state() + ")");
                }
                unit.vehicles().updateDetails(v.requireId(), plate, kilometers, estimatedPrice, vehicleYear, VehicleState.WAITING);
                vehicle = unit.vehicles().findById(v.requireId()).orElseThrow();
            } else {
                vehicle = Vehicle.create(model.requireId(), vin, plate, kilometers, estimatedPrice, vehicleYear, VehicleState.WAITING);
                vehicle = unit.vehicles().save(vehicle);
            }

            Request request = Request.createSaleProposal(vehicle.requireId(), clientId, estimatedPrice);
            unit.requests().save(request);

            notifyObservers(InventoryEvent.statusChanged(vehicle.requireId(), vehicle.licencePlate(), null, VehicleState.WAITING));
            return vehicle;
        });
    }

    public Vehicle submitVehicleForSale(String brandName, String modelName, int modelYear,
                                        String vin, String plate, int kilometers,
                                        BigDecimal price, int vehicleYear) {
        return submitVehicleForSale(brandName, modelName, modelYear, vin, plate, kilometers, price, vehicleYear, 1L);
    }

    public Vehicle proposeVehicleForSale(long clientId, long modelId, String plate, String vin,
                                         BigDecimal price, int year, int kilometers) {
        return transactions.execute(unit -> {
            VehicleModel model = unit.vehicleModels().findById(modelId)
                    .orElseThrow(() -> new EntityNotFoundException("model", modelId));
            BigDecimal estimatedPrice = pricingService.calculateValuation(model.releasePrice(), plate, kilometers, year);

            Optional<Vehicle> existing = unit.vehicles().findByVin(vin);
            Vehicle vehicle;
            if (existing.isPresent()) {
                Vehicle v = existing.get();
                if (v.state() != VehicleState.SOLD) {
                    throw new ConflictException("Un veicolo attivo con questo VIN è già presente a catalogo (stato: " + v.state() + ")");
                }
                unit.vehicles().updateDetails(v.requireId(), plate, kilometers, estimatedPrice, year, VehicleState.WAITING);
                vehicle = unit.vehicles().findById(v.requireId()).orElseThrow();
            } else {
                vehicle = Vehicle.create(modelId, vin, plate, kilometers, estimatedPrice, year, VehicleState.WAITING);
                vehicle = unit.vehicles().save(vehicle);
            }

            Request request = Request.createSaleProposal(vehicle.requireId(), clientId, estimatedPrice);
            unit.requests().save(request);

            notifyObservers(InventoryEvent.statusChanged(vehicle.requireId(), vehicle.licencePlate(), null, VehicleState.WAITING));
            return vehicle;
        });
    }

    /**
     * Un venditore approva la proposta di vendita del cliente:
     * 1. Creata tupla in Sale con amount = deposit (negativo).
     * 2. Stato del veicolo aggiornato in SALE.
     * 3. Eliminata la tupla di Request.
     * 4. Il saldo del cliente viene incrementato del valore dell'auto.
     */
    public Sale approveClientSale(long requestId, long salesmanId) {
        return transactions.execute(unit -> {
            Request request = unit.requests().findById(requestId)
                    .orElseThrow(() -> new EntityNotFoundException("request", requestId));
            if (!request.isSaleProposal()) {
                throw new DomainRuleViolationException("La richiesta specificata non è una proposta di vendita da cliente.");
            }
            Vehicle vehicle = unit.vehicles().findById(request.vehicleId())
                    .orElseThrow(() -> new EntityNotFoundException("vehicle", request.vehicleId()));

            // Aggiorna stato veicolo a SALE
            unit.vehicles().updateState(vehicle.requireId(), VehicleState.SALE);

            // Incrementa saldo del cliente del valore dell'auto
            BigDecimal payout = request.deposit().abs();
            BankAccount account = unit.bankAccounts().findByClientId(request.clientId())
                    .orElseThrow(() -> new EntityNotFoundException("BankAccount for client", request.clientId()));
            account.deposit(payout);
            unit.bankAccounts().updateBalance(account.iban(), account.balance());

            // Crea tupla in Sale con amount negativo
            Sale sale = Sale.record(vehicle.requireId(), salesmanId, request.clientId(), request.deposit(), LocalDateTime.now());
            Sale saved = unit.sales().save(sale);

            unit.requests().delete(requestId);

            notifyObservers(InventoryEvent.statusChanged(vehicle.requireId(), vehicle.licencePlate(), VehicleState.WAITING, VehicleState.SALE));
            return saved;
        });
    }

    public void approveVehicle(long vehicleId) {
        transactions.execute(unit -> {
            Optional<Request> reqOpt = unit.requests().findByVehicleId(vehicleId);
            if (reqOpt.isPresent() && reqOpt.get().isSaleProposal()) {
                approveClientSale(reqOpt.get().requireId(), 1L);
            } else {
                unit.vehicles().updateState(vehicleId, VehicleState.SALE);
            }
            return null;
        });
    }

    /**
     * Un venditore rifiuta la proposta di vendita del cliente:
     * 1. Elimina la Request.
     * 2. Controlla se il veicolo ha operazioni di vendita pregresse:
     *    - Se sì (auto precedentemente venduta): non cancella la riga, la reimposta a SOLD.
     *    - Se no: cancella la tupla Vehicle.
     */
    public void rejectClientSale(long requestId, long salesmanId) {
        transactions.execute(unit -> {
            Request request = unit.requests().findById(requestId)
                    .orElseThrow(() -> new EntityNotFoundException("request", requestId));
            long vehicleId = request.vehicleId();

            unit.requests().delete(requestId);

            // Prima di eliminare la tupla, controlla se ci sono operazioni di vendita associate
            if (unit.sales().hasOperationsForVehicle(vehicleId)) {
                unit.vehicles().updateState(vehicleId, VehicleState.SOLD);
            } else {
                unit.vehicles().delete(vehicleId);
            }
            return null;
        });
    }

    public void rejectVehicle(long vehicleId) {
        transactions.execute(unit -> {
            Optional<Request> reqOpt = unit.requests().findByVehicleId(vehicleId);
            if (reqOpt.isPresent() && reqOpt.get().isSaleProposal()) {
                rejectClientSale(reqOpt.get().requireId(), 1L);
            } else {
                if (unit.sales().hasOperationsForVehicle(vehicleId)) {
                    unit.vehicles().updateState(vehicleId, VehicleState.SOLD);
                } else {
                    unit.vehicles().delete(vehicleId);
                }
            }
            return null;
        });
    }

    public List<Request> findRequestsByClient(long clientId) {
        return transactions.execute(unit -> unit.requests().findByClientId(clientId));
    }

    public List<Request> allPendingRequests() {
        return transactions.execute(unit -> unit.requests().findAll());
    }

    public List<Sale> salesByClient(long clientId) {
        return transactions.execute(unit -> unit.sales().findByClientId(clientId));
    }

    public List<Sale> allSales() {
        return transactions.execute(unit -> unit.sales().findAll());
    }
}
