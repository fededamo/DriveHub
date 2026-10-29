package it.unifi.drivehub.service;

import it.unifi.drivehub.dao.BankAccountDao;
import it.unifi.drivehub.dao.DatabaseManager;
import it.unifi.drivehub.dao.DiscountDao;
import it.unifi.drivehub.dao.ModelDao;
import it.unifi.drivehub.dao.RequestDao;
import it.unifi.drivehub.dao.SaleDao;
import it.unifi.drivehub.dao.VehicleDao;
import it.unifi.drivehub.domain.discount.ModelDiscount;
import it.unifi.drivehub.domain.discount.VehicleDiscount;
import it.unifi.drivehub.domain.operation.Request;
import it.unifi.drivehub.domain.operation.RequestType;
import it.unifi.drivehub.domain.operation.Sale;
import it.unifi.drivehub.domain.user.BankAccount;
import it.unifi.drivehub.domain.vehicle.Model;
import it.unifi.drivehub.domain.vehicle.Vehicle;
import it.unifi.drivehub.domain.vehicle.VehicleState;
import it.unifi.drivehub.event.VehicleEventPublisher;
import it.unifi.drivehub.event.VehicleStateChangeEvent;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Servizio per la gestione del ciclo di vita dell'acquisto dei veicoli.
 * Include gestione rigorosa della concorrenza (con update atomico dello stato BLOCKED)
 * e rispetto delle transazioni ACID con rollback su errore (Pagina 3 degli appunti).
 */
public final class PurchaseService {

    private final DatabaseManager db;
    private final VehicleDao vehicleDao;
    private final ModelDao modelDao;
    private final DiscountDao discountDao;
    private final RequestDao requestDao;
    private final BankAccountDao bankAccountDao;
    private final SaleDao saleDao;
    private final PriceCalculatorService priceCalculator;
    private final VehicleEventPublisher eventPublisher;

    public PurchaseService(DatabaseManager db, VehicleDao vehicleDao, ModelDao modelDao,
                           DiscountDao discountDao, RequestDao requestDao,
                           BankAccountDao bankAccountDao, SaleDao saleDao,
                           PriceCalculatorService priceCalculator,
                           VehicleEventPublisher eventPublisher) {
        this.db = db;
        this.vehicleDao = vehicleDao;
        this.modelDao = modelDao;
        this.discountDao = discountDao;
        this.requestDao = requestDao;
        this.bankAccountDao = bankAccountDao;
        this.saleDao = saleDao;
        this.priceCalculator = priceCalculator;
        this.eventPublisher = eventPublisher;
    }

    /**
     * Passo 1 & 2 & 3: Il cliente blocca il veicolo e versa l'acconto del 20%.
     * Operazione atomica con lock concorrente sullo stato SALE.
     */
    public Request initiatePurchase(Long vehicleId, Long clientId) {
        return db.executeInTransactionWithResult(conn -> {
            // 1. Concorrenza: lo stato passa a BLOCKED solo se era SALE (nella stessa query)
            boolean updated = vehicleDao.updateStateAtomically(vehicleId, VehicleState.SALE, VehicleState.BLOCKED, conn);
            if (!updated) {
                throw new IllegalStateException("Il veicolo non è più disponibile per l'acquisto (è già stato riservato o venduto).");
            }

            // 2. Calcolo prezzo effettivo e acconto (20%)
            Vehicle vehicle = vehicleDao.findById(vehicleId, conn)
                    .orElseThrow(() -> new IllegalStateException("Veicolo non trovato: " + vehicleId));
            Model model = modelDao.findById(vehicle.getModelId())
                    .orElseThrow(() -> new IllegalStateException("Modello non trovato: " + vehicle.getModelId()));

            Integer mDisc = discountDao.findModelDiscount(model.getId()).map(ModelDiscount::getValue).orElse(0);
            Integer vDisc = discountDao.findVehicleDiscount(vehicle.getId()).map(VehicleDiscount::getValue).orElse(0);
            BigDecimal currentPrice = priceCalculator.calculateVehiclePrice(model, vehicle, mDisc, vDisc);
            BigDecimal deposit = priceCalculator.calculatePurchaseDeposit(currentPrice);

            // 3. Verifica e addebito sul conto corrente del cliente
            BankAccount account = bankAccountDao.findByClientId(clientId, conn)
                    .orElseThrow(() -> new IllegalStateException("Conto corrente non trovato per il cliente id: " + clientId));

            account.withdraw(deposit);
            bankAccountDao.updateBalance(account.getIban(), account.getBalance(), conn);

            // 4. Creazione tupla in Request con salesman = null e exp_date = null
            Request newRequest = Request.createNewPurchaseRequest(vehicleId, clientId, deposit);
            return requestDao.save(newRequest, conn);
        });
    }

