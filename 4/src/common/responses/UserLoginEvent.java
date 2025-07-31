package common.responses;

public class UserLoginEvent extends ChatResponse {
    private final String username;

    public UserLoginEvent(String username) {
        super(null);
        this.username = username;
    }

    public String getUsername() {
        return username;
    }
}