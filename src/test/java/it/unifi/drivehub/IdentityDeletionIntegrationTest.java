package it.unifi.drivehub;

import it.unifi.drivehub.dao.DatabaseManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

@Tag("postgres")
class IdentityDeletionIntegrationTest {
    private DatabaseManager db;
    private AppContext ctx;

    @BeforeEach
    void setUp() throws Exception {
        db = TestDatabase.fromEnvironment().reset();
        ctx = new AppContext(db);
        sql("""
                DELETE FROM sale; DELETE FROM rent; DELETE FROM test_drive; DELETE FROM request;
                UPDATE bank_account SET balance = 30000 WHERE client IN (1, 2);
                INSERT INTO model(id, brand, name, release_price, release_year)
                VALUES (1301, 'Fiat', 'Ticket13', 20000, EXTRACT(YEAR FROM CURRENT_DATE));
                INSERT INTO vehicle(id, model, vin, kilometers, price, year, state) VALUES
                (1301, 1301, 'T1300000000000001', 0, 20000, EXTRACT(YEAR FROM CURRENT_DATE), 'SALE'),
                (1302, 1301, 'T1300000000000002', 0, 20000, EXTRACT(YEAR FROM CURRENT_DATE), 'RENT');
                """);
    }

    @ParameterizedTest
    @CsvSource({"Venditore,SALE,20000", "SALESMAN,SALE,20000",
            "Venditore,PURCHASE,16000", "SALESMAN,PURCHASE,16000",
            "Venditore,PENALTY,400", "SALESMAN,PENALTY,400"})
    void salesmanWithOnlyCompletedSalesRetainsIdentityAndAttribution(String role, String type, int amount) throws Exception {
        sql("INSERT INTO sale(vehicle, salesman, client, amount, date, type) VALUES "
                + "(1301,16,1," + amount + ",CURRENT_TIMESTAMP,'" + type + "')");
        var before = snapshot();

        var error = assertThrows(IllegalStateException.class,
                () -> ctx.getAuthService().deleteAccount(16L, role, "password"));

        assertTrue(error.getMessage().contains("associate"));
        assertEquals(before, snapshot());
        assertEquals(16L, new AppContext(db).getAuthService().login("paolo.costa@drivehub.it", "password").getId());
    }

    @ParameterizedTest
    @CsvSource({"Cliente,PURCHASE", "CLIENT,PURCHASE", "Cliente,SALE", "CLIENT,SALE"})
    void clientWithOnlyARequestCannotDeleteIdentity(String role, String type) throws Exception {
        if (type.equals("SALE")) ctx.getPurchaseService().initiatePurchase(1301L, 1L);
        else ctx.getSaleToDealershipService().submitSaleProposal(1L, 1301L,
                "T1300000000000003", null, 0, java.time.LocalDate.now().getYear());
        var before = snapshot();

        var error = assertThrows(IllegalStateException.class,
                () -> ctx.getAuthService().deleteAccount(1L, role, "password"));

        assertTrue(error.getMessage().contains("richieste"));
        assertEquals(before, snapshot());
        assertEquals(1L, new AppContext(db).getAuthService().login("mario.rossi@email.it", "password").getId());
    }

    @ParameterizedTest
    @CsvSource({"SALE,20000", "PURCHASE,16000", "PENALTY,400"})
    void databaseCannotEraseSalesmanAttributionEvenWithoutServices(String type, int amount) throws Exception {
        sql("INSERT INTO sale(vehicle, salesman, client, amount, date, type) VALUES "
                + "(1301,16,1," + amount + ",CURRENT_TIMESTAMP,'" + type + "')");
        var before = snapshot();

        var error = assertThrows(SQLException.class, () -> sql("DELETE FROM salesman WHERE id = 16"));

        assertEquals("23503", error.getSQLState());
        assertEquals(before, snapshot());
    }

