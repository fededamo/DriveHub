package it.unifi.ing.drivehub.dao.interfaces;

import it.unifi.ing.drivehub.domain.users.Role;
import it.unifi.ing.drivehub.domain.users.User;

import java.util.List;
import java.util.Optional;

public interface UserDao {
    User save(User user);
    void update(User user);
    void updateEmail(long id, Role role, String newEmail);
    void updatePassword(long id, Role role, String newPasswordHash);
    void delete(long id, Role role);
    Optional<User> findById(long id);
    Optional<User> findByEmail(String normalizedEmail);
    List<User> findByRole(Role role);
    List<User> findAll();
    boolean hasOperations(long userId, Role role);
}
