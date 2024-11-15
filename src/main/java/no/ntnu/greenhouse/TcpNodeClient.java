package no.ntnu.greenhouse;
import java.io.*;
import java.net.*;

/**
 * A TCP client for a node to connect a sensor/actuator.
 */
public class TcpNodeClient {

  private boolean running;
  private Socket socket;
  private PrintWriter writer;
  private BufferedReader reader;
  private String ip;
  private int port;

  /**
   * Creates a new TCP client for a node to connect to a server.
   *
   * @param ipAddress The IP address of the server
   * @param port The port number of the server
   * @param node The sensor/actuator node to connect
   */
  public TcpNodeClient(String ipAddress, int port, SensorActuatorNode node) {
    if (node == null) throw new RuntimeException("Node cannot be null");
    if (ipAddress == null) throw new RuntimeException("IP Address cannot be null");
    if (port < 0 || port > 65535) throw new RuntimeException("Port number must be within 5 digits and not negative");
    this.ip = ipAddress;
    this.port = port;
  }

  /**
   * Starts the TCP client and connects to the server.
   */
  public void run() {
    try {
      startConnection();
      this.running = true;
    } catch (IOException e) {
      System.out.println("Error connecting to server");
    }

    while (running) {
    }

    stopConnection();
  }

  /**
   * Starts the conections to the server.
   *
   * @throws IOException Upon failure to connect to the server
   */
  private void startConnection() throws IOException {
      this.socket = new Socket(this.ip, this.port);
      this.writer = new PrintWriter(this.socket.getOutputStream(), true);
      this.reader = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));

      sendCommand("nodeAdded-4;3_window");
      sendCommand("nodeAdded-1");
      sendCommand("updateSensorData-1;temperature=27.4 °C,temperature=26.8 °C,humidity=80 %");
    try {
      Thread.sleep(5000);
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }
    sendCommand("updateSensorData-1;temperature=23.4 °C,temperature=36.8 °C,humidity=20 %");
  }

  /**
   * Stops the connection to the server.
   */
  private void stopConnection () {
    try {
      if (this.writer != null) writer.close();
      if (this.reader != null) reader.close();
      if (socket != null && !socket.isClosed()) {
        socket.close();
      }

      System.out.println("Connection closed");
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Stops the client.
   */
  public void stop() {
    this.running = false;
  }

  /**
   * Send a command to the server.
   *
   * @param command The command to send to the server
   * @return {@code true} if the command was sent, {@code false} otherwise
   */
  public boolean sendCommand(String command) {
    boolean sent = false;
    if (writer != null && reader != null) {
      try {
        writer.println(command);
        sent = true;
      } catch (Exception e) {
        System.out.println("Error sending command: " + e.getMessage());
      }
    }
    return sent;
  }
}
