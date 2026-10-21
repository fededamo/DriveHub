package it.unifi.ing.drivehub.dao.postgres;

import javax.sql.DataSource;

/**
 * Utility per eseguire il reset del database e il ripopolamento con i dati predefiniti di demo.sql.
 */
public final class ResetDatabase {

    private ResetDatabase() {
    }

    public static void main(String[] args) {
        try {
            System.out.println("==================================================");
            System.out.println("   DriveHub - Reset e Ripopolamento Database      ");
            System.out.println("==================================================");

            DatabaseConfig config = DatabaseConfig.fromEnvironment();
            System.out.println("Connessione a: " + config.url() + " (utente: " + config.user() + ")");

            DataSource dataSource = new DriverManagerDataSource(config);

            // Assicura che la struttura delle tabelle (init.sql) esista
            System.out.println("Verifica schema tabelle (init.sql)...");
            new DatabaseBootstrap(dataSource, false).initialize();

            // Esegue demo.sql per svuotare e ripopolare
            System.out.println("Esecuzione di demo.sql (svuotamento e inserimento dati demo)...");
            new JdbcTransactionManager(dataSource).inTransaction((JdbcTransactionManager.SqlAction)
                    conn -> SqlScriptRunner.run(conn, "db/demo.sql"));

            System.out.println("--------------------------------------------------");
            System.out.println("COMPLETATO CON SUCCESSO!");
            System.out.println("Tutti i dati sono stati azzerati e rimpiazzati con i dati di demo.sql.");
            System.out.println("==================================================");
        } catch (Exception e) {
            System.err.println("ERRORE durante il ripristino del database: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}
