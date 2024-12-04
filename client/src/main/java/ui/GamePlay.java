package ui;

import chess.*;

import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.*;
import static ui.EscapeSequences.*;

import model.AuthData;
import model.GameData;
import websocket.WebSocketClient;
import websocket.commands.MakeMoveCommand;
import websocket.commands.UserGameCommand;
import websocket.commands.UserGameCommand.CommandType;
import websocket.messages.*;

public class GamePlay implements WebSocketClient.ServerMessageHandler {
    private static final int BOARD_SIZE_IN_SQUARES = 8;
    private final PrintStream out;
    private final Scanner scanner;
    private final GameData gameData;
    private final AuthData authData;  // Add this field
    private WebSocketClient webSocketClient;
    private boolean isWhitePlayer;
    private boolean isPlaying = true;
    private ChessGame game;
    private Collection<ChessMove> highlightedMoves = new ArrayList<>();
    private ChessPosition highlightedPosition = null;
    private boolean hasPlayerResigned = false;
    private boolean gameLoaded = true;

    public GamePlay(GameData gameData, WebSocketClient webSocketClient, boolean isWhitePlayer, AuthData authData) {
        this.out = new PrintStream(System.out, true, StandardCharsets.UTF_8);
        this.scanner = new Scanner(System.in);
        this.gameData = gameData;
        this.webSocketClient = webSocketClient;
        this.isWhitePlayer = isWhitePlayer;
        this.game = new ChessGame();
        this.authData = authData;
    }

    public void setWebSocketClient(WebSocketClient webSocketClient) {
        this.webSocketClient = webSocketClient;
    }


    public void run() {
        try {
            // After WebSocket connection is established, send CONNECT message
            out.print(ERASE_SCREEN);
            out.println("Connecting to game..");
            UserGameCommand connectCommand = new UserGameCommand(
                    CommandType.CONNECT,
                    authData.authToken(),
                    gameData.gameID()
            );
            webSocketClient.sendCommand(connectCommand);

            // Now start the game UI loop
            Thread.sleep(1000);
            displayHelp();
            while (isPlaying) {
                handleCommand();
            }
        } catch (Exception e) {
            System.out.println("Error in game: " + e.getMessage());
        }
    }


    public void handleServerMessage(ServerMessage message) {
        switch (message.getServerMessageType()) {
            case LOAD_GAME:
                LoadGameMessage loadMessage = (LoadGameMessage) message;
                this.game = loadMessage.getGame();
                this.gameLoaded = true;
                displayGame();
                break;
            case ERROR:
                String errorMsg = ((ErrorMessage) message).getErrorMessage();
                out.println(errorMsg);
                break;
            case NOTIFICATION:
                NotificationMessage notificationMessage = (NotificationMessage) message;
                out.println("\nNotification: " + notificationMessage.getMessage());
                // Handle game state changes for notifications
                handleGameStateChange(notificationMessage);
                break;
        }
    }

    private void displayGame() {
        if (!gameLoaded) {
            out.println("Game Loading...");
            return;
        }
        out.print(ERASE_SCREEN);
        out.print(SET_BG_COLOR_BLACK);  // Reset background to black
        out.print(SET_TEXT_COLOR_WHITE); // Reset text to white
        out.println();  // Move to
        makeChessBoard();
        // Reset colors after board
        out.print(SET_BG_COLOR_BLACK);
        out.print(SET_TEXT_COLOR_WHITE);
    }


    private void displayHelp() {
        out.print(SET_BG_COLOR_BLACK);  // Reset background to black
        out.print(SET_TEXT_COLOR_WHITE); // Reset text to white
        out.println("Available commands:");
        out.println("  Help                    - Display this help message");
        out.println("  Redraw                  - Redraw the chess board");
        out.println("  Move <from> <to>        - Make a move (e.g., 'move e2 e4')");
        out.println("  Highlight <position>    - Show legal moves for piece");
        out.println("  Resign                  - Forfeit from the game");
        out.println("  Leave                   - Leave the game");
        out.println();
    }

    private void handleGameStateChange(NotificationMessage message) {
        String notification = message.getMessage().toLowerCase();
        if (notification.contains("checkmate")) {
            out.println("\n*** CHECKMATE - GAME OVER ***");
            displayGame();
        } else if (notification.contains("check")) {
            out.println("\n*** CHECK ***");
            displayGame();
        } else if (notification.contains("resign")) {
            out.println("\n*** GAME OVER - PLAYER RESIGN ***");
            hasPlayerResigned = true;
            displayGame();
        } else if (notification.contains("leave")) {
            out.println("\n PLAYER LEFT GAME");
            displayGame();
        }
    }


