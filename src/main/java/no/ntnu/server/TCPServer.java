package no.ntnu.server;

import no.ntnu.greenhouse.TcpSensorActuatorNodeClient;

import java.io.*;
import java.net.*;
import java.util.ArrayList;

/**
 * A TCP server for clients to connect to.
 */
public class TCPServer {
  public static final int PORT_NUMBER = 10020;
  private ServerSocket serverSocket;
  private boolean running = false;
  private static TCPServer instance;
  private ArrayList<ClientHandler> clientsHandlers = new ArrayList<>();

  /**
   * Creates an instance of a TCP server.
   *
   */
  private TCPServer() {
  }

  /**
   * Controls that only one instance of the TCP server is created.
   *
   * @return the TCP server instance
   */
  public static TCPServer getInstance() {
    if (instance == null)
      instance = new TCPServer();
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

      while(running) {
        acceptNewClient();
      }

    } catch (IOException e) {
      throw new RuntimeException("Cannot open port", e);
    }
    finally {
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
   * Stops the server.
   */
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
