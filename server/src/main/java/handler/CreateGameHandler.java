package handler;
import com.google.gson.Gson;
import dataaccess.DataAccessException;
import service.CreateGameService;
import spark.Request;
import spark.Response;
import spark.Route;


public class CreateGameHandler implements Route {
    private final CreateGameService createGameService;
    public CreateGameHandler(CreateGameService createGameService) {
        this.createGameService = createGameService;
    }
    @Override
    public Object handle(Request request, Response response) {
        String authToken = request.headers("authorization");

        try {
            CreateGameRequest createRequest = new Gson().fromJson(request.body(), CreateGameRequest.class);
            if (createRequest.gameName == null) {
                response.status(400);
                return new Gson().toJson(new ErrorResult("Error: bad request"));
            }
            int gameID = createGameService.createGame(authToken, createRequest.gameName);
            response.status(200);
            return new Gson().toJson(new CreateGameResult(gameID));
        } catch (DataAccessException ex) {
          if (ex.getMessage().equals("Error: unauthorized")) {
              response.status(401);
              return new Gson().toJson(new ErrorResult("Error: unauthorized"));
          }
          response.status(400);
          return new Gson().toJson(new ErrorResult("Error: bad request"));
        }
    }
    private static class CreateGameRequest {
        String gameName;
    }

    private static class CreateGameResult {
        int gameID;
        CreateGameResult(int gameID) {
            this.gameID = gameID;
        }
    }

    private static class ErrorResult {
        private String message;
        public ErrorResult(String message) {
            this.message = message;
        }
    }
}
