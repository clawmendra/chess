// Add this class in the websocket package
package websocket;

import chess.ChessGame;
import com.google.gson.*;
import websocket.messages.*;

import java.lang.reflect.Type;

public class ServerMessageDeserializer implements JsonDeserializer<ServerMessage> {
    @Override
    public ServerMessage deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
            throws JsonParseException {

        JsonObject jsonObject = json.getAsJsonObject();
        String messageType = jsonObject.get("serverMessageType").getAsString();

        return switch (messageType) {
            case "LOAD_GAME" -> {
                ChessGame game = null;
                if (jsonObject.has("game")) {
                    game = context.deserialize(jsonObject.get("game"), ChessGame.class);
                }
                yield new LoadGameMessage(game);
            }
            case "ERROR" -> {
                String errorMessage = "";
                if (jsonObject.has("errorMessage")) {
                    errorMessage = jsonObject.get("errorMessage").getAsString();
                }
                yield new ErrorMessage(errorMessage);
            }
            case "NOTIFICATION" -> {
                String message = "";
                if (jsonObject.has("message")) {
                    message = jsonObject.get("message").getAsString();
                }
                yield new NotificationMessage(message);
            }
            default -> throw new JsonParseException("Unknown message type: " + messageType);
        };
    }
}