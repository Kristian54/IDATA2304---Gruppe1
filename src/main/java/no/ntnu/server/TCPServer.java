package no.ntnu.server;

import java.io.*;
import java.net.*;
import java.util.Objects;
import no.ntnu.controlpanel.TcpCommunicationChannel;

public class TCPServer {
  public static final int PORT_NUMBER = 10020;
  private ServerSocket serverSocket;
  private boolean running = false;
  private TcpCommunicationChannel communicationChannel;


  public TCPServer(TcpCommunicationChannel communicationChannel) {
    if (communicationChannel == null) throw new RuntimeException("Communication channel cannot be null");
    this.communicationChannel = communicationChannel;
  }

  public void startServer(int port) {
    try {
      serverSocket = new ServerSocket(port);
      running = true;
      System.out.println("Server started on port " + port + ".");

      while(running) {
        Socket clientSocket = serverSocket.accept();
        System.out.println("Client connected: " + clientSocket.getInetAddress().getHostAddress());

        ClientHandler clientHandler = new ClientHandler(clientSocket);
        new Thread(clientHandler).start();

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
