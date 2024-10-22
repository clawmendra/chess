package handler;
import chess.ChessGame;
import com.google.gson.Gson;
import service.JoinGameService;
import spark.Request;
import spark.Response;
import spark.Route;
import dataaccess.DataAccessException;

public class JoinGameHandler implements Route {
    private final JoinGameService joinGameService;

    public JoinGameHandler(JoinGameService joinGameService) {
        this.joinGameService = joinGameService;
    }

    @Override
    public Object handle(Request request, Response response) {
        String authToken = request.headers("authorization");

        try {
            JoinGameRequest joinRequest = new Gson().fromJson(request.body(), JoinGameRequest.class);
            if (joinRequest.gameID == null || joinRequest.playerColor == null) {
                response.status(400);
                return new Gson().toJson(new ErrorResult("Error: bad request"));
            }
            ChessGame.TeamColor playerColor = ChessGame.TeamColor.valueOf(joinRequest.playerColor);
            joinGameService.joinGame(authToken, playerColor, joinRequest.gameID);
            response.status(200);
            return new Gson().toJson(new Result());
        } catch (DataAccessException ex) {
            if (ex.getMessage().equals("Error: unauthorized")) {
                response.status(401);
                return new Gson().toJson(new ErrorResult("Error: unauthorized"));
            } else if (ex.getMessage().equals("Error: already taken")) {
                response.status(403);
                return new Gson().toJson(new ErrorResult("Error: already taken"));
            }
            response.status(500);
            return new Gson().toJson(new ErrorResult("Error: bad request"));
        }
    }

    private static class JoinGameRequest {
        private String playerColor;
        private Integer gameID;
        public String getPlayerColor() {
            return playerColor;
        }
        public Integer getGameID() {
            return gameID;
        }
    }
    private static class Result {}
    private static class ErrorResult {
        private final String message;
        public ErrorResult(String message) {
            this.message = message;
        }
        public String getMessage() {
            return message;
        }
    }
}