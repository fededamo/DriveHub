package it.unifi.ing.drivehub.dao.postgres;

import it.unifi.ing.drivehub.dao.interfaces.BankAccountDao;
import it.unifi.ing.drivehub.domain.users.BankAccount;

import java.math.BigDecimal;
import java.sql.Connection;
import java.util.Optional;

final class PostgresBankAccountDao extends AbstractPostgresDao implements BankAccountDao {

    PostgresBankAccountDao(Connection connection, JdbcEntityLoader loader) {
        super(connection, loader);
    }

    @Override
    public BankAccount save(BankAccount account) {
        execute("Could not save bank account", () -> {
            try (var stmt = connection.prepareStatement(
                    "INSERT INTO \"bank_account\" (iban, client, balance) VALUES (?, ?, ?)")) {
                stmt.setString(1, account.iban());
                stmt.setLong(2, account.clientId());
                stmt.setBigDecimal(3, account.balance());
                stmt.executeUpdate();
                return null;
            }
        });
        return account;
    }

    @Override
    public void update(BankAccount account) {
        updateBalance(account.iban(), account.balance());
    }

    @Override
    public void updateBalance(String iban, BigDecimal newBalance) {
        execute("Could not update bank account balance", () -> {
            try (var stmt = connection.prepareStatement(
                    "UPDATE \"bank_account\" SET balance = ? WHERE iban = ?")) {
                stmt.setBigDecimal(1, newBalance);
                stmt.setString(2, iban);
                stmt.executeUpdate();
                return null;
            }
        });
    }

    @Override
    public Optional<BankAccount> findByIban(String iban) {
        return loader.bankAccount(iban);
    }

    @Override
    public Optional<BankAccount> findByIbanForUpdate(String iban) {
        return execute("Could not find bank account for update", () -> {
            try (var stmt = connection.prepareStatement(
                    "SELECT iban, client, balance FROM \"bank_account\" WHERE iban = ? FOR UPDATE")) {
                stmt.setString(1, iban);
                try (var rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return Optional.of(new BankAccount(
                                rs.getString("iban"),
                                rs.getLong("client"),
                                rs.getBigDecimal("balance")));
                    }
                    return Optional.empty();
                }
            }
        });
    }

    @Override
    public Optional<BankAccount> findByClientId(long clientId) {
        return loader.bankAccountByClient(clientId);
    }
}
