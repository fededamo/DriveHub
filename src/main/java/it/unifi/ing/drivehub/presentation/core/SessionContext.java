package it.unifi.ing.drivehub.presentation.core;

import java.util.Optional;

public final class SessionContext {
    private UiModels.Session current;

    public Optional<UiModels.Session> current() {
        return Optional.ofNullable(current);
    }

    public boolean isLoggedIn() {
        return current != null;
    }

    public UiModels.Session requireCurrent() {
        if (current == null) {
            throw new IllegalStateException("Nessun utente autenticato");
        }
        return current;
    }

    public void open(UiModels.Session session) {
        if (session == null) {
            throw new IllegalArgumentException("La sessione non può essere nulla");
        }
        current = session;
    }

    public void clear() {
        current = null;
    }
}
