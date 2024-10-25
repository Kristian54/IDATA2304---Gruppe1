package no.ntnu.server;

import java.io.*;
import java.net.*;

public class TCPServer {
  private ServerSocket serverSocket;
  private boolean running = false;

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
      e.printStackTrace();
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
      e.printStackTrace();
    }
  }

  public static void main(String[] args) {
    TCPServer server = new TCPServer();
    server.startServer(8080);
  }

}
