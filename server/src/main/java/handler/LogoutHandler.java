package handler;
import com.google.gson.Gson;
import dataaccess.DataAccessException;
import service.LogoutService;
import spark.Request;
import spark.Response;
import spark.Route;
import model.AuthData;

import javax.xml.crypto.Data;
public class LogoutHandler implements Route {
    private final LogoutService logoutService;
    public LogoutHandler(LogoutService logoutService) {
        this.logoutService = logoutService;
    }

    @Override
    public Object handle(Request request, Response response) {
        String authToken = request.headers("Authorization");
        try {
            logoutService.logout(authToken);
            response.status(200);
            return new Gson().toJson(new Result());
        } catch (DataAccessException ex) {
            response.status(401);
            return new Gson().toJson(new Result("Error: unauthorized"));
        } catch (Exception ex) {
            response.status(500);
            return new Gson().toJson(new Result("Error: " + ex.getMessage()));
        }
    }
    private static class Result {
        private String message;
        public Result() {}
        public Result(String message) {
            this.message = message;
        }
    }
}