    @Test
    void saleCommittedWhileDeletionWaitsPreservesAttributionAndExplainsRefusal() throws Exception {
        var failures = race("SELECT id FROM salesman WHERE id = 16 FOR NO KEY UPDATE",
                "INSERT INTO sale(vehicle,salesman,client,amount,date,type) VALUES (1301,16,1,20000,CURRENT_TIMESTAMP,'SALE')",
                () -> ctx.getAuthService().deleteAccount(16L, "Venditore", "password"));

        assertInstanceOf(IllegalStateException.class, failures.getFirst());
        assertEquals(16, scalar("SELECT salesman FROM sale WHERE vehicle = 1301"));
        assertEquals(16L, new AppContext(db).getAuthService().login("paolo.costa@drivehub.it", "password").getId());
    }

    @Test
    void purchaseAlreadyWaitingOnBalanceCannotDeadlockWithIdentityDeletion() throws Exception {
        var failures = race("SELECT client FROM bank_account WHERE client = 1 FOR UPDATE", "",
                () -> new AppContext(db).getPurchaseService().initiatePurchase(1301L, 1L),
                () -> new AppContext(db).getAuthService().deleteAccount(1L, "Cliente", "password"));

        assertNull(failures.getFirst(), () -> String.valueOf(failures.getFirst()));
        assertInstanceOf(IllegalStateException.class, failures.get(1));
        assertEquals(1, scalar("SELECT count(*) FROM request WHERE client = 1"));
        assertEquals(26000, scalar("SELECT balance FROM bank_account WHERE client = 1"));
    }

    @AfterEach
    void cleanupTriggers() throws Exception {
        if (db != null) sql("DROP FUNCTION IF EXISTS ticket13_gate() CASCADE; "
                + "DROP FUNCTION IF EXISTS ticket13_fail() CASCADE; DROP SEQUENCE IF EXISTS ticket13_writes");
    }

    @Test
    void skippedDeletionCannotReportSuccess() throws Exception {
        sql("""
                CREATE FUNCTION ticket13_fail() RETURNS trigger LANGUAGE plpgsql AS $$
                BEGIN RETURN NULL; END $$;
                CREATE TRIGGER ticket13_fail BEFORE DELETE ON client
                FOR EACH ROW EXECUTE FUNCTION ticket13_fail();
                """);
        var before = snapshot();

        assertThrows(IllegalStateException.class,
                () -> ctx.getAuthService().deleteAccount(1L, "Cliente", "password"));

        assertEquals(before, snapshot());
    }

    @ParameterizedTest
    @CsvSource({"1,Cliente,mario.rossi@email.it", "1,CLIENT,mario.rossi@email.it",
            "16,Venditore,paolo.costa@drivehub.it", "16,SALESMAN,paolo.costa@drivehub.it",
            "20,Manager,vittorio.manager@drivehub.it", "20,MANAGER,vittorio.manager@drivehub.it"})
    void identitiesWithoutOperationsRemainDeletableIncludingDemonstrationBalance(long id, String role, String email) throws Exception {
        var before = snapshot();
        int accounts = scalar("SELECT count(*) FROM bank_account");

        ctx.getAuthService().deleteAccount(id, role, "password");

        assertThrows(IllegalArgumentException.class, () -> new AppContext(db).getAuthService().login(email, "password"));
        assertEquals(0, scalar("SELECT count(*) FROM public.\"user\" WHERE id = " + id));
        assertEquals(accounts - (id == 1 ? 1 : 0), scalar("SELECT count(*) FROM bank_account"));
        var after = snapshot();
        String changed = id == 1 ? "client" : id == 16 ? "salesman" : "manager";
        before.remove(changed); after.remove(changed);
        if (id == 1) { before.remove("bank_account"); after.remove("bank_account"); }
        assertEquals(before, after);
    }

    @Test
    void clientWithoutADemonstrationAccountIsStillDeletable() throws Exception {
        sql("DELETE FROM bank_account WHERE client = 1");
        ctx.getAuthService().deleteAccount(1L, "Cliente", "password");
        assertEquals(0, scalar("SELECT count(*) FROM client WHERE id = 1"));
    }

