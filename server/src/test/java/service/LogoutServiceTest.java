package service;
import dataaccess.DataAccess;
import dataaccess.DataAccessException;
import dataaccess.MemoryDataAccess;
import model.AuthData;
import model.UserData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class LogoutServiceTest {
    private LogoutService logoutService;
    private DataAccess dataAccess;
    private String existAuthToken;

    @BeforeEach
    void setUp() throws DataAccessException {
        dataAccess = new MemoryDataAccess();
        logoutService = new LogoutService(dataAccess);
        String username = "testUser";
        String password = "testPassword";
        String email = "test@example.com";
        dataAccess.createUser(new UserData(username, password, email));
        existAuthToken = "testAuthToken";
        dataAccess.createAuth(new AuthData(existAuthToken, username));
    }
    @Test
    void goodLogout() throws DataAccessException {
        logoutService.logout(existAuthToken);
        assertNull(dataAccess.getAuth(existAuthToken));
    }
    @Test
    void badAuthToken() {
        String badAuthToken = "badAuthToken";
        DataAccessException ex = assertThrows(DataAccessException.class,
                () -> logoutService.logout(badAuthToken));
        assertEquals("Error: unauthorized", ex.getMessage());
    }

    @Test
    void nullToken() {
        DataAccessException exception = assertThrows(DataAccessException.class,
                () -> logoutService.logout(null));
        assertEquals("Error: unauthorized", exception.getMessage());
    }
    @Test
    void authTokenRemove() throws DataAccessException {
       logoutService.logout(existAuthToken);
        assertThrows(DataAccessException.class, () -> logoutService.logout(existAuthToken));
    }

}
