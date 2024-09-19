package chess;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Represents a single chess piece
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessPiece {
    private final ChessGame.TeamColor color;
    private final PieceType type;

    public ChessPiece(ChessGame.TeamColor color, PieceType type) {
        this.color = color;
        this.type = type;
    }

    /**
     * The various different chess piece options
     */
    public enum PieceType {
        KING,
        QUEEN,
        BISHOP,
        KNIGHT,
        ROOK,
        PAWN
    }

    /**
     * @return Which team this chess piece belongs to
     */
    public ChessGame.TeamColor getTeamColor() {
//        throw new RuntimeException("Not implemented");
        return this.color;
    }

    /**
     * @return which type of chess piece this piece is
     */
    public PieceType getPieceType() {
//        throw new RuntimeException("Not implemented");
        return this.type;
    }

    /**
     * Calculates all the positions a chess piece can move to
     * Does not take into account moves that are illegal due to leaving the king in
     * danger
     *
     * @return Collection of valid moves
     */
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
        //if this piece is a king, then call calculateKingMoves and return what it got
        //return calculateKingMoves;
        switch (this.type) {
            case KING:
                return calculateKingMoves(board, myPosition);
            case QUEEN:
                return calculateQueenMoves(board, myPosition);
            case BISHOP:
                return calculateBishopMoves(board, myPosition);
            case KNIGHT:
                return calculateKnightMoves(board, myPosition);
            case ROOK:
                return calculateRookMoves(board, myPosition);
            case PAWN:
                return calculatePawnMoves(board, myPosition);
            default:
                return new ArrayList<>();
        }
    }

    private Collection<ChessMove> calculateKingMoves(ChessBoard board, ChessPosition myPosition) {
        //calculate king moves--return set of possible moves for a king
        // 1. on board boundaries and 2. if space is empty
        if (inBounds(myPosition) & checkSpace(myPosition)) {
            // move
            return new ArrayList<>();
        }

        // or
        // 3. Teammate or different color
    }
    private Collection<ChessMove> calculateQueenMoves(ChessBoard board, ChessPosition myPosition) {
        return new ArrayList<>();
    }

    private Collection<ChessMove> calculateBishopMoves(ChessBoard board, ChessPosition myPosition) {
        return new ArrayList<>();
    }

    private Collection<ChessMove> calculateKnightMoves(ChessBoard board, ChessPosition myPosition) {
        return new ArrayList<>();
    }

    private Collection<ChessMove> calculateRookMoves(ChessBoard board, ChessPosition myPosition) {
        return new ArrayList<>();
    }

    private Collection<ChessMove> calculatePawnMoves(ChessBoard board, ChessPosition myPosition) {
        return new ArrayList<>();
    }

    public Boolean inBounds (ChessPosition myPosition) {
        if(myPosition.getRow() > 8 | myPosition.getRow() < 1 | myPosition.getColumn() > 8 | myPosition.getColumn() <  1) {
            return false;
        }
        return true;
        }


    }



