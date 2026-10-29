package it.unifi.drivehub.domain.discount;

/**
 * Classe base astratta per gli sconti di dominio (DRY).
 * Impone l'invariante di dominio: percentuale compresa rigorosamente tra 0 e 50.
 */
public abstract class Discount {

    protected final int value;

    protected Discount(int value) {
        if (value < 0 || value > 50) {
            throw new IllegalArgumentException("Lo sconto deve essere compreso tra 0 e 50. Valore inserito: " + value);
        }
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}

