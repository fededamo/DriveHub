package it.unifi.ing.drivehub.business.security;

public interface PasswordHasher {
    String hash(char[] rawPassword);

    boolean verify(char[] rawPassword, String encodedHash);
}
