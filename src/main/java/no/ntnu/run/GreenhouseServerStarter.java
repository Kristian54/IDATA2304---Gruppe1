package no.ntnu.run;

import no.ntnu.server.TCPServer;

/** Starter class for the greenhouse server. */
public class GreenhouseServerStarter {
  /**
   * Entrypoint for the greenhouse server.
   *
   * @param args Command line arguments, not used.
   */
  public static void main(String[] args) {
    TCPServer server = TCPServer.getInstance();
    server.startServer(TCPServer.PORT_NUMBER);
  }
}
