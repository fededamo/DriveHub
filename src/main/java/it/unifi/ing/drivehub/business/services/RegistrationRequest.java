package it.unifi.ing.drivehub.business.services;

import it.unifi.ing.drivehub.domain.users.Role;

public record RegistrationRequest(
        String firstName,
        String lastName,
        String email,
        Role role
) {
    public RegistrationRequest(String firstName, String lastName, String email) {
        this(firstName, lastName, email, Role.CLIENT);
    }
}
