package ui;

import client.ServerFacade;
import model.AuthData;
import java.util.Scanner;

// Help, Quit, Login, Register
public class PreLogin {
    public static void help() {
        System.out.println("""
                register - to create an account
                login - to play chess
                quit - playing chess
                help - with possible commands""");
    }

    public static void quit() {
        System.exit(0);
    }

    public static AuthData login(ServerFacade server, Scanner scanner) {
            System.out.print("Username: ");
            String username = scanner.nextLine();
            System.out.print("Password: ");
            String password = scanner.nextLine();
            try {
                return server.login(username, password);
            } catch (Exception e) {
                System.out.println("Invalid username or password.");
                return null;
            }
    }

    public static AuthData register(ServerFacade server, Scanner scanner) {
        AuthData result = null;
        while (result == null) {
            String username = getValidInput(scanner, "Username");
            String password = getValidInput(scanner, "Password");
            String email = getValidInput(scanner, "Email");
            result = tryRegister(server, username, password, email);
        }
        return result;
    }

    private static String getValidInput(Scanner scanner, String fieldName) {
        while (true) {
            System.out.print(fieldName + ": ");
            String input = scanner.nextLine().trim();
            if (input.isEmpty()) {
                System.out.println(fieldName + " cannot be empty. Please try again.");
                continue;
            }
            return input;
        }
    }

    private static AuthData tryRegister(ServerFacade server, String username, String password, String email) {
        try {
            return server.register(username, password, email);
        } catch (Exception e) {
            if (e.getMessage().contains("403")) {
                System.out.println("Username already taken. Please try again.");
                return null;
            }
            System.out.println("Registration failed: " + e.getMessage());
            return null;
        }
    }
}
