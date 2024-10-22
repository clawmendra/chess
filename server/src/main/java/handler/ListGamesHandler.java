package handler;
import com.google.gson.Gson;
import dataaccess.DataAccessException;
import service.ListGamesService;
import spark.Request;
import spark.Response;
import spark.Route;
import model.GameData;


public class ListGamesHandler implements Route {
    private final ListGamesService listGamesService;
    public ListGamesHandler(ListGamesService listGamesService) {
        this.listGamesService = listGamesService;
    }

    @Override
    public Object handle(Request request, Response response) {
        String authToken = request.headers("authorization");

        try {
            GameData[] games = listGamesService.listGames(authToken);
            response.status(200);
            return new Gson().toJson(new ListGamesResult(games));
        } catch (DataAccessException ex) {
            response.status(401);
            return new Gson().toJson(new ErrorResult("Error: unauthorized"));
        } catch (Exception ex) {
            response.status(500);
            return new Gson().toJson(new ErrorResult("Error: " + ex.getMessage()));
        }
    }

    private static class ListGamesResult {
        private final GameData[] games;
        public ListGamesResult(GameData[] games) {
            this.games = games;
        }
    }
    private static class ErrorResult {
        private String message;
        public ErrorResult(String message) {
            this.message = message;
        }
    }
}
