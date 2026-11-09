package it.unifi.drivehub;

import it.unifi.drivehub.dao.DatabaseManager;
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

import java.sql.Connection;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

@Tag("postgres")
class SaleOutcomeIntegrationTest {
    private DatabaseManager db;
    private Request proposal;

    @BeforeEach
    void setUp() throws Exception {
        db = TestDatabase.fromEnvironment().reset();
        sql("""
                DELETE FROM sale; DELETE FROM rent; DELETE FROM test_drive; DELETE FROM request;
                UPDATE bank_account SET balance = 1000 WHERE client = 1;
                UPDATE bank_account SET balance = 2000 WHERE client = 2;
                INSERT INTO model (id, brand, name, release_price, release_year)
                VALUES (1001, 'Fiat', 'Ticket10', 20000, EXTRACT(YEAR FROM CURRENT_DATE));
                """);
        proposal = new AppContext(db).getSaleToDealershipService().submitSaleProposal(
                1L, 1001L, "T1000000000000001", "AB123CD", 0, java.time.Year.now().getValue());
    }

    @ParameterizedTest
    @CsvSource({"approval,SALE", "approval,SOLD", "approval,RENT", "approval,BLOCKED",
            "rejection,SALE", "rejection,SOLD", "rejection,RENT", "rejection,BLOCKED"})
    void onlyAWaitingVehicleCanReceiveAnOutcome(String phase, VehicleState state) throws Exception {
        sql("UPDATE vehicle SET state = '" + state + "' WHERE id = " + proposal.getVehicleId());
        String before = snapshot();
        assertThrows(RuntimeException.class, operation(phase)::run);
        assertEquals(before, snapshot());
    }

    @Test
    void approvalPaysTheClientAndRecordsOneDealershipExpense() throws Exception {
        var request = new AppContext(db).getOperationQueryService().getClientRequests(1L).getFirst();
        assertEquals(it.unifi.drivehub.domain.operation.RequestType.PURCHASE, request.getType());
        assertEquals(new BigDecimal("16000.00"), request.getDeposit());
        assertNull(request.getAgreedTotal());
        operation("approval").run();
        var reloaded = new AppContext(db);
        assertEquals(new BigDecimal("17000.00"), reloaded.getAuthService().getClientBankAccount(1L).getBalance());
        var sales = reloaded.getOperationQueryService().getAllSales();
        assertEquals(1, sales.size());
        var acquisition = sales.getFirst();
        assertEquals(new BigDecimal("16000.00"), acquisition.getAmount());
        assertEquals(it.unifi.drivehub.domain.operation.SaleType.PURCHASE, acquisition.getType());
        assertEquals(proposal.getVehicleId(), acquisition.getVehicleId());
        assertEquals(16L, acquisition.getSalesmanId());
        assertEquals(1L, acquisition.getClientId());
        assertEquals(sales, reloaded.getOperationQueryService().getClientSales(1L));
        assertTrue(reloaded.getOperationQueryService().getClientRequests(1L).isEmpty());
        assertEquals(VehicleState.SALE, reloaded.getCatalogService().getVehicleById(proposal.getVehicleId()).orElseThrow().getState());
        String after = snapshot();
        assertThrows(RuntimeException.class, operation("approval")::run);
        assertThrows(RuntimeException.class, operation("rejection")::run);
        assertEquals(after, snapshot());
    }

    @ParameterizedTest
    @ValueSource(strings = {"NONE", "SALE", "PURCHASE", "RENT", "TEST_DRIVE", "ALL"})
    void rejectionPreservesEveryKindOfHistoryAndRemovesOnlyANewVehicle(String kind) throws Exception {
        addHistory(kind);
        String history = snapshot("sale", "rent", "test_drive");
        operation("rejection").run();
        var reloaded = new AppContext(db);
        assertEquals(new BigDecimal("1000.00"), reloaded.getAuthService().getClientBankAccount(1L).getBalance());
        assertTrue(reloaded.getOperationQueryService().getClientRequests(1L).isEmpty());
        var vehicle = reloaded.getCatalogService().getVehicleById(proposal.getVehicleId());
        if (kind.equals("NONE")) {
            assertTrue(vehicle.isEmpty());
            assertTrue(reloaded.getCatalogService().getAllVehicles().stream().noneMatch(v -> v.getVin().equals("T1000000000000001")));
        } else {
            assertEquals(VehicleState.SOLD, vehicle.orElseThrow().getState());
        }
        assertEquals(history, snapshot("sale", "rent", "test_drive"));
        String after = snapshot();
        assertThrows(RuntimeException.class, operation("approval")::run);
        assertThrows(RuntimeException.class, operation("rejection")::run);
        assertEquals(after, snapshot());
    }

