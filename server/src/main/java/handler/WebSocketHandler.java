package handler;

import com.google.gson.Gson;
import dataaccess.DataAccess;
import model.GameData;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.*;
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
//                    handleMove(session, command);
                    break;
                case LEAVE:
//                    handleLeave(session, command);
                    break;
                case RESIGN:
//                    handleResign(session, command);
                    break;
                default:
                    sendError(session, "Unsupported command type: " + command.getCommandType());
            }

        } catch (Exception e) {
            System.err.println("Error processing message: " + e.getMessage()); // Add debug logging
            e.printStackTrace();
            sendError(session, "Error processing command: " + e.getMessage());
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
