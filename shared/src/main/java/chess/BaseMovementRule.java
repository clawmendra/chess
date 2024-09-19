package chess;

import java.util.Collection;

public abstract class BaseMovementRule implements MovementRule {
    protected void calculateMoves(ChessBoard board, ChessPosition pos, int rowInc, int colInc,
                                  Collection<ChessMove> moves, boolean allowDistance) {

        // Generic code for calculating most piece rules
        // ...
    }

    public abstract Collection<ChessMove> moves(ChessBoard board, ChessPosition position);
}
