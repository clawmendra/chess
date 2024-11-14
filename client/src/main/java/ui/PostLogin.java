package ui;

import model.GameData;
import client.ServerFacade;
import java.util.Scanner;

// Help, Logout, Create Game, List Games, Play Game, Observe Game
public class PostLogin {
    private static GameData[] gamesList = null;
    public static void help2() {
        System.out.println("""
               create <NAME> - a game
               list - games
               join <ID> [WHITE|BLACK] - a game
               observe <ID> - a game
               logout - when you are done
               quit - playing chess
               help - with possible commands""");
    }

    public static void logout(ServerFacade server, String authToken) throws Exception {
        server.logout(authToken);
    }

    public static void createGame(ServerFacade server, String authToken, Scanner scanner) throws Exception {
        System.out.print("Name of the game: ");
        String gameName = scanner.nextLine();
        server.createGame(gameName, authToken);
        System.out.println("Game created successfully");
    }


    public static void listGames(ServerFacade server, String authToken) throws Exception {
        gamesList = server.listGames(authToken);
        if (gamesList.length == 0) {
            System.out.println("No games available");
            return;
        }

        System.out.println("Available games:");
        for (int i = 0; i < gamesList.length; i++) {
            GameData game = gamesList[i];
            System.out.printf("%d. %s%n", i + 1, formatGame(game));
        }
    }

    private static String formatGame(GameData game) {
        StringBuilder info = new StringBuilder(game.gameName());
        if (game.whiteUsername() != null) {
            info.append(" | White: ").append(game.whiteUsername());
        }
        if (game.blackUsername() != null) {
            info.append(" | Black: ").append(game.blackUsername());
        }
        return info.toString();
    }

    public static void playGame(ServerFacade server, String authToken, Scanner scanner) throws Exception {
        if (gamesList == null) {
            System.out.println("List games first using 'list' command");
            return;
        }

        System.out.print("Game number: ");
        int gameNumber = Integer.parseInt(scanner.nextLine());
        if (gameNumber < 1 || gameNumber > gamesList.length) {
            throw new Exception("Invalid game number");
        }

        System.out.print("Color (WHITE/BLACK): ");
        String color = scanner.nextLine().toUpperCase();
        if (!color.equals("WHITE") && !color.equals("BLACK")) {
            throw new Exception("Invalid color. Must be WHITE or BLACK");
        }

        GameData selectedGame = gamesList[gameNumber - 1];
        server.joinGame(selectedGame.gameID(), color, authToken);
        System.out.println("Successfully joined game");
        }
    }
