package handler;

import com.google.gson.Gson;
import service.ClearService;
import spark.Request;
import spark.Response;
import spark.Route;

public class ClearHandler implements Route {
    private final ClearService clearService;

    public ClearHandler(ClearService clearService) {
        this.clearService = clearService;
    }

    @Override
    public Object handle(Request request, Response response) {
        try {
            clearService.clearDatabase();
            response.status(200);
            return new Gson().toJson(new Result("success"));
        } catch (Exception e) {
            response.status(500);
            return new Gson().toJson(new Result("Error: " + e.getMessage()));
        }
    }

    private static class Result {
        private final String message;

        public Result(String message) {
            this.message = message;
        }
    }
}