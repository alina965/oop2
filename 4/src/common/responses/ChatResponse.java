package common.responses;

import java.io.Serializable;

public class ChatResponse implements Serializable {
    private final String errorMessage;

    public ChatResponse(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getErrorMessage() {
        return errorMessage;
    }
}
