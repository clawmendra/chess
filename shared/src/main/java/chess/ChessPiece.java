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
        if (this.getTeamColor() == ChessGame.TeamColor.BLACK) {
            moveBlackPawn(board, myPosition, possibleMoves, myPosition.getRow(), myPosition.getColumn());
        }
        else {
            moveWhitePawn(board, myPosition, possibleMoves, myPosition.getRow(), myPosition.getColumn());
        }
        return possibleMoves;
    }


    public void moveBlackPawn(ChessBoard board, ChessPosition myPos, Collection<ChessMove> posMoves, int row, int col) {
        // default
        defaultBlack(board, myPos, posMoves, row, col);
        // first move - 2 spaces
        if (inBounds(row, col) && row == 7) {
            ChessPiece new2move = board.getPiece(new ChessPosition(row - 2, col));
            ChessPiece new1move = board.getPiece(new ChessPosition(row - 1, col));
            if (new1move == null && new2move == null) {
                posMoves.add(new ChessMove(myPos, new ChessPosition(row - 2, col), null));
            }}
        // down right attack
        blackAttack(board, myPos, posMoves, row - 1, col + 1);
        // down left attack
        blackAttack(board, myPos, posMoves, row - 1, col - 1);}

    private void blackAttack(ChessBoard board, ChessPosition myPos, Collection<ChessMove> posMoves, int row, int col) {
        if (inBounds(row, col)) {
            ChessPiece newPiece = board.getPiece(new ChessPosition(row, col));
            if (newPiece != null) {
                if (this.getTeamColor() != newPiece.getTeamColor() && row == 1) {
                    posMoves.add(new ChessMove(myPos, new ChessPosition(row, col), PieceType.QUEEN));
                    posMoves.add(new ChessMove(myPos, new ChessPosition(row, col), PieceType.ROOK));
                    posMoves.add(new ChessMove(myPos, new ChessPosition(row, col), PieceType.KNIGHT));
                    posMoves.add(new ChessMove(myPos, new ChessPosition(row, col), PieceType.BISHOP));
                }
                else if (this.getTeamColor() != newPiece.getTeamColor()) {
                    posMoves.add(new ChessMove(myPos, new ChessPosition(row, col), null));
                }}}}

    private void defaultBlack(ChessBoard board, ChessPosition myPos, Collection<ChessMove> posMoves, int row, int col) {
        ChessPiece newPiece = board.getPiece(new ChessPosition(row - 1, col));
        if (inBounds(row - 1, col)) {
            if (newPiece == null && row == 2) {
                posMoves.add(new ChessMove(myPos, new ChessPosition(row - 1, col), PieceType.QUEEN));
                posMoves.add(new ChessMove(myPos, new ChessPosition(row - 1, col), PieceType.ROOK));
                posMoves.add(new ChessMove(myPos, new ChessPosition(row - 1, col), PieceType.KNIGHT));
                posMoves.add(new ChessMove(myPos, new ChessPosition(row - 1, col), PieceType.BISHOP));
            }
            else if (newPiece == null) {
                posMoves.add(new ChessMove(myPos, new ChessPosition(row - 1, col), null));
            }}}



    public void moveWhitePawn(ChessBoard board, ChessPosition myPos, Collection<ChessMove> posMoves, int row, int col) {
        // default
        defaultWhite(board, myPos, posMoves, row, col);
        // first move - 2 spaces
        if (inBounds(row, col) && row == 2) {
            ChessPiece new2move = board.getPiece(new ChessPosition(row + 2, col));
            ChessPiece new1move = board.getPiece(new ChessPosition(row + 1, col));
            if (new1move == null && new2move == null) {
                posMoves.add(new ChessMove(myPos, new ChessPosition(row + 2, col), null));
            }}
        // up right attack
        whiteAttack(board, myPos, posMoves, row + 1, col + 1);
        // up left attack
        whiteAttack(board, myPos, posMoves, row + 1, col - 1);
    }

    private void whiteAttack(ChessBoard board, ChessPosition myPos, Collection<ChessMove> posMoves, int row, int col) {
        if (inBounds(row, col)) {
            ChessPiece newPiece = board.getPiece(new ChessPosition(row, col));
            if (newPiece != null) {
                if (this.getTeamColor() != newPiece.getTeamColor() && row == 8) {
                    posMoves.add(new ChessMove(myPos, new ChessPosition(row, col), PieceType.QUEEN));
                    posMoves.add(new ChessMove(myPos, new ChessPosition(row, col), PieceType.ROOK));
                    posMoves.add(new ChessMove(myPos, new ChessPosition(row, col), PieceType.KNIGHT));
                    posMoves.add(new ChessMove(myPos, new ChessPosition(row, col), PieceType.BISHOP));
                }
                else if (this.getTeamColor() != newPiece.getTeamColor()) {
                    posMoves.add(new ChessMove(myPos, new ChessPosition(row, col), null));
                }}}}

    private void defaultWhite(ChessBoard board, ChessPosition myPos, Collection<ChessMove> posMoves, int row, int col) {
        ChessPiece newPiece = board.getPiece(new ChessPosition(row + 1, col));
        if (inBounds(row + 1, col)) {
            if (newPiece == null && row == 7) {
                posMoves.add(new ChessMove(myPos, new ChessPosition(row + 1, col), PieceType.QUEEN));
                posMoves.add(new ChessMove(myPos, new ChessPosition(row + 1, col), PieceType.ROOK));
                posMoves.add(new ChessMove(myPos, new ChessPosition(row + 1, col), PieceType.KNIGHT));
                posMoves.add(new ChessMove(myPos, new ChessPosition(row + 1, col), PieceType.BISHOP));
            }
            else if (newPiece == null) {
                posMoves.add(new ChessMove(myPos, new ChessPosition(row + 1, col), null));
            }}}

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
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ChessPiece that = (ChessPiece) o;
        return color == that.color && type == that.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(color, type);
    }
}



