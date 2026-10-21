package it.unifi.ing.drivehub.architecture;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;

/** Source dependency checks include fully qualified references, not only imports. */
class LayeringTest {
    private static final Path JAVA_ROOT = Path.of("src/main/java/it/unifi/ing/drivehub");
    private static final String APP = "it.unifi.ing.drivehub.";

    @Test
    void domainDoesNotDependOnOuterLayersOrInfrastructure() throws IOException {
        assertNoDependencies("domain", List.of(APP + "business", APP + "dao", APP + "presentation",
                "javafx", "java.sql", "javax.sql"));
    }

    @Test
    void businessDependsOnlyOnPersistencePortsAndDomain() throws IOException {
        assertNoDependencies("business", List.of(APP + "dao.postgres", APP + "presentation",
                "javafx", "java.sql", "javax.sql"));
    }

    @Test
    void persistencePortsAreIndependentOfAdapters() throws IOException {
        assertNoDependencies("dao/interfaces", List.of(APP + "dao.postgres", APP + "business",
                APP + "presentation", "javafx", "java.sql", "javax.sql"));
    }

    @Test
    void postgresAdapterDoesNotDependOnBusinessOrPresentation() throws IOException {
        assertNoDependencies("dao/postgres", List.of(APP + "business", APP + "presentation", "javafx"));
    }

    @Test
    void presentationCannotBypassBusinessForPersistence() throws IOException {
        assertNoDependencies("presentation", List.of(APP + "dao", "java.sql", "javax.sql"));
    }

    @Test
    void controllersUseOnlyTheUiGatewayAndProjections() throws IOException {
        assertNoDependencies("presentation/controller", List.of(APP + "business"));
    }

    private static void assertNoDependencies(String packageName, List<String> forbidden) throws IOException {
        Path root = JAVA_ROOT.resolve(packageName);
        assertTrue(Files.isDirectory(root), () -> "Missing production package: " + root);
        try (var files = Files.walk(root)) {
            List<Path> sources = files.filter(path -> path.toString().endsWith(".java")).toList();
            assertFalse(sources.isEmpty(), () -> "No source checked in " + root);
            for (Path source : sources) {
                // Comments and literals cannot introduce Java type dependencies.
                String text = Files.readString(source).replaceAll(
                        "(?s)/\\*.*?\\*/|//[^\\r\\n]*|\"(?:\\\\.|[^\"\\\\])*\"", " ");
                for (String prefix : forbidden) {
                    Pattern dependency = Pattern.compile("(?<![\\w$])" + Pattern.quote(prefix) + "\\.");
                    assertFalse(dependency.matcher(text).find(),
                            () -> source + " contains forbidden dependency " + prefix);
                }
            }
        }
    }
}
