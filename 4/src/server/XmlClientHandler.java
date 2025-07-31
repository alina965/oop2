package server;

import common.UserInfo;
import common.responses.ChatResponse;
import common.responses.XmlResponse;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

public class XmlClientHandler extends ClientHandler {
    private final OutputStream output;

    public XmlClientHandler(Socket socket, Server server) throws IOException {
        super(socket, server);
        this.output = socket.getOutputStream();
    }

    @Override
    public void run() {
        try {
            InputStream input = socket.getInputStream();

            while (running) {
                byte[] lengthBytes = new byte[4]; // длина сообщения
                input.read(lengthBytes);
                int length = ((lengthBytes[0] & 0xFF) << 24) |
                        ((lengthBytes[1] & 0xFF) << 16) |
                        ((lengthBytes[2] & 0xFF) << 8) |
                        (lengthBytes[3] & 0xFF);

                byte[] messageBytes = new byte[length];
                input.read(messageBytes);
                String xmlMessage = new String(messageBytes, StandardCharsets.UTF_8); // кодируем строку в UTF_8

                processXmlCommand(xmlMessage, output);
            }
        }
        catch (IOException e) {
            throw new RuntimeException("Client disconnected: " + e.getMessage());
        }
        finally {
            handleLogout();
            server.getClients().remove(this);
        }
    }

    private void processXmlCommand(String xmlMessage, OutputStream output) throws IOException {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance(); // создание экземпляра фабрики
            DocumentBuilder builder = factory.newDocumentBuilder(); // инициализация строителя
            Document doc = builder.parse(new ByteArrayInputStream(xmlMessage.getBytes())); // делаем из строки Document

            Element commandElement = doc.getDocumentElement();
            String commandName = commandElement.getAttribute("name");

            switch (commandName) {
                case "login":
                    handleLogin(commandElement, output);
                    break;
                case "list":
                    handleList(commandElement, output);
                    break;
                case "message":
                    handleMessage(commandElement, output);
                    break;
                case "logout":
                    handleLogout();
                    sendXmlResponse(output, "<success/>");
                    running = false;
                    break;
                default:
                    sendXmlResponse(output, "<error><message>Unknown command</message></error>");
            }
        }
        catch (Exception e) {
            sendXmlResponse(output, "<error><message>" + e.getMessage() + "</message></error>");
        }
    }

    private void handleLogin(Element commandElement, OutputStream output) throws IOException {
        NodeList nameNodes = commandElement.getElementsByTagName("name");
        NodeList typeNodes = commandElement.getElementsByTagName("type");

        if (nameNodes.getLength() == 0 || typeNodes.getLength() == 0) {
            sendXmlResponse(output, "<error><message>Name and type are required</message></error>");
            return;
        }

        String name = nameNodes.item(0).getTextContent();
        String type = typeNodes.item(0).getTextContent();

        UserInfo userInfo = new UserInfo(name, type);
        UUID sessionId = server.getSessionManager().createSession(userInfo);

        if (sessionId == null) {
            sendXmlResponse(output, "<error><message>User already exists</message></error>");
            return;
        }

        this.userInfo = userInfo;
        sendXmlResponse(output, "<success><session>" + sessionId + "</session></success>");

        String event = String.format("<event name=\"userlogin\"><name>%s</name></event>", name);
        server.broadcast(new XmlResponse(event), null);
    }

    private void handleList(Element commandElement, OutputStream output) throws IOException {
        NodeList sessionNodes = commandElement.getElementsByTagName("session");
        if (sessionNodes.getLength() == 0) {
            sendXmlResponse(output, "<error><message>Session required</message></error>");
            return;
        }

        UUID sessionId = UUID.fromString(sessionNodes.item(0).getTextContent());
        UserInfo currentUser = server.getSessionManager().getUserInfo(sessionId);
        if (currentUser == null) {
            sendXmlResponse(output, "<error><message>Invalid session</message></error>");
            return;
        }

        UserInfo[] users = server.getSessionManager().getAllUsers();

        StringBuilder usersXml = new StringBuilder("<success><listusers>");
        for (UserInfo user : users) {
            usersXml.append("<user><name>").append(user.getName()).append("</name><type>").append(user.getType()).append("</type></user>");
        }
        usersXml.append("</listusers></success>");

        sendXmlResponse(output, usersXml.toString());
    }

    private void handleMessage(Element commandElement, OutputStream output) throws IOException {
        NodeList sessionNodes = commandElement.getElementsByTagName("session");
        NodeList messageNodes = commandElement.getElementsByTagName("message");

        if (sessionNodes.getLength() == 0 || messageNodes.getLength() == 0) {
            sendXmlResponse(output, "<error><message>Session and message are required</message></error>");
            return;
        }

        UUID sessionId = UUID.fromString(sessionNodes.item(0).getTextContent());
        UserInfo sender = server.getSessionManager().getUserInfo(sessionId);
        if (sender == null) {
            sendXmlResponse(output, "<error><message>Invalid session</message></error>");
            return;
        }

        String message = messageNodes.item(0).getTextContent();

        String event = String.format("<event name=\"message\">" + "<message>%s</message>" + "<name>%s</name>" + "</event>", escapeXml(message), escapeXml(sender.getName()));

        server.broadcast(new XmlResponse(event), this);
        sendXmlResponse(output, "<success/>");
    }

    private String escapeXml(String input) {
        return input.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&apos;");
    }

    private void handleLogout() {
        if (userInfo != null) {
            server.getSessionManager().removeSession(userInfo.getName());
            String event = String.format("<event name=\"userlogout\"><name>%s</name></event>", userInfo.getName());
            server.broadcast(new XmlResponse(event), this);
        }
    }

    private void sendXmlResponse(OutputStream output, String xml) throws IOException {
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

    @Override
    public void sendResponse(ChatResponse response) {
        try {
            String xml;
            if (response instanceof XmlResponse) {
                xml = ((XmlResponse) response).getXml();
            }
            else {
                xml = "<error><message>Unsupported response type</message></error>";
            }
            sendXmlResponse(output, xml);
        }
        catch (IOException e) {
            throw new RuntimeException("Error sending response: " + e.getMessage());
        }
    }

    @Override
    public void stop() throws IOException {
        running = false;
        if (output != null) {
            output.close();
        }
        socket.close();
    }
}