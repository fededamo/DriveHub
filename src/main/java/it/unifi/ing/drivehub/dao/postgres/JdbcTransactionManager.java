package it.unifi.ing.drivehub.dao.postgres;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Objects;

/**
 * Opens one JDBC connection per unit of work and guarantees commit or rollback.
 */
public final class JdbcTransactionManager {

    private final DataSource dataSource;

    public JdbcTransactionManager(DataSource dataSource) {
        this.dataSource = Objects.requireNonNull(dataSource, "dataSource");
    }

    public <T> T inTransaction(SqlWork<T> work) {
        Objects.requireNonNull(work, "work");
        try (Connection connection = dataSource.getConnection()) {
            boolean previousAutoCommit = connection.getAutoCommit();
            int previousIsolation = connection.getTransactionIsolation();
            try {
                connection.setAutoCommit(false);
                connection.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
                T result = work.execute(connection);
                connection.commit();
                return result;
            } catch (Exception failure) {
                rollbackAfterFailure(connection, failure);
                if (failure instanceof DatabaseException databaseException) {
                    throw databaseException;
                }
                throw new DatabaseException("Database transaction failed", failure);
            } finally {
                restore(connection, previousAutoCommit, previousIsolation);
            }
        } catch (SQLException exception) {
            throw new DatabaseException("Could not open database transaction", exception);
        }
    }

    public void inTransaction(SqlAction action) {
        inTransaction(connection -> {
            action.execute(connection);
            return null;
        });
    }

    private static void rollbackAfterFailure(Connection connection, Exception failure) {
        try {
            connection.rollback();
        } catch (SQLException rollbackFailure) {
            failure.addSuppressed(rollbackFailure);
        }
    }

    private static void restore(Connection connection, boolean autoCommit, int isolation) {
        try {
            connection.setTransactionIsolation(isolation);
            connection.setAutoCommit(autoCommit);
        } catch (SQLException ignored) {
            // The connection is about to be closed; the original failure remains authoritative.
        }
    }

    @FunctionalInterface
    public interface SqlWork<T> {
        T execute(Connection connection) throws Exception;
    }

    @FunctionalInterface
    public interface SqlAction {
        void execute(Connection connection) throws Exception;
    }
}
