package server;
import spark.*;

import dataaccess.DataAccess;
import dataaccess.MemoryDataAccess;

import handler.ClearHandler;
import handler.RegisterHandler;
import handler.LoginHandler;
import handler.LogoutHandler;
import handler.ListGamesHandler;

import service.RegisterService;
import service.ClearService;
import service.LoginService;
import service.LogoutService;
import service.ListGamesService;

public class Server {
    private final ClearHandler clearHandler;
    private final RegisterHandler registerHandler;
    private final LoginHandler loginHandler;
    private final LogoutHandler logoutHandler;
    private final ListGamesHandler listGamesHandler;

    public Server() {
        DataAccess dataAccess = new MemoryDataAccess();
        ClearService clearService = new ClearService(dataAccess);
        RegisterService registerService = new RegisterService(dataAccess);
        LoginService loginService = new LoginService(dataAccess);
        LogoutService logoutService = new LogoutService(dataAccess);
        ListGamesService listGamesService = new ListGamesService(dataAccess);

        this.clearHandler = new ClearHandler(clearService);
        this.registerHandler = new RegisterHandler(registerService);
        this.loginHandler = new LoginHandler(loginService);
        this.logoutHandler = new LogoutHandler(logoutService);
        this.listGamesHandler = new ListGamesHandler(listGamesService);
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





