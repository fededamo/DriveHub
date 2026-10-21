package it.unifi.ing.drivehub.dao.interfaces;

import it.unifi.ing.drivehub.domain.users.BankAccount;

import java.math.BigDecimal;
import java.util.Optional;

public interface BankAccountDao {
    BankAccount save(BankAccount account);
    void update(BankAccount account);
    void updateBalance(String iban, BigDecimal newBalance);
    Optional<BankAccount> findByIban(String iban);
    Optional<BankAccount> findByIbanForUpdate(String iban);
    Optional<BankAccount> findByClientId(long clientId);
}
