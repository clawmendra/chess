package service;

import dataaccess.DataAccess;
import dataaccess.DataAccessException;
import dataaccess.MemoryDataAccess;
import model.AuthData;
import model.GameData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;


class ListGamesServiceTest {
    private ListGamesService listGamesService;
    private DataAccess dataAccess;
    private String existAuthToken;

    @BeforeEach
    void setUp() throws DataAccessException {
        dataAccess = new MemoryDataAccess();
        listGamesService = new ListGamesService(dataAccess);
        String username = "testUser";
        existAuthToken = "testAuthToken";
        dataAccess.createAuth(new AuthData(existAuthToken, username));
    }

    @Test
    void listEmpty() throws DataAccessException {
        GameData[] games = listGamesService.listGames(existAuthToken);
        assertNotNull(games);
        assertEquals(0, games.length);
    }

    @Test
    void invalidAuthToken() {
        String badAuthToken = "badAuthToken";
        DataAccessException exception = assertThrows(DataAccessException.class,
                () -> listGamesService.listGames(badAuthToken));
        assertEquals("Error: unauthorized", exception.getMessage());
    }

    @Test
    void nullAuthToken() {
        // Act & Assert
        DataAccessException exception = assertThrows(DataAccessException.class,
                () -> listGamesService.listGames(null));
        assertEquals("Error: unauthorized", exception.getMessage());
    }

}
