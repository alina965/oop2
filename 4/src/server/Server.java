package server;

import common.SessionManager;

import common.responses.ChatResponse;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Server {
    private final SessionManager sessionManager = new SessionManager();
    private final ProtocolType protocol;
    private final int port;
    private boolean running;
    private ServerSocket serverSocket;
    private final List<ClientHandler> clients = new ArrayList<>();

    public Server(int port, ProtocolType protocol) {
        this.port = port;
        this.protocol = protocol;
    }

    public void start() throws IOException {
        running = true;
        serverSocket = new ServerSocket(port);

        while (running) {
            Socket clientSocket = serverSocket.accept();
            ClientHandler clientHandler = createClientHandler(clientSocket);
            clients.add(clientHandler);
            new Thread(clientHandler).start();
        }
    }

    public void stop() throws IOException {
        running = false;
        for (ClientHandler client : clients) {
            client.stop();
        }

        serverSocket.close();
    }

    public ClientHandler createClientHandler(Socket clientSocket) throws IOException {
        return switch (protocol) {
            case SERIALIZED -> new SerializedClientHandler(clientSocket, this);
            case XML -> new XmlClientHandler(clientSocket, this);
        };
    }

    public void broadcast(ChatResponse response, ClientHandler exclude) {
        for (ClientHandler client : clients) {
            if (client != exclude) {
                client.sendResponse(response);
            }
        }
    }

    public SessionManager getSessionManager() {
        return sessionManager;
    }

    public List<ClientHandler> getClients() {
        return clients;
    }
}
