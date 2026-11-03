package it.unifi.drivehub;

import it.unifi.drivehub.dao.DatabaseManager;
import it.unifi.drivehub.dao.postgres.PostgresBankAccountDao;
import it.unifi.drivehub.domain.vehicle.VehicleState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigDecimal;
import java.sql.Connection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

@Tag("postgres")
class BalanceConcurrencyIntegrationTest {
    private DatabaseManager db;
    private AppContext ctx;

    @BeforeEach
    void setUp() throws Exception {
        db = TestDatabase.fromEnvironment().reset();
        ctx = new AppContext(db);
        sql("""
                DELETE FROM sale;
                DELETE FROM rent;
                DELETE FROM test_drive;
                DELETE FROM request;
                UPDATE bank_account SET balance = 1000;
                INSERT INTO model (id, brand, name, release_price, release_year) VALUES
                  (1001, 'Fiat', 'Ticket04 A', 500, EXTRACT(YEAR FROM CURRENT_DATE)),
                  (1002, 'Fiat', 'Ticket04 B', 1000, EXTRACT(YEAR FROM CURRENT_DATE));
                INSERT INTO vehicle (id, model, vin, kilometers, price, year, state) VALUES
                  (1001, 1001, 'T0400000000000001', 0, 500, EXTRACT(YEAR FROM CURRENT_DATE), 'SALE'),
                  (1002, 1002, 'T0400000000000002', 0, 1000, EXTRACT(YEAR FROM CURRENT_DATE), 'SALE');
                """);
    }

    @AfterEach
    void removeFailureInjection() throws Exception {
        if (db != null) sql("DROP FUNCTION IF EXISTS fail_ticket04_request() CASCADE;"
                + "DROP FUNCTION IF EXISTS fail_ticket04_vehicle() CASCADE");
    }

    @Test
    void concurrentPurchaseDepositsKeepBothDebits() throws Exception {
        List<RuntimeException> failures = race(
                () -> ctx.getPurchaseService().initiatePurchase(1001L, 1L),
                () -> ctx.getPurchaseService().initiatePurchase(1002L, 1L));

        assertTrue(failures.isEmpty(), failures.toString());
        balance(1L, "700.00");
        assertEquals(2, ctx.getOperationQueryService().getClientRequests(1L).size());
        assertEquals(new BigDecimal("100.00"), ctx.getOperationQueryService().getClientRequests(1L)
                .stream().filter(r -> r.getVehicleId() == 1001L).findFirst().orElseThrow().getDeposit());
        assertEquals(new BigDecimal("200.00"), ctx.getOperationQueryService().getClientRequests(1L)
                .stream().filter(r -> r.getVehicleId() == 1002L).findFirst().orElseThrow().getDeposit());
    }

    @Test
    void accountLockRequiresAnActiveTransaction() throws Exception {
        try (var conn = db.getConnection()) {
            assertTrue(conn.getAutoCommit());
            assertThrows(IllegalArgumentException.class,
                    () -> new PostgresBankAccountDao(db).findByClientIdForUpdate(1L, conn));
        }
        balance(1L, "1000.00");
    }

    @Test
    void unaffordableConcurrentPurchasesLeaveOnlyTheSuccessfulOperation() throws Exception {
        sql("UPDATE bank_account SET balance = 250 WHERE client = 1");
        List<RuntimeException> failures = race(
                () -> ctx.getPurchaseService().initiatePurchase(1001L, 1L),
                () -> ctx.getPurchaseService().initiatePurchase(1002L, 1L));

        assertEquals(1, failures.size());
        assertInstanceOf(IllegalStateException.class, failures.getFirst().getCause());
        assertTrue(failures.getFirst().getCause().getMessage().contains("Saldo insufficiente"));
        var requests = ctx.getOperationQueryService().getClientRequests(1L);
        assertEquals(1, requests.size());
        long successfulVehicle = requests.getFirst().getVehicleId();
        balance(1L, successfulVehicle == 1001L ? "150.00" : "50.00");
        assertEquals(VehicleState.BLOCKED, vehicleState(successfulVehicle));
        assertEquals(VehicleState.SALE, vehicleState(successfulVehicle == 1001L ? 1002L : 1001L));
        assertTrue(ctx.getOperationQueryService().getAllSales().isEmpty());
    }

