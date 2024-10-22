package service;
import dataaccess.DataAccess;
import dataaccess.DataAccessException;
import dataaccess.MemoryDataAccess;
import model.UserData;
import model.AuthData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class LoginServiceTest {
    private LoginService loginService;
    private DataAccess dataAccess;

    @BeforeEach
    void setUp() {
        dataAccess = new MemoryDataAccess();
        loginService = new LoginService(dataAccess);
    }

    @Test
    void login_GoodLogin() throws DataAccessException {
        String username = "testUser";
        String password = "testPassword";
        String email = "test@example.com";
        dataAccess.createUser(new UserData(username, password, email));
        AuthData result = loginService.login(username, password);
        assertNotNull(result);
        assertEquals(username, result.username());
        assertNotNull(result.authToken());
    }

    @Test
    void login_InvalidUsername() {
        String username = "testUser";
        String password = "testPassword";
        assertThrows(DataAccessException.class, () -> loginService.login(username, password));
    }

    @Test
    void login_InvalidPassword() throws DataAccessException {
        String username = "testUser";
        String correctPassword = "correctPassword";
        String wrongPassword = "wrongPassword";
        String email = "test@example.com";
        dataAccess.createUser(new UserData(username, correctPassword, email));
        assertThrows(DataAccessException.class, () -> loginService.login(username, wrongPassword));
    }

    @Test
    void login_NullPassword() {
        String username = "testUser";
        assertThrows(DataAccessException.class, () -> loginService.login(username, null));
    }
}
