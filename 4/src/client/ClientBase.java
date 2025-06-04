package client;

import common.commands.ChatCommand;
import common.responses.ChatResponse;

import java.io.IOException;

public abstract class ClientBase {
    protected String serverAddress;
    protected int serverPort;
    protected String name;
    protected String type;
    protected volatile boolean connected = false;

    public ClientBase(String serverAddress, int serverPort, String name, String type) {
        this.serverAddress = serverAddress;
        this.serverPort = serverPort;
        this.name = name;
        this.type = type;
    }

    public abstract void connect() throws IOException, ClassNotFoundException;
    public abstract void disconnect() throws IOException;
    public abstract void sendCommand(ChatCommand command) throws IOException;
    public abstract ChatResponse receiveResponse() throws IOException;
    public abstract boolean isConnected();

    public String getName() {
        return name;
    }
}
