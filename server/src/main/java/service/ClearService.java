package service;
import dataaccess.DataAccess;
import dataaccess.DataAccessException;


public class ClearService {
    private final DataAccess dataAccess;

    public ClearService(DataAccess dataAccess) {
        this.dataAccess = dataAccess;
    }

    public void clearDatabase() throws dataaccess.DataAccessException {
        dataAccess.clear();
    }
}
