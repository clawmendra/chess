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
//        if (auth == null) {
//            throw new DataAccessException
//        }
    }


}
