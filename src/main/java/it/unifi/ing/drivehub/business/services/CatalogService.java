package it.unifi.ing.drivehub.business.services;

import it.unifi.ing.drivehub.business.core.TransactionRunner;
import it.unifi.ing.drivehub.business.exceptions.ConflictException;
import it.unifi.ing.drivehub.business.exceptions.EntityNotFoundException;
import it.unifi.ing.drivehub.dao.interfaces.DaoFactory;
import it.unifi.ing.drivehub.domain.DomainRuleViolationException;
import it.unifi.ing.drivehub.domain.vehicles.Brand;
import it.unifi.ing.drivehub.domain.vehicles.Vehicle;
import it.unifi.ing.drivehub.domain.vehicles.VehicleModel;
import it.unifi.ing.drivehub.domain.vehicles.VehicleState;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

public final class CatalogService {
    private final TransactionRunner transactions;

    public CatalogService(DaoFactory daoFactory) {
        this.transactions = new TransactionRunner(daoFactory);
    }

    public List<Vehicle> availableForSale() {
        return transactions.execute(unit -> unit.vehicles().findByState(VehicleState.SALE));
    }

    public List<Vehicle> availableForRental() {
        return transactions.execute(unit -> unit.vehicles().findByState(VehicleState.RENT));
    }

    public List<Vehicle> waitingVehicles() {
        return transactions.execute(unit -> unit.vehicles().findByState(VehicleState.WAITING));
    }

    public List<Vehicle> allVehicles() {
        return transactions.execute(unit -> unit.vehicles().findAll());
    }

    public Vehicle vehicle(long vehicleId) {
        return transactions.execute(unit -> unit.vehicles().findById(vehicleId)
                .orElseThrow(() -> new EntityNotFoundException("vehicle", vehicleId)));
    }

    public VehicleModel model(long modelId) {
        return transactions.execute(unit -> unit.vehicleModels().findById(modelId)
                .orElseThrow(() -> new EntityNotFoundException("model", modelId)));
    }

    public List<Brand> brands() {
        return transactions.execute(unit -> List.copyOf(unit.brands().findAll()));
    }

    public List<VehicleModel> models() {
        return transactions.execute(unit -> List.copyOf(unit.vehicleModels().findAll()));
    }

    public Brand addBrand(String name) {
        Objects.requireNonNull(name, "brand name");
        return transactions.execute(unit -> unit.brands().save(new Brand(name.trim())));
    }

    public void deleteBrand(String brandName) {
        transactions.execute(unit -> {
            if (unit.brands().hasModels(brandName)) {
                throw new DomainRuleViolationException("Impossibile eliminare il brand " + brandName + ": esistono modelli associati a questo marchio.");
            }
            unit.brands().delete(brandName);
            return null;
        });
    }

    public VehicleModel addModel(String brandName, String name, BigDecimal releasePrice, int releaseYear) {
        return transactions.execute(unit -> {
            unit.brands().save(new Brand(brandName.trim()));
            VehicleModel model = VehicleModel.create(brandName, name, releasePrice, releaseYear);
            return unit.vehicleModels().save(model);
        });
    }

    public void deleteModel(long modelId) {
        transactions.execute(unit -> {
            if (unit.vehicleModels().hasVehicles(modelId)) {
                throw new DomainRuleViolationException("Impossibile eliminare il modello #" + modelId + ": esistono veicoli a catalogo associati a questo modello.");
            }
            unit.vehicleModels().delete(modelId);
            return null;
        });
    }

    public Vehicle addVehicle(long modelId, String vin, String licencePlate, int kilometers, BigDecimal price, int year, VehicleState state) {
        return transactions.execute(unit -> {
            if (unit.vehicles().findByVin(vin).isPresent()) {
                throw new ConflictException("Un veicolo con VIN " + vin + " è già presente nel sistema.");
            }
            Vehicle vehicle = Vehicle.create(modelId, vin, licencePlate, kilometers, price, year, state);
            return unit.vehicles().save(vehicle);
        });
    }

    public void deleteVehicle(long vehicleId) {
        transactions.execute(unit -> {
            if (unit.vehicles().hasOperations(vehicleId)) {
                throw new DomainRuleViolationException("Impossibile eliminare il veicolo #" + vehicleId + ": sono presenti operazioni collegate (Test Drive, Noleggio o Vendita).");
            }
            unit.vehicles().delete(vehicleId);
            return null;
        });
    }
}
