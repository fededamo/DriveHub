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
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

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
    private final Map<Long, Optional<Model>> modelCache = new ConcurrentHashMap<>();

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
        return modelCache.computeIfAbsent(id, modelDao::findById);
    }

    public List<Vehicle> getVehiclesByState(VehicleState state) {
        return vehicleDao.findByState(state);
    }

    public List<Vehicle> getAllVehicles() {
        return vehicleDao.findAll();
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

    public BigDecimal getEffectiveVehiclePrice(Vehicle vehicle) {
        if (vehicle == null) return BigDecimal.ZERO;
        Model model = getModelById(vehicle.getModelId()).orElse(null);
        if (model == null) {
            return vehicle.getPrice();
        }
        Integer mDisc = discountDao.findModelDiscount(model.getId()).map(ModelDiscount::getValue).orElse(0);
        Integer vDisc = discountDao.findVehicleDiscount(vehicle.getId()).map(VehicleDiscount::getValue).orElse(0);
        return priceCalculator.calculateVehiclePrice(model, vehicle, mDisc, vDisc);
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
        modelCache.clear();
        Model model = Model.create(null, brand, name, releasePrice, releaseYear);
        return db.executeInTransactionWithResult(conn -> modelDao.save(model, conn));
    }

    public void deleteModel(Long modelId) {
        modelCache.clear();
        if (modelDao.hasVehicles(modelId)) {
            throw new IllegalStateException("Impossibile eliminare il modello: sono presenti veicoli associati a questo modello nel catalogo.");
        }
        db.executeInTransaction(conn -> modelDao.delete(modelId, conn));
    }

    public Vehicle addVehicle(Long modelId, String vin, String licencePlate, int km, BigDecimal price, int year, VehicleState state) {
        Vehicle vehicle = Vehicle.create(null, modelId, vin, licencePlate, km, price, year, state);
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

