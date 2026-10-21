package it.unifi.ing.drivehub.dao.postgres;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

final class SqlScriptRunner {

    private SqlScriptRunner() {
    }

    static void run(Connection connection, String classpathResource) throws SQLException {
        String script = read(classpathResource);
        boolean isH2 = isH2Database(connection);
        for (String sql : splitStatements(script)) {
            if (isH2 && isPostgresProceduralConstruct(sql)) {
                continue;
            }
            try (Statement statement = connection.createStatement()) {
                statement.execute(sql);
            }
        }
    }

    private static boolean isH2Database(Connection connection) {
        try {
            return connection.getMetaData().getDatabaseProductName().toLowerCase(java.util.Locale.ROOT).contains("h2");
        } catch (SQLException e) {
            return false;
        }
    }

    private static boolean isPostgresProceduralConstruct(String sql) {
        String upper = sql.toUpperCase(java.util.Locale.ROOT);
        return upper.startsWith("DROP FUNCTION")
                || upper.startsWith("CREATE OR REPLACE FUNCTION")
                || upper.startsWith("CREATE FUNCTION")
                || upper.startsWith("DROP TRIGGER")
                || upper.startsWith("CREATE TRIGGER")
                || upper.startsWith("SELECT SETVAL");
    }

    private static String read(String classpathResource) {
        String path = classpathResource.startsWith("/") ? classpathResource.substring(1) : classpathResource;
        ClassLoader loader = SqlScriptRunner.class.getClassLoader();
        try (InputStream input = loader.getResourceAsStream(path)) {
            if (input == null) {
                throw new IllegalArgumentException("Missing SQL resource: " + classpathResource);
            }
            return new String(input.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException exception) {
            throw new DatabaseException("Could not read SQL resource " + classpathResource, exception);
        }
    }

    static List<String> splitStatements(String script) {
        List<String> statements = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean singleQuoted = false;
        boolean doubleQuoted = false;
        boolean dollarQuoted = false;
        boolean lineComment = false;
        boolean blockComment = false;

        for (int index = 0; index < script.length(); index++) {
            char ch = script.charAt(index);
            char next = index + 1 < script.length() ? script.charAt(index + 1) : '\0';

            if (lineComment) {
                if (ch == '\n') {
                    lineComment = false;
                    current.append(ch);
                }
                continue;
            }
            if (blockComment) {
                if (ch == '*' && next == '/') {
                    blockComment = false;
                    index++;
                }
                continue;
            }
            if (!singleQuoted && !doubleQuoted && !dollarQuoted && ch == '-' && next == '-') {
                lineComment = true;
                index++;
                continue;
            }
            if (!singleQuoted && !doubleQuoted && !dollarQuoted && ch == '/' && next == '*') {
                blockComment = true;
                index++;
                continue;
            }
            if (!singleQuoted && !doubleQuoted && ch == '$' && next == '$') {
                dollarQuoted = !dollarQuoted;
                current.append("$$");
                index++;
                continue;
            }
            if (ch == '\'' && !doubleQuoted && !dollarQuoted) {
                current.append(ch);
                if (singleQuoted && next == '\'') {
                    current.append(next);
                    index++;
                } else {
                    singleQuoted = !singleQuoted;
                }
                continue;
            }
            if (ch == '"' && !singleQuoted && !dollarQuoted) {
                doubleQuoted = !doubleQuoted;
                current.append(ch);
                continue;
            }
            if (ch == ';' && !singleQuoted && !doubleQuoted && !dollarQuoted) {
                addIfNotBlank(statements, current);
                current.setLength(0);
            } else {
                current.append(ch);
            }
        }
        addIfNotBlank(statements, current);
        return List.copyOf(statements);
    }

    private static void addIfNotBlank(List<String> statements, StringBuilder sql) {
        String candidate = sql.toString().trim();
        if (!candidate.isEmpty()) {
            statements.add(candidate);
        }
    }
}
