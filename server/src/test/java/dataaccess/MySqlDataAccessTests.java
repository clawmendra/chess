package dataaccess;

import chess.ChessGame;
import model.AuthData;
import model.GameData;
import model.UserData;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

public class MySqlDataAccessTests {
    private static DataAccess dataAccess;

    @BeforeAll
    static void init() throws DataAccessException {
        dataAccess = new MySqlDataAccess();
    }

    @BeforeEach
    void setUp() throws DataAccessException {
        dataAccess.clear();
    }

    // Clear Tests (only needs positive case)
    @Test
    void clearPositive() throws DataAccessException {
        // Create some test data
        UserData user = new UserData("testUser", "password", "test@email.com");
        dataAccess.createUser(user);
        GameData game = new GameData(1, null, null, "testGame", new ChessGame());
        dataAccess.createGame(game);

        // Clear the database
        dataAccess.clear();

        // Verify everything is cleared
        assertNull(dataAccess.getUser("testUser"));
        assertNull(dataAccess.getGame(1));
    }

    @Test
    void createUserPositive() throws DataAccessException {
        UserData user = new UserData("testUser", "password", "test@email.com");
        dataAccess.createUser(user);

        UserData retrievedUser = dataAccess.getUser("testUser");
        assertNotNull(retrievedUser);
        assertEquals("testUser", retrievedUser.username());
        assertEquals("test@email.com", retrievedUser.email());
    }

    @Test
    void createUserNegative() throws DataAccessException {
        UserData user1 = new UserData("testUser", "password1", "test1@email.com");
        dataAccess.createUser(user1);

        UserData user2 = new UserData("testUser", "password2", "test2@email.com");
        assertThrows(DataAccessException.class, () -> dataAccess.createUser(user2));
    }

    @Test
    void createAuthPositive() throws DataAccessException {
        UserData user = new UserData("testUser", "password", "test@email.com");
        dataAccess.createUser(user);

        AuthData auth = new AuthData("testAuthToken", "testUser");
        dataAccess.createAuth(auth);

        AuthData retrievedAuth = dataAccess.getAuth("testAuthToken");
        assertNotNull(retrievedAuth);
        assertEquals("testUser", retrievedAuth.username());
    }

    @Test
    void createAuthNegative() {
        AuthData auth = new AuthData("testAuthToken", "nonExistentUser");
        assertThrows(DataAccessException.class, () -> dataAccess.createAuth(auth));
    }

    @Test
    void getAuthPositive() throws DataAccessException {
        // Create user and auth token
        UserData user = new UserData("testUser", "password", "test@email.com");
        dataAccess.createUser(user);
        AuthData auth = new AuthData("testAuthToken", "testUser");
        dataAccess.createAuth(auth);

        AuthData retrieved = dataAccess.getAuth("testAuthToken");
        assertNotNull(retrieved);
        assertEquals(auth.authToken(), retrieved.authToken());
        assertEquals(auth.username(), retrieved.username());
    }

    @Test
    void getAuthNegative() throws DataAccessException {
        AuthData auth = dataAccess.getAuth("invalidToken");
        assertNull(auth);
    }


    @Test
    void createGamePositive() throws DataAccessException {
        GameData game = new GameData(1, null, null, "testGame", new ChessGame());
        dataAccess.createGame(game);

        GameData[] games = dataAccess.listGames();
        assertEquals(1, games.length);
        assertEquals("testGame", games[0].gameName());
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

        GameData retrievedGame = dataAccess.getGame(1);
        assertNotNull(retrievedGame);
        assertEquals("testGame", retrievedGame.gameName());
    }

    @Test
    void getGameNegative() throws DataAccessException {
        GameData game = dataAccess.getGame(-1);
        assertNull(game);
    }

    @Test
    void listGamesPositive() throws DataAccessException {
        // Create multiple games
        GameData game1 = new GameData(1, null, null, "game1", new ChessGame());
        GameData game2 = new GameData(2, null, null, "game2", new ChessGame());
        dataAccess.createGame(game1);
        dataAccess.createGame(game2);

        GameData[] games = dataAccess.listGames();
        assertEquals(2, games.length);

        boolean foundGame1 = false;
        boolean foundGame2 = false;
        for (GameData game : games) {
            if (game.gameName().equals("game1")) {
                foundGame1 = true;
            }
            if (game.gameName().equals("game2")) {
                foundGame2 = true;
            }
        }
        assertTrue(foundGame1 && foundGame2);
    }

    @Test
    void updateGamePositive() throws DataAccessException {
        GameData game = new GameData(1, null, null, "testGame", new ChessGame());
        dataAccess.createGame(game);

        GameData updatedGame = new GameData(1, "whitePlayer", "blackPlayer",
                "testGame", game.game());
        dataAccess.updateGame(updatedGame);

        GameData retrievedGame = dataAccess.getGame(1);
        assertEquals("whitePlayer", retrievedGame.whiteUsername());
        assertEquals("blackPlayer", retrievedGame.blackUsername());
    }

    @Test
    void updateGameNegative() {
        GameData invalidGame = new GameData(-1, "white", "black", "test", new ChessGame());
        assertThrows(DataAccessException.class, () -> dataAccess.updateGame(invalidGame));
    }

    @AfterAll
    static void cleanup() throws DataAccessException {
        dataAccess.clear();
    }
}