    /**
     * Passo 4.1: Il venditore rifiuta la proposta di acquisto.
     * I soldi dell'acconto vengono restituiti, il veicolo torna SALE, la request viene cancellata.
     */
    public void rejectPurchase(Long requestId) {
        db.executeInTransaction(conn -> {
            Request request = requestDao.findById(requestId, conn)
                    .orElseThrow(() -> new IllegalStateException("Richiesta non trovata: " + requestId));
            if (request.getType() != RequestType.PURCHASE) {
                throw new IllegalArgumentException("La richiesta non è di acquisto.");
            }

            // Restituzione acconto
            BankAccount account = bankAccountDao.findByClientId(request.getClientId(), conn)
                    .orElseThrow(() -> new IllegalStateException("Conto cliente non trovato."));
            account.deposit(request.getDeposit());
            bankAccountDao.updateBalance(account.getIban(), account.getBalance(), conn);

            // Stato veicolo torna SALE
            Vehicle vehicle = vehicleDao.findById(request.getVehicleId(), conn)
                    .orElseThrow(() -> new IllegalStateException("Veicolo non trovato."));
            vehicle.setState(VehicleState.SALE);
            vehicleDao.update(vehicle, conn);

            // Eliminazione richiesta
            requestDao.delete(requestId, conn);
        });
    }

    /**
     * Passo 4.2: Il venditore approva la richiesta.
     * La tupla viene aggiornata con salesman e scadenza fissata a un mese dopo.
     */
    public void approvePurchase(Long requestId, Long salesmanId) {
        db.executeInTransaction(conn -> {
            Request request = requestDao.findById(requestId, conn)
                    .orElseThrow(() -> new IllegalStateException("Richiesta non trovata: " + requestId));
            if (request.getType() != RequestType.PURCHASE) {
                throw new IllegalArgumentException("La richiesta non è di acquisto.");
            }

            request.setSalesmanId(salesmanId);
            request.setExpDate(LocalDate.now().plusMonths(1));
            requestDao.update(request, conn);
        });
    }

