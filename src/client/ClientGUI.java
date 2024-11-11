package client;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.table.DefaultTableModel;

public class ClientGUI extends JFrame {
    private JTextField messageField;
    private JTextArea chatArea;
    private JTextField nameField;
    private ChatClient client;
    private JTable userTable;
    private DefaultTableModel userTableModel;
    private JComboBox<String> messageTypeCombo;

    public ClientGUI() {
        super("Chat Application");
        
        // Create components
        chatArea = new JTextArea(20, 40);
        chatArea.setEditable(false);
        chatArea.setLineWrap(true);
        chatArea.setWrapStyleWord(true);
        
        messageField = new JTextField(30);
        nameField = new JTextField(20);
        JButton connectButton = new JButton("Connect");
        
        // Create user table
        String[] columnNames = {"Online Users"};
        userTableModel = new DefaultTableModel(columnNames, 0);
        userTable = new JTable(userTableModel);
        userTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        // Message type combo box
        messageTypeCombo = new JComboBox<>(new String[]{"Broadcast", "Private Message"});
        
        // Layout
        setLayout(new BorderLayout());
        
        // Name panel
        JPanel namePanel = new JPanel();
        namePanel.add(new JLabel("Name: "));
        namePanel.add(nameField);
        namePanel.add(connectButton);
        
        // Chat panel with user list
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                new JScrollPane(userTable),
                new JScrollPane(chatArea));
        splitPane.setDividerLocation(150);
        
        // Message panel
        JPanel messagePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        messagePanel.add(messageTypeCombo);
        messagePanel.add(messageField);
        
        // Add components
        add(namePanel, BorderLayout.NORTH);
        add(splitPane, BorderLayout.CENTER);
        add(messagePanel, BorderLayout.SOUTH);
        
        // Event handlers
        connectButton.addActionListener(e -> {
            client = new ChatClient(nameField.getText(), this);
            client.connect();
            nameField.setEnabled(false);
            connectButton.setEnabled(false);
            messageField.requestFocus();
        });
        
        messageField.addActionListener(e -> {
            String message = messageField.getText();
            if (!message.trim().isEmpty()) {
                if (messageTypeCombo.getSelectedItem().equals("Private Message")) {
                    int selectedRow = userTable.getSelectedRow();
                    if (selectedRow != -1) {
                        String recipient = (String) userTable.getValueAt(selectedRow, 0);
                        client.sendPrivateMessage(recipient, message);
                        chatArea.append("(Private to " + recipient + "): " + message + "\n");
                    } else {
                        JOptionPane.showMessageDialog(this, "Please select a user from the list for private message.");
                        return;
                    }
                } else {
                    client.sendMessage(message);
                }
                messageField.setText("");
            }
        });
        
        // Window settings
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null);
    }

    public void appendMessage(String message) {
        chatArea.append(message + "\n");
        chatArea.setCaretPosition(chatArea.getDocument().getLength());
    }

    public void updateUserList(String[] users) {
        SwingUtilities.invokeLater(() -> {
            userTableModel.setRowCount(0);
            for (String user : users) {
                if (!user.equals(nameField.getText())) {
                    userTableModel.addRow(new Object[]{user});
                }
            }
        });
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new ClientGUI().setVisible(true);
        });
    }
}