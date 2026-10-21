package it.unifi.ing.drivehub.support;

import it.unifi.ing.drivehub.business.banking.IbanGenerator;
import it.unifi.ing.drivehub.domain.users.BankAccount;
import it.unifi.ing.drivehub.domain.users.Role;
import it.unifi.ing.drivehub.domain.users.User;
import it.unifi.ing.drivehub.domain.vehicles.Brand;
import it.unifi.ing.drivehub.domain.vehicles.Vehicle;
import it.unifi.ing.drivehub.domain.vehicles.VehicleModel;
import it.unifi.ing.drivehub.domain.vehicles.VehicleState;

import java.math.BigDecimal;

public final class TestFixtures {
    private TestFixtures() { }

    public static User customer(long id) {
        return new User(id, "Maria", "Rossi", "maria" + id + "@test.it",
                "encoded-password", Role.CLIENT);
    }

    public static User salesman(long id) {
        return new User(id, "Luigi", "Verdi", "luigi" + id + "@test.it",
                "encoded-password", Role.SALESMAN);
    }

    public static User manager(long id) {
        return new User(id, "Luca", "Bianchi", "luca" + id + "@test.it",
                "encoded-password", Role.MANAGER);
    }

    public static User user(long id, String name, String surname, String email, Role role) {
        return new User(id, name, surname, email, "encoded-password", role);
    }

    public static Brand brand() {
        return new Brand("Alfa Romeo");
    }

    public static VehicleModel model() {
        return new VehicleModel(20L, "Alfa Romeo", "Giulia", new BigDecimal("40000.00"), 2023);
    }

    public static Vehicle saleVehicle(long id, String plate) {
        return new Vehicle(id, 20L, "VIN" + id, plate, 12_000, new BigDecimal("40000.00"), 2023, VehicleState.SALE);
    }

    public static Vehicle rentalVehicle(long id, String plate) {
        return new Vehicle(id, 20L, "VIN" + id, plate, 22_000, new BigDecimal("100.00"), 2022, VehicleState.RENT);
    }

    public static Vehicle waitingVehicle(long id, String plate) {
        return new Vehicle(id, 20L, "VIN" + id, plate, 90_000, new BigDecimal("15000.00"), 2020, VehicleState.WAITING);
    }

    public static BankAccount bankAccount(long clientId, BigDecimal balance) {
        return new BankAccount(IbanGenerator.generate(), clientId, balance);
    }
}
