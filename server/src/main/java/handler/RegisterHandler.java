package handler;

import com.google.gson.Gson;
import dataaccess.DataAccessException;
import service.RegisterService;
import spark.Request;
import spark.Response;
import spark.Route;
import model.AuthData;

public class RegisterHandler implements Route {
    private final RegisterService registerService;

    public RegisterHandler(RegisterService registerService) {
        this.registerService = registerService;
    }

    @Override
    public Object handle(Request request, Response response) {
        Gson gson = new Gson();
        RegisterRequest registerRequest = gson.fromJson(request.body(), RegisterRequest.class);

        try {
            AuthData authData = registerService.register(
                    registerRequest.username,
                    registerRequest.password,
                    registerRequest.email);
            response.status(200);
            return gson.toJson(new RegisterResult(authData.authToken(), authData.username()));
    }
        catch (DataAccessException err) {
            if (err.getMessage().equals("Error: already taken")) {
                response.status(403);
                return gson.toJson(new ErrorResult("Error: already taken"));
            } else {
                response.status(400);
                return gson.toJson(new ErrorResult("Error: bad request"));
            }
        }
        catch (Exception e) {
            response.status(500);
            return gson.toJson(new ErrorResult("Error: description of error"));
        }
}

    private static class RegisterRequest {
        String username;
        String password;
        String email;
    }

    private static class RegisterResult {
        String authToken;
        String username;

        RegisterResult(String authToken, String username) {
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


