package no.ntnu.run;

import no.ntnu.gui.greenhouse.GreenhouseApplication;
import no.ntnu.server.TCPServer;

//TODO: FIX OR DELETE
public class SimulationStarter {
  public static void main(String[] args) {
    TCPServer server = TCPServer.getInstance();
    server.startServer(TCPServer.PORT_NUMBER);

    try {
      Thread.sleep(500);
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }

    ControlPanelStarter controlPanelStarter = new ControlPanelStarter();
    controlPanelStarter.start();

    try {
      Thread.sleep(500);
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }

    GreenhouseApplication.startApp();
  }
}
