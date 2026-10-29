package it.unifi.drivehub;

import it.unifi.drivehub.dao.DatabaseManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@DisplayName("Validazione esecuzione demo.sql su schema.sql")
class DemoSqlValidationTest {

    @Test
    @DisplayName("Verifica che demo.sql si esegua perfettamente senza errori SQL")
    void testExecuteDemoSql() {
        assertDoesNotThrow(() -> {
            DatabaseManager db = new DatabaseManager();
            String script;
            try (InputStream in = getClass().getClassLoader().getResourceAsStream("db/demo.sql")) {
                if (in == null) throw new IllegalStateException("db/demo.sql non trovato");
                script = new String(in.readAllBytes(), StandardCharsets.UTF_8);
            }

            try (Connection conn = db.getConnection()) {
                conn.setAutoCommit(false);
                try (Statement stmt = conn.createStatement()) {
                    // Separa per istruzioni terminanti con ';'
                    String[] statements = script.split(";\\s*(\\r?\\n)+");
                    for (String sql : statements) {
                        String clean = sql.trim();
                        // Salta commenti vuoti
                        clean = clean.replaceAll("(?m)^--.*$", "").trim();
                        if (!clean.isEmpty()) {
                            try {
                                stmt.execute(clean);
                            } catch (Exception ex) {
                                System.err.println("ERRORE NELLA QUERY:\n" + clean);
                                throw ex;
                            }
                        }
                    }
                    conn.commit();
                    System.out.println("SUCCESSO: Tutte le query di demo.sql sono state eseguite correttamente!");
                } catch (Exception ex) {
                    conn.rollback();
                    throw ex;
                }
            }
        });
    }
}
