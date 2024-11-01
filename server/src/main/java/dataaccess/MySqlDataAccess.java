package dataaccess;

import chess.ChessGame;
import com.google.gson.Gson;
import model.AuthData;
import model.GameData;
import model.UserData;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.*;
import java.util.ArrayList;


import static java.sql.Statement.RETURN_GENERATED_KEYS;
import static java.sql.Types.NULL;

public class MySqlDataAccess implements DataAccess {

    private final Gson gson;

    public MySqlDataAccess()  throws DataAccessException {
        gson = new Gson();
        configureDatabase();
    }

    @Override
    public void clear() throws DataAccessException {
        var statements = new String[] {
                "DROP TABLE IF EXISTS auth_tokens",
                "DROP TABLE IF EXISTS games",
                "DROP TABLE IF EXISTS users",
        };

        try (var conn = DatabaseManager.getConnection()) {
            for (var statement : statements) {
                try (var ps = conn.prepareStatement(statement)) {
                    ps.executeUpdate();
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException(String.format("Unable to clear database: %s", e.getMessage()));
        }
        configureDatabase();
    }

    @Override
    public void createUser(UserData user) throws DataAccessException {
        var statement = "INSERT INTO users(username, password, email) VALUES (?, ?, ?)";
        var hashedPassword = BCrypt.hashpw(user.password(), BCrypt.gensalt());
        executeUpdate(statement, user.username(), hashedPassword, user.email());
    }

    @Override
    public UserData getUser(String username) throws DataAccessException {
        try (var conn = DatabaseManager.getConnection()) {
            var statement = "SELECT username, password, email FROM users WHERE username=?";
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



    @Override
    public void createAuth(AuthData auth) throws DataAccessException {
        // First check if user exists
        if (getUser(auth.username()) == null) {
            throw new DataAccessException("User doesn't exist");
        }

        var statement = "INSERT INTO auth_tokens (auth_token, username) VALUES (?, ?)";
        try (var conn = DatabaseManager.getConnection()) {
            try (var ps = conn.prepareStatement(statement)) {
                ps.setString(1, auth.authToken());
                ps.setString(2, auth.username());
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            throw new DataAccessException(String.format("unable to update database: %s", e.getMessage()));
        }
    }

    @Override
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
        } catch (SQLException e) {
            throw new DataAccessException(String.format("Unable to read data: %s", e.getMessage()));
        }
        return null;
    }

    @Override
    public void deleteAuth(String authToken) throws DataAccessException {
        var statement = "DELETE FROM auth_tokens WHERE auth_token=?";
        try (var conn = DatabaseManager.getConnection()) {
            try (var ps = conn.prepareStatement(statement)) {
                ps.setString(1, authToken);
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            throw new DataAccessException(String.format("unable to update database: %s", e.getMessage()));
        }
    }

    @Override
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

    // helper function
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
    @Override
    public void createGame(GameData game) throws DataAccessException {
        var statement = "INSERT INTO games (game_id, game_name, white_username, black_username, game_state) VALUES (?, ?, ?, ?, ?)";
        try (var conn = DatabaseManager.getConnection()) {
            try (var ps = conn.prepareStatement(statement)) {
                ps.setInt(1, game.gameID());
                ps.setString(2, game.gameName());
                ps.setString(3, game.whiteUsername());
                ps.setString(4, game.blackUsername());
                ps.setString(5, gson.toJson(game.game()));  // Serialize the chess game to JSON
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            throw new DataAccessException(String.format("unable to update database: %s", e.getMessage()));
        }
    }

    @Override
    public GameData getGame(int gameID) throws DataAccessException {
        try (var conn = DatabaseManager.getConnection()) {
            var statement = "SELECT game_id, white_username, black_username, game_name, game_state " +
                    "FROM games WHERE game_id=?";
            try (var ps = conn.prepareStatement(statement)) {
                ps.setInt(1, gameID);
                try (var rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return readGame(rs);
                    }
                }
            }
        } catch (Exception e) {
            throw new DataAccessException(String.format("Unable to read game: %s", e.getMessage()));
        }
        return null;
    }

    @Override
    public void updateGame(GameData game) throws DataAccessException {
        try (var conn = DatabaseManager.getConnection()) {
            var checkStatement = "SELECT game_id FROM games WHERE game_id=?";
            try (var ps = conn.prepareStatement(checkStatement)) {
                ps.setInt(1, game.gameID());
                var rs = ps.executeQuery();
                if (!rs.next()) {
                    throw new DataAccessException("Game not found");
                }
            }
            var statement = "UPDATE games SET white_username=?, black_username=?, game_state=? WHERE game_id=?";
            try (var ps = conn.prepareStatement(statement)) {
                ps.setString(1, game.whiteUsername());
                ps.setString(2, game.blackUsername());
                ps.setString(3, gson.toJson(game.game()));
                ps.setInt(4, game.gameID());
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            throw new DataAccessException(String.format("unable to update database: %s", e.getMessage()));
        }
    }

    // helper function
    private int executeUpdate(String statement, Object... params) throws DataAccessException {
        try (var conn = DatabaseManager.getConnection()) {
            try (var ps = conn.prepareStatement(statement, RETURN_GENERATED_KEYS)) {
                for (var i = 0; i < params.length; i++) {
                    var param = params[i];
                    if (param instanceof String p) {
                        ps.setString(i + 1, p);
                    }
                    else if (param instanceof Integer p) {
                        ps.setInt(i + 1, p);
                    }
                    else if (param == null) {
                        ps.setNull(i + 1, NULL);
                        }
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
        PRIMARY KEY (game_id)
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
