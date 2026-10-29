package it.unifi.drivehub.domain.user;

/**
 * Entità Venditore (Salesman).
 * Estende AbstractUser per la gestione del ciclo di vendita e noleggio.
 */
public final class Salesman extends AbstractUser {

    private Salesman(Long id, String name, String surname, String email, String hashPassword) {
        super(id, name, surname, email, hashPassword);
    }

    public static Salesman create(Long id, String name, String surname, String email, String hashPassword) {
        return new Salesman(id, name, surname, email, hashPassword);
    }

    @Override
    public String getRoleDisplayName() {
        return "Venditore";
    }

    @Override
    public void openDashboard(UserDashboardNavigator navigator) {
        navigator.showSalesmanDashboard(this);
    }

    @Override
    public String toString() {
        return "Salesman{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", surname='" + surname + '\'' +
                ", email='" + email + '\'' +
                '}';
    }
}
