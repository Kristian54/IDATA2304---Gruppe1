package no.ntnu.server;

import java.io.*;
import java.net.Socket;

public class ClientHandler implements Runnable {
  private Socket clientSocket;


  public ClientHandler(Socket socket) {
    this.clientSocket = socket;
  }

  @Override
  public void run() {
    try (BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
         PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)) {

      String message;
      while ((message = in.readLine()) != null) {
        System.out.println("Received: " + message);
        out.println("Echo: " + message); // Echo back the message to the client
      }
    } catch (IOException e) {
      e.printStackTrace();
    } finally {
      try {
        clientSocket.close();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }
}