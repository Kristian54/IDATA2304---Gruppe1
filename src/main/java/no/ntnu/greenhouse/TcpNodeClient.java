package no.ntnu.greenhouse;
import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

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
  private final SensorActuatorNode node;

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
    this.node = node;
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

    StringBuilder sb = new StringBuilder();
    sb.append("nodeAdded-");
    sb.append(node.getId());
    sb.append(";");
    Map<String, Integer> actuatorCounts = countActuators(node.getActuators());
    actuatorCounts.forEach((type, count) -> {
      sb.append(count);
      sb.append("_");
      sb.append(type);
      sb.append(" ");
    });

    sendCommand(sb.toString());


    while (running) {
      StringBuilder builder = new StringBuilder();
      builder.append("updateSensorData-");
      builder.append(node.getId());
      builder.append(";");
      List<Sensor> sensors = node.getSensors();
      for (Sensor sensor: sensors) {
        SensorReading reading = sensor.getReading();
        builder.append(reading.getType());
        builder.append("=");
        builder.append(reading.getValue());
        builder.append(" ");
        builder.append(reading.getUnit());
        builder.append(",");
      }
      sendCommand(builder.toString());
      try {
        Thread.sleep(200);
      } catch (InterruptedException e) {
        throw new RuntimeException(e);
      }
    }


    stopConnection();
  }


  public static Map<String, Integer> countActuators(ActuatorCollection actuators) {
    Map<String, Integer> actuatorCounts = new HashMap<>();

    for (Actuator actuator : actuators) {
      actuatorCounts.put(actuator.getType(), actuatorCounts.getOrDefault(actuator.getType(), 0) + 1);
    }

    return actuatorCounts;
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