    @Test
    void errorAfterDebitRollsBackBalanceVehicleAndRequest() throws Exception {
        sql("""
                CREATE FUNCTION fail_ticket04_request() RETURNS trigger LANGUAGE plpgsql AS $$
                BEGIN
                  IF (SELECT balance FROM bank_account WHERE client = NEW.client) <> 900 THEN
                    RAISE EXCEPTION 'ticket04_missing_debit';
                  END IF;
                  RAISE EXCEPTION 'ticket04_after_debit';
                END $$;
                CREATE TRIGGER fail_ticket04_request BEFORE INSERT ON request
                  FOR EACH ROW EXECUTE FUNCTION fail_ticket04_request();
                """);

        RuntimeException failure = assertThrows(RuntimeException.class,
                () -> ctx.getPurchaseService().initiatePurchase(1001L, 1L));
        assertTrue(rootMessage(failure).contains("ticket04_after_debit"), failure.toString());
        balance(1L, "1000.00");
        assertEquals(VehicleState.SALE, vehicleState(1001L));
        assertTrue(ctx.getOperationQueryService().getClientRequests(1L).isEmpty());
        assertTrue(ctx.getOperationQueryService().getAllSales().isEmpty());
    }

    @ParameterizedTest(name = "{0} conserva anche la ricarica concorrente")
    @CsvSource({"topup,1300.00", "deposit,1100.00", "completion,800.00", "rejection,1300.00",
            "expiration,1280.00", "acquisition,1300.00", "rental,1100.00", "refund,1300.00"})
    void everyMovementSharesTheAccountProtocol(String flow, String expected) throws Exception {
        Runnable movement = switch (flow) {
            case "topup" -> () -> ctx.getAuthService().topUpClientBalance(1L, new BigDecimal("100"));
            case "deposit" -> () -> ctx.getPurchaseService().initiatePurchase(1001L, 1L);
            case "completion", "rejection", "expiration" -> {
                sql("""
                        UPDATE vehicle SET state = 'BLOCKED' WHERE id = 1001;
                        INSERT INTO request (id, vehicle, client, deposit, agreed_total, type)
                          VALUES (1001, 1001, 1, 100, 500, 'PURCHASE');
                        """);
                if (flow.equals("rejection")) yield () -> ctx.getPurchaseService().rejectPurchase(1001L);
                ctx.getPurchaseService().approvePurchase(1001L, 16L);
                if (flow.equals("completion")) yield () -> ctx.getPurchaseService().completePurchase(1001L);
                sql("UPDATE request SET exp_date = CURRENT_DATE - 1 WHERE id = 1001");
                yield () -> assertEquals(1, ctx.getPurchaseService().checkAndHandleExpiredRequests(1L).size());
            }
            case "acquisition" -> {
                sql("""
                        UPDATE vehicle SET state = 'WAITING' WHERE id = 1001;
                        INSERT INTO request (id, vehicle, client, deposit, type)
                          VALUES (1001, 1001, 1, -100, 'SALE');
                        """);
                yield () -> ctx.getSaleToDealershipService().approveSaleProposal(1001L, 16L);
            }
            case "rental" -> {
                sql("UPDATE model SET release_price = 100000 WHERE id = 1001;"
                        + "UPDATE vehicle SET state = 'RENT' WHERE id = 1001");
                yield () -> ctx.getRentalService().bookRental(1001L, 1L,
                        java.time.LocalDate.now().plusDays(1), java.time.LocalDate.now().plusDays(1));
            }
            case "refund" -> {
                futureRefunds(false);
                yield () -> assertEquals(2, ctx.getRentalService().changeVehicleRentalState(1001L, VehicleState.SALE));
            }
            default -> throw new IllegalArgumentException(flow);
        };
        List<RuntimeException> failures = race(movement,
                () -> ctx.getAuthService().topUpClientBalance(1L, new BigDecimal("200")));

        assertTrue(failures.isEmpty(), failures.toString());
        balance(1L, expected);
        switch (flow) {
            case "deposit" -> assertEquals(1, ctx.getOperationQueryService().getClientRequests(1L).size());
            case "completion", "acquisition", "expiration" -> {
                assertEquals(1, ctx.getOperationQueryService().getAllSales().size());
                assertTrue(ctx.getOperationQueryService().getClientRequests(1L).isEmpty());
            }
            case "rejection" -> {
                assertTrue(ctx.getOperationQueryService().getClientRequests(1L).isEmpty());
                assertEquals(VehicleState.SALE, vehicleState(1001L));
            }
            case "rental" -> assertEquals(1, ctx.getOperationQueryService().getClientRents(1L).size());
            case "refund" -> {
                assertTrue(ctx.getOperationQueryService().getClientRents(1L).isEmpty());
                balance(2L, "1200.00");
            }
        }
    }