    /**
     * Passo 5.1: Il cliente paga il restante 80% entro la scadenza.
     * Si crea la riga in Sale, si cancella la Request, il veicolo diventa SOLD.
     */
    public void completePurchase(Long requestId) {
        VehicleStateChangeEvent event = db.executeInTransactionWithResult(conn -> {
            Request request = requestDao.findById(requestId, conn)
                    .orElseThrow(() -> new IllegalStateException("Richiesta non trovata: " + requestId));
            if (request.getSalesmanId() == null) {
                throw new IllegalStateException("La richiesta deve essere prima approvata da un venditore.");
            }
            if (request.isExpired()) {
                throw new IllegalStateException("La richiesta è scaduta e non può più essere saldata.");
            }

            Vehicle vehicle = vehicleDao.findById(request.getVehicleId(), conn)
                    .orElseThrow(() -> new IllegalStateException("Veicolo non trovato."));
            Model model = modelDao.findById(vehicle.getModelId())
                    .orElseThrow(() -> new IllegalStateException("Modello non trovato."));

            Integer mDisc = discountDao.findModelDiscount(model.getId()).map(ModelDiscount::getValue).orElse(0);
            Integer vDisc = discountDao.findVehicleDiscount(vehicle.getId()).map(VehicleDiscount::getValue).orElse(0);
            BigDecimal fullPrice = priceCalculator.calculateVehiclePrice(model, vehicle, mDisc, vDisc);

            // Saldo restante: 80% del totale (prezzo pieno - acconto già versato)
            BigDecimal remainingAmount = fullPrice.subtract(request.getDeposit());

            // Addebito saldo restante
            BankAccount account = bankAccountDao.findByClientId(request.getClientId(), conn)
                    .orElseThrow(() -> new IllegalStateException("Conto cliente non trovato."));
            account.withdraw(remainingAmount);
            bankAccountDao.updateBalance(account.getIban(), account.getBalance(), conn);

            // Generazione riga in Sale (importo totale veicolo)
            Sale saleRecord = Sale.createPurchaseRecord(vehicle.getId(), request.getSalesmanId(), request.getClientId(), fullPrice);
            saleDao.save(saleRecord, conn);

            // Cancellazione riga Request
            requestDao.delete(requestId, conn);

            // Stato veicolo impostato su SOLD
            vehicle.setState(VehicleState.SOLD);
            vehicleDao.update(vehicle, conn);

            return new VehicleStateChangeEvent(vehicle.getId(), VehicleState.BLOCKED, VehicleState.SOLD);
        });

        // Notifica Observer (cancellazione automatica test drive futuri)
        if (event != null) {
            eventPublisher.publishStateChange(event);
        }
    }

    /**
     * Passo 5.2: Controllo scadenze all'accesso del cliente.
     * Se una richiesta d'acquisto approvata è scaduta senza saldo finale:
     * - Restituito l'80% dell'acconto al cliente (penale del 20%)
     * - Generata tupla in Sale con salesman = null, amount = 20% acconto trattenuto
     * - Cancellata la Request
     * - Veicolo torna in stato SALE
     * Ritorna i messaggi di notifica per l'utente.
     */
    public List<String> checkAndHandleExpiredRequests(Long clientId) {
        List<String> notifications = new ArrayList<>();
        List<Request> clientRequests = requestDao.findByClientId(clientId);

        for (Request req : clientRequests) {
            if (req.getType() == RequestType.PURCHASE && req.isExpired()) {
                db.executeInTransaction(conn -> {
                    BigDecimal deposit = req.getDeposit();
                    // Penale: 20% della caparra trattenuta dall'azienda
                    BigDecimal penalty = deposit.multiply(BigDecimal.valueOf(0.20)).setScale(2, RoundingMode.HALF_UP);
                    // Rimborsato: 80% della caparra restituito al cliente
                    BigDecimal refunded = deposit.subtract(penalty);

                    BankAccount account = bankAccountDao.findByClientId(clientId, conn)
                            .orElseThrow(() -> new IllegalStateException("Conto cliente non trovato."));
                    account.deposit(refunded);
                    bankAccountDao.updateBalance(account.getIban(), account.getBalance(), conn);

                    // Registrazione penale in Sale (salesman = null, vehicle = null)
                    Sale penaltyRecord = Sale.createPenaltyRecord(clientId, penalty);
                    saleDao.save(penaltyRecord, conn);

                    // Eliminazione tupla Request
                    requestDao.delete(req.getId(), conn);

                    // Ripristino stato veicolo a SALE
                    Vehicle vehicle = vehicleDao.findById(req.getVehicleId(), conn).orElse(null);
                    if (vehicle != null && vehicle.getState() == VehicleState.BLOCKED) {
                        vehicle.setState(VehicleState.SALE);
                        vehicleDao.update(vehicle, conn);
                    }
                });

                notifications.add("La richiesta di acquisto per il veicolo #" + req.getVehicleId() +
                        " è scaduta in data " + req.getExpDate() +
                        ". Come da regolamento, è stata applicata una penale del 20% sull'acconto. Ti sono stati rimborsati € " +
                        req.getDeposit().multiply(BigDecimal.valueOf(0.80)).setScale(2, RoundingMode.HALF_UP) + ".");
            }
        }
        return notifications;
    }
}

