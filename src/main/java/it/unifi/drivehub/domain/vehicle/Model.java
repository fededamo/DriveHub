package it.unifi.drivehub.domain.vehicle;

import java.math.BigDecimal;
import java.time.Year;
import java.util.Objects;

/**
 * Entità Model (Modello di veicolo).
 * Invarianti:
 * - Prezzo di rilascio non negativo
 * - Anno compreso tra 1900 e l'anno corrente (dinamico)
 */
public final class Model {

    private final Long id;
    private final String brand;
    private final String name;
    private final BigDecimal releasePrice;
    private final int releaseYear;

    private Model(Long id, String brand, String name, BigDecimal releasePrice, int releaseYear) {
        this.id = id;
        this.brand = brand;
        this.name = name;
        this.releasePrice = releasePrice;
        this.releaseYear = releaseYear;
    }

    public static Model create(Long id, String brand, String name, BigDecimal releasePrice, int releaseYear) {
        if (brand == null || brand.trim().isEmpty()) {
            throw new IllegalArgumentException("Il brand non può essere vuoto.");
        }
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Il nome del modello non può essere vuoto.");
        }
        if (releasePrice == null || releasePrice.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Il prezzo di listino non può essere negativo.");
        }
        int currentYear = Year.now().getValue();
        if (releaseYear < 1900 || releaseYear > currentYear) {
            throw new IllegalArgumentException("L'anno di rilascio deve essere compreso tra il 1900 e il " + currentYear);
        }
        return new Model(id, brand.trim(), name.trim(), releasePrice, releaseYear);
    }

    public Long getId() {
        return id;
    }

    public String getBrand() {
        return brand;
    }

    public String getName() {
        return name;
    }

    public BigDecimal getReleasePrice() {
        return releasePrice;
    }

    public int getReleaseYear() {
        return releaseYear;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Model model)) return false;
        if (id != null && model.id != null) {
            return Objects.equals(id, model.id);
        }
        if (id == null && model.id == null) {
            return Objects.equals(brand, model.brand) && Objects.equals(name, model.name);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return id != null ? Objects.hash(id) : Objects.hash(brand, name);
    }

    @Override
    public String toString() {
        return brand + " " + name + " (" + releaseYear + ")";
    }
}