    @ParameterizedTest
    @CsvSource({"1,Cliente", "16,Venditore", "20,Manager"})
    void wrongPasswordCannotDeleteAnyRole(long id, String role) throws Exception {
        var before = snapshot();
        assertThrows(IllegalArgumentException.class, () -> ctx.getAuthService().deleteAccount(id, role, "errata"));
        assertEquals(before, snapshot());
    }

    @ParameterizedTest
    @CsvSource({"1,Manager", "16,Cliente", "20,Venditore"})
    void mismatchedRoleCannotReportADeletion(long id, String role) throws Exception {
        var before = snapshot();
        assertThrows(IllegalStateException.class, () -> ctx.getAuthService().deleteAccount(id, role, "password"));
        assertEquals(before, snapshot());
    }

    @ParameterizedTest
    @ValueSource(strings = {"unknown", "client;DELETE FROM sale", ""})
    void unrecognizedRoleCannotChangeAnyData(String role) throws Exception {
        var before = snapshot();
        var error = assertThrows(RuntimeException.class,
                () -> ctx.getAuthService().deleteAccount(1L, role, "password"));
        assertInstanceOf(IllegalArgumentException.class, rootCause(error));
        assertEquals(before, snapshot());
    }

    @ParameterizedTest
    @CsvSource({"Cliente,TD,-1", "CLIENT,TD,1", "Cliente,RENT,-2", "CLIENT,RENT,0", "Cliente,RENT,2",
            "Cliente,SALE,0", "CLIENT,PURCHASE,0", "Cliente,PENALTY,0"})
    void clientOperationsAndHistoryRemainBinding(String role, String operation, int day) throws Exception {
        switch (operation) {
            case "TD" -> sql("INSERT INTO test_drive(vehicle,client,date) VALUES (1302,1,CURRENT_DATE + " + day + " + TIME '10:00')");
            case "RENT" -> sql("INSERT INTO rent(vehicle,client,amount,start,\"end\") VALUES (1302,1,20,CURRENT_DATE + " + day + ",CURRENT_DATE + " + day + ")");
            default -> sql("INSERT INTO sale(vehicle,salesman,client,amount,date,type) VALUES (1301,16,1,"
                    + (operation.equals("PURCHASE") ? 16000 : 400) + ",CURRENT_TIMESTAMP,'" + operation + "')");
        }
        var before = snapshot();
        assertThrows(IllegalStateException.class, () -> ctx.getAuthService().deleteAccount(1L, role, "password"));
        assertEquals(before, snapshot());
    }

    @ParameterizedTest
    @CsvSource({"Venditore,PURCHASE", "SALESMAN,PURCHASE", "Venditore,SALE", "SALESMAN,SALE"})
    void salesmanWithOnlyAnAssignedRequestCannotDelete(String role, String type) throws Exception {
        var request = type.equals("SALE") ? ctx.getPurchaseService().initiatePurchase(1301L, 1L)
                : ctx.getSaleToDealershipService().submitSaleProposal(1L, 1301L, "T1300000000000003", null, 0, java.time.LocalDate.now().getYear());
        sql("UPDATE request SET salesman = 16 WHERE id = " + request.getId());
        var before = snapshot();
        assertThrows(IllegalStateException.class, () -> ctx.getAuthService().deleteAccount(16L, role, "password"));
        assertEquals(before, snapshot());
    }

