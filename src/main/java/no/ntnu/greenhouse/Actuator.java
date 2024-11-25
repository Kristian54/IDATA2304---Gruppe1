package no.ntnu.greenhouse;

import java.util.HashMap;
import java.util.Map;
import no.ntnu.listeners.common.ActuatorListener;

/**
 * An actuator that can change the environment in a way. The actuator will make impact on the
 * sensors attached to this same node.
 */
public class Actuator {

  /**
   * The next ID to be used when creating a new actuator.
   */
  private static int nextId = 1;

  /**
   * The type of the actuator.
   */
  private final String type;

  /**
   * The ID of the node to which this actuator is connected.
   */
  private final int nodeId;

  /**
   * The unique ID of the actuator.
   */
  private final int id;

  /**
   * The impacts of this actuator on the sensors.
   */
  private Map<String, Double> impacts = new HashMap<>();

  /**
   * The listener which will be notified when actuator state changes.
   */
  private ActuatorListener listener;

  /**
   * Whether the actuator is currently active (ON) or inactive (OFF).
   */
  private boolean on;

  /**
   * Create an actuator. An ID will be auto-generated.
   *
   * @param type   The type of the actuator.
   * @param nodeId ID of the node to which this actuator is connected.
   */
  public Actuator(String type, int nodeId) {
    this.type = type;
    this.nodeId = nodeId;
    this.on = false;
    this.id = generateUniqueId();
  }

  /**
   * Create an actuator.
   *
   * @param id     The desired ID of the node.
   * @param type   The type of the actuator.
   * @param nodeId ID of the node to which this actuator is connected.
   */
  public Actuator(int id, String type, int nodeId) {
    this.type = type;
    this.nodeId = nodeId;
    this.on = false;
    this.id = id;
  }

  private static int generateUniqueId() {
    return nextId++;
  }

  /**
   * Set the listener which will be notified when actuator state changes.
   *
   * @param listener The listener of state change events
   */
  public void setListener(ActuatorListener listener) {
    this.listener = listener;
  }

  /**
   * Register the impact of this actuator when active.
   *
   * @param sensorType     Which type of sensor readings will be impacted. Example: "temperature"
   * @param diffWhenActive What will be the introduced difference in the sensor reading when
   *                       the actuator is active. For example, if this value is 2.0 and the
   *                       sensorType is "temperature", this means that "activating this actuator
   *                       will increase the readings of temperature sensors attached to the
   *                       same node by +2 degrees".
   */
  public void setImpact(String sensorType, double diffWhenActive) {
    impacts.put(sensorType, diffWhenActive);
  }

  /**
   * Get the type of the actuator.
   *
   * @return The type of the actuator
   */
  public String getType() {
    return type;
  }

  /**
   * Create a clone of this actuator.
   *
   * @return A clone of this actuator, where all the fields are the same
   */
  public Actuator createClone() {
    Actuator a = new Actuator(type, nodeId);
    // Note - we pass a reference to the same map! This should not be problem, as long as we
    // don't modify the impacts AFTER creating the template
    a.impacts = impacts;
    return a;
  }

  /**
   * Toggle the actuator - if it was off, not it will be ON, and vice versa.
   */
  public void toggle() {
    this.on = !this.on;
    notifyChanges();
  }

  private void notifyChanges() {
    if (listener != null) {
      listener.actuatorUpdated(this.nodeId, this);
    }
  }

  /**
   * Check whether the actuator is active (ON), or inactive (OFF).
   *
   * @return True when the actuator is ON, false if it is OFF
   */
  public boolean isOn() {
    return on;
  }

  /**
   * Apply impact of this actuator to all sensors of one specific sensor node.
   *
   * @param node The sensor node to be affected by this actuator.
   */
  public void applyImpact(SensorActuatorNode node) {
    for (Map.Entry<String, Double> impactEntry : impacts.entrySet()) {
      String sensorType = impactEntry.getKey();
      double impact = impactEntry.getValue();
      if (!on) {
        impact = -impact;
      }
      node.applyActuatorImpact(sensorType, impact);
    }
  }

  /**
   * Get the impacts of this actuator.
   *
   * @return The impacts of this actuator
   */
  @Override
  public String toString() {
    return "Actuator{"
        + "type='" + type + '\''
        + ", on=" + on
        + '}';
  }

  /**
   * Turn on the actuator.
   */
  public void turnOn() {
    if (!on) {
      on = true;
      notifyChanges();
    }
  }

  /**
   * Turn off the actuator.
   */
  public void turnOff() {
    if (on) {
      on = false;
      notifyChanges();
    }
  }

  /**
   * Returns the ID of the actuator.
   *
   * @return An ID which is guaranteed to be unique at a node level, not necessarily unique at
   *     the whole greenhouse-network level.
   */
  public int getId() {
    return id;
  }

  /**
   * Returns the ID of the node to which this actuator is connected.
   *
   * @return The ID of the node
   */
  public int getNodeId() {
    return nodeId;
  }

  /**
   * Set the actuator to the desired state.
   *
   * @param on Turn on when true, turn off when false
   */
  public void set(boolean on) {
    Runnable action =  on ? this::turnOn : this::turnOff;
    action.run();
  }
}
