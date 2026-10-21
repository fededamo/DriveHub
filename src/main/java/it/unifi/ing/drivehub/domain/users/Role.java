package it.unifi.ing.drivehub.domain.users;

public enum Role {
    CLIENT,
    CUSTOMER,
    SALESMAN,
    MANAGER;

    public static Role fromString(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim().toUpperCase(java.util.Locale.ROOT);
        if ("CUSTOMER".equals(normalized)) {
            return CLIENT;
        }
        return Role.valueOf(normalized);
    }
}
