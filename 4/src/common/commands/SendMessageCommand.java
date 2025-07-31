package common.commands;

import java.util.UUID;

public class SendMessageCommand extends ChatCommand {
    private final String message;
    private UUID sessionId;

    public SendMessageCommand(String message) {
        super("message");
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public UUID getSessionId() {
        return sessionId;
    }

    public void setSessionId(UUID sessionId) {
        this.sessionId = sessionId;
    }
}