    private boolean isGameOver() {
        if (game.isInCheckmate(ChessGame.TeamColor.WHITE)) {
            out.println("White is in checkmate!");
            return true;
        }
        if (game.isInCheckmate(ChessGame.TeamColor.BLACK)) {
            out.println("Black is in checkmate!");
            return true;
        }
        if (game.isInStalemate(ChessGame.TeamColor.WHITE) || game.isInStalemate(ChessGame.TeamColor.BLACK)) {
            out.println("Game is in stalemate!");
            return true;
        }
        return false;
    }

    private boolean isGameActive() {
        return !isGameOver() && !hasPlayerResigned;  // Add hasPlayerResigned field
    }

    private void handleCommand() {
        out.print("Enter command: ");
        String input = scanner.nextLine().trim().toLowerCase();
        String[] parts = input.split("\\s+");

        if (parts.length == 0) {
            return;
        }

        // Handle commands that should always be available first
        switch (parts[0]) {
            case "help" -> {
                displayHelp();
                return;
            }
            case "leave" -> {
                handleLeave();
                return;
            }
        }

        // Then check if game is active for other commands
        if (!isGameActive()) {
            out.println("Game is over. You can only use 'help' or 'leave' commands.");
            return;
        }

        // Handle gameplay commands
        try {
            switch (parts[0]) {
                case "redraw" -> displayGame();
                case "move" -> {
                    if (parts.length != 3) {
                        out.println("Invalid format. Needs 'move <from> <to>'");
                        break;
                    }
                    handleMove(parts[1], parts[2]);
                }
                case "highlight" -> {
                    if (parts.length != 2) {
                        out.println("Invalid format. Needs 'highlight <position>'");
                        break;
                    }
                    highlightLegalMoves(parts[1]);
                }
                case "resign" -> handleResign();
                default -> out.println("Unknown command. Type 'help' for available commands.");
            }
        } catch (Exception e) {
            out.println(e.getMessage());
        }
    }

    private void sendCommand(CommandType type) {
        UserGameCommand command = new UserGameCommand(type, authData.authToken(), gameData.gameID());
        webSocketClient.sendCommand(command);
    }

    private void handleMove(String startPos, String endPos) {
        try {
            ChessPosition start = parsePosition(startPos);
            ChessPosition end = parsePosition(endPos);
            ChessMove move = new ChessMove(start, end, null);

            MakeMoveCommand moveCommand = new MakeMoveCommand(
                    authData.authToken(),
                    gameData.gameID(),
                    move
            );
            webSocketClient.sendCommand(moveCommand);
        } catch (IllegalArgumentException e) {
            out.println("Invalid position: " + e.getMessage());
        }
    }


    private void highlightLegalMoves(String pos) {
        try {
            ChessPosition position = parsePosition(pos);
            ChessPiece piece = game.getBoard().getPiece(position);

            if (piece == null) {
                out.println("No piece at position " + pos);
                return;
            }

            highlightedMoves = piece.pieceMoves(game.getBoard(), position);
            highlightedPosition = position;
            displayGame();
            out.println("Enter to continue.");
            scanner.nextLine();

            highlightedMoves = new ArrayList<>();
            highlightedPosition = null;
            displayGame();
        } catch (IllegalArgumentException e) {
            out.println("Invalid position: " + e.getMessage());
        }
    }

    private void handleResign() {
        out.print("Are you sure you want to forfeit? (yes/no): ");
        String confirm = scanner.nextLine().trim().toLowerCase();

        if (confirm.equals("yes")) {
            hasPlayerResigned = true;
            sendCommand(CommandType.RESIGN);
        }
    }

    private void handleLeave() {
        sendCommand(CommandType.LEAVE);
        out.println("You have left the game.");
        out.print("Type 'help' to display options.\n");
        isPlaying = false;
    }

    private void makeChessBoard() {
        if (game == null || game.getBoard() == null) {
            out.println("Error: Game or board not initialized");
            return;
        }

        // Draw the top header
        drawHeader();

        // Draw each row
        for (int row = 0; row < BOARD_SIZE_IN_SQUARES; row++) {
            // Display row numbers from 8 to 1
            int displayRank = isWhitePlayer ? (BOARD_SIZE_IN_SQUARES - row) : (row + 1);
            // Draw the row number on the left
            out.print(SET_BG_COLOR_BLACK);
            out.print(SET_TEXT_COLOR_WHITE);
            out.print(" " + displayRank + " ");

            // Draw the row's squares and pieces
            paintRow(row);

            // Draw the row number on the right
            out.print(SET_BG_COLOR_BLACK);
            out.print(SET_TEXT_COLOR_WHITE);
            out.print(" " + displayRank + " ");
            out.println();
        }

        // Draw the bottom header
        drawHeader();
    }

