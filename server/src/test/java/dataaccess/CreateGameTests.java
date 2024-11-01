package dataaccess;

import chess.ChessGame;
import model.GameData;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

public class CreateGameTests {
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
    void createGamePositive() throws DataAccessException {
        GameData game = new GameData(1, null, null, "testGame", new ChessGame());
        dataAccess.createGame(game);

        GameData retrieved = dataAccess.getGame(1);
        assertNotNull(retrieved);
        assertEquals("testGame", retrieved.gameName());
    }

    @Test
    void createGameNegative() throws DataAccessException {
        GameData game1 = new GameData(1, null, null, "testGame1", new ChessGame());
        dataAccess.createGame(game1);

        GameData game2 = new GameData(1, null, null, "testGame2", new ChessGame());
        assertThrows(DataAccessException.class, () -> dataAccess.createGame(game2));
    }
}