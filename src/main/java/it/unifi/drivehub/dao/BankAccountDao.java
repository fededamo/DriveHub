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

    /**
     * Legge e blocca il conto fino a commit/rollback sulla connessione transazionale ricevuta.
     * Prima di modificare un saldo usare questa lettura; per più conti acquisire i lock
     * in ordine crescente di clientId, senza aprire altre transazioni.
     */
    Optional<BankAccount> findByClientIdForUpdate(Long clientId, Connection conn);

    Optional<BankAccount> findByIban(String iban);

    void save(BankAccount account, Connection conn);

    void updateBalance(String iban, BigDecimal newBalance, Connection conn);
}

