package it.unifi.drivehub;

import it.unifi.drivehub.dao.DatabaseManager;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@Tag("postgres")
class DemoSqlValidationTest {
    @Test
    void schemaAndDemoCanBeReloadedWithoutChangingRowsOrSequences() throws Exception {
        TestDatabase fixture = TestDatabase.fromEnvironment();
        DatabaseManager db = fixture.reset();
        var sales = new AppContext(db).getOperationQueryService().getAllSales();
        assertEquals(32, sales.stream().filter(s -> s.getType() == it.unifi.drivehub.domain.operation.SaleType.SALE).count());
        assertEquals(8, sales.stream().filter(s -> s.getType() == it.unifi.drivehub.domain.operation.SaleType.PENALTY).count());
        assertTrue(sales.stream().allMatch(s -> s.getAmount().signum() >= 0));
        List<String> expected = snapshot(db);
        assertFalse(expected.isEmpty());

        fixture.reloadDemo();
        assertEquals(expected, snapshot(db), "Secondo caricamento demo");
        fixture.reloadDemo();
        assertEquals(expected, snapshot(db), "Terzo caricamento demo");
        fixture.reset();
        assertEquals(expected, snapshot(db), "Secondo caricamento schema e demo");
    }

    private static List<String> snapshot(DatabaseManager db) throws Exception {
        List<String> rows = new ArrayList<>();
        try (Connection conn = db.getConnection(); var statement = conn.createStatement()) {
            for (String table : List.of("brand", "model", "model_discount", "vehicle", "vehicle_discount",
                    "client", "bank_account", "salesman", "manager", "request", "test_drive", "rent", "sale")) {
                try (var result = statement.executeQuery("SELECT row_to_json(t)::text FROM public.\""
                        + table + "\" t ORDER BY row_to_json(t)::text")) {
                    while (result.next()) rows.add(table + ":" + result.getString(1));
                }
            }
            try (var result = statement.executeQuery("""
                    SELECT sequencename, last_value FROM pg_sequences
                    WHERE schemaname = 'public' ORDER BY sequencename
                    """)) {
                while (result.next()) rows.add(result.getString(1) + ":" + result.getString(2));
            }
        }
        return rows;
    }
}
