package it.unifi.drivehub;

import it.unifi.drivehub.dao.DatabaseManager;
import org.postgresql.Driver;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;
import java.util.Properties;

/** Unico ingresso ai dati sacrificabili: nessun fallback alla configurazione applicativa. */
final class TestDatabase {
    private static final String TEST_NAME = "drivehub_test";
    private final DatabaseManager database;

    private TestDatabase(DatabaseManager database) {
        this.database = database;
    }

    static TestDatabase fromEnvironment() throws IOException {
        Properties app = new Properties();
        try (InputStream in = TestDatabase.class.getResourceAsStream("/application.properties")) {
            if (in != null) app.load(in);
        }
        return configured(System.getenv(), app);
    }

    static TestDatabase configured(Map<String, String> env, Properties app) {
        String url = required(env, "DRIVEHUB_TEST_DB_URL");
        String user = required(env, "DRIVEHUB_TEST_DB_USER");
        String password = required(env, "DRIVEHUB_TEST_DB_PASSWORD");
        // Escludere parametri JDBC, service, host multipli e nomi codificati evita override del target.
        if (!url.matches("jdbc:postgresql://[A-Za-z0-9.-]+(?::[0-9]{1,5})?/drivehub_test")
                || !TEST_NAME.equals(user)) {
            throw new IllegalArgumentException("I test richiedono database e ruolo dedicati drivehub_test, URL senza parametri.");
        }
        Properties appUrl = Driver.parseURL(app.getProperty("db.url", "jdbc:postgresql://localhost:5432/drivehub"), null);
        if (appUrl == null || TEST_NAME.equals(appUrl.getProperty("PGDBNAME"))
                || TEST_NAME.equals(appUrl.getProperty("user"))
                || TEST_NAME.equals(app.getProperty("db.user", "drivehub"))) {
            throw new IllegalArgumentException("La configurazione applicativa deve essere distinta da database e ruolo di test.");
        }
        return new TestDatabase(new DatabaseManager(url, user, password));
    }

    static String required(Map<String, String> env, String key) {
        String value = env.get(key);
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Impostare esplicitamente " + key + "; nessun test PostgreSQL è stato eseguito.");
        }
        return value;
    }

    DatabaseManager reset() throws IOException, SQLException {
        executeScripts("/db/schema.sql", "/db/demo.sql");
        return database;
    }

    void reloadDemo() throws IOException, SQLException {
        executeScripts("/db/demo.sql");
    }

    private void executeScripts(String... resources) throws IOException, SQLException {
        try (Connection conn = database.getConnection()) {
            verifyDestination(conn);
            conn.setAutoCommit(false);
            try (var statement = conn.createStatement()) {
                for (String resource : resources) {
                    try (InputStream in = TestDatabase.class.getResourceAsStream(resource)) {
                        if (in == null) throw new IOException("Risorsa SQL mancante: " + resource);
                        // PostgreSQL gestisce l'intero script, inclusi i corpi delle funzioni $$...$$.
                        statement.execute(new String(in.readAllBytes(), StandardCharsets.UTF_8));
                    }
                }
                conn.commit();
            } catch (IOException | SQLException | RuntimeException e) {
                try {
                    conn.rollback();
                } catch (SQLException rollbackError) {
                    e.addSuppressed(rollbackError);
                }
                throw e;
            }
        }
    }

    private static void verifyDestination(Connection conn) throws SQLException {
        try (var statement = conn.createStatement(); var result = statement.executeQuery("""
                SELECT current_database() = 'drivehub_test'
                   AND current_user = 'drivehub_test'
                   AND current_schema() = 'public'
                   AND pg_get_userbyid(d.datdba) = current_user
                   AND NOT (r.rolsuper OR r.rolcreatedb OR r.rolcreaterole OR r.rolreplication OR r.rolbypassrls)
                   AND NOT EXISTS (SELECT 1 FROM pg_auth_members WHERE member = r.oid)
                   AND NOT EXISTS (SELECT 1 FROM pg_database other
                                   WHERE other.datdba = r.oid AND other.datname <> 'drivehub_test')
                FROM pg_database d JOIN pg_roles r ON r.rolname = current_user
                WHERE d.datname = current_database()
                """)) {
            if (!result.next() || !result.getBoolean(1)) {
                throw new IllegalArgumentException("Reset rifiutato: identità, proprietario o privilegi del PostgreSQL di test non sicuri.");
            }
        }
    }
}
