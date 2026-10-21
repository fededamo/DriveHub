package it.unifi.ing.drivehub.business.banking;

import java.math.BigInteger;
import java.util.Locale;

/**
 * Generatore deterministico e conforme di IBAN italiani (ISO 13616 / ABI).
 * Formato: IT + 2 cifre controllo (ISO 7064) + 1 lettera CIN + 5 cifre ABI + 5 cifre CAB + 12 cifre Conto.
 */
public final class IbanGenerator {

    private static final String COUNTRY_CODE = "IT";
    private static final String DEFAULT_ABI = "05034"; // DriveHub Banca Partner
    private static final String DEFAULT_CAB = "01600";

    // Pesi per il calcolo del carattere CIN italiano (posizioni dispari, 1-indexed)
    private static final int[] ODD_WEIGHTS = {
            1, 0, 5, 7, 9, 13, 15, 17, 19, 21, // 0-9
            1, 0, 5, 7, 9, 13, 15, 17, 19, 21, // A-J
            2, 4, 18, 20, 11, 3, 6, 8, 12, 14, // K-T
            16, 10, 22, 25, 24, 23             // U-Z
    };

    // Pesi per il calcolo del carattere CIN italiano (posizioni pari, 1-indexed)
    private static final int[] EVEN_WEIGHTS = {
            0, 1, 2, 3, 4, 5, 6, 7, 8, 9,       // 0-9
            0, 1, 2, 3, 4, 5, 6, 7, 8, 9,       // A-J
            10, 11, 12, 13, 14, 15, 16, 17, 18, 19, // K-T
            20, 21, 22, 23, 24, 25             // U-Z
    };

    private IbanGenerator() {
    }

    public static String generate() {
        return generateForClient(java.util.concurrent.ThreadLocalRandom.current().nextLong(1, 999_999_999L));
    }

    /**
     * Genera un IBAN italiano valido di 27 caratteri a partire dall'identificativo del cliente.
     */
    public static String generateForClient(long clientId) {
        if (clientId <= 0) {
            throw new IllegalArgumentException("clientId must be positive");
        }
        String accountNumber = String.format(Locale.ROOT, "%012d", clientId);
        String bbanBase = DEFAULT_ABI + DEFAULT_CAB + accountNumber; // 22 caratteri
        char cin = calculateCin(bbanBase);
        String bban = cin + bbanBase; // 23 caratteri: CIN (1) + ABI (5) + CAB (5) + Conto (12)

        // Calcolo check digits ISO 7064 MOD 97-10
        // IT = 18 28 00
        StringBuilder numeric = new StringBuilder();
        for (char c : bban.toCharArray()) {
            if (c >= '0' && c <= '9') {
                numeric.append(c);
            } else if (c >= 'A' && c <= 'Z') {
                numeric.append((int) c - 'A' + 10);
            }
        }
        numeric.append("182800"); // IT = 18 28 00
        BigInteger number = new BigInteger(numeric.toString());
        int remainder = number.remainder(BigInteger.valueOf(97)).intValue();
        int checkDigits = 98 - remainder;
        String checkDigitsStr = String.format(Locale.ROOT, "%02d", checkDigits);

        return COUNTRY_CODE + checkDigitsStr + bban;
    }

    private static char calculateCin(String bbanBase) {
        int sum = 0;
        for (int i = 0; i < bbanBase.length(); i++) {
            char c = bbanBase.charAt(i);
            int index = charToIndex(c);
            if ((i + 1) % 2 != 0) { // Dispari (1-indexed)
                sum += ODD_WEIGHTS[index];
            } else { // Pari
                sum += EVEN_WEIGHTS[index];
            }
        }
        int cinIndex = sum % 26;
        return (char) ('A' + cinIndex);
    }

    private static int charToIndex(char c) {
        if (c >= '0' && c <= '9') {
            return c - '0';
        }
        if (c >= 'A' && c <= 'Z') {
            return (c - 'A') + 10;
        }
        if (c >= 'a' && c <= 'z') {
            return (c - 'a') + 10;
        }
        throw new IllegalArgumentException("Invalid BBAN character: " + c);
    }
}
