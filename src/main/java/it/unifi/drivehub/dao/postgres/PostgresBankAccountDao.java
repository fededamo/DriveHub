package it.unifi.drivehub.dao.postgres;

import it.unifi.drivehub.dao.BankAccountDao;
import it.unifi.drivehub.dao.DatabaseManager;
import it.unifi.drivehub.domain.user.BankAccount;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

public final class PostgresBankAccountDao implements BankAccountDao {

    private final DatabaseManager db;

    public PostgresBankAccountDao(DatabaseManager db) {
        this.db = db;
    }

    @Override
    public Optional<BankAccount> findByClientId(Long clientId) {
        try (Connection conn = db.getConnection()) {
            return findByClientId(clientId, conn);
        } catch (SQLException e) {
            throw new RuntimeException("Errore di connessione durante la ricerca del conto bancario per cliente: " + clientId, e);
        }
    }

    @Override
    public Optional<BankAccount> findByClientId(Long clientId, Connection conn) {
        String sql = "SELECT iban, client, balance FROM bank_account WHERE client = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, clientId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(BankAccount.create(
                            rs.getString("iban"),
                            rs.getLong("client"),
                            rs.getBigDecimal("balance")
                    ));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Errore durante la ricerca del conto bancario per cliente: " + clientId, e);
        }
        return Optional.empty();
    }

    @Override
    public Optional<BankAccount> findByIban(String iban) {
        String sql = "SELECT iban, client, balance FROM bank_account WHERE UPPER(iban) = UPPER(?)";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, iban.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(BankAccount.create(
                            rs.getString("iban"),
                            rs.getLong("client"),
                            rs.getBigDecimal("balance")
                    ));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Errore durante la ricerca del conto bancario per IBAN: " + iban, e);
        }
        return Optional.empty();
    }

    @Override
    public void save(BankAccount account, Connection conn) {
        String sql = "INSERT INTO bank_account (iban, client, balance) VALUES (?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, account.getIban());
            ps.setLong(2, account.getClientId());
            ps.setBigDecimal(3, account.getBalance());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Errore durante il salvataggio del conto bancario.", e);
        }
    }

    @Override
    public void updateBalance(String iban, BigDecimal newBalance, Connection conn) {
        String sql = "UPDATE bank_account SET balance = ? WHERE iban = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setBigDecimal(1, newBalance);
            ps.setString(2, iban);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Errore durante l'aggiornamento del saldo del conto: " + iban, e);
        }
    }
}

