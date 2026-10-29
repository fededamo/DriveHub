package it.unifi.drivehub.domain.user;

/**
 * Entità Manager (Dirigente).
 * Estende AbstractUser con privilegi amministrativi su catalogo e sconti.
 */
public final class Manager extends AbstractUser {

    private Manager(Long id, String name, String surname, String email, String hashPassword) {
        super(id, name, surname, email, hashPassword);
    }

    public static Manager create(Long id, String name, String surname, String email, String hashPassword) {
        return new Manager(id, name, surname, email, hashPassword);
    }

    @Override
    public String getRoleDisplayName() {
        return "Manager";
    }

    @Override
    public void openDashboard(UserDashboardNavigator navigator) {
        navigator.showManagerDashboard(this);
    }

    @Override
    public String toString() {
        return "Manager{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", surname='" + surname + '\'' +
                ", email='" + email + '\'' +
                '}';
    }
}
