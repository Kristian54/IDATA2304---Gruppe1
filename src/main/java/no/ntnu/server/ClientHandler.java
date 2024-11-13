package no.ntnu.server;

import java.io.*;
import java.net.Socket;
import java.util.List;

public class ClientHandler extends Thread {
  private final Socket clientSocket;
  private final TcpCommunicationChannel channel;
  private final BufferedReader socketReader;
  private final PrintWriter socketWriter;

  public ClientHandler(Socket socket, TcpCommunicationChannel channel) throws IOException {
    if (socket == null || channel == null) {
      throw new IllegalArgumentException("Socket and channel cannot be null");
    }
    this.clientSocket = socket;
    this.channel = channel;
    socketReader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
    socketWriter = new PrintWriter(clientSocket.getOutputStream(), true);
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
        handleInput(message);
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


  private void handleInput(String inputLine) {
    List<String> inputParts = List.of(inputLine.split("-"));
    switch (inputParts.get(0)) {
      case "updateSensorData":
        channel.advertiseSensorData(inputParts.get(1));
        break;
      default:
        System.out.println("Unknown command: " + inputParts.get(0));
    }
  }

  /**
   * Send a response from the server to the client by using the TCP socket.
   *
   * @param message The message to sent to the client
   */
  public void senToClient(String message) {
    socketWriter.println(message);
  }
}
