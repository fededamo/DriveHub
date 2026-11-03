package it.unifi.drivehub;

import it.unifi.drivehub.dao.DatabaseManager;
import it.unifi.drivehub.domain.vehicle.VehicleState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

@Tag("postgres")
class VehicleDestinationIntegrationTest {
    private DatabaseManager db;

    @BeforeEach
    void setUp() throws Exception {
        db = TestDatabase.fromEnvironment().reset();
        sql("""
                DELETE FROM sale; DELETE FROM rent; DELETE FROM test_drive; DELETE FROM request;
                UPDATE bank_account SET balance = 1000 WHERE client IN (1, 2);
                INSERT INTO model (id, brand, name, release_price, release_year)
                  VALUES (1201, 'Fiat', 'Ticket12', 20000, EXTRACT(YEAR FROM CURRENT_DATE));
                INSERT INTO vehicle (id, model, vin, kilometers, price, year, state) VALUES
                  (1201, 1201, 'T1200000000000001', 0, 20000, EXTRACT(YEAR FROM CURRENT_DATE), 'RENT'),
                  (1202, 1201, 'T1200000000000002', 0, 20000, EXTRACT(YEAR FROM CURRENT_DATE), 'RENT');
                """);
    }

    @ParameterizedTest
    @CsvSource({"SOLD,SALE", "SOLD,RENT", "BLOCKED,SALE", "BLOCKED,RENT",
            "WAITING,SALE", "WAITING,RENT", "SALE,SALE", "RENT,RENT"})
    void onlyOppositeSaleAndRentStatesCanBeSelected(VehicleState from, VehicleState to) throws Exception {
        sql("UPDATE vehicle SET state = '" + from + "' WHERE id = 1201");
        var failure = assertThrows(IllegalStateException.class,
                () -> new AppContext(db).getRentalService().changeVehicleRentalState(1201L, to));
        assertTrue(failure.getMessage().contains("transizione"), failure.getMessage());
        assertEquals(from, state());
        balance(1L, "1000.00");
    }

    @ParameterizedTest
    @CsvSource({"-1,1", "-1,0", "0,1", "0,0"})
    void rentalIncludingTodayPreventsSaleAndAllRefunds(int from, int to) throws Exception {
        rent(1, "100.25", from, to);
        rent(2, "200.75", 3, 4);
        var failure = assertThrows(IllegalStateException.class,
                () -> new AppContext(db).getRentalService().changeVehicleRentalState(1201L, VehicleState.SALE));
        assertTrue(failure.getMessage().contains("noleggio in corso"), failure.getMessage());
        assertEquals(VehicleState.RENT, state());
        balance(1L, "1000.00");
        balance(2L, "1000.00");
        assertEquals(1, new AppContext(db).getOperationQueryService().getClientRents(1L).size());
        assertEquals(1, new AppContext(db).getOperationQueryService().getClientRents(2L).size());
    }

    private void rent(long client, String amount, int from, int to) throws Exception {
        sql("INSERT INTO rent (vehicle, client, amount, start, \"end\") VALUES (1201, "
                + client + ", " + amount + ", CURRENT_DATE + " + from + ", CURRENT_DATE + " + to + ")");
    }

    @Test
    void futureRentalPreviewExcludesTodayAndHistory() throws Exception {
        rent(1, "10", -2, -1);
        rent(1, "20", 0, 0);
        rent(2, "30", 1, 1);
        var future = new AppContext(db).getRentalService().getFutureRentsForVehicle(1201L);
        assertEquals(1, future.size());
        assertEquals(new BigDecimal("30.00"), future.getFirst().getAmount());
    }

    @Test
    void skippedCancellationCannotCommitRefundsOrSale() throws Exception {
        rent(1, "100.25", 1, 1);
        rent(2, "200.75", 2, 2);
        sql("""
                CREATE FUNCTION ticket12_failure() RETURNS trigger LANGUAGE plpgsql AS $$
                BEGIN IF OLD.client=2 THEN RETURN NULL; END IF; RETURN OLD; END $$;
                CREATE TRIGGER ticket12_failure BEFORE DELETE ON rent
                  FOR EACH ROW EXECUTE FUNCTION ticket12_failure();
                """);
        assertThrows(RuntimeException.class,
                () -> new AppContext(db).getRentalService().changeVehicleRentalState(1201L, VehicleState.SALE));
        assertRefundsRolledBack();
    }

