import chess.*;
import server.Server;

public class Main {
    public static void main(String[] args) {
        Server server = new Server();
        int port = 8080;
//        server.run(8080);
//        var piece = new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.PAWN);
//        System.out.println("♕ 240 Chess Server: " + piece);
        server.run(port);
        System.out.println("Server started successfully on port" + port);
    }
}