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
     * Gestisce la concorrenza e il vincolo di unicità sul VIN:
     * - Se il VIN è già nel database (veicolo precedentemente venduto dal concessionario), aggiorna la tupla esistente
     * - Altrimenti inserisce una nuova tupla
     * - Crea la richiesta con stato WAITING e deposit negativo
     */
    public Request submitSaleProposal(Long clientId, Long modelId, String vin, String licencePlate, int km, int year) {
        BigDecimal estimatedPrice = calculateEstimate(modelId, licencePlate, km, year);

        return db.executeInTransactionWithResult(conn -> {
            Optional<Vehicle> existingOpt = vehicleDao.findByVin(vin, conn);
            Vehicle vehicle;

            if (existingOpt.isPresent()) {
                // Aggiornamento della tupla esistente per non violare l'unicità del VIN
                vehicle = existingOpt.get();
                vehicle.setLicencePlate(licencePlate);
                vehicle.setKilometers(km);
                vehicle.setPrice(estimatedPrice);
                vehicle.setState(VehicleState.WAITING);
                vehicleDao.update(vehicle, conn);
            } else {
                // Nuovo inserimento
                Vehicle newVehicle = Vehicle.create(null, modelId, vin, licencePlate, km, estimatedPrice, year, VehicleState.WAITING);
                vehicle = vehicleDao.save(newVehicle, conn);
            }

            // Generazione tupla in Request con deposit negativo
            Request request = Request.createNewSaleRequest(vehicle.getId(), clientId, estimatedPrice);
            return requestDao.save(request, conn);
        });
    }

    /**
     * Passo 2: Il venditore approva la proposta di vendita.
     * - Crea una tupla in Sale con amount = deposit (positivo per il cliente)
     * - Lo stato del veicolo diventa SALE
     * - Elimina la tupla da Request
     * - Accredita il pagamento sul conto corrente del cliente
     */
    public void approveSaleProposal(Long requestId, Long salesmanId) {
        db.executeInTransaction(conn -> {
            Request request = requestDao.findById(requestId, conn)
                    .orElseThrow(() -> new IllegalStateException("Richiesta non trovata: " + requestId));
            if (request.getType() != RequestType.SALE) {
                throw new IllegalArgumentException("La richiesta selezionata non è di vendita.");
            }

            Vehicle vehicle = vehicleDao.findById(request.getVehicleId(), conn)
                    .orElseThrow(() -> new IllegalStateException("Veicolo non trovato: " + request.getVehicleId()));

            // Importo da accreditare al cliente (valore assoluto di deposit)
            BigDecimal payout = request.getDeposit().abs();

            // Accredito sul conto del cliente
            BankAccount account = bankAccountDao.findByClientId(request.getClientId(), conn)
                    .orElseThrow(() -> new IllegalStateException("Conto corrente cliente non trovato."));
            account.deposit(payout);
            bankAccountDao.updateBalance(account.getIban(), account.getBalance(), conn);

            // Registrazione compravendita in Sale
            Sale saleRecord = Sale.createAcquisitionRecord(vehicle.getId(), salesmanId, request.getClientId(), payout);
            saleDao.save(saleRecord, conn);

            // Stato veicolo diventa SALE
            vehicle.setState(VehicleState.SALE);
            vehicleDao.update(vehicle, conn);

            // Eliminazione richiesta
            requestDao.delete(requestId, conn);
        });
    }

    /**
     * Il venditore rifiuta la proposta di vendita del cliente.
     * Regola Pagina 4: prima di eliminare la tupla Vehicle, controlla se ci sono operazioni di vendita associate.
     * Se non ce ne sono elimina il veicolo, altrimenti ripristina lo stato a SOLD lasciandolo intatto.
     */
    public void rejectSaleProposal(Long requestId) {
        db.executeInTransaction(conn -> {
            Request request = requestDao.findById(requestId, conn)
                    .orElseThrow(() -> new IllegalStateException("Richiesta non trovata: " + requestId));
            if (request.getType() != RequestType.SALE) {
                throw new IllegalArgumentException("La richiesta selezionata non è di vendita.");
            }

            Long vehicleId = request.getVehicleId();
            requestDao.delete(requestId, conn);

            boolean hasSales = saleDao.hasSaleOperationsForVehicle(vehicleId);
            if (hasSales) {
                // Il veicolo ha uno storico vendite: ripristina stato SOLD senza cancellarlo
                Vehicle vehicle = vehicleDao.findById(vehicleId, conn).orElse(null);
                if (vehicle != null) {
                    vehicle.setState(VehicleState.SOLD);
                    vehicleDao.update(vehicle, conn);
                }
            } else {
                // Nessuna vendita associata: può essere rimosso dal catalogo in sicurezza
                vehicleDao.delete(vehicleId, conn);
            }
        });
    }
}

