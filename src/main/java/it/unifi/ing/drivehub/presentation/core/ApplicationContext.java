package it.unifi.ing.drivehub.presentation.core;

import java.util.Objects;

public final class ApplicationContext implements AutoCloseable {
    private final UiGateway gateway;
    private final SessionContext session = new SessionContext();

    public ApplicationContext(UiGateway gateway) {
        this.gateway = Objects.requireNonNull(gateway);
    }

    public UiGateway gateway() {
        return gateway;
    }

    public SessionContext session() {
        return session;
    }

    @Override
    public void close() {
        session.clear();
        gateway.close();
    }
}
