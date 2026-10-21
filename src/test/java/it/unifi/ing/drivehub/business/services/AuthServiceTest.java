package it.unifi.ing.drivehub.business.services;

import it.unifi.ing.drivehub.business.exceptions.ConflictException;
import it.unifi.ing.drivehub.business.security.PasswordHasher;
import it.unifi.ing.drivehub.dao.interfaces.BankAccountDao;
import it.unifi.ing.drivehub.dao.interfaces.DaoFactory;
import it.unifi.ing.drivehub.dao.interfaces.UnitOfWork;
import it.unifi.ing.drivehub.dao.interfaces.UserDao;
import it.unifi.ing.drivehub.domain.users.BankAccount;
import it.unifi.ing.drivehub.domain.users.Role;
import it.unifi.ing.drivehub.domain.users.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AuthServiceTest {
    private UnitOfWork unit;
    private UserDao users;
    private BankAccountDao bankAccounts;
    private PasswordHasher hasher;
    private AuthService service;

    @BeforeEach
    void setUp() {
        DaoFactory factory = mock(DaoFactory.class);
        unit = mock(UnitOfWork.class);
        users = mock(UserDao.class);
        bankAccounts = mock(BankAccountDao.class);
        hasher = mock(PasswordHasher.class);
        when(factory.begin()).thenReturn(unit);
        when(unit.users()).thenReturn(users);
        when(unit.bankAccounts()).thenReturn(bankAccounts);
        when(users.findByEmail(any())).thenReturn(Optional.empty());
        when(hasher.hash(any())).thenReturn("pbkdf2-encoded");
        when(users.save(any())).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.assignId(99L);
            return user;
        });
        when(bankAccounts.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        service = new AuthService(factory, hasher);
    }

    @ParameterizedTest(name = "registers {0}")
    @EnumSource(Role.class)
    @DisplayName("UC-REG-ALL: client, salesman and manager registration use one invariant path")
    void registersEveryRole(Role role) {
        User user = service.register(new RegistrationRequest("Maria", "Rossi",
                "maria@example.it", role), "password-123".toCharArray());

        assertEquals(role, user.role());
        assertEquals(99L, user.id());
        if (role == Role.CLIENT || role == Role.CUSTOMER) {
            verify(bankAccounts).save(any(BankAccount.class));
        }
        verify(unit).commit();
    }

    @Test
    @DisplayName("UC-REG-DUP: duplicate email aborts without hashing or saving")
    void rejectsDuplicateEmail() {
        when(users.findByEmail("maria@example.it"))
                .thenReturn(Optional.of(mock(User.class)));

        assertThrows(ConflictException.class, () -> service.register(
                new RegistrationRequest("Maria", "Rossi",
                        "maria@example.it", Role.CLIENT),
                "password-123".toCharArray()));

        verify(hasher, never()).hash(any());
        verify(users, never()).save(any());
        verify(unit).rollback();
    }
}
