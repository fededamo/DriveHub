package it.unifi.ing.drivehub.dao.postgres;

import it.unifi.ing.drivehub.dao.interfaces.DataAccessException;

/**
 * Unchecked boundary exception used by the PostgreSQL adapters.
 */
public final class DatabaseException extends DataAccessException {

    public DatabaseException(String message, Throwable cause) {
        super(message, cause);
    }
}
