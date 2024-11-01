package no.ntnu.controlpanel;

import no.ntnu.server.TCPServer;

public class TcpCommunicationChannel implements CommunicationChannel {
  TCPServer server;
  ControlPanelLogic logic;

  public TcpCommunicationChannel(ControlPanelLogic logic) {
    if (logic == null) throw new RuntimeException("Logic cannot be null");
    this.logic = logic;
  }

  @Override
  public void sendActuatorChange(int nodeId, int actuatorId, boolean isOn) {
    logic.onActuatorStateChanged(nodeId, actuatorId, isOn);
  }

  @Override
  public boolean open() {
    Boolean result = false;
    try {
      this.server = new TCPServer(this);
      this.server.startServer(TCPServer.PORT_NUMBER);
      result = true;
    } catch (RuntimeException e) {
      //TODO: Handle error :)
    }

    return result;
  }
}
