package it.unifi.drivehub;

import it.unifi.drivehub.dao.DatabaseManager;
import it.unifi.drivehub.dao.postgres.PostgresBrandDao;
import it.unifi.drivehub.domain.vehicle.Brand;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Verifica Connessione PostgreSQL reale e DAO")
class DatabaseConnectionIntegrationTest {

    @Test
    @DisplayName("Connessione e lettura marchi dal database PostgreSQL")
    void testRealDatabaseConnection() {
        DatabaseManager db = new DatabaseManager();
        assertDoesNotThrow(() -> {
            try (Connection conn = db.getConnection()) {
                assertNotNull(conn);
                assertFalse(conn.isClosed());
            }
        });

        PostgresBrandDao brandDao = new PostgresBrandDao(db);
        List<Brand> brands = brandDao.findAll();
        assertNotNull(brands);
        assertFalse(brands.isEmpty(), "Il database deve contenere i brand precaricati");
        assertTrue(brands.stream().anyMatch(b -> b.getName().equalsIgnoreCase("Fiat") || b.getName().equalsIgnoreCase("BMW")),
                "Marchi noti devono essere presenti");
    }
}

