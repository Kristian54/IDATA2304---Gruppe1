package no.ntnu.server;

import java.io.*;
import java.net.Socket;

public class ClientHandler extends Thread {
  private Socket clientSocket;


  public ClientHandler(Socket socket) {
    this.clientSocket = socket;
  }

  @Override
  public void run() {
    try (BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
         PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)) {
      System.out.println("Processing client on "+ Thread.currentThread().getName());

      String message;
      while ((message = in.readLine()) != null) {
        System.out.println("Received: " + message);
        System.out.println("Echo: " + message); // Echo back the message to the client
      }
    } catch (IOException e) {
      e.printStackTrace();
    } finally {
      System.out.println("Client on "+ Thread.currentThread().getName()+" disconnected");
      //clientSocket.close();
    }
  }
}