package no.ntnu.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.List;

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

    /**
     * Handle the input from the client (TCP socket).
     *
     * @param inputLine The input from the client
     */
    private void handleInput(String inputLine) {
        System.out.println("Received: " + inputLine);
        List<String> inputParts = List.of(inputLine.split("-"));
        switch (inputParts.get(0)) {
            case "setNodeType":
                setNodeType(inputParts.get(1));
                break;
            case "setId":
                setId(inputParts.get(1));
                break;
            case "updateSensorData":
                //TODO: Implement
                break;
            case "nodeAdded":
                //TODO: Implement
                break;
            default:
                System.out.println("Unknown command: " + inputParts.get(0));
        }
    }

    private void setNodeType(String nodeType) {
        if (nodeType.equals("SensorActuator")) {
            this.nodeType = NodeType.SENSORACTUATOR;
        } else if (nodeType.equals("ControlPanel")) {
            this.nodeType = NodeType.CONTROLPANEL;
        }
    }

    private void setId(String id) {
        try {
            int iD = Integer.parseInt(id);
            if (iD < 0) {
                //TODO: Send error response to client
            } else {
                this.id = iD;
                System.out.println("Node ID set to: " + this.id);
            }
        } catch (NumberFormatException e) {
            //TODO: Send error response to client
        }

    }

    /**
     * Send a response from the server to the client by using the TCP socket.
     *
     * @param message The message to sent to the client
     */
    public void sendToClient(String message) {
        if (socketWriter != null) {
            socketWriter.println(message);
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
