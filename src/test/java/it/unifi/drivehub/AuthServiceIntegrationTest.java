package it.unifi.drivehub;

import it.unifi.drivehub.domain.user.User;
import it.unifi.drivehub.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

@Tag("postgres")
class AuthServiceIntegrationTest {

    private AuthService auth;

    @BeforeEach
    void setUp() throws Exception {
        auth = new AppContext(TestDatabase.fromEnvironment().reset()).getAuthService();
    }

    @ParameterizedTest
    @ValueSource(strings = {"mario.rossi@email.it", "paolo.costa@drivehub.it", "vittorio.manager@drivehub.it"})
    void rejectedEmailPreservesStoredIdentityAndLogin(String email) {
        User before = auth.login(email, "password");

        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> auth.changeEmail(before.getId(), before.getRoleDisplayName(), "nuova email@example.it", "password"));

        assertEquals("Nuovo formato email non valido: nuova email@example.it", error.getMessage());
        assertIdentityUnchanged(before);
    }

    @ParameterizedTest
    @ValueSource(strings = {"mario.rossi@email.it", "paolo.costa@drivehub.it", "vittorio.manager@drivehub.it"})
    void validEmailIsNormalizedPersistedAndUsedForLogin(String email) {
        User before = auth.login(email, "password");

        auth.changeEmail(before.getId(), before.getRoleDisplayName(), "  Nuova.Email@Example.IT \t", "password");

        User after = auth.login("nuova.email@example.it", "password");
        assertAll(
                () -> assertEquals(before.getId(), after.getId()),
                () -> assertEquals("nuova.email@example.it", after.getEmail()),
                () -> assertEquals(before.getHashPassword(), after.getHashPassword()),
                () -> assertEquals(before.getRoleDisplayName(), after.getRoleDisplayName()),
                () -> assertThrows(IllegalArgumentException.class, () -> auth.login(email, "password")));
    }

    @ParameterizedTest
    @CsvSource({
            "mario.rossi@email.it, luigi.verdi@email.it",
            "paolo.costa@drivehub.it, laura.gatti@drivehub.it",
            "mario.rossi@email.it, paolo.costa@drivehub.it",
            "paolo.costa@drivehub.it, vittorio.manager@drivehub.it",
            "vittorio.manager@drivehub.it, mario.rossi@email.it"
    })
    void duplicateEmailPreservesBothIdentitiesAndLogins(String email, String duplicate) {
        User before = auth.login(email, "password");
        User owner = auth.login(duplicate, "password");

        assertThrows(RuntimeException.class,
                () -> auth.changeEmail(before.getId(), before.getRoleDisplayName(),
                        "  " + duplicate.toUpperCase(java.util.Locale.ROOT) + "  ", "password"));

        assertIdentityUnchanged(before);
        assertIdentityUnchanged(owner);
    }

    @ParameterizedTest
    @ValueSource(strings = {"mario.rossi@email.it", "paolo.costa@drivehub.it", "vittorio.manager@drivehub.it"})
    void wrongPasswordPreservesStoredIdentityAndLogin(String email) {
        User before = auth.login(email, "password");

        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> auth.changeEmail(before.getId(), before.getRoleDisplayName(), "nuova@example.it", "errata"));

        assertEquals("Password attuale errata: operazione annullata.", error.getMessage());
        assertIdentityUnchanged(before);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" \t ", "senza-chiocciola"})
    void malformedEmailPreservesStoredIdentityAndLogin(String email) {
        User before = auth.login("mario.rossi@email.it", "password");

        assertThrows(IllegalArgumentException.class,
                () -> auth.changeEmail(before.getId(), before.getRoleDisplayName(), email, "password"));

        assertIdentityUnchanged(before);
    }

    private void assertIdentityUnchanged(User before) {
        User after = auth.login(before.getEmail(), "password");
        assertAll(
                () -> assertEquals(before.getId(), after.getId()),
                () -> assertEquals(before.getName(), after.getName()),
                () -> assertEquals(before.getSurname(), after.getSurname()),
                () -> assertEquals(before.getEmail(), after.getEmail()),
                () -> assertEquals(before.getHashPassword(), after.getHashPassword()),
                () -> assertEquals(before.getRoleDisplayName(), after.getRoleDisplayName()));
    }
}
