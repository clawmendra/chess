package websocket.messages;

public class ErrorMessage extends ServerMessage {
    private final String errorMessage;

    public ErrorMessage(String message) {
        super(ServerMessageType.ERROR);
        this.errorMessage = "Error: " + message;
    }

    public String getErrorMessage() {
        return errorMessage;
    }
}
