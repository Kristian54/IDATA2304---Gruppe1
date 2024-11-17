package no.ntnu.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ClientHandler extends Thread {
    private final Socket clientSocket;
    private final TCPServer server;
    private NodeType nodeType;
    private int id;
    private boolean running = false;

    private final BufferedReader socketReader;
    private final PrintWriter socketWriter;

    public ClientHandler(Socket clientSocket, TCPServer server) throws IOException {
        if (clientSocket == null || server == null) {
            throw new IllegalArgumentException("Socket, server or node type cannot be null");
        }

        this.clientSocket = clientSocket;
        this.server = server;
        socketReader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        socketWriter = new PrintWriter(clientSocket.getOutputStream(), true);
    }

    @Override
    public void run() {
        this.running = true;
        System.out.println("Processing client on thread: " + Thread.currentThread().getName());

        while (this.running) {
            receiveCommand();
        }
    }

    private void receiveCommand() {
        try {
            String command = socketReader.readLine();
            if (command != null) {
                handleInput(command);
            }
        } catch (Exception e) {
            System.out.println("Error reading command: " + e.getMessage());
        }
    }

    private void handleInput(String command) {
        System.out.println("Received command: " + command);
        switch (command) {
            case "set":
        }
    }

    public NodeType getNodeType() {
        return nodeType;
    }

    public int getHandlerId() {
        return id;
    }

    public void stopHandler() {
        this.running = false;
    }
}
