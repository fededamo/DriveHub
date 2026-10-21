package it.unifi.ing.drivehub.dao.postgres;

import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DatabaseBootstrapIntegrationTest extends PostgresDaoIntegrationSupport {

    @Test
    void bootstrapIsIdempotentAndTracksMigration() throws SQLException {
        DatabaseBootstrap bootstrap = new DatabaseBootstrap(dataSource, false);

        bootstrap.initialize();

        assertEquals(1, scalarLong("SELECT COUNT(*) FROM drivehub_schema_migrations"));
        assertEquals(13, scalarLong("""
                SELECT COUNT(*)
                FROM information_schema.tables
                WHERE table_schema = 'public'
                  AND table_name IN (
                    'brand', 'model', 'vehicle', 'client', 'salesman', 'manager',
                    'bank_account', 'request', 'sale', 'rent', 'test_drive',
                    'model_discount', 'vehicle_discount'
                  )
                """));
    }

    @Test
    void optionalDemoSeedPopulatesCatalogAndIsIdempotent() throws SQLException {
        JdbcDataSource h2 = new JdbcDataSource();
        h2.setURL("jdbc:h2:mem:seed_" + UUID.randomUUID().toString().replace("-", "")
                + ";MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1");
        h2.setUser("sa");
        h2.setPassword("");
        DatabaseBootstrap bootstrap = new DatabaseBootstrap(h2, true);

        bootstrap.initialize();
        bootstrap.initialize();

        try (Connection connection = h2.getConnection()) {
            assertTrue(scalarLong(connection, "SELECT COUNT(*) FROM brand") >= 2);
            assertTrue(scalarLong(connection, "SELECT COUNT(*) FROM model") >= 2);
            assertTrue(scalarLong(connection, "SELECT COUNT(*) FROM vehicle") >= 2);
        }
    }

    @Test
    void resetSqlLoadsAllEntitiesWithExpectedCounts() throws SQLException {
        JdbcDataSource h2 = new JdbcDataSource();
        h2.setURL("jdbc:h2:mem:reset_" + UUID.randomUUID().toString().replace("-", "")
                + ";MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1");
        h2.setUser("sa");
        h2.setPassword("");

        DatabaseBootstrap bootstrap = new DatabaseBootstrap(h2, false);
        bootstrap.initialize();

        try (Connection connection = h2.getConnection()) {
            SqlScriptRunner.run(connection, "db/demo.sql");

            assertEquals(5, scalarLong(connection, "SELECT COUNT(*) FROM \"brand\""));
            assertEquals(20, scalarLong(connection, "SELECT COUNT(*) FROM \"model\""));
            assertEquals(56, scalarLong(connection, "SELECT COUNT(*) FROM \"vehicle\""));
            assertEquals(2, scalarLong(connection, "SELECT COUNT(*) FROM \"model_discount\""));
            assertEquals(5, scalarLong(connection, "SELECT COUNT(*) FROM \"vehicle_discount\""));
            assertEquals(15, scalarLong(connection, "SELECT COUNT(*) FROM \"client\""));
            assertEquals(15, scalarLong(connection, "SELECT COUNT(*) FROM \"bank_account\""));
            assertEquals(4, scalarLong(connection, "SELECT COUNT(*) FROM \"salesman\""));
            assertEquals(1, scalarLong(connection, "SELECT COUNT(*) FROM \"manager\""));
            assertEquals(10, scalarLong(connection, "SELECT COUNT(*) FROM \"request\""));
            assertEquals(4, scalarLong(connection, "SELECT COUNT(*) FROM \"vehicle\" WHERE \"state\" = 'WAITING'"));
            assertEquals(10, scalarLong(connection, "SELECT COUNT(*) FROM \"test_drive\""));
            assertEquals(10, scalarLong(connection, "SELECT COUNT(*) FROM \"rent\""));
            assertEquals(40, scalarLong(connection, "SELECT COUNT(*) FROM \"sale\""));
            assertEquals(8, scalarLong(connection, "SELECT COUNT(*) FROM \"sale\" WHERE \"vehicle\" IS NULL AND \"salesman\" IS NULL"));
        }
    }

    private long scalarLong(String sql) throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            return scalarLong(connection, sql);
        }
    }

    private static long scalarLong(Connection connection, String sql) throws SQLException {
        try (var statement = connection.createStatement(); var result = statement.executeQuery(sql)) {
            result.next();
            return result.getLong(1);
        }
    }
}
