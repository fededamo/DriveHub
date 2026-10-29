package it.unifi.drivehub.domain.discount;

import java.util.Objects;

/**
 * Entità Sconto Modello.
 * Invariante: percentuale compresa tra 0 e 50.
 */
public final class ModelDiscount {

    private final Long modelId;
    private final int value;

    private ModelDiscount(Long modelId, int value) {
        this.modelId = modelId;
        this.value = value;
    }

    public static ModelDiscount create(Long modelId, int value) {
        if (modelId == null) {
            throw new IllegalArgumentException("L'ID del modello non può essere nullo.");
        }
        if (value < 0 || value > 50) {
            throw new IllegalArgumentException("Lo sconto deve essere compreso tra 0 e 50. Valore inserito: " + value);
        }
        return new ModelDiscount(modelId, value);
    }

    public Long getModelId() {
        return modelId;
    }

    public int getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ModelDiscount that)) return false;
        return Objects.equals(modelId, that.modelId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(modelId);
    }

    @Override
    public String toString() {
        return "ModelDiscount{" + "modelId=" + modelId + ", value=" + value + "%}";
    }
}

