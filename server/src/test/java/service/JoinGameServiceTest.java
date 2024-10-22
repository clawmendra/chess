package service;

import chess.ChessGame;
import dataaccess.DataAccess;
import dataaccess.DataAccessException;
import dataaccess.MemoryDataAccess;
import model.AuthData;
import model.GameData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;


class JoinGameServiceTest {
    private JoinGameService joinGameService;
    private DataAccess dataAccess;
    private String existAuthToken;
    private int existGameID;

    @BeforeEach
    void setUp() throws DataAccessException {
        dataAccess = new MemoryDataAccess();
        joinGameService = new JoinGameService(dataAccess);
        existAuthToken = "testAuthToken";
        String username = "testUser";
        dataAccess.createAuth(new AuthData(existAuthToken, username));
        existGameID = 1;
        GameData game = new GameData(existGameID, null, null, "Test Game", new ChessGame());
        dataAccess.createGame(game);
    }

    @Test
    void joinGame_White() throws DataAccessException {
        joinGameService.joinGame(existAuthToken, ChessGame.TeamColor.WHITE, existGameID);
        GameData game = dataAccess.getGame(existGameID);
        assertEquals("testUser", game.whitePlayer());
        assertNull(game.blackPlayer());
    }

    @Test
    void joinGame_Black() throws DataAccessException  {
        joinGameService.joinGame(existAuthToken, ChessGame.TeamColor.BLACK, existGameID);
        GameData game = dataAccess.getGame(existGameID);
        assertNull(game.whitePlayer());
        assertEquals("testUser", game.blackPlayer());
    }

    @Test
    void joinGame_NullAuthToken() {
        assertThrows(DataAccessException.class,
                () -> joinGameService.joinGame(null, ChessGame.TeamColor.WHITE, existGameID));
    }

    @Test
    void joinGame_BadToken() {
        String badAuthToken = "badAuthToken";
        assertThrows(DataAccessException.class,
                () -> joinGameService.joinGame(badAuthToken, ChessGame.TeamColor.WHITE, existGameID));
    }

    @Test
    void joinGame_BadGameId() {
        int badGameID = -1201;
        assertThrows(DataAccessException.class,
                () -> joinGameService.joinGame(existAuthToken, ChessGame.TeamColor.WHITE, badGameID));
    }

    @Test
    void joinGame_ColorTaken() throws DataAccessException {
        joinGameService.joinGame(existAuthToken, ChessGame.TeamColor.WHITE, existGameID);
        String AuthToken2 = "AuthToken2";
        dataAccess.createAuth(new AuthData(AuthToken2, "secondPlayer"));
        DataAccessException exception = assertThrows(DataAccessException.class,
                () -> joinGameService.joinGame(AuthToken2, ChessGame.TeamColor.WHITE, existGameID));
        assertEquals("Error: already taken", exception.getMessage());
    }
}

