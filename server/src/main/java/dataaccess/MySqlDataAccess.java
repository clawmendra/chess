package dataaccess;

import chess.ChessGame;
import com.google.gson.Gson;
import model.AuthData;
import model.GameData;
import model.UserData;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.*;
import java.util.ArrayList;
import java.util.UUID;

import static java.sql.Statement.RETURN_GENERATED_KEYS;
import static java.sql.Types.NULL;

public class MySqlDataAccess implements DataAccess {
    private final Gson gson;

    public MySqlDataAccess()  throws DataAccessException {
        gson = new Gson();
        configureDatabase();
    }

    public void clear() throws DataAccessException {
        var statements = new String[] {
                "TRUNCATE auth_tokens",
                "TRUNCATE games",
                "TRUNCATE users"
    };
    for (var statement : statements) {
        executeUpdate(statement);
     }
}

    public void createUser(UserData user) throws DataAccessException {
        var statement = "INSERT INTO users(username, password, email) VALUES (?, ?, ?)";
        var hashedPassword = BCrypt.hashpw(user.password(), BCrypt.gensalt());
        executeUpdate(statement, user.username(), hashedPassword, user.email());
    }

    public UserData getUser(String username) throws DataAccessException {
        try (var conn = DatabaseManager.getConnection()) {
            var statement = "SELECT username, password, email FROM user WHERE username=?";
            try (var ps = conn.prepareStatement(statement)) {
                ps.setString(1, username);
                try (var rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return new UserData(
                                rs.getString("username"),
                                rs.getString("password"),
                                rs.getString("email")
                        );
                    }
                }
            }
        } catch (Exception e) {
            throw new DataAccessException(String.format("Unable to read data: %s", e.getMessage()));
        }
        return null;
    }

    public AuthData createAuth(String username) throws DataAccessException {
        var statement = "INSERT INTO auth_tokens (auth_token, username) VALUES (?, ?)";
        var authToken = UUID.randomUUID().toString();
        executeUpdate(statement, authToken, username);
        return new AuthData(authToken, username);
    }

    public AuthData getAuth(String authToken) throws DataAccessException {
        try (var conn = DatabaseManager.getConnection()) {
            var statement = "SELECT auth_token, username FROM auth_tokens WHERE auth_token=?";
            try (var ps = conn.prepareStatement(statement)) {
                ps.setString(1, authToken);
                try (var rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return new AuthData(
                                rs.getString("auth_token"),
                                rs.getString("username")
                        );
                    }
                }
            }
        } catch (Exception e) {
            throw new DataAccessException(String.format("Unable to read auth token: %s", e.getMessage()));
        }
        return null;
    }

    public void deleteAuth(String authToken) throws DataAccessException {
        var statement = "DELETE FROM auth_tokens WHERE auth_token=?";
        executeUpdate(statement, authToken);
    }

    public GameData[] listGames() throws DataAccessException {
        var games = new ArrayList<GameData>();
        try (var conn = DatabaseManager.getConnection()) {
            var statement = "SELECT game_id, white_username, black_username, game_name, game_state FROM games";
            try (var ps = conn.prepareStatement(statement)) {
                try (var rs = ps.executeQuery()) {
                    while (rs.next()) {
                        games.add(readGame(rs));
                    }
                }
            }
        } catch (Exception e) {
            throw new DataAccessException(String.format("Unable to read games: %s", e.getMessage()));
        }
        return games.toArray(new GameData[0]);
    }

    private GameData readGame(ResultSet rs) throws SQLException {
        var gameState = rs.getString("game_state");
        var game = gson.fromJson(gameState, ChessGame.class);
        return new GameData(
                rs.getInt("game_id"),
                rs.getString("white_username"),
                rs.getString("black_username"),
                rs.getString("game_name"),
                game
        );
    }

    public void createGame(GameData gameName) throws DataAccessException {
        var statement = "INSERT INTO games(game_name, game_state) VALUES (?, ?)";
        var game = new ChessGame();
        var json = gson.toJson(game);
        executeUpdate(statement, gameName, json);
    }

    private int executeUpdate(String statement, Object... params) throws DataAccessException {
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
            throw new DataAccessException(String.format("unable to update database: %s, %s", statement, e.getMessage()));
        }
    }

    private final String[] createStatements = {
            """
            CREATE TABLE IF NOT EXISTS users (
                username VARCHAR(255) PRIMARY KEY,
                password VARCHAR(255) NOT NULL,
                email VARCHAR(255) NOT NULL
                )
            """,
            """
            CREATE TABLE IF NOT EXISTS auth_tokens (
                auth_token VARCHAR(255) PRIMARY KEY,
                username VARCHAR(255) NOT NULL
                )
            """,
            """
            CREATE TABLE IF NOT EXISTS games (
                game_id INT NOT NULL AUTO_INCREMENT,
                game_name VARCHAR(255) NOT NULL,
                white_username VARCHAR(255),
                black_username VARCHAR(255),
                game_state TEXT NOT NULL,
                PRIMARY KEY (game_id),
                )
            """
    };

    private void configureDatabase() throws DataAccessException {
        DatabaseManager.createDatabase();
        try (var conn = DatabaseManager.getConnection()) {
            for (var statement : createStatements) {
                try (var ps = conn.prepareStatement(statement)) {
                    ps.executeUpdate();
                }
            }
        } catch (SQLException ex) {
            throw new DataAccessException(String.format("Unable to configure database: %s", ex.getMessage()));
        }
    }


}
