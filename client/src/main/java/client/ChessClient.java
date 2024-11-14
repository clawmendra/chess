package client;

import model.AuthData;
import static ui.PreLogin.*;
import static ui.PostLogin.*;

import java.io.PrintStream;
import java.util.Scanner;
import java.nio.charset.StandardCharsets;

public class ChessClient {
    private final ServerFacade server;
    private State state = State.LOGGED_OUT;
    private AuthData authData;
    private final Scanner scanner = new Scanner(System.in);
    private final PrintStream out;

    public enum State {
        LOGGED_IN,
        LOGGED_OUT,
    }

    public ChessClient(String serverUrl) {
        server = new ServerFacade(serverUrl);
        out = new PrintStream(System.out, true, StandardCharsets.UTF_8);
    }

    public void run() {
        System.out.println("Welcome to 240 Chess. Type 'help' to get started.");
        while (true) {
            try {
                String line = scanner.nextLine();
                if (state == State.LOGGED_OUT) {
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

    private void doPostLogin(String line) throws Exception {
        switch(line.toLowerCase()) {
            case "help" -> help2();
            case "logout" -> {
                logout(server, authData.authToken());
                state = State.LOGGED_OUT;
                authData = null;
                System.out.println("Logout successful. Type 'help' to see available commands.");
            }
            case "create" -> createGame(server, authData.authToken(), scanner);
            case "list" -> listGames(server, authData.authToken());
            case "play" -> playGame(server, authData.authToken(), scanner);
            case "observe" -> observeGame(server, authData.authToken(), scanner);
            case "quit" -> quit2();
            default -> System.out.println("Unknown command. Type 'help' to see available commands.");
        }
    }
}
