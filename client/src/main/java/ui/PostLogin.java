package ui;

import model.GameData;
import client.ServerFacade;
import java.util.Scanner;

// Help, Logout, Create Game, List Games, Play Game, Observe Game
public class PostLogin {
    private static GameData[] gamesList = null;
    public static void help() {
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

//    public static void listGames(ServerFacade server, String authToken) throws Exception {
//        gamesList = server.listGames(authToken);
//        if (gamesList.length == 0) {
//            System.out.println("No games available");
//            return;
//        }
//
//        System.out.println("Available games:");
//        for (int i = 0; i < gamesList.length; i++) {
//            GameData game = gamesList[i];
//            System.out.printf("%d. %s%n", i + 1, formatGameInfo(game));
//        }


    }