    @ParameterizedTest
    @CsvSource({"false,false", "false,true", "true,false", "true,true"})
    void clientBookingAndDeletionAreCoherentInBothOrders(boolean rental, boolean deleteFirst) throws Exception {
        Runnable booking = () -> {
            var fresh = new AppContext(db);
            var date = java.time.LocalDate.now().plusDays(2);
            if (rental) fresh.getRentalService().bookRental(1302L, 1L, date, date);
            else fresh.getTestDriveService().bookTestDrive(1302L, 1L, date.atTime(10, 0));
        };
        Runnable deletion = () -> new AppContext(db).getAuthService().deleteAccount(1L, "Cliente", "password");
        var failures = race("SELECT id FROM client WHERE id = 1 FOR NO KEY UPDATE", "",
                deleteFirst ? deletion : booking, deleteFirst ? booking : deletion);
        assertNull(failures.getFirst(), () -> String.valueOf(failures.getFirst()));
        assertInstanceOf(IllegalStateException.class, failures.get(1));
        assertEquals(deleteFirst ? 0 : 1, scalar("SELECT count(*) FROM client WHERE id = 1"));
        assertEquals(deleteFirst ? 0 : 1, scalar("SELECT count(*) FROM " + (rental ? "rent" : "test_drive") + " WHERE client = 1"));
        if (!deleteFirst) assertEquals(rental ? 29980 : 30000, scalar("SELECT balance FROM bank_account WHERE client = 1"));
    }

