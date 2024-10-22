package handler;
import com.google.gson.Gson;
import dataaccess.DataAccessException;
import service.LoginService;
import spark.Request;
import spark.Response;
import spark.Route;
import model.AuthData;

public class LoginHandler implements Route {
    private final LoginService loginService;
    public LoginHandler(LoginService loginService) {
        this.loginService = loginService;
    }

    @Override
    public Object handle(Request request, Response response) {
        Gson gson = new Gson();
        LoginRequest loginRequest = gson.fromJson(request.body(), LoginRequest.class);
        try {
            AuthData authData = loginService.login(loginRequest.username, loginRequest.password);
            response.status(200);
            return gson.toJson(new LoginResult(authData.authToken(), authData.username()));
        } catch (DataAccessException ex) {
            response.status(401);
            return gson.toJson(new ErrorResult("Error: unauthorized"));
        } catch (Exception ex) {
            response.status(500);
            return gson.toJson(new ErrorResult("Error: " + ex.getMessage()));
        }
    }

    private static class LoginRequest {
        String username;
        String password;
    }
    private static class LoginResult {
        String authToken;
        String username;

        LoginResult(String authToken, String username) {
            this.authToken = authToken;
            this.username = username;
        }
    }

    private static class ErrorResult {
        String message;

        ErrorResult(String message) {
            this.message = message;
        }
    }

}
