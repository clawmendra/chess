package handler;

import chess.*;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dataaccess.DataAccess;
import dataaccess.DataAccessException;
import model.AuthData;
import model.GameData;
import org.eclipse.jetty.util.ConcurrentHashSet;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.*;
import websocket.commands.MakeMoveCommand;
import websocket.commands.UserGameCommand;
import websocket.messages.*;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@WebSocket
public class WebSocketHandler {
    private final Map<Session, Connection> connections;
    private final DataAccess dataAccess;
    private static final Gson gson = new Gson();
    private final Set<Integer> resignedGames = new ConcurrentHashSet<>();

    private static class Connection {
        public String username;
        public Integer gameID;

        public Connection(String username, Integer gameID) {
            this.username = username;
            this.gameID = gameID;
        }
    }

    public WebSocketHandler(DataAccess dataAccess) {
        this.dataAccess = dataAccess;
        this.connections = new ConcurrentHashMap<>();
    }

    @OnWebSocketConnect
    public void onConnect(Session session) {
        if (session != null) {
            connections.put(session, new Connection(null, null));
            System.out.println("Client connected to websocket");
        }
    }

    @OnWebSocketClose
    public void onClose(Session session, int statusCode, String reason) {
        Connection conn = connections.remove(session);
        if (conn != null) {
            System.out.println("Client disconnected from websocket: " + conn.username);
        }
    }

    @OnWebSocketError
    public void onError(Session session, Throwable error) {
        System.err.println("WebSocket Error: " + error.getMessage());
        error.printStackTrace();
    }

    @OnWebSocketMessage
    public void onMessage(Session session, String message) {
        try {
            System.out.println("Received message: " + message);
            JsonObject jsonObject = JsonParser.parseString(message).getAsJsonObject();
            UserGameCommand.CommandType commandType = UserGameCommand.CommandType.valueOf(jsonObject.get("commandType").getAsString());

            UserGameCommand command = (commandType == UserGameCommand.CommandType.MAKE_MOVE)
                    ? gson.fromJson(message, MakeMoveCommand.class)
                    : gson.fromJson(message, UserGameCommand.class);

            switch (command.getCommandType()) {
                case CONNECT -> handleConnect(session, command);
                case MAKE_MOVE -> handleMove(session, (MakeMoveCommand) command);
                case LEAVE -> handleLeave(session, command);
                case RESIGN -> handleResign(session, command);
            }
        } catch (Exception e) {
            System.err.println("Error processing message: " + e.getMessage());
            e.printStackTrace();
            sendError(session, "Error processing command: " + e.getMessage());
        }
    }

    private void handleResign(Session session, UserGameCommand command) {
        try {
            GameSession context = validateGameSession(session, command);
            if (!context.isValid()) return;

            if (resignedGames.contains(command.getGameID())) {
                sendError(session, "Error: Game is already over");
                return;
            }

            if (!isPlayerInGame(context.gameData(), context.connection().username)) {
                sendError(session, "Error: Only players can resign");
                return;
            }

            resignedGames.add(command.getGameID());
            broadcastToAll(command.getGameID(),
                    new NotificationMessage(context.connection().username + " has resigned"));

        } catch (Exception e) {
            sendError(session, "Error resigning: " + e.getMessage());
        }
    }

    private void handleLeave(Session session, UserGameCommand command) {
        try {
            GameSession context = validateGameSession(session, command);
            if (!context.isValid()) return;

            GameData updatedGameData = createUpdatedGameData(context.gameData(), context.connection().username);

            if (!updatedGameData.equals(context.gameData())) {
                dataAccess.updateGame(updatedGameData);
            }

            connections.remove(session);
            broadcastNotification(command.getGameID(), session,
                    String.format("%s left the game", context.connection().username));

        } catch (Exception e) {
            sendError(session, "Error leaving game: " + e.getMessage());
        }
    }

    private void handleMove(Session session, MakeMoveCommand moveCommand) {
        try {
            GameSession context = validateGameSession(session, moveCommand);
            if (!context.isValid()) return;

            if (!validateAuthToken(moveCommand.getAuthToken(), context.connection().username)) {
                sendError(session, "Error: Invalid auth token");
                return;
            }

            if (isGameOver(context.gameData(), moveCommand.getGameID())) {
                sendError(session, "Error: Game is already over");
                return;
            }

            ChessGame game = context.gameData().game();
            if (!validateMove(game, context.gameData(), context.connection().username, moveCommand.getMove())) {
                return;
            }

            game.makeMove(moveCommand.getMove());

            GameData updatedGameData = new GameData(
                    context.gameData().gameID(),
                    context.gameData().whiteUsername(),
                    context.gameData().blackUsername(),
                    context.gameData().gameName(),
                    game
            );
            dataAccess.updateGame(updatedGameData);

            broadcastToAll(moveCommand.getGameID(), new LoadGameMessage(game));
            broadcastMoveNotification(context, moveCommand);
            broadcastGameStatus(game, moveCommand.getGameID());

        } catch (InvalidMoveException e) {
            sendError(session, "Error: Invalid move");
        } catch (Exception e) {
            sendError(session, "Error making move: " + e.getMessage());
        }
    }

    private void handleConnect(Session session, UserGameCommand command) {
        try {
            AuthData auth = validateAuthToken(command.getAuthToken());
            if (auth == null) {
                sendError(session, "Error: Invalid auth token");
                return;
            }

            GameData gameData = getGameData(command.getGameID());
            if (gameData == null) {
                sendError(session, "Error: Game not found");
                return;
            }

            connections.put(session, new Connection(auth.username(), command.getGameID()));
            sendToSession(session, new LoadGameMessage(gameData.game()));

            String role = determineRole(gameData, auth.username());
            broadcastNotification(command.getGameID(), session,
                    String.format("%s connected as %s", auth.username(), role));

        } catch (Exception e) {
            sendError(session, "Error: " + e.getMessage());
        }
    }