    @Test
    void aSkippedVehicleDeletionCannotConsumeTheRequest() throws Exception {
        String before = snapshot();
        sql("""
                CREATE FUNCTION ticket10_skip_delete() RETURNS trigger LANGUAGE plpgsql AS $$
                BEGIN RETURN NULL; END $$;
                CREATE TRIGGER ticket10_skip_delete BEFORE DELETE ON vehicle
                FOR EACH ROW EXECUTE FUNCTION ticket10_skip_delete();
                """);
        try {
            assertThrows(RuntimeException.class, operation("rejection")::run);
            assertEquals(before, snapshot());
        } finally {
            sql("DROP FUNCTION ticket10_skip_delete() CASCADE");
        }
    }

    @ParameterizedTest
    @CsvSource({"approval,approval,false", "approval,rejection,false", "rejection,approval,false", "rejection,rejection,false",
            "approval,approval,true", "approval,rejection,true", "rejection,approval,true", "rejection,rejection,true"})
    void competingOutcomesHaveExactlyOnePaymentAndAvailabilityChange(String first, String second, boolean history) throws Exception {
        if (history) addHistory("ALL");
        monitorVehicleWrites();
        var failures = race("SELECT id FROM vehicle WHERE id = " + proposal.getVehicleId() + " FOR UPDATE", false,
                operation(first), operation(second));
        assertNull(failures.get(0));
        assertNotNull(failures.get(1), "La sessione obsoleta deve rileggere la richiesta consumata");
        assertOutcome(first, history);
        try (var conn = db.getConnection(); var statement = conn.createStatement();
             var rows = statement.executeQuery("SELECT count(*) FROM ticket10_changes")) {
            assertTrue(rows.next());
            assertEquals(1, rows.getInt(1), "Una sola transizione o eliminazione del veicolo");
        }
        String after = snapshot();
        assertThrows(RuntimeException.class, operation(first)::run);
        assertThrows(RuntimeException.class, operation(second)::run);
        assertEquals(after, snapshot());
    }

    @ParameterizedTest
    @ValueSource(strings = {"approval", "rejection"})
    void anObsoleteSessionUsesTheRequestRereadAfterTheLock(String phase) throws Exception {
        var failures = race("UPDATE request SET client = 2, deposit = 123.45 WHERE id = " + proposal.getId(), true,
                operation(phase), null);
        assertNull(failures.getFirst());
        var ctx = new AppContext(db);
        assertEquals(new BigDecimal("1000.00"), ctx.getAuthService().getClientBankAccount(1L).getBalance());
        assertEquals(new BigDecimal(phase.equals("approval") ? "2123.45" : "2000.00"),
                ctx.getAuthService().getClientBankAccount(2L).getBalance());
        assertTrue(ctx.getOperationQueryService().getClientRequests(1L).isEmpty());
        assertTrue(ctx.getOperationQueryService().getClientRequests(2L).isEmpty());
        if (phase.equals("approval")) {
            var sale = ctx.getOperationQueryService().getAllSales().getFirst();
            assertEquals(2L, sale.getClientId());
            assertEquals(new BigDecimal("123.45"), sale.getAmount());
            assertEquals(it.unifi.drivehub.domain.operation.SaleType.PURCHASE, sale.getType());
        } else {
            assertTrue(ctx.getOperationQueryService().getAllSales().isEmpty());
        }
    }

    @ParameterizedTest
    @CsvSource({"approval,type", "rejection,type", "approval,state", "rejection,state"})
    void typeAndStateAreCheckedAfterWaitingForTheirLocks(String phase, String field) throws Exception {
        String change = field.equals("type")
                ? "UPDATE request SET type = 'SALE', deposit = 4000, agreed_total = 20000 WHERE id = " + proposal.getId()
                : "UPDATE vehicle SET state = 'SOLD' WHERE id = " + proposal.getVehicleId();
        var failures = race(change, true, operation(phase), null);
        assertNotNull(failures.getFirst());
        var ctx = new AppContext(db);
        assertEquals(new BigDecimal("1000.00"), ctx.getAuthService().getClientBankAccount(1L).getBalance());
        assertTrue(ctx.getOperationQueryService().getAllSales().isEmpty());
        assertEquals(1, ctx.getOperationQueryService().getClientRequests(1L).size());
        assertEquals(field.equals("type") ? VehicleState.WAITING : VehicleState.SOLD,
                ctx.getCatalogService().getVehicleById(proposal.getVehicleId()).orElseThrow().getState());
    }

