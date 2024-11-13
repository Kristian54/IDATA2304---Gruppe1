package no.ntnu.server;

import java.io.*;
import java.net.*;

public class TCPServer {
  public static final int PORT_NUMBER = 10020;
  private ServerSocket serverSocket;
  private boolean running = false;
  private TcpCommunicationChannel communicationChannel;
  private static TCPServer instance;


  private TCPServer(TcpCommunicationChannel communicationChannel) {
    if (communicationChannel == null) throw new RuntimeException("Communication channel cannot be null");
    this.communicationChannel = communicationChannel;
  }

  /**
   * Controls that only one instance of the TCP server is created.
   *
   * @param communicationChannel the communication channel to use
   * @return the TCP server instance
   */
  public static TCPServer getInstance(TcpCommunicationChannel communicationChannel) {
    if (instance == null)
      instance = new TCPServer(communicationChannel);
    return instance;
  }

  public void startServer(int port) {
    try {
      serverSocket = new ServerSocket(port);
      running = true;
      System.out.println("Server started on port " + port + ".");

      while(running) {
        Socket clientSocket = serverSocket.accept();
        System.out.println("Client connected: " + clientSocket.getInetAddress().getHostAddress());

        Thread thread = new Thread(new ClientHandler(clientSocket, communicationChannel));
        thread.start();
      }

    } catch (IOException e) {
      throw new RuntimeException("Cannot open port", e);
    }
    finally {
      stopServer();
    }
  }

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
