package client;

import common.*;
import common.commands.ChatCommand;
import common.commands.ListUsersCommand;
import common.commands.SendMessageCommand;
import common.responses.*;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.swing.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

public class XmlChatClient extends ClientBase implements Runnable {
    private Socket socket;
    private InputStream input;
    private OutputStream output;
    private UUID sessionId;
    private MessageListener messageListener;
    private Element root;

    public XmlChatClient(String serverAddress, int serverPort, String name, String type) {
        super(serverAddress, serverPort, name, type);
    }

    public void setMessageListener(MessageListener listener) {
        this.messageListener = listener;
    }

    @Override
    public void connect() throws IOException {
        socket = new Socket(serverAddress, serverPort);
        input = socket.getInputStream();
        output = socket.getOutputStream();
        connected = true;

        String loginXml = String.format("<command name=\"login\"><name>%s</name><type>%s</type></command>", name, type);
        sendXmlCommand(loginXml);

        String response = readXmlResponse();
        if (response.contains("<error>")) {
            connected = false;
            throw new IOException("Login failed: " + extractErrorMessage(response));
        }

        this.sessionId = UUID.fromString(extractSessionId(response));

        Thread listenerThread = new Thread(this);
        listenerThread.start();
    }

    @Override
    public void run() {
        while (connected) {
            try {
                String xml = readXmlResponse();
                ChatResponse response = parseResponse(xml);

                if (response != null && messageListener != null) {
                    SwingUtilities.invokeLater(() -> handleResponse(response));
                }
            }
            catch (IOException e) {
                handleDisconnection(e);
                break;
            }
        }
    }

    private ChatResponse parseResponse(String xml) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(new ByteArrayInputStream(xml.getBytes()));
            root = doc.getDocumentElement();

