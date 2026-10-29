package it.unifi.drivehub.domain.user;

import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Entità Manager (Dirigente).
 * Implementa User con poteri amministrativi sul catalogo (brand, modelli, veicoli e sconti).
 */
public final class Manager implements User {

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$");

    private final Long id;
    private final String name;
    private final String surname;
    private String email;
    private String hashPassword;

    private Manager(Long id, String name, String surname, String email, String hashPassword) {
        this.id = id;
        this.name = name;
        this.surname = surname;
        this.email = email;
        this.hashPassword = hashPassword;
    }

    public static Manager create(Long id, String name, String surname, String email, String hashPassword) {
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
        return new Manager(id, name.trim(), surname.trim(), email.trim().toLowerCase(), hashPassword);
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

    public void updateHashPassword(String newHashPassword) {
        if (newHashPassword == null || newHashPassword.trim().isEmpty()) {
            throw new IllegalArgumentException("Il nuovo hash della password non può essere vuoto.");
        }
        this.hashPassword = newHashPassword;
    }

    @Override
    public String getRoleDisplayName() {
        return "Manager";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Manager manager)) return false;
        return Objects.equals(id, manager.id) || Objects.equals(email, manager.email);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, email);
    }

    @Override
    public String toString() {
        return "Manager{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", surname='" + surname + '\'' +
                ", email='" + email + '\'' +
                '}';
    }
}

