package client;

import java.io.*;
import java.net.*;
import javax.swing.SwingUtilities;

public class ChatClient {
    private BufferedReader in;
    private PrintWriter out;
    private String name;
    private ClientGUI gui;
    private static final String SERVER_ADDRESS = "localhost";
    private static final int SERVER_PORT = 9001;

    public ChatClient(String name, ClientGUI gui) {
        this.name = name;
        this.gui = gui;
    }

    public void connect() {
        try {
            Socket socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            new Thread(() -> {
                try {
                    while (true) {
                        String line = in.readLine();
                        if (line.startsWith("SUBMITNAME")) {
                            out.println(name);
                        } else if (line.startsWith("NAMEACCEPTED")) {
                            SwingUtilities.invokeLater(() -> {
                                gui.appendMessage("Connected to server!");
                            });
                        } else if (line.startsWith("MESSAGE")) {
                            SwingUtilities.invokeLater(() -> {
                                gui.appendMessage(line.substring(8));
                            });
                        } else if (line.startsWith("USERLIST")) {
                            String[] users = line.substring(9).split(",");
                            gui.updateUserList(users);
                        } else if (line.startsWith("PRIVATE")) {
                            String[] parts = line.substring(8).split(":", 2);
                            SwingUtilities.invokeLater(() -> {
                                gui.appendMessage("(Private from " + parts[0] + "): " + parts[1]);
                            });
                        }
                    }
                } catch (IOException e) {
                    SwingUtilities.invokeLater(() -> {
                        gui.appendMessage("Lost connection to server!");
                    });
                }
            }).start();

        } catch (IOException e) {
            gui.appendMessage("Could not connect to server!");
        }
    }

    public void sendMessage(String message) {
        out.println("BROADCAST " + message);
    }

    public void sendPrivateMessage(String recipient, String message) {
        out.println("PRIVATE " + recipient + " " + message);
    }
}