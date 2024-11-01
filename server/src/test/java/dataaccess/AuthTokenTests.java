package dataaccess;

import model.UserData;
import model.AuthData;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

public class AuthTokenTests {
    private static DataAccess dataAccess;

    @BeforeAll
    static void init() throws DataAccessException {
        dataAccess = new MySqlDataAccess();
    }

    @BeforeEach
    void setUp() throws DataAccessException {
        dataAccess.clear();
    }

    @Test
    @DisplayName("Create Auth - Positive")
    void createAuthPositive() throws DataAccessException {
        UserData user = new UserData("testUser", "password", "email");
        dataAccess.createUser(user);

        AuthData auth = new AuthData("testAuth", "testUser");
        dataAccess.createAuth(auth);

        AuthData retrieved = dataAccess.getAuth("testAuth");
        assertNotNull(retrieved);
        assertEquals("testUser", retrieved.username());
    }

    @Test
    @DisplayName("Create Auth - Negative (Non-existent User)")
    void createAuthNegative() {
        AuthData auth = new AuthData("testAuth", "nonexistentUser");
        assertThrows(DataAccessException.class, () -> dataAccess.createAuth(auth));
    }

    @Test
    @DisplayName("Delete Auth - Positive")
    void deleteAuthPositive() throws DataAccessException {
        UserData user = new UserData("testUser", "password", "email");
        dataAccess.createUser(user);
        AuthData auth = new AuthData("testAuth", "testUser");
        dataAccess.createAuth(auth);

        dataAccess.deleteAuth("testAuth");
        assertNull(dataAccess.getAuth("testAuth"));
    }

    @Test
    @DisplayName("Delete Auth - Negative (Non-existent)")
    void deleteAuthNegative() throws DataAccessException {
        assertDoesNotThrow(() -> dataAccess.deleteAuth("nonexistentAuth"));
    }
}