package dataaccess;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class ConnectTest {
    @Test
    void testConnection() throws Exception {
        try {
            DatabaseManager.createDatabase();
            var conn = DatabaseManager.getConnection();
            assertNotNull(conn);
            conn.close();
        } catch (DataAccessException e) {
            fail("Database connection failed: " + e.getMessage());
        }
    }
}