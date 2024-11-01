package no.ntnu.greenhouse;
import java.io.*;
import java.net.*;
import no.ntnu.controlpanel.TcpCommunicationChannel;

public class TcpNodeClient {

  private boolean running;
  private Socket socket;
  private PrintWriter writer;
  private BufferedReader reader;
  private String ip;
  private int port;

  public TcpNodeClient(String ipAddress, int port, SensorActuatorNode node) {
    if (node == null) throw new RuntimeException("Node cannot be null");
    if (ipAddress == null) throw new RuntimeException("IP Adress cannot be null");
    if (port < 0 || port > 65535) throw new RuntimeException("Port number must be within 5 digits and not negative");
    this.ip = ipAddress;
    this.port = port;

    run();
  }
  
  private void run() {
    try {
      startConnection();
      this.running = true;
    } catch (IOException e) {
      System.out.println("Error connecting to server");
    }


    while (running) {
      try {
        Thread.sleep(1000);
      } catch (InterruptedException e) {
        System.out.println("Oida");
      }
      System.out.println("Do Things");
    }

    stopConnection();
  }

  private void startConnection() throws IOException {
      this.socket = new Socket(this.ip, this.port);
      this.writer = new PrintWriter(this.socket.getOutputStream(), true);
      this.reader = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
  }
  private void stopConnection () {
    try {
      if (this.writer != null) writer.close();
      if (this.reader != null) reader.close();
      if (socket != null && !socket.isClosed()) {
        socket.close();
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public static void main(String args[]) {
    TcpNodeClient client = new TcpNodeClient("127.0.0.1", 10020, new SensorActuatorNode(1));
  }
}
