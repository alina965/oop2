package common.responses;

public class MessageEvent extends ChatResponse {
    private final String sender;
    private final String message;

    public MessageEvent(String sender, String message) {
        super(null);
        this.sender = sender;
        this.message = message;
    }

    public String getSender() {
        return sender;
    }

    public String getMessage() {
        return message;
    }
}