package no.ntnu.greenhouse;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.file.Files;
import java.sql.SQLOutput;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import no.ntnu.listeners.common.ActuatorListener;
import no.ntnu.listeners.greenhouse.NodeStateListener;
import no.ntnu.listeners.greenhouse.SensorListener;

/**
 * A TCP client for a node to connect a sensor/actuator.
 */
public class TcpSensorActuatorNodeClient
    implements SensorListener, ActuatorListener, NodeStateListener {

  private boolean running;
  private Socket socket;
  private PrintWriter writer;
  private BufferedReader reader;
  private String ip;
  private int port;
  private final SensorActuatorNode node;
  private boolean stopped = false;

  /**
   * Creates a new TCP client for a node to connect to a server.
   *
   * @param ipAddress The IP address of the server
   * @param port      The port number of the server
   * @param node      The sensor/actuator node to connect
   */
  public TcpSensorActuatorNodeClient(String ipAddress, int port, SensorActuatorNode node) {
    if (node == null) {
      throw new RuntimeException("Node cannot be null");
    }
    if (ipAddress == null) {
      throw new RuntimeException("IP Address cannot be null");
    }
    if (port < 0 || port > 65535) {
      throw new RuntimeException("Port number must be within 5 digits and not negative");
    }
    this.node = node;
    this.ip = ipAddress;
    this.port = port;
    node.addSensorListener(this);
    node.addActuatorListener(this);
    node.addStateListener(this);
  }

  /**
   * Starts the TCP client and connects to the server.
   */
  public void run() {
    startConnection();
    running = true;
    while (running) {
      receiveCommand();
    }
  }

  /**
   * Starts the connection to the server or reconnects.
   */
  private void startConnection() {
    boolean connected = false;
    while (!connected && !stopped) {
      try {
        if (socket != null) {
          socket.close();
          System.out.println("Attempting reconnect");
        }
        Thread.sleep(2000);
        this.socket = new Socket(this.ip, this.port);
        this.writer = new PrintWriter(this.socket.getOutputStream(), true);
        this.reader = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
        connected = true;
        System.out.println(node.getId() + "connected to server");
        sendId();
        sendNodeType();
        sendNodeActuatorData();
      } catch (IOException e) {
        System.out.println("Error connecting to server");
      } catch (InterruptedException e) {
        System.out.println("Error sleeping thread");
      }
    }
  }

  /**
   * Receives a command from the server.
   */
  private void receiveCommand() {
    try {
      String command = reader.readLine();
      if (command != null) {
        handleInput(command);
      }
    } catch (IOException e) {
      System.out.println("Error reading command: " + e.getMessage());
      if (running) {
        startConnection();
      }
    } catch (NullPointerException e) {
      System.out.println("Reader is null");
    }
  }

  /**
   * Handles the input from the server.
   *
   * @param command The command to handle
   */
  private void handleInput(String command) {
    System.out.println("Received: " + command);
    String[] parts = command.split("-");
    switch (parts[0]) {
      case "controlPanelUpdateActuator":
        updateNode(parts[1]);
        break;
      case "controlPanelAdded":
        sendNodeActuatorData();
        break;
      default:
        throw new RuntimeException("Unknown command: " + command);
    }
  }

  /**
   * Updates the node based on the command.
   *
   * @param command The command to update the node with
   */
  private void updateNode(String command) {
    String[] parts = command.split(";");
    String[] actuatorParts = parts[1].split("=");
    int actuatorId = Integer.parseInt(actuatorParts[0]);
    boolean isOn = Boolean.parseBoolean(actuatorParts[1]);
    node.setActuator(actuatorId, isOn);
  }

  /**
   * Sends the ID of the node to the server.
   */
  private void sendId() {
    sendCommand("setId-" + node.getId());
  }

  /**
   * Sends the type of the node to the server.
   */
  private void sendNodeType() {
    sendCommand("setNodeType-" + "SensorActuator");
  }

  /**
   * Sends the actuator data of the node to the server.
   */
  private void sendNodeActuatorData() {
    StringBuilder sb = new StringBuilder();
    sb.append("nodeAdded-");
    sb.append(node.getId());
    sb.append(";");
    Map<Integer, String> actuatorCounts = mapActuators(node.getActuators());

    actuatorCounts.forEach(
        (type, count) -> {
          sb.append(count);
          sb.append("_");
          sb.append(type);
          sb.append(" ");
        });

    sendCommand(sb.toString());
  }

  /**
   * Sends the updated sensor data to the server.
   */
  private void sendUpdatedSensorData() {
    StringBuilder builder = new StringBuilder();
    builder.append("updateSensorData-");
    builder.append(node.getId());
    builder.append(";");
    List<Sensor> sensors = node.getSensors();
    for (Sensor sensor : sensors) {
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

  /**
   * Maps the actuators to their respective counts.
   *
   * @param actuators The actuators to map
   * @return A map of the actuators and their counts
   */
  private static Map<Integer, String> mapActuators(ActuatorCollection actuators) {
    Map<Integer, String> actuatorCounts = new HashMap<>();

    for (Actuator actuator : actuators) {
      actuatorCounts.put(actuator.getId(), actuator.getType());
    }

    return actuatorCounts;
  }

  /**
   * Stops the client.
   */
  public void stop() {
    this.running = false;
    this.stopped = true;
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

  /**
   * Sends the updated actuator data to the server.
   */
  @Override
  public void actuatorUpdated(int nodeId, Actuator actuator) {
    sendActuatorUpdate(actuator);
  }

  /**
   * Sends the updated actuator data to the server.
   *
   * @param actuator The actuator to send the update for
   */
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

  public void sendImageToServer(String path) {
    String base64 = convertImageToBase64(path);

    StringBuilder builder = new StringBuilder();
    builder.append("sendCameraImage-");
    builder.append(node.getId());
    builder.append(";");
    builder.append(base64);
    sendCommand(builder.toString());
  }

  public String convertImageToBase64(String imagePath) {
    try {
      byte[] imageBytes = Files.readAllBytes(new File(imagePath).toPath());
      return Base64.getEncoder().encodeToString(imageBytes);
    } catch (IOException e) {
      throw new RuntimeException("Error reading image file", e);
    }
  }

  @Override
  public void onNodeReady(SensorActuatorNode node) {
    sendNodeActuatorData();
  }

  @Override
  public void onNodeStopped(SensorActuatorNode node) {
    sendNodeRemoved();
  }

  private void sendNodeRemoved() {
    sendCommand("nodeRemoved-" + node.getId());
  }
}
