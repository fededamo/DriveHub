package it.unifi.drivehub.security;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Objects;

/**
 * Implementazione di hashing password conforme a PBKDF2WithHmacSHA256,
 * senza dipendenze esterne proprietarie, pienamente compatibile con gli hash esistenti.
 */
public final class Pbkdf2PasswordHasher implements PasswordHasher {

    private static final String PREFIX = "pbkdf2-sha256";
    private static final String ALGORITHM = "PBKDF2WithHmacSHA256";
    private static final int DEFAULT_ITERATIONS = 210_000;
    private static final int DEFAULT_SALT_BYTES = 16;
    private static final int DEFAULT_KEY_BITS = 256;

    private final int iterations;
    private final int saltBytes;
    private final int keyBits;
    private final SecureRandom random;

    public Pbkdf2PasswordHasher() {
        this(DEFAULT_ITERATIONS, DEFAULT_SALT_BYTES, DEFAULT_KEY_BITS, new SecureRandom());
    }

    public Pbkdf2PasswordHasher(int iterations, int saltBytes, int keyBits, SecureRandom random) {
        if (iterations < 10_000 || saltBytes < 16 || keyBits < 128) {
            throw new IllegalArgumentException("Parametri PBKDF2 non conformi agli standard minimi di sicurezza.");
        }
        this.iterations = iterations;
        this.saltBytes = saltBytes;
        this.keyBits = keyBits;
        this.random = Objects.requireNonNull(random, "random");
    }

    @Override
    public String hash(String rawPassword) {
        if (rawPassword == null || rawPassword.isEmpty()) {
            throw new IllegalArgumentException("La password non può essere vuota.");
        }
        byte[] salt = new byte[saltBytes];
        random.nextBytes(salt);
        byte[] derived = derive(rawPassword.toCharArray(), salt, iterations, keyBits);
        return PREFIX + "$" + iterations + "$" + Base64.getEncoder().encodeToString(salt)
                + "$" + Base64.getEncoder().encodeToString(derived);
    }

    @Override
    public boolean verify(String rawPassword, String encodedHash) {
        if (rawPassword == null || encodedHash == null) {
            return false;
        }
        try {
            String[] parts = encodedHash.split("\\$", -1);
            if (parts.length != 4 || !PREFIX.equals(parts[0])) {
                return false;
            }
            int encodedIterations = Integer.parseInt(parts[1]);
            byte[] salt = Base64.getDecoder().decode(parts[2]);
            byte[] expected = Base64.getDecoder().decode(parts[3]);
            byte[] actual = derive(rawPassword.toCharArray(), salt, encodedIterations, expected.length * 8);
            return MessageDigest.isEqual(expected, actual);
        } catch (Exception e) {
            return false;
        }
    }

    private static byte[] derive(char[] password, byte[] salt, int iterations, int keyBits) {
        try {
            PBEKeySpec spec = new PBEKeySpec(password, salt, iterations, keyBits);
            SecretKeyFactory factory = SecretKeyFactory.getInstance(ALGORITHM);
            return factory.generateSecret(spec).getEncoded();
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("Errore durante la derivazione della chiave PBKDF2", e);
        }
    }
}

