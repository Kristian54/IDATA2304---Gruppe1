package no.ntnu.run;

import no.ntnu.server.TcpServer;

/** Starter class for the greenhouse server. */
public class GreenhouseServerStarter {
  /**
   * Entrypoint for the greenhouse server.
   *
   * @param args Command line arguments, not used.
   */
  public static void main(String[] args) {
    TcpServer server = TcpServer.getInstance();
    server.startServer(TcpServer.PORT_NUMBER);
  }
}
