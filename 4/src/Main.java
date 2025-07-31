import client.*;
import server.*;
import javax.swing.*;
import java.io.IOException;

public class Main {
    public static void main(String[] args) throws ClassNotFoundException {
        Object[] options = {"Server", "Client"};
        int choice = JOptionPane.showOptionDialog(null, "Run as server or client?", "Chat Application", JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);

        if (choice == 0) {
            runServer();
        }
        else {
            runClient();
        }
    }

    private static void runServer() {
        String portStr = JOptionPane.showInputDialog("Enter server port:", "12345");
        String protocol = JOptionPane.showInputDialog("Enter protocol (xml/serial):", "xml");

        try {
            int port = Integer.parseInt(portStr);
            Server server = null;

            if ("xml".equalsIgnoreCase(protocol)) {
                server = new Server(port, ProtocolType.XML);
            }
            else if ("serial".equalsIgnoreCase(protocol)) {
                server = new Server(port, ProtocolType.SERIALIZED);
            }
            else {
                JOptionPane.showMessageDialog(null, "Invalid protocol", "Error", JOptionPane.ERROR_MESSAGE);
                System.exit(1);
            }

            server.start();
        }
        catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(null, "Invalid port number", "Error", JOptionPane.ERROR_MESSAGE);
        }
        catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Server error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private static void runClient() throws ClassNotFoundException {
        String serverAddress = JOptionPane.showInputDialog("Enter server address:", "localhost");
        String portStr = JOptionPane.showInputDialog("Enter server port:", "12345");
        String name = JOptionPane.showInputDialog("Enter your name:");
        String type = JOptionPane.showInputDialog("Enter your type:", "CHAT_CLIENT");
        String protocol = JOptionPane.showInputDialog("Enter protocol (xml/serial):", "xml");

        try {
            int port = Integer.parseInt(portStr);
            ClientBase client = null;

            if ("xml".equalsIgnoreCase(protocol)) {
                client = new XmlChatClient(serverAddress, port, name, type);
            }
            else if ("serial".equalsIgnoreCase(protocol)) {
                client = new SerializedChatClient(serverAddress, port, name, type);
            }
            else {
                JOptionPane.showMessageDialog(null, "Invalid protocol", "Error", JOptionPane.ERROR_MESSAGE);
                System.exit(1);
            }

            client.connect();
            ChatWindow window = new ChatWindow(client);
            window.setVisible(true);
        }
        catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(null, "Invalid port number", "Error", JOptionPane.ERROR_MESSAGE);
        }
        catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Connection error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}