package service;
import chess.ChessGame;
import dataaccess.DataAccess;
import dataaccess.DataAccessException;
import model.AuthData;
import model.GameData;

public class JoinGameService {
    private final DataAccess dataAccess;
    public JoinGameService(DataAccess dataAccess) {
        this.dataAccess = dataAccess;
    }
    public void joinGame(String authToken, ChessGame.TeamColor playerColor, int gameID) throws DataAccessException {
        if (authToken == null) {
            throw new DataAccessException("Error: unauthorized");
        }
        AuthData auth = dataAccess.getAuth(authToken);
        if (auth == null) {
            throw new DataAccessException("Error: bad request");
        }
        GameData game = dataAccess.getGame(gameID);
        if (game == null) {
            throw new DataAccessException("Error: bad request");
        }

        String username = auth.username();

        if (playerColor == null) {
            return;
        }
        GameData updateGame;
        if (playerColor == ChessGame.TeamColor.WHITE) {
            if (game.whiteUsername() != null) {
                throw new DataAccessException("Error: already taken");
            }
            updateGame = new GameData(game.gameID(), username, game.blackUsername(), game.gameName(), game.game());
        }
        else {
            if (game.blackUsername() != null) {
                throw new DataAccessException("Error: already taken");
            }
            updateGame = new GameData(game.gameID(), game.whiteUsername(), username, game.gameName(), game.game());
        }
        dataAccess.updateGame(updateGame);
    }
}
