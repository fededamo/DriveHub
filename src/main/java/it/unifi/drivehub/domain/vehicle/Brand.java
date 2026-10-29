package it.unifi.drivehub.domain.vehicle;

import java.util.Objects;

/**
 * Entità Brand (Marca di veicoli).
 */
public final class Brand {

    private final String name;

    private Brand(String name) {
        this.name = name;
    }

    public static Brand create(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Il nome del brand non può essere vuoto.");
        }
        return new Brand(name.trim());
    }

    public String getName() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Brand brand)) return false;
        return Objects.equals(name, brand.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    @Override
    public String toString() {
        return name;
    }
}

