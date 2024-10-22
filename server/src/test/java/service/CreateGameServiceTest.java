package service;

import dataaccess.DataAccess;
import dataaccess.DataAccessException;
import dataaccess.MemoryDataAccess;
import model.AuthData;
import model.GameData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;


class CreateGameServiceTest {
    private CreateGameService createGameService;
    private DataAccess dataAccess;
    private String existAuthToken;

    @BeforeEach
    void setUp() throws DataAccessException {
        dataAccess = new MemoryDataAccess();
        createGameService = new CreateGameService(dataAccess);
        String username = "testUser";
        existAuthToken = "testAuthToken";
        dataAccess.createAuth(new AuthData(existAuthToken, username));
    }

    @Test
    void gameWorks() throws DataAccessException {
        String gameName = "testGame";
        int gameID = createGameService.createGame(existAuthToken, gameName);
        GameData game = dataAccess.getGame(gameID);
        assertNotNull(game);
        assertEquals(gameName, game.gameName());
        assertNull(game.whiteUsername());
        assertNull(game.blackUsername());
        assertNotNull(game.game());
    }

    @Test
    void nullGame() {
        DataAccessException exception = assertThrows(DataAccessException.class,
                () -> createGameService.createGame(existAuthToken, null));
        assertEquals("Error: bad request", exception.getMessage());
    }

    @Test
    void badAuthToken() {
        String badAuthToken = "badToken";
        String gameName = "testGame";
        DataAccessException exception = assertThrows(DataAccessException.class,
                () -> createGameService.createGame(badAuthToken, gameName));
        assertEquals("Error: unauthorized", exception.getMessage());
    }

    @Test
    void gameId() throws DataAccessException {
        String gameName1 = "Game 1";
        String gameName2 = "Game 2";
        int gameID1 = createGameService.createGame(existAuthToken, gameName1);
        int gameID2 = createGameService.createGame(existAuthToken, gameName2);
        assertNotEquals(gameID1, gameID2);
        assertNotNull(dataAccess.getGame(gameID1));
        assertNotNull(dataAccess.getGame(gameID2));
    }
}
