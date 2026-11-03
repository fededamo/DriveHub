package it.unifi.drivehub.service;

import it.unifi.drivehub.dao.BrandDao;
import it.unifi.drivehub.dao.DatabaseManager;
import it.unifi.drivehub.dao.DiscountDao;
import it.unifi.drivehub.dao.ModelDao;
import it.unifi.drivehub.dao.VehicleDao;
import it.unifi.drivehub.domain.discount.ModelDiscount;
import it.unifi.drivehub.domain.discount.VehicleDiscount;
import it.unifi.drivehub.domain.vehicle.Brand;
import it.unifi.drivehub.domain.vehicle.Model;
import it.unifi.drivehub.domain.vehicle.Vehicle;
import it.unifi.drivehub.domain.vehicle.VehicleState;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Servizio per la consultazione e la gestione del catalogo da parte del Manager (Pagina 4 degli appunti).
 * Impone i vincoli di eliminazione condizionale su Marchi, Modelli e Veicoli.
 */
public final class CatalogService {

    private final DatabaseManager db;
    private final BrandDao brandDao;
    private final ModelDao modelDao;
    private final VehicleDao vehicleDao;
    private final DiscountDao discountDao;
    private final PriceCalculatorService priceCalculator;

    public CatalogService(DatabaseManager db, BrandDao brandDao, ModelDao modelDao,
                          VehicleDao vehicleDao, DiscountDao discountDao,
                          PriceCalculatorService priceCalculator) {
        this.db = db;
        this.brandDao = brandDao;
        this.modelDao = modelDao;
        this.vehicleDao = vehicleDao;
        this.discountDao = discountDao;
        this.priceCalculator = priceCalculator;
    }

    public List<Brand> getAllBrands() {
        return brandDao.findAll();
    }

    public List<Model> getAllModels() {
        return modelDao.findAll();
    }

    public List<Model> getModelsByBrand(String brandName) {
        return modelDao.findByBrand(brandName);
    }

    public Optional<Model> getModelById(Long id) {
        if (id == null) return Optional.empty();
        return modelDao.findById(id);
    }

    public List<Vehicle> getVehiclesByState(VehicleState state) {
        return vehicleDao.findByState(state);
    }

    public List<Vehicle> getAllVehicles() {
        return vehicleDao.findAll();
    }

    /** Dati di una singola lettura: nessun accesso ai DAO durante la visualizzazione. */
    public record VehicleRow(Vehicle vehicle, String modelName, BigDecimal base,
                             BigDecimal recommended, BigDecimal effective, BigDecimal dailyRate) {}

    public record ModelDiscountRow(ModelDiscount discount, String modelName) {}

    public List<VehicleRow> getVehicleRows(VehicleState state) {
        Map<Long, Model> models = getAllModels().stream()
                .collect(Collectors.toMap(Model::getId, Function.identity()));
        Map<Long, Integer> modelDiscounts = getAllModelDiscounts().stream()
                .collect(Collectors.toMap(ModelDiscount::getModelId, ModelDiscount::getValue));
        Map<Long, Integer> vehicleDiscounts = getAllVehicleDiscounts().stream()
                .collect(Collectors.toMap(VehicleDiscount::getVehicleId, VehicleDiscount::getValue));
        List<Vehicle> vehicles = state == null ? getAllVehicles() : getVehiclesByState(state);
        return vehicles.stream().map(vehicle -> {
            Model model = models.get(vehicle.getModelId());
            if (model == null) throw new IllegalStateException("Modello del veicolo non trovato.");
            int mDisc = modelDiscounts.getOrDefault(model.getId(), 0);
            int vDisc = vehicleDiscounts.getOrDefault(vehicle.getId(), 0);
            BigDecimal effective = priceCalculator.calculateVehiclePrice(model, vehicle, mDisc, vDisc);
            return new VehicleRow(vehicle, model.getBrand() + " " + model.getName(),
                    vehicle.getSelectedBasePrice(model),
                    priceCalculator.calculateRecommendedVehiclePrice(model, vehicle, mDisc, vDisc),
                    effective, priceCalculator.calculateDailyRentalRate(effective));
        }).toList();
    }

    public List<ModelDiscountRow> getModelDiscountRows() {
        Map<Long, Model> models = getAllModels().stream()
                .collect(Collectors.toMap(Model::getId, Function.identity()));
        return getAllModelDiscounts().stream().map(discount -> {
            Model model = models.get(discount.getModelId());
            if (model == null) throw new IllegalStateException("Modello dello sconto non trovato.");
            return new ModelDiscountRow(discount, model.getBrand() + " " + model.getName());
        }).toList();
    }

    public Optional<Vehicle> getVehicleById(Long id) {
        return vehicleDao.findById(id);
    }

    public Optional<ModelDiscount> getModelDiscount(Long modelId) {
        return discountDao.findModelDiscount(modelId);
    }

    public Optional<VehicleDiscount> getVehicleDiscount(Long vehicleId) {
        return discountDao.findVehicleDiscount(vehicleId);
    }

    public List<ModelDiscount> getAllModelDiscounts() {
        return discountDao.findAllModelDiscounts();
    }

