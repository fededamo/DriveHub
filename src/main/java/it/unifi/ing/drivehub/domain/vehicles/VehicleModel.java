package it.unifi.ing.drivehub.domain.vehicles;

import it.unifi.ing.drivehub.domain.BaseEntity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

public final class VehicleModel extends BaseEntity {
    private final String brandName;
    private final String name;
    private final BigDecimal releasePrice;
    private final int releaseYear;

    public VehicleModel(Long id, String brandName, String name, BigDecimal releasePrice, int releaseYear) {
        super(id);
        if (brandName == null || brandName.isBlank()) {
            throw new IllegalArgumentException("brandName must not be blank");
        }
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("model name must not be blank");
        }
        this.brandName = brandName.trim();
        this.name = name.trim();
        this.releasePrice = Objects.requireNonNull(releasePrice, "releasePrice");
        if (this.releasePrice.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("releasePrice cannot be negative");
        }
        int currentYear = LocalDate.now().getYear();
        if (releaseYear < 1900 || releaseYear > currentYear) {
            throw new IllegalArgumentException("Anno di rilascio non valido (" + releaseYear + "): deve essere compreso tra 1900 e " + currentYear);
        }
        this.releaseYear = releaseYear;
    }

    public static VehicleModel create(String brandName, String name, BigDecimal releasePrice, int releaseYear) {
        return new VehicleModel(null, brandName, name, releasePrice, releaseYear);
    }

    public String brandName() {
        return brandName;
    }

    public String name() {
        return name;
    }

    public BigDecimal releasePrice() {
        return releasePrice;
    }

    public int releaseYear() {
        return releaseYear;
    }

    @Override
    public String toString() {
        return brandName + " " + name + " (" + releaseYear + ")";
    }
}
