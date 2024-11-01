package dataaccess;

import chess.ChessGame;
import model.GameData;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

public class ListGamesTests {
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
    @DisplayName("List Games - Positive")
    void listGamesPositive() throws DataAccessException {
        GameData game1 = new GameData(1, null, null, "testGame1", new ChessGame());
        GameData game2 = new GameData(2, null, null, "testGame2", new ChessGame());
        dataAccess.createGame(game1);
        dataAccess.createGame(game2);

        GameData[] games = dataAccess.listGames();
        assertEquals(2, games.length);
    }

    @Test
    @DisplayName("List Games - Negative (Empty)")
    void listGamesNegative() throws DataAccessException {
        GameData[] games = dataAccess.listGames();
        assertEquals(0, games.length);
    }
}