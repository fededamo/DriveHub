package it.unifi.ing.drivehub.business.services;

import it.unifi.ing.drivehub.business.banking.IbanGenerator;
import it.unifi.ing.drivehub.business.core.TransactionRunner;
import it.unifi.ing.drivehub.business.exceptions.AuthenticationException;
import it.unifi.ing.drivehub.business.exceptions.ConflictException;
import it.unifi.ing.drivehub.business.exceptions.EntityNotFoundException;
import it.unifi.ing.drivehub.business.security.PasswordHasher;
import it.unifi.ing.drivehub.dao.interfaces.DaoFactory;
import it.unifi.ing.drivehub.domain.DomainRuleViolationException;
import it.unifi.ing.drivehub.domain.users.BankAccount;
import it.unifi.ing.drivehub.domain.users.Role;
import it.unifi.ing.drivehub.domain.users.User;

import java.math.BigDecimal;
import java.util.Locale;
import java.util.Objects;

public final class AuthService {
    private final TransactionRunner transactions;
    private final PasswordHasher passwordHasher;

    public AuthService(DaoFactory daoFactory, PasswordHasher passwordHasher) {
        this.transactions = new TransactionRunner(daoFactory);
        this.passwordHasher = Objects.requireNonNull(passwordHasher, "passwordHasher");
    }

    public User register(RegistrationRequest request, char[] rawPassword) {
        Objects.requireNonNull(request, "request");
        String normalizedEmail = request.email().trim().toLowerCase(Locale.ROOT);
        return transactions.execute(unit -> {
            if (unit.users().findByEmail(normalizedEmail).isPresent()) {
                throw new ConflictException("email is already registered: " + normalizedEmail);
            }
            String encodedPassword = passwordHasher.hash(rawPassword);
            User user = User.create(request.firstName(), request.lastName(), normalizedEmail,
                    encodedPassword, request.role());
            User saved = unit.users().save(user);

            // Generazione automatica del conto bancario per i clienti con saldo iniziale 0
            if (saved.role() == Role.CLIENT || saved.role() == Role.CUSTOMER) {
                String iban = IbanGenerator.generateForClient(saved.requireId());
                BankAccount account = BankAccount.create(iban, saved.requireId(), BigDecimal.ZERO.setScale(2));
                unit.bankAccounts().save(account);
            }
            return saved;
        });
    }

    public User login(String email, char[] rawPassword) {
        String normalizedEmail = email == null ? "" : email.trim().toLowerCase(Locale.ROOT);
        return transactions.execute(unit -> {
            User user = unit.users().findByEmail(normalizedEmail)
                    .orElseThrow(() -> new AuthenticationException("invalid email or password"));
            if (!passwordHasher.verify(rawPassword, user.passwordHash())) {
                throw new AuthenticationException("invalid email or password");
            }
            return user;
        });
    }

    public void updateEmail(long userId, Role role, char[] currentPassword, String newEmail) {
        Objects.requireNonNull(newEmail, "newEmail");
        String normalizedEmail = newEmail.trim().toLowerCase(Locale.ROOT);
        transactions.execute(unit -> {
            User user = unit.users().findById(userId)
                    .orElseThrow(() -> new EntityNotFoundException("user", userId));
            if (!passwordHasher.verify(currentPassword, user.passwordHash())) {
                throw new AuthenticationException("Password attuale non corretta");
            }
            if (unit.users().findByEmail(normalizedEmail).isPresent() && !user.email().equalsIgnoreCase(normalizedEmail)) {
                throw new ConflictException("L'indirizzo email è già utilizzato da un altro utente");
            }
            unit.users().updateEmail(userId, role, normalizedEmail);
            return null;
        });
    }

    public void updatePassword(long userId, Role role, char[] currentPassword, char[] newPassword) {
        transactions.execute(unit -> {
            User user = unit.users().findById(userId)
                    .orElseThrow(() -> new EntityNotFoundException("user", userId));
            if (!passwordHasher.verify(currentPassword, user.passwordHash())) {
                throw new AuthenticationException("Password attuale non corretta");
            }
            if (newPassword == null || newPassword.length == 0) {
                throw new IllegalArgumentException("La nuova password non può essere vuota");
            }
            String newHash = passwordHasher.hash(newPassword);
            unit.users().updatePassword(userId, role, newHash);
            return null;
        });
    }

    public void deleteAccount(long userId, Role role, char[] currentPassword) {
        transactions.execute(unit -> {
            User user = unit.users().findById(userId)
                    .orElseThrow(() -> new EntityNotFoundException("user", userId));
            if (!passwordHasher.verify(currentPassword, user.passwordHash())) {
                throw new AuthenticationException("Password attuale non corretta");
            }
            // Controllo: non devono essere presenti tuple con quell'utente nelle tabelle TestDrive, Rent, Sale
            if (unit.users().hasOperations(userId, role)) {
                throw new DomainRuleViolationException("Impossibile eliminare l'account: sono presenti operazioni registrate (Test Drive, Noleggio o Compravendita) collegate all'utente.");
            }
            unit.users().delete(userId, role);
            return null;
        });
    }
}
