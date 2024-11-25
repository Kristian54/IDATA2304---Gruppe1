package no.ntnu.gui.greenhouse;


import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import no.ntnu.greenhouse.GreenhouseSimulator;
import no.ntnu.greenhouse.SensorActuatorNode;


/**
 * The main GUI window for greenhouse simulator.
 */
public class MainGreenhouseGuiWindow extends Scene {
  public static final int WIDTH = 300;
  public static final int HEIGHT = 200;

  public MainGreenhouseGuiWindow(GreenhouseSimulator simulator) {
    super(createMainContent(simulator), WIDTH, HEIGHT);
  }

  private static Parent createMainContent(GreenhouseSimulator simulator) {
    VBox container = new VBox(createCloseButton(), addNewNodeButton(simulator));
    container.setPadding(new Insets(20));
    container.setAlignment(Pos.CENTER);
    container.setSpacing(5);
    return container;
  }

  private static Node addNewNodeButton(GreenhouseSimulator simulator) {
    Button addNewNodeButton = new Button("Add new node");
    addNewNodeButton.setOnAction(event -> {
      SensorActuatorNode newNode = simulator.createNode(2, 1, 3, 2, 1);
      newNode.start();
      openNodeGuiWindow(newNode);
    });
    return addNewNodeButton;
  }

  private static void openNodeGuiWindow(SensorActuatorNode node) {
    NodeGuiWindow nodeWindow = new NodeGuiWindow(node);
    nodeWindow.show();
  }


  private static Node createCloseButton() {
    Button closeButton = new Button("Close Greenhouse Simulator");
    closeButton.setOnAction(event -> {
      javafx.application.Platform.exit();
      System.exit(0);
    });
    return closeButton;
  }

}