    private void paintRow(int row) {
        for (int col = 0; col < BOARD_SIZE_IN_SQUARES; col++) {
            int displayRow = isWhitePlayer ? row : (BOARD_SIZE_IN_SQUARES - 1 - row);
            int displayCol = isWhitePlayer ? col : (BOARD_SIZE_IN_SQUARES - 1 - col);

            boolean isLightSquare = (displayRow + displayCol) % 2 == 0;

            // Create chess position
            ChessPosition position = new ChessPosition(8 - displayRow, displayCol + 1);
            // Set background color
            if (isPositionHighlighted(position)) {
                if (isLightSquare) {
                    out.print(SET_BG_COLOR_GREEN);
                } else {
                    out.print(SET_BG_COLOR_DARK_GREEN);
                }
            } else {
                out.print(isLightSquare ? SET_BG_COLOR_WHITE : SET_BG_COLOR_BLACK);
            }

            // Get and draw the piece
            ChessPiece piece = game.getBoard().getPiece(position);
            if (piece == null) {
                // For empty squares, maintain background color visibility
                out.print("     ");
            } else {
                // Set piece color (red for white pieces, blue for black pieces)
                if (piece.getTeamColor() == ChessGame.TeamColor.WHITE) {
                    out.print(SET_TEXT_COLOR_RED);
                } else {
                    out.print(SET_TEXT_COLOR_BLUE);
                }
                String pieceSymbol = getPieceSymbol(piece);
                out.print(" " + pieceSymbol + " ");
            }
            // Reset colors after each square
            out.print(RESET_BG_COLOR);
            out.print(RESET_TEXT_COLOR);
        }
    }


    private boolean isPositionHighlighted(ChessPosition pos) {
        if (highlightedPosition != null && highlightedPosition.equals(pos)) return true;
        {
            return highlightedMoves.stream().anyMatch(move -> move.getEndPosition().equals(pos));
        }
    }

    private String getPieceSymbol(ChessPiece piece) {
        return switch (piece.getPieceType()) {
            case KING -> piece.getTeamColor() == ChessGame.TeamColor.WHITE ? WHITE_KING : BLACK_KING;
            case QUEEN -> piece.getTeamColor() == ChessGame.TeamColor.WHITE ? WHITE_QUEEN : BLACK_QUEEN;
            case BISHOP -> piece.getTeamColor() == ChessGame.TeamColor.WHITE ? WHITE_BISHOP : BLACK_BISHOP;
            case KNIGHT -> piece.getTeamColor() == ChessGame.TeamColor.WHITE ? WHITE_KNIGHT : BLACK_KNIGHT;
            case ROOK -> piece.getTeamColor() == ChessGame.TeamColor.WHITE ? WHITE_ROOK : BLACK_ROOK;
            case PAWN -> piece.getTeamColor() == ChessGame.TeamColor.WHITE ? WHITE_PAWN : BLACK_PAWN;
        };
    }

    private void drawHeader() {
        out.print("   ");
        for (int col = 0; col < BOARD_SIZE_IN_SQUARES; col++) {
            int displayCol = isWhitePlayer ? col : (BOARD_SIZE_IN_SQUARES - 1 - col);
            char letter = (char) ('a' + displayCol);
            out.print(SET_BG_COLOR_BLACK);
            out.print(SET_TEXT_COLOR_WHITE);
            out.print("  " + letter + "  ");
        }
        out.println();
    }

    private ChessPosition parsePosition(String pos) {
        if (pos.length() != 2) {
            throw new IllegalArgumentException("Invalid position format. Use letter+number (ex: 'e2')");
        }

        int col = pos.charAt(0) - 'a' + 1;  // Convert file (a-h) to column number (1-8)
        int row = pos.charAt(1) - '0';      // Get the actual number from the input

        // Validate position
        if (col < 1 || col > BOARD_SIZE_IN_SQUARES || row < 1 || row > BOARD_SIZE_IN_SQUARES) {
            throw new IllegalArgumentException("Position out of bounds");
        }

        return new ChessPosition(row, col);
    }
}