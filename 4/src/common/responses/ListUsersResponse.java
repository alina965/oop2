package common.responses;

import common.UserInfo;

public class ListUsersResponse extends ChatResponse {
    private final UserInfo[] users;

    public ListUsersResponse(UserInfo[] users) {
        super(null);
        this.users = users;
    }

    public UserInfo[] getUsers() {
        return users;
    }
}