    private record GameSession(Connection connection, GameData gameData, boolean isValid) {}

    private GameSession validateGameSession(Session session, UserGameCommand command) {
        Connection conn = connections.get(session);
        if (conn == null) {
            sendError(session, "Error: Not connected to a game");
            return new GameSession(null, null, false);
        }

        try {
            GameData gameData = dataAccess.getGame(command.getGameID());
            if (gameData == null) {
                sendError(session, "Error: Game not found");
                return new GameSession(conn, null, false);
            }
            return new GameSession(conn, gameData, true);
        } catch (DataAccessException e) {
            sendError(session, "Error: Cannot access game data");
            return new GameSession(conn, null, false);
        }
    }

    private boolean validateAuthToken(String token, String username) {
        try {
            AuthData auth = dataAccess.getAuth(token);
            return auth != null && auth.username().equals(username);
        } catch (DataAccessException e) {
            return false;
        }
    }

    private AuthData validateAuthToken(String token) throws DataAccessException {
        return dataAccess.getAuth(token);
    }

    private GameData getGameData(int gameId) throws DataAccessException {
        return dataAccess.getGame(gameId);
    }

    private boolean isGameOver(GameData gameData, int gameId) {
        ChessGame game = gameData.game();
        return resignedGames.contains(gameId) ||
                game.isInCheckmate(ChessGame.TeamColor.WHITE) ||
                game.isInCheckmate(ChessGame.TeamColor.BLACK);
    }

    private boolean validateMove(ChessGame game, GameData gameData, String username, ChessMove move) {
        boolean isWhiteMove = game.getTeamTurn() == ChessGame.TeamColor.WHITE;
        if ((isWhiteMove && !username.equals(gameData.whiteUsername())) ||
                (!isWhiteMove && !username.equals(gameData.blackUsername()))) {
            return false;
        }

        ChessPiece piece = game.getBoard().getPiece(move.getStartPosition());
        if (piece == null) return false;

        boolean isWhitePiece = piece.getTeamColor() == ChessGame.TeamColor.WHITE;
        return (isWhitePiece && username.equals(gameData.whiteUsername())) ||
                (!isWhitePiece && username.equals(gameData.blackUsername()));
    }

    private GameData createUpdatedGameData(GameData currentGame, String leavingPlayer) {
        if (leavingPlayer.equals(currentGame.whiteUsername())) {
            return new GameData(currentGame.gameID(), null, currentGame.blackUsername(),
                    currentGame.gameName(), currentGame.game());
        } else if (leavingPlayer.equals(currentGame.blackUsername())) {
            return new GameData(currentGame.gameID(), currentGame.whiteUsername(), null,
                    currentGame.gameName(), currentGame.game());
        }
        return currentGame;
    }

    private boolean isPlayerInGame(GameData game, String username) {
        return username.equals(game.whiteUsername()) || username.equals(game.blackUsername());
    }

    private void broadcastMoveNotification(GameSession context, MakeMoveCommand moveCommand) {
        String moveNotification = String.format("%s moved from %s to %s",
                context.connection().username,
                moveCommand.getMove().getStartPosition(),
                moveCommand.getMove().getEndPosition());
        broadcastNotification(moveCommand.getGameID(), null, moveNotification);
    }

    private void broadcastGameStatus(ChessGame game, int gameId) {
        if (game.isInCheckmate(ChessGame.TeamColor.WHITE)) {
            broadcastToAll(gameId, new NotificationMessage("White is in checkmate!"));
        } else if (game.isInCheckmate(ChessGame.TeamColor.BLACK)) {
            broadcastToAll(gameId, new NotificationMessage("Black is in checkmate!"));
        } else if (game.isInCheck(ChessGame.TeamColor.WHITE)) {
            broadcastToAll(gameId, new NotificationMessage("White is in check!"));
        } else if (game.isInCheck(ChessGame.TeamColor.BLACK)) {
            broadcastToAll(gameId, new NotificationMessage("Black is in check!"));
        }
    }

    private String determineRole(GameData game, String username) {
        if (username.equals(game.whiteUsername())) return "WHITE";
        if (username.equals(game.blackUsername())) return "BLACK";
        return "observer";
    }

    private void sendToSession(Session session, ServerMessage message) {
        try {
            session.getRemote().sendString(gson.toJson(message));
        } catch (Exception e) {
            System.err.println("Error sending message: " + e.getMessage());
        }
    }

    private void broadcastToAll(Integer gameID, ServerMessage message) {
        for (Map.Entry<Session, Connection> entry : connections.entrySet()) {
            if (entry.getValue() != null && entry.getValue().gameID.equals(gameID)) {
                sendToSession(entry.getKey(), message);
            }
        }
    }

    private void broadcastNotification(Integer gameID, Session excludeSession, String message) {
        NotificationMessage notification = new NotificationMessage(message);
        for (Map.Entry<Session, Connection> entry : connections.entrySet()) {
            if (entry.getValue() != null &&
                    entry.getValue().gameID.equals(gameID) &&
                    entry.getKey() != excludeSession) {
                sendToSession(entry.getKey(), notification);
            }
        }
    }

    private void sendError(Session session, String message) {
        ErrorMessage error = new ErrorMessage(message);
        sendToSession(session, error);
    }
}