package it.unifi.ing.drivehub.dao.postgres;

import it.unifi.ing.drivehub.dao.interfaces.UserDao;
import it.unifi.ing.drivehub.domain.users.Role;
import it.unifi.ing.drivehub.domain.users.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

final class PostgresUserDao extends AbstractPostgresDao implements UserDao {

    PostgresUserDao(Connection connection, JdbcEntityLoader loader) {
        super(connection, loader);
    }

    private static String tableName(Role role) {
        return switch (role) {
            case CLIENT, CUSTOMER -> "client";
            case SALESMAN -> "salesman";
            case MANAGER -> "manager";
        };
    }

    @Override
    public User save(User user) {
        if (user.id() != null) {
            throw new IllegalArgumentException("user is already persistent");
        }
        String table = tableName(user.role());

        long id = execute("Could not save user", () -> JdbcSupport.insert(connection,
                "INSERT INTO \"" + table + "\"(name, surname, email, hash_password) VALUES (?, ?, ?, ?)",
                statement -> {
                    statement.setString(1, user.name());
                    statement.setString(2, user.surname());
                    statement.setString(3, user.email());
                    statement.setString(4, user.passwordHash());
                }));
        user.assignId(id);
        return user;
    }

    @Override
    public void update(User user) {
        String table = tableName(user.role());

        execute("Could not update user", () -> JdbcSupport.updateExactlyOne(connection,
                "UPDATE \"" + table + "\" SET name = ?, surname = ?, email = ?, hash_password = ? WHERE id = ?",
                statement -> {
                    statement.setString(1, user.name());
                    statement.setString(2, user.surname());
                    statement.setString(3, user.email());
                    statement.setString(4, user.passwordHash());
                    statement.setLong(5, user.requireId());
                }));
    }

    @Override
    public void updateEmail(long id, Role role, String newEmail) {
        String table = tableName(role);
        execute("Could not update user email", () -> JdbcSupport.updateExactlyOne(connection,
                "UPDATE \"" + table + "\" SET email = ? WHERE id = ?",
                statement -> {
                    statement.setString(1, newEmail);
                    statement.setLong(2, id);
                }));
    }

    @Override
    public void updatePassword(long id, Role role, String newPasswordHash) {
        String table = tableName(role);
        execute("Could not update user password", () -> JdbcSupport.updateExactlyOne(connection,
                "UPDATE \"" + table + "\" SET hash_password = ? WHERE id = ?",
                statement -> {
                    statement.setString(1, newPasswordHash);
                    statement.setLong(2, id);
                }));
    }

    @Override
    public void delete(long id, Role role) {
        String table = tableName(role);
        execute("Could not delete user", () -> {
            try (PreparedStatement stmt = connection.prepareStatement(
                    "DELETE FROM \"" + table + "\" WHERE id = ?")) {
                stmt.setLong(1, id);
                stmt.executeUpdate();
                return null;
            }
        });
    }

    @Override
    public Optional<User> findById(long id) {
        return loader.user(id);
    }

    @Override
    public Optional<User> findByEmail(String normalizedEmail) {
        return execute("Could not find user by email", () -> {
            try (PreparedStatement stmt = connection.prepareStatement(
                    "SELECT id, name, surname, email, hash_password, role FROM \"user\" WHERE LOWER(email) = LOWER(?)")) {
                stmt.setString(1, normalizedEmail);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return Optional.of(new User(
                                rs.getLong("id"),
                                rs.getString("name"),
                                rs.getString("surname"),
                                rs.getString("email"),
                                rs.getString("hash_password"),
                                Role.fromString(rs.getString("role"))));
                    }
                    return Optional.empty();
                }
            }
        });
    }

    @Override
    public List<User> findByRole(Role role) {
        return execute("Could not find users by role", () -> {
            try (PreparedStatement stmt = connection.prepareStatement(
                    "SELECT id, name, surname, email, hash_password, role FROM \"user\" WHERE role = ?")) {
                stmt.setString(1, role.name());
                try (ResultSet rs = stmt.executeQuery()) {
                    List<User> users = new ArrayList<>();
                    while (rs.next()) {
                        users.add(new User(
                                rs.getLong("id"),
                                rs.getString("name"),
                                rs.getString("surname"),
                                rs.getString("email"),
                                rs.getString("hash_password"),
                                Role.fromString(rs.getString("role"))));
                    }
                    return List.copyOf(users);
                }
            }
        });
    }

    @Override
    public List<User> findAll() {
        return execute("Could not find all users", () -> {
            try (PreparedStatement stmt = connection.prepareStatement(
                    "SELECT id, name, surname, email, hash_password, role FROM \"user\"")) {
                try (ResultSet rs = stmt.executeQuery()) {
                    List<User> users = new ArrayList<>();
                    while (rs.next()) {
                        users.add(new User(
                                rs.getLong("id"),
                                rs.getString("name"),
                                rs.getString("surname"),
                                rs.getString("email"),
                                rs.getString("hash_password"),
                                Role.fromString(rs.getString("role"))));
                    }
                    return List.copyOf(users);
                }
            }
        });
    }

    @Override
    public boolean hasOperations(long userId, Role role) {
        return execute("Could not check operations for user", () -> {
            if (role == Role.CLIENT || role == Role.CUSTOMER) {
                String sql = "SELECT 1 FROM (" +
                        "SELECT client FROM \"test_drive\" WHERE client = ? UNION ALL " +
                        "SELECT client FROM \"rent\" WHERE client = ? UNION ALL " +
                        "SELECT client FROM \"sale\" WHERE client = ?" +
                        ") ops LIMIT 1";
                try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                    stmt.setLong(1, userId);
                    stmt.setLong(2, userId);
                    stmt.setLong(3, userId);
                    try (ResultSet rs = stmt.executeQuery()) {
                        return rs.next();
                    }
                }
            } else if (role == Role.SALESMAN) {
                String sql = "SELECT 1 FROM \"sale\" WHERE salesman = ? LIMIT 1";
                try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                    stmt.setLong(1, userId);
                    try (ResultSet rs = stmt.executeQuery()) {
                        return rs.next();
                    }
                }
            }
            return false;
        });
    }
}
