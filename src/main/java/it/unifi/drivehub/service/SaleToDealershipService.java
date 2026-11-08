package it.unifi.drivehub.service;

import it.unifi.drivehub.dao.BankAccountDao;
import it.unifi.drivehub.dao.DatabaseManager;
import it.unifi.drivehub.dao.DiscountDao;
import it.unifi.drivehub.dao.ModelDao;
import it.unifi.drivehub.dao.RequestDao;
import it.unifi.drivehub.dao.SaleDao;
import it.unifi.drivehub.dao.VehicleDao;
import it.unifi.drivehub.domain.discount.ModelDiscount;
import it.unifi.drivehub.domain.operation.Request;
import it.unifi.drivehub.domain.operation.RequestType;
import it.unifi.drivehub.domain.operation.Sale;
import it.unifi.drivehub.domain.user.BankAccount;
import it.unifi.drivehub.domain.vehicle.Model;
import it.unifi.drivehub.domain.vehicle.Vehicle;
import it.unifi.drivehub.domain.vehicle.VehicleState;

import java.math.BigDecimal;
import java.sql.Connection;
import java.util.Optional;

/**
 * Servizio per la gestione della proposta di vendita di un veicolo da parte del cliente alla concessionaria.
 * Rispetta le regole di gestione unicità del VIN e controllo di storicizzazione (Pagina 3 & 4 degli appunti).
 */
public final class SaleToDealershipService {

    private final DatabaseManager db;
    private final VehicleDao vehicleDao;
    private final ModelDao modelDao;
    private final DiscountDao discountDao;
    private final RequestDao requestDao;
    private final BankAccountDao bankAccountDao;
    private final SaleDao saleDao;
    private final PriceCalculatorService priceCalculator;

    public SaleToDealershipService(DatabaseManager db, VehicleDao vehicleDao, ModelDao modelDao,
                                   DiscountDao discountDao, RequestDao requestDao,
                                   BankAccountDao bankAccountDao, SaleDao saleDao,
                                   PriceCalculatorService priceCalculator) {
        this.db = db;
        this.vehicleDao = vehicleDao;
        this.modelDao = modelDao;
        this.discountDao = discountDao;
        this.requestDao = requestDao;
        this.bankAccountDao = bankAccountDao;
        this.saleDao = saleDao;
        this.priceCalculator = priceCalculator;
    }

    /**
     * Calcola la stima preventiva per il veicolo del cliente.
     */
    public BigDecimal calculateEstimate(Long modelId, String plate, int km, int year) {
        Model model = modelDao.findById(modelId)
                .orElseThrow(() -> new IllegalArgumentException("Modello non trovato: " + modelId));
        if (year < model.getReleaseYear()) {
            throw new IllegalArgumentException("L'anno di produzione del veicolo (" + year
                    + ") non può essere precedente all'anno di rilascio del modello (" + model.getReleaseYear() + ").");
        }
        int mDisc = discountDao.findModelDiscount(modelId).map(ModelDiscount::getValue).orElse(0);
        return priceCalculator.estimateVehicleValue(model.getReleasePrice(), mDisc, plate, km, year);
    }

