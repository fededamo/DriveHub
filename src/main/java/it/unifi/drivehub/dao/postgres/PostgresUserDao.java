package it.unifi.drivehub.dao.postgres;

import it.unifi.drivehub.dao.DatabaseManager;
import it.unifi.drivehub.dao.UserDao;
import it.unifi.drivehub.domain.user.Client;
import it.unifi.drivehub.domain.user.Manager;
import it.unifi.drivehub.domain.user.Salesman;
import it.unifi.drivehub.domain.user.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

public final class PostgresUserDao implements UserDao {

    private final DatabaseManager db;

    public PostgresUserDao(DatabaseManager db) {
        this.db = db;
    }

    @Override
    public Optional<User> findByEmail(String email) {
        String sql = "SELECT id, name, surname, email, hash_password, role FROM public.\"user\" WHERE LOWER(email) = LOWER(?)";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    long id = rs.getLong("id");
                    String name = rs.getString("name");
                    String surname = rs.getString("surname");
                    String mail = rs.getString("email");
                    String hash = rs.getString("hash_password");
                    String role = rs.getString("role");

                    User user = switch (role.toUpperCase()) {
                        case "CLIENT" -> Client.createWithoutAccount(id, name, surname, mail, hash);
                        case "SALESMAN" -> Salesman.create(id, name, surname, mail, hash);
                        case "MANAGER" -> Manager.create(id, name, surname, mail, hash);
                        default -> throw new IllegalStateException("Ruolo utente non riconosciuto: " + role);
                    };
                    return Optional.of(user);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Errore durante la ricerca utente per email: " + email, e);
        }
        return Optional.empty();
    }

    @Override
    public Optional<User> findById(Long id) {
        String sql = "SELECT id, name, surname, email, hash_password, role FROM public.\"user\" WHERE id = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    long userId = rs.getLong("id");
                    String name = rs.getString("name");
                    String surname = rs.getString("surname");
                    String mail = rs.getString("email");
                    String hash = rs.getString("hash_password");
                    String role = rs.getString("role");

                    User user = switch (role.toUpperCase()) {
                        case "CLIENT" -> Client.createWithoutAccount(userId, name, surname, mail, hash);
                        case "SALESMAN" -> Salesman.create(userId, name, surname, mail, hash);
                        case "MANAGER" -> Manager.create(userId, name, surname, mail, hash);
                        default -> throw new IllegalStateException("Ruolo utente non riconosciuto: " + role);
                    };
                    return Optional.of(user);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Errore durante la ricerca utente per id: " + id, e);
        }
        return Optional.empty();
    }

    @Override
    public Optional<Client> findClientById(Long id) {
        String sql = "SELECT id, name, surname, email, hash_password FROM client WHERE id = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(Client.createWithoutAccount(
                            rs.getLong("id"),
                            rs.getString("name"),
                            rs.getString("surname"),
                            rs.getString("email"),
                            rs.getString("hash_password")
                    ));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Errore durante la ricerca del cliente id: " + id, e);
        }
        return Optional.empty();
    }

    @Override
    public Optional<Salesman> findSalesmanById(Long id) {
        String sql = "SELECT id, name, surname, email, hash_password FROM salesman WHERE id = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(Salesman.create(
                            rs.getLong("id"),
                            rs.getString("name"),
                            rs.getString("surname"),
                            rs.getString("email"),
                            rs.getString("hash_password")
                    ));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Errore durante la ricerca del venditore id: " + id, e);
        }
        return Optional.empty();
    }

    @Override
    public Optional<Manager> findManagerById(Long id) {
        String sql = "SELECT id, name, surname, email, hash_password FROM manager WHERE id = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(Manager.create(
                            rs.getLong("id"),
                            rs.getString("name"),
                            rs.getString("surname"),
                            rs.getString("email"),
                            rs.getString("hash_password")
                    ));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Errore durante la ricerca del manager id: " + id, e);
        }
        return Optional.empty();
    }

    @Override
    public Client saveClient(Client client, Connection conn) {
        String sql = "INSERT INTO client (name, surname, email, hash_password) VALUES (?, ?, ?, ?) RETURNING id";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, client.getName());
            ps.setString(2, client.getSurname());
            ps.setString(3, client.getEmail());
            ps.setString(4, client.getHashPassword());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    long generatedId = rs.getLong(1);
                    return Client.create(generatedId, client.getName(), client.getSurname(), client.getEmail(), client.getHashPassword(), client.getBankAccount());
                }
                throw new SQLException("Inserimento client non ha restituito l'ID generato.");
            }
        } catch (SQLException e) {
            throw new RuntimeException("Errore durante il salvataggio del cliente.", e);
        }
    }

    @Override
    public void updateEmail(Long id, String role, String newEmail, Connection conn) {
        String table = resolveTable(role);
        String sql = "UPDATE " + table + " SET email = ? WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, newEmail);
            ps.setLong(2, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Errore durante l'aggiornamento dell'email.", e);
        }
    }

    @Override
    public void updatePassword(Long id, String role, String newHashPassword, Connection conn) {
        String table = resolveTable(role);
        String sql = "UPDATE " + table + " SET hash_password = ? WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, newHashPassword);
            ps.setLong(2, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Errore durante l'aggiornamento della password.", e);
        }
    }

    @Override
    public void deleteUser(Long id, String role, Connection conn) {
        String table = resolveTable(role);
        String sql = "DELETE FROM " + table + " WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Errore durante la cancellazione dell'utente.", e);
        }
    }

    @Override
    public boolean hasAssociatedOperations(Long userId, String role) {
        String checkSql;
        if ("CLIENT".equalsIgnoreCase(role)) {
            checkSql = "SELECT (" +
                    "EXISTS(SELECT 1 FROM test_drive WHERE client = ?) OR " +
                    "EXISTS(SELECT 1 FROM rent WHERE client = ?) OR " +
                    "EXISTS(SELECT 1 FROM sale WHERE client = ?)" +
                    ") AS has_ops";
        } else if ("SALESMAN".equalsIgnoreCase(role)) {
            checkSql = "SELECT (" +
                    "EXISTS(SELECT 1 FROM request WHERE salesman = ?) OR " +
                    "EXISTS(SELECT 1 FROM sale WHERE salesman = ?)" +
                    ") AS has_ops";
        } else {
            return false;
        }

        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(checkSql)) {
            if ("CLIENT".equalsIgnoreCase(role)) {
                ps.setLong(1, userId);
                ps.setLong(2, userId);
                ps.setLong(3, userId);
            } else {
                ps.setLong(1, userId);
                ps.setLong(2, userId);
            }
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getBoolean("has_ops");
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Errore durante la verifica delle operazioni associate all'utente.", e);
        }
        return false;
    }

    private static String resolveTable(String role) {
        return switch (role.toUpperCase()) {
            case "CLIENT", "CLIENTE" -> "client";
            case "SALESMAN", "VENDITORE" -> "salesman";
            case "MANAGER" -> "manager";
            default -> throw new IllegalArgumentException("Ruolo non valido: " + role);
        };
    }
}

