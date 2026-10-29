package it.unifi.drivehub.domain.user;

/**
 * Interfaccia comune per gli utenti del sistema.
 * Conforme ai requisiti di progetto: l'astrazione è realizzata tramite interfaccia
 * e polimorfismo, evitando l'uso di un'enumerazione per distinguere i ruoli.
 */
public interface User {

    Long getId();

    String getName();

    String getSurname();

    String getEmail();

    String getHashPassword();

    String getRoleDisplayName();

    void updateEmail(String newEmail);

    void updateHashPassword(String newHashPassword);

    /**
     * Dispatch polimorfico (Double Dispatch / Visitor GoF) per navigare alla dashboard
     * specifica dell'utente senza violare il principio di incapsulamento né usare instanceof.
     */
    void openDashboard(UserDashboardNavigator navigator);
}
