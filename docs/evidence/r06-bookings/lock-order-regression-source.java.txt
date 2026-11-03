package it.unifi.drivehub;

import it.unifi.drivehub.dao.DatabaseManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

@Tag("postgres")
class BookingConcurrencyIntegrationTest {
    private DatabaseManager db;
    private AppContext ctx;
    private LocalDate start;

    @BeforeEach
    void setUp() throws Exception {
        db = TestDatabase.fromEnvironment().reset();
        ctx = new AppContext(db);
        start = LocalDate.now().plusDays(2);
        sql("""
                DELETE FROM sale; DELETE FROM rent; DELETE FROM test_drive; DELETE FROM request;
                UPDATE bank_account SET balance = 30000 WHERE client IN (1, 2);
                INSERT INTO model (id, brand, name, release_price, release_year)
                  VALUES (1101, 'Fiat', 'Ticket11', 20000, EXTRACT(YEAR FROM CURRENT_DATE));
                INSERT INTO vehicle (id, model, vin, kilometers, price, year, state) VALUES
                  (1101, 1101, 'T1100000000000001', 0, 20000, EXTRACT(YEAR FROM CURRENT_DATE), 'RENT'),
                  (1102, 1101, 'T1100000000000002', 0, 20000, EXTRACT(YEAR FROM CURRENT_DATE), 'RENT');
                """);
    }

    @Test
    void overlappingRentalsFromDifferentClientsCannotBothBeConfirmed() throws Exception {
        var failures = race("SELECT id FROM vehicle WHERE id = 1101 FOR UPDATE",
                () -> new AppContext(db).getRentalService().bookRental(1101L, 1L, start, start),
                () -> new AppContext(db).getRentalService().bookRental(1101L, 2L, start, start));
        assertNull(failures.get(0));
        assertNotNull(failures.get(1), "Due noleggi sovrapposti non possono entrambi riuscire");
        assertTrue(rootMessage(failures.get(1)).contains("già prenotato"), rootMessage(failures.get(1)));
        ctx = new AppContext(db);
        assertEquals(1, ctx.getOperationQueryService().getClientRents(1L).size());
        assertTrue(ctx.getOperationQueryService().getClientRents(2L).isEmpty());
        assertEquals(new BigDecimal("30000.00"), ctx.getAuthService().getClientBankAccount(2L).getBalance());
    }

    @Test
    void sameClientCannotExceedThreeTestDrivesAcrossDifferentVehicles() throws Exception {
        sql("INSERT INTO test_drive (vehicle, client, date) VALUES "
                + "(1101, 1, CURRENT_DATE + 10 + TIME '10:00'),"
                + "(1101, 1, CURRENT_DATE + 11 + TIME '10:00')");
        var failures = race("SELECT id FROM client WHERE id = 1 FOR UPDATE",
                () -> new AppContext(db).getTestDriveService().bookTestDrive(1101L, 1L, start.atTime(10, 0)),
                () -> new AppContext(db).getTestDriveService().bookTestDrive(1102L, 1L, start.atTime(10, 0)));
        assertNull(failures.get(0));
        assertNotNull(failures.get(1), "Lo stesso cliente non deve ottenere quattro test drive");
        assertTrue(rootMessage(failures.get(1)).contains("limite massimo di 3"), rootMessage(failures.get(1)));
        assertEquals(3, new AppContext(db).getTestDriveService().getClientTestDrives(1L).size());
    }

    @Test
    void rentalWinsAgainstATestDriveStartedBeforeItsCommit() throws Exception {
        var failures = race("SELECT client FROM bank_account WHERE client = 1 FOR UPDATE",
                () -> new AppContext(db).getRentalService().bookRental(1101L, 1L, start, start),
                () -> new AppContext(db).getTestDriveService().bookTestDrive(1101L, 2L, start.atTime(10, 0)));
        assertNull(failures.get(0));
        assertNotNull(failures.get(1), "Il test drive non può sopravvivere al noleggio concorrente");
        assertTrue(rootMessage(failures.get(1)).contains("non disponibile"), rootMessage(failures.get(1)));
        ctx = new AppContext(db);
        assertEquals(1, ctx.getOperationQueryService().getClientRents(1L).size());
        assertTrue(ctx.getTestDriveService().getClientTestDrives(2L).isEmpty());
    }

