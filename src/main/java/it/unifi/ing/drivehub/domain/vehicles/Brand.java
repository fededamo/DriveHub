package it.unifi.ing.drivehub.domain.vehicles;

import java.util.Objects;

public final class Brand {
    private final String name;

    public Brand(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("brand name must not be blank");
        }
        this.name = name.trim();
    }

    public static Brand create(String name) {
        return new Brand(name);
    }

    public String name() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Brand brand)) return false;
        return Objects.equals(name.toLowerCase(java.util.Locale.ROOT),
                brand.name.toLowerCase(java.util.Locale.ROOT));
    }

    @Override
    public int hashCode() {
        return Objects.hash(name.toLowerCase(java.util.Locale.ROOT));
    }

    @Override
    public String toString() {
        return name;
    }
}
