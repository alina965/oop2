package common.responses;

import java.util.UUID;

public class LoginSuccessResponse extends ChatResponse {
    private final UUID sessionId;

    public LoginSuccessResponse(UUID sessionId) {
        super(null);
        this.sessionId = sessionId;
    }

    public UUID getSessionId() {
        return sessionId;
    }
}