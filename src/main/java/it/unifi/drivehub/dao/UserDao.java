package it.unifi.drivehub.dao;

import it.unifi.drivehub.domain.user.Client;
import it.unifi.drivehub.domain.user.Manager;
import it.unifi.drivehub.domain.user.Salesman;
import it.unifi.drivehub.domain.user.User;

import java.sql.Connection;
import java.util.Optional;

/**
 * Interfaccia DAO per la gestione dell'anagrafica utenti.
 */
public interface UserDao {

    Optional<User> findByEmail(String email);

    Optional<User> findById(Long id);

    Optional<Client> findClientById(Long id);

    Optional<Salesman> findSalesmanById(Long id);

    Optional<Manager> findManagerById(Long id);

    Client saveClient(Client client, Connection conn);

    void updateEmail(Long id, String role, String newEmail, Connection conn);

    void updatePassword(Long id, String role, String newHashPassword, Connection conn);

    void deleteUser(Long id, String role, Connection conn);

    boolean hasAssociatedOperations(Long userId, String role);
}

