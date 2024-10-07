package chess;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;

/**
 * For a class that can manage a chess game, making moves on a board
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessGame {
    private ChessBoard board;
    private TeamColor currentTurn;

    public ChessGame() {
        this.board = new ChessBoard();
        // white goes first
        this.currentTurn = TeamColor.WHITE;
        board.resetBoard();
    }

    /**
     * @return Which team's turn it is
     */
    public TeamColor getTeamTurn() {
        // throw new RuntimeException("Not implemented");
        return currentTurn;
    }

    /**
     * Set's which teams turn it is
     *
     * @param team the team whose turn it is
     */
    public void setTeamTurn(TeamColor team) {
        // throw new RuntimeException("Not implemented");
        this.currentTurn = team;
    }

    /**
     * Enum identifying the 2 possible teams in a chess game
     */
    public enum TeamColor {
        WHITE,
        BLACK
    }

    /**
     * Gets a valid moves for a piece at the given location
     *
     * @param startPosition the piece to get valid moves for
     * @return Set of valid moves for requested piece, or null if no piece at
     * startPosition
     */
    public Collection<ChessMove> validMoves(ChessPosition startPosition) {
    // Checks all possible moves for a piece and filters out any that would leave the king in check.
        ChessPiece startPiece = board.getPiece(startPosition);
        if (startPiece == null) {
            return new ArrayList<>();
        }
        Collection<ChessMove> posMoves = startPiece.pieceMoves(board, startPosition);
        Collection<ChessMove> validMoves = new ArrayList<>();

        for (ChessMove move : posMoves) {
            // try the move on the temp board
            ChessBoard tempBoard = new ChessBoard();
            copyBoard(board, tempBoard);
            ChessPiece movingPiece = tempBoard.getPiece(move.getStartPosition());
            tempBoard.addPiece(move.getEndPosition(), movingPiece);
            tempBoard.addPiece(move.getStartPosition(), null);
            if (!isInCheckAfterMove(tempBoard, startPiece.getTeamColor())) {
                validMoves.add(move);
            }}
    return validMoves;
}

    /**
     * Makes a move in a chess game
     *
     * @param move chess move to preform
     * @throws InvalidMoveException if move is invalid
     */
    public void makeMove(ChessMove move) throws InvalidMoveException {
        ChessPiece piece = board.getPiece(move.getStartPosition());
        if (piece == null || piece.getTeamColor() != currentTurn) {
            throw new InvalidMoveException("Move is invalid");
        }
        Collection<ChessMove> moves = validMoves(move.getStartPosition());
        if (moves == null || !moves.contains(move)) {
            throw new InvalidMoveException("Move is invalid");
        }
        // Make move
        if (move.getPromotionPiece() != null) {
            board.addPiece(move.getEndPosition(), new ChessPiece(currentTurn, move.getPromotionPiece()));
        } else {
            board.addPiece(move.getEndPosition(), piece);
        }
        board.addPiece(move.getStartPosition(), null);
        // Switch turns
        currentTurn = (currentTurn == TeamColor.WHITE) ? TeamColor.BLACK : TeamColor.WHITE;
    }


    /**
     * Determines if the given team is in check
     *
     * @param teamColor which team to check for check
     * @return True if the specified team is in check
     */
    public boolean isInCheck(TeamColor teamColor) {
        return isInCheckAfterMove(this.board, teamColor);
    }

    private boolean isInCheckAfterMove(ChessBoard checkBoard, TeamColor teamColor) {
        // Find the king's position
        ChessPosition kingPos = null;
        for (int row = 1; row <= 8; row++) {
            for (int col = 1; col <= 8; col++) {
                ChessPosition position = new ChessPosition(row, col);
                ChessPiece piece = checkBoard.getPiece(position);
                if (piece != null && piece.getPieceType() == ChessPiece.PieceType.KING && piece.getTeamColor() == teamColor) {
                    kingPos= position;
                    break;
                }}
            if (kingPos != null)
                break;
        }
        // no king is found--error
        if (kingPos == null) {
            return false;
        }
        // Check if any opponent piece can capture the king
        TeamColor oppColor = (teamColor == TeamColor.WHITE) ? TeamColor.BLACK : TeamColor.WHITE;
        for (int row = 1; row <= 8; row++) {
            for (int col = 1; col <= 8; col++) {
                ChessPosition pos = new ChessPosition(row, col);
                ChessPiece piece = checkBoard.getPiece(pos);
                if (piece != null && piece.getTeamColor() == oppColor) {
                    Collection<ChessMove> moves = piece.pieceMoves(checkBoard, pos);
                    for (ChessMove move : moves) {
                        if (move.getEndPosition().equals(kingPos)) {
                            return true;
                        }}}}}
            return false;
        }
        /**
         * Determines if the given team is in checkmate
         *
         * @param teamColor which team to check for checkmate
         * @return True if the specified team is in checkmate
         */
        public boolean isInCheckmate (TeamColor teamColor){
            if(!isInCheck(teamColor)) {
                return false;
            }
            for (int row = 1; row <= 8; row ++) {
                for (int col = 1; col <= 8; col++) {
                    ChessPosition pos = new ChessPosition(row, col);
                    ChessPiece piece = board.getPiece(pos);
                    if (piece != null && piece.getTeamColor() == teamColor) {
                        Collection<ChessMove> moves = validMoves(pos);
                        for (ChessMove move : moves) {
                            ChessBoard tempBoard = new ChessBoard();
                            copyBoard(board, tempBoard);
                            ChessPiece movePiece = tempBoard.getPiece(move.getStartPosition());
                            tempBoard.addPiece(move.getEndPosition(), movePiece);
                            tempBoard.addPiece(move.getStartPosition(), null);
                            if (!isInCheckAfterMove(tempBoard, teamColor)) {
                                return false;
                            }}}}}
            return true;
        }



        /**
         * Determines if the given team is in stalemate, which here is defined as having
         * no valid moves
         *
         * @param teamColor which team to check for stalemate
         * @return True if the specified team is in stalemate, otherwise false
         */
        public boolean isInStalemate (TeamColor teamColor) {
            // can only be stale if not in check
            if (isInCheck(teamColor)) {
                return false;
            }
            boolean newPiece = false;
            // check all pieces for valid moves
            for (int row = 1; row <= 8; row++) {
                for (int col = 1; col <= 8; col++) {
                    ChessPosition pos = new ChessPosition(row, col);
                    ChessPiece piece = board.getPiece(pos);
                    if (piece != null && piece.getTeamColor() == teamColor) {
                       newPiece = true;
                        Collection<ChessMove> moves = validMoves(pos);
                        // not empty--not stalemate
                        if (!moves.isEmpty()) {
                            return false;
                        }}}}
            return newPiece;
        }
        /**
         * Sets this game's chessboard with a given board
         *
         * @param board the new board to use
         */
        public void setBoard (ChessBoard board){
            this.board = board;
        }

        /**
         * Gets the current chessboard
         *
         * @return the chessboard
         */
        public ChessBoard getBoard() {
            return board;
        }

        private void copyBoard(ChessBoard currentBoard, ChessBoard copyBoard) {
            for (int row = 1; row <= 8; row++) {
                for (int col = 1; col <= 8; col++) {
                    ChessPosition pos = new ChessPosition(row, col);
                    ChessPiece piece = currentBoard.getPiece(pos);
                    // copy piece if it exists
                    if (piece != null) {
                        copyBoard.addPiece(pos, new ChessPiece(piece.getTeamColor(), piece.getPieceType()));
                    } else {
                    copyBoard.addPiece(pos, null);
                    }}}}

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ChessGame chessGame = (ChessGame) o;
        return Objects.equals(board, chessGame.board) && currentTurn == chessGame.currentTurn;
    }

    @Override
    public int hashCode() {
        return Objects.hash(board, currentTurn);
    }
}

