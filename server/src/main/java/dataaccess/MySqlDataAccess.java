package dataaccess;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.sql.SQLException;

public class MySqlDataAccess implements DataAccess {
    private final Gson gson;

    public MySqlDataAccess()  throws DataAccessException {
        gson = new Gson();
        configureDatabase();
    }

    public void clear() throws DataAccessException {
        var statement = new String[] {
                "TRUNCATE auth_tokens",
                "TRUNCATE games",
                "TRUNCATE users"
    };
    for (var statement : statements) {
        executeUpdate(statement);
     }
}
    private int executeUpdate(String statement, Object... params) throws ResponseException {
        try (var conn = DatabaseManager.getConnection()) {
            try (var ps = conn.prepareStatement(statement, RETURN_GENERATED_KEYS)) {
                for (var i = 0; i < params.length; i++) {
                    var param = params[i];
                    if (param instanceof String p) ps.setString(i + 1, p);
                    else if (param instanceof Integer p) ps.setInt(i + 1, p);
                    else if (param == null) ps.setNull(i + 1, NULL);
                }
                ps.executeUpdate();

                var rs = ps.getGeneratedKeys();
                if (rs.next()) {
                    return rs.getInt(1);
                }
                return 0;
            }
        } catch (SQLException e) {
            throw new ResponseException(500, String.format("unable to update database: %s, %s", statement, e.getMessage()));
        }
    }


}
