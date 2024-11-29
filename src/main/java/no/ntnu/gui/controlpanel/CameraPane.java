package no.ntnu.gui.controlpanel;

import java.io.ByteArrayInputStream;
import java.util.List;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.image.Image;
import javafx.scene.layout.HBox;
import no.ntnu.controlpanel.SensorActuatorNodeInfo;
import no.ntnu.greenhouse.SensorReading;
import no.ntnu.listeners.controlpanel.GreenhouseEventListener;

/**
 * A pane for displaying camera data.
 */
public class CameraPane extends TitledPane {
  private final int nodeId;

  /**
   * Create a camera pane.
   *
   * @param nodeId The node ID of the camera
   */
  public CameraPane(int nodeId) {
    super();
    this.nodeId = nodeId;
    setText("Camera");
    setContent(new HBox());
  }

  /**
   * Get the node ID of the camera.
   *
   * @return The node ID of the camera
   */
  public int getNodeId() {
    return nodeId;
  }

  /**
   * Add an image to the camera pane.
   *
   * @param data The image data
   */
  public void addImage(String data) {
    byte[] decoded = java.util.Base64.getDecoder().decode(data);
    Image image = new Image(new ByteArrayInputStream(decoded));
    setContent(new HBox(new javafx.scene.image.ImageView(image)));
  }

}
