package it.unifi.drivehub.domain.user;

/**
 * Entità Cliente.
 * Estende AbstractUser e detiene un conto bancario (BankAccount).
 */
public final class Client extends AbstractUser {

    private BankAccount bankAccount;

    private Client(Long id, String name, String surname, String email, String hashPassword, BankAccount bankAccount) {
        super(id, name, surname, email, hashPassword);
        this.bankAccount = bankAccount;
    }

    /**
     * Factory Method per Client.
     */
    public static Client create(Long id, String name, String surname, String email, String hashPassword, BankAccount bankAccount) {
        return new Client(id, name, surname, email, hashPassword, bankAccount);
    }

    public static Client createWithoutAccount(Long id, String name, String surname, String email, String hashPassword) {
        return create(id, name, surname, email, hashPassword, null);
    }

    @Override
    public String getRoleDisplayName() {
        return "Cliente";
    }

    @Override
    public void openDashboard(UserDashboardNavigator navigator) {
        navigator.showClientDashboard(this);
    }

    public BankAccount getBankAccount() {
        return bankAccount;
    }

    public void setBankAccount(BankAccount bankAccount) {
        this.bankAccount = bankAccount;
    }

    @Override
    public String toString() {
        return "Client{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", surname='" + surname + '\'' +
                ", email='" + email + '\'' +
                '}';
    }
}
