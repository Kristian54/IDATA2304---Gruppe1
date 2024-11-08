package no.ntnu.server;

import java.io.*;
import java.net.Socket;

public class ClientHandler extends Thread {
  private final Socket clientSocket;

  public ClientHandler(Socket socket) {
    this.clientSocket = socket;
  }

  @Override
  public void run() {
    try (BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
         PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)) {
      System.out.println("Processing client on " + Thread.currentThread().getName());

      //TODO: MAke threads exit cleanly
      String message;
      // Read messages from the client
      while ((message = in.readLine()) != null) {
        System.out.println("Received: " + message);
        out.println("Echo: " + message); // Echo back the message to the client

        // Optional: Add a command to allow the client to close the connection
        if ("bye".equalsIgnoreCase(message)) {
          System.out.println("Client requested to close the connection.");
          break; // Exit the loop to close the socket and end the thread
        }
      }
    } catch (IOException e) {
      System.out.println("Error handling client: " + e.getMessage());
    } finally {
      try {
        clientSocket.close(); // Ensure the socket is closed when done
        System.out.println("Client on " + Thread.currentThread().getName() + " disconnected");
      } catch (IOException e) {
        System.out.println("Error closing client socket: " + e.getMessage());
      }
    }
  }
}
