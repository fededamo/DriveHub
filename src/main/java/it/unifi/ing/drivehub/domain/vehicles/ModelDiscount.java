package it.unifi.ing.drivehub.domain.vehicles;

public final class ModelDiscount {
    private final long modelId;
    private final int value;

    public ModelDiscount(long modelId, int value) {
        if (modelId <= 0) {
            throw new IllegalArgumentException("modelId must be positive");
        }
        if (value < 0 || value > 50) {
            throw new IllegalArgumentException("discount value must be between 0 and 50");
        }
        this.modelId = modelId;
        this.value = value;
    }

    public long modelId() {
        return modelId;
    }

    public int value() {
        return value;
    }
}
