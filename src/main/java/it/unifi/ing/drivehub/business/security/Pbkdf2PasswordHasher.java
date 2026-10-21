package it.unifi.ing.drivehub.business.security;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Objects;

/** PBKDF2-HMAC-SHA256 password storage implemented only with the standard JDK. */
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
            throw new IllegalArgumentException("PBKDF2 parameters are below the accepted minimum");
        }
        this.iterations = iterations;
        this.saltBytes = saltBytes;
        this.keyBits = keyBits;
        this.random = Objects.requireNonNull(random, "random");
    }

    @Override
    public String hash(char[] rawPassword) {
        validatePassword(rawPassword);
        byte[] salt = new byte[saltBytes];
        random.nextBytes(salt);
        byte[] derived = derive(rawPassword, salt, iterations, keyBits);
        return PREFIX + "$" + iterations + "$" + Base64.getEncoder().encodeToString(salt)
                + "$" + Base64.getEncoder().encodeToString(derived);
    }

    @Override
    public boolean verify(char[] rawPassword, String encodedHash) {
        if (rawPassword == null || encodedHash == null) {
            return false;
        }
        try {
            String[] parts = encodedHash.split("\\$", -1);
            if (parts.length != 4 || !PREFIX.equals(parts[0])) {
                return false;
            }
            int encodedIterations = Integer.parseInt(parts[1]);
            if (encodedIterations < 10_000 || encodedIterations > 10_000_000) {
                return false;
            }
            byte[] salt = Base64.getDecoder().decode(parts[2]);
            byte[] expected = Base64.getDecoder().decode(parts[3]);
            if (salt.length < 16 || expected.length < 16) {
                return false;
            }
            byte[] actual = derive(rawPassword, salt, encodedIterations, expected.length * Byte.SIZE);
            return MessageDigest.isEqual(expected, actual);
        } catch (IllegalArgumentException malformed) {
            return false;
        }
    }

    private static byte[] derive(char[] rawPassword, byte[] salt, int iterations, int keyBits) {
        PBEKeySpec spec = new PBEKeySpec(rawPassword, salt, iterations, keyBits);
        try {
            return SecretKeyFactory.getInstance(ALGORITHM).generateSecret(spec).getEncoded();
        } catch (GeneralSecurityException unavailable) {
            throw new IllegalStateException("PBKDF2-HMAC-SHA256 is unavailable", unavailable);
        } finally {
            spec.clearPassword();
        }
    }

    private static void validatePassword(char[] password) {
        Objects.requireNonNull(password, "rawPassword");
        if (password.length < 8) {
            throw new IllegalArgumentException("password must contain at least 8 characters");
        }
        boolean onlyWhitespace = true;
        for (char character : password) {
            if (!Character.isWhitespace(character)) {
                onlyWhitespace = false;
                break;
            }
        }
        if (onlyWhitespace) {
            throw new IllegalArgumentException("password must not be blank");
        }
    }
}
