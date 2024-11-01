package dataaccess;

import chess.ChessGame;
import model.GameData;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

public class MySqlGameTests {
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

    @Test
    void getGamePositive() throws DataAccessException {
        GameData game = new GameData(1, null, null, "testGame", new ChessGame());
        dataAccess.createGame(game);

        GameData retrieved = dataAccess.getGame(1);
        assertNotNull(retrieved);
        assertEquals("testGame", retrieved.gameName());
    }

    @Test
    void getGameNegative() throws DataAccessException {
        GameData retrieved = dataAccess.getGame(-1);
        assertNull(retrieved);
    }

    @Test
    void listGamesPositive() throws DataAccessException {
        GameData game1 = new GameData(1, null, null, "testGame1", new ChessGame());
        GameData game2 = new GameData(2, null, null, "testGame2", new ChessGame());
        dataAccess.createGame(game1);
        dataAccess.createGame(game2);

        GameData[] games = dataAccess.listGames();
        assertEquals(2, games.length);
    }

    @Test
    void listGamesNegative() throws DataAccessException {
        GameData[] games = dataAccess.listGames();
        assertEquals(0, games.length);
    }

    @Test
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
    void updateGameNegative() {
        GameData nonExistentGame = new GameData(-1, "white", "black", "testGame", new ChessGame());
        assertThrows(DataAccessException.class, () -> dataAccess.updateGame(nonExistentGame));
    }
}