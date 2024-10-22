package server;
import spark.*;

import dataaccess.DataAccess;
import dataaccess.MemoryDataAccess;
import handler.ClearHandler;
import handler.RegisterHandler;
import handler.LoginHandler;

import service.RegisterService;
import service.ClearService;
import service.LoginService;


public class Server {
    private final ClearHandler clearHandler;
    private final RegisterHandler registerHandler;
    private final LoginHandler loginHandler;

    public Server() {
        DataAccess dataAccess = new MemoryDataAccess();
        ClearService clearService = new ClearService(dataAccess);
        RegisterService registerService = new RegisterService(dataAccess);
        LoginService loginService = new LoginService(dataAccess);

        this.clearHandler = new ClearHandler(clearService);
        this.registerHandler = new RegisterHandler(registerService);
        this.loginHandler = new LoginHandler(loginService);
    }

    public int run(int desiredPort) {
        Spark.port(desiredPort);
        Spark.staticFiles.location("web");

        // Register your endpoints and handle exceptions here.
        Spark.delete("/db", clearHandler);
        Spark.post("/user", registerHandler);
        Spark.post("/session", loginHandler);

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





