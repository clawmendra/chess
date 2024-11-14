import client.ChessClient;

public class ClientMain {
    public static void main(String[] args) {
        var serverUrl = "http://localhost:8080";
        new ChessClient(serverUrl).run();
    }
}