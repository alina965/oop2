package common.responses;

public class UserLogoutEvent extends ChatResponse {
    private final String username;

    public UserLogoutEvent(String username) {
        super(null);
        this.username = username;
    }

    public String getUsername() {
        return username;
    }
}