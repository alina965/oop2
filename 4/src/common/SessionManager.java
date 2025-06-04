package common;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SessionManager {
    private final Map<UUID, UserInfo> sessions = new HashMap<>();
    private final Map<String, UUID> nameToSession = new HashMap<>();

    public synchronized UUID createSession(UserInfo userInfo) {
        if (nameToSession.containsKey(userInfo.getName())) {
            return null;
        }

        UUID sessionId = UUID.randomUUID();
        sessions.put(sessionId, userInfo);
        nameToSession.put(userInfo.getName(), sessionId);
        return sessionId;
    }

    public synchronized UserInfo getUserInfo(UUID sessionId) {
        return sessions.get(sessionId);
    }

    public synchronized void removeSession(String sessionId) {
        UserInfo userInfo = sessions.get(sessionId);
        if (userInfo != null) {
            nameToSession.remove(userInfo.getName());
            sessions.remove(sessionId);
        }
    }

    public synchronized UserInfo[] getAllUsers() {
        return sessions.values().toArray(new UserInfo[0]);
    }
}