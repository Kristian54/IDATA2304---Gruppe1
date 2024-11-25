package no.ntnu.gui.controlpanel;

import static no.ntnu.tools.Parser.parseDoubleOrError;
import static no.ntnu.tools.Parser.parseIntegerOrError;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.LinkedList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import no.ntnu.controlpanel.ControlPanelLogic;
import no.ntnu.controlpanel.SensorActuatorNodeInfo;
import no.ntnu.greenhouse.Actuator;
import no.ntnu.greenhouse.SensorReading;
import no.ntnu.listeners.controlpanel.GreenhouseEventListener;

/**
 * A TCP client for a control panel node.
 */
public class TcpControlpanelNodeClient implements GreenhouseEventListener {
  ControlPanelLogic logic;
  String ip;
  int port;
  Socket socket;
  PrintWriter writer;
  BufferedReader reader;
  boolean running;

  /**
   * Create a new TCP client for a control panel node.
   *
   * @param ip    The IP address of the server
   * @param port  The port number of the server
   * @param logic The logic of the control panel
   */
  public TcpControlpanelNodeClient(String ip, int port, ControlPanelLogic logic) {
    if (logic == null) throw new IllegalArgumentException("Logic cannot be null");
    if (ip == null) throw new IllegalArgumentException("IP Address cannot be null");
    if (port < 0 || port > 65535) throw new IllegalArgumentException("Port number must be within 5 digits and not negative");
    this.logic = logic;
    this.ip = ip;
    this.port = port;
    logic.addListener(this);
  }

  /**
   * Starts the TCP client and connects to the server.
   */
  public void run() {
    startConnection();
    running = true;
    sendCommand("setNodeType-ControlPanel");
    sendCommand("controlPanelAdded");

    while (running) {
      recieveCommand();
      try {
        Thread.sleep(0);
      } catch (InterruptedException e) {
        throw new RuntimeException(e);
      }
    }
  }

  /**
   * Starts the connection to the server.
   */
  private void startConnection() {
    try {
      socket = new Socket(ip, port);
      writer = new PrintWriter(socket.getOutputStream(), true);
      reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
    } catch (IOException e) {
      throw new RuntimeException("Error connecting to server", e);
    }
  }

  /**
   * Receives a command from the server.
   */
  private void recieveCommand() {
    try {
      String command = reader.readLine();
      if (command != null) {
        handleInput(command);
      }
    } catch (Exception e) {
      System.out.println("Error reading command: " + e.getMessage());
    }
  }

  /**
   * Handles the input from the server.
   *
   * @param inputLine The input to handle
   */
  private void handleInput(String inputLine) {
    System.out.println("Received: " + inputLine);
    List<String> inputParts = List.of(inputLine.split("-"));
    switch (inputParts.get(0)) {
      case "nodeAdded":
        spawnNode(inputParts.get(1));
        break;
      case "updateSensorData":
        advertiseSensorData(inputParts.get(1));
        break;
      case "actuatorUpdated":
        advertiseActuatorChange(inputParts.get(1));
        break;
      case "nodeRemoved":
        int nodeId = parseIntegerOrError(inputParts.get(1), "Invalid node ID: " + inputParts.get(1));
        logic.onNodeRemoved(nodeId);
        break;
      default:
        System.out.println("Unknown command: " + inputParts.get(0));
    }
  }

  /**
   * Advertise new sensor readings.
   *
   * @param specification Specification of the readings in the following format:
   *                      [nodeID]
   *                      semicolon
   *                      [sensor_type_1] equals [sensor_value_1] space [unit_1]
   *                      comma
   *                      ...
   *                      comma
   *                      [sensor_type_N] equals [sensor_value_N] space [unit_N]
   */
  public void advertiseSensorData(String specification) {
    if (specification == null || specification.isEmpty()) {
      throw new IllegalArgumentException("Sensor specification can't be empty");
    }
    String[] parts = specification.split(";");
    if (parts.length != 2) {
      throw new IllegalArgumentException("Incorrect specification format: " + specification);
    }
    int nodeId = parseIntegerOrError(parts[0], "Invalid node ID:" + parts[0]);
    List<SensorReading> sensors = parseSensors(parts[1]);
    Timer timer = new Timer();
    timer.schedule(new TimerTask() {
      @Override
      public void run() {
        logic.onSensorData(nodeId, sensors);
      }
    }, 1000L);
  }

  /**
   * Parse the sensor readings from the specification.
   *
   * @param sensorInfo The sensor information to parse
   * @return A list of sensor readings
   */
  private List<SensorReading> parseSensors(String sensorInfo) {
    List<SensorReading> readings = new LinkedList<>();
    String[] readingInfo = sensorInfo.split(",");
    for (String reading : readingInfo) {
      readings.add(parseReading(reading));
    }
    return readings;
  }

  /**
   * Parse a sensor reading from a string.
   *
   * @param reading The reading to parse
   * @return A sensor reading
   */
  private SensorReading parseReading(String reading) {
    String[] assignmentParts = reading.split("=");
    if (assignmentParts.length != 2) {
      throw new IllegalArgumentException("Invalid sensor reading specified: " + reading);
    }
    String[] valueParts = assignmentParts[1].split(" ");
    if (valueParts.length != 2) {
      throw new IllegalArgumentException("Invalid sensor value/unit: " + reading);
    }
    String sensorType = assignmentParts[0];
    double value = parseDoubleOrError(valueParts[0], "Invalid sensor value: " + valueParts[0]);
    String unit = valueParts[1];
    return new SensorReading(sensorType, value, unit);
  }

