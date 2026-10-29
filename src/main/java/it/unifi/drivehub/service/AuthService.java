package it.unifi.drivehub.service;

import it.unifi.drivehub.dao.BankAccountDao;
import it.unifi.drivehub.dao.DatabaseManager;
import it.unifi.drivehub.dao.UserDao;
import it.unifi.drivehub.domain.user.BankAccount;
import it.unifi.drivehub.domain.user.Client;
import it.unifi.drivehub.domain.user.User;
import it.unifi.drivehub.security.PasswordHasher;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.util.Optional;

/**
 * Servizio per l'autenticazione, la registrazione e la gestione dell'account.
 */
public final class AuthService {

    private final DatabaseManager db;
    private final UserDao userDao;
    private final BankAccountDao bankAccountDao;
    private final PasswordHasher hasher;
    private final SecureRandom random = new SecureRandom();

    public AuthService(DatabaseManager db, UserDao userDao, BankAccountDao bankAccountDao, PasswordHasher hasher) {
        this.db = db;
        this.userDao = userDao;
        this.bankAccountDao = bankAccountDao;
        this.hasher = hasher;
    }

    public User login(String email, String rawPassword) {
        if (email == null || rawPassword == null) {
            throw new IllegalArgumentException("Email e password sono obbligatorie.");
        }
        Optional<User> userOpt = userDao.findByEmail(email);
        if (userOpt.isEmpty()) {
            throw new IllegalArgumentException("Credenziali non valide: utente non trovato.");
        }
        User user = userOpt.get();
        if (!hasher.verify(rawPassword, user.getHashPassword())) {
            throw new IllegalArgumentException("Credenziali non valide: password errata.");
        }

        // Se è un cliente, carica il conto bancario on-demand
        if (user instanceof Client client) {
            bankAccountDao.findByClientId(client.getId()).ifPresent(client::setBankAccount);
        }
        return user;
    }

    public Client registerClient(String name, String surname, String email, String rawPassword) {
        String hash = hasher.hash(rawPassword);
        Client clientToSave = Client.createWithoutAccount(null, name, surname, email, hash);

        return db.executeInTransactionWithResult(conn -> {
            Client savedClient = userDao.saveClient(clientToSave, conn);
            String generatedIban = generateUniqueIban();
            BankAccount initialAccount = BankAccount.createInitial(generatedIban, savedClient.getId());
            bankAccountDao.save(initialAccount, conn);
            savedClient.setBankAccount(initialAccount);
            return savedClient;
        });
    }

    public void changeEmail(Long userId, String role, String newEmail, String currentPassword) {
        verifyPasswordBeforeAction(userId, currentPassword);
        db.executeInTransaction(conn -> userDao.updateEmail(userId, role, newEmail.trim().toLowerCase(), conn));
    }

    public void changePassword(Long userId, String role, String newRawPassword, String currentPassword) {
        verifyPasswordBeforeAction(userId, currentPassword);
        String newHash = hasher.hash(newRawPassword);
        db.executeInTransaction(conn -> userDao.updatePassword(userId, role, newHash, conn));
    }

    public void deleteAccount(Long userId, String role, String currentPassword) {
        verifyPasswordBeforeAction(userId, currentPassword);
        if (userDao.hasAssociatedOperations(userId, role)) {
            throw new IllegalStateException("Impossibile eliminare l'account: sono presenti operazioni storiche associate (Test Drive, Noleggi o Vendite).");
        }
        db.executeInTransaction(conn -> userDao.deleteUser(userId, role, conn));
    }

    public void topUpClientBalance(Long clientId, BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("L'importo da ricaricare deve essere maggiore di zero.");
        }
        db.executeInTransaction(conn -> {
            BankAccount account = bankAccountDao.findByClientId(clientId, conn)
                    .orElseThrow(() -> new IllegalStateException("Conto bancario non trovato per il cliente id: " + clientId));
            account.deposit(amount);
            bankAccountDao.updateBalance(account.getIban(), account.getBalance(), conn);
        });
    }

    public BankAccount getClientBankAccount(Long clientId) {
        return bankAccountDao.findByClientId(clientId)
                .orElseThrow(() -> new IllegalStateException("Conto bancario non trovato per il cliente id: " + clientId));
    }

    private void verifyPasswordBeforeAction(Long userId, String currentPassword) {
        User user = userDao.findById(userId)
                .orElseThrow(() -> new IllegalStateException("Utente non trovato con id: " + userId));
        if (!hasher.verify(currentPassword, user.getHashPassword())) {
            throw new IllegalArgumentException("Password attuale errata: operazione annullata.");
        }
    }

    private String generateUniqueIban() {
        // Formato italiano standard: IT + 2 cifre controllo + 1 lettera CIN + 5 cifre ABI + 5 cifre CAB + 12 caratteri c/c
        long randomNum = Math.abs(random.nextLong()) % 1_000_000_000_000L;
        return String.format("IT%02d%c%05d%05d%012d",
                10 + random.nextInt(89),
                (char) ('A' + random.nextInt(26)),
                5034,
                1600,
                randomNum);
    }
}

