package dataaccess;
import model.UserData;
import model.AuthData;
import model.GameData;

public interface DataAccess {
    void clear() throws DataAccessException;
    void createUser(UserData user) throws DataAccessException;
    UserData getUser(String username) throws DataAccessException;
    void createAuth(AuthData auth) throws DataAccessException;
    AuthData getAuth(String authToken) throws DataAccessException;
    void deleteAuth(String authToken) throws DataAccessException;
    GameData[] listGames() throws DataAccessException;
    void createGame(GameData game) throws DataAccessException;
    GameData getGame(int gameId) throws DataAccessException;
}