  /**
   * Spawn a new sensor/actuator node information after a given delay.
   *
   * @param specification A (temporary) manual configuration of the node in the following format
   *                      [nodeId] semicolon
   *                      [actuator_count_1] underscore [actuator_type_1] space ... space
   *                      [actuator_count_M] underscore [actuator_type_M]
   */
  public void spawnNode(String specification) {
    SensorActuatorNodeInfo nodeInfo = createSensorNodeInfoFrom(specification);
    Timer timer = new Timer();
    timer.schedule(new TimerTask() {
      @Override
      public void run() {
        System.out.println("Spawning node " + specification);
        logic.onNodeAdded(nodeInfo);
      }
    },1000L);
  }

  /**
   * Create a sensor/actuator node information from a specification.
   *
   * @param specification The specification of the node
   * @return A sensor/actuator node information
   */
  private SensorActuatorNodeInfo createSensorNodeInfoFrom(String specification) {
    if (specification == null || specification.isEmpty()) {
      throw new IllegalArgumentException("Node specification can't be empty");
    }
    String[] parts = specification.split(";");
    if (parts.length > 3) {
      throw new IllegalArgumentException("Incorrect specification format");
    }
    int nodeId = parseIntegerOrError(parts[0], "Invalid node ID:" + parts[0]);
    SensorActuatorNodeInfo info = new SensorActuatorNodeInfo(nodeId);
    if (parts.length == 2) {
      parseActuators(parts[1], info);
    }
    return info;
  }

  /**
   * Parse the actuators from the specification.
   *
   * @param actuatorSpecification The actuator specification to parse
   * @param info                  The sensor/actuator node information to update
   */
  private void parseActuators(String actuatorSpecification, SensorActuatorNodeInfo info) {
    String[] parts = actuatorSpecification.split(" ");
    for (String part : parts) {
      parseActuatorInfo(part, info);
    }
  }

  /**
   * Parse the actuator information from a string.
   *
   * @param s    The actuator information to parse
   * @param info The sensor/actuator node information to update
   */
  private void parseActuatorInfo(String s, SensorActuatorNodeInfo info) {
    String[] actuatorInfo = s.split("_");
    if (actuatorInfo.length != 2) {
      throw new IllegalArgumentException("Invalid actuator info format: " + s);
    }
    int actuatorId = parseIntegerOrError(actuatorInfo[1],
        "Invalid actuator count: " + actuatorInfo[1]);
    String actuatorType = actuatorInfo[0];
    Actuator actuator = new Actuator(actuatorId, actuatorType, info.getId());
    actuator.setListener(logic);
    info.addActuator(actuator);
  }

 /**
   * Advertise an actuator change.
   *
   * @param s The actuator change specification in the following format:
   *          [nodeId] semicolon [actuatorId] equals [isOn]
   */
  private void advertiseActuatorChange(String s) {
    String[] parts = s.split(";");
    if (parts.length != 2) {
      throw new IllegalArgumentException("Invalid actuator change specification: " + s);
    }
    int nodeId = parseIntegerOrError(parts[0], "Invalid node ID: " + parts[0]);
    String[] actuatorInfo = parts[1].split("=");
    if (actuatorInfo.length != 2) {
      throw new IllegalArgumentException("Invalid actuator info: " + parts[1]);
    }
    int actuatorId = parseIntegerOrError(actuatorInfo[0], "Invalid actuator ID: " + actuatorInfo[0]);
    boolean isOn = Boolean.parseBoolean(actuatorInfo[1]);

    sendActuatorChange(nodeId, actuatorId, isOn);
  }
  private void sendActuatorChange(int nodeId, int actuatorId, boolean isOn) {
    logic.onActuatorStateChanged(nodeId, actuatorId, isOn);
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
   * Called when a node has been added.
   *
   * @param nodeInfo Information about the added node
   */
  @Override
  public void onNodeAdded(SensorActuatorNodeInfo nodeInfo) {
  }

  /**
   * Called when a node has been removed.
   *
   * @param nodeId The ID of the removed node
   */
  @Override
  public void onNodeRemoved(int nodeId) {
  }

  /**
   * Called when sensor data has been received.
   *
   * @param nodeId  The ID of the node that sent the data
   * @param sensors The sensor readings
   */
  @Override
  public void onSensorData(int nodeId, List<SensorReading> sensors) {

  }

  /**
   * Called when an actuator has changed its state.
   *
   * @param nodeId   The ID of the node on which the actuator is placed
   * @param actuatorId The ID of the actuator that has changed its state
   * @param isOn     The new state of the actuator
   */
  @Override
  public void onActuatorStateChanged(int nodeId, int actuatorId, boolean isOn) {
    StringBuilder sb = new StringBuilder();
    sb.append("controlPanelUpdateActuator-");
    sb.append(nodeId);
    sb.append(";");
    sb.append(actuatorId);
    sb.append("=");
    sb.append(isOn);
    sendCommand(sb.toString());
  }
}
