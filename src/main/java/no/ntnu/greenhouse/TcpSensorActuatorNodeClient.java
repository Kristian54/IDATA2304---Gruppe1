package no.ntnu.greenhouse;
import java.io.*;
import java.net.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import no.ntnu.listeners.common.ActuatorListener;
import no.ntnu.listeners.greenhouse.NodeStateListener;
import no.ntnu.listeners.greenhouse.SensorListener;

/**
 * A TCP client for a node to connect a sensor/actuator.
 */
public class TcpSensorActuatorNodeClient implements SensorListener, ActuatorListener {

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
  public TcpSensorActuatorNodeClient(String ipAddress, int port, SensorActuatorNode node) {
    if (node == null) throw new RuntimeException("Node cannot be null");
    if (ipAddress == null) throw new RuntimeException("IP Address cannot be null");
    if (port < 0 || port > 65535) throw new RuntimeException("Port number must be within 5 digits and not negative");
    this.node = node;
    this.ip = ipAddress;
    this.port = port;
    node.addSensorListener(this);
    node.addActuatorListener(this);
  }

  /**
   * Starts the TCP client and connects to the server.
   */
  public void run() {
    startConnection();
    sendId();
    sendNodeType();
    sendNodeActuatorData();

    while (running) {
    }
  }

  private void sendId() {
    sendCommand("setId-" + node.getId());
  }

  private void sendNodeType() {
    sendCommand("setNodeType-" + "SensorActuator");
  }

  private void sendNodeActuatorData() {
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
  }

  private void sendUpdatedSensorData() {
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
  }

  private static Map<String, Integer> countActuators(ActuatorCollection actuators) {
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
  private void startConnection() {
    try {
      this.socket = new Socket(this.ip, this.port);
      this.writer = new PrintWriter(this.socket.getOutputStream(), true);
      this.reader = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
      this.running = true;
    } catch (IOException e) {
      System.out.println("Error connecting to server");
    }
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
  private boolean sendCommand(String command) {
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

  /**
   * Sends the updated sensor data to the server.
   */
  @Override
  public void sensorsUpdated(List<Sensor> sensors) {
    sendUpdatedSensorData();
  }


  @Override
  public void actuatorUpdated(int nodeId, Actuator actuator) {
    sendActuatorUpdate(actuator);
  }

  private void sendActuatorUpdate(Actuator actuator) {
    StringBuilder builder = new StringBuilder();
    builder.append("actuatorUpdated-");
    builder.append(node.getId());
    builder.append(";");
    builder.append(actuator.getId());
    builder.append("=");
    builder.append(actuator.isOn());
    sendCommand(builder.toString());
  }
}
