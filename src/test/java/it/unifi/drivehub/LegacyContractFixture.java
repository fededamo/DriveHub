package it.unifi.drivehub;

import it.unifi.drivehub.dao.DatabaseManager;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;

/** Fotografia controllata del contratto 841d4bc, caricata solo dal loader isolato. */
final class LegacyContractFixture {
    static DatabaseManager reset() throws Exception {
        return TestDatabase.fromEnvironment().reset("/db/pre-semantics-841d4bc-schema.sql",
                "/db/pre-semantics-fixture.sql");
    }

    static void convert(DatabaseManager db) throws Exception {
        convert(db, script());
    }

    static String script() throws Exception {
        try (var in = LegacyContractFixture.class.getResourceAsStream("/db/convert-operation-semantics.sql")) {
            if (in == null) throw new IllegalStateException("Conversione SQL mancante");
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    static void convert(DatabaseManager db, String script) throws Exception {
        try (var conn = db.getConnection(); var statement = conn.createStatement()) {
            // La chiusura JDBC può precedere di pochi ms l'uscita del backend precedente.
            for (int attempt = 0; attempt < 100; attempt++) {
                try (var result = statement.executeQuery("SELECT count(*) FROM pg_stat_activity WHERE datname=current_database()")) {
                    result.next();
                    if (result.getInt(1) == 1) break;
                }
                Thread.sleep(10);
            }
            statement.execute(script);
        }
    }

    static void sql(DatabaseManager db, String text) throws SQLException {
        try (var conn = db.getConnection(); var statement = conn.createStatement()) {
            statement.execute(text);
        }
    }
}
