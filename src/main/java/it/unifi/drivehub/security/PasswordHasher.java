package it.unifi.drivehub.security;

/**
 * Interfaccia per l'hashing e la verifica sicura delle password.
 */
public interface PasswordHasher {
    String hash(String rawPassword);
    boolean verify(String rawPassword, String encodedHash);
}

