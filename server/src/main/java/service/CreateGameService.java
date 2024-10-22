package service;
import chess.ChessGame;
import dataaccess.DataAccess;
import dataaccess.DataAccessException;
import model.AuthData;
import model.GameData;

public class CreateGameService {
    private final DataAccess dataAccess;
    private static int newGameID = 1;
    public CreateGameService(DataAccess dataAccess) {
        this.dataAccess = dataAccess;
    }
    public int createGame(String authToken, String gameName) throws DataAccessException {
        if (authToken == null || gameName == null) {
            throw new DataAccessException("Error: bad request");
        }
        AuthData auth = dataAccess.getAuth(authToken);
        if (auth == null){
            throw new DataAccessException("Error: unauthorized");
        }
        int gameID = newGameID++;
        GameData game = new GameData(gameID, null, null, gameName, new ChessGame());
        dataAccess.createGame(game);
        return gameID;
    }

}