    public List<VehicleDiscount> getAllVehicleDiscounts() {
        return discountDao.findAllVehicleDiscounts();
    }

    public BigDecimal getVehicleBasePrice(Vehicle vehicle) {
        Model model = getModelById(vehicle.getModelId())
                .orElseThrow(() -> new IllegalStateException("Modello del veicolo non trovato."));
        return vehicle.getSelectedBasePrice(model);
    }

    public BigDecimal getEffectiveVehiclePrice(Vehicle vehicle) {
        return calculateVehiclePrice(vehicle, false);
    }

    public BigDecimal getRecommendedVehiclePrice(Vehicle vehicle) {
        return calculateVehiclePrice(vehicle, true);
    }

    private BigDecimal calculateVehiclePrice(Vehicle vehicle, boolean recommended) {
        if (vehicle == null) return BigDecimal.ZERO;
        Model model = getModelById(vehicle.getModelId())
                .orElseThrow(() -> new IllegalStateException("Modello del veicolo non trovato."));
        Integer mDisc = discountDao.findModelDiscount(model.getId()).map(ModelDiscount::getValue).orElse(0);
        Integer vDisc = discountDao.findVehicleDiscount(vehicle.getId()).map(VehicleDiscount::getValue).orElse(0);
        return recommended
                ? priceCalculator.calculateRecommendedVehiclePrice(model, vehicle, mDisc, vDisc)
                : priceCalculator.calculateVehiclePrice(model, vehicle, mDisc, vDisc);
    }

    /** Una base nulla ripristina il consigliato; un valore seleziona la modalità manuale. */
    public void updateVehiclePricing(Long vehicleId, BigDecimal manualBase) {
        db.executeInTransaction(conn -> {
            Vehicle vehicle = vehicleDao.findById(vehicleId, conn)
                    .orElseThrow(() -> new IllegalArgumentException("Veicolo non trovato: " + vehicleId));
            if (manualBase == null) vehicle.useRecommendedPrice();
            else vehicle.setPrice(manualBase);
            vehicleDao.updatePricing(vehicle, conn);
        });
    }

    // --- OPERAZIONI AMMINISTRATIVE DEL MANAGER (PAGINA 4) ---

    public void addBrand(String name) {
        Brand brand = Brand.create(name);
        db.executeInTransaction(conn -> brandDao.save(brand, conn));
    }

    public void deleteBrand(String brandName) {
        if (brandDao.hasModels(brandName)) {
            throw new IllegalStateException("Impossibile eliminare il brand '" + brandName + "': sono presenti modelli associati.");
        }
        db.executeInTransaction(conn -> brandDao.delete(brandName, conn));
    }

    public Model addModel(String brand, String name, BigDecimal releasePrice, int releaseYear) {
        Model model = Model.create(null, brand, name, releasePrice, releaseYear);
        return db.executeInTransactionWithResult(conn -> modelDao.save(model, conn));
    }

    public void deleteModel(Long modelId) {
        if (modelDao.hasVehicles(modelId)) {
            throw new IllegalStateException("Impossibile eliminare il modello: sono presenti veicoli associati a questo modello nel catalogo.");
        }
        db.executeInTransaction(conn -> modelDao.delete(modelId, conn));
    }

    /** price è la base manuale; null seleziona la base del modello. */
    public Vehicle addVehicle(Long modelId, String vin, String licencePlate, int km, BigDecimal price, int year, VehicleState state) {
        Model model = getModelById(modelId)
                .orElseThrow(() -> new IllegalArgumentException("Modello non trovato: " + modelId));
        Vehicle vehicle = Vehicle.create(null, modelId, vin, licencePlate, km,
                price != null ? price : model.getReleasePrice(), year, state);
        if (price != null) vehicle.setPrice(price);
        vehicle.validateProductionYear(model.getReleaseYear());
        return db.executeInTransactionWithResult(conn -> vehicleDao.save(vehicle, conn));
    }

    public void deleteVehicle(Long vehicleId) {
        if (vehicleDao.hasAssociatedOperations(vehicleId)) {
            throw new IllegalStateException("Impossibile eliminare il veicolo: sono presenti operazioni associate (Test Drive, Noleggi o Vendite).");
        }
        db.executeInTransaction(conn -> vehicleDao.delete(vehicleId, conn));
    }

    public void setModelDiscount(Long modelId, int discountPercent) {
        ModelDiscount discount = ModelDiscount.create(modelId, discountPercent);
        db.executeInTransaction(conn -> discountDao.saveModelDiscount(discount, conn));
    }

    public void removeModelDiscount(Long modelId) {
        db.executeInTransaction(conn -> discountDao.deleteModelDiscount(modelId, conn));
    }

    public void setVehicleDiscount(Long vehicleId, int discountPercent) {
        VehicleDiscount discount = VehicleDiscount.create(vehicleId, discountPercent);
        db.executeInTransaction(conn -> discountDao.saveVehicleDiscount(discount, conn));
    }

    public void removeVehicleDiscount(Long vehicleId) {
        db.executeInTransaction(conn -> discountDao.deleteVehicleDiscount(vehicleId, conn));
    }
}

