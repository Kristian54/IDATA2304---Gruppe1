package no.ntnu.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.List;

/**
 * The class that handles the communication between the server and a client.
 */
public class ClientHandler extends Thread {
  private final Socket clientSocket;
  private final TCPServer server;
  private NodeType nodeType = NodeType.UNDEFINED;
  private int id;
  private boolean running = false;

  private final BufferedReader socketReader;
  private final PrintWriter socketWriter;

  /**
   * Create a new client handler.
   *
   * @param clientSocket The socket to the client
   * @param server       The server
   * @throws IOException If an I/O error occurs when creating the input or output stream, the socket is closed,
   *                     the socket is not connected, or the socket input has been shutdown using shutdownInput(),
   *                     or the socket output has been shutdown using shutdownOutput().
   */
  public ClientHandler(Socket clientSocket, TCPServer server) throws IOException {
    if (clientSocket == null || server == null) {
      throw new IllegalArgumentException("Socket, server or node type cannot be null");
    }

    this.clientSocket = clientSocket;
    this.server = server;
    socketReader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
    socketWriter = new PrintWriter(clientSocket.getOutputStream(), true);
  }

  /**
   * Run the client handler.
   */
  @Override
  public void run() {
    this.running = true;
    System.out.println("Processing client on thread: " + Thread.currentThread().getName());

    while (this.running) {
      if (clientSocket.isClosed()) {
        this.running = false;
      }
      receiveCommand();
    }
  }

  /**
   * Receive a command from the client (TCP socket).
   */
  private void receiveCommand() {
    try {
      String command = socketReader.readLine();
      if (command != null) {
        handleInput(command);
      }
    } catch (IOException e) {
      System.out.println("Error reading command: " + e.getMessage());
      this.running = false;
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
        server.sendMessageToControlPanels(inputLine);
        break;
      case "nodeAdded":
        server.sendMessageToControlPanels(inputLine);
        break;
      case "controlPanelAdded":
        server.sendMessageToSensorActuatorNodes(inputLine);
        break;
      case "actuatorUpdated":
        server.sendMessageToControlPanels(inputLine);
        break;
      case "controlPanelUpdateActuator":
        server.sendMessageToSensorActuatorNode(inputLine, extractNodeId(inputLine));
        break;
      case "nodeRemoved":
        server.sendMessageToControlPanels(inputLine);
        running = false;
        break;
      case "checkConnection":
        break;
      case "getCameraImage":
        break;
      default:
        System.out.println("Unknown command: " + inputParts.get(0));
    }
  }

  /**
   * Extract the node ID from the input string.
   *
   * @param input The input string
   * @return The node ID
   */
  private int extractNodeId(String input) {
    String[] parts = input.split("-");
    if (parts.length < 2) {
      throw new IllegalArgumentException("Invalid input");
    }
    int id;
    try {
      id = Integer.parseInt(parts[1].split(";")[0]);
    } catch (NumberFormatException e) {
      throw new IllegalArgumentException("Invalid input");
    }

    return id;
  }

  /**
   * Set the node type.
   *
   * @param nodeType The node type
   */
  private void setNodeType(String nodeType) {
    if (nodeType.equals("SensorActuator")) {
      this.nodeType = NodeType.SENSORACTUATOR;
    } else if (nodeType.equals("ControlPanel")) {
      this.nodeType = NodeType.CONTROLPANEL;
    }
  }

  /**
   * Set the ID of the node.
   *
   * @param id The ID of the node
   */
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
      System.out.println("Sent: " + message);
    }
  }


  public void sendCameraImage() {
    //String cameraImageBase64 = node.getCameraImageBase64();
    //sendToClient("cameraImage-" + cameraImageBase64);
  }

  /**
   * Returns the node type.
   *
   * @return The node type
   */
  public NodeType getNodeType() {
    return nodeType;
  }

  /**
   * Returns the ID of the node.
   *
   * @return The ID of the node
   */
  public int getHandlerId() {
    return id;
  }

  /**
   * Stop the handler.
   */
  public void stopHandler() {
    this.running = false;
  }
}
