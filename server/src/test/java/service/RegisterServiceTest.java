package service;

import dataaccess.DataAccess;
import dataaccess.DataAccessException;
import dataaccess.MemoryDataAccess;
import model.AuthData;
import model.UserData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class RegisterServiceTest {
    private RegisterService registerService;
    private DataAccess dataAccess;

    @BeforeEach
    void setUp() {
        dataAccess = new MemoryDataAccess();
        registerService = new RegisterService(dataAccess);
    }

    @Test
    void register_SuccessfulRegistration() throws DataAccessException {
        AuthData result = registerService.register("newUser", "password123", "user@example.com");

        assertNotNull(result);
        assertEquals("newUser", result.username());
        assertNotNull(result.authToken());
    }

    @Test
    void register_NullUsername() {
        assertThrows(DataAccessException.class, () ->
                registerService.register(null, "password123", "user@example.com")
        );
    }

    @Test
    void register_NullPassword() {
        assertThrows(DataAccessException.class, () ->
                registerService.register("newUser", null, "user@example.com")
        );
    }

    @Test
    void register_NullEmail() {
        assertThrows(DataAccessException.class, () ->
                registerService.register("newUser", "password123", null)
        );
    }

    @Test
    void register_DuplicateUsername() throws DataAccessException {
        registerService.register("existingUser", "password123", "user@example.com");

        assertThrows(DataAccessException.class, () ->
                registerService.register("existingUser", "newPassword", "new@example.com")
        );
    }

    @Test
    void register_UserPersistedInDataAccess() throws DataAccessException {
        registerService.register("newUser", "password123", "user@example.com");

        UserData retrievedUser = dataAccess.getUser("newUser");
        assertNotNull(retrievedUser);
        assertEquals("newUser", retrievedUser.username());
        assertEquals("password123", retrievedUser.password());
        assertEquals("user@example.com", retrievedUser.email());
    }
}