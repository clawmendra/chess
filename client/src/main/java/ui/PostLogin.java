package ui;

import chess.ChessGame;
import model.GameData;
import client.ServerFacade;
import java.util.Scanner;

// Help, Logout, Create Game, List Games, Play Game, Observe Game
public class PostLogin {
    private static GameData[] gamesList = null;

    public static void help2() {
        System.out.println("""
                create - a game
                list - games
                play - a game
                observe - a game
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
        if (gameName.isEmpty()) {
            System.out.println("Game name cannot be empty. Please try again.");
            return;
        }
        server.createGame(gameName, authToken);
        System.out.println("Game created successfully");
    }


    public static void listGames(ServerFacade server, String authToken) throws Exception {
       // Add users with name of game
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
        info.append(game.gameName());
//        if (game.whiteUsername() != null) {
//            info.append(" | White: ").append(game.whiteUsername());
//        }
//        if (game.blackUsername() != null) {
//            info.append(" | Black: ").append(game.blackUsername());
//        }
        String whitePlayer = game.whiteUsername() != null ? game.whiteUsername() : "<EMPTY>";
        String blackPlayer = game.blackUsername() != null ? game.blackUsername() : "<EMPTY>";
        info.append(" | White: ").append(whitePlayer);
        info.append(" | Black: ").append(blackPlayer);
        return info.toString();
    }

    public static void playGame(ServerFacade server, String authToken, Scanner scanner) throws Exception {
        if (gamesList == null) {
            System.out.println("Sorry, this game is full");
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
        var game = new ChessGame();
        game.getBoard().resetBoard();

        boolean whiteView = color.equals("WHITE");
        GamePlay.displayChessBoard(whiteView, selectedGame);
    }

    public static void observeGame(ServerFacade server, String authToken, Scanner scanner) throws Exception {
        if (gamesList == null || gamesList.length == 0) {
            System.out.println("Sorry, no available games to observe");
            return;
        }
        System.out.print("Enter game number you want to observe: ");
        int gameNum;
        try {
            gameNum = Integer.parseInt(scanner.nextLine());
        } catch (NumberFormatException e) {
            System.out.println("Invalid game number. Please enter a valid number.");
            return;
        }

        if (gameNum < 1 || gameNum > gamesList.length) {
            System.out.println("Invalid game number.");
            return;
        }
        GameData gamePicked = gamesList[gameNum - 1];
        GamePlay.displayChessBoard(true, gamePicked);
    }

    public static void quit2() {
        System.exit(0);
    }
}
