package no.ntnu.gui.greenhouse;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;

/**
 * The main GUI window for greenhouse simulator.
 */
public class MainGreenhouseGuiWindow extends Scene {
  public static final int WIDTH = 300;
  public static final int HEIGHT = 200;

  public MainGreenhouseGuiWindow() {
    super(createMainContent(), WIDTH, HEIGHT);
  }

  private static Parent createMainContent() {
    VBox container = new VBox(createCloseButton());
    container.setPadding(new Insets(20));
    container.setAlignment(Pos.CENTER);
    container.setSpacing(5);
    return container;
  }


  private static Node createCloseButton() {
    Button closeButton = new Button("Close Greenhouse Simulator");
    closeButton.setOnAction(event -> {
      javafx.application.Platform.exit();
    });
    return closeButton;
  }

}
