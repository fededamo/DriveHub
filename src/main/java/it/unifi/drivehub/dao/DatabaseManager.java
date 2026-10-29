package it.unifi.drivehub.dao;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Gestore centralizzato delle connessioni PostgreSQL e delle transazioni atomiche.
 * Garantisce l'integrità dei dati e la conformità al principio Single Source of Truth.
 */
public final class DatabaseManager {

    private static final String DEFAULT_URL = "jdbc:postgresql://localhost:5432/drivehub";
    private static final String DEFAULT_USER = "drivehub";
    private static final String DEFAULT_PASS = "drivehub";

    private final String url;
    private final String user;
    private final String password;

    public DatabaseManager() {
        Properties props = new Properties();
        try (InputStream in = getClass().getClassLoader().getResourceAsStream("application.properties")) {
            if (in != null) {
                props.load(in);
            }
        } catch (IOException ignored) {}

        this.url = props.getProperty("db.url", DEFAULT_URL);
        this.user = props.getProperty("db.user", DEFAULT_USER);
        this.password = props.getProperty("db.password", DEFAULT_PASS);

        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("PostgreSQL JDBC Driver non trovato nel classpath.", e);
        }
    }

    public DatabaseManager(String url, String user, String password) {
        this.url = url;
        this.user = user;
        this.password = password;
    }

    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, user, password);
    }

    @FunctionalInterface
    public interface TransactionConsumer {
        void execute(Connection conn) throws Exception;
    }

    @FunctionalInterface
    public interface TransactionFunction<T> {
        T execute(Connection conn) throws Exception;
    }

    /**
     * Esegue una sequenza di operazioni in una transazione ACID con rollback atomico su errore.
     */
    public void executeInTransaction(TransactionConsumer consumer) {
        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);
            try {
                consumer.execute(conn);
                conn.commit();
            } catch (Exception e) {
                try {
                    conn.rollback();
                } catch (SQLException rollbackEx) {
                    e.addSuppressed(rollbackEx);
                }
                throw new RuntimeException("Transazione fallita: eseguito rollback.", e);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Errore di connessione al database durante la transazione.", e);
        }
    }

    /**
     * Esegue una transazione con valore di ritorno e rollback atomico su eccezione.
     */
    public <T> T executeInTransactionWithResult(TransactionFunction<T> function) {
        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);
            try {
                T result = function.execute(conn);
                conn.commit();
                return result;
            } catch (Exception e) {
                try {
                    conn.rollback();
                } catch (SQLException rollbackEx) {
                    e.addSuppressed(rollbackEx);
                }
                throw new RuntimeException("Transazione fallita: eseguito rollback.", e);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Errore di connessione al database durante la transazione.", e);
        }
    }
}

