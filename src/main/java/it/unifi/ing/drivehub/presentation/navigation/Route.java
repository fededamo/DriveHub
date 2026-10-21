package it.unifi.ing.drivehub.presentation.navigation;

import it.unifi.ing.drivehub.domain.users.Role;

import java.util.EnumSet;
import java.util.Set;

public enum Route {
    WELCOME("Welcome.fxml", EnumSet.noneOf(Role.class)),
    LOGIN("Login.fxml", EnumSet.noneOf(Role.class)),
    REGISTER("Register.fxml", EnumSet.noneOf(Role.class)),
    CUSTOMER("CustomerWorkspace.fxml", EnumSet.of(Role.CLIENT, Role.CUSTOMER)),
    SALESMAN("SalesmanWorkspace.fxml", EnumSet.of(Role.SALESMAN)),
    MANAGER("ManagerWorkspace.fxml", EnumSet.of(Role.MANAGER));

    private final String fxml;
    private final Set<Role> allowedRoles;

    Route(String fxml, Set<Role> allowedRoles) {
        this.fxml = fxml;
        this.allowedRoles = Set.copyOf(allowedRoles);
    }

    public String fxml() {
        return fxml;
    }

    public boolean isPublic() {
        return allowedRoles.isEmpty();
    }

    public boolean allows(Role role) {
        return allowedRoles.contains(role);
    }
}
