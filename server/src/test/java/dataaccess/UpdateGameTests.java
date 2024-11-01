package dataaccess;

import chess.ChessGame;
import model.GameData;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

public class UpdateGameTests {
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
    @DisplayName("Update Game - Positive")
    void updateGamePositive() throws DataAccessException {
        GameData game = new GameData(1, null, null, "testGame", new ChessGame());
        dataAccess.createGame(game);

        GameData updatedGame = new GameData(1, "white", "black", "testGame", game.game());
        dataAccess.updateGame(updatedGame);

        GameData retrieved = dataAccess.getGame(1);
        assertNotNull(retrieved);
        assertEquals("white", retrieved.whiteUsername());
        assertEquals("black", retrieved.blackUsername());
    }

    @Test
    @DisplayName("Update Game - Negative (Non-existent)")
    void updateGameNegative() {
        GameData nonExistentGame = new GameData(-1, "white", "black", "testGame", new ChessGame());
        assertThrows(DataAccessException.class, () -> dataAccess.updateGame(nonExistentGame));
    }
}