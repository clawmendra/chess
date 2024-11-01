package server;
import dataaccess.DataAccessException;
import handler.*;
import service.*;
import spark.*;

import dataaccess.DataAccess;
//import dataaccess.MemoryDataAccess;
import dataaccess.MySqlDataAccess;

public class Server {
    private final ClearHandler clearHandler;
    private final RegisterHandler registerHandler;
    private final LoginHandler loginHandler;
    private final LogoutHandler logoutHandler;
    private final ListGamesHandler listGamesHandler;
    private final CreateGameHandler createGameHandler;
    private final JoinGameHandler joinGameHandler;

    public Server() {
        //DataAccess dataAccess = new MemoryDataAccess();
        DataAccess dataAccess;
        try {
            dataAccess = new MySqlDataAccess();
        } catch (DataAccessException e) {
            throw new RuntimeException("Unable to create MySQL Data Access: " + e.getMessage());
        }
        ClearService clearService = new ClearService(dataAccess);
        RegisterService registerService = new RegisterService(dataAccess);
        LoginService loginService = new LoginService(dataAccess);
        LogoutService logoutService = new LogoutService(dataAccess);
        ListGamesService listGamesService = new ListGamesService(dataAccess);
        CreateGameService createGameService = new CreateGameService(dataAccess);
        JoinGameService joinGameService = new JoinGameService(dataAccess);

        this.clearHandler = new ClearHandler(clearService);
        this.registerHandler = new RegisterHandler(registerService);
        this.loginHandler = new LoginHandler(loginService);
        this.logoutHandler = new LogoutHandler(logoutService);
        this.listGamesHandler = new ListGamesHandler(listGamesService);
        this.createGameHandler = new CreateGameHandler(createGameService);
        this.joinGameHandler = new JoinGameHandler(joinGameService);
    }

    public int run(int desiredPort) {
        Spark.port(desiredPort);
        Spark.staticFiles.location("web");

        // Register your endpoints and handle exceptions here.
        Spark.delete("/db", clearHandler);
        Spark.post("/user", registerHandler);
        Spark.post("/session", loginHandler);
        Spark.delete("/session", logoutHandler);
        Spark.get("/game", listGamesHandler);
        Spark.post("/game", createGameHandler);
        Spark.put("/game", joinGameHandler);

        // This line initializes the server and can be removed once you have a functioning endpoint
        Spark.init();
        Spark.awaitInitialization();
        return Spark.port();
    }

    public void stop() {
        Spark.stop();
        Spark.awaitStop();
    }

}





