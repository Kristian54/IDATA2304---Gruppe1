package no.ntnu.greenhouse;

import java.util.Objects;

/**
 * Represents one sensor reading (value).
 */
public class SensorReading {
  private final String type;
  private double value;
  private final String unit;

  /**
   * Create a new sensor reading.
   *
   * @param type  The type of sensor being red
   * @param value The current value of the sensor
   * @param unit  The unit, for example: %, lux
   */
  public SensorReading(String type, double value, String unit) {
    this.type = type;
    this.value = value;
    this.unit = unit;
  }

  /**
   * Returns the type of the sensor.
   *
   * @return The type of the sensor
   */
  public String getType() {
    return type;
  }

  /**
   * Returns the value of the sensor.
   *
   * @return The value of the sensor
   */
  public double getValue() {
    return value;
  }

  /**
   * Returns the unit of the sensor.
   *
   * @return The unit of the sensor
   */
  public String getUnit() {
    return unit;
  }

  /**
   * Set the value of the sensor.
   *
   * @param newValue The new value of the sensor
   */
  public void setValue(double newValue) {
    this.value = newValue;
  }

  /**
   * Get a string representation of the sensor reading.
   * @return A string representation of the sensor reading
   */
  @Override
  public String toString() {
    return "{ type=" + type + ", value=" + value + ", unit=" + unit + " }";
  }

  /**
   * Get a human-readable (formatted) version of the current reading, including the unit.
   *
   * @return The sensor reading and the unit
   */
  public String getFormatted() {
    return value + unit;
  }

  /**
   * Check if two sensor readings are equal.
   *
   * @param o The object to compare with
   * @return True if the sensor readings are equal, false otherwise
   */
  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    SensorReading that = (SensorReading) o;
    return Double.compare(value, that.value) == 0
        && Objects.equals(type, that.type)
        && Objects.equals(unit, that.unit);
  }

  /**
   * Get the hash code of the sensor reading.
   *
   * @return The hash code of the sensor reading
   */
  @Override
  public int hashCode() {
    return Objects.hash(type, value, unit);
  }
}
