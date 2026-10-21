package it.unifi.ing.drivehub.domain.users;

import it.unifi.ing.drivehub.domain.BaseEntity;

import java.util.Locale;
import java.util.Objects;
import java.util.regex.Pattern;

public final class User extends BaseEntity {
    private static final Pattern EMAIL = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");

    private final String name;
    private final String surname;
    private final String email;
    private final String passwordHash;
    private final Role role;

    public User(Long id, String name, String surname, String email, String passwordHash, Role role) {
        super(id);
        this.name = requireText(name, "name");
        this.surname = requireText(surname, "surname");
        this.email = normalizeEmail(email);
        this.passwordHash = requireText(passwordHash, "passwordHash");
        this.role = Objects.requireNonNull(role, "role");
    }

    public static User create(String name, String surname, String email, String passwordHash, Role role) {
        return new User(null, name, surname, email, passwordHash, role);
    }

    public String name() {
        return name;
    }

    public String surname() {
        return surname;
    }

    public String firstName() {
        return name;
    }

    public String lastName() {
        return surname;
    }

    public String email() {
        return email;
    }

    public String passwordHash() {
        return passwordHash;
    }

    public Role role() {
        return role;
    }

    public String fullName() {
        return name + " " + surname;
    }

    public String displayName() {
        return fullName();
    }

    private static String requireText(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " must not be blank");
        }
        return value.trim();
    }

    private static String normalizeEmail(String email) {
        String trimmed = requireText(email, "email").toLowerCase(Locale.ROOT);
        if (!EMAIL.matcher(trimmed).matches()) {
            throw new IllegalArgumentException("invalid email: " + email);
        }
        return trimmed;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User user)) return false;
        return Objects.equals(email, user.email);
    }

    @Override
    public int hashCode() {
        return Objects.hash(email);
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id() +
                ", name='" + name + '\'' +
                ", surname='" + surname + '\'' +
                ", email='" + email + '\'' +
                ", role=" + role +
                '}';
    }
}
