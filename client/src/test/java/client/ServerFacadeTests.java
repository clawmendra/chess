package client;

import exception.ResponseException;
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
        var authData = facade.register("testUser", "password", "test@byu.edu");
        assertTrue(authData.authToken().length() > 10);
    }

    @Test
    void registerNegative() throws Exception {
        facade.register("player1", "password", "test@byu.edu");
        assertThrows(Exception.class, () -> facade.register("player1", "password", "test@byu.edu"));
    }

    @Test
    void loginPositive() throws Exception {
        facade.register("player1", "password", "test@byu.edu");
        var authData = facade.login("player1", "password");
        assertTrue(authData.authToken().length() > 10);
    }

    @Test
    void loginNegative() throws Exception {
        assertThrows(Exception.class, () -> facade.login("DNEuser", "password"));
    }

    @Test
    void logoutPositive() throws Exception {
        var authData = facade.register("player1", "password", "test@byu.edu");
        assertDoesNotThrow(() -> facade.logout(authData.authToken()));
        assertThrows(Exception.class, () -> facade.listGames(authData.authToken()));
    }

    @Test
    void logoutNegative() throws Exception {
        assertThrows(Exception.class, () -> facade.logout("badToken"));
    }

    @Test
    public void sampleTest() {
        Assertions.assertTrue(true);
    }

}
