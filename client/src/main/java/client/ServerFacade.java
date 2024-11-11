package client;

import com.google.gson.Gson;
import exception.ResponseException;

public class ServerFacade {

    private final String serverUrl;

    public ServerFacade(String url) {
        serverUrl = url;
        gson = new Gson();
    }

    public RegisterResult register(RegisterRequest request){...}

    public JoinResult join(JoinRequest request) {...}

    }