    @ParameterizedTest
    @EnumSource(value = VehicleState.class, names = {"SOLD", "BLOCKED", "WAITING"})
    @NullSource
    void nonDestinationTargetsAreRejected(VehicleState target) {
        assertThrows(IllegalArgumentException.class,
                () -> new AppContext(db).getRentalService().changeVehicleRentalState(1201L, target));
        assertEquals(VehicleState.RENT, state());
    }

    @Test
    void unknownVehicleCannotChangeDestination() {
        assertThrows(IllegalStateException.class,
                () -> new AppContext(db).getRentalService().changeVehicleRentalState(Long.MAX_VALUE, VehicleState.SALE));
    }

    @ParameterizedTest
    @ValueSource(strings = {"SALE", "RENT"})
    void bothDirectionsWorkWithoutRentals(VehicleState from) throws Exception {
        sql("UPDATE vehicle SET state='" + from + "' WHERE id=1201");
        var target = from == VehicleState.SALE ? VehicleState.RENT : VehicleState.SALE;
        assertEquals(0, new AppContext(db).getRentalService().changeVehicleRentalState(1201L, target));
        assertEquals(target, state());
        balance(1L, "1000.00");
    }

    @Test
    void futureRentalsAreFullyRefundedForEveryClientWhileHistoryAndOtherVehiclesRemain() throws Exception {
        rent(1, "100.25", 1, 1);
        rent(2, "200.75", 2, 2);
        rent(1, "50", 3, 3);
        rent(1, "70", -3, -1);
        sql("""
                INSERT INTO rent(vehicle,client,amount,start,"end")
                  VALUES(1202,2,60,CURRENT_DATE+1,CURRENT_DATE+1);
                INSERT INTO test_drive(vehicle,client,date) VALUES
                  (1201,1,CURRENT_DATE+10+TIME '10:00'),(1202,2,CURRENT_DATE+10+TIME '10:00');
                """);
        assertEquals(3, new AppContext(db).getRentalService().changeVehicleRentalState(1201L, VehicleState.SALE));
        assertEquals(VehicleState.SALE, state());
        balance(1L, "1150.25");
        balance(2L, "1200.75");
        var reread = new AppContext(db);
        assertEquals(new BigDecimal("70.00"), reread.getOperationQueryService().getClientRents(1L).getFirst().getAmount());
        assertEquals(1, reread.getOperationQueryService().getClientRents(1L).size());
        assertEquals(1202L, reread.getOperationQueryService().getClientRents(2L).getFirst().getVehicleId());
        assertEquals(1, reread.getOperationQueryService().getClientRents(2L).size());
        assertEquals(1, reread.getTestDriveService().getClientTestDrives(1L).size());
        assertEquals(1, reread.getTestDriveService().getClientTestDrives(2L).size());
        assertTrue(reread.getOperationQueryService().getAllSales().isEmpty());
    }

