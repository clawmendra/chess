import chess.*;
import server.Server;
import com.google.gson.Gson;
import spark.Spark;

public class Main {
    public static void main(String[] args) {
        int port = 8080;
        Server server = new Server();
        server.run(port);
        var piece = new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.PAWN);
        System.out.println("♕ 240 Chess Server: " + piece);
    }
}