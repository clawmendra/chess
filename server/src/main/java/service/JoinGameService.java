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
    public void joinGame(String authToken, ChessGame.TeamColor playercolor, int gameID) throws DataAccessException {
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
        GameData updateGame;

        if (playercolor == null) {
            updateGame = game;
        }
        else if (playercolor == ChessGame.TeamColor.WHITE) {
            if (game.whitePlayer() != null) {
                throw new DataAccessException("Error: already taken");
            }
            updateGame = new GameData(game.gameID(), username, game.blackPlayer(), game.gameName(), game.game());
        }
        else {
            if (game.blackPlayer() != null) {
                throw new DataAccessException("Error: already taken");
            }
            updateGame = new GameData(game.gameID(), game.whitePlayer(), username, game.gameName(), game.game());
        }
        dataAccess.updateGame(updateGame);
    }
}
