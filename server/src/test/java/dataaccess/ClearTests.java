package dataaccess;

import chess.ChessGame;
import model.UserData;
import model.GameData;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

public class ClearTests {
    private static DataAccess dataAccess;

    @BeforeAll
    static void init() throws DataAccessException {
        dataAccess = new MySqlDataAccess();
    }

    @Test
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
}