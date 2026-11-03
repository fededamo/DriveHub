# R00 — verifiche del 31 ottobre 2026

Baseline Git: `c2a81122ee6be44760ded4160fde229bd33e837c`; modifiche locali, nessun commit.
Ambiente: OpenJDK Linux 21.0.12.1, Maven 3.9.16, PostgreSQL 16.15 Ubuntu x86_64.
POM mantenuto a Java 21. Pacchetti estratti in /tmp; nessuna installazione di sistema.

| Comando / prova | Risultato |
|---|---|
| `mvn -Dtest=TestDatabaseConfigurationTest test` | 7 test passati con Java 21 |
| `mvn test` | 21 test senza DB passati, 0 saltati; [log](no-db.txt) |
| `mvn -Ppostgres-tests -Dtest=DatabaseConnectionIntegrationTest,DemoSqlValidationTest,FullWorkflowIntegrationTest,DatabaseIsolationIntegrationTest test` | 7 test PostgreSQL passati, 0 saltati; [log](postgres-targeted.txt) |
| Ruolo di test temporaneamente `CREATEDB`, poi `mvn -Ppostgres-tests -Dtest=DatabaseConnectionIntegrationTest test` | Fallimento atteso `Reset rifiutato`, prima del reset; sentinelle test/app entrambe conservate; ruolo ripristinato `NOCREATEDB`; [log negativo](server-guard-negative.txt) |
| `mvn -Ppostgres-tests clean test` | BUILD SUCCESS, 62 sorgenti principali + 8 di test ricompilati target 21; 28 test, 0 failure/error/skipped; [log](full-suite.txt) |

Il server isolato ha usato esclusivamente `127.0.0.1:55432` e dati in
`/tmp/drivehub-r00-pgdata`. `drivehub_test` ha ruolo dedicato non amministrativo;
`drivehub` è una nuova copia applicativa con schema e marchio `R00_SENTINEL`.
Il test sentinella legge questa copia in sola lettura e conferma che il marchio
sopravvive ai rifiuti di configurazione e ai reset/ricaricamenti del database di test.
La prova aggiuntiva dei privilegi verifica anche `R00_TEST_SENTINEL` nel DB di test,
presente prima e dopo il reset rifiutato. Nessun accesso SQL al DB applicativo preesistente.

La fixture confronta tutte le righe delle 13 tabelle e le sequenze dopo due
ricaricamenti demo e un nuovo reset completo. La prova scadenze usa date relative
nel solo DB di test. Non sono state modificate le date dei file SQL della demo.

Resta il warning Maven già presente sul modello di dipendenza JavaFX (6 problemi);
non impedisce compilazione o test. Non verificati JavaFX grafico e difetti degli altri
ticket. Relazione_SWE esclusa. I vecchi risultati Java 17 e i blocchi precedenti sono
storici e sono superati dalle prove Java 21 riportate qui.

[Hash SHA-256 dei sorgenti/configurazione verificati](sources.sha256).
