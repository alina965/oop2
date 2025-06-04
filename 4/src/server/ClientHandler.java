package server;

import common.responses.ChatResponse;
import common.UserInfo;

import java.io.IOException;
import java.net.Socket;

public abstract class ClientHandler implements Runnable {
    protected Socket socket;
    protected Server server;
    protected volatile boolean running = true;
    protected UserInfo userInfo;

    public ClientHandler(Socket socket, Server server) {
        this.socket = socket;
        this.server = server;
    }

    public abstract void sendResponse(ChatResponse response);

    public abstract void stop() throws IOException;
}