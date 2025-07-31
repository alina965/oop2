package common.responses;

public class ErrorResponse extends ChatResponse {
    public ErrorResponse(String errorMessage) {
        super(errorMessage);
    }
}