package client;

import com.google.gson.Gson;
import exception.ResponseException;
import model.AuthData;
import model.GameData;
import model.UserData;

import java.io.*;
import java.net.*;


public class ServerFacade {
    private final String serverUrl;

    public ServerFacade(String url) {
        serverUrl = url;
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
        record listGamesResponse(GameData[] games) {}
        var response = this.makeRequest("GET", path, null, listGamesResponse.class);
        return response.games();
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

    private void throwIfNotSuccessful(HttpURLConnection http) throws IOException, ResponseException {
        var status = http.getResponseCode();
        if (!isSuccessful(status)) {
            throw new ResponseException(status, "failure: " + status);
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


