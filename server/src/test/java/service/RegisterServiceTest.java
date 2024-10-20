package service;

import dataaccess.DataAccess;
import dataaccess.DataAccessException;
import dataaccess.MemoryDataAccess;
import model.AuthData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class RegisterServiceTest {
    private RegisterService registerService;

    @BeforeEach
    void setUp() {
        DataAccess dataAccess = new MemoryDataAccess();
        registerService = new RegisterService(dataAccess);
    }

    @Test
    void register_NormalRegistration() throws DataAccessException {
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

}