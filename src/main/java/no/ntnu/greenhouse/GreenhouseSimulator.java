package no.ntnu.greenhouse;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import no.ntnu.controlpanel.SensorActuatorNodeInfo;
import no.ntnu.listeners.controlpanel.GreenhouseEventListener;
import no.ntnu.listeners.greenhouse.NodeStateListener;
import no.ntnu.listeners.greenhouse.SensorListener;
import no.ntnu.tools.Logger;

/**
 * Application entrypoint - a simulator for a greenhouse.
 */
public class GreenhouseSimulator {

  /**
   * The nodes in the greenhouse.
   */
  private final Map<Integer, SensorActuatorNode> nodes = new HashMap<>();

  /**
   * The clients for the nodes in the greenhouse.
   */
  private final Map<Integer, TcpSensorActuatorNodeClient> nodeClients = new HashMap<>();

  /**
   * The listeners for the sensors in the greenhouse.
   */
  private final List<PeriodicSwitch> periodicSwitches = new LinkedList<>();

  /**
   * Create a greenhouse simulator.
   */
  public GreenhouseSimulator() {
  }

  /**
   * Initialise the greenhouse but don't start the simulation just yet.
   */
  public void initialize() {
    createNode(2, 1, 3, 2, 1);
    createNode(1, 2, 3, 2, 1);
    Logger.info("Greenhouse initialized");
  }

  /**
   * Create a sensor/actuator node with given parameters.
   *
   * @param temperature The temperature sensor value
   * @param humidity    The humidity sensor value
   * @param windows     The number of windows
   * @param fans        The number of fans
   * @param heaters     The number of heaters
   */
  private void createNode(int temperature, int humidity, int windows, int fans, int heaters) {
    SensorActuatorNode node = DeviceFactory.createNode(
        temperature, humidity, windows, fans, heaters);
    nodes.put(node.getId(), node);
    initiateTcpNodeClient(node);
  }


  /**
   * Create a TCP client for a sensor/actuator node.
   *
   * @param node The node to create a client for
   */
  private void initiateTcpNodeClient(SensorActuatorNode node) {
    Thread clientProcessor = new Thread(() -> {
      TcpSensorActuatorNodeClient client = new TcpSensorActuatorNodeClient("127.0.0.1", 10020, node);

      nodeClients.put(node.getId(), client);
      System.out.println("Client created for node " + node.getId() + " on " + Thread.currentThread().getName());

      client.run();

    });
    clientProcessor.start();
  }

  /**
   * Start a simulation of a greenhouse - all the sensor and actuator nodes inside it.
   */
  public void start() {
    for (SensorActuatorNode node : nodes.values()) {
      node.start();
    }
    for (PeriodicSwitch periodicSwitch : periodicSwitches) {
      periodicSwitch.start();
    }

    Logger.info("Simulator started");
  }

  /**
   * Stop the simulation of the greenhouse - all the nodes in it.
   */
  public void stop() {
    stopCommunication();
    for (SensorActuatorNode node : nodes.values()) {
      node.stop();
    }
  }

  /**
   * Add a listener for greenhouse events.
   */
  private void stopCommunication() {
    nodeClients.forEach((id, client) -> client.stop());
  }

  /**
   * Add a listener for notification of node staring and stopping.
   *
   * @param listener The listener which will receive notifications
   */
  public void subscribeToLifecycleUpdates(NodeStateListener listener) {
    for (SensorActuatorNode node : nodes.values()) {
      node.addStateListener(listener);
    }
  }
}
