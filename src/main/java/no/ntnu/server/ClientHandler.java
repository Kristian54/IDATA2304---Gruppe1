package no.ntnu.server;

import java.io.*;
import java.net.Socket;
import java.util.List;

/**
 * Handles communication with a client over a TCP socket.
 */
public class ClientHandler extends Thread {
  private final Socket clientSocket;
  private final TcpCommunicationChannel channel;
  private final BufferedReader socketReader;
  private final PrintWriter socketWriter;
  private boolean running;

  /**
   * Creates a new client handler and set up the input and output streams.
   *
   * @param socket Socket associated with this client
   * @param channel Reference to the main TCP server class
   * @throws IOException Upon failure to establish input or output streams
   */
  public ClientHandler(Socket socket, TcpCommunicationChannel channel) throws IOException {
    if (socket == null || channel == null) {
      throw new IllegalArgumentException("Socket and channel cannot be null");
    }
    this.clientSocket = socket;
    this.channel = channel;
    socketReader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
    socketWriter = new PrintWriter(clientSocket.getOutputStream(), true);
  }

  /**
   * Run the client handling logic.
   */
  @Override
  public void run() {
    this.running = true;

    while (this.running) {
      recieveCommand();
    }
  }

  private void recieveCommand() {
    try {
      String command = socketReader.readLine();
      if (command != null) {
        handleInput(command);
      }
    } catch (IOException e) {
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
      case "updateSensorData":
        channel.advertiseSensorData(inputParts.get(1));
        break;
      case "nodeAdded":
        channel.spawnNode(inputParts.get(1));
      default:
        System.out.println("Unknown command: " + inputParts.get(0));
    }
  }

  /**
   * Send a response from the server to the client by using the TCP socket.
   *
   * @param message The message to sent to the client
   */
  private void sendToClient(String message) {
    if (socketWriter != null) {
      socketWriter.println(message);
    }
  }
}
