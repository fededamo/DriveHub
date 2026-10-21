package it.unifi.ing.drivehub.dao.postgres;

import it.unifi.ing.drivehub.dao.interfaces.DaoFactory;
import it.unifi.ing.drivehub.dao.interfaces.UnitOfWork;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Objects;

/**
 * Opens transaction-scoped JDBC units of work.
 */
public final class PostgresDaoFactory implements DaoFactory {

    private final DataSource dataSource;

    public PostgresDaoFactory(DataSource dataSource) {
        this.dataSource = Objects.requireNonNull(dataSource, "dataSource");
    }

    public static PostgresDaoFactory fromEnvironment() {
        return new PostgresDaoFactory(new DriverManagerDataSource(DatabaseConfig.fromEnvironment()));
    }

    @Override
    public UnitOfWork begin() {
        try {
            Connection connection = dataSource.getConnection();
            try {
                connection.setAutoCommit(false);
                connection.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
                return new PostgresUnitOfWork(connection);
            } catch (SQLException failure) {
                try {
                    connection.close();
                } catch (SQLException closeFailure) {
                    failure.addSuppressed(closeFailure);
                }
                throw failure;
            }
        } catch (SQLException exception) {
            throw new DatabaseException("Could not begin unit of work", exception);
        }
    }
}
