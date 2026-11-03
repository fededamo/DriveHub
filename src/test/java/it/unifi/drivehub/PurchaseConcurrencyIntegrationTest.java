package it.unifi.drivehub;

import it.unifi.drivehub.dao.DatabaseManager;
import it.unifi.drivehub.dao.postgres.PostgresRequestDao;
import it.unifi.drivehub.domain.operation.Request;
import it.unifi.drivehub.domain.vehicle.VehicleState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.Arguments;

import java.math.BigDecimal;
import java.sql.Connection;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

@Tag("postgres")
class PurchaseConcurrencyIntegrationTest {
    private DatabaseManager db;
    private AppContext ctx;

    @BeforeEach
    void setUp() throws Exception {
        db = TestDatabase.fromEnvironment().reset();
        ctx = new AppContext(db);
        sql("""
                DELETE FROM sale; DELETE FROM rent; DELETE FROM test_drive; DELETE FROM request;
                UPDATE bank_account SET balance = 50000 WHERE client = 1;
                INSERT INTO model (id, brand, name, release_price, release_year)
                  VALUES (1001, 'Fiat', 'Ticket07', 20000, EXTRACT(YEAR FROM CURRENT_DATE));
                INSERT INTO vehicle (id, model, vin, kilometers, price, year, state)
                  VALUES (1001, 1001, 'T0700000000000001', 0, 20000, EXTRACT(YEAR FROM CURRENT_DATE), 'SALE');
                """);
        var request = ctx.getPurchaseService().initiatePurchase(1001L, 1L);
        sql("UPDATE request SET id = 1001 WHERE id = " + request.getId());
        ctx.getPurchaseService().approvePurchase(1001L, 16L);
    }

    @Test
    void concurrentCompletionsChargeAndSellOnlyOnce() throws Exception {
        var failures = race(
                () -> new AppContext(db).getPurchaseService().completePurchase(1001L),
                () -> new AppContext(db).getPurchaseService().completePurchase(1001L));

        assertNull(failures.get(0));
        assertNotNull(failures.get(1), "La seconda sessione deve rileggere una richiesta già consumata");
        balance("30000.00");
        assertEquals(VehicleState.SOLD, ctx.getCatalogService().getVehicleById(1001L).orElseThrow().getState());
        assertTrue(ctx.getOperationQueryService().getClientRequests(1L).isEmpty());
        var sales = ctx.getOperationQueryService().getClientSales(1L);
        assertEquals(1, sales.size());
        assertEquals(new BigDecimal("20000.00"), sales.getFirst().getAmount());
    }

