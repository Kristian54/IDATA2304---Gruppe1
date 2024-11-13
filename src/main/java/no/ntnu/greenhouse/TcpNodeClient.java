package no.ntnu.greenhouse;
import java.io.*;
import java.net.*;

public class TcpNodeClient {

  private boolean running;
  private Socket socket;
  private PrintWriter writer;
  private BufferedReader reader;
  private String ip;
  private int port;

  public TcpNodeClient(String ipAddress, int port, SensorActuatorNode node) {
    if (node == null) throw new RuntimeException("Node cannot be null");
    if (ipAddress == null) throw new RuntimeException("IP Address cannot be null");
    if (port < 0 || port > 65535) throw new RuntimeException("Port number must be within 5 digits and not negative");
    this.ip = ipAddress;
    this.port = port;
  }
  
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

  private void startConnection() throws IOException {
      this.socket = new Socket(this.ip, this.port);
      this.writer = new PrintWriter(this.socket.getOutputStream(), true);
      this.reader = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));

      sendCommand("nodeAdded-4;3_window");
      sendCommand("updateSensorData-1;temperature=27.4 °C,temperature=26.8 °C,humidity=80 %");
  }
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
