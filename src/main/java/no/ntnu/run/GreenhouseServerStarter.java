package no.ntnu.run;

import no.ntnu.server.TCPServer;
import no.ntnu.server.TcpCommunicationChannel;

public class GreenhouseServerStarter {
    public static void main(String[] args) {
        TCPServer server = TCPServer.getInstance();
        server.startServer(TCPServer.PORT_NUMBER);
    }
}
