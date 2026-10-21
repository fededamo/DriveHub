package it.unifi.ing.drivehub.dao.interfaces;

@FunctionalInterface
public interface DaoFactory {
    UnitOfWork begin();
}
