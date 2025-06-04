package client;

import common.responses.ChatResponse;

public interface MessageListener {
    void onMessageReceived(ChatResponse response);
}