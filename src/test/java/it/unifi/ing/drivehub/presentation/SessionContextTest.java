package it.unifi.ing.drivehub.presentation;

import it.unifi.ing.drivehub.domain.users.Role;
import it.unifi.ing.drivehub.presentation.core.SessionContext;
import it.unifi.ing.drivehub.presentation.core.UiModels;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SessionContextTest {
    @Test
    void opensAndClearsSession() {
        SessionContext context = new SessionContext();
        UiModels.Session session = new UiModels.Session(7, "Cliente Demo", "cliente@drivehub.test", Role.CUSTOMER);

        assertTrue(context.current().isEmpty());
        context.open(session);
        assertSame(session, context.requireCurrent());
        context.clear();
        assertTrue(context.current().isEmpty());
        assertThrows(IllegalStateException.class, context::requireCurrent);
    }

    @Test
    void rejectsNullSession() {
        SessionContext context = new SessionContext();
        assertThrows(IllegalArgumentException.class, () -> context.open(null));
    }
}
