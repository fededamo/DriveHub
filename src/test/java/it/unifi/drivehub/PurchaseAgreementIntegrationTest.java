package it.unifi.drivehub;

import it.unifi.drivehub.domain.operation.Request;
import it.unifi.drivehub.domain.operation.RequestType;
import it.unifi.drivehub.dao.postgres.PostgresRequestDao;
import it.unifi.drivehub.domain.vehicle.Vehicle;
import it.unifi.drivehub.domain.vehicle.VehicleState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigDecimal;
import java.time.Year;

import static org.junit.jupiter.api.Assertions.*;

@Tag("postgres")
class PurchaseAgreementIntegrationTest {
    private AppContext ctx;
    private Vehicle vehicle;

    @BeforeEach
    void setUp() throws Exception {
        ctx = new AppContext(TestDatabase.fromEnvironment().reset());
        try (var conn = ctx.getDatabaseManager().getConnection(); var sql = conn.createStatement()) {
            sql.execute("DELETE FROM sale; DELETE FROM rent; DELETE FROM test_drive; DELETE FROM request;"
                    + "UPDATE bank_account SET balance = 30000 WHERE client = 1");
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void completionKeepsAgreementAfterPricingChangesAndNewSession(boolean manual) throws Exception {
        int year = Year.now().getValue();
        var catalog = ctx.getCatalogService();
        var model = catalog.addModel("Fiat", "Ticket06", new BigDecimal("20000"), year);
        vehicle = catalog.addVehicle(model.getId(), "T0600000000000001", null, 0,
                manual ? new BigDecimal("20000") : null, year, VehicleState.SALE);
        Request request = ctx.getPurchaseService().initiatePurchase(vehicle.getId(), 1L);
        assertEquals(RequestType.SALE, request.getType());
        assertEquals(new BigDecimal("4000.00"), request.getDeposit());
        assertEquals(new BigDecimal("20000.00"), request.getAgreedTotal());
        assertEquals(new BigDecimal("16000.00"), request.getRemainingAmount());
        assertEquals(VehicleState.BLOCKED, ctx.getCatalogService().getVehicleById(vehicle.getId()).orElseThrow().getState());
        assertThrows(RuntimeException.class, () -> ctx.getPurchaseService().initiatePurchase(vehicle.getId(), 2L));
        assertEquals(1, ctx.getOperationQueryService().getClientRequests(1L).size());
        assertEquals(new BigDecimal("26000.00"), ctx.getAuthService().getClientBankAccount(1L).getBalance());
        ctx.getPurchaseService().approvePurchase(request.getId(), 16L);

        try (var conn = ctx.getDatabaseManager().getConnection();
             var sql = conn.prepareStatement("UPDATE model SET release_price = 30000 WHERE id = ?")) {
            sql.setLong(1, model.getId());
            assertEquals(1, sql.executeUpdate());
        }
        catalog.updateVehiclePricing(vehicle.getId(), manual ? null : new BigDecimal("35000"));
        catalog.setModelDiscount(model.getId(), 10);
        catalog.setVehicleDiscount(vehicle.getId(), 20);
        ctx = new AppContext(ctx.getDatabaseManager());
        assertNotEquals(new BigDecimal("20000.00"), ctx.getCatalogService().getEffectiveVehiclePrice(
                ctx.getCatalogService().getVehicleById(vehicle.getId()).orElseThrow()));
        Request reloaded = request(request.getId());
        assertEquals(RequestType.SALE, reloaded.getType());
        assertEquals(new BigDecimal("20000.00"), reloaded.getAgreedTotal());
        assertEquals(new BigDecimal("4000.00"), reloaded.getDeposit());
        assertEquals(new BigDecimal("16000.00"), reloaded.getRemainingAmount());
        ctx.getPurchaseService().completePurchase(request.getId());
        var sale = ctx.getOperationQueryService().getClientSales(1L).stream()
                .filter(s -> s.getVehicleId().equals(vehicle.getId())).findFirst().orElseThrow();
        assertEquals(it.unifi.drivehub.domain.operation.SaleType.SALE, sale.getType());
        assertEquals(new BigDecimal("20000.00"), sale.getAmount());
        assertEquals(new BigDecimal("10000.00"), ctx.getAuthService().getClientBankAccount(1L).getBalance());
        assertEquals(VehicleState.SOLD, ctx.getCatalogService().getVehicleById(vehicle.getId()).orElseThrow().getState());
        assertTrue(ctx.getOperationQueryService().getClientRequests(1L).stream()
                .noneMatch(r -> r.getId().equals(request.getId())));
    }

    @AfterEach
    void removeFailureInjection() throws Exception {
        if (ctx != null) sql("DROP FUNCTION IF EXISTS fail_ticket06() CASCADE");
    }

    @Test
    void fractionalCentManualBaseKeepsRoundedAgreementAfterReload() {
        createVehicle(true, Year.now().getValue());
        ctx.getCatalogService().updateVehiclePricing(vehicle.getId(), new BigDecimal("20000.025"));
        Request request = ctx.getPurchaseService().initiatePurchase(vehicle.getId(), 1L);
        assertEquals(new BigDecimal("20000.03"), request.getAgreedTotal());
        assertEquals(new BigDecimal("4000.01"), request.getDeposit());
        ctx.getPurchaseService().approvePurchase(request.getId(), 16L);
        ctx.getCatalogService().updateVehiclePricing(vehicle.getId(), new BigDecimal("10000.005"));
        ctx = new AppContext(ctx.getDatabaseManager());
        assertEquals(new BigDecimal("16000.02"), request(request.getId()).getRemainingAmount());
        ctx.getPurchaseService().completePurchase(request.getId());
        assertEquals(new BigDecimal("20000.03"), ctx.getOperationQueryService().getClientSales(1L).getFirst().getAmount());
        balance("9999.97");
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void agreementFromEarlierPricingYearDoesNotDepreciateAtCompletion(boolean manual) throws Exception {
        createVehicle(manual, Year.now().getValue() - 1);
        // Accordo concluso quando il veicolo era nuovo: nessun clock di sistema viene modificato.
        sql("UPDATE vehicle SET state = 'BLOCKED' WHERE id = " + vehicle.getId());
        sql("UPDATE bank_account SET balance = 26000 WHERE client = 1");
        sql("INSERT INTO request (id, vehicle, client, salesman, deposit, agreed_total, exp_date, type) VALUES (1001, "
                + vehicle.getId() + ", 1, 16, 4000, 20000, CURRENT_DATE + 30, 'SALE')");
        ctx = new AppContext(ctx.getDatabaseManager());
        assertEquals(new BigDecimal("19000.00"), ctx.getCatalogService().getEffectiveVehiclePrice(vehicle));
        assertEquals(new BigDecimal("16000.00"), request(1001L).getRemainingAmount());
        ctx.getPurchaseService().completePurchase(1001L);
        assertEquals(new BigDecimal("20000.00"), ctx.getOperationQueryService().getClientSales(1L).getFirst().getAmount());
        balance("10000.00");
    }

    @Test
    void insufficientDepositLeavesVehicleAccountAndOperationsUnchanged() throws Exception {
        createVehicle(false, Year.now().getValue());
        sql("UPDATE bank_account SET balance = 3999.99 WHERE client = 1");
        RuntimeException failure = assertThrows(RuntimeException.class,
                () -> ctx.getPurchaseService().initiatePurchase(vehicle.getId(), 1L));
        assertTrue(rootMessage(failure).contains("Saldo insufficiente"));
        balance("3999.99");
        assertEquals(VehicleState.SALE, ctx.getCatalogService().getVehicleById(vehicle.getId()).orElseThrow().getState());
        assertTrue(ctx.getOperationQueryService().getClientRequests(1L).isEmpty());
        assertTrue(ctx.getOperationQueryService().getAllSales().isEmpty());
    }

    @Test
    void insufficientRemainingFundsLeaveAgreementAndReservationUnchanged() throws Exception {
        Request request = approvedPurchase();
        sql("UPDATE bank_account SET balance = 15999.99 WHERE client = 1");
        RuntimeException failure = assertThrows(RuntimeException.class,
                () -> ctx.getPurchaseService().completePurchase(request.getId()));
        assertTrue(rootMessage(failure).contains("Saldo insufficiente"));
        balance("15999.99");
        assertPendingAgreement(request.getId());
    }

    @Test
    void completionStillRequiresApprovalAndUnexpiredRequest() throws Exception {
        createVehicle(false, Year.now().getValue());
        Request request = ctx.getPurchaseService().initiatePurchase(vehicle.getId(), 1L);
        RuntimeException unapproved = assertThrows(RuntimeException.class,
                () -> ctx.getPurchaseService().completePurchase(request.getId()));
        assertTrue(rootMessage(unapproved).contains("approvata"));
        ctx.getPurchaseService().approvePurchase(request.getId(), 16L);
        sql("UPDATE request SET exp_date = CURRENT_DATE - 1 WHERE id = " + request.getId());
        RuntimeException expired = assertThrows(RuntimeException.class,
                () -> ctx.getPurchaseService().completePurchase(request.getId()));
        assertTrue(rootMessage(expired).contains("scaduta"));
        balance("26000.00");
        assertPendingAgreement(request.getId());
    }

    @ParameterizedTest
    @CsvSource({"initiation,request,INSERT,26000", "sale,sale,INSERT,10000",
            "deletion,request,DELETE,10000", "state,vehicle,UPDATE,10000"})
    void persistenceFailuresRollBackEveryStep(String stage, String table, String event, int debitedBalance) throws Exception {
        Request request;
        if (stage.equals("initiation")) {
            createVehicle(false, Year.now().getValue());
            request = null;
        } else {
            request = approvedPurchase();
        }
        sql("CREATE FUNCTION fail_ticket06() RETURNS trigger LANGUAGE plpgsql AS $$ BEGIN "
                + "IF (SELECT balance FROM bank_account WHERE client = 1) <> " + debitedBalance
                + " THEN RAISE EXCEPTION 'ticket06_debit_not_observed'; END IF; "
                + "RAISE EXCEPTION 'ticket06_after_debit'; END $$; "
                + "CREATE TRIGGER fail_ticket06 BEFORE " + event + " ON " + table
                + " FOR EACH ROW EXECUTE FUNCTION fail_ticket06()");
        RuntimeException failure = assertThrows(RuntimeException.class, () -> {
            if (request == null) ctx.getPurchaseService().initiatePurchase(vehicle.getId(), 1L);
            else ctx.getPurchaseService().completePurchase(request.getId());
        });
        assertTrue(rootMessage(failure).contains("ticket06_after_debit"), rootMessage(failure));
        ctx = new AppContext(ctx.getDatabaseManager());
        if (request == null) {
            balance("30000.00");
            assertEquals(VehicleState.SALE, ctx.getCatalogService().getVehicleById(vehicle.getId()).orElseThrow().getState());
            assertTrue(ctx.getOperationQueryService().getClientRequests(1L).isEmpty());
            assertTrue(ctx.getOperationQueryService().getAllSales().isEmpty());
        } else {
            balance("26000.00");
            assertPendingAgreement(request.getId());
        }
    }

    private void createVehicle(boolean manual, int year) {
        var model = ctx.getCatalogService().addModel("Fiat", "Agreement06", new BigDecimal("20000"), year);
        vehicle = ctx.getCatalogService().addVehicle(model.getId(), "T0600000000000002", null, 0,
                manual ? new BigDecimal("20000") : null, year, VehicleState.SALE);
    }

    @ParameterizedTest
    @CsvSource({"SALE,4000,20000,true", "SALE,0,0,true", "SALE,4000,NULL,false",
            "SALE,-1,20000,false", "SALE,0,-1,false", "SALE,20001,20000,false",
            "PURCHASE,16000,NULL,true", "PURCHASE,0,NULL,true",
            "PURCHASE,-16000,NULL,false", "PURCHASE,16000,20000,false"})
    void databaseEnforcesDealershipRequestAmounts(String type, String deposit, String total, boolean valid) throws Exception {
        createVehicle(false, Year.now().getValue());
        String insert = "INSERT INTO request (vehicle, client, deposit, agreed_total, type) VALUES ("
                + vehicle.getId() + ", 1, " + deposit + ", " + total + ", '" + type + "')";
        if (valid) {
            sql(insert);
            Request reloaded = ctx.getOperationQueryService().getClientRequests(1L).getFirst();
            assertEquals(RequestType.valueOf(type), reloaded.getType());
            assertEquals(new BigDecimal(deposit).setScale(2), reloaded.getDeposit());
            if (total.equals("NULL")) assertNull(reloaded.getAgreedTotal());
            else assertEquals(new BigDecimal(total).setScale(2), reloaded.getAgreedTotal());
            var customerPurchase = new PostgresRequestDao(ctx.getDatabaseManager())
                    .findActivePurchaseRequestForVehicle(vehicle.getId());
            assertEquals(type.equals("SALE"), customerPurchase.isPresent());
        } else {
            var failure = assertThrows(java.sql.SQLException.class, () -> sql(insert));
            assertEquals("23514", failure.getSQLState());
            assertTrue(ctx.getOperationQueryService().getClientRequests(1L).isEmpty());
        }
        balance("30000.00");
        assertTrue(ctx.getOperationQueryService().getAllSales().isEmpty());
    }

    private Request approvedPurchase() {
        createVehicle(false, Year.now().getValue());
        Request request = ctx.getPurchaseService().initiatePurchase(vehicle.getId(), 1L);
        ctx.getPurchaseService().approvePurchase(request.getId(), 16L);
        return request;
    }

    private Request request(Long id) {
        return ctx.getOperationQueryService().getClientRequests(1L).stream()
                .filter(r -> r.getId().equals(id)).findFirst().orElseThrow();
    }

    private void assertPendingAgreement(Long id) {
        assertEquals(new BigDecimal("20000.00"), request(id).getAgreedTotal());
        assertEquals(new BigDecimal("4000.00"), request(id).getDeposit());
        assertEquals(new BigDecimal("16000.00"), request(id).getRemainingAmount());
        assertEquals(16L, request(id).getSalesmanId());
        assertEquals(VehicleState.BLOCKED, ctx.getCatalogService().getVehicleById(vehicle.getId()).orElseThrow().getState());
        assertTrue(ctx.getOperationQueryService().getAllSales().isEmpty());
    }

    private void balance(String expected) {
        assertEquals(new BigDecimal(expected), ctx.getAuthService().getClientBankAccount(1L).getBalance());
    }

    private void sql(String statement) throws Exception {
        try (var conn = ctx.getDatabaseManager().getConnection(); var sql = conn.createStatement()) {
            sql.execute(statement);
        }
    }

    private static String rootMessage(Throwable failure) {
        while (failure.getCause() != null) failure = failure.getCause();
        return failure.getMessage();
    }
}