    @Test
    void rejectionReadsHistoryWrittenInItsOwnTransaction() throws Exception {
        sql("""
                CREATE FUNCTION ticket10_history() RETURNS trigger LANGUAGE plpgsql AS $$
                BEGIN
                  INSERT INTO sale (vehicle,salesman,client,amount,date,type)
                  VALUES (OLD.vehicle,16,OLD.client,500,CURRENT_TIMESTAMP,'PURCHASE');
                  RETURN OLD;
                END $$;
                CREATE TRIGGER ticket10_history AFTER DELETE ON request
                FOR EACH ROW EXECUTE FUNCTION ticket10_history();
                """);
        operation("rejection").run();
        var ctx = new AppContext(db);
        assertEquals(VehicleState.SOLD, ctx.getCatalogService().getVehicleById(proposal.getVehicleId()).orElseThrow().getState());
        assertEquals(proposal.getVehicleId(), ctx.getOperationQueryService().getAllSales().getFirst().getVehicleId());
        assertEquals(new BigDecimal("1000.00"), ctx.getAuthService().getClientBankAccount(1L).getBalance());
    }

    @Test
    void approvalNeedsASalesmanBeforePaying() throws Exception {
        String before = snapshot();
        assertThrows(RuntimeException.class,
                () -> new AppContext(db).getSaleToDealershipService().approveSaleProposal(proposal.getId(), null));
        assertEquals(before, snapshot());
    }

    @ParameterizedTest
    @CsvSource({"SALE,-0.01", "PURCHASE,-0.01", "PENALTY,-0.01"})
    void databaseRejectsNegativeAmountsForEveryType(String type, BigDecimal amount) throws Exception {
        String before = snapshot();
        var failure = assertThrows(java.sql.SQLException.class, () -> sql(
                "INSERT INTO sale (vehicle,salesman,client,amount,date,type) VALUES (" + proposal.getVehicleId()
                        + ",16,1," + amount + ",CURRENT_DATE,'" + type + "')"));
        assertEquals("23514", failure.getSQLState());
        assertEquals(before, snapshot());
    }

    @ParameterizedTest
    @MethodSource("writeFailures")
    void errorsAndSkippedWritesNeverLeaveAPartialOutcome(String phase, String table, String event,
                                                       boolean history, String balance, boolean skip) throws Exception {
        if (history) addHistory("ALL");
        String before = snapshot();
        sql("CREATE FUNCTION ticket10_fault() RETURNS trigger LANGUAGE plpgsql AS $$ BEGIN "
                + "IF (SELECT balance FROM bank_account WHERE client=1) <> " + balance
                + " THEN RAISE EXCEPTION 'ticket10_movement_not_observed'; END IF; "
                + (phase.equals("approval") && (table.equals("vehicle") || table.equals("request"))
                    ? "IF NOT EXISTS (SELECT 1 FROM sale WHERE vehicle=" + proposal.getVehicleId()
                        + " AND amount=16000 AND type='PURCHASE') THEN RAISE EXCEPTION 'ticket10_acquisition_not_observed'; END IF; " : "")
                + (phase.equals("rejection") && table.equals("vehicle")
                    ? "IF EXISTS (SELECT 1 FROM request WHERE id=" + proposal.getId()
                        + ") THEN RAISE EXCEPTION 'ticket10_deletion_not_observed'; END IF; " : "")
                + (skip ? "RETURN NULL; " : "RAISE EXCEPTION 'ticket10_after_prior_writes'; ")
                + "END $$; CREATE TRIGGER ticket10_fault BEFORE " + event + " ON " + table
                + " FOR EACH ROW EXECUTE FUNCTION ticket10_fault()");
        RuntimeException failure = assertThrows(RuntimeException.class, operation(phase)::run);
        if (!skip) assertTrue(rootMessage(failure).contains("ticket10_after_prior_writes"), rootMessage(failure));
        assertEquals(before, snapshot());
        assertEquals(VehicleState.WAITING, new AppContext(db).getCatalogService()
                .getVehicleById(proposal.getVehicleId()).orElseThrow().getState());
        assertEquals(1, new AppContext(db).getOperationQueryService().getClientRequests(1L).size());
    }