    @Test
    void deletionAlreadyWaitingOnBalancePreventsPurchaseWithoutPartialVehicleChange() throws Exception {
        var failures = race("SELECT client FROM bank_account WHERE client = 1 FOR UPDATE", "",
                () -> new AppContext(db).getAuthService().deleteAccount(1L, "Cliente", "password"),
                () -> new AppContext(db).getPurchaseService().initiatePurchase(1301L, 1L));
        assertNull(failures.getFirst());
        assertNotNull(failures.get(1));
        assertFalse(rootCause(failures.get(1)) instanceof SQLException error && "40P01".equals(error.getSQLState()));
        assertEquals(0, scalar("SELECT count(*) FROM request WHERE client = 1"));
        assertEquals(0, scalar("SELECT count(*) FROM bank_account WHERE client = 1"));
        assertEquals(1, scalar("SELECT count(*) FROM vehicle WHERE id = 1301 AND state = 'SALE'"));
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void acquisitionInsertedBeforeDeletionEitherRetainsAttributionOrRollsBack(boolean failOperation) throws Exception {
        var request = ctx.getSaleToDealershipService().submitSaleProposal(1L, 1301L, "T1300000000000003", null, 0, java.time.LocalDate.now().getYear());
        sql("CREATE SEQUENCE ticket13_writes; CREATE FUNCTION ticket13_gate() RETURNS trigger LANGUAGE plpgsql AS $$ BEGIN "
                + "IF (SELECT balance FROM bank_account WHERE client = 1) <> 50000 OR NOT EXISTS (SELECT 1 FROM sale WHERE salesman = 16) "
                + "THEN RAISE EXCEPTION 'ticket13_writes_not_observed'; END IF; "
                + "PERFORM nextval('ticket13_writes'); PERFORM pg_advisory_xact_lock(1313); "
                + (failOperation ? "RAISE EXCEPTION 'ticket13_after_acquisition'; " : "")
                + "RETURN NEW; END $$; CREATE TRIGGER ticket13_gate AFTER INSERT ON sale FOR EACH ROW EXECUTE FUNCTION ticket13_gate()");
        var failures = race("SELECT pg_advisory_xact_lock(1313)", "",
                () -> new AppContext(db).getSaleToDealershipService().approveSaleProposal(request.getId(), 16L),
                () -> new AppContext(db).getAuthService().deleteAccount(16L, "Venditore", "password"));
        assertEquals(1, scalar("SELECT last_value FROM ticket13_writes"));
        if (failOperation) {
            assertNotNull(failures.getFirst());
            assertTrue(rootCause(failures.getFirst()).getMessage().contains("ticket13_after_acquisition"));
            assertNull(failures.get(1));
        } else {
            assertNull(failures.getFirst());
            assertInstanceOf(IllegalStateException.class, failures.get(1));
        }
        assertEquals(failOperation ? 0 : 1, scalar("SELECT count(*) FROM salesman WHERE id = 16"));
        assertEquals(failOperation ? 0 : 1, scalar("SELECT count(*) FROM sale WHERE salesman = 16"));
        assertEquals(failOperation ? 30000 : 50000, scalar("SELECT balance FROM bank_account WHERE client = 1"));
        assertEquals(failOperation ? 1 : 0, scalar("SELECT count(*) FROM request WHERE id = " + request.getId()));
        assertEquals(1, scalar("SELECT count(*) FROM vehicle WHERE vin = 'T1300000000000003' AND state = '" + (failOperation ? "WAITING" : "SALE") + "'"));
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void acquisitionAfterActualSalesmanDeletionRollsBackItsCredit(boolean failDeletion) throws Exception {
        var request = ctx.getSaleToDealershipService().submitSaleProposal(1L, 1301L, "T1300000000000003", null, 0, java.time.LocalDate.now().getYear());
        sql("CREATE SEQUENCE ticket13_writes; CREATE FUNCTION ticket13_gate() RETURNS trigger LANGUAGE plpgsql AS $$ BEGIN "
                + "IF EXISTS (SELECT 1 FROM salesman WHERE id = 16) THEN RAISE EXCEPTION 'ticket13_delete_not_observed'; END IF; "
                + "PERFORM nextval('ticket13_writes'); PERFORM pg_advisory_xact_lock(1313); "
                + (failDeletion ? "RAISE EXCEPTION 'ticket13_after_deletion'; " : "")
                + "RETURN OLD; END $$; CREATE TRIGGER ticket13_gate AFTER DELETE ON salesman FOR EACH ROW EXECUTE FUNCTION ticket13_gate(); "
                + "CREATE FUNCTION ticket13_fail() RETURNS trigger LANGUAGE plpgsql AS $$ BEGIN "
                + "IF (SELECT balance FROM bank_account WHERE client = 1) <> 50000 THEN RAISE EXCEPTION 'ticket13_credit_not_observed'; END IF; "
                + "PERFORM nextval('ticket13_writes'); RETURN NEW; END $$; "
                + "CREATE TRIGGER ticket13_fail BEFORE INSERT ON sale FOR EACH ROW EXECUTE FUNCTION ticket13_fail()");
        var failures = race("SELECT pg_advisory_xact_lock(1313)", "",
                () -> new AppContext(db).getAuthService().deleteAccount(16L, "Venditore", "password"),
                () -> new AppContext(db).getSaleToDealershipService().approveSaleProposal(request.getId(), 16L));
        assertEquals(2, scalar("SELECT last_value FROM ticket13_writes"));
        if (failDeletion) {
            assertNotNull(failures.getFirst());
            assertTrue(rootCause(failures.getFirst()).getMessage().contains("ticket13_after_deletion"));
            assertNull(failures.get(1));
        } else {
            assertNull(failures.getFirst());
            assertInstanceOf(SQLException.class, rootCause(failures.get(1)));
            assertEquals("23503", ((SQLException) rootCause(failures.get(1))).getSQLState());
        }
        assertEquals(failDeletion ? 1 : 0, scalar("SELECT count(*) FROM salesman WHERE id = 16"));
        assertEquals(failDeletion ? 1 : 0, scalar("SELECT count(*) FROM sale WHERE salesman = 16"));
        assertEquals(failDeletion ? 50000 : 30000, scalar("SELECT balance FROM bank_account WHERE client = 1"));
        assertEquals(failDeletion ? 0 : 1, scalar("SELECT count(*) FROM request WHERE id = " + request.getId()));
        assertEquals(1, scalar("SELECT count(*) FROM vehicle WHERE vin = 'T1300000000000003' AND state = '" + (failDeletion ? "SALE" : "WAITING") + "'"));
    }

    @Test
    void errorAfterActualIdentityAndAccountDeletionRollsBackBoth() throws Exception {
        sql("""
                CREATE SEQUENCE ticket13_writes;
                CREATE FUNCTION ticket13_fail() RETURNS trigger LANGUAGE plpgsql AS $$ BEGIN
                IF EXISTS (SELECT 1 FROM client WHERE id = 1) OR EXISTS (SELECT 1 FROM bank_account WHERE client = 1)
                THEN RAISE EXCEPTION 'ticket13_deletions_not_observed'; END IF;
                PERFORM nextval('ticket13_writes'); RAISE EXCEPTION 'ticket13_after_deletions'; END $$;
                CREATE TRIGGER ticket13_fail AFTER DELETE ON client FOR EACH ROW EXECUTE FUNCTION ticket13_fail();
                """);
        var before = snapshot();
        var failure = assertThrows(RuntimeException.class, () -> ctx.getAuthService().deleteAccount(1L, "Cliente", "password"));
        assertTrue(rootCause(failure).getMessage().contains("ticket13_after_deletions"));
        assertEquals(1, scalar("SELECT last_value FROM ticket13_writes"));
        assertEquals(before, snapshot());
        assertEquals(1L, new AppContext(db).getAuthService().login("mario.rossi@email.it", "password").getId());
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void clientDeletionRollbackAllowsWaitingBooking(boolean rental) throws Exception {
        sql("""
                CREATE SEQUENCE ticket13_writes;
                CREATE FUNCTION ticket13_gate() RETURNS trigger LANGUAGE plpgsql AS $$ BEGIN
                IF EXISTS (SELECT 1 FROM client WHERE id = 1) OR EXISTS (SELECT 1 FROM bank_account WHERE client = 1)
                THEN RAISE EXCEPTION 'ticket13_deletions_not_observed'; END IF;
                PERFORM nextval('ticket13_writes'); PERFORM pg_advisory_xact_lock(1313);
                RAISE EXCEPTION 'ticket13_after_deletions'; END $$;
                CREATE TRIGGER ticket13_gate AFTER DELETE ON client FOR EACH ROW EXECUTE FUNCTION ticket13_gate();
                """);
        var failures = race("SELECT pg_advisory_xact_lock(1313)", "",
                () -> new AppContext(db).getAuthService().deleteAccount(1L, "Cliente", "password"),
                () -> {
                    var fresh = new AppContext(db); var date = java.time.LocalDate.now().plusDays(2);
                    if (rental) fresh.getRentalService().bookRental(1302L, 1L, date, date);
                    else fresh.getTestDriveService().bookTestDrive(1302L, 1L, date.atTime(10, 0));
                });
        assertNotNull(failures.getFirst());
        assertTrue(rootCause(failures.getFirst()).getMessage().contains("ticket13_after_deletions"));
        assertNull(failures.get(1));
        assertEquals(1, scalar("SELECT last_value FROM ticket13_writes"));
        assertEquals(1, scalar("SELECT count(*) FROM client WHERE id = 1"));
        assertEquals(1, scalar("SELECT count(*) FROM " + (rental ? "rent" : "test_drive") + " WHERE client = 1"));
        assertEquals(rental ? 29980 : 30000, scalar("SELECT balance FROM bank_account WHERE client = 1"));
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void newVehicleProposalAfterClientDeletionCommitsOnlyIfDeletionRollsBack(boolean failDeletion) throws Exception {
        sql("CREATE SEQUENCE ticket13_writes; CREATE FUNCTION ticket13_gate() RETURNS trigger LANGUAGE plpgsql AS $$ BEGIN "
                + "IF EXISTS (SELECT 1 FROM client WHERE id = 1) OR EXISTS (SELECT 1 FROM bank_account WHERE client = 1) "
                + "THEN RAISE EXCEPTION 'ticket13_deletions_not_observed'; END IF; "
                + "PERFORM nextval('ticket13_writes'); PERFORM pg_advisory_xact_lock(1313); "
                + (failDeletion ? "RAISE EXCEPTION 'ticket13_after_deletion'; " : "")
                + "RETURN OLD; END $$; CREATE TRIGGER ticket13_gate AFTER DELETE ON client FOR EACH ROW EXECUTE FUNCTION ticket13_gate(); "
                + "CREATE FUNCTION ticket13_fail() RETURNS trigger LANGUAGE plpgsql AS $$ BEGIN "
                + "IF NOT EXISTS (SELECT 1 FROM vehicle WHERE vin = 'T1300000000000003' AND state = 'WAITING') "
                + "THEN RAISE EXCEPTION 'ticket13_vehicle_not_observed'; END IF; "
                + "PERFORM nextval('ticket13_writes'); RETURN NEW; END $$; "
                + "CREATE TRIGGER ticket13_fail BEFORE INSERT ON request FOR EACH ROW EXECUTE FUNCTION ticket13_fail()");
        var failures = race("SELECT pg_advisory_xact_lock(1313)", "",
                () -> new AppContext(db).getAuthService().deleteAccount(1L, "Cliente", "password"),
                () -> new AppContext(db).getSaleToDealershipService().submitSaleProposal(1L, 1301L,
                        "T1300000000000003", null, 0, java.time.LocalDate.now().getYear()));
        assertEquals(2, scalar("SELECT last_value FROM ticket13_writes"));
        if (failDeletion) {
            assertTrue(rootCause(failures.getFirst()).getMessage().contains("ticket13_after_deletion"));
            assertNull(failures.get(1));
        } else {
            assertNull(failures.getFirst());
            assertInstanceOf(SQLException.class, rootCause(failures.get(1)));
            assertEquals("23503", ((SQLException) rootCause(failures.get(1))).getSQLState());
        }
        assertEquals(failDeletion ? 1 : 0, scalar("SELECT count(*) FROM client WHERE id = 1"));
        assertEquals(failDeletion ? 1 : 0, scalar("SELECT count(*) FROM request WHERE client = 1"));
        assertEquals(failDeletion ? 1 : 0, scalar("SELECT count(*) FROM vehicle WHERE vin = 'T1300000000000003'"));
        if (failDeletion) assertEquals(30000, scalar("SELECT balance FROM bank_account WHERE client = 1"));
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void purchaseApprovalCannotReferToADeletedSalesman(boolean failDeletion) throws Exception {
        var request = ctx.getPurchaseService().initiatePurchase(1301L, 1L);
        sql("CREATE SEQUENCE ticket13_writes; CREATE FUNCTION ticket13_gate() RETURNS trigger LANGUAGE plpgsql AS $$ BEGIN "
                + "IF EXISTS (SELECT 1 FROM salesman WHERE id = 16) THEN RAISE EXCEPTION 'ticket13_delete_not_observed'; END IF; "
                + "PERFORM nextval('ticket13_writes'); PERFORM pg_advisory_xact_lock(1313); "
                + (failDeletion ? "RAISE EXCEPTION 'ticket13_after_deletion'; " : "")
                + "RETURN OLD; END $$; CREATE TRIGGER ticket13_gate AFTER DELETE ON salesman FOR EACH ROW EXECUTE FUNCTION ticket13_gate()");
        var failures = race("SELECT pg_advisory_xact_lock(1313)", "",
                () -> new AppContext(db).getAuthService().deleteAccount(16L, "Venditore", "password"),
                () -> new AppContext(db).getPurchaseService().approvePurchase(request.getId(), 16L));
        assertEquals(1, scalar("SELECT last_value FROM ticket13_writes"));
        if (failDeletion) {
            assertTrue(rootCause(failures.getFirst()).getMessage().contains("ticket13_after_deletion"));
            assertNull(failures.get(1));
        } else {
            assertNull(failures.getFirst());
            assertInstanceOf(SQLException.class, rootCause(failures.get(1)));
            assertEquals("23503", ((SQLException) rootCause(failures.get(1))).getSQLState());
        }
        assertEquals(failDeletion ? 1 : 0, scalar("SELECT count(*) FROM salesman WHERE id = 16"));
        assertEquals(failDeletion ? 1 : 0, scalar("SELECT count(*) FROM request WHERE salesman = 16 AND exp_date IS NOT NULL"));
        assertEquals(failDeletion ? 0 : 1, scalar("SELECT count(*) FROM request WHERE salesman IS NULL AND exp_date IS NULL"));
        assertEquals(26000, scalar("SELECT balance FROM bank_account WHERE client = 1"));
    }

    private static Throwable rootCause(Throwable error) {
        while (error.getCause() != null) error = error.getCause();
        return error;
    }

    private List<RuntimeException> race(String gateQuery, String beforeRelease, Runnable... operations) throws Exception {
        var workers = Executors.newFixedThreadPool(operations.length);
        try (var gate = db.getConnection(); var observer = db.getConnection()) {
            gate.setAutoCommit(false);
            try (var statement = gate.createStatement()) {
                statement.setQueryTimeout(10);
                statement.execute(gateQuery);
            }
            try {
                var attempts = new ArrayList<java.util.concurrent.Future<RuntimeException>>();
                for (Runnable operation : operations) {
                    attempts.add(workers.submit(outcome(operation)));
                    awaitWaiters(observer, attempts.size());
                }
                if (!beforeRelease.isEmpty()) {
                    try (var statement = gate.createStatement()) {
                        statement.setQueryTimeout(10);
                        statement.execute(beforeRelease);
                    }
                }
                gate.commit();
                var failures = new ArrayList<RuntimeException>();
                for (var attempt : attempts) failures.add(attempt.get(10, TimeUnit.SECONDS));
                return failures;
            } finally {
                gate.rollback();
                try (var statement = observer.createStatement()) {
                    statement.setQueryTimeout(10);
                    statement.executeQuery("""
                            SELECT pg_cancel_backend(pid) FROM pg_stat_activity
                            WHERE datname = current_database() AND usename = current_user
                            AND pid <> pg_backend_pid() AND state = 'active' AND wait_event_type = 'Lock'
                            """).close();
                }
            }
        } finally {
            workers.shutdownNow();
            assertTrue(workers.awaitTermination(10, TimeUnit.SECONDS), "Worker JDBC non terminati");
        }
    }

    private static Callable<RuntimeException> outcome(Runnable operation) {
        return () -> {
            try { operation.run(); return null; }
            catch (RuntimeException failure) { return failure; }
        };
    }

    private static void awaitWaiters(Connection observer, int expected) throws Exception {
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(10);
        try (var statement = observer.createStatement()) {
            statement.setQueryTimeout(10);
            do {
                try (var result = statement.executeQuery("""
                        SELECT count(DISTINCT pid), string_agg(pid || ':' || pg_blocking_pids(pid)::text, ', ')
                        FROM pg_stat_activity WHERE datname = current_database() AND usename = current_user
                        AND pid <> pg_backend_pid() AND state = 'active' AND wait_event_type = 'Lock'
                        AND cardinality(pg_blocking_pids(pid)) > 0
                        """)) {
                    assertTrue(result.next());
                    if (result.getInt(1) == expected) {
                        System.out.println("Ticket13 waiters " + expected + " pid:blockers " + result.getString(2));
                        return;
                    }
                }
            } while (System.nanoTime() < deadline);
        }
        fail("Attese PostgreSQL distinte mancanti: " + expected);
    }

    private int scalar(String query) throws Exception {
        try (var conn = db.getConnection(); var statement = conn.createStatement()) {
            statement.setQueryTimeout(10);
            try (var result = statement.executeQuery(query)) {
                assertTrue(result.next());
                return result.getInt(1);
            }
        }
    }

    private Map<String, String> snapshot() throws Exception {
        var rows = new LinkedHashMap<String, String>();
        try (var conn = db.getConnection(); var statement = conn.createStatement()) {
            statement.setQueryTimeout(10);
            for (String table : new String[]{"client", "salesman", "manager", "bank_account", "vehicle", "request", "sale", "rent", "test_drive"}) {
                try (var result = statement.executeQuery("SELECT COALESCE(jsonb_agg(to_jsonb(t) ORDER BY to_jsonb(t)::text),'[]')::text FROM " + table + " t")) {
                    assertTrue(result.next());
                    rows.put(table, result.getString(1));
                }
            }
        }
        return rows;
    }

    private void sql(String query) throws Exception {
        try (var conn = db.getConnection(); var statement = conn.createStatement()) {
            statement.setQueryTimeout(10);
            statement.execute(query);
        }
    }
}
