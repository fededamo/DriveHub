package it.unifi.drivehub.dao;

import it.unifi.drivehub.domain.user.BankAccount;

import java.math.BigDecimal;
import java.sql.Connection;
import java.util.Optional;

/**
 * Interfaccia DAO per il conto bancario del cliente.
 */
public interface BankAccountDao {

    Optional<BankAccount> findByClientId(Long clientId);

    default Optional<BankAccount> findByClientId(Long clientId, Connection conn) {
        return findByClientId(clientId);
    }

    Optional<BankAccount> findByIban(String iban);

    void save(BankAccount account, Connection conn);

    void updateBalance(String iban, BigDecimal newBalance, Connection conn);
}

