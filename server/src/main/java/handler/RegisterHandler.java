package handler;

import com.google.gson.Gson;
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
        catch (Exception err) {
           response.status(400) ;
           return gson.toJson(new ErrorResult(err.getMessage()));
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


