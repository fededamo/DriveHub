package it.unifi.drivehub.domain.vehicle;

import java.math.BigDecimal;
import java.time.Year;
import java.util.Objects;

/**
 * Entità Veicolo.
 * Invarianti:
 * - VIN univoco di 17 caratteri
 * - Prezzo >= 0
 * - Anno tra 1900 e anno attuale (dinamico)
 * - Se la targa è assente (null), i chilometri devono obbligatoriamente essere 0.
 * - Stato conforme a vehicle_state (SALE, RENT, BLOCKED, WAITING, SOLD)
 */
public final class Vehicle {

    private final Long id;
    private final Long modelId;
    private final String vin;
    private String licencePlate;
    private int kilometers;
    private BigDecimal price;
    private final int year;
    private VehicleState state;

    private Vehicle(Long id, Long modelId, String vin, String licencePlate, int kilometers, BigDecimal price, int year, VehicleState state) {
        this.id = id;
        this.modelId = modelId;
        this.vin = vin;
        this.licencePlate = licencePlate;
        this.kilometers = kilometers;
        this.price = price;
        this.year = year;
        this.state = state;
    }

    public static Vehicle create(Long id, Long modelId, String vin, String licencePlate, int kilometers, BigDecimal price, int year, VehicleState state) {
        if (modelId == null) {
            throw new IllegalArgumentException("L'ID del modello non può essere nullo.");
        }
        if (vin == null || vin.trim().isEmpty() || vin.trim().length() > 17) {
            throw new IllegalArgumentException("Il VIN non può essere vuoto e non può superare i 17 caratteri alfanumerici.");
        }
        if (kilometers < 0) {
            throw new IllegalArgumentException("I chilometri non possono essere negativi.");
        }
        String cleanPlate = (licencePlate != null && !licencePlate.trim().isEmpty()) ? licencePlate.trim().toUpperCase() : null;
        if (cleanPlate == null && kilometers != 0) {
            throw new IllegalArgumentException("Se un veicolo non ha targa, obbligatoriamente i chilometri devono essere 0.");
        }
        if (price == null || price.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Il prezzo del veicolo non può essere negativo.");
        }
        int currentYear = Year.now().getValue();
        if (year < 1900 || year > currentYear) {
            throw new IllegalArgumentException("L'anno di produzione deve essere compreso tra il 1900 e il " + currentYear);
        }
        if (state == null) {
            throw new IllegalArgumentException("Lo stato del veicolo non può essere nullo.");
        }
        return new Vehicle(id, modelId, vin.trim().toUpperCase(), cleanPlate, kilometers, price, year, state);
    }

    public Long getId() {
        return id;
    }

    public Long getModelId() {
        return modelId;
    }

    public String getVin() {
        return vin;
    }

    public String getLicencePlate() {
        return licencePlate;
    }

    public void setLicencePlate(String licencePlate) {
        this.licencePlate = (licencePlate != null && !licencePlate.trim().isEmpty()) ? licencePlate.trim().toUpperCase() : null;
        if (this.licencePlate == null && this.kilometers != 0) {
            throw new IllegalArgumentException("Se la targa viene rimossa, i chilometri devono essere impostati a 0.");
        }
    }

    public int getKilometers() {
        return kilometers;
    }

    public void setKilometers(int kilometers) {
        if (kilometers < 0) {
            throw new IllegalArgumentException("I chilometri non possono essere negativi.");
        }
        if (this.licencePlate == null && kilometers != 0) {
            throw new IllegalArgumentException("Impossibile impostare chilometri > 0 per un veicolo senza targa.");
        }
        this.kilometers = kilometers;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        if (price == null || price.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Il prezzo non può essere negativo.");
        }
        this.price = price;
    }

    public int getYear() {
        return year;
    }

    public VehicleState getState() {
        return state;
    }

    public void setState(VehicleState state) {
        if (state == null) {
            throw new IllegalArgumentException("Lo stato non può essere nullo.");
        }
        this.state = state;
    }

    public boolean isUsed() {
        return this.licencePlate != null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Vehicle vehicle)) return false;
        return Objects.equals(id, vehicle.id) || Objects.equals(vin, vehicle.vin);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, vin);
    }

    @Override
    public String toString() {
        return "Vehicle{" +
                "id=" + id +
                ", modelId=" + modelId +
                ", vin='" + vin + '\'' +
                ", licencePlate='" + licencePlate + '\'' +
                ", km=" + kilometers +
                ", price=" + price +
                ", state=" + state +
                '}';
    }
}

