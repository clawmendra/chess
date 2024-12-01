package handler;

import chess.ChessGame;
import chess.InvalidMoveException;
import com.google.gson.Gson;
import dataaccess.DataAccess;
import model.AuthData;
import model.GameData;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.*;
import websocket.commands.MakeMoveCommand;
import websocket.commands.UserGameCommand;
import websocket.messages.ErrorMessage;
import websocket.messages.LoadGameMessage;
import websocket.messages.NotificationMessage;
import websocket.messages.ServerMessage;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

@WebSocket
public class WebSocketHandler {
    private final Map<Session, Connection> connections = new ConcurrentHashMap<>();
    private final DataAccess dataAccess;
    private static final Gson gson = new Gson();

    private static class Connection {
        public AuthData authData;
        public Integer gameID;

        public Connection(AuthData authData, Integer gameID) {
            this.authData = authData;
            this.gameID = gameID;
        }
    }

    public WebSocketHandler(DataAccess dataAccess) {
        this.dataAccess = dataAccess;
    }

    @OnWebSocketConnect
    public void onConnect(Session session) {
        System.out.println("Client connected to websocket");
        connections.put(session, null);
    }

    @OnWebSocketClose
    public void onClose(Session session, int statusCode, String reason) {
        Connection conn = connections.get(session);
        if (conn != null && conn.gameID != null) {
            handleLeaveGame(session, conn);
        }
        connections.remove(session);
        System.out.println("Client disconnected from websocket");
    }

    @OnWebSocketMessage
    public void onMessage(Session session, String message) {
        try {
            UserGameCommand command = gson.fromJson(message, UserGameCommand.class);

            switch (command.getCommandType()) {
                case CONNECT -> handleConnect(session, command);
                case MAKE_MOVE -> handleMove(session, command);
                case LEAVE -> handleLeave(session, command);
                case RESIGN -> handleResign(session, command);
            }
        } catch (Exception e) {
            sendError(session, "Error processing command: " + e.getMessage());
        }
    }

    private void handleConnect(Session session, UserGameCommand command) {
        try {
            // Verify auth token and get game
            AuthData authData = dataAccess.getAuth(command.getAuthToken());
            if (authData == null) {
                sendError(session, "Invalid authentication token");
                return;
            }
            GameData game = dataAccess.getGame(command.getGameID());

            // Store connection info
            connections.put(session, new Connection(authData, command.getGameID()));

            // Send game state to connecting client
            LoadGameMessage gameMessage = new LoadGameMessage(game.game());
            sendToSession(session, gameMessage);

            // Notify other clients
            NotificationMessage notification = new NotificationMessage(
                    String.format("%s connected to the game", authData.username())
            );
            broadcastMessage(command.getGameID(), session, notification);

        } catch (Exception e) {
            sendError(session, e.getMessage());
        }
    }

    private void handleMove(Session session, UserGameCommand command) {
        try {
            Connection conn = connections.get(session);
            if (conn == null) {
                sendError(session, "Error: Not connected to a game");
                return;
            }

            // Verify auth and get game
            AuthData authData = conn.authData;
            GameData game = dataAccess.getGame(command.getGameID());
            ChessGame chessGame = game.game();

            // Validate the move
            MakeMoveCommand moveCommand = (MakeMoveCommand) command;
            chess.ChessMove move = moveCommand.getMove();

            // Make the move
            chessGame.makeMove(move);

            // Update game in database
            dataAccess.updateGame(game);

            // Send updated game state to all clients
            LoadGameMessage gameMessage = new LoadGameMessage(game.game());
            broadcastMessage(command.getGameID(), gameMessage);

            // Notify others about the move
            String notification = String.format("%s moved from %s to %s",
                    authData.username(),
                    move.getStartPosition().toString(),
                    move.getEndPosition().toString());
            broadcastMessage(command.getGameID(), session, new NotificationMessage(notification));

        } catch (InvalidMoveException e) {
            sendError(session, "Invalid move: " + e.getMessage());
        } catch (Exception e) {
            sendError(session, e.getMessage());
        }
    }

    private void handleLeave(Session session, UserGameCommand command) {
        try {
            Connection conn = connections.get(session);
            if (conn == null) {
                sendError(session, "Error: Not connected to a game");
                return;
            }

            // Verify auth
            AuthData authData = conn.authData;

            handleLeaveGame(session, conn);

            // Remove the connection
            connections.remove(session);

        } catch (Exception e) {
            sendError(session, e.getMessage());
        }
    }

    private void handleLeaveGame(Session session, Connection conn) {
        try {
            // Get the game
            GameData game = dataAccess.getGame(conn.gameID);
            ChessGame chessGame = game.game();

            // Remove player from game if they were playing
            if (chessGame.getTeamTurn() == ChessGame.TeamColor.WHITE && conn.authData.username().equals(game.whiteUsername())) {
                return;
            } else if (chessGame.getTeamTurn() == ChessGame.TeamColor.BLACK && conn.authData.username().equals(game.blackUsername())) {
                return;
            }

            dataAccess.updateGame(game);

            broadcastMessage(conn.gameID, session, new NotificationMessage(conn.authData.username() + " left the game"));
        } catch (Exception e) {
            System.err.println("Error handling leave game: " + e.getMessage());
        }
    }

    private void handleResign(Session session, UserGameCommand command) {
        try {
            Connection conn = connections.get(session);
            if (conn == null) {
                sendError(session, "Error: Not connected to a game");
                return;
            }

            // Verify auth
            AuthData authData = conn.authData;

            // Get game
            GameData game = dataAccess.getGame(command.getGameID());
            ChessGame chessGame = game.game();

            // Notify all clients (including the resigning player)
            NotificationMessage notification = new NotificationMessage(authData.username() + " has resigned");
            broadcastMessage(command.getGameID(), notification);

        } catch (Exception e) {
            sendError(session, e.getMessage());
        }
    }

    private void sendError(Session session, String message) {
        sendToSession(session, new ErrorMessage(message));
    }

    private void broadcastMessage(Integer gameID, Session excludeSession, ServerMessage message) {
        for (Map.Entry<Session, Connection> entry : connections.entrySet()) {
            if (entry.getValue() != null &&
                    entry.getValue().gameID.equals(gameID) &&
                    entry.getKey() != excludeSession) {
                sendToSession(entry.getKey(), message);
            }
        }
    }

    private void broadcastMessage(Integer gameID, ServerMessage message) {
        broadcastMessage(gameID, null, message);
    }

    private void sendToSession(Session session, ServerMessage message) {
        try {
            session.getRemote().sendString(gson.toJson(message));
        } catch (Exception e) {
            System.err.println("Error sending message: " + e.getMessage());
        }
    }
}