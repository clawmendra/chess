package chess;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;

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
        // 1. on board boundaries and
        // 2. if space is empty then add to array list
        // 3. Teammate or different color
        ArrayList<ChessMove> possibleMoves = new ArrayList<>();
        if(inBounds(myPosition.getRow() - 1, myPosition.getColumn() - 1)) {
            addShortMove(board,myPosition, possibleMoves, myPosition.getRow() - 1, myPosition.getColumn() - 1);
        }
        if(inBounds(myPosition.getRow() - 1, myPosition.getColumn())) {
            addShortMove(board,myPosition, possibleMoves, myPosition.getRow() - 1, myPosition.getColumn());
        }
        if(inBounds(myPosition.getRow() - 1, myPosition.getColumn() + 1)) {
            addShortMove(board,myPosition, possibleMoves, myPosition.getRow() - 1, myPosition.getColumn() + 1);
        }
        if(inBounds(myPosition.getRow() + 1, myPosition.getColumn())) {
            addShortMove(board, myPosition, possibleMoves, myPosition.getRow() + 1, myPosition.getColumn());
        }
        if(inBounds(myPosition.getRow(), myPosition.getColumn() - 1)) {
            addShortMove(board,myPosition, possibleMoves, myPosition.getRow(), myPosition.getColumn() - 1);
        }
        if(inBounds(myPosition.getRow(), myPosition.getColumn() + 1)) {
            addShortMove(board,myPosition, possibleMoves, myPosition.getRow(), myPosition.getColumn() + 1);
        }
        if(inBounds(myPosition.getRow() + 1, myPosition.getColumn() - 1)) {
            addShortMove(board,myPosition, possibleMoves, myPosition.getRow() + 1, myPosition.getColumn() - 1);
        }
        if(inBounds(myPosition.getRow() + 1, myPosition.getColumn() + 1)) {
            addShortMove(board,myPosition, possibleMoves, myPosition.getRow() + 1, myPosition.getColumn() + 1);
        }
        return possibleMoves;
    }

    private Collection<ChessMove> calculateQueenMoves(ChessBoard board, ChessPosition myPosition) {
        ArrayList<ChessMove> possibleMoves = new ArrayList<>();
        possibleMoves.addAll(calculateBishopMoves(board, myPosition));
        possibleMoves.addAll(calculateRookMoves(board, myPosition));
        return possibleMoves;
    }

    private Collection<ChessMove> calculateBishopMoves(ChessBoard board, ChessPosition myPosition) {
        ArrayList<ChessMove> possibleMoves = new ArrayList<>();
        addLongMove(board, myPosition, possibleMoves, myPosition.getRow() + 1, myPosition.getColumn() + 1, 1, 1);
        addLongMove(board, myPosition, possibleMoves, myPosition.getRow() + 1, myPosition.getColumn() - 1, 1, -1);
        addLongMove(board, myPosition, possibleMoves, myPosition.getRow() - 1, myPosition.getColumn() + 1, -1, 1);
        addLongMove(board, myPosition, possibleMoves, myPosition.getRow() - 1, myPosition.getColumn() - 1, -1, -1);
        return possibleMoves;
    }

    private Collection<ChessMove> calculateKnightMoves(ChessBoard board, ChessPosition myPosition) {
        // 8 potential moves
        ArrayList<ChessMove> possibleMoves = new ArrayList<>();
        if(inBounds(myPosition.getRow() - 1, myPosition.getColumn() + 2 )) {
            addShortMove(board,myPosition, possibleMoves, myPosition.getRow() - 1, myPosition.getColumn() + 2);
        }
        if(inBounds(myPosition.getRow() - 1, myPosition.getColumn() - 2 )) {
            addShortMove(board,myPosition, possibleMoves, myPosition.getRow() - 1, myPosition.getColumn() -  2);
        }
        if(inBounds(myPosition.getRow() + 1, myPosition.getColumn() + 2)) {
            addShortMove(board,myPosition, possibleMoves, myPosition.getRow() + 1, myPosition.getColumn() + 2);
        }
        if(inBounds(myPosition.getRow() + 1, myPosition.getColumn() - 2)) {
            addShortMove(board, myPosition, possibleMoves, myPosition.getRow() + 1, myPosition.getColumn() - 2);
        }
        if(inBounds(myPosition.getRow() - 2, myPosition.getColumn() + 1)) {
            addShortMove(board,myPosition, possibleMoves, myPosition.getRow() - 2, myPosition.getColumn() + 1);
        }
        if(inBounds(myPosition.getRow() + 2, myPosition.getColumn() - 1)) {
            addShortMove(board,myPosition, possibleMoves, myPosition.getRow() + 2, myPosition.getColumn() - 1);
        }
        if(inBounds(myPosition.getRow() + 2 , myPosition.getColumn() + 1)) {
            addShortMove(board,myPosition, possibleMoves, myPosition.getRow() + 2, myPosition.getColumn() + 1);
        }
        if(inBounds(myPosition.getRow() - 2, myPosition.getColumn() - 1)) {
            addShortMove(board,myPosition, possibleMoves, myPosition.getRow() - 2, myPosition.getColumn() - 1);
        }
        return possibleMoves;
    }

    private Collection<ChessMove> calculateRookMoves(ChessBoard board, ChessPosition myPosition) {
        ArrayList<ChessMove> possibleMoves = new ArrayList<>();
        addLongMove(board, myPosition, possibleMoves, myPosition.getRow(), myPosition.getColumn() + 1, 0, 1);
        addLongMove(board, myPosition, possibleMoves, myPosition.getRow(), myPosition.getColumn() - 1, 0, -1);
        addLongMove(board, myPosition, possibleMoves, myPosition.getRow() + 1, myPosition.getColumn(), 1, 0);
        addLongMove(board, myPosition, possibleMoves, myPosition.getRow() - 1, myPosition.getColumn(), -1, 0);
        return possibleMoves;
    }

    private Collection<ChessMove> calculatePawnMoves(ChessBoard board, ChessPosition myPosition) {
        ArrayList<ChessMove> possibleMoves = new ArrayList<>();
        // if the pawn is black, then keep track of when it goes to row 1 and pick a promotion piece
        if (this.getTeamColor() == ChessGame.TeamColor.BLACK) {
            // If the black pawn starts at the beginning then...
            if (myPosition.getRow() == 7) {
                // if they move 2 forward on the first round
                if (inBounds(myPosition.getRow() - 2, myPosition.getColumn())) {
                    addShortMove(board, myPosition, possibleMoves, myPosition.getRow() - 2, myPosition.getColumn());
                }
                // else they only move once
                if (inBounds(myPosition.getRow() - 1, myPosition.getColumn())) {
                    addShortMove(board, myPosition, possibleMoves, myPosition.getRow() - 1, myPosition.getColumn());
                }
            }
            // diagonal left-capture
            if (inBounds(myPosition.getRow() - 1, myPosition.getColumn() - 1)) {
                addShortMove(board, myPosition, possibleMoves, myPosition.getRow() - 1, myPosition.getColumn() - 1);
            }
            // diagonal right-capture
            if (inBounds(myPosition.getRow() - 1, myPosition.getColumn() + 1)) {
                addShortMove(board, myPosition, possibleMoves, myPosition.getRow() - 1, myPosition.getColumn() + 1);
            }
            // if it's not at beginning then it can only move once
            // default move for black
            if (inBounds(myPosition.getRow() - 1, myPosition.getColumn())) {
                addShortMove(board, myPosition, possibleMoves, myPosition.getRow() - 1, myPosition.getColumn());
            }
            // black pawn reaches front of board -- promote
            if (myPosition.getRow() == 1) {
                possibleMoves.addAll(calculateRookMoves(board, myPosition));
                possibleMoves.addAll(calculateKnightMoves(board, myPosition));
                possibleMoves.addAll(calculateBishopMoves(board, myPosition));
                possibleMoves.addAll(calculateQueenMoves(board, myPosition));
            }

            return possibleMoves;
        }
        // if the pawn is white, then keep track of when it goes to row 8 and pick a promotion piece
        if (this.getTeamColor() == ChessGame.TeamColor.WHITE) {
            // If the white pawn starts at the beginning then...
            if (myPosition.getRow() == 2) {
                // if they move 2 forward first round
                if (inBounds(myPosition.getRow() + 2, myPosition.getColumn())) {
                    addShortMove(board, myPosition, possibleMoves, myPosition.getRow() + 2, myPosition.getColumn());
                }
                if (inBounds(myPosition.getRow() + 1, myPosition.getColumn())) {
                    addShortMove(board, myPosition, possibleMoves, myPosition.getRow() + 1, myPosition.getColumn());
                }
            }
            // if it starts anywhere else...
            // diagonal left-capture
            if (inBounds(myPosition.getRow() + 1, myPosition.getColumn() - 1)) {
                addShortMove(board, myPosition, possibleMoves, myPosition.getRow() + 1, myPosition.getColumn() - 1);
            }
            // diagonal right-capture
            if (inBounds(myPosition.getRow() + 1, myPosition.getColumn() + 1)) {
                addShortMove(board, myPosition, possibleMoves, myPosition.getRow() + 1, myPosition.getColumn() + 1);
            }
            // default move for white
            if (inBounds(myPosition.getRow() + 1, myPosition.getColumn())) {
                addShortMove(board, myPosition, possibleMoves, myPosition.getRow() + 1, myPosition.getColumn());
            }
            // white pawn reaches end of board -- promote
            if (myPosition.getRow() == 8) {
                possibleMoves.addAll(calculateRookMoves(board, myPosition));
                possibleMoves.addAll(calculateKnightMoves(board, myPosition));
                possibleMoves.addAll(calculateBishopMoves(board, myPosition));
                possibleMoves.addAll(calculateQueenMoves(board, myPosition);
            }

        }
        return possibleMoves;
    }


//    private void onlyPawnMoves (ChessBoard board, ChessPosition myPosition, Collection <ChessMove> possibleMoves, int row, int col ) {
//        ChessPiece newPiece = board.getPiece(new ChessPosition(row, col));
//        // empty space
//        if (newPiece == null) {
//            possibleMoves.add(new ChessMove(myPosition, new ChessPosition(row, col),null));
//        }
//        // opponent move
//        else if (newPiece.getTeamColor() != this.getTeamColor()) {
//            possibleMoves.add(new ChessMove(myPosition, new ChessPosition(row, col), null));
//        }
//    }

    public Boolean inBounds (int row, int col) {
        return row > 0 && row <= 8 && col > 0 && col <= 8;
    }

    public void addShortMove(ChessBoard board, ChessPosition myPosition, Collection<ChessMove> possibleMoves, int row, int col) {
        ChessPiece newPiece = board.getPiece(new ChessPosition(row, col));
        if (newPiece == null) {
            possibleMoves.add(new ChessMove(myPosition, new ChessPosition(row, col), null));
        } else if (newPiece.getTeamColor() != this.getTeamColor()) {
            possibleMoves.add(new ChessMove(myPosition, new ChessPosition(row, col), null));
        }
    }

    public void addLongMove (ChessBoard board, ChessPosition myPosition, Collection<ChessMove> possibleMoves, int row, int col, int hor, int ver) {
        while(inBounds(row, col)) {
            ChessPiece newPiece = board.getPiece(new ChessPosition(row, col));
            if (newPiece == null) {
                possibleMoves.add(new ChessMove(myPosition, new ChessPosition(row, col), null));
                col = col + ver;
                row = row + hor;
            }
            else if (newPiece.getTeamColor() != this.getTeamColor()) {
                possibleMoves.add(new ChessMove(myPosition, new ChessPosition(row, col), null));
                break;
            }
            else {
                break;
            }
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ChessPiece that = (ChessPiece) o;
        return color == that.color && type == that.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(color, type);
    }
}


