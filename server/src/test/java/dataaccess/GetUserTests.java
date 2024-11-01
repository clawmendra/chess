package dataaccess;

import model.UserData;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

public class GetUserTests {
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
    @DisplayName("Get User - Positive")
    void getUserPositive() throws DataAccessException {
        UserData user = new UserData("testUser", "password", "email");
        dataAccess.createUser(user);

        UserData retrieved = dataAccess.getUser("testUser");
        assertNotNull(retrieved);
        assertEquals("testUser", retrieved.username());
    }

    @Test
    @DisplayName("Get User - Negative (Non-existent)")
    void getUserNegative() throws DataAccessException {
        UserData retrieved = dataAccess.getUser("nonexistentUser");
        assertNull(retrieved);
    }
}