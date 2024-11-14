import server.Server;

public class ServerMain {
    public static void main(String[] args) {
        Server server = new Server();
        int port = 8080;
        server.run(port);
        System.out.println("Server started successfully on port" + port);
    }
}