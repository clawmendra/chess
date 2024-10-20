package service;
import dataaccess.DataAccess;
import dataaccess.DataAccessException;
import model.UserData;
import model.AuthData;
import java.util.UUID;

public class LoginService {
    private final DataAccess dataAccess;
    public LoginService(DataAccess dataAccess) {
        this.dataAccess = dataAccess;
    }
    public AuthData login(String username, String password) throws DataAccessException {
    if (username == null || password == null) {
        throw new DataAccessException("Error: bad request");
        }
    UserData user = dataAccess.getUser(username);
    if (user == null || !user.password().equals(password)) {
        throw new DataAccessException("Error: unauthorized");
    }

    String authToken = UUID.randomUUID().toString();
    AuthData auth =  new AuthData(authToken, username);
    dataAccess.createAuth(auth);
    return auth;
}
}
