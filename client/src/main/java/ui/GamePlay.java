package ui;

import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import static ui.EscapeSequences.*;


public class GamePlay {
    private static final int BOARD_SIZE_IN_SQUARES = 8;
    private static final int SQUARE_SIZE_IN_CHARS = 3;
    private static final int LINE_WIDTH_IN_CHARS = 1;

    public static void main(String[] args) {
        var out = new PrintStream(System.out, true, StandardCharsets.UTF_8);
        out.print(ERASE_SCREEN);
        // white view
        makeChessBoard(out, true);
        out.println();
        // black view
        makeChessBoard(out, false);
        out.print(SET_BG_COLOR_BLACK);
        out.print(SET_TEXT_COLOR_WHITE);
    }

    private static void makeChessBoard(PrintStream out, boolean whiteView) {
        drawHeader(out, whiteView);
        for (int row = 0; row < BOARD_SIZE_IN_SQUARES; row++) {
            int drawRow = whiteView ? BOARD_SIZE_IN_SQUARES - row : row + 1;

            out.print(SET_BG_COLOR_BLACK);
            out.print(SET_TEXT_COLOR_WHITE);
            out.print(" " + drawRow + " ");

            paintRow(out, row, whiteView);

            // Print row number
            out.print(SET_BG_COLOR_BLACK);
            out.print(SET_TEXT_COLOR_WHITE);
            out.print(" " + drawRow + " ");
            out.println();
        }

    }

    private static void paintRow(PrintStream out, int row, boolean whiteView) {
        for (int col = 0; col < BOARD_SIZE_IN_SQUARES; col++) {
            boolean isLightSquare = (row + col) % 2 == 0;
            drawSquare(out, row, col, isLightSquare, whiteView);
        }
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
