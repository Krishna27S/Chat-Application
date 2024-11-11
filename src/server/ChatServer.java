package server;

import java.io.*;
import java.net.*;
import java.util.*;

public class ChatServer {
    private static final int PORT = 9001;
    private static HashSet<PrintWriter> writers = new HashSet<>();
    private static HashMap<String, PrintWriter> userWriters = new HashMap<>();

    public static void main(String[] args) throws Exception {
        System.out.println("Chat Server is running...");
        ServerSocket listener = new ServerSocket(PORT);
        try {
            while (true) {
                new Handler(listener.accept()).start();
            }
        } finally {
            listener.close();
        }
    }

    private static void broadcastUserList() {
        String userList = "USERLIST " + String.join(",", userWriters.keySet());
        for (PrintWriter writer : writers) {
            writer.println(userList);
        }
    }

    private static class Handler extends Thread {
        private String name;
        private Socket socket;
        private BufferedReader in;
        private PrintWriter out;

        public Handler(Socket socket) {
            this.socket = socket;
        }

        public void run() {
            try {
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);

                while (true) {
                    out.println("SUBMITNAME");
                    name = in.readLine();
                    if (name == null) {
                        return;
                    }
                    synchronized (userWriters) {
                        if (!userWriters.containsKey(name)) {
                            userWriters.put(name, out);
                            break;
                        }
                    }
                }

                out.println("NAMEACCEPTED");
                writers.add(out);
                broadcastUserList();

                for (PrintWriter writer : writers) {
                    writer.println("MESSAGE " + name + " has joined");
                }

                while (true) {
                    String input = in.readLine();
                    if (input == null) {
                        return;
                    }
                    
                    if (input.startsWith("BROADCAST ")) {
                        String message = input.substring(10);
                        for (PrintWriter writer : writers) {
                            writer.println("MESSAGE " + name + ": " + message);
                        }
                    } else if (input.startsWith("PRIVATE ")) {
                        String[] parts = input.substring(8).split(" ", 2);
                        String recipient = parts[0];
                        String message = parts[1];
                        PrintWriter recipientWriter = userWriters.get(recipient);
                        if (recipientWriter != null) {
                            recipientWriter.println("PRIVATE " + name + ":" + message);
                        }
                    }
                }
            } catch (IOException e) {
                System.out.println(e);
            } finally {
                if (name != null) {
                    userWriters.remove(name);
                    for (PrintWriter writer : writers) {
                        writer.println("MESSAGE " + name + " has left");
                    }
                    broadcastUserList();
                }
                if (out != null) {
                    writers.remove(out);
                }
                try {
                    socket.close();
                } catch (IOException e) {
                }
            }
        }
    }
}