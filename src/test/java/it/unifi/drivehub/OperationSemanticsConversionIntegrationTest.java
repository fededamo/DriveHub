package it.unifi.drivehub;

import it.unifi.drivehub.dao.DatabaseManager;
import it.unifi.drivehub.domain.operation.RequestType;
import it.unifi.drivehub.domain.operation.SaleType;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

@Tag("postgres")
class OperationSemanticsConversionIntegrationTest {
    @Test
    void freshlyInitializedFinalContractIsAlreadyConverted() throws Exception {
        var db = TestDatabase.fromEnvironment().reset();
        var before = snapshot(db, false);
        LegacyContractFixture.convert(db);
        assertEquals(before, snapshot(db, false));
    }

    @Test
    void legacyRequestsAndLedgerReachFinalContractIncludingZeroAmounts() throws Exception {
        DatabaseManager db = LegacyContractFixture.reset();
        var preserved = snapshot(db, true);
        LegacyContractFixture.convert(db);
        assertEquals(preserved, snapshot(db, true), "Righe, collegamenti, date, conti e sequenze conservati");
        var ctx = new AppContext(db);
        var requests = ctx.getOperationQueryService().getClientRequests(1L);
        assertEquals(6, requests.size());
        var purchase = requests.stream().filter(r -> r.getId() == 101L).findFirst().orElseThrow();
        assertEquals(RequestType.SALE, purchase.getType());
        assertEquals(new BigDecimal("20000.00"), purchase.getAgreedTotal());
        assertEquals(new BigDecimal("4000.00"), purchase.getDeposit());
        assertEquals(new BigDecimal("16000.00"), purchase.getRemainingAmount());
        for (var request : requests) {
            assertEquals(request.getId() <= 104L ? RequestType.SALE : RequestType.PURCHASE, request.getType());
            assertEquals(new BigDecimal(request.getId() <= 103L ? "4000.00"
                    : request.getId() == 105L ? "16000.00" : "0.00"), request.getDeposit());
        }
        var sales = ctx.getOperationQueryService().getAllSales();
        assertEquals(8, sales.size());
        for (var sale : sales) {
            SaleType expected = switch (sale.getId().intValue()) {
                case 201, 204, 207 -> SaleType.SALE;
                case 202, 205, 208 -> SaleType.PURCHASE;
                default -> SaleType.PENALTY;
            };
            assertEquals(expected, sale.getType());
            assertTrue(sale.getAmount().signum() >= 0);
            assertEquals(new BigDecimal(switch (sale.getId().intValue()) {
                case 201 -> "20000.00";
                case 202 -> "16000.00";
                case 203 -> "800.00";
                case 207 -> "19000.00";
                case 208 -> "10000.00";
                default -> "0.00";
            }), sale.getAmount());
        }
        assertEquals(new BigDecimal("26000.00"), ctx.getAuthService().getClientBankAccount(1L).getBalance());
        var converted = snapshot(db, false);
        LegacyContractFixture.convert(db);
        assertEquals(converted, snapshot(db, false), "Seconda esecuzione senza alcuna modifica");
    }

