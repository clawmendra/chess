package client;

import model.AuthData;
import static ui.PreLogin.*;
import java.util.Scanner;

public class ChessClient {
    private final ServerFacade server;
    private State state = State.LOGGED_OUT;
    private AuthData authData;
    private final Scanner scanner = new Scanner(System.in);

    public enum State {
        LOGGED_IN,
        LOGGED_OUT,
    }

    public ChessClient(String serverUrl) {
        server = new ServerFacade(serverUrl);
    }

    public void run() {
        System.out.println("Welcome to 240 Chess. Type 'help' to get started.");
        while (true) {
            try {
                String line = scanner.nextLine();
                if (state == State.LOGGED_OUT) {
                    doPreLogin(line);
                } else {
                    // doPostLogin(line);
                    return;
                }
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
            }
        }
    }

    private void doPreLogin(String line) throws Exception {
        switch(line.toLowerCase()) {
            case "help" -> help();
            case "quit" -> quit();
            case "login" -> {
                authData = login(server, scanner);
                state = State.LOGGED_IN;
                System.out.println("Login successful! Type 'help' to see available commands.");
            }
            case "register" -> {
                authData = register(server, scanner);
                state = State.LOGGED_IN;
                System.out.println("Registration successful. Type 'help' to see available commands.");
            }
            default -> System.out.println("Unknown command. Type 'help' to see available commands.");
        }
    }
}
