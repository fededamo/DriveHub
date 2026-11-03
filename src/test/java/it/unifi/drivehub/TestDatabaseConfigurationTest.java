package it.unifi.drivehub;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;

class TestDatabaseConfigurationTest {
    private static Map<String, String> validEnvironment() {
        return Map.of("DRIVEHUB_TEST_DB_URL", "jdbc:postgresql://localhost:5432/drivehub_test",
                "DRIVEHUB_TEST_DB_USER", "drivehub_test", "DRIVEHUB_TEST_DB_PASSWORD", "test-only");
    }

    @Test
    void explicitDedicatedConfigurationIsAcceptedWithoutConnecting() {
        assertDoesNotThrow(() -> TestDatabase.configured(validEnvironment(), new Properties()));
    }

    @Test
    void everySettingIsRequiredWithoutApplicationFallback() {
        for (String key : validEnvironment().keySet()) {
            Map<String, String> env = new HashMap<>(validEnvironment());
            env.remove(key);
            assertThrows(IllegalArgumentException.class, () -> TestDatabase.configured(env, new Properties()));
            env.put(key, " ");
            assertThrows(IllegalArgumentException.class, () -> TestDatabase.configured(env, new Properties()));
        }
    }

    @Test
    void applicationTargetsAndAmbiguousJdbcUrlsAreRejectedBeforeConnecting() {
        for (String url : new String[]{
                "jdbc:postgresql://localhost:5432/drivehub",
                "jdbc:postgresql:drivehub_test", "jdbc:postgresql://localhost/drivehub_test?user=drivehub",
                "jdbc:postgresql://localhost/drivehub_test?service=app", "jdbc:postgresql://localhost/drivehub_test/",
                "jdbc:postgresql://localhost,other/drivehub_test", "jdbc:postgresql://localhost/%64rivehub_test"}) {
            Map<String, String> env = new HashMap<>(validEnvironment());
            env.put("DRIVEHUB_TEST_DB_URL", url);
            assertThrows(IllegalArgumentException.class, () -> TestDatabase.configured(env, new Properties()));
        }
    }

    @Test
    void applicationAndPrivilegedRoleNamesAreRejectedBeforeConnecting() {
        for (String user : new String[]{"drivehub", "postgres", "other"}) {
            Map<String, String> env = new HashMap<>(validEnvironment());
            env.put("DRIVEHUB_TEST_DB_USER", user);
            assertThrows(IllegalArgumentException.class, () -> TestDatabase.configured(env, new Properties()));
        }
    }

    @Test
    void applicationCannotBeConfiguredToUseTheTestDatabaseOrRole() {
        for (Map.Entry<String, String> setting : Map.of(
                "db.url", "jdbc:postgresql://other-host:5433/drivehub_test",
                "db.user", "drivehub_test").entrySet()) {
            Properties app = new Properties();
            app.setProperty(setting.getKey(), setting.getValue());
            assertThrows(IllegalArgumentException.class, () -> TestDatabase.configured(validEnvironment(), app));
        }
    }

    @Test
    void applicationJdbcOverridesCannotUseTheTestDatabaseOrRole() {
        for (String url : new String[]{"jdbc:postgresql:drivehub_test",
                "jdbc:postgresql://localhost/%64rivehub_test",
                "jdbc:postgresql://localhost/drivehub?user=drivehub_test"}) {
            Properties app = new Properties();
            app.setProperty("db.url", url);
            assertThrows(IllegalArgumentException.class, () -> TestDatabase.configured(validEnvironment(), app));
        }
    }

    @Test
    void rejectionDoesNotExposePasswords() {
        Map<String, String> env = new HashMap<>(validEnvironment());
        env.put("DRIVEHUB_TEST_DB_URL", "jdbc:postgresql://localhost/drivehub?password=secret-in-url");
        var error = assertThrows(IllegalArgumentException.class,
                () -> TestDatabase.configured(env, new Properties()));
        assertFalse(error.getMessage().contains("secret-in-url"));
        assertFalse(error.getMessage().contains("test-only"));
    }
}