    @ParameterizedTest
    @CsvSource({"completion,rejection,30000.00,SOLD,20000.00", "rejection,completion,50000.00,SALE,0",
            "rejection,rejection,50000.00,SALE,0", "expiration,rejection,49200.00,SALE,800.00",
            "rejection,expiration,50000.00,SALE,0", "expiration,expiration,49200.00,SALE,800.00",
            "completion,approval,30000.00,SOLD,20000.00", "rejection,approval,50000.00,SALE,0"})
    void competingPhasesConsumeTheRequestOnce(String first, String second, String expectedBalance,
                                             VehicleState state, BigDecimal amount) throws Exception {
        if (first.equals("expiration") || second.equals("expiration")) {
            sql("UPDATE request SET exp_date = CURRENT_DATE - 1 WHERE id = 1001");
        }
        var notices = new AtomicInteger();
        var failures = race(operation(first, notices), operation(second, notices));
        assertNull(failures.get(0));
        if (second.equals("expiration")) assertNull(failures.get(1));
        else assertNotNull(failures.get(1));
        assertConsumed(expectedBalance, state, amount);
        assertEquals(first.equals("expiration") ? 1 : 0, notices.get());
        assertTrue(ctx.getPurchaseService().checkAndHandleExpiredRequests(1L).isEmpty());
        assertConsumed(expectedBalance, state, amount);
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void completionAndExpirationRecheckTheLockedDeadline(boolean expired) throws Exception {
        sql("UPDATE request SET exp_date = CURRENT_DATE " + (expired ? "- 1" : "") + " WHERE id = 1001");
        var notices = new AtomicInteger();
        var failures = race("SELECT id FROM request WHERE id = 1001 FOR UPDATE", false,
                operation("expiration", notices), operation("completion", notices));
        assertNull(failures.get(0));
        if (expired) assertNotNull(failures.get(1));
        else assertNull(failures.get(1));
        assertEquals(expired ? 1 : 0, notices.get());
        assertConsumed(expired ? "49200.00" : "30000.00", expired ? VehicleState.SALE : VehicleState.SOLD,
                new BigDecimal(expired ? "800.00" : "20000.00"));
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void staleExpirationCandidateCannotRefundACompletedOrExtendedRequest(boolean expirationFirst) throws Exception {
        sql("UPDATE request SET exp_date = CURRENT_DATE - 1 WHERE id = 1001");
        var notices = new AtomicInteger();
        var failures = race("UPDATE request SET exp_date = CURRENT_DATE WHERE id = 1001", true,
                operation(expirationFirst ? "expiration" : "completion", notices),
                operation(expirationFirst ? "completion" : "expiration", notices));
        assertNull(failures.get(0));
        assertNull(failures.get(1));
        assertEquals(0, notices.get());
        assertConsumed("30000.00", VehicleState.SOLD, new BigDecimal("20000.00"));
    }

    @ParameterizedTest
    @CsvSource({"NULL,NULL", "NULL,CURRENT_DATE - 1", "16,NULL"})
    void incompleteApprovalCannotEnableCompletionOrExpiration(String salesman, String deadline) throws Exception {
        sql("UPDATE request SET salesman = " + salesman + ", exp_date = " + deadline + " WHERE id = 1001");
        Request before = request();
        var failure = assertThrows(RuntimeException.class, () -> ctx.getPurchaseService().completePurchase(1001L));
        assertTrue(rootMessage(failure).contains("approvata"));
        assertTrue(ctx.getPurchaseService().checkAndHandleExpiredRequests(1L).isEmpty());
        assertPending(before, VehicleState.BLOCKED);
    }

    @ParameterizedTest
    @ValueSource(strings = {"completion", "rejection", "approval"})
    void purchaseCommandsRejectSaleRequests(String phase) throws Exception {
        sql("UPDATE request SET type = 'SALE', agreed_total = NULL, deposit = -4000, exp_date = CURRENT_DATE - 1 WHERE id = 1001");
        var failure = assertThrows(RuntimeException.class, operation(phase, new AtomicInteger())::run);
        assertTrue(rootMessage(failure).contains("non è di acquisto"));
        assertTrue(ctx.getPurchaseService().checkAndHandleExpiredRequests(1L).isEmpty());
        balance("46000.00");
        assertEquals(new BigDecimal("-4000.00"), request().getDeposit());
        assertTrue(ctx.getOperationQueryService().getAllSales().isEmpty());
        assertEquals(VehicleState.BLOCKED, ctx.getCatalogService().getVehicleById(1001L).orElseThrow().getState());
    }

    @ParameterizedTest
    @ValueSource(strings = {"completion", "rejection", "expiration"})
    void missingAccountPreventsEveryWrite(String phase) throws Exception {
        if (phase.equals("expiration")) sql("UPDATE request SET exp_date = CURRENT_DATE - 1 WHERE id = 1001");
        Request before = request();
        sql("DELETE FROM bank_account WHERE client = 1");
        var failure = assertThrows(RuntimeException.class, operation(phase, new AtomicInteger())::run);
        assertTrue(rootMessage(failure).contains("Conto cliente non trovato"));
        ctx = new AppContext(db);
        assertEquals(before.getDeposit(), request().getDeposit());
        assertEquals(before.getAgreedTotal(), request().getAgreedTotal());
        assertEquals(before.getSalesmanId(), request().getSalesmanId());
        assertEquals(before.getExpDate(), request().getExpDate());
        assertTrue(ctx.getOperationQueryService().getAllSales().isEmpty());
        assertEquals(VehicleState.BLOCKED, ctx.getCatalogService().getVehicleById(1001L).orElseThrow().getState());
    }

    @ParameterizedTest
    @CsvSource({"completion,SALE", "completion,SOLD", "completion,WAITING", "completion,RENT",
            "rejection,SALE", "rejection,SOLD", "rejection,WAITING", "rejection,RENT",
            "expiration,SALE", "expiration,SOLD", "expiration,WAITING", "expiration,RENT"})
    void inconsistentVehicleStateRollsBackTheWholePhase(String phase, VehicleState state) throws Exception {
        if (phase.equals("expiration")) sql("UPDATE request SET exp_date = CURRENT_DATE - 1 WHERE id = 1001");
        sql("UPDATE vehicle SET state = '" + state + "' WHERE id = 1001");
        Request before = request();
        var failure = assertThrows(RuntimeException.class, operation(phase, new AtomicInteger())::run);
        assertTrue(rootMessage(failure).contains("non è riservato"));
        assertPending(before, state);
    }

    @Test
    void expirationUsesTheRefundActuallyWrittenInTheNotification() throws Exception {
        sql("UPDATE request SET deposit = 0.03, agreed_total = 0.15, exp_date = CURRENT_DATE - 1 WHERE id = 1001");
        var notices = ctx.getPurchaseService().checkAndHandleExpiredRequests(1L);
        assertEquals(1, notices.size());
        assertTrue(notices.getFirst().contains("€ 0.02."));
        assertConsumed("46000.02", VehicleState.SALE, new BigDecimal("0.01"));
        assertTrue(ctx.getPurchaseService().checkAndHandleExpiredRequests(1L).isEmpty());
        assertConsumed("46000.02", VehicleState.SALE, new BigDecimal("0.01"));
    }

    @Test
    void requestLockRequiresAnActiveTransaction() throws Exception {
        try (var conn = db.getConnection()) {
            assertTrue(conn.getAutoCommit());
            assertThrows(IllegalArgumentException.class,
                    () -> new PostgresRequestDao(db).findByIdForUpdate(1001L, conn));
        }
        assertPending(request(), VehicleState.BLOCKED);
    }

    @ParameterizedTest
    @MethodSource("writeFailures")
    void failedOrSkippedWritesRollBackTheWholePhase(String phase, String table, String event,
                                                   String observedBalance, boolean skip) throws Exception {
        if (phase.equals("expiration")) sql("UPDATE request SET exp_date = CURRENT_DATE - 1 WHERE id = 1001");
        if (phase.equals("approval")) sql("UPDATE request SET salesman = NULL, exp_date = NULL WHERE id = 1001");
        Request before = request();
        sql("CREATE FUNCTION fail_ticket07() RETURNS trigger LANGUAGE plpgsql AS $$ BEGIN "
                + "IF (SELECT balance FROM bank_account WHERE client = 1) <> " + observedBalance
                + " THEN RAISE EXCEPTION 'ticket07_missing_movement'; END IF; "
                + (skip ? "RETURN NULL; " : "RAISE EXCEPTION 'ticket07_after_movement'; ")
                + "END $$; CREATE TRIGGER fail_ticket07 BEFORE " + event + " ON " + table
                + " FOR EACH ROW EXECUTE FUNCTION fail_ticket07()");
        var failure = assertThrows(RuntimeException.class, operation(phase, new AtomicInteger())::run);
        if (!skip) assertTrue(rootMessage(failure).contains("ticket07_after_movement"), rootMessage(failure));
        assertPending(before, VehicleState.BLOCKED);
    }

    static Stream<Arguments> writeFailures() {
        return Stream.of(
                new String[]{"completion", "bank_account", "UPDATE", "46000"},
                new String[]{"completion", "sale", "INSERT", "30000"},
                new String[]{"completion", "request", "DELETE", "30000"},
                new String[]{"completion", "vehicle", "UPDATE", "30000"},
                new String[]{"rejection", "bank_account", "UPDATE", "46000"},
                new String[]{"rejection", "request", "DELETE", "50000"},
                new String[]{"rejection", "vehicle", "UPDATE", "50000"},
                new String[]{"expiration", "bank_account", "UPDATE", "46000"},
                new String[]{"expiration", "sale", "INSERT", "49200"},
                new String[]{"expiration", "request", "DELETE", "49200"},
                new String[]{"expiration", "vehicle", "UPDATE", "49200"},
                new String[]{"approval", "request", "UPDATE", "46000"})
                .flatMap(row -> Stream.of(false, true).map(skip -> Arguments.of(row[0], row[1], row[2], row[3], skip)));
    }

    @AfterEach
    void removeFailureInjection() throws Exception {
        if (db != null) sql("DROP FUNCTION IF EXISTS fail_ticket07() CASCADE");
    }

    private Request request() {
        return ctx.getOperationQueryService().getClientRequests(1L).stream()
                .filter(r -> r.getId().equals(1001L)).findFirst().orElseThrow();
    }

    private void assertPending(Request before, VehicleState state) {
        ctx = new AppContext(db);
        balance("46000.00");
        Request reloaded = request();
        assertEquals(before.getDeposit(), reloaded.getDeposit());
        assertEquals(before.getAgreedTotal(), reloaded.getAgreedTotal());
        assertEquals(before.getSalesmanId(), reloaded.getSalesmanId());
        assertEquals(before.getExpDate(), reloaded.getExpDate());
        assertEquals(state, ctx.getCatalogService().getVehicleById(1001L).orElseThrow().getState());
        assertTrue(ctx.getOperationQueryService().getAllSales().isEmpty());
    }

    private static String rootMessage(Throwable failure) {
        while (failure.getCause() != null) failure = failure.getCause();
        return failure.getMessage();
    }

    private Runnable operation(String phase, AtomicInteger notices) {
        return () -> {
            var service = new AppContext(db).getPurchaseService();
            switch (phase) {
                case "completion" -> service.completePurchase(1001L);
                case "rejection" -> service.rejectPurchase(1001L);
                case "approval" -> service.approvePurchase(1001L, 16L);
                case "expiration" -> notices.addAndGet(service.checkAndHandleExpiredRequests(1L).size());
                default -> throw new IllegalArgumentException(phase);
            }
        };
    }

    private void assertConsumed(String expectedBalance, VehicleState state, BigDecimal amount) {
        ctx = new AppContext(db);
        balance(expectedBalance);
        assertEquals(state, ctx.getCatalogService().getVehicleById(1001L).orElseThrow().getState());
        assertTrue(ctx.getOperationQueryService().getClientRequests(1L).isEmpty());
        var sales = ctx.getOperationQueryService().getClientSales(1L);
        assertEquals(amount.signum() == 0 ? 0 : 1, sales.size());
        if (amount.signum() != 0) {
            assertEquals(0, amount.compareTo(sales.getFirst().getAmount()));
            if (state == VehicleState.SALE) {
                assertNull(sales.getFirst().getVehicleId());
                assertNull(sales.getFirst().getSalesmanId());
            } else {
                assertEquals(1001L, sales.getFirst().getVehicleId());
                assertEquals(16L, sales.getFirst().getSalesmanId());
            }
        }
    }

    // Il conto ferma la prima fase; la seconda attende il conto (RED) o la richiesta (GREEN).
    private List<RuntimeException> race(Runnable first, Runnable second) throws Exception {
        return race("SELECT client FROM bank_account WHERE client = 1 FOR UPDATE", false, first, second);
    }

    private List<RuntimeException> race(String gateQuery, boolean commitGate, Runnable first, Runnable second) throws Exception {
        var workers = Executors.newFixedThreadPool(2);
        try (Connection gate = db.getConnection(); Connection observer = db.getConnection()) {
            gate.setAutoCommit(false);
            try (var statement = gate.createStatement()) {
                statement.setQueryTimeout(10);
                statement.execute(gateQuery);
            }
            var a = workers.submit(outcome(first));
            try {
                awaitWaiters(observer, 1);
                var b = workers.submit(outcome(second));
                awaitWaiters(observer, 2);
                if (commitGate) gate.commit();
                else gate.rollback();
                return Arrays.asList(a.get(10, TimeUnit.SECONDS), b.get(10, TimeUnit.SECONDS));
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
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(10);
        try (var statement = observer.createStatement()) {
            statement.setQueryTimeout(10);
            do {
                try (var result = statement.executeQuery("""
                        SELECT count(DISTINCT pid) FROM pg_stat_activity
                        WHERE datname = current_database() AND usename = current_user
                          AND pid <> pg_backend_pid() AND state = 'active' AND wait_event_type = 'Lock'
                          AND (query ILIKE '%request%' OR query ILIKE '%bank_account%')
                          AND cardinality(pg_blocking_pids(pid)) > 0
                        """)) {
                    assertTrue(result.next());
                    if (result.getInt(1) == expected) return;
                }
            } while (System.nanoTime() < deadline);
        }
        fail("Attese PostgreSQL distinte mancanti: " + expected);
    }

    private void balance(String expected) {
        assertEquals(new BigDecimal(expected), ctx.getAuthService().getClientBankAccount(1L).getBalance());
    }

    private void sql(String query) throws Exception {
        try (var conn = db.getConnection(); var statement = conn.createStatement()) {
            statement.setQueryTimeout(10);
            statement.execute(query);
        }
    }
}
