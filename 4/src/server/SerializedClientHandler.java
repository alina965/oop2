package server;

import common.UserInfo;
import common.commands.*;
import common.responses.*;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.UUID;

public class SerializedClientHandler extends ClientHandler {
    private final ObjectOutputStream output;

    public SerializedClientHandler(Socket socket, Server server) throws IOException {
        super(socket, server);
        this.output = new ObjectOutputStream(socket.getOutputStream());
    }

    @Override
    public void run() {
        try {
            ObjectInputStream input = new ObjectInputStream(socket.getInputStream());

            while (running) {
                ChatCommand command = (ChatCommand) input.readObject();
                processCommand(command);
            }
        }
        catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException("Client disconnected: " + e.getMessage());
        }
        finally {
            handleLogout();
            server.getClients().remove(this);
        }
    }

    private void processCommand(ChatCommand command) throws IOException {
        switch (command) {
            case LoginCommand loginCommand -> handleLogin(loginCommand);
            case ListUsersCommand listUsersCommand -> handleListUsers();
            case SendMessageCommand sendMessageCommand -> handleSendMessage(sendMessageCommand);
            case LogoutCommand logoutCommand -> {
                handleLogout();
                sendResponse(new SuccessResponse());
                running = false;
            }
            case null, default -> sendResponse(new ErrorResponse("Unknown command"));
        }
    }

    private void handleLogin(LoginCommand command) {
        UserInfo userInfo = new UserInfo(command.getName(), command.getType());
        UUID sessionId = server.getSessionManager().createSession(userInfo);

        if (sessionId == null) {
            sendResponse(new ErrorResponse("User already exists"));
            return;
        }

        this.userInfo = userInfo;
        sendResponse(new LoginSuccessResponse(sessionId));

        UserLoginEvent event = new UserLoginEvent(userInfo.getName());
        server.broadcast(event, this);
    }

    private void handleListUsers() {
        UserInfo[] users = server.getSessionManager().getAllUsers();
        sendResponse(new ListUsersResponse(users));
    }

    private void handleSendMessage(SendMessageCommand command) {
        UserInfo sender = server.getSessionManager().getUserInfo(command.getSessionId());
        if (sender == null) {
            sendResponse(new ErrorResponse("Invalid session"));
            return;
        }

        MessageEvent event = new MessageEvent(sender.getName(), command.getMessage());
        server.broadcast(event, this);
        sendResponse(new SuccessResponse());
    }

    private void handleLogout() {
        if (userInfo != null) {
            server.getSessionManager().removeSession(userInfo.getName());
            UserLogoutEvent event = new UserLogoutEvent(userInfo.getName());
            server.broadcast(event, this);
        }
    }

    @Override
    public void sendResponse(ChatResponse response) {
        try {
            output.writeObject(response);
            output.flush();
        }
        catch (IOException e) {
            throw new RuntimeException("Error sending response: " + e.getMessage());
        }
    }

    @Override
    public void stop() throws IOException {
        running = false;
        socket.close();
    }
}