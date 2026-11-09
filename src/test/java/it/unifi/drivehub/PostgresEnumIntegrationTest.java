package it.unifi.drivehub;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@Tag("postgres")
class PostgresEnumIntegrationTest {
    @ParameterizedTest
    @CsvSource({
            "vehicle,state,vehicle_state,SALE|RENT|BLOCKED|WAITING|SOLD",
            "request,type,request_type,PURCHASE|SALE",
            "vehicle,price_mode,price_mode,AUTOMATIC|MANUAL",
            "sale,type,sale_type,SALE|PURCHASE|PENALTY"
    })
    void closedDomainsUseNativeEnumsAndRejectUnknownValues(
            String table, String column, String type, String labels) throws Exception {
        var db = TestDatabase.fromEnvironment().reset();
        try (var conn = db.getConnection()) {
            try (var query = conn.prepareStatement("""
                    SELECT data_type, udt_name FROM information_schema.columns
                    WHERE table_schema = 'public' AND table_name = ? AND column_name = ?
                    """)) {
                query.setString(1, table);
                query.setString(2, column);
                try (var result = query.executeQuery()) {
                    assertTrue(result.next());
                    assertEquals("USER-DEFINED", result.getString("data_type"), table + "." + column);
                    assertEquals(type, result.getString("udt_name"));
                }
            }
            try (var query = conn.prepareStatement("""
                    SELECT e.enumlabel FROM pg_type t JOIN pg_enum e ON e.enumtypid = t.oid
                    WHERE t.typnamespace = 'public'::regnamespace AND t.typname = ?
                    ORDER BY e.enumsortorder
                    """)) {
                query.setString(1, type);
                List<String> actual = new ArrayList<>();
                try (var result = query.executeQuery()) {
                    while (result.next()) actual.add(result.getString(1));
                }
                assertEquals(List.of(labels.split("\\|")), actual);
            }
            try (var statement = conn.createStatement()) {
                var failure = assertThrows(SQLException.class, () -> statement.executeUpdate(
                        "UPDATE " + table + " SET " + column + " = 'UNKNOWN_ENUM_VALUE' WHERE id = 1"));
                assertEquals("22P02", failure.getSQLState());
            }
        }
    }
}
