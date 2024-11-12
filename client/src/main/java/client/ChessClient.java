package client;

import model.AuthData;
import static ui.PreLogin.*;
import java.util.Scanner;

public class ChessClient {
    private final ServerFacade server;
    private State state = State.LOGGEDON;
    private AuthData authData;
    private final Scanner scanner = new Scanner(System.in);

    public enum State {
        LOGGEDON,
        LOGGEDOUT,
    }

    public ChessClient(String serverUrl) {
        server = new ServerFacade(serverUrl);
    }

    public void run() {
        System.out.println("Welcome to 240 Chess. Type 'Help' to get started.");
        while (true) {
            try {
                String line = scanner.nextLine();
                if (state == State.LOGGEDOUT) {
                    doPreLogin(line);
                } else {
                    doPostLogin(line);
                }
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
            }
        }
    }

    private void doPreLogin(String line) throws Exception {
        return;
    }
}
