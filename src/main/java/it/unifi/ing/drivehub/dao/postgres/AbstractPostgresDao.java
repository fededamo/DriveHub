package it.unifi.ing.drivehub.dao.postgres;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

abstract class AbstractPostgresDao {

    protected final Connection connection;
    protected final JdbcEntityLoader loader;

    AbstractPostgresDao(Connection connection, JdbcEntityLoader loader) {
        this.connection = connection;
        this.loader = loader;
    }

    protected <T> T execute(String operation, SqlSupplier<T> work) {
        try {
            return work.get();
        } catch (SQLException exception) {
            throw new DatabaseException(operation, exception);
        }
    }

    protected void execute(String operation, SqlRunnable work) {
        execute(operation, () -> {
            work.run();
            return null;
        });
    }

    protected <T> Optional<T> findOne(
            String sql, JdbcSupport.Binder binder, EntityFinder<T> finder, String operation) {
        return execute(operation, () -> {
            var id = JdbcSupport.queryId(connection, sql, binder);
            return id.isPresent() ? finder.find(id.getAsLong()) : Optional.empty();
        });
    }

    protected <T> List<T> findMany(
            String sql, JdbcSupport.Binder binder, EntityFinder<T> finder, String operation) {
        return execute(operation, () -> JdbcSupport.queryIds(connection, sql, binder).stream()
                .map(id -> finder.find(id).orElseThrow(
                        () -> new DatabaseException("Row disappeared while executing " + operation, null)))
                .toList());
    }

    @FunctionalInterface
    protected interface SqlSupplier<T> {
        T get() throws SQLException;
    }

    @FunctionalInterface
    protected interface SqlRunnable {
        void run() throws SQLException;
    }

    @FunctionalInterface
    protected interface EntityFinder<T> {
        Optional<T> find(long id);
    }
}
