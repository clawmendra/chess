package server;

import dataaccess.DataAccess;
import dataaccess.MemoryDataAccess;
import handler.ClearHandler;
import service.ClearService;
import spark.*;

public class Server {
    private final ClearHandler clearHandler;

    public Server() {
        DataAccess dataAccess = new MemoryDataAccess();
        ClearService clearService = new ClearService(dataAccess);
        this.clearHandler = new ClearHandler(clearService);
    }

    public int run(int desiredPort) {
        Spark.port(desiredPort);
        Spark.staticFiles.location("web");

        // Register your endpoints and handle exceptions here.
        Spark.delete("/db", clearHandler);


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





