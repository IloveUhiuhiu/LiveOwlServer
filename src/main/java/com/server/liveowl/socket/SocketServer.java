package com.server.liveowl.socket;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class SocketServer implements Runnable {
    public void run() {
        try (ServerSocket serverSocket = new ServerSocket(9876)) {
            System.out.println("Server đang lắng nghe trên cổng 9876");
            while (true) {
                Socket clientSocket = serverSocket.accept();
                new Thread(new ClientHandler(clientSocket)).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
