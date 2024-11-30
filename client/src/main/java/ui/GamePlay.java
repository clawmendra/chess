package ui;

import chess.*;

import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.*;
import static ui.EscapeSequences.*;

import model.GameData;
import websocket.WebSocketClient;
import websocket.commands.UserGameCommand;
import websocket.commands.UserGameCommand.CommandType;
import websocket.messages.*;


import javax.swing.text.Position;

public class GamePlay {
    private static final int BOARD_SIZE_IN_SQUARES = 8;
    private final PrintStream out;
    private final Scanner scanner;
    private final GameData gameData;
    private final WebSocketClient webSocketClient;
    private boolean isWhitePlayer;
    private boolean isPlaying = true;
    private ChessGame game;
    private Collection<ChessMove> highlightedMoves = new ArrayList<>();
    private Position highlightedPosition = null;

    public GamePlay(GameData gameData, WebSocketClient webSocketClient, boolean isWhitePlayer) {
        this.out = new PrintStream(System.out, true, StandardCharsets.UTF_8);
        this.scanner = new Scanner(System.in);
        this.gameData = gameData;
        this.webSocketClient = webSocketClient;
        this.isWhitePlayer = isWhitePlayer;
        this.game = new ChessGame();
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
                out.println("Error: " + ((ErrorMessage)message).getErrorMessage());
                break;
            case NOTIFICATION:
                out.println("Notification: " + ((NotificationMessage)message).getMessage());
                break;
        }
    }

    private void displayGame() {
        out.print(ERASE_SCREEN);
        displayHelp();
        makeChessBoard(out, isWhitePlayer);
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

    private void handleCommand() {
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
        UserGameCommand command = new UserGameCommand(type, gameData.authToken(), gameData.gameID());
        webSocketClient.sendCommand(command);
    }

    private void handleMove(String startPos, String endPos) {

    }

    public static void displayChessBoard(boolean whiteView, GameData gameData) {
        var out = new PrintStream(System.out, true, StandardCharsets.UTF_8);
        out.print(ERASE_SCREEN);
        var board = new chess.ChessBoard();
        board.resetBoard();
        makeChessBoard(out, whiteView);
    }

    private void highlightLegalMoves(String pos) {
        Position position = parsePosition(pos);
        ChessPiece piece = game.getBoard().getPiece(position);

        if (piece == null) {
            out.println("No piece at position " + pos);
            return;
        }

        highlightedMoves = piece.legalMoves(game.getBoard());
        highlightedPosition = position;
        displayGame();

        // Clear highlights after displaying
        highlightedMoves = new ArrayList<>();
        highlightedPosition = null;
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

    private void makeChessBoard(PrintStream out, boolean whiteView) {
        drawHeader(out, whiteView);
        for (int row = 0; row < BOARD_SIZE_IN_SQUARES; row++) {
            int drawRow = whiteView ? BOARD_SIZE_IN_SQUARES - row : row + 1;

            out.print(SET_BG_COLOR_BLACK);
            out.print(SET_TEXT_COLOR_WHITE);
            out.print(" " + drawRow + " ");

            paintRow(out, row, whiteView);

            out.print(SET_BG_COLOR_BLACK);
            out.print(SET_TEXT_COLOR_WHITE);
            out.print(" " + drawRow + " ");
            out.println();
        }
        drawHeader(out, whiteView);
    }


    private Position parsePosition(String pos) {
        if (pos.length() != 2) {
            throw new IllegalArgumentException("Invalid position format. Use letter+number (e.g., 'e2')");
        }

        int col = pos.charAt(0) - 'a';
        int row = BOARD_SIZE_IN_SQUARES - (pos.charAt(1) - '0');

        if (col < 0 || col >= BOARD_SIZE_IN_SQUARES || row < 0 || row >= BOARD_SIZE_IN_SQUARES) {
            throw new IllegalArgumentException("Position out of bounds");
        }

        return new Position(row, col);
    }


    private static void paintRow(PrintStream out, int row, boolean whiteView) {
        for (int col = 0; col < BOARD_SIZE_IN_SQUARES; col++) {
            boolean isLightSquare = (row + col) % 2 == 0;
            drawSquare(out, row, col, isLightSquare, whiteView);
        }
    }


    private boolean isPosHighlighted(Position pos) {
        if (pos.equals(highlightedPosition)) return true; {
            return highlightedMoves.stream().anyMatch(move -> move.getEndPosition().equals(pos));
        }
    }

    private static String getPiece(int row, int col, boolean whiteView) {
        if (whiteView) {
            if (row == 0) {
                return backPieces(col, false);
            }
            if (row == 7) {
                return backPieces(col, true);
            }
            if (row == 1) {
                return BLACK_PAWN;
            }
            if (row == 6) {
                return WHITE_PAWN;
            }
        } else {
            if (row == 0) {
                return backPieces(BOARD_SIZE_IN_SQUARES - 1 - col, true);
            }
            if (row == 1) {
                return WHITE_PAWN;
            }
            if (row == 6) {
                return BLACK_PAWN;
            }
            if (row == 7) {
                return backPieces(BOARD_SIZE_IN_SQUARES - 1 - col, false);
            }
        }
        return EMPTY;
    }

    private static String backPieces(int col, boolean isWhite) {
        return switch (col) {
            case 0 -> isWhite ? BLACK_ROOK : WHITE_ROOK;
            case 1 -> isWhite ? BLACK_KNIGHT : WHITE_KNIGHT;
            case 2 -> isWhite ? BLACK_BISHOP : WHITE_BISHOP;
            case 3 -> isWhite ? BLACK_QUEEN : WHITE_QUEEN;
            case 4 -> isWhite ? BLACK_KING : WHITE_KING;
            case 5 -> isWhite ? BLACK_BISHOP : WHITE_BISHOP;
            case 6 -> isWhite ? BLACK_KNIGHT : WHITE_KNIGHT;
            case 7 -> isWhite ? BLACK_ROOK : WHITE_ROOK;
            default -> EMPTY;
        };
    }

    private static void drawSquare(PrintStream out, int row, int col, boolean isLight, boolean whiteView) {
        String piece = getPiece(row, col, whiteView);
        // Set square color
        if (isLight) {
            out.print(SET_BG_COLOR_WHITE);
        } else {
            out.print(SET_BG_COLOR_BLACK);
        }
        // Set piece color based on team
        if (piece.equals(EMPTY)) {
            out.print(piece);
        } else {
            boolean isWhitePiece = row > 4;
            if (whiteView) {
                isWhitePiece = row < 3;
            }
            out.print(isWhitePiece ? SET_TEXT_COLOR_RED : SET_TEXT_COLOR_BLUE);
            out.print(piece);
        }
    }


    private static void drawHeader(PrintStream out, boolean whiteView) {
        out.print("  ");
        for (int col = 0; col < BOARD_SIZE_IN_SQUARES; col++) {
            char letter = (char)('a' + (whiteView ? col : BOARD_SIZE_IN_SQUARES - 1- col));
            out.print(SET_BG_COLOR_BLACK);
            out.print(SET_TEXT_COLOR_WHITE);
            out.print(" " + letter + " ");
        }
        out.println();
    }

}