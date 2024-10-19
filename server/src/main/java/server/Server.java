package server;

import dataaccess.DataAccess;
import dataaccess.MemoryDataAccess;
import handler.ClearHandler;
import handler.RegisterHandler;
import service.RegisterService;
import service.ClearService;
import spark.*;

public class Server {
    private final ClearHandler clearHandler;
    private final RegisterHandler registerHandler;

    public Server() {
        DataAccess dataAccess = new MemoryDataAccess();
        ClearService clearService = new ClearService(dataAccess);
        RegisterService registerService = new RegisterService(dataAccess);
        this.clearHandler = new ClearHandler(clearService);
        this.registerHandler = new RegisterHandler(registerService);
    }

    public int run(int desiredPort) {
        Spark.port(desiredPort);
        Spark.staticFiles.location("web");

        // Register your endpoints and handle exceptions here.
        Spark.delete("/db", clearHandler);
        Spark.post("/user", registerHandler);


//      This line initializes the server and can be removed once you have a functioning endpoint
        Spark.init();
        Spark.awaitInitialization();
        return Spark.port();
    }

    public void stop() {
        Spark.stop();
        Spark.awaitStop();
    }

}





