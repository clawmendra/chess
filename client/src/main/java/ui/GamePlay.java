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
    private static ChessGame game;
    private static Collection<ChessMove> highlightedMoves = new ArrayList<>();
    private static ChessPosition highlightedPosition = null;

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
        sendCommand(CommandType.CONNECT);
        while (isPlaying) {
            displayGame();
            handleCommand();
        }
    }

    public void handleServerMessage(ServerMessage message) {
        switch (message.getServerMessageType()) {
            case LOAD_GAME:
                this.game = ((LoadGameMessage)message).getGame();
                displayGame();
                break;
            case ERROR:
                String errorMsg = ((ErrorMessage)message).getErrorMessage();
                out.println("Error: " + errorMsg);
                // If this was a move error, we might want to redisplay the board
                if (errorMsg.contains("invalid move")) {
                    displayGame();
                }
                break;
            case NOTIFICATION:
                NotificationMessage notificationMessage = (NotificationMessage)message;
                out.println("\nNotification: " + notificationMessage.getMessage());
                // Handle game state changes for notifications
                handleGameStateChange(notificationMessage);
                break;
        }
    }

    private void displayGame() {
        out.print(ERASE_SCREEN);
        displayHelp();
        makeChessBoard();
    }


    private void displayHelp() {
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
        String notification = message.getMessage();
        if (notification.contains("checkmate")) {
            out.println("\n*** CHECKMATE ***");
            // Maybe disable further moves
        } else if (notification.contains("in check")) {
            out.println("\n*** CHECK ***");
        } else if (notification.contains("resigned")) {
            out.println("\n*** GAME OVER - RESIGNATION ***");
            // Maybe disable further moves
        }
        displayGame();
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

    private void handleCommand() {
        if (isGameOver()) {
            out.println("Game over! Type 'leave' to exit or 'help' for other commands.");
        }
        out.print("Enter command: ");
        String input = scanner.nextLine().trim().toLowerCase();
        String[] parts = input.split("\\s+");

        try {
            switch (parts[0]) {
                case "help" -> displayHelp();
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
                case "leave" -> handleLeave();
                default -> out.println("Unknown command. Type 'help' for available commands.");
            }
        } catch (Exception e) {
            out.println("Error: " + e.getMessage());
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
            sendCommand(CommandType.RESIGN);
        }
    }

    private void handleLeave() {
        sendCommand(CommandType.LEAVE);
        isPlaying = false;
    }

    private void makeChessBoard() {
        drawHeader();
        for (int row = 0; row < BOARD_SIZE_IN_SQUARES; row++) {
            int drawRow = isWhitePlayer ? BOARD_SIZE_IN_SQUARES - row : row + 1;
            out.print(SET_BG_COLOR_BLACK);
            out.print(SET_TEXT_COLOR_WHITE);
            out.print(" " + drawRow + " ");

            paintRow(row);

            out.print(SET_BG_COLOR_BLACK);
            out.print(SET_TEXT_COLOR_WHITE);
            out.print(" " + drawRow + " ");
            out.println();
        }
        drawHeader();
    }

    private void paintRow(int row) {
        for (int col = 0; col < BOARD_SIZE_IN_SQUARES; col++) {
            boolean isLightSquare = (row + col) % 2 == 0;
            drawSquare(row, col, isLightSquare);
        }
    }

    private void drawSquare(int row, int col, boolean isLight) {
        ChessPosition pos = new ChessPosition(row, col);
        ChessPiece piece = game.getBoard().getPiece(pos);

        if (isPositionHighlighted(pos)) {
            out.print(SET_BG_COLOR_GREEN);
        } else if (isLight) {
            out.print(SET_TEXT_COLOR_WHITE);
        } else {
            out.print(SET_BG_COLOR_BLACK);
        }
        // Draw piece
        if (piece == null) {
            out.print(EMPTY);
        } else {
            boolean isWhitePiece = piece.getTeamColor() == ChessGame.TeamColor.WHITE;
            out.print(isWhitePiece ? SET_TEXT_COLOR_RED : SET_TEXT_COLOR_BLUE);
            out.print(getPieceSymbol(piece));
        }
    }

    private boolean isPositionHighlighted(ChessPosition pos) {
        if (highlightedPosition != null && highlightedPosition.equals(pos)) return true; {
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
            char letter = (char)('a' + (isWhitePlayer ? col : BOARD_SIZE_IN_SQUARES - 1 - col));
            out.print(SET_BG_COLOR_BLACK);
            out.print(SET_TEXT_COLOR_WHITE);
            out.print(" " + letter + " ");
        }
        out.println();
    }

    private ChessPosition parsePosition(String pos) {
        if (pos.length() != 2) {
            throw new IllegalArgumentException("Invalid position format. Use letter+number (ex: 'e2')");
        }

        int col = pos.charAt(0) - 'a';
        int row = BOARD_SIZE_IN_SQUARES - (pos.charAt(1) - '0');

        if (col < 0 || col >= BOARD_SIZE_IN_SQUARES || row < 0 || row >= BOARD_SIZE_IN_SQUARES) {
            throw new IllegalArgumentException("Position out of bounds");
        }
        return new ChessPosition(row, col);
    }
    }