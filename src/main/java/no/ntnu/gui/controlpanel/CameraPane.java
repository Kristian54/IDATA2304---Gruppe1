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

public class CameraPane extends TitledPane {
  private final int nodeId;
  private HBox content;

  public CameraPane(int nodeId) {
    super();
    this.nodeId = nodeId;
    setText("Camera");
    content = new HBox();
    Button cameraButton = new Button("Take picture");
    content.getChildren().add(cameraButton);
    //cameraButton.setOnAction(event -> takePicture());
    setContent(content);
  }

  public int getNodeId() {
    return nodeId;
  }

  public void addImage(String data) {
    byte[] decoded = java.util.Base64.getDecoder().decode(data);
    Image image = new Image(new ByteArrayInputStream(decoded));
    setContent(new HBox(new javafx.scene.image.ImageView(image)));
  }

}