    /**
     * Passo 1: Il cliente conferma la proposta di vendita alla concessionaria.
     * Il VIN noto richiede stato SOLD, ultima vendita al proponente e nessuna richiesta aperta.
     * Lock/rilettura e scritture condividono la transazione; VIN e richiesta sono univoci anche nel DB.
     */
    public Request submitSaleProposal(Long clientId, Long modelId, String vin, String licencePlate, int km, int year) {
        try {
            return db.executeInTransactionWithResult(conn -> {
                Optional<Vehicle> existingOpt = vehicleDao.findByVinForUpdate(vin, conn);
                Vehicle vehicle;
                if (existingOpt.isPresent()) {
                    vehicle = existingOpt.get();
                    if (vehicle.getState() != VehicleState.SOLD) {
                        throw new IllegalStateException("Puoi proporre solo un veicolo venduto a te e senza richieste aperte.");
                    }
                    Sale latestSale = saleDao.findLatestPurchaseForVehicle(vehicle.getId(), conn)
                            .orElseThrow(() -> new IllegalStateException("Nessuna vendita al cliente registrata per questo VIN."));
                    if (!latestSale.getClientId().equals(clientId)) {
                        throw new IllegalStateException("L'ultima vendita di questo veicolo è a un altro cliente.");
                    }
                    if (requestDao.hasOpenRequestForVehicle(vehicle.getId(), conn)) {
                        throw new IllegalStateException("Esiste già una richiesta aperta per questo veicolo.");
                    }
                    if (!vehicle.getModelId().equals(modelId) || vehicle.getYear() != year) {
                        throw new IllegalArgumentException("Modello e anno devono corrispondere al veicolo già registrato per questo VIN.");
                    }
                    vehicle.setLicencePlate(licencePlate);
                    vehicle.setKilometers(km);
                    vehicle.setState(VehicleState.WAITING);
                    vehicleDao.update(vehicle, conn);
                } else {
                    BigDecimal modelBase = modelDao.findById(modelId)
                            .orElseThrow(() -> new IllegalArgumentException("Modello non trovato: " + modelId)).getReleasePrice();
                    Vehicle newVehicle = Vehicle.create(null, modelId, vin, licencePlate, km, modelBase, year, VehicleState.WAITING);
                    vehicle = vehicleDao.save(newVehicle, conn);
                }
                BigDecimal estimatedPrice = calculateEstimate(modelId, licencePlate, km, year);
                Request request = Request.createNewPurchaseRequest(vehicle.getId(), clientId, estimatedPrice);
                return requestDao.save(request, conn);
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
     * Passo 2: Il venditore approva la proposta di vendita.
     * - Registra un'acquisizione negativa per la concessionaria
     * - Lo stato del veicolo diventa SALE
     * - Elimina la tupla da Request
     * - Accredita il pagamento sul conto corrente del cliente
     */
    public void approveSaleProposal(Long requestId, Long salesmanId) {
        db.executeInTransaction(conn -> {
            Request request = lockWaitingPurchaseRequest(requestId, conn);
            if (salesmanId == null) throw new IllegalArgumentException("Il venditore che accetta la cessione è obbligatorio.");

            BigDecimal payout = request.getDeposit();

            // Accredito sul conto del cliente
            BankAccount account = bankAccountDao.findByClientIdForUpdate(request.getClientId(), conn)
                    .orElseThrow(() -> new IllegalStateException("Conto corrente cliente non trovato."));
            account.deposit(payout);
            bankAccountDao.updateBalance(account.getIban(), account.getBalance(), conn);

            // Registrazione compravendita in Sale
            Sale saleRecord = Sale.createAcquisitionRecord(request.getVehicleId(), salesmanId, request.getClientId(), payout);
            saleDao.save(saleRecord, conn);

            // Stato veicolo diventa SALE
            if (!vehicleDao.updateStateAtomically(request.getVehicleId(), VehicleState.WAITING, VehicleState.SALE, conn)) {
                throw new IllegalStateException("Il veicolo non è più in attesa di valutazione.");
            }

            // Eliminazione richiesta
            requestDao.delete(requestId, conn);
        });
    }

    /**
     * Il venditore rifiuta la proposta di vendita del cliente.
     * Elimina il veicolo soltanto se privo di storico; altrimenti ripristina SOLD.
     * La presenza di operazioni non equivale alla titolarità, che richiede l'ultima PURCHASE.
     */
    public void rejectSaleProposal(Long requestId) {
        db.executeInTransaction(conn -> {
            Request request = lockWaitingPurchaseRequest(requestId, conn);

            Long vehicleId = request.getVehicleId();
            requestDao.delete(requestId, conn);

            if (vehicleDao.hasAssociatedOperations(vehicleId, conn)) {
                if (!vehicleDao.updateStateAtomically(vehicleId, VehicleState.WAITING, VehicleState.SOLD, conn)) {
                    throw new IllegalStateException("Il veicolo non è più in attesa di valutazione.");
                }
            } else {
                vehicleDao.delete(vehicleId, conn);
            }
        });
    }

    private Request lockWaitingPurchaseRequest(Long requestId, Connection conn) {
        Request request = requestDao.findByIdForUpdate(requestId, conn)
                .orElseThrow(() -> new IllegalStateException("Richiesta non trovata: " + requestId));
        if (request.getType() != RequestType.PURCHASE) {
            throw new IllegalArgumentException("La richiesta selezionata non è una cessione.");
        }
        Vehicle vehicle = vehicleDao.findByIdForUpdate(request.getVehicleId(), conn)
                .orElseThrow(() -> new IllegalStateException("Veicolo non trovato: " + request.getVehicleId()));
        if (vehicle.getState() != VehicleState.WAITING) {
            throw new IllegalStateException("Il veicolo non è in attesa di valutazione.");
        }
        return request;
    }
}

