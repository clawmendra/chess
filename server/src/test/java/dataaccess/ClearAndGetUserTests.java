package dataaccess;

import chess.ChessGame;
import model.UserData;
import model.GameData;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

public class ClearAndGetUserTests {
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
    @DisplayName("Clear - Positive")
    void clearPositive() throws DataAccessException {
        // Create test data
        UserData user = new UserData("testUser", "password", "email");
        dataAccess.createUser(user);
        GameData game = new GameData(1, null, null, "testGame", new ChessGame());
        dataAccess.createGame(game);

        // Clear
        dataAccess.clear();

        // Verify
        assertNull(dataAccess.getUser("testUser"));
        assertNull(dataAccess.getGame(1));
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