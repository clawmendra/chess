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
            // Initialize with empty connection object instead of null
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
            // Create a new Gson instance that knows about MakeMoveCommand
            Gson gson = new Gson();
            JsonObject jsonObject = JsonParser.parseString(message).getAsJsonObject();

            UserGameCommand.CommandType commandType = UserGameCommand.CommandType.valueOf(jsonObject.get("commandType").getAsString());

            UserGameCommand command;
            if (commandType == UserGameCommand.CommandType.MAKE_MOVE) {
                // Parse as MakeMoveCommand
                command = gson.fromJson(message, MakeMoveCommand.class);
            } else {
                // Parse as regular UserGameCommand
                command = gson.fromJson(message, UserGameCommand.class);
            }

            switch (command.getCommandType()) {
                case CONNECT:
                    handleConnect(session, command);
                    break;
                case MAKE_MOVE:
                    handleMove(session, (MakeMoveCommand) command);
                    break;
                case LEAVE:
                    handleLeave(session, command);
                    break;
                case RESIGN:
                    handleResign(session, command);
                    break;
            }
        } catch (Exception e) {
            System.err.println("Error processing message: " + e.getMessage());
            e.printStackTrace();
            sendError(session, "Error processing command: " + e.getMessage());
        }
    }

    private void handleResign(Session session, UserGameCommand command) {
        try {
            Connection conn = connections.get(session);
            if (conn == null) {
                sendError(session, "Error: Not connected to a game");
                return;
            }

            // Check if game is already over due to resignation
            if (resignedGames.contains(command.getGameID())) {
                sendError(session, "Error: Game is already over");
                return;
            }

            // Get the game
            GameData gameData = dataAccess.getGame(command.getGameID());
            if (gameData == null) {
                sendError(session, "Error: Game not found");
                return;
            }

            // Only players can resign
            if (!conn.username.equals(gameData.whiteUsername()) &&
                    !conn.username.equals(gameData.blackUsername())) {
                sendError(session, "Error: Only players can resign");
                return;
            }

            // Mark game as resigned
            resignedGames.add(command.getGameID());

            // Notify all clients about resignation
            String notification = String.format("%s has resigned", conn.username);
            broadcastToAll(command.getGameID(), new NotificationMessage(notification));

        } catch (Exception e) {
            sendError(session, "Error resigning: " + e.getMessage());
        }
    }
    private void handleLeave(Session session, UserGameCommand command) {
        try {
            Connection conn = connections.get(session);
            if (conn == null) {
                sendError(session, "Error: Not connected to a game");
                return;
            }

            // Get current game state
            GameData currentGameData = dataAccess.getGame(command.getGameID());
            if (currentGameData == null) {
                sendError(session, "Error: Game not found");
                return;
            }
            // Create new GameData, removing player that left
            GameData updatedGameData;
            if (conn.username.equals(currentGameData.whiteUsername())) {
                updatedGameData = new GameData(
                        currentGameData.gameID(),
                        null,  // Remove white player
                        currentGameData.blackUsername(),
                        currentGameData.gameName(),
                        currentGameData.game()
                );
            } else if (conn.username.equals(currentGameData.blackUsername())) {
                updatedGameData = new GameData(
                        currentGameData.gameID(),
                        currentGameData.whiteUsername(),
                        null,  // Remove black player
                        currentGameData.gameName(),
                        currentGameData.game()
                );
            } else {
                // Observer leaving - no need to update game data
                updatedGameData = currentGameData;
            }

            // Update game in database if player left (not just observer)
            if (!updatedGameData.equals(currentGameData)) {
                dataAccess.updateGame(updatedGameData);
            }

            connections.remove(session);
            broadcastNotification(command.getGameID(), session,
                    String.format("%s left the game", conn.username));

        } catch (Exception e) {
            sendError(session, "Error leaving game: " + e.getMessage());
        }
    }

    private void handleMove(Session session, MakeMoveCommand moveCommand) {
        try {
            Connection conn = connections.get(session);
            if (conn == null) {
                sendError(session, "Error: Not connected to a game");
                return;
            }

            // Verify auth token first
            try {
                AuthData auth = dataAccess.getAuth(moveCommand.getAuthToken());
                if (auth == null || !auth.username().equals(conn.username)) {
                    sendError(session, "Error: Invalid auth token");
                    return;
                }
            } catch (DataAccessException e) {
                sendError(session, "Error: Invalid auth token");
                return;
            }

            // Get current game state
            GameData gameData = dataAccess.getGame(moveCommand.getGameID());
            if (gameData == null) {
                sendError(session, "Error: Game not found");
                return;
            }

            // Check if game has been resigned
            if (resignedGames.contains(moveCommand.getGameID())) {
                sendError(session, "Error: Game is already over");
                return;
            }
            ChessGame game = gameData.game();

            // Check if game is over (resigned or checkmate)
            if (game.isInCheckmate(ChessGame.TeamColor.WHITE) ||
                    game.isInCheckmate(ChessGame.TeamColor.BLACK)) {
                sendError(session, "Error: Game is already over");
                return;
            }

            // Verify it's the player's turn and they're a player (not observer)
            boolean isWhiteMove = game.getTeamTurn() == ChessGame.TeamColor.WHITE;
            if ((isWhiteMove && !conn.username.equals(gameData.whiteUsername())) ||
                    (!isWhiteMove && !conn.username.equals(gameData.blackUsername()))) {
                sendError(session, "Error: Not your turn");
                return;
            }

            // Make sure they're moving their own piece
            ChessPosition startPos = moveCommand.getMove().getStartPosition();
            ChessPiece piece = game.getBoard().getPiece(startPos);
            if (piece == null) {
                sendError(session, "Error: No piece at start position");
                return;
            }

            boolean isWhitePiece = piece.getTeamColor() == ChessGame.TeamColor.WHITE;
            if ((isWhitePiece && !conn.username.equals(gameData.whiteUsername())) ||
                    (!isWhitePiece && !conn.username.equals(gameData.blackUsername()))) {
                sendError(session, "Error: Can't move opponent's pieces");
                return;
            }

            // Make the move
            try {
                game.makeMove(moveCommand.getMove());
            } catch (InvalidMoveException e) {
                sendError(session, "Error: Invalid move");
                return;
            }

            // Update game in database
            GameData updatedGameData = new GameData(
                    gameData.gameID(),
                    gameData.whiteUsername(),
                    gameData.blackUsername(),
                    gameData.gameName(),
                    game
            );
            dataAccess.updateGame(updatedGameData);

            // Send updated game state to all clients
            LoadGameMessage gameMessage = new LoadGameMessage(game);
            broadcastToAll(moveCommand.getGameID(), gameMessage);

            // Notify about the move
            String moveNotification = String.format("%s moved from %s to %s",
                    conn.username, moveCommand.getMove().getStartPosition(),
                    moveCommand.getMove().getEndPosition());
            broadcastNotification(moveCommand.getGameID(), session, moveNotification);

            // Check for check/checkmate
            if (game.isInCheckmate(ChessGame.TeamColor.WHITE)) {
                broadcastToAll(moveCommand.getGameID(),
                        new NotificationMessage("White is in checkmate!"));
            } else if (game.isInCheckmate(ChessGame.TeamColor.BLACK)) {
                broadcastToAll(moveCommand.getGameID(),
                        new NotificationMessage("Black is in checkmate!"));
            } else if (game.isInCheck(ChessGame.TeamColor.WHITE)) {
                broadcastToAll(moveCommand.getGameID(),
                        new NotificationMessage("White is in check!"));
            } else if (game.isInCheck(ChessGame.TeamColor.BLACK)) {
                broadcastToAll(moveCommand.getGameID(),
                        new NotificationMessage("Black is in check!"));
            }
        } catch (Exception e) {
            sendError(session, "Error making move: " + e.getMessage());
        }
    }

    // Helper method for broadcasting to all clients in a game
    private void broadcastToAll(Integer gameID, ServerMessage message) {
        for (Map.Entry<Session, Connection> entry : connections.entrySet()) {
            if (entry.getValue() != null && entry.getValue().gameID.equals(gameID)) {
                sendToSession(entry.getKey(), message);
            }
        }
    }

    private void handleConnect(Session session, UserGameCommand command) {
        try {
            // First validate the auth token using getAuth
            AuthData auth = dataAccess.getAuth(command.getAuthToken());
            if (auth == null) {
                sendError(session, "Error: Invalid auth token");
                return;
            }

            // Get the game
            GameData gameData = dataAccess.getGame(command.getGameID());
            if (gameData == null) {
                sendError(session, "Error: Game not found");
                return;
            }

            // Store connection info
            connections.put(session, new Connection(auth.username(), command.getGameID()));

            // Send game state to connecting client
            LoadGameMessage gameMessage = new LoadGameMessage(gameData.game());
            sendToSession(session, gameMessage);

            // Determine if player or observer
            String role = determineRole(gameData, auth.username());
            String notification = String.format("%s connected as %s", auth.username(), role);

            // Notify other clients
            broadcastNotification(command.getGameID(), session, notification);

        } catch (DataAccessException e) {
            sendError(session, "Error: Invalid auth token");
        } catch (Exception e) {
            sendError(session, "Error: " + e.getMessage());
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
