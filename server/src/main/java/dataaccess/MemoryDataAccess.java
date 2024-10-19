package dataaccess;

import java.util.HashMap;
import java.util.Map;

public class MemoryDataAccess implements DataAccess{
    private final Map<String, Object> data = new HashMap<>();

    @Override
    public void clear() throws DataAccessException {
        data.clear();
    }
}
