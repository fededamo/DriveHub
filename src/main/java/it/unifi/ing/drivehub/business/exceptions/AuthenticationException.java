package it.unifi.ing.drivehub.business.exceptions;

public final class AuthenticationException extends RuntimeException {
    public AuthenticationException(String message) {
        super(message);
    }
}