    @Test
    void databaseRejectsInclusiveRentalOverlapWithoutServices() throws Exception {
        sql("INSERT INTO rent (vehicle, client, amount, start, \"end\") VALUES "
                + "(1101, 1, 20, CURRENT_DATE + 2, CURRENT_DATE + 4)");
        var failure = assertThrows(SQLException.class, () -> sql(
                "INSERT INTO rent (vehicle, client, amount, start, \"end\") VALUES "
                        + "(1101, 2, 20, CURRENT_DATE + 4, CURRENT_DATE + 5)"));
        assertEquals("23P01", failure.getSQLState());
        assertTrue(ctx.getOperationQueryService().getClientRents(2L).isEmpty());
    }

    @Test
    void rentalUsesThePriceRereadAfterTheVehicleLock() throws Exception {
        var rent = behindGate("UPDATE vehicle SET price = 10000, price_mode = 'MANUAL' WHERE id = 1101",
                true, () -> new AppContext(db).getRentalService().bookRental(1101L, 1L, start, start));
        assertEquals(new BigDecimal("10.00"), rent.getAmount());
        assertEquals(new BigDecimal("29990.00"),
                new AppContext(db).getAuthService().getClientBankAccount(1L).getBalance());
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void rejectedBookingsExposeTheBusinessReasonAfterRollback(boolean rental) {
        if (rental) {
            ctx.getRentalService().bookRental(1101L, 1L, start, start);
        } else {
            for (int day = 0; day < 3; day++) {
                ctx.getTestDriveService().bookTestDrive(1101L, 1L, start.plusDays(day).atTime(10, 0));
            }
        }
        var failure = assertThrows(IllegalStateException.class, () -> {
            if (rental) ctx.getRentalService().bookRental(1101L, 2L, start, start);
            else ctx.getTestDriveService().bookTestDrive(1102L, 1L, start.atTime(10, 0));
        });
        assertTrue(failure.getMessage().contains(rental ? "già prenotato" : "limite massimo di 3"));
    }

    @ParameterizedTest
    @CsvSource({"-2,-1,true", "-1,0,false", "-1,3,false", "0,0,false", "1,1,false", "2,2,false", "3,4,true"})
    void rentalPeriodsAreInclusiveAndAdjacentDaysRemainBookable(int from, int to, boolean available) {
        ctx.getRentalService().bookRental(1101L, 1L, start, start.plusDays(2));
        Runnable booking = () -> ctx.getRentalService().bookRental(1101L, 2L,
                start.plusDays(from), start.plusDays(to));
        if (available) assertDoesNotThrow(booking::run);
        else assertThrows(IllegalStateException.class, booking::run);
        ctx = new AppContext(db);
        assertEquals(available ? 1 : 0, ctx.getOperationQueryService().getClientRents(2L).size());
        assertEquals(new BigDecimal(available ? "29961.00" : "30000.00"),
                ctx.getAuthService().getClientBankAccount(2L).getBalance());
    }

    @Test
    void concurrentRentalsForSameClientOnDifferentVehiclesCannotOverdraw() throws Exception {
        sql("UPDATE bank_account SET balance = 25 WHERE client = 1");
        var failures = race("SELECT id FROM client WHERE id = 1 FOR UPDATE",
                () -> new AppContext(db).getRentalService().bookRental(1101L, 1L, start, start),
                () -> new AppContext(db).getRentalService().bookRental(1102L, 1L, start, start));
        assertNull(failures.get(0));
        assertNotNull(failures.get(1));
        assertTrue(failures.get(1).getMessage().contains("Saldo insufficiente"));
        ctx = new AppContext(db);
        assertEquals(1, ctx.getOperationQueryService().getClientRents(1L).size());
        assertEquals(new BigDecimal("5.00"), ctx.getAuthService().getClientBankAccount(1L).getBalance());
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void testDriveCommittedFirstIsCancelledAtomicallyByRental(boolean failCancellation) throws Exception {
        if (failCancellation) {
            sql("CREATE FUNCTION fail_ticket11() RETURNS trigger LANGUAGE plpgsql AS $$ BEGIN "
                    + "IF (SELECT balance FROM bank_account WHERE client = 1) <> 29980 "
                    + "OR NOT EXISTS (SELECT 1 FROM rent WHERE vehicle = 1101) "
                    + "OR EXISTS (SELECT 1 FROM test_drive WHERE vehicle = 1101) "
                    + "THEN RAISE EXCEPTION 'ticket11_writes_not_observed'; END IF; "
                    + "RAISE EXCEPTION 'ticket11_after_cancellation'; END $$; "
                    + "CREATE TRIGGER fail_ticket11 AFTER DELETE ON test_drive FOR EACH STATEMENT "
                    + "EXECUTE FUNCTION fail_ticket11()");
        }
        var failures = race("SELECT id FROM client WHERE id = 2 FOR UPDATE",
                () -> new AppContext(db).getTestDriveService().bookTestDrive(1101L, 2L, start.atTime(10, 0)),
                () -> new AppContext(db).getRentalService().bookRental(1101L, 1L, start, start));
        assertNull(failures.get(0));
        if (failCancellation) {
            assertNotNull(failures.get(1));
            assertTrue(rootMessage(failures.get(1)).contains("ticket11_after_cancellation"));
        } else assertNull(failures.get(1));
        ctx = new AppContext(db);
        assertEquals(failCancellation ? 1 : 0, ctx.getTestDriveService().getClientTestDrives(2L).size());
        assertEquals(failCancellation ? 0 : 1, ctx.getOperationQueryService().getClientRents(1L).size());
        assertEquals(new BigDecimal(failCancellation ? "30000.00" : "29980.00"),
                ctx.getAuthService().getClientBankAccount(1L).getBalance());
    }

    @Test
    void twoClientsCannotBookTheSameTestDriveSlot() throws Exception {
        var failures = race("SELECT id FROM vehicle WHERE id = 1101 FOR UPDATE",
                () -> new AppContext(db).getTestDriveService().bookTestDrive(1101L, 1L, start.atTime(10, 0)),
                () -> new AppContext(db).getTestDriveService().bookTestDrive(1101L, 2L, start.atTime(10, 0)));
        assertNull(failures.get(0));
        assertNotNull(failures.get(1));
        assertTrue(failures.get(1).getMessage().contains("non disponibile"));
        ctx = new AppContext(db);
        assertEquals(1, ctx.getTestDriveService().getClientTestDrives(1L).size());
        assertTrue(ctx.getTestDriveService().getClientTestDrives(2L).isEmpty());
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1, 2})
    void testDriveCannotUseAnyDayOfARental(int day) {
        ctx.getRentalService().bookRental(1101L, 1L, start, start.plusDays(2));
        assertThrows(IllegalStateException.class, () -> ctx.getTestDriveService()
                .bookTestDrive(1101L, 2L, start.plusDays(day).atTime(10, 0)));
        assertDoesNotThrow(() -> ctx.getTestDriveService().bookTestDrive(1101L, 2L, start.minusDays(1).atTime(9, 0)));
        assertDoesNotThrow(() -> ctx.getTestDriveService().bookTestDrive(1101L, 2L, start.plusDays(3).atTime(16, 0)));
        assertEquals(2, new AppContext(db).getTestDriveService().getClientTestDrives(2L).size());
    }

    @Test
    void testDriveDoesNotRequireABankAccount() throws Exception {
        sql("DELETE FROM bank_account WHERE client = 2; UPDATE vehicle SET state = 'SALE' WHERE id = 1102");
        var td = ctx.getTestDriveService().bookTestDrive(1102L, 2L, start.atTime(9, 0));
        assertEquals(td.getId(), new AppContext(db).getTestDriveService().getClientTestDrives(2L).getFirst().getId());
    }

    @Test
    void clientLockIsCompatibleWithPurchaseForeignKeysAndTheAccountProtocol() throws Exception {
        sql("UPDATE vehicle SET state = 'SALE' WHERE id = 1102");
        var failures = race("SELECT client FROM bank_account WHERE client = 1 FOR UPDATE",
                () -> new AppContext(db).getPurchaseService().initiatePurchase(1102L, 1L),
                () -> new AppContext(db).getRentalService().bookRental(1101L, 1L, start, start));
        assertNull(failures.get(0));
        assertNull(failures.get(1));
        ctx = new AppContext(db);
        assertEquals(1, ctx.getOperationQueryService().getClientRequests(1L).size());
        assertEquals(1, ctx.getOperationQueryService().getClientRents(1L).size());
        assertEquals(new BigDecimal("25980.00"), ctx.getAuthService().getClientBankAccount(1L).getBalance());
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void databaseSerializesOverlapsEvenWhenWritersBypassServices(boolean commitFirst) throws Exception {
        var error = behindGate("INSERT INTO rent (vehicle, client, amount, start, \"end\") "
                        + "VALUES (1101, 1, 20, CURRENT_DATE + 2, CURRENT_DATE + 4)", commitFirst, () -> {
                    try {
                        sql("INSERT INTO rent (vehicle, client, amount, start, \"end\") "
                                + "VALUES (1101, 2, 20, CURRENT_DATE + 4, CURRENT_DATE + 5)");
                        return null;
                    } catch (SQLException failure) {
                        return failure;
                    }
                });
        if (commitFirst) {
            assertNotNull(error);
            assertEquals("23P01", error.getSQLState());
        } else assertNull(error);
        ctx = new AppContext(db);
        assertEquals(commitFirst ? 1 : 0, ctx.getOperationQueryService().getClientRents(1L).size());
        assertEquals(commitFirst ? 0 : 1, ctx.getOperationQueryService().getClientRents(2L).size());
    }

    @ParameterizedTest
    @CsvSource({"08:59,23514", "16:00:00.000001,23514", "10:00,23505"})
    void databasePreservesTestDriveHoursAndVehicleTimestampUniqueness(String time, String state) throws Exception {
        sql("INSERT INTO test_drive (vehicle, client, date) VALUES (1101, 1, CURRENT_DATE + 2 + TIME '10:00')");
        var failure = assertThrows(SQLException.class, () -> sql(
                "INSERT INTO test_drive (vehicle, client, date) VALUES (1101, 2, CURRENT_DATE + 2 + TIME '" + time + "')"));
        assertEquals(state, failure.getSQLState());
        assertTrue(ctx.getTestDriveService().getClientTestDrives(2L).isEmpty());
    }

    @Test
    void insufficientBalanceDoesNotCreateRentalOrCancelTestDrives() throws Exception {
        sql("UPDATE bank_account SET balance = 19.99 WHERE client = 1");
        var td = ctx.getTestDriveService().bookTestDrive(1101L, 2L, start.atTime(10, 0));
        var failure = assertThrows(IllegalStateException.class,
                () -> ctx.getRentalService().bookRental(1101L, 1L, start, start));
        assertTrue(failure.getMessage().contains("Saldo insufficiente"));
        ctx = new AppContext(db);
        assertTrue(ctx.getOperationQueryService().getClientRents(1L).isEmpty());
        assertEquals(td.getId(), ctx.getTestDriveService().getClientTestDrives(2L).getFirst().getId());
        assertEquals(new BigDecimal("19.99"), ctx.getAuthService().getClientBankAccount(1L).getBalance());
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void failureAfterBookingInsertRollsBackActualWrites(boolean rental) throws Exception {
        String table = rental ? "rent" : "test_drive";
        sql("CREATE FUNCTION fail_ticket11() RETURNS trigger LANGUAGE plpgsql AS $$ BEGIN "
                + "IF NOT EXISTS (SELECT 1 FROM " + table + " WHERE id = NEW.id) "
                + (rental ? "OR (SELECT balance FROM bank_account WHERE client = 1) <> 29980 " : "")
                + "THEN RAISE EXCEPTION 'ticket11_writes_not_observed'; END IF; "
                + "RAISE EXCEPTION 'ticket11_after_insert'; END $$; "
                + "CREATE TRIGGER fail_ticket11 AFTER INSERT ON " + table
                + " FOR EACH ROW EXECUTE FUNCTION fail_ticket11()");
        var failure = assertThrows(RuntimeException.class, () -> {
            if (rental) ctx.getRentalService().bookRental(1101L, 1L, start, start);
            else ctx.getTestDriveService().bookTestDrive(1101L, 1L, start.atTime(10, 0));
        });
        assertTrue(rootMessage(failure).contains("ticket11_after_insert"), rootMessage(failure));
        ctx = new AppContext(db);
        assertTrue(ctx.getOperationQueryService().getClientRents(1L).isEmpty());
        assertTrue(ctx.getTestDriveService().getClientTestDrives(1L).isEmpty());
        assertEquals(new BigDecimal("30000.00"), ctx.getAuthService().getClientBankAccount(1L).getBalance());
    }

    @Test
    void pastDatesAndReversedRentalPeriodAreRejectedWithoutWrites() {
        assertThrows(IllegalArgumentException.class, () -> ctx.getRentalService()
                .bookRental(1101L, 1L, LocalDate.now().minusDays(1), start));
        assertThrows(IllegalArgumentException.class, () -> ctx.getRentalService()
                .bookRental(1101L, 1L, start, start.minusDays(1)));
        assertThrows(IllegalArgumentException.class, () -> ctx.getTestDriveService()
                .bookTestDrive(1101L, 1L, LocalDate.now().minusDays(1).atTime(10, 0)));
        assertTrue(ctx.getOperationQueryService().getClientRents(1L).isEmpty());
        assertTrue(ctx.getTestDriveService().getClientTestDrives(1L).isEmpty());
    }

    @AfterEach
    void removeFailureInjection() throws Exception {
        if (db != null) sql("DROP FUNCTION IF EXISTS fail_ticket11() CASCADE");
    }

    @Test
    void concurrentRentalsOnAdjacentDaysCanBothBeConfirmed() throws Exception {
        var failures = race("SELECT id FROM vehicle WHERE id = 1101 FOR UPDATE",
                () -> new AppContext(db).getRentalService().bookRental(1101L, 1L, start, start),
                () -> new AppContext(db).getRentalService().bookRental(1101L, 2L, start.plusDays(1), start.plusDays(1)));
        assertNull(failures.get(0));
        assertNull(failures.get(1));
        ctx = new AppContext(db);
        assertEquals(1, ctx.getOperationQueryService().getClientRents(1L).size());
        assertEquals(1, ctx.getOperationQueryService().getClientRents(2L).size());
        assertEquals(new BigDecimal("29980.00"), ctx.getAuthService().getClientBankAccount(1L).getBalance());
        assertEquals(new BigDecimal("29980.00"), ctx.getAuthService().getClientBankAccount(2L).getBalance());
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void bookingRereadsVehicleStateAfterWaitingForTheLock(boolean rental) throws Exception {
        String state = rental ? "SALE" : "SOLD";
        var failure = behindGate("UPDATE vehicle SET state = '" + state + "' WHERE id = 1101", true,
                outcome(() -> {
                    if (rental) new AppContext(db).getRentalService().bookRental(1101L, 1L, start, start);
                    else new AppContext(db).getTestDriveService().bookTestDrive(1101L, 1L, start.atTime(10, 0));
                }));
        assertInstanceOf(IllegalStateException.class, failure);
        ctx = new AppContext(db);
        assertTrue(ctx.getOperationQueryService().getClientRents(1L).isEmpty());
        assertTrue(ctx.getTestDriveService().getClientTestDrives(1L).isEmpty());
        assertEquals(new BigDecimal("30000.00"), ctx.getAuthService().getClientBankAccount(1L).getBalance());
    }

    private <T> T behindGate(String gateQuery, boolean commitGate, Callable<T> operation) throws Exception {
        var worker = Executors.newSingleThreadExecutor();
        try (var gate = db.getConnection(); var observer = db.getConnection()) {
            gate.setAutoCommit(false);
            try (var statement = gate.createStatement()) {
                statement.setQueryTimeout(10);
                statement.execute(gateQuery);
            }
            var result = worker.submit(operation);
            try {
                awaitWaiters(observer, 1);
                if (commitGate) gate.commit();
                else gate.rollback();
                return result.get(10, TimeUnit.SECONDS);
            } finally {
                gate.rollback();
            }
        } finally {
            worker.shutdownNow();
            assertTrue(worker.awaitTermination(10, TimeUnit.SECONDS), "Worker JDBC non terminato");
        }
    }

    @Test
    void purchaseCompletionAndRentalCannotDeadlockWithATestDriveOnTheBlockedVehicle() throws Exception {
        sql("UPDATE vehicle SET state = 'SALE' WHERE id = 1101");
        var request = ctx.getPurchaseService().initiatePurchase(1101L, 1L);
        ctx.getPurchaseService().approvePurchase(request.getId(), 16L);
        var failures = race("SELECT client FROM bank_account WHERE client = 1 FOR UPDATE", true,
                () -> new AppContext(db).getPurchaseService().completePurchase(request.getId()),
                () -> new AppContext(db).getRentalService().bookRental(1102L, 1L, start, start),
                () -> new AppContext(db).getTestDriveService().bookTestDrive(1101L, 1L, start.atTime(10, 0)));
        assertNull(failures.get(0), "L'acquisto deve completarsi senza deadlock");
        assertNull(failures.get(1), "Il noleggio deve completarsi senza deadlock");
        assertInstanceOf(IllegalStateException.class, failures.get(2), () -> rootMessage(failures.get(2)));
        assertTrue(failures.get(2).getMessage().contains("solo su veicoli"));
        ctx = new AppContext(db);
        assertEquals(new BigDecimal("9980.00"), ctx.getAuthService().getClientBankAccount(1L).getBalance());
        assertEquals(1, ctx.getOperationQueryService().getClientSales(1L).size());
        assertEquals(1, ctx.getOperationQueryService().getClientRents(1L).size());
        assertTrue(ctx.getTestDriveService().getClientTestDrives(1L).isEmpty());
    }

    private List<RuntimeException> race(String gateQuery, Runnable... operations) throws Exception {
        return race(gateQuery, false, operations);
    }

    private List<RuntimeException> race(String gateQuery, boolean lastMayRejectImmediately, Runnable... operations) throws Exception {
        var workers = Executors.newFixedThreadPool(operations.length);
        try (Connection gate = db.getConnection(); Connection observer = db.getConnection()) {
            gate.setAutoCommit(false);
            try (var statement = gate.createStatement()) {
                statement.setQueryTimeout(10);
                statement.execute(gateQuery);
            }
            try {
                var attempts = new ArrayList<Future<RuntimeException>>();
                for (Runnable operation : operations) {
                    attempts.add(workers.submit(outcome(operation)));
                    awaitWaiters(observer, attempts.size(),
                            lastMayRejectImmediately && attempts.size() == operations.length ? attempts.getLast() : null);
                }
                gate.rollback();
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
            try {
                operation.run();
                return null;
            } catch (RuntimeException e) {
                return e;
            }
        };
    }

    private static void awaitWaiters(Connection observer, int expected) throws Exception {
        awaitWaiters(observer, expected, null);
    }

    private static void awaitWaiters(Connection observer, int expected, Future<?> immediateRejection) throws Exception {
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(10);
        try (var statement = observer.createStatement()) {
            statement.setQueryTimeout(10);
            do {
                try (var result = statement.executeQuery("""
                        SELECT count(DISTINCT pid), string_agg(pid || ':' || pg_blocking_pids(pid)::text, ', ')
                        FROM pg_stat_activity
                        WHERE datname = current_database() AND usename = current_user
                          AND pid <> pg_backend_pid() AND state = 'active' AND wait_event_type = 'Lock'
                          AND cardinality(pg_blocking_pids(pid)) > 0
                        """)) {
                    assertTrue(result.next());
                    if (result.getInt(1) == expected) {
                        System.out.println("Ticket11 waiters " + expected + " pid:blockers " + result.getString(2));
                        return;
                    }
                }
                if (immediateRejection != null && immediateRejection.isDone()) {
                    System.out.println("Ticket11 operation " + expected + " completed before gate release");
                    return;
                }
            } while (System.nanoTime() < deadline);
        }
        fail("Attese PostgreSQL distinte mancanti: " + expected);
    }

    private static String rootMessage(Throwable failure) {
        while (failure.getCause() != null) failure = failure.getCause();
        return failure.getMessage();
    }

    private void sql(String query) throws Exception {
        try (var conn = db.getConnection(); var statement = conn.createStatement()) {
            statement.setQueryTimeout(10);
            statement.execute(query);
        }
    }
}
