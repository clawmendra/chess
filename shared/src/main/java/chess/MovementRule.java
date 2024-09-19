package chess;

import java.util.Collection;

    // INTERFACE ABSTRACTION
public interface MovementRule {
    Collection<ChessMove> moves(ChessBoard board, ChessPosition pos);
}
}
