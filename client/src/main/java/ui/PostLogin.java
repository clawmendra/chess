package ui;

import model.AuthData;
import model.GameData;
import client.ServerFacade;
import websocket.WebSocketClient;

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
        gamesList = server.listGames(authToken);
        if (gamesList.length == 0) {
            System.out.println("No games available.");
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
        String whitePlayer = game.whiteUsername() != null ? game.whiteUsername() : "<EMPTY>";
        String blackPlayer = game.blackUsername() != null ? game.blackUsername() : "<EMPTY>";
        info.append(" | White: ").append(whitePlayer);
        info.append(" | Black: ").append(blackPlayer);
        return info.toString();
    }

    private static GameData selectGame(Scanner scanner) {
        System.out.print("Game number: ");
        int gameNumber;
        try {
            gameNumber = Integer.parseInt(scanner.nextLine());
            if (gameNumber < 1 || gameNumber > gamesList.length) {
                System.out.println("Invalid game number.");
                return null;
            }
            return gamesList[gameNumber - 1];
        } catch (NumberFormatException e) {
            System.out.println("Invalid number.");
            return null;
        }
    }

    private static boolean isGameFull(GameData game) {
        return game.whiteUsername() != null && game.blackUsername() != null;
    }

    private static String getPlayerColor(Scanner scanner, GameData game) {
        System.out.print("Color (WHITE/BLACK): ");
        String color = scanner.nextLine().toUpperCase();

        if (!color.equals("WHITE") && !color.equals("BLACK")) {
            System.out.println("Invalid color. Must be WHITE or BLACK");
            return null;
        }

        if (color.equals("WHITE") && game.whiteUsername() != null) {
            System.out.println("White player slot is already taken.");
            return null;
        }
        if (color.equals("BLACK") && game.blackUsername() != null) {
            System.out.println("Black player slot is already taken.");
            return null;
        }
        return color;
    }

    private static void joinDisplay(ServerFacade server, AuthData authData, GameData game, String color) throws Exception {
        server.joinGame(game.gameID(), color, authData.authToken());
        System.out.println("Successfully joined game");

        try {
            boolean whiteView = color.equals("WHITE");

            // Initialize WebSocket
            GamePlay gamePlay = new GamePlay(game, null, whiteView, authData);  // Pass the AuthData
            WebSocketClient webSocketClient = server.initWebSocket(gamePlay);
            gamePlay.setWebSocketClient(webSocketClient);

            // Run the game
            gamePlay.run();
        } catch (Exception e) {
            System.out.println("Error connecting to game: " + e.getMessage());
        }
    }


    public static void playGame(ServerFacade server, AuthData authData, Scanner scanner) throws Exception {
        // Fetch latest games list
        gamesList = server.listGames(authData.authToken());

        if (gamesList.length == 0) {
            System.out.println("Sorry, there aren't any games to play");
            return;
        }

        // Display available games
        System.out.println("Available games:");
        for (int i = 0; i < gamesList.length; i++) {
            GameData game = gamesList[i];
            System.out.printf("%d. %s%n", i + 1, formatGame(game));
        }

        GameData selectedGame = selectGame(scanner);
        if (selectedGame == null) {
            return;
        }

        if (isGameFull(selectedGame)) {
            System.out.println("This game is full.");
            System.out.println("Type 'observe' to watch this game or 'create' to start a new one.");
            return;
        }

        String color = getPlayerColor(scanner, selectedGame);
        if (color == null) {
            return;
        }

        joinDisplay(server, authData, selectedGame, color);
    }

    public static void observeGame(ServerFacade server, AuthData authData, Scanner scanner) {
        try {
            // Fetch latest games list
            gamesList = server.listGames(authData.authToken());

            if (gamesList.length == 0) {
                System.out.println("Sorry, no available games to observe");
                return;
            }

            // Display available games
            System.out.println("Available games:");
            for (int i = 0; i < gamesList.length; i++) {
                GameData game = gamesList[i];
                System.out.printf("%d. %s%n", i + 1, formatGame(game));
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
            GamePlay gamePlay = new GamePlay(gamePicked, null, true, authData);
            WebSocketClient webSocketClient = server.initWebSocket(gamePlay);
            gamePlay.setWebSocketClient(webSocketClient);
            gamePlay.run();
        } catch (Exception e) {
            System.out.println("Error connecting to game: " + e.getMessage());
        }
    }
}