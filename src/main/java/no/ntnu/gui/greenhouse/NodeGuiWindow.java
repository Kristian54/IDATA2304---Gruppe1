package no.ntnu.gui.greenhouse;

import java.util.List;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;
import no.ntnu.greenhouse.Actuator;
import no.ntnu.greenhouse.Sensor;
import no.ntnu.greenhouse.SensorActuatorNode;
import no.ntnu.gui.common.ActuatorPane;
import no.ntnu.gui.common.CameraPane;
import no.ntnu.gui.common.SensorPane;
import no.ntnu.listeners.common.ActuatorListener;
import no.ntnu.listeners.greenhouse.SensorListener;
import no.ntnu.tools.ImageLoader;

/**
 * Window with GUI for overview and control of one specific sensor/actuator node.
 */
public class NodeGuiWindow extends Stage implements SensorListener, ActuatorListener {
  private static final double VERTICAL_OFFSET = 50;
  private static final double HORIZONTAL_OFFSET = 150;
  private static final double WINDOW_WIDTH = 300;
  private static final double WINDOW_HEIGHT = 450;
  private final SensorActuatorNode node;

  private ActuatorPane actuatorPane;
  private SensorPane sensorPane;

  private CameraPane cameraPane;

  private final Image[] cameraImages = {
      ImageLoader.loadImageFromFile("images/camera1.jpg"),
      ImageLoader.loadImageFromFile("images/camera2.jpg"),
      ImageLoader.loadImageFromFile("images/camera3.jpg"),
  };

  private int currentImageIndex = 0;

  /**
   * Create a GUI window for a specific node.
   *
   * @param node The node which will be handled in this window
   */
  public NodeGuiWindow(SensorActuatorNode node) {
    this.node = node;
    Scene scene = new Scene(createContent(), WINDOW_WIDTH, WINDOW_HEIGHT);
    setScene(scene);
    setTitle("Node " + node.getId());
    initializeListeners(node);
    setPositionAndSize();
    startImageRotation();
    requestCameraImage();
  }

  private void setPositionAndSize() {
    setX((node.getId() - 1) * HORIZONTAL_OFFSET);
    setY(node.getId() * VERTICAL_OFFSET);
    setMinWidth(WINDOW_HEIGHT);
    setMinHeight(WINDOW_WIDTH);
  }


  private void initializeListeners(SensorActuatorNode node) {
    setOnCloseRequest(windowEvent -> shutDownNode());
    node.addSensorListener(this);
    node.addActuatorListener(this);
  }

  private void shutDownNode() {
    node.stop();
  }

  private Parent createContent() {
    actuatorPane = new ActuatorPane(node.getActuators());
    sensorPane = new SensorPane(node.getSensors());
    cameraPane = new CameraPane();
    return new VBox(sensorPane, actuatorPane, cameraPane);
  }

  private void startImageRotation() {
    Timeline timeline = new Timeline(
        new KeyFrame(Duration.seconds(10), event -> switchToNextImage())
    );
    timeline.setCycleCount(Timeline.INDEFINITE);
    timeline.play();
  }

  private void switchToNextImage() {
    currentImageIndex = (currentImageIndex + 1) % cameraImages.length;
    cameraPane.setCameraImage(cameraImages[currentImageIndex]);
  }

  private void requestCameraImage() {
    // TODO: Implement
  }

  @Override
  public void sensorsUpdated(List<Sensor> sensors) {
    if (sensorPane != null) {
      sensorPane.update(sensors);
    }
  }

  @Override
  public void actuatorUpdated(int nodeId, Actuator actuator) {
    if (actuatorPane != null) {
      actuatorPane.update(actuator);
    }
  }

}