    @Test
    void concurrentRefundsAcquireAccountsInClientOrder() throws Exception {
        futureRefunds(true);
        List<RuntimeException> failures = race(
                () -> assertEquals(2, ctx.getRentalService().changeVehicleRentalState(1001L, VehicleState.SALE)),
                () -> assertEquals(2, ctx.getRentalService().changeVehicleRentalState(1002L, VehicleState.SALE)),
                () -> {
                    // Mentre il primo rimborso attende il conto 1, non deve già possedere il conto 2.
                    assertDoesNotThrow(() -> {
                        try (var probe = db.getConnection(); var statement = probe.createStatement()) {
                            statement.setQueryTimeout(10);
                            statement.executeQuery("SELECT client FROM bank_account WHERE client = 2 FOR UPDATE NOWAIT").close();
                        }
                    });
                });

        assertTrue(failures.isEmpty(), failures.toString());
        balance(1L, "1400.00");
        balance(2L, "1600.00");
        assertTrue(ctx.getOperationQueryService().getClientRents(1L).isEmpty());
        assertTrue(ctx.getOperationQueryService().getClientRents(2L).isEmpty());
        assertEquals(VehicleState.SALE, vehicleState(1001L));
        assertEquals(VehicleState.SALE, vehicleState(1002L));
    }

    @Test
    void missingRefundAccountRollsBackEveryRefundAndCancellation() throws Exception {
        futureRefunds(false);
        sql("DELETE FROM bank_account WHERE client = 2");

        assertThrows(RuntimeException.class,
                () -> ctx.getRentalService().changeVehicleRentalState(1001L, VehicleState.SALE));
        balance(1L, "1000.00");
        assertEquals(1, ctx.getOperationQueryService().getClientRents(1L).size());
        assertEquals(1, ctx.getOperationQueryService().getClientRents(2L).size());
        assertEquals(VehicleState.RENT, vehicleState(1001L));
    }

    @Test
    void multipleRefundsToOneAccountKeepEveryCredit() throws Exception {
        futureRefunds(false);
        sql("UPDATE rent SET client = 1 WHERE vehicle = 1001");
        List<RuntimeException> failures = race(
                () -> assertEquals(2, ctx.getRentalService().changeVehicleRentalState(1001L, VehicleState.SALE)),
                () -> ctx.getAuthService().topUpClientBalance(1L, new BigDecimal("200")));

        assertTrue(failures.isEmpty(), failures.toString());
        balance(1L, "1500.00");
        assertTrue(ctx.getOperationQueryService().getClientRents(1L).isEmpty());
        assertEquals(VehicleState.SALE, vehicleState(1001L));
    }

    @Test
    void errorAfterMultipleRefundsRollsBackAllAccountsAndCancellations() throws Exception {
        futureRefunds(false);
        sql("""
                CREATE FUNCTION fail_ticket04_vehicle() RETURNS trigger LANGUAGE plpgsql AS $$
                BEGIN
                  IF (SELECT balance FROM bank_account WHERE client = 1) <> 1100
                     OR (SELECT balance FROM bank_account WHERE client = 2) <> 1200
                     OR EXISTS (SELECT 1 FROM rent WHERE vehicle = NEW.id) THEN
                    RAISE EXCEPTION 'ticket04_missing_refunds';
                  END IF;
                  RAISE EXCEPTION 'ticket04_after_refunds';
                END $$;
                CREATE TRIGGER fail_ticket04_vehicle BEFORE UPDATE ON vehicle
                  FOR EACH ROW EXECUTE FUNCTION fail_ticket04_vehicle();
                """);

        RuntimeException failure = assertThrows(RuntimeException.class,
                () -> ctx.getRentalService().changeVehicleRentalState(1001L, VehicleState.SALE));
        assertTrue(rootMessage(failure).contains("ticket04_after_refunds"), failure.toString());
        balance(1L, "1000.00");
        balance(2L, "1000.00");
        assertEquals(1, ctx.getOperationQueryService().getClientRents(1L).size());
        assertEquals(1, ctx.getOperationQueryService().getClientRents(2L).size());
        assertEquals(VehicleState.RENT, vehicleState(1001L));
    }

