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

    public static AuthData login(ServerFacade server, Scanner scanner) throws Exception {
        System.out.print("Username: ");
        String username = scanner.nextLine();
        System.out.print("Password: ");
        String password = scanner.nextLine();
        try {
            return server.login(username, password);
        } catch (Exception e) {
            System.out.println("Invalid username or password--Please try again");
            return login(server, scanner);
        }
    }

    public static AuthData register(ServerFacade server, Scanner scanner) throws Exception {
        System.out.print("Username: ");
        String username = scanner.nextLine();
        System.out.print("Password: ");
        String password = scanner.nextLine();
        System.out.print("Email: ");
        String email = scanner.nextLine();
        return server.register(username, password, email);
    }

}
