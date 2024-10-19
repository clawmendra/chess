package service;

import dataaccess.DataAccess;
import dataaccess.DataAccessException;
import model.UserData;
import model.AuthData;
import java.util.UUID;


public class RegisterService {
    private final DataAccess dataAccess;
    public RegisterService(DataAccess dataAccess) {
        this.dataAccess = dataAccess;
    }
    public AuthData register(String username, String password, String email) throws DataAccessException {
        if (username == null || password == null || email == null) {
            throw new DataAccessException("Error: bad request");
        }
        if (dataAccess.getUser(username) != null) {
            throw new DataAccessException("Error: already taken");
        }

        UserData user = new UserData(username, password, email);
        dataAccess.createUser(user);

        String authToken = UUID.randomUUID().toString();
        AuthData auth = new AuthData(authToken, username);
        dataAccess.createAuth(auth);

        return auth;
    }
}