    private void futureRefunds(boolean bothVehicles) throws Exception {
        sql("""
                UPDATE vehicle SET state = 'RENT' WHERE id IN (1001, 1002);
                INSERT INTO rent (vehicle, client, amount, start, "end") VALUES
                  (1001, 2, 200, CURRENT_DATE + 1, CURRENT_DATE + 1),
                  (1001, 1, 100, CURRENT_DATE + 2, CURRENT_DATE + 2);
                """);
        if (bothVehicles) sql("""
                INSERT INTO rent (vehicle, client, amount, start, "end") VALUES
                  (1002, 1, 300, CURRENT_DATE + 1, CURRENT_DATE + 1),
                  (1002, 2, 400, CURRENT_DATE + 2, CURRENT_DATE + 2);
                """);
    }

    private VehicleState vehicleState(long id) {
        return ctx.getCatalogService().getAllVehicles().stream().filter(v -> v.getId() == id)
                .findFirst().orElseThrow().getState();
    }

    private static String rootMessage(Throwable failure) {
        while (failure.getCause() != null) failure = failure.getCause();
        return failure.getMessage();
    }

    private void balance(long clientId, String expected) {
        assertEquals(new BigDecimal(expected), ctx.getAuthService().getClientBankAccount(clientId).getBalance());
    }

    private void sql(String sql) throws Exception {
        try (var conn = db.getConnection(); var statement = conn.createStatement()) {
            statement.setQueryTimeout(10);
            statement.execute(sql);
        }
    }

    // Il gate blocca UPDATE prima della correzione e SELECT FOR UPDATE dopo: stessa gara reale nei due casi.
    private List<RuntimeException> race(Runnable first, Runnable second) throws Exception {
        return race(first, second, () -> {});
    }

    private List<RuntimeException> race(Runnable first, Runnable second, Runnable afterFirstBlocked) throws Exception {
        ExecutorService workers = Executors.newFixedThreadPool(2);
        try (Connection gate = db.getConnection(); Connection observer = db.getConnection()) {
            gate.setAutoCommit(false);
            try (var statement = gate.createStatement()) {
                statement.setQueryTimeout(10);
                statement.executeQuery("SELECT client FROM bank_account WHERE client = 1 FOR UPDATE").close();
            }
            Future<RuntimeException> a = workers.submit(outcome(first));
            try {
                awaitAccountWaiters(observer, 1);
                afterFirstBlocked.run();
                Future<RuntimeException> b = workers.submit(outcome(second));
                awaitAccountWaiters(observer, 2);
                gate.rollback();
                RuntimeException firstFailure = a.get(10, TimeUnit.SECONDS);
                RuntimeException secondFailure = b.get(10, TimeUnit.SECONDS);
                return java.util.stream.Stream.of(firstFailure, secondFailure).filter(e -> e != null).toList();
            } finally {
                gate.rollback();
                cancelAccountWaiters(observer);
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

    private static void awaitAccountWaiters(Connection observer, int expected) throws Exception {
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(10);
        try (var statement = observer.createStatement()) {
            statement.setQueryTimeout(10);
            do {
                try (var result = statement.executeQuery("""
                        SELECT count(DISTINCT pid) FROM pg_stat_activity
                        WHERE datname = current_database() AND usename = current_user
                          AND pid <> pg_backend_pid() AND state = 'active'
                          AND wait_event_type = 'Lock' AND query ILIKE '%bank_account%'
                          AND cardinality(pg_blocking_pids(pid)) > 0
                        """)) {
                    assertTrue(result.next());
                    if (result.getInt(1) == expected) return;
                }
            } while (System.nanoTime() < deadline);
        }
        fail("PostgreSQL non mostra " + expected + " connessioni distinte in competizione sul conto");
    }

    private static void cancelAccountWaiters(Connection observer) throws Exception {
        try (var statement = observer.createStatement()) {
            statement.setQueryTimeout(10);
            statement.executeQuery("""
                    SELECT pg_cancel_backend(pid) FROM pg_stat_activity
                    WHERE datname = current_database() AND usename = current_user
                      AND pid <> pg_backend_pid() AND state = 'active'
                      AND wait_event_type = 'Lock' AND query ILIKE '%bank_account%'
                    """).close();
        }
    }
}
