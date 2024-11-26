package no.ntnu.run;

import java.util.ArrayList;
import no.ntnu.controlpanel.ControlPanelLogic;
import no.ntnu.gui.controlpanel.ControlPanelApplication;
import no.ntnu.gui.controlpanel.TcpControlpanelNodeClient;
import no.ntnu.tools.Logger;

/**
 * Starter class for the control panel. Note: we could launch the Application class directly, but
 * then we would have issues with the debugger (JavaFX modules not found)
 */
public class ControlPanelStarter {
  private final ArrayList<TcpControlpanelNodeClient> nodeClients = new ArrayList<>();

  /**
   * Create a new starter for the control panel.
   */
  public ControlPanelStarter() {}

  /**
   * Entrypoint for the application.
   *
   * @param args Command line arguments, only the first one of them used: when it is "fake", emulate
   *     fake events, when it is either something else or not present, use real socket
   *     communication. Go to Run → Edit Configurations. Add "fake" to the Program Arguments field.
   *     Apply the changes.
   */
  public static void main(String[] args) {
    boolean fake = false; // make it true to test in fake mode
    if (args.length == 1 && "fake".equals(args[0])) {
      Logger.info("Using FAKE events");
    }

    ControlPanelStarter starter = new ControlPanelStarter();
    starter.start();
  }

  /**
   * Start the control panel application.
   */
  public void start() {
    ControlPanelLogic logic = new ControlPanelLogic();
    initiateCommunication(logic);
    ControlPanelApplication.startApp(logic);
    // This code is reached only after the GUI-window is closed
    for (TcpControlpanelNodeClient client : nodeClients) {
      client.stop();
    }
    Logger.info("Exiting the control panel application");
  }

  private void initiateCommunication(ControlPanelLogic logic) {
    Thread clientProcessor =
        new Thread(
            () -> {
              TcpControlpanelNodeClient client =
                  new TcpControlpanelNodeClient("127.0.0.1", 10020, logic);

              nodeClients.add(client);
              System.out.println(
                  "Client created for control panel on " + Thread.currentThread().getName());

              client.run();
            });
    clientProcessor.start();
  }
}
