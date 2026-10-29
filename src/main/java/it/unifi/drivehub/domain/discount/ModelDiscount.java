package it.unifi.drivehub.domain.discount;

import java.util.Objects;

/**
 * Entità Sconto Modello.
 * Invariante: percentuale compresa tra 0 e 50 (ereditata da Discount).
 */
public final class ModelDiscount extends Discount {

    private final Long modelId;

    private ModelDiscount(Long modelId, int value) {
        super(value);
        this.modelId = modelId;
    }

    public static ModelDiscount create(Long modelId, int value) {
        if (modelId == null) {
            throw new IllegalArgumentException("L'ID del modello non può essere nullo.");
        }
        return new ModelDiscount(modelId, value);
    }

    public Long getModelId() {
        return modelId;
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
