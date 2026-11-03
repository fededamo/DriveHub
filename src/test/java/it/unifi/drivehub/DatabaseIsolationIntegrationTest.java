package it.unifi.drivehub;

import it.unifi.drivehub.dao.DatabaseManager;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.util.HashMap;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;

@Tag("postgres")
class DatabaseIsolationIntegrationTest {
    @Test
    void applicationSentinelSurvivesRejectedConfigurationAndTestResets() throws Exception {
        var env = System.getenv();
        String appUrl = TestDatabase.required(env, "DRIVEHUB_SENTINEL_DB_URL");
        String appUser = TestDatabase.required(env, "DRIVEHUB_SENTINEL_DB_USER");
        String appPassword = TestDatabase.required(env, "DRIVEHUB_SENTINEL_DB_PASSWORD");
        Properties app = new Properties();
        app.setProperty("db.url", appUrl);
        app.setProperty("db.user", appUser);
        TestDatabase fixture = TestDatabase.fromEnvironment();
        TestDatabase.configured(env, app);

        // Connessione applicativa esplicita e di sola lettura: sentinella predisposta dall'operatore.
        try (Connection conn = new DatabaseManager(appUrl, appUser, appPassword).getConnection()) {
            conn.setReadOnly(true);
            assertSentinelPresent(conn);

            var unsafe = new HashMap<>(env);
            unsafe.put("DRIVEHUB_TEST_DB_URL", appUrl);
            assertThrows(IllegalArgumentException.class, () -> TestDatabase.configured(unsafe, app).reset());
            unsafe.put("DRIVEHUB_TEST_DB_URL", env.get("DRIVEHUB_TEST_DB_URL"));
            unsafe.put("DRIVEHUB_TEST_DB_USER", appUser);
            assertThrows(IllegalArgumentException.class, () -> TestDatabase.configured(unsafe, app).reset());
            assertSentinelPresent(conn);

            fixture.reset();
            fixture.reloadDemo();
            fixture.reset();
            assertSentinelPresent(conn);
        }
    }

    private static void assertSentinelPresent(Connection conn) throws Exception {
        try (var statement = conn.createStatement(); var result = statement.executeQuery("""
                SELECT current_database() <> 'drivehub_test'
                   AND EXISTS (SELECT 1 FROM public.brand WHERE name = 'R00_SENTINEL')
                """)) {
            assertTrue(result.next());
            assertTrue(result.getBoolean(1), "Serve un DB applicativo distinto con il marchio R00_SENTINEL");
        }
    }
}