    @Test
    void missingSecondAccountRollsBackAnAlreadyWrittenFirstRefund() throws Exception {
        rent(1, "100.25", 1, 1);
        rent(2, "200.75", 2, 2);
        sql("""
                DELETE FROM bank_account WHERE client=2;
                CREATE SEQUENCE ticket12_writes;
                CREATE FUNCTION ticket12_failure() RETURNS trigger LANGUAGE plpgsql AS $$
                BEGIN
                  IF NEW.client=1 AND NEW.balance=1100.25 AND NOT EXISTS(SELECT 1 FROM rent WHERE vehicle=1201) THEN
                    PERFORM nextval('ticket12_writes');
                  END IF;
                  RETURN NEW;
                END $$;
                CREATE TRIGGER ticket12_failure AFTER UPDATE ON bank_account
                  FOR EACH ROW EXECUTE FUNCTION ticket12_failure();
                """);
        var failure = assertThrows(IllegalStateException.class,
                () -> new AppContext(db).getRentalService().changeVehicleRentalState(1201L, VehicleState.SALE));
        assertTrue(failure.getMessage().contains("Conto cliente non trovato"));
        try (var conn = db.getConnection(); var statement = conn.createStatement()) {
            statement.setQueryTimeout(10);
            try (var result = statement.executeQuery("SELECT last_value, is_called FROM ticket12_writes")) {
                assertTrue(result.next());
                assertTrue(result.getBoolean(2), "Primo rimborso e cancellazioni realmente scritti prima del conto mancante");
                assertEquals(1, result.getLong(1));
            }
        }
        assertEquals(VehicleState.RENT, state());
        balance(1L, "1000.00");
        assertThrows(IllegalStateException.class, () -> new AppContext(db).getAuthService().getClientBankAccount(2L));
        assertEquals(1, new AppContext(db).getOperationQueryService().getClientRents(1L).size());
        assertEquals(1, new AppContext(db).getOperationQueryService().getClientRents(2L).size());
    }

    @ParameterizedTest
    @CsvSource({"bank_account,BEFORE", "bank_account,AFTER", "vehicle,BEFORE", "vehicle,AFTER"})
    void refundOrStateFailureRollsBackActualCancellationsAndCredits(String table, String timing) throws Exception {
        rent(1, "100.25", 1, 1);
        rent(2, "200.75", 2, 2);
        String condition = table.equals("bank_account") ? "NEW.client=2" : "NEW.id=1201";
        String secondBalance = table.equals("bank_account") && timing.equals("BEFORE") ? "1000" : "1200.75";
        String failureAction = timing.equals("BEFORE") ? "RETURN NULL;" : "RAISE EXCEPTION 'ticket12_after_writes';";
        sql("""
                CREATE FUNCTION ticket12_failure() RETURNS trigger LANGUAGE plpgsql AS $$
                BEGIN
                  IF %s THEN
                    IF EXISTS(SELECT 1 FROM rent WHERE vehicle=1201)
                       OR (SELECT balance FROM bank_account WHERE client=1)<>1100.25
                       OR (SELECT balance FROM bank_account WHERE client=2)<>%s THEN
                      RAISE EXCEPTION 'ticket12_missing_writes';
                    END IF;
                    %s
                  END IF;
                  RETURN NEW;
                END $$;
                CREATE TRIGGER ticket12_failure %s UPDATE ON %s
                  FOR EACH ROW EXECUTE FUNCTION ticket12_failure();
                """.formatted(condition, secondBalance, failureAction, timing, table));
        var failure = assertThrows(RuntimeException.class,
                () -> new AppContext(db).getRentalService().changeVehicleRentalState(1201L, VehicleState.SALE));
        assertFalse(rootMessage(failure).contains("ticket12_missing_writes"), rootMessage(failure));
        if (timing.equals("AFTER")) assertTrue(rootMessage(failure).contains("ticket12_after_writes"), rootMessage(failure));
        assertRefundsRolledBack();
    }

