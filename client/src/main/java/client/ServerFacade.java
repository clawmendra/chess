package client;

import com.google.gson.Gson;
import model.AuthData;
import model.GameData;
import model.UserData;

import java.io.*;
import java.net.*;


public class ServerFacade {
    private final String serverUrl;

    public ServerFacade(String url) {
        this.serverUrl = url;
    }

    public AuthData register(String username, String password, String email) throws Exception {
        var path = "/user";
        var request = new UserData(username, password, email);
        return this.makeRequest("POST", path, request, AuthData.class);
    }

    public AuthData login(String username, String password) throws Exception {
        var path = "/session";
        var request = new UserData(username, password, null);
        return this.makeRequest("POST", path, request, AuthData.class);
    }

    public void logout(String authToken) throws Exception {
        var path = "/session";
        this.makeRequest("DELETE", path, null, null, authToken);
    }

    public GameData[] listGames(String authToken) throws Exception {
        var path = "/game";
        record ListGamesResp(GameData[] games) {}
        var response = this.makeRequest("GET", path, null, ListGamesResp.class, authToken);
        return response.games();
    }

    public GameData createGame(String gameName, String authToken) throws Exception {
        var path = "/game";
        record CreateGameRequest(String gameName) {}
        var request = new CreateGameRequest(gameName);
        return this.makeRequest("POST", path, request, GameData.class, authToken);
    }

    public void joinGame(int gameID, String playerColor, String authToken) throws Exception {
        var path = "/game";
        record JoinGameRequest(String playerColor, int gameID) {}
        var request = new JoinGameRequest(playerColor, gameID);
        this.makeRequest("PUT", path, request, null, authToken);
    }

    public <T> T makeRequest(String method, String path, Object request, Class<T> responseClass, String authToken) throws Exception {
        try {
            URL url = (new URI(serverUrl + path)).toURL();
            HttpURLConnection http = (HttpURLConnection) url.openConnection();
            http.setRequestMethod(method);
            http.setDoOutput(true);

            if (authToken != null) {
                http.addRequestProperty("Authorization", authToken);
            }
            writeBody(request, http);
            http.connect();
            throwIfNotSuccessful(http);
            return readBody(http, responseClass);
        } catch (Exception ex) {
            throw new Exception(ex.getMessage());
        }
    }

    public <T> T makeRequest(String method, String path, Object request, Class<T> responseClass) throws Exception {
        return makeRequest(method, path, request, responseClass, null);
    }



    private static void writeBody(Object request, HttpURLConnection http) throws IOException {
        if (request != null) {
            http.addRequestProperty("Content-Type", "application/json");
            String reqData = new Gson().toJson(request);
            try (OutputStream reqBody = http.getOutputStream()) {
                reqBody.write(reqData.getBytes());
            }
        }
    }

    private void throwIfNotSuccessful(HttpURLConnection http) throws IOException {
        var status = http.getResponseCode();
        if (!isSuccessful(status)) {
            String errorMessage = "Failure: " + status + " - " + http.getResponseMessage();
            throw new IOException(errorMessage);
        }
    }

    private static <T> T readBody(HttpURLConnection http, Class<T> responseClass) throws IOException {
        T response = null;
        if (http.getContentLength() < 0) {
            try (InputStream respBody = http.getInputStream()) {
                InputStreamReader reader = new InputStreamReader(respBody);
                if (responseClass != null) {
                    response = new Gson().fromJson(reader, responseClass);
                }
            }
        }
        return response;
    }


    private boolean isSuccessful(int status) {
        return status / 100 == 2;
    }
}

