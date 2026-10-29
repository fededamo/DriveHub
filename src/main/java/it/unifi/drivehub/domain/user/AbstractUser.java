package it.unifi.drivehub.domain.user;

import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Classe base astratta per gli utenti del sistema.
 * Fattorizza gli attributi e i comportamenti comuni a tutti gli utenti (DRY),
 * mantenendo l'astrazione basata sull'interfaccia User.
 */
public abstract class AbstractUser implements User {

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$");

    protected final Long id;
    protected final String name;
    protected final String surname;
    protected String email;
    protected String hashPassword;

    protected AbstractUser(Long id, String name, String surname, String email, String hashPassword) {
        validateAttributes(name, surname, email, hashPassword);
        this.id = id;
        this.name = name.trim();
        this.surname = surname.trim();
        this.email = email.trim().toLowerCase();
        this.hashPassword = hashPassword;
    }

    protected static void validateAttributes(String name, String surname, String email, String hashPassword) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Il nome non può essere vuoto.");
        }
        if (surname == null || surname.trim().isEmpty()) {
            throw new IllegalArgumentException("Il cognome non può essere vuoto.");
        }
        if (email == null || !EMAIL_PATTERN.matcher(email.trim()).matches()) {
            throw new IllegalArgumentException("Formato email non valido: " + email);
        }
        if (hashPassword == null || hashPassword.trim().isEmpty()) {
            throw new IllegalArgumentException("L'hash della password non può essere vuoto.");
        }
    }

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getSurname() {
        return surname;
    }

    @Override
    public String getEmail() {
        return email;
    }

    @Override
    public void updateEmail(String newEmail) {
        if (newEmail == null || !EMAIL_PATTERN.matcher(newEmail.trim()).matches()) {
            throw new IllegalArgumentException("Nuovo formato email non valido: " + newEmail);
        }
        this.email = newEmail.trim().toLowerCase();
    }

    @Override
    public String getHashPassword() {
        return hashPassword;
    }

    @Override
    public void updateHashPassword(String newHashPassword) {
        if (newHashPassword == null || newHashPassword.trim().isEmpty()) {
            throw new IllegalArgumentException("Il nuovo hash della password non può essere vuoto.");
        }
        this.hashPassword = newHashPassword;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AbstractUser that = (AbstractUser) o;
        if (id != null && that.id != null) {
            return Objects.equals(id, that.id);
        }
        if (id == null && that.id == null) {
            return Objects.equals(email, that.email);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return id != null ? Objects.hash(id) : Objects.hash(email);
    }
}

