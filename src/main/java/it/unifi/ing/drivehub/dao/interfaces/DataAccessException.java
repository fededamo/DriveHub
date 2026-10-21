package it.unifi.ing.drivehub.dao.interfaces;

/** Technology-neutral wrapper used by persistence adapters. */
public class DataAccessException extends RuntimeException {
    public DataAccessException(String message) {
        super(message);
    }

    public DataAccessException(String message, Throwable cause) {
        super(message, cause);
    }
}
