package it.unifi.drivehub.domain.user;

/**
 * Interfaccia Visitor / Double Dispatch per la navigazione polimorfica delle dashboard.
 * Permette alla UI di mostrare la vista adatta al tipo di utente senza ricorrere a cascate di instanceof.
 */
public interface UserDashboardNavigator {

    void showClientDashboard(Client client);

    void showSalesmanDashboard(Salesman salesman);

    void showManagerDashboard(Manager manager);
}