    @Test
    void destinationCommittedFirstRejectsConcurrentRentalWithoutChargingIt() throws Exception {
        rent(1, "100.25", 1, 1);
        rent(2, "200.75", 2, 2);
        var failures = race(
                () -> assertEquals(2, new AppContext(db).getRentalService().changeVehicleRentalState(1201L, VehicleState.SALE)),
                () -> new AppContext(db).getRentalService().bookRental(1201L, 1L,
                        LocalDate.now().plusDays(3), LocalDate.now().plusDays(3)));
        assertNull(failures.getFirst());
        assertNotNull(failures.getLast());
        assertTrue(rootMessage(failures.getLast()).contains("non è abilitato"));
        assertEquals(VehicleState.SALE, state());
        balance(1L, "1100.25"); balance(2L, "1200.75");
        assertTrue(new AppContext(db).getOperationQueryService().getClientRents(1L).isEmpty());
        assertTrue(new AppContext(db).getOperationQueryService().getClientRents(2L).isEmpty());
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 2})
    void rentalCommittedFirstIsRereadUnderVehicleLock(int day) throws Exception {
        sql("INSERT INTO test_drive(vehicle,client,date) VALUES (1201,2,CURRENT_DATE+10+TIME '10:00')");
        var date = LocalDate.now().plusDays(day);
        var failures = race(
                () -> new AppContext(db).getRentalService().bookRental(1201L, 1L, date, date),
                () -> assertEquals(1, new AppContext(db).getRentalService().changeVehicleRentalState(1201L, VehicleState.SALE)));
        assertNull(failures.getFirst());
        if (day == 0) {
            assertNotNull(failures.getLast());
            assertTrue(rootMessage(failures.getLast()).contains("noleggio in corso"));
            assertEquals(VehicleState.RENT, state());
            balance(1L, "980.00");
            assertEquals(1, new AppContext(db).getOperationQueryService().getClientRents(1L).size());
        } else {
            assertNull(failures.getLast());
            assertEquals(VehicleState.SALE, state());
            balance(1L, "1000.00");
            assertTrue(new AppContext(db).getOperationQueryService().getClientRents(1L).isEmpty());
        }
        assertTrue(new AppContext(db).getTestDriveService().getClientTestDrives(2L).isEmpty(), "Observer 08 conserva la priorità commerciale");
        assertTrue(new AppContext(db).getOperationQueryService().getAllSales().isEmpty());
    }

    @Test
    void destinationRollbackReleasesVehicleForConcurrentRental() throws Exception {
        rent(1, "100.25", 1, 1);
        rent(2, "200.75", 2, 2);
        sql("""
                CREATE FUNCTION ticket12_failure() RETURNS trigger LANGUAGE plpgsql AS $$
                BEGIN
                  IF NEW.id=1201 AND NEW.state='SALE' THEN
                    IF EXISTS(SELECT 1 FROM rent WHERE vehicle=1201)
                       OR (SELECT balance FROM bank_account WHERE client=1)<>1100.25
                       OR (SELECT balance FROM bank_account WHERE client=2)<>1200.75 THEN
                      RAISE EXCEPTION 'ticket12_missing_writes';
                    END IF;
                    RAISE EXCEPTION 'ticket12_after_writes';
                  END IF;
                  RETURN NEW;
                END $$;
                CREATE TRIGGER ticket12_failure AFTER UPDATE ON vehicle
                  FOR EACH ROW EXECUTE FUNCTION ticket12_failure();
                """);
        var date = LocalDate.now().plusDays(3);
        var failures = race(
                () -> new AppContext(db).getRentalService().changeVehicleRentalState(1201L, VehicleState.SALE),
                () -> new AppContext(db).getRentalService().bookRental(1201L, 1L, date, date));
        assertNotNull(failures.getFirst());
        assertTrue(rootMessage(failures.getFirst()).contains("ticket12_after_writes"));
        assertNull(failures.getLast());
        assertEquals(VehicleState.RENT, state());
        balance(1L, "980.00"); balance(2L, "1000.00");
        assertEquals(2, new AppContext(db).getOperationQueryService().getClientRents(1L).size());
        assertEquals(1, new AppContext(db).getOperationQueryService().getClientRents(2L).size());
    }

    @ParameterizedTest
    @ValueSource(strings = {"SALE", "RENT"})
    void concurrentDestinationCommandsCannotRefundTwice(VehicleState secondTarget) throws Exception {
        rent(1, "100.25", 1, 1);
        rent(2, "200.75", 2, 2);
        var failures = race(
                () -> assertEquals(2, new AppContext(db).getRentalService().changeVehicleRentalState(1201L, VehicleState.SALE)),
                () -> assertEquals(0, new AppContext(db).getRentalService().changeVehicleRentalState(1201L, secondTarget)));
        assertNull(failures.getFirst());
        if (secondTarget==VehicleState.SALE) assertNotNull(failures.getLast());
        else assertNull(failures.getLast());
        assertEquals(secondTarget, state());
        balance(1L, "1100.25"); balance(2L, "1200.75");
        assertTrue(new AppContext(db).getOperationQueryService().getClientRents(1L).isEmpty());
        assertTrue(new AppContext(db).getOperationQueryService().getClientRents(2L).isEmpty());
    }

    private List<RuntimeException> race(Runnable... operations) throws Exception {
        var workers = Executors.newFixedThreadPool(operations.length);
        try (Connection gate = db.getConnection(); Connection observer = db.getConnection()) {
            gate.setAutoCommit(false);
            try (var statement = gate.createStatement()) {
                statement.setQueryTimeout(10);
                statement.executeQuery("SELECT client FROM bank_account WHERE client=1 FOR UPDATE").close();
            }
            try {
                var attempts = new ArrayList<Future<RuntimeException>>();
                for (Runnable operation : operations) {
                    attempts.add(workers.submit(() -> {
                        try { operation.run(); return null; }
                        catch (RuntimeException failure) { return failure; }
                    }));
                    awaitWaiters(observer, attempts.size());
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
                            WHERE datname=current_database() AND usename=current_user
                              AND pid<>pg_backend_pid() AND state='active' AND wait_event_type='Lock'
                            """).close();
                }
            }
        } finally {
            workers.shutdownNow();
            assertTrue(workers.awaitTermination(10, TimeUnit.SECONDS), "Worker JDBC non terminati");
        }
    }

    private static void awaitWaiters(Connection observer, int expected) throws Exception {
        long deadline = System.nanoTime()+TimeUnit.SECONDS.toNanos(10);
        try (var statement = observer.createStatement()) {
            statement.setQueryTimeout(10);
            do {
                try (var result = statement.executeQuery("""
                        SELECT count(DISTINCT pid),string_agg(pid||':'||pg_blocking_pids(pid)::text,', ')
                        FROM pg_stat_activity
                        WHERE datname=current_database() AND usename=current_user
                          AND pid<>pg_backend_pid() AND state='active' AND wait_event_type='Lock'
                          AND cardinality(pg_blocking_pids(pid))>0
                        """)) {
                    assertTrue(result.next());
                    if (result.getInt(1)==expected) {
                        System.out.println("Ticket12 waiters "+expected+" pid:blockers "+result.getString(2));
                        return;
                    }
                }
            } while (System.nanoTime()<deadline);
        }
        fail("Attese PostgreSQL distinte mancanti: "+expected);
    }

    private static String rootMessage(Throwable failure) {
        while (failure.getCause() != null) failure = failure.getCause();
        return failure.getMessage();
    }

    private void assertRefundsRolledBack() {
        assertEquals(VehicleState.RENT, state());
        balance(1L, "1000.00");
        balance(2L, "1000.00");
        assertEquals(1, new AppContext(db).getOperationQueryService().getClientRents(1L).size());
        assertEquals(1, new AppContext(db).getOperationQueryService().getClientRents(2L).size());
        assertTrue(new AppContext(db).getOperationQueryService().getAllSales().isEmpty());
    }

    @AfterEach
    void removeFailureInjection() throws Exception {
        sql("DROP FUNCTION IF EXISTS ticket12_failure() CASCADE; DROP SEQUENCE IF EXISTS ticket12_writes");
    }

    private VehicleState state() {
        return new AppContext(db).getCatalogService().getAllVehicles().stream()
                .filter(v -> v.getId().equals(1201L)).findFirst().orElseThrow().getState();
    }

    private void balance(long client, String expected) {
        assertEquals(new BigDecimal(expected),
                new AppContext(db).getAuthService().getClientBankAccount(client).getBalance());
    }

    private void sql(String query) throws Exception {
        try (var conn = db.getConnection(); var statement = conn.createStatement()) {
            statement.setQueryTimeout(10);
            statement.execute(query);
        }
    }
}