            switch (root.getNodeName()) {
                // обработка сообщений от сервера
                case "event" -> {
                    String eventName = root.getAttribute("name");
                    switch (eventName) {
                        case "message":
                            return parseMessageEvent();
                        case "userlogin":
                            return new UserLoginEvent(root.getElementsByTagName("name").item(0).getTextContent());
                        case "userlogout":
                            return new UserLogoutEvent(root.getElementsByTagName("name").item(0).getTextContent());
                    }
                }
                // обработка успешных ответов сервера
                case "success" -> {
                    NodeList listUsers = root.getElementsByTagName("listusers");

                    if (listUsers.getLength() > 0) {
                        return parseListUsersResponse();
                    }

                    NodeList sessionNodes = root.getElementsByTagName("session");
                    if (sessionNodes.getLength() > 0) {
                        return new LoginSuccessResponse(UUID.fromString(sessionNodes.item(0).getTextContent()));
                    }

                    return new SuccessResponse();
                }

                case "error" -> {
                    return new ErrorResponse(root.getElementsByTagName("message").item(0).getTextContent());
                }
            }
        }
        catch (Exception e) {
            return new ErrorResponse("Response parsing error");
        }
        return null;
    }

    @Override
    public void disconnect() {
        if (connected) {
            String logoutXml = String.format("<command name=\"logout\"><session>%s</session></command>", sessionId);
            try {
                sendXmlCommand(logoutXml);
            }
            catch (IOException e) {
                throw new RuntimeException(e);
            }
            connected = false;
        }
        if (socket != null) {
            try {
                socket.close();
            }
            catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public void sendCommand(ChatCommand command) throws IOException {
        if (command instanceof ListUsersCommand) {
            String listXml = String.format("<command name=\"list\"><session>%s</session></command>", sessionId);
            sendXmlCommand(listXml);
        }
        else if (command instanceof SendMessageCommand msgCmd) {
            String messageXml = String.format("<command name=\"message\"><session>%s</session><message>%s</message></command>", sessionId, escapeXml(msgCmd.getMessage()));
            sendXmlCommand(messageXml);
        }
    }

    @Override
    public ChatResponse receiveResponse() throws IOException {
        String xml = readXmlResponse();
        if (xml.contains("<event name=\"message\">")) {
            return parseMessageEvent();
        }
        else if (xml.contains("<event name=\"userlogin\">")) {
            return parseUserLoginEvent(xml);
        }
        else if (xml.contains("<event name=\"userlogout\">")) {
            return parseUserLogoutEvent(xml);
        }
        else if (xml.contains("<success><listusers>")) {
            return parseListUsersResponse();
        }
        else if (xml.contains("<success>")) {
            return new SuccessResponse();
        }
        else if (xml.contains("<error>")) {
            return new ErrorResponse(extractErrorMessage(xml));
        }
        return null;
    }

    @Override
    public boolean isConnected() {
        return connected;
    }

    private void sendXmlCommand(String xml) throws IOException {
        byte[] xmlBytes = xml.getBytes(StandardCharsets.UTF_8);
        byte[] lengthBytes = new byte[4];
        lengthBytes[0] = (byte) (xmlBytes.length >> 24);
        lengthBytes[1] = (byte) (xmlBytes.length >> 16);
        lengthBytes[2] = (byte) (xmlBytes.length >> 8);
        lengthBytes[3] = (byte) (xmlBytes.length);

        output.write(lengthBytes);
        output.write(xmlBytes);
        output.flush();
    }

    private String readXmlResponse() throws IOException {
        byte[] lengthBytes = new byte[4];
        input.read(lengthBytes);
        int length = ((lengthBytes[0] & 0xFF) << 24) |
                ((lengthBytes[1] & 0xFF) << 16) |
                ((lengthBytes[2] & 0xFF) << 8) |
                (lengthBytes[3] & 0xFF);

        byte[] messageBytes = new byte[length];
        input.read(messageBytes);
        return new String(messageBytes, StandardCharsets.UTF_8);
    }

    private String extractErrorMessage(String xml) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(new ByteArrayInputStream(xml.getBytes()));
            NodeList messageNodes = doc.getElementsByTagName("message");
            return messageNodes.item(0).getTextContent();
        }
        catch (Exception e) {
            return "Unknown error";
        }
    }

    private String extractSessionId(String xml) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(new ByteArrayInputStream(xml.getBytes()));
            NodeList sessionNodes = doc.getElementsByTagName("session");
            return sessionNodes.item(0).getTextContent();
        }
        catch (Exception e) {
            return null;
        }
    }

    private ChatResponse parseMessageEvent() {
        NodeList messageNodes = root.getElementsByTagName("message");
        NodeList nameNodes = root.getElementsByTagName("name");
        return new MessageEvent(nameNodes.item(0).getTextContent(), messageNodes.item(0).getTextContent());
    }

    private ChatResponse parseUserLoginEvent(String xml) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(new ByteArrayInputStream(xml.getBytes()));

            NodeList nameNodes = doc.getElementsByTagName("name");
            String username = nameNodes.item(0).getTextContent();

            return new UserLoginEvent(username);
        }
        catch (Exception e) {
            return new ErrorResponse("Failed to parse user login event");
        }
    }

    private ChatResponse parseUserLogoutEvent(String xml) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(new ByteArrayInputStream(xml.getBytes()));

            NodeList nameNodes = doc.getElementsByTagName("name");
            String username = nameNodes.item(0).getTextContent();

            return new UserLogoutEvent(username);
        }
        catch (Exception e) {
            return new ErrorResponse("Failed to parse user logout event");
        }
    }

    private ListUsersResponse parseListUsersResponse() {
        NodeList userNodes = root.getElementsByTagName("user");
        UserInfo[] users = new UserInfo[userNodes.getLength()];

        for (int i = 0; i < userNodes.getLength(); i++) {
            Element user = (Element) userNodes.item(i);
            users[i] = new UserInfo(user.getElementsByTagName("name").item(0).getTextContent(), user.getElementsByTagName("type").item(0).getTextContent());
        }
        return new ListUsersResponse(users);
    }

    private String escapeXml(String input) {
        return input.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&apos;");
    }

    private void handleResponse(ChatResponse response) {
        if (response instanceof ErrorResponse) {
            JOptionPane.showMessageDialog(null, "Ошибка: " + response.getErrorMessage(), "Ошибка", JOptionPane.ERROR_MESSAGE);
        }
        messageListener.onMessageReceived(response);
    }

    private void handleDisconnection(IOException e) {
        if (connected) {
            connected = false;
            SwingUtilities.invokeLater(() -> messageListener.onMessageReceived(new ErrorResponse("Соединение прервано: " + e.getMessage())));
            disconnect();
        }
    }
}