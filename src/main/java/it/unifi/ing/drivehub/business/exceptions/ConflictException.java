package it.unifi.ing.drivehub.business.exceptions;

public final class ConflictException extends RuntimeException {
    public ConflictException(String message) {
        super(message);
    }
}
