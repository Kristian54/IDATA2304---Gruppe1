package no.ntnu.server;

<<<<<<< HEAD:src/main/java/no/ntnu/server/TCPServer.java
import java.nio.file.Files;
import no.ntnu.greenhouse.TcpSensorActuatorNodeClient;

import java.io.*;
import java.net.*;
=======
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
>>>>>>> dev:src/main/java/no/ntnu/server/TcpServer.java
import java.util.ArrayList;

/** A TCP server for clients to connect to. */
public class TcpServer {
  public static final int PORT_NUMBER = 10020;
  private ServerSocket serverSocket;
  private boolean running = false;
  private static TcpServer instance;
  private ArrayList<ClientHandler> clientsHandlers = new ArrayList<>();

<<<<<<< HEAD:src/main/java/no/ntnu/server/TCPServer.java
  /**
   * Creates an instance of a TCP server.
   */
  private TCPServer() {
  }
=======
  /** Creates an instance of a TCP server. */
  private TcpServer() {}
>>>>>>> dev:src/main/java/no/ntnu/server/TcpServer.java

  /**
   * Controls that only one instance of the TCP server is created.
   *
   * @return the TCP server instance
   */
<<<<<<< HEAD:src/main/java/no/ntnu/server/TCPServer.java
  public static TCPServer getInstance() {
    if (instance == null) {
      instance = new TCPServer();
=======
  public static TcpServer getInstance() {
    if (instance == null) {
      instance = new TcpServer();
>>>>>>> dev:src/main/java/no/ntnu/server/TcpServer.java
    }
    return instance;
  }

  /**
   * Starts the server on the specified port.
   *
   * @param port The port to start the server on
   */
  public void startServer(int port) {
    try {
      serverSocket = new ServerSocket(port);
      running = true;
      System.out.println("Server started on port " + port + ".");

      while (running) {
        acceptNewClient();
      }

    } catch (IOException e) {
      throw new RuntimeException("Cannot open port", e);
    } finally {
      stopServer();
    }
  }

  private void acceptNewClient() {
    Socket clientSocket = null;
    try {
      clientSocket = serverSocket.accept();
      if (clientSocket != null) {
        System.out.println("Client connected: " + clientSocket.getInetAddress().getHostAddress());
        ClientHandler clientHandler = new ClientHandler(clientSocket, this);
        Thread clientProcessor = new Thread(clientHandler::run);
        clientProcessor.start();
        clientsHandlers.add(clientHandler);
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Sends a message to all control panel clients.
   *
   * @param message The message to send
   */
  public void sendMessageToControlPanels(String message) {
    for (ClientHandler clientHandler : clientsHandlers) {
      if (clientHandler.getNodeType().equals(NodeType.CONTROLPANEL)) {
        clientHandler.sendToClient(message);
      }
    }
  }

  /**
   * Sends a message to a single control panel client.
   *
   * @param message The message to send
   */
  public void sendMessageToSensorActuatorNodes(String message) {
    for (ClientHandler clientHandler : clientsHandlers) {
      if (clientHandler.getNodeType().equals(NodeType.SENSORACTUATOR)) {
        clientHandler.sendToClient(message);
      }
    }
  }

  /**
   * Sends a message to a single sensor/actuator node.
   *
   * @param message The message to send
   * @param id      The id of the node to send the message to
   */
  public void sendMessageToSensorActuatorNode(String message, int id) {
    for (ClientHandler clientHandler : clientsHandlers) {
<<<<<<< HEAD:src/main/java/no/ntnu/server/TCPServer.java
      if (clientHandler.getNodeType().equals(NodeType.SENSORACTUATOR) &&
          clientHandler.getHandlerId() == id) {
=======
      if (clientHandler.getNodeType().equals(NodeType.SENSORACTUATOR)
          && clientHandler.getHandlerId() == id) {
>>>>>>> dev:src/main/java/no/ntnu/server/TcpServer.java
        clientHandler.sendToClient(message);
      }
    }
  }

  /** Stops the server. */
  public void stopServer() {
    running = false;
    try {
      if (serverSocket != null && !serverSocket.isClosed()) {
        serverSocket.close();
        System.out.println("Server stopped.");
      }
    } catch (IOException e) {
      throw new RuntimeException("Error closing server", e);
    }
  }


}