    @Test
    void failureAfterRealUpdatesAndNewConstraintsRestoresEntireSource() throws Exception {
        var db = LegacyContractFixture.reset();
        String failing = LegacyContractFixture.script().replace("COMMIT;", """
                DO $$ BEGIN
                    IF NOT EXISTS (SELECT 1 FROM request WHERE id=101 AND type='SALE')
                        OR NOT EXISTS (SELECT 1 FROM request WHERE id=105 AND type='PURCHASE' AND deposit=16000)
                        OR NOT EXISTS (SELECT 1 FROM sale WHERE id=202 AND type='PURCHASE' AND amount=16000)
                        OR NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conrelid='sale'::regclass
                                   AND conname='ck_sale_amount_type' AND pg_get_constraintdef(oid)='CHECK ((amount >= (0)::numeric))') THEN
                        RAISE EXCEPTION 'failure_injection_too_early';
                    END IF;
                    RAISE EXCEPTION 'conversion_after_real_writes';
                END $$;
                COMMIT;
                """);
        var source = snapshot(db, false);
        var failure = assertThrows(SQLException.class, () -> LegacyContractFixture.convert(db, failing));
        assertTrue(failure.getMessage().contains("conversion_after_real_writes"), failure.getMessage());
        assertEquals(source, snapshot(db, false), "Rollback di dati, vincoli e sequenze");
        LegacyContractFixture.convert(db);
        assertEquals(RequestType.SALE, new AppContext(db).getOperationQueryService()
                .getClientRequests(1L).stream().filter(r -> r.getId() == 101L).findFirst().orElseThrow().getType());
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "ALTER TABLE request DROP CONSTRAINT ck_request_agreement",
            "ALTER TABLE request ADD CONSTRAINT unknown_contract CHECK (deposit <> 123)",
            "ALTER TABLE request ALTER COLUMN agreed_total TYPE numeric(12,2)",
            "ALTER TYPE request_type ADD VALUE 'UNKNOWN'",
            "UPDATE request SET deposit=16000 WHERE id=105",
            "UPDATE request SET deposit='NaN', agreed_total='NaN' WHERE id=104",
            "UPDATE sale SET amount='NaN' WHERE id=201",
            "ALTER TABLE request DROP CONSTRAINT ck_request_agreement; UPDATE request SET type=CASE type WHEN 'PURCHASE' THEN 'SALE'::request_type ELSE 'PURCHASE'::request_type END, deposit=abs(deposit); ALTER TABLE request ADD CONSTRAINT ck_request_agreement CHECK ((type='SALE' AND agreed_total IS NOT NULL AND agreed_total>=0 AND deposit>=0 AND deposit<=agreed_total) OR (type='PURCHASE' AND agreed_total IS NULL AND deposit>=0))",
            "ALTER TABLE sale DROP CONSTRAINT sale_type_check, DROP CONSTRAINT ck_sale_amount_type; UPDATE sale SET type=CASE type WHEN 'PURCHASE' THEN 'SALE' WHEN 'ACQUISITION' THEN 'PURCHASE' ELSE type END, amount=abs(amount); ALTER TABLE sale ADD CONSTRAINT sale_type_check CHECK (type IN ('SALE','PURCHASE','PENALTY')), ADD CONSTRAINT ck_sale_amount_type CHECK (amount>=0)",
            "CREATE OR REPLACE FUNCTION unknown_trigger() RETURNS trigger LANGUAGE plpgsql AS $$ BEGIN RETURN NEW; END $$; CREATE TRIGGER unknown_trigger BEFORE UPDATE ON request FOR EACH ROW EXECUTE FUNCTION unknown_trigger()"
    })
    void unknownOrIntermediateSourceIsRejectedWithoutWrites(String mutation) throws Exception {
        var db = LegacyContractFixture.reset();
        LegacyContractFixture.sql(db, mutation);
        var source = snapshot(db, false);
        var failure = assertThrows(SQLException.class, () -> LegacyContractFixture.convert(db));
        assertTrue(failure.getMessage().contains("Origine non riconosciuta"), failure.getMessage());
        assertEquals(source, snapshot(db, false));
    }

    @Test
    void openApplicationConnectionPreventsConversion() throws Exception {
        var db = LegacyContractFixture.reset();
        var source = snapshot(db, false);
        try (var application = db.getConnection()) {
            assertFalse(application.isClosed());
            var failure = assertThrows(SQLException.class, () -> LegacyContractFixture.convert(db));
            assertTrue(failure.getMessage().contains("chiudere tutte le altre connessioni"));
        }
        assertEquals(source, snapshot(db, false));
    }

    @Test
    void convertedAgreementKeeps16000RemainingAfterPriceChangeAndPaysOnce() throws Exception {
        var db = LegacyContractFixture.reset();
        LegacyContractFixture.convert(db);
        var ctx = new AppContext(db);
        ctx.getCatalogService().updateVehiclePricing(1L, new BigDecimal("35000"));
        LegacyContractFixture.sql(db, "UPDATE model SET release_price=30000 WHERE id=1");
        ctx = new AppContext(db);
        var request = ctx.getOperationQueryService().getClientRequests(1L).stream()
                .filter(r -> r.getId() == 101L).findFirst().orElseThrow();
        assertEquals(new BigDecimal("16000.00"), request.getRemainingAmount());
        assertEquals(new BigDecimal("20000.00"), request.getAgreedTotal());
        assertNotEquals(request.getAgreedTotal(), ctx.getCatalogService().getEffectiveVehiclePrice(
                ctx.getCatalogService().getVehicleById(1L).orElseThrow()));
        ctx.getPurchaseService().completePurchase(101L);
        var reloaded = new AppContext(db);
        assertThrows(RuntimeException.class, () -> reloaded.getPurchaseService().completePurchase(101L));
        assertEquals(new BigDecimal("10000.00"), reloaded.getAuthService().getClientBankAccount(1L).getBalance());
        var sale = reloaded.getOperationQueryService().getAllSales().stream()
                .filter(s -> Long.valueOf(1L).equals(s.getVehicleId())).findFirst().orElseThrow();
        assertEquals(SaleType.SALE, sale.getType());
        assertEquals(new BigDecimal("20000.00"), sale.getAmount());
        assertEquals(9, reloaded.getOperationQueryService().getAllSales().size());
    }

    @Test
    void convertedProposalCreditsEstimateExactlyOnce() throws Exception {
        var db = LegacyContractFixture.reset();
        LegacyContractFixture.convert(db);
        new AppContext(db).getSaleToDealershipService().approveSaleProposal(105L, 16L);
        var reloaded = new AppContext(db);
        assertThrows(RuntimeException.class, () -> reloaded.getSaleToDealershipService().approveSaleProposal(105L, 16L));
        assertEquals(new BigDecimal("42000.00"), reloaded.getAuthService().getClientBankAccount(1L).getBalance());
        var purchases = reloaded.getOperationQueryService().getAllSales().stream()
                .filter(s -> s.getId() > 251L).toList();
        assertEquals(1, purchases.size());
        assertEquals(SaleType.PURCHASE, purchases.getFirst().getType());
        assertEquals(new BigDecimal("16000.00"), purchases.getFirst().getAmount());
    }

    @Test
    void convertedExpiryRefunds3200AndKeeps800PenaltyOnce() throws Exception {
        var db = LegacyContractFixture.reset();
        LegacyContractFixture.convert(db);
        var ctx = new AppContext(db);
        assertEquals(1, ctx.getPurchaseService().checkAndHandleExpiredRequests(1L).size());
        assertTrue(ctx.getPurchaseService().checkAndHandleExpiredRequests(1L).isEmpty());
        assertEquals(new BigDecimal("29200.00"), ctx.getAuthService().getClientBankAccount(1L).getBalance());
        var penalties = ctx.getOperationQueryService().getAllSales().stream().filter(s -> s.getId() > 251L).toList();
        assertEquals(1, penalties.size());
        assertEquals(SaleType.PENALTY, penalties.getFirst().getType());
        assertEquals(new BigDecimal("800.00"), penalties.getFirst().getAmount());
    }

    @Test
    void convertedHistoryFindsLatestBuyerEvenAtZeroAndIgnoresNewerAcquisition() throws Exception {
        var db = LegacyContractFixture.reset();
        LegacyContractFixture.convert(db);
        LegacyContractFixture.sql(db, "DELETE FROM request WHERE id IN (105,106); UPDATE vehicle SET state='SOLD' WHERE id IN (5,6)");
        var service = new AppContext(db).getSaleToDealershipService();
        for (long vehicle : new long[]{5, 6}) {
            String vin = "LEGACY" + String.format("%011d", vehicle);
            assertThrows(RuntimeException.class, () -> service.submitSaleProposal(2L, 1L, vin, "OLD" + vehicle, 0, 2020));
            assertEquals(RequestType.PURCHASE, service.submitSaleProposal(1L, 1L, vin, "OLD" + vehicle, 0, 2020).getType());
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "UPDATE request SET deposit=-1 WHERE id=101",
            "UPDATE request SET deposit=-1 WHERE id=105",
            "UPDATE request SET agreed_total=3999 WHERE id=101",
            "UPDATE request SET agreed_total=16000 WHERE id=105",
            "UPDATE sale SET amount=-1 WHERE id=201",
            "UPDATE sale SET amount=-1 WHERE id=202",
            "UPDATE sale SET amount=-1 WHERE id=203"
    })
    void convertedConstraintsRejectNewInvalidAmounts(String invalid) throws Exception {
        var db = LegacyContractFixture.reset();
        LegacyContractFixture.convert(db);
        var converted = snapshot(db, false);
        var failure = assertThrows(SQLException.class, () -> LegacyContractFixture.sql(db, invalid));
        assertEquals("23514", failure.getSQLState());
        assertEquals(converted, snapshot(db, false));
    }

    private static List<String> snapshot(DatabaseManager db, boolean unchangedOnly) throws Exception {
        List<String> rows = new ArrayList<>();
        try (var conn = db.getConnection(); var sql = conn.createStatement()) {
            for (String table : List.of("brand", "model", "model_discount", "vehicle", "vehicle_discount",
                    "client", "salesman", "manager", "bank_account", "request", "sale", "rent", "test_drive")) {
                String omit = unchangedOnly ? switch (table) {
                    case "request" -> " - 'type' - 'deposit'";
                    case "sale" -> " - 'type' - 'amount'";
                    default -> "";
                } : "";
                try (var result = sql.executeQuery("SELECT (to_jsonb(t)" + omit + ")::text FROM public.\""
                        + table + "\" t ORDER BY 1")) {
                    while (result.next()) rows.add(table + ":" + result.getString(1));
                }
            }
            try (var result = sql.executeQuery("SELECT sequencename FROM pg_sequences WHERE schemaname='public' ORDER BY 1")) {
                List<String> sequences = new ArrayList<>();
                while (result.next()) sequences.add(result.getString(1));
                for (String sequence : sequences) {
                    try (var values = conn.createStatement(); var value = values.executeQuery(
                            "SELECT last_value, is_called FROM public.\"" + sequence + "\"")) {
                        assertTrue(value.next());
                        rows.add(sequence + ":" + value.getLong(1) + ":" + value.getBoolean(2));
                    }
                }
            }
            try (var result = sql.executeQuery("SELECT row_to_json(s)::text FROM pg_sequences s WHERE schemaname='public' ORDER BY sequencename")) {
                while (result.next()) rows.add(result.getString(1));
            }
            if (!unchangedOnly) {
                for (String query : List.of("""
                        SELECT conrelid::regclass::text || ':' || conname || ':' || pg_get_constraintdef(oid)
                        FROM pg_constraint WHERE connamespace='public'::regnamespace ORDER BY 1
                        """, """
                        SELECT table_name || ':' || column_name || ':' || data_type || ':' ||
                            coalesce(numeric_precision::text,'') || ':' || coalesce(numeric_scale::text,'')
                        FROM information_schema.columns WHERE table_schema='public' ORDER BY 1
                        """, """
                        SELECT enumtypid::regtype::text || ':' || enumlabel FROM pg_enum ORDER BY 1
                        """)) {
                    try (var result = sql.executeQuery(query)) {
                        while (result.next()) rows.add(result.getString(1));
                    }
                }
            }
        }
        return rows;
    }
}
