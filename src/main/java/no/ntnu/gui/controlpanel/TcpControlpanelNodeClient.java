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

public class TcpControlpanelNodeClient {
  ControlPanelLogic logic;
  String ip;
  int port;
  Socket socket;
  PrintWriter writer;
  BufferedReader reader;
  boolean running;

  public TcpControlpanelNodeClient(String ip, int port, ControlPanelLogic logic) {
    if (logic == null) throw new IllegalArgumentException("Logic cannot be null");
    if (ip == null) throw new IllegalArgumentException("IP Address cannot be null");
    if (port < 0 || port > 65535) throw new IllegalArgumentException("Port number must be within 5 digits and not negative");
    this.logic = logic;
    this.ip = ip;
    this.port = port;
  }

  public void run() {
    startConnection();
    running = true;
    sendCommand("setNodeType-ControlPanel");

    while (running) {
      recieveCommand();
      try {
        Thread.sleep(1000);
      } catch (InterruptedException e) {
        throw new RuntimeException(e);
      }
    }
  }

  private void startConnection() {
    try {
      socket = new Socket(ip, port);
      writer = new PrintWriter(socket.getOutputStream(), true);
      reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
    } catch (IOException e) {
      throw new RuntimeException("Error connecting to server", e);
    }
  }

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

  private List<SensorReading> parseSensors(String sensorInfo) {
    List<SensorReading> readings = new LinkedList<>();
    String[] readingInfo = sensorInfo.split(",");
    for (String reading : readingInfo) {
      readings.add(parseReading(reading));
    }
    return readings;
  }

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

  private void parseActuators(String actuatorSpecification, SensorActuatorNodeInfo info) {
    String[] parts = actuatorSpecification.split(" ");
    for (String part : parts) {
      parseActuatorInfo(part, info);
    }
  }

  private void parseActuatorInfo(String s, SensorActuatorNodeInfo info) {
    String[] actuatorInfo = s.split("_");
    if (actuatorInfo.length != 2) {
      throw new IllegalArgumentException("Invalid actuator info format: " + s);
    }
    int actuatorCount = parseIntegerOrError(actuatorInfo[0],
        "Invalid actuator count: " + actuatorInfo[0]);
    String actuatorType = actuatorInfo[1];
    for (int i = 0; i < actuatorCount; ++i) {
      Actuator actuator = new Actuator(actuatorType, info.getId());
      actuator.setListener(logic);
      info.addActuator(actuator);
    }
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
}
