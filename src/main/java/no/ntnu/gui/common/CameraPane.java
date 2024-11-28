package no.ntnu.gui.common;

import javafx.geometry.Insets;
import javafx.scene.control.TitledPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;

public class CameraPane extends TitledPane {
  private VBox vbox;
  private ImageView imageView;

  public CameraPane() {
    super();
    setText("Camera");

    vbox = new VBox();
    vbox.setSpacing(10);
    vbox.setPadding(new Insets(10));

    imageView = new ImageView();
    imageView.setFitWidth(200);
    imageView.setPreserveRatio(true);

    vbox.getChildren().add(imageView);
    setContent(vbox);

    GuiTools.stretchVertically(this);
  }

  /**
   * Sets the camera image in the CameraPane.
   *
   * @param image The image captured by the camera.
   */
  public void setCameraImage(Image image) {
    imageView.setImage(image);
  }
}
