package dataaccess;

import model.UserData;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

public class CreateUserTests {
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
    void createUserPositive() throws DataAccessException {
        UserData user = new UserData("testUser", "password", "email");
        dataAccess.createUser(user);

        UserData retrieved = dataAccess.getUser("testUser");
        assertNotNull(retrieved);
        assertEquals("testUser", retrieved.username());
    }

    @Test
    void createUserNegative() throws DataAccessException {
        UserData user = new UserData("testUser", "password", "email");
        dataAccess.createUser(user);

        assertThrows(DataAccessException.class, () ->
                dataAccess.createUser(new UserData("testUser", "password2", "email2"))
        );
    }
}