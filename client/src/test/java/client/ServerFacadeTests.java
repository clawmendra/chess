package client;

import model.AuthData;
import model.GameData;
import org.junit.jupiter.api.*;
import server.Server;
import static org.junit.jupiter.api.Assertions.*;


public class ServerFacadeTests {
    private static Server server;
    static ServerFacade facade;

    @BeforeAll
    public static void init() {
        server = new Server();
        var port = server.run(0);
        System.out.println("Started test HTTP server on " + port);
        facade = new ServerFacade("http://localhost:" + port);
    }

    @BeforeEach
    void clearDatabase() throws Exception {
        facade.makeRequest("DELETE", "/db", null, null);
    }


    @AfterAll
    static void stopServer() {
        server.stop();
    }

    @Test
    void registerPositive() throws Exception {
        var authData = facade.register("testUser", "password", "test@byu.ed");
        assertTrue(authData.authToken().length() > 10);
    }

    @Test
    void registerNegative() throws Exception {
        facade.register("player1", "password", "test@byu.edu");
        assertThrows(Exception.class, () -> facade.register("player1", "password", "test@byu.edu"));
    }

    @Test
    public void sampleTest() {
        Assertions.assertTrue(true);
    }

}
