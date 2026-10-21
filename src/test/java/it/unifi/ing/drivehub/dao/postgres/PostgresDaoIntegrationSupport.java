package it.unifi.ing.drivehub.dao.postgres;

import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.BeforeEach;

import javax.sql.DataSource;
import java.util.UUID;

abstract class PostgresDaoIntegrationSupport {

    protected DataSource dataSource;

    @BeforeEach
    void initializeDatabase() {
        dataSource = createDataSource();
        new DatabaseBootstrap(dataSource, false).initialize();
    }

    protected DataSource createDataSource() {
        JdbcDataSource h2 = new JdbcDataSource();
        String databaseName = "drivehub_" + UUID.randomUUID().toString().replace("-", "");
        h2.setURL("jdbc:h2:mem:" + databaseName
                + ";MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DEFAULT_NULL_ORDERING=HIGH;DB_CLOSE_DELAY=-1");
        h2.setUser("sa");
        h2.setPassword("");
        return h2;
    }
}
