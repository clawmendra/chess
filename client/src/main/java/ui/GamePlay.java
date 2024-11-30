package ui;

import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static ui.EscapeSequences.*;
import model.GameData;
import websocket.WebSocketClient;

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
        // Send initial connect command
        sendCommand(CommandType.CONNECT);

        while (isPlaying) {
            displayGame();
            handleCommand();
        }
    }

    private void sendCommand(CommandType type) {
        UserGameCommand command = new UserGameCommand(type, gameData.authToken(), gameData.gameID());
        webSocketClient.sendCommand(command);
    }


    private void displayHelp() {
        out.println("Available commands:");
        out.println("  help                    - Show this help message");
        out.println("  redraw                  - Redraw the chess board");
        out.println("  move <from> <to>        - Make a move (e.g., 'move e2 e4')");
        out.println("  highlight <position>    - Show legal moves for piece (e.g., 'highlight e2')");
        out.println("  resign                  - Resign from the game");
        out.println("  leave                   - Leave the game");
        out.println();
    }

    public static void displayChessBoard(boolean whiteView, GameData gameData) {
        var out = new PrintStream(System.out, true, StandardCharsets.UTF_8);
        out.print(ERASE_SCREEN);
        var board = new chess.ChessBoard();
        board.resetBoard();
        makeChessBoard(out, whiteView);
    }

    private static void makeChessBoard(PrintStream out, boolean whiteView) {
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

    private static void paintRow(PrintStream out, int row, boolean whiteView) {
        for (int col = 0; col < BOARD_SIZE_IN_SQUARES; col++) {
            boolean isLightSquare = (row + col) % 2 == 0;
            drawSquare(out, row, col, isLightSquare, whiteView);
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