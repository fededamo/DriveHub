package it.unifi.ing.drivehub.dao.postgres;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Objects;

/**
 * Small idempotent migration bootstrap used in lieu of a runtime migration dependency.
 */
public final class DatabaseBootstrap {

    private static final List<String> MIGRATIONS = List.of(
            "db/migration/init.sql"
    );
    private static final String DEMO_SEED = "db/demo.sql";

    private final JdbcTransactionManager transactions;
    private final boolean seedDemoData;

    public DatabaseBootstrap(DataSource dataSource, boolean seedDemoData) {
        this.transactions = new JdbcTransactionManager(Objects.requireNonNull(dataSource, "dataSource"));
        this.seedDemoData = seedDemoData;
    }

    public static DatabaseBootstrap fromEnvironment() {
        DatabaseConfig config = DatabaseConfig.fromEnvironment();
        return new DatabaseBootstrap(new DriverManagerDataSource(config), config.seedDemoData());
    }

    public void initialize() {
        transactions.inTransaction((JdbcTransactionManager.SqlAction) DatabaseBootstrap::createMigrationTable);
        for (String migration : MIGRATIONS) {
            transactions.inTransaction((JdbcTransactionManager.SqlAction)
                    connection -> applyIfNeeded(connection, migration));
        }
        if (seedDemoData) {
            transactions.inTransaction((JdbcTransactionManager.SqlAction)
                    connection -> SqlScriptRunner.run(connection, DEMO_SEED));
        }
    }

    private static void createMigrationTable(Connection connection) throws SQLException {
        try (var statement = connection.createStatement()) {
            statement.execute("""
                    CREATE TABLE IF NOT EXISTS drivehub_schema_migrations (
                        version VARCHAR(160) PRIMARY KEY,
                        applied_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
                    )
                    """);
        }
    }

    private static void applyIfNeeded(Connection connection, String migration) throws SQLException {
        if (isApplied(connection, migration)) {
            return;
        }
        SqlScriptRunner.run(connection, migration);
        try (PreparedStatement statement = connection.prepareStatement(
                "INSERT INTO drivehub_schema_migrations (version) VALUES (?)")) {
            statement.setString(1, migration);
            statement.executeUpdate();
        }
    }

    private static boolean isApplied(Connection connection, String migration) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT 1 FROM drivehub_schema_migrations WHERE version = ?")) {
            statement.setString(1, migration);
            try (ResultSet result = statement.executeQuery()) {
                return result.next();
            }
        }
    }
}