    static Stream<Arguments> writeFailures() {
        return Stream.of(
                new String[]{"approval", "bank_account", "UPDATE", "false", "1000"},
                new String[]{"approval", "sale", "INSERT", "false", "17000"},
                new String[]{"approval", "vehicle", "UPDATE", "false", "17000"},
                new String[]{"approval", "request", "DELETE", "false", "17000"},
                new String[]{"rejection", "request", "DELETE", "false", "1000"},
                new String[]{"rejection", "vehicle", "DELETE", "false", "1000"},
                new String[]{"rejection", "vehicle", "UPDATE", "true", "1000"})
                .flatMap(row -> Stream.of(false, true).map(skip -> Arguments.of(
                        row[0], row[1], row[2], Boolean.parseBoolean(row[3]), row[4], skip)));
    }

    @ParameterizedTest
    @ValueSource(strings = {"approval", "rejection-new", "rejection-history"})
    void failureAfterTheFinalWriteRollsBackEveryActualEffect(String phase) throws Exception {
        boolean approved = phase.equals("approval");
        boolean history = phase.equals("rejection-history");
        if (history) addHistory("ALL");
        String before = snapshot();
        String table = approved ? "request" : "vehicle";
        String event = history ? "UPDATE" : "DELETE";
        sql("CREATE FUNCTION ticket10_fault() RETURNS trigger LANGUAGE plpgsql AS $$ BEGIN "
                + "IF EXISTS (SELECT 1 FROM request WHERE id=" + proposal.getId()
                + ") THEN RAISE EXCEPTION 'ticket10_request_not_consumed'; END IF; "
                + "IF (SELECT balance FROM bank_account WHERE client=1) <> " + (approved ? 17000 : 1000)
                + " THEN RAISE EXCEPTION 'ticket10_wrong_balance'; END IF; "
                + (approved ? "IF NOT EXISTS (SELECT 1 FROM sale WHERE vehicle=" + proposal.getVehicleId()
                        + " AND amount=16000 AND type='PURCHASE') THEN RAISE EXCEPTION 'ticket10_missing_acquisition'; END IF; " : "")
                + (approved || history ? "IF NOT EXISTS (SELECT 1 FROM vehicle WHERE id=" + proposal.getVehicleId()
                        + " AND state='" + (approved ? "SALE" : "SOLD") + "') THEN RAISE EXCEPTION 'ticket10_wrong_state'; END IF; "
                        : "IF EXISTS (SELECT 1 FROM vehicle WHERE id=" + proposal.getVehicleId()
                        + ") THEN RAISE EXCEPTION 'ticket10_vehicle_not_deleted'; END IF; ")
                + "RAISE EXCEPTION 'ticket10_all_effects_observed_then_failed'; END $$; "
                + "CREATE TRIGGER ticket10_fault AFTER " + event + " ON " + table
                + " FOR EACH ROW EXECUTE FUNCTION ticket10_fault()");
        RuntimeException failure = assertThrows(RuntimeException.class, operation(approved ? "approval" : "rejection")::run);
        assertTrue(rootMessage(failure).contains("ticket10_all_effects_observed_then_failed"), rootMessage(failure));
        assertEquals(before, snapshot());
        var ctx = new AppContext(db);
        assertEquals(new BigDecimal("1000.00"), ctx.getAuthService().getClientBankAccount(1L).getBalance());
        assertEquals(VehicleState.WAITING, ctx.getCatalogService().getVehicleById(proposal.getVehicleId()).orElseThrow().getState());
    }

    @ParameterizedTest
    @ValueSource(strings = {"account", "vehicle", "request", "salesman", "overflow", "type"})
    void missingOrInvalidDataCannotLeaveAnyWrite(String missing) throws Exception {
        switch (missing) {
            case "account" -> sql("DELETE FROM bank_account WHERE client=1");
            case "vehicle" -> sql("ALTER TABLE request DROP CONSTRAINT fk_request_vehicle; DELETE FROM vehicle WHERE id=" + proposal.getVehicleId());
            case "request" -> sql("DELETE FROM request WHERE id=" + proposal.getId());
            case "overflow" -> sql("UPDATE bank_account SET balance=99999999.99 WHERE client=1");
            case "type" -> sql("UPDATE request SET type='SALE', deposit=4000, agreed_total=20000 WHERE id=" + proposal.getId());
            case "salesman" -> { }
            default -> throw new IllegalArgumentException(missing);
        }
        String before = snapshot();
        assertThrows(RuntimeException.class, () -> new AppContext(db).getSaleToDealershipService()
                .approveSaleProposal(proposal.getId(), missing.equals("salesman") ? 999999L : 16L));
        assertEquals(before, snapshot());
        if (missing.equals("vehicle") || missing.equals("request") || missing.equals("type")) {
            assertThrows(RuntimeException.class, operation("rejection")::run);
            assertEquals(before, snapshot());
        }
    }

