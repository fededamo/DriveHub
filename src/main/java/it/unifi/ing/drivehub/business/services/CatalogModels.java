package it.unifi.ing.drivehub.business.services;

import it.unifi.ing.drivehub.dao.interfaces.UnitOfWork;
import it.unifi.ing.drivehub.domain.vehicles.Brand;
import it.unifi.ing.drivehub.domain.vehicles.VehicleModel;

import java.math.BigDecimal;

/** Ricerca o creazione modello e marca all'interno della transazione del chiamante. */
final class CatalogModels {
    private CatalogModels() { }

    static VehicleModel findOrCreate(UnitOfWork unit, String brandName, String modelName, BigDecimal releasePrice, int releaseYear) {
        Brand validatedBrand = Brand.create(brandName);
        unit.brands().findByName(validatedBrand.name())
                .orElseGet(() -> unit.brands().save(validatedBrand));

        return unit.vehicleModels().findByName(modelName)
                .orElseGet(() -> {
                    BigDecimal price = releasePrice != null ? releasePrice : new BigDecimal("20000.00");
                    int year = releaseYear > 0 ? releaseYear : 2024;
                    VehicleModel model = VehicleModel.create(validatedBrand.name(), modelName, price, year);
                    return unit.vehicleModels().save(model);
                });
    }
}
