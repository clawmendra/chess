package handler;

import chess.ChessGame;
import chess.ChessMove;
import chess.InvalidMoveException;
import com.google.gson.Gson;
import dataaccess.DataAccess;
import model.GameData;
import org.eclipse.jetty.server.Authentication;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.*;
import websocket.commands.MakeMoveCommand;
import websocket.commands.UserGameCommand;
import websocket.messages.*;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@WebSocket
public class WebSocketHandler {
    private final Map<Session, Connection> connections;
    private final DataAccess dataAccess;
    private static final Gson gson = new Gson();

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
            System.out.println("Received message: " + message); // Add debug logging
            UserGameCommand command = gson.fromJson(message, UserGameCommand.class);

            switch (command.getCommandType()) {
                case CONNECT:
                    handleConnect(session, command);
                    break;
                case MAKE_MOVE:
                    handleMove(session, command);
                    break;
                case LEAVE:
                    handleLeave(session, command);
                    break;
                case RESIGN:
//                    handleResign(session, command);
                    break;
                default:
                    sendError(session, "Unsupported command type: " + command.getCommandType());
            }

        } catch (Exception e) {
            System.err.println("Error processing message: " + e.getMessage()); // Add debug logging
            sendError(session, "Error processing command: " + e.getMessage());
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

    private void handleMove(Session session, UserGameCommand command) {
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

            ChessGame game = currentGameData.game();

            // Make the move
            ChessMove move = ((MakeMoveCommand)command).getMove();
            game.makeMove(move);

            // Create new GameData with updated game state
            GameData updatedGameData = new GameData(
                    currentGameData.gameID(),
                    currentGameData.whiteUsername(),
                    currentGameData.blackUsername(),
                    currentGameData.gameName(),
                    game
            );

            dataAccess.updateGame(updatedGameData);
            // Send updated game state to all clients
            LoadGameMessage gameMessage = new LoadGameMessage(game);
            broadcastToAll(command.getGameID(), gameMessage);

            // Notify about the move
            String moveNotification = String.format("%s moved from %s to %s",
                    conn.username, move.getStartPosition(), move.getEndPosition());
            broadcastNotification(command.getGameID(), session, moveNotification);

            // Check game state and send appropriate notifications
            if (game.isInCheckmate(ChessGame.TeamColor.WHITE)) {
                broadcastToAll(command.getGameID(),
                        new NotificationMessage("White is in checkmate!"));
            } else if (game.isInCheckmate(ChessGame.TeamColor.BLACK)) {
                broadcastToAll(command.getGameID(),
                        new NotificationMessage("Black is in checkmate!"));
            } else if (game.isInCheck(ChessGame.TeamColor.WHITE)) {
                broadcastToAll(command.getGameID(),
                        new NotificationMessage("White is in check!"));
            } else if (game.isInCheck(ChessGame.TeamColor.BLACK)) {
                broadcastToAll(command.getGameID(),
                        new NotificationMessage("Black is in check!"));
            }
        } catch (InvalidMoveException e) {
            sendError(session, "Invalid move: " + e.getMessage());
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
            // Get the game
            GameData game = dataAccess.getGame(command.getGameID());
            if (game == null) {
                sendError(session, "Error: Game not found");
                return;
            }

            // Store connection info
            String username = command.getAuthToken(); // For now, using authToken as username
            connections.put(session, new Connection(username, command.getGameID()));

            // Send game state to connecting client
            LoadGameMessage gameMessage = new LoadGameMessage(game.game());
            sendToSession(session, gameMessage);

            // Determine if player or observer
            String role = determineRole(game, username);
            String notification = String.format("%s connected as %s", username, role);

            // Notify other clients
            broadcastNotification(command.getGameID(), session, notification);

        } catch (Exception e) {
            sendError(session, "Error connecting to game: " + e.getMessage());
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
