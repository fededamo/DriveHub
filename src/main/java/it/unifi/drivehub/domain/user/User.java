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
}

