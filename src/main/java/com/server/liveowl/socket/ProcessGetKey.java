package com.server.liveowl.socket;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import static com.server.liveowl.ServerConfig.*;


public class ProcessGetKey implements Runnable {
    private final ProcessGetImage processGetData;
    private static int PORT;
    private ServerSocket serverSocket;
    private final ExecutorService threadPool = Executors.newFixedThreadPool(NUM_OF_THREAD);

    public ProcessGetKey(ProcessGetImage processGetData) {
        this.processGetData = processGetData;
        PORT = processGetData.getProcessId() + 12345;
    }

    @Override
    public void run() {
        while (true) {
            synchronized (processGetData) {
                while (!processGetData.isRunning()) {
                    try {
                        System.out.println("Dịch vụ tạm dừng, chờ bật lại...");
                        processGetData.wait(); // Chờ cho đến khi isRunning là true
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        return; // Thoát nếu bị gián đoạn
                    }
                }
            }

            // Khởi động ServerSocket
            try (ServerSocket serverSocket = new ServerSocket(PORT)) {
                this.serverSocket = serverSocket;
                System.out.println("Dịch vụ bắt đầu lắng nghe trên cổng " + PORT);
                while (processGetData.isRunning()) {
                    try {
                        serverSocket.setSoTimeout(2000); // Timeout ngắn để kiểm tra trạng thái
                        Socket socket = serverSocket.accept();
                        System.out.println("Socket accepted");
                        threadPool.submit(() -> handleClient(socket));
                    } catch (SocketTimeoutException e) {
                        // Tiếp tục lắng nghe nếu timeout
                    }
                }
            } catch (IOException e) {
                if (processGetData.isRunning()) {
                    e.printStackTrace();
                }
            } finally {
                System.out.println("Dịch vụ dừng lại.");
            }
        }
    }

    private void handleClient(Socket socket) {
        String clientId = "";
        try (InputStream inputStream = socket.getInputStream();
             BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            clientId = reader.readLine();
            if (clientId == null) {
                System.out.println("Không nhận được Id client.");
                return;
            }
            String logFilePath = KEYBOARD_PATH + "_" + processGetData.getCode() + "\\keyboard_" + clientId + ".txt";
            System.out.println("Đường dẫn lưu file " + logFilePath);
            try (FileWriter writer = new FileWriter(logFilePath, true)) {
                String keyStroke;
                while ((keyStroke = reader.readLine()) != null) {
                    System.out.println("key nhận được: " + keyStroke);
                    writer.write(keyStroke);
                    writer.flush();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                socket.close();
                System.out.println("Đã đóng kết nối client " + clientId);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void signalStateChange() {
        synchronized (processGetData) {
            processGetData.notifyAll();
        }
    }
}