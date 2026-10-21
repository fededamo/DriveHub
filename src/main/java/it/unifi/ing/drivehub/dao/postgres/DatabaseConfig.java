package it.unifi.ing.drivehub.dao.postgres;

import java.util.Map;
import java.util.Objects;
import java.util.Properties;

/**
 * Database settings loaded exclusively from JVM properties and environment variables.
 */
public record DatabaseConfig(String url, String user, String password, boolean seedDemoData) {

    public static final String URL_PROPERTY = "drivehub.db.url";
    public static final String USER_PROPERTY = "drivehub.db.user";
    public static final String PASSWORD_PROPERTY = "drivehub.db.password";
    public static final String SEED_PROPERTY = "drivehub.db.seed-demo";

    public static final String URL_ENV = "DRIVEHUB_DB_URL";
    public static final String USER_ENV = "DRIVEHUB_DB_USER";
    public static final String PASSWORD_ENV = "DRIVEHUB_DB_PASSWORD";
    public static final String SEED_ENV = "DRIVEHUB_DB_SEED_DEMO";

    public DatabaseConfig {
        url = requireNonBlank(url, "Database URL");
        user = requireNonBlank(user, "Database user");
        password = requireNonBlank(password, "Database password");
    }

    public static DatabaseConfig fromEnvironment() {
        Properties props = new Properties();
        loadDotEnvIfPresent(props);
        props.putAll(System.getProperties());
        return from(props, System.getenv());
    }

    private static void loadDotEnvIfPresent(Properties props) {
        java.nio.file.Path[] candidates = {
            java.nio.file.Paths.get(".env"),
            java.nio.file.Paths.get("DriveHub/.env"),
            java.nio.file.Paths.get("../.env")
        };
        for (java.nio.file.Path p : candidates) {
            if (java.nio.file.Files.isRegularFile(p)) {
                try (java.io.BufferedReader reader = java.nio.file.Files.newBufferedReader(p, java.nio.charset.StandardCharsets.UTF_8)) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        line = line.trim();
                        if (!line.isEmpty() && !line.startsWith("#") && line.contains("=")) {
                            int idx = line.indexOf('=');
                            String key = line.substring(0, idx).trim();
                            String val = line.substring(idx + 1).trim();
                            props.setProperty(key, val);
                            if (key.equals(PASSWORD_ENV)) props.setProperty(PASSWORD_PROPERTY, val);
                            if (key.equals(USER_ENV)) props.setProperty(USER_PROPERTY, val);
                            if (key.equals(URL_ENV)) props.setProperty(URL_PROPERTY, val);
                            if (key.equals(SEED_ENV)) props.setProperty(SEED_PROPERTY, val);
                        }
                    }
                } catch (Exception ignored) {
                }
                break;
            }
        }
    }

    static DatabaseConfig from(Properties properties, Map<String, String> environment) {
        Objects.requireNonNull(properties, "properties");
        Objects.requireNonNull(environment, "environment");

        String url = setting(properties, environment, URL_PROPERTY, URL_ENV,
                "jdbc:postgresql://localhost:5432/drivehub");
        String user = setting(properties, environment, USER_PROPERTY, USER_ENV, "drivehub");
        String password = setting(properties, environment, PASSWORD_PROPERTY, PASSWORD_ENV, "drivehub");
        boolean seedDemo = Boolean.parseBoolean(setting(
                properties, environment, SEED_PROPERTY, SEED_ENV, "true"));
        return new DatabaseConfig(url, user, password, seedDemo);
    }

    private static String setting(
            Properties properties,
            Map<String, String> environment,
            String propertyName,
            String environmentName,
            String fallback) {
        String value = trimToNull(properties.getProperty(propertyName));
        if (value != null) {
            return value;
        }
        value = trimToNull(environment.get(environmentName));
        return value == null ? fallback : value;
    }

    private static String requireNonBlank(String value, String label) {
        String normalized = trimToNull(value);
        if (normalized == null) {
            throw new IllegalStateException(label + " is required via a JVM property or environment variable");
        }
        return normalized;
    }

    private static String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
