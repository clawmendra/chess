package service;
import dataaccess.DataAccess;
import dataaccess.MemoryDataAccess;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class ClearServiceTest {
    private ClearService clearService;
    private DataAccess dataAccess;

    @BeforeEach
    void setUp() {
        dataAccess = new MemoryDataAccess();
        clearService = new ClearService(dataAccess);
    }

    @Test
    void clearDatabaseConfirm() throws Exception {
        clearService.clearDatabase();
    }
}