    @Test
    void zeroPaymentCannotConsumeTheProposalOrWriteAnAcquisition() throws Exception {
        sql("UPDATE request SET deposit=0 WHERE id=" + proposal.getId());
        String before = snapshot();
        assertThrows(RuntimeException.class, operation("approval")::run);
        assertEquals(before, snapshot());
        var ctx = new AppContext(db);
        assertTrue(ctx.getOperationQueryService().getAllSales().isEmpty());
        assertEquals(new BigDecimal("1000.00"), ctx.getAuthService().getClientBankAccount(1L).getBalance());
    }

    @Test
    void vehicleLockRequiresAnActiveTransaction() throws Exception {
        try (var conn = db.getConnection()) {
            assertThrows(IllegalArgumentException.class, () -> new it.unifi.drivehub.dao.postgres.PostgresVehicleDao(db)
                    .findByIdForUpdate(proposal.getVehicleId(), conn));
        }
    }

    private static String rootMessage(Throwable failure) {
        while (failure.getCause() != null) failure = failure.getCause();
        return failure.getMessage();
    }

    private void assertOutcome(String first, boolean history) {
        var ctx = new AppContext(db);
        boolean approved = first.equals("approval");
        assertEquals(new BigDecimal(approved ? "17000.00" : "1000.00"), ctx.getAuthService().getClientBankAccount(1L).getBalance());
        assertTrue(ctx.getOperationQueryService().getClientRequests(1L).isEmpty());
        var vehicle = ctx.getCatalogService().getVehicleById(proposal.getVehicleId());
        if (approved || history) assertEquals(approved ? VehicleState.SALE : VehicleState.SOLD, vehicle.orElseThrow().getState());
        else assertTrue(vehicle.isEmpty());
        var sales = ctx.getOperationQueryService().getAllSales();
        assertEquals((history ? 2 : 0) + (approved ? 1 : 0), sales.size());
        assertEquals(approved ? 1 : 0, sales.stream().filter(s -> s.getType() == it.unifi.drivehub.domain.operation.SaleType.PURCHASE
                && s.getAmount().equals(new BigDecimal("16000.00"))).count());
        assertEquals(history ? 1 : 0, ctx.getOperationQueryService().getClientRents(2L).size());
        assertEquals(history ? 1 : 0, ctx.getOperationQueryService().getClientTestDrives(2L).size());
    }

    @ParameterizedTest
    @CsvSource({"SALE,0", "PURCHASE,0", "PENALTY,0", "SALE,123.45", "PURCHASE,123.45", "PENALTY,123.45"})
    void nonNegativeRegisterAmountsKeepTheirExplicitTypeAfterReload(
            it.unifi.drivehub.domain.operation.SaleType type, BigDecimal amount) {
        var dao = new it.unifi.drivehub.dao.postgres.PostgresSaleDao(db);
        var record = it.unifi.drivehub.domain.operation.Sale.create(null,
                type == it.unifi.drivehub.domain.operation.SaleType.PENALTY ? null : proposal.getVehicleId(),
                type == it.unifi.drivehub.domain.operation.SaleType.PENALTY ? null : 16L,
                1L, amount, java.time.LocalDateTime.now(), type);
        var saved = db.executeInTransactionWithResult(conn -> dao.save(record, conn));
        var reloaded = dao.findById(saved.getId()).orElseThrow();
        assertEquals(type, reloaded.getType());
        assertEquals(0, amount.compareTo(reloaded.getAmount()));
        assertEquals(java.util.List.of(reloaded), new AppContext(db).getOperationQueryService().getClientSales(1L));
    }

    @Test
    void databaseRejectsTheRemovedAcquisitionType() throws Exception {
        String before = snapshot();
        var failure = assertThrows(java.sql.SQLException.class, () -> sql(
                "INSERT INTO sale (client,amount,date,type) VALUES (1,0,CURRENT_DATE,'ACQUISITION')"));
        assertEquals("22P02", failure.getSQLState());
        assertEquals(before, snapshot());
    }

    private void monitorVehicleWrites() throws Exception {
        sql("CREATE TABLE ticket10_changes (state text); "
                + "CREATE FUNCTION ticket10_monitor() RETURNS trigger LANGUAGE plpgsql AS $$ BEGIN "
                + "IF OLD.id = " + proposal.getVehicleId() + " THEN "
                + "INSERT INTO ticket10_changes VALUES (TG_OP); END IF; RETURN NULL; END $$; "
                + "CREATE TRIGGER ticket10_monitor AFTER UPDATE OR DELETE ON vehicle "
                + "FOR EACH ROW EXECUTE FUNCTION ticket10_monitor()");
    }

    // Il primo worker attende il gate; il secondo deve risultare bloccato prima del rilascio.
    private List<RuntimeException> race(String gateQuery, boolean commit, Runnable first, Runnable second) throws Exception {
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
                var b = second == null ? null : workers.submit(outcome(second));
                if (b != null) awaitWaiters(observer, 2);
                if (commit) gate.commit();
                else gate.rollback();
                return Arrays.asList(a.get(10, TimeUnit.SECONDS), b == null ? null : b.get(10, TimeUnit.SECONDS));
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
                try (var rows = statement.executeQuery("""
                        SELECT count(DISTINCT pid), string_agg(pid::text || ':' || pg_blocking_pids(pid)::text, ', ')
                        FROM pg_stat_activity
                        WHERE datname = current_database() AND usename = current_user
                          AND pid <> pg_backend_pid() AND state = 'active' AND wait_event_type = 'Lock'
                          AND cardinality(pg_blocking_pids(pid)) > 0
                        """)) {
                    assertTrue(rows.next());
                    if (rows.getInt(1) == expected) {
                        System.out.println("Ticket10 waiters " + expected + " pid:blockers " + rows.getString(2));
                        return;
                    }
                }
            } while (System.nanoTime() < deadline);
        }
        fail("Attese PostgreSQL distinte mancanti: " + expected);
    }

    @AfterEach
    void removeFaults() throws Exception {
        if (db != null) sql("DROP FUNCTION IF EXISTS ticket10_history() CASCADE; "
                + "DROP FUNCTION IF EXISTS ticket10_monitor() CASCADE; DROP TABLE IF EXISTS ticket10_changes; "
                + "DROP FUNCTION IF EXISTS ticket10_fault() CASCADE");
    }

    private void addHistory(String kind) throws Exception {
        long id = proposal.getVehicleId();
        if (kind.equals("SALE") || kind.equals("ALL")) {
            sql("INSERT INTO sale (vehicle,salesman,client,amount,date,type) VALUES (" + id
                    + ",16,1,20000,CURRENT_DATE - INTERVAL '3 days','SALE')");
        }
        if (kind.equals("PURCHASE") || kind.equals("ALL")) {
            sql("INSERT INTO sale (vehicle,salesman,client,amount,date,type) VALUES (" + id
                    + ",16,2,15000,CURRENT_DATE - INTERVAL '2 days','PURCHASE')");
        }
        if (kind.equals("RENT") || kind.equals("ALL")) {
            sql("INSERT INTO rent (vehicle,client,amount,start,\"end\") VALUES (" + id + ",2,100,CURRENT_DATE-5,CURRENT_DATE-4)");
        }
        if (kind.equals("TEST_DRIVE") || kind.equals("ALL")) {
            sql("INSERT INTO test_drive (vehicle,client,date) VALUES (" + id + ",2,CURRENT_DATE - INTERVAL '6 days' + TIME '10:00')");
        }
    }

    private Runnable operation(String phase) {
        var service = new AppContext(db).getSaleToDealershipService();
        return switch (phase) {
            case "approval" -> () -> service.approveSaleProposal(proposal.getId(), 16L);
            case "rejection" -> () -> service.rejectSaleProposal(proposal.getId());
            default -> throw new IllegalArgumentException(phase);
        };
    }

    private String snapshot(String... tables) throws Exception {
        StringBuilder result = new StringBuilder();
        try (Connection conn = db.getConnection(); var statement = conn.createStatement()) {
            statement.setQueryTimeout(10);
            for (String table : tables.length == 0 ? new String[]{"vehicle", "request", "sale", "bank_account", "rent", "test_drive"} : tables) {
                try (var rows = statement.executeQuery("SELECT row_to_json(t)::text FROM " + table + " t ORDER BY 1")) {
                    result.append(table);
                    while (rows.next()) result.append(rows.getString(1));
                }
            }
        }
        return result.toString();
    }

    private void sql(String query) throws Exception {
        try (var conn = db.getConnection(); var statement = conn.createStatement()) {
            statement.setQueryTimeout(10);
            statement.execute(query);
        }
    }
}
