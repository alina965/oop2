package client;

import common.commands.*;
import common.responses.ChatResponse;
import common.responses.ErrorResponse;
import common.responses.LoginSuccessResponse;

import java.io.*;
import java.net.Socket;
import java.util.UUID;

public class SerializedChatClient extends ClientBase {
    private Socket socket;
    private ObjectOutputStream output;
    private ObjectInputStream input;
    private UUID sessionId;

    public SerializedChatClient(String serverAddress, int serverPort, String name, String type) {
        super(serverAddress, serverPort, name, type);
    }

    @Override
    public void connect() {
        try {
            socket = new Socket(serverAddress, serverPort);
            output = new ObjectOutputStream(socket.getOutputStream());
            input = new ObjectInputStream(socket.getInputStream());
            connected = true;

            LoginCommand loginCmd = new LoginCommand(name, type);
            output.writeObject(loginCmd);

            ChatResponse response = (ChatResponse) input.readObject();
            if (response instanceof ErrorResponse) {
                connected = false;
                throw new IOException("Login failed: " + response.getErrorMessage());
            }

            this.sessionId = ((LoginSuccessResponse) response).getSessionId();
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void disconnect() {
        try {
            if (connected) {
                LogoutCommand logoutCmd = new LogoutCommand();
                output.writeObject(logoutCmd);
                connected = false;
            }

            input.close();
            output.close();

            if (socket != null) {
                socket.close();
            }
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void sendCommand(ChatCommand command) throws IOException {
        if (command instanceof ListUsersCommand) {
            ListUsersCommand listCmd = new ListUsersCommand();
            output.writeObject(listCmd);
        }
        else if (command instanceof SendMessageCommand msgCmd) {
            msgCmd.setSessionId(sessionId);
            output.writeObject(msgCmd);
        }
    }

    @Override
    public ChatResponse receiveResponse() throws IOException {
        try {
            return (ChatResponse) input.readObject();
        }
        catch (ClassNotFoundException | IOException e) {
            throw new IOException("Invalid server response", e);
        }
    }

    @Override
    public boolean isConnected() {
        return connected;
    }
}