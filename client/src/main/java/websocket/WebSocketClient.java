package websocket;
import com.google.gson.Gson;

import com.google.gson.GsonBuilder;
import websocket.messages.ServerMessage;
import websocket.commands.UserGameCommand;

import javax.websocket.*;
import java.net.URI;


public class WebSocketClient extends Endpoint {
    private Session session;
    private final ServerMessageHandler messageHandler;
    private final Gson gson;

    public WebSocketClient(String serverUrl, ServerMessageHandler handler) throws Exception {
        this.messageHandler = handler;
        this.gson = new GsonBuilder()
                .registerTypeAdapter(ServerMessage.class, new ServerMessageDeserializer())
                .create();

        URI uri = new URI(serverUrl);
        WebSocketContainer container = ContainerProvider.getWebSocketContainer();
        this.session = container.connectToServer(this, uri);

        // Add message handler for incoming messages
        this.session.addMessageHandler(new MessageHandler.Whole<String>() {
            @Override
            public void onMessage(String message) {
                try {
                    // Convert JSON message to ServerMessage object
                    ServerMessage serverMessage = gson.fromJson(message, ServerMessage.class);
                    // Pass to message handler (GamePlay)
                    messageHandler.handleServerMessage(serverMessage);
                } catch (Exception e) {
                    System.err.println("Error handling message: " + e.getMessage());
                }
            }
        });
    }

    public void sendCommand(UserGameCommand command) {
        try {
            if (session != null && session.isOpen()) {
                // Convert command to JSON
                String jsonCommand = gson.toJson(command);
                session.getBasicRemote().sendText(jsonCommand);
            } else {
                System.err.println("WebSocket is not connected!");
            }
        } catch (Exception e) {
            System.err.println("Error sending command: " + e.getMessage());
        }
    }

    @Override
    public void onOpen(Session session, EndpointConfig endpointConfig) {
        System.out.println("WebSocket Connected!");
        this.session = session;
    }

    @Override
    public void onClose(Session session, CloseReason closeReason) {
        System.out.println("WebSocket Closed! Reason: " + closeReason.getReasonPhrase());
        this.session = null;
    }

    @Override
    public void onError(Session session, Throwable throwable) {
        System.err.println("WebSocket Error: " + throwable.getMessage());
    }

    public interface ServerMessageHandler {
        void handleServerMessage(ServerMessage message);
    }

}
