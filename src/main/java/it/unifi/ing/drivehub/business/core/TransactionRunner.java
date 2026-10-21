package it.unifi.ing.drivehub.business.core;

import it.unifi.ing.drivehub.dao.interfaces.DaoFactory;
import it.unifi.ing.drivehub.dao.interfaces.UnitOfWork;

import java.util.Objects;
import java.util.function.Function;

public final class TransactionRunner {
    private final DaoFactory daoFactory;

    public TransactionRunner(DaoFactory daoFactory) {
        this.daoFactory = Objects.requireNonNull(daoFactory, "daoFactory");
    }

    public <T> T execute(Function<UnitOfWork, T> work) {
        Objects.requireNonNull(work, "work");
        try (UnitOfWork unit = daoFactory.begin()) {
            try {
                T result = work.apply(unit);
                unit.commit();
                return result;
            } catch (RuntimeException | Error failure) {
                try {
                    unit.rollback();
                } catch (RuntimeException rollbackFailure) {
                    failure.addSuppressed(rollbackFailure);
                }
                throw failure;
            }
        }
    }
}
