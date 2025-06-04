package client;

import common.*;
import common.commands.ListUsersCommand;
import common.commands.SendMessageCommand;
import common.responses.*;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.util.Date;

public class ChatWindow extends JFrame implements MessageListener {
    private final ClientBase client;
    private JTextArea chatArea;
    private JTextField messageField;
    private DefaultListModel<String> userListModel;

    public ChatWindow(ClientBase client) {
        this.client = client;
        setTitle("Chat Client - " + client.getName());
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        createUI();

        if (client instanceof XmlChatClient) {
            ((XmlChatClient)client).setMessageListener(this);
        }

        startMessageListener();
    }

    @Override
    public void onMessageReceived(ChatResponse response) {
        processResponse(response);
    }

    private void createUI() {
        JPanel mainPanel = new JPanel(new BorderLayout());

        // поле для отображения сообщений
        chatArea = new JTextArea();
        chatArea.setEditable(false);
        JScrollPane chatScroll = new JScrollPane(chatArea);
        mainPanel.add(chatScroll, BorderLayout.CENTER);

        // поле для списка клиентов
        userListModel = new DefaultListModel<>();
        JList<String> userList = new JList<>(userListModel);
        JScrollPane userScroll = new JScrollPane(userList);
        userScroll.setPreferredSize(new Dimension(150, 0));
        mainPanel.add(userScroll, BorderLayout.EAST);

        // панель ввода сообщений
        JPanel inputPanel = new JPanel(new BorderLayout());
        messageField = new JTextField();
        messageField.addActionListener(_ -> sendMessage());
        inputPanel.add(messageField, BorderLayout.CENTER);

        // кнопка для отправки сообщений
        JButton sendButton = new JButton("Send");
        sendButton.addActionListener(_ -> sendMessage());
        inputPanel.add(sendButton, BorderLayout.EAST);

        // кнопка для обновления списка клиентов
        JButton refreshButton = new JButton("Refresh Users");
        refreshButton.addActionListener(_ -> refreshUsers());
        inputPanel.add(refreshButton, BorderLayout.WEST);

        mainPanel.add(inputPanel, BorderLayout.SOUTH);

        add(mainPanel);
    }

    private void sendMessage() {
        String message = messageField.getText();
        if (!message.isEmpty()) {
            try {
                client.sendCommand(new SendMessageCommand(message));
                chatArea.append(String.format("[%s] Вы: %s\n", new Date(), message));
                chatArea.setCaretPosition(chatArea.getDocument().getLength());
                messageField.setText("");
            }
            catch (IOException e) {
                JOptionPane.showMessageDialog(this, "Failed to send message: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void refreshUsers() {
        try {
            client.sendCommand(new ListUsersCommand());
        }
        catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Failed to refresh users: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void startMessageListener() {
        if (!(client instanceof XmlChatClient)) {
            new Thread(() -> {
                while (client.isConnected()) {
                    try {
                        ChatResponse response = client.receiveResponse();
                        if (response != null) {
                            processResponse(response);
                        }
                        Thread.sleep(50);
                    }
                    catch (IOException e) {
                        handleDisconnection(e);
                        break;
                    }
                    catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }).start();
        }
    }

    private void handleDisconnection(IOException e) {
        SwingUtilities.invokeLater(() -> {
            JOptionPane.showMessageDialog(this, "Connection error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            try {
                client.disconnect();
            }
            catch (IOException _) {
            }
            dispose();
        });
    }

    private void processResponse(ChatResponse response) {
        if (response instanceof MessageEvent msgEvent) {
            chatArea.append(String.format("[%s] %s: %s\n", new Date(), msgEvent.getSender(), msgEvent.getMessage()));
        }
        else if (response instanceof UserLoginEvent loginEvent) {
            userListModel.addElement(loginEvent.getUsername());
            chatArea.append(String.format("[%s] %s joined the chat\n", new Date(), loginEvent.getUsername()));
        }
        else if (response instanceof UserLogoutEvent logoutEvent) {
            userListModel.removeElement(logoutEvent.getUsername());
            chatArea.append(String.format("[%s] %s left the chat\n", new Date(), logoutEvent.getUsername()));
        }
        else if (response instanceof ListUsersResponse listResponse) {
            userListModel.clear();
            for (UserInfo user : listResponse.getUsers()) {
                userListModel.addElement(user.toString());
            }
        }
        else if (response instanceof ErrorResponse error) {
            chatArea.append(String.format("[%s] Error: %s\n", new Date(), error.getErrorMessage()));
        }
        chatArea.setCaretPosition(chatArea.getDocument().getLength());
    }
}