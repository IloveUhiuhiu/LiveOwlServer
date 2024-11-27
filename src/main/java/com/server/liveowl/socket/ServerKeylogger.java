package com.server.liveowl.socket;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.server.liveowl.ServerConfig.keyboardPath;

public class ServerKeylogger implements Runnable {
    private ProcessGetData processGetData;
    private static final int PORT = 12345;
   // private static final String LOG_DIRECTORY = "E:\\Downloads\\LiveOwlServer\\src\\main\\java\\com\\server\\liveowl\\Keylogger\\";
    //private static final String LOG_DIRECTORY = "D:\\PBL4\\LiveOwlServer\\src\\main\\java\\com\\server\\liveowl\\Keylogger\\";
    private static final int MAX_THREADS = 25;
    private final ExecutorService threadPool = Executors.newFixedThreadPool(MAX_THREADS);
public ServerKeylogger(ProcessGetData processGetData) {
    this.processGetData = processGetData;
}
public void run() {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("ServerKeylogger đang lắng nghe ở cổng " + PORT);
            while (processGetData.isRunning()) {
                try {
                    serverSocket.setSoTimeout(2000); // 10 giây
                    Socket socket = serverSocket.accept();
                    threadPool.submit(() -> handleClient(socket));
                } catch (SocketTimeoutException e) {
                    // Không có kết nối mới trong 10 giây
                    System.out.println("Không có kết nối mới, tiếp tục lắng nghe...");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            threadPool.shutdown();
            System.out.println("Close ServerKeyLogger");
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
            String logFilePath = keyboardPath + "/_94e653ee/keyboard_" + clientID + ".txt";
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
//private void handleClient(Socket socket) {
//    String clientID = "";
//    try {
//        // Thiết lập timeout cho socket
//        socket.setSoTimeout(30000); // 30 giây (30000 mili giây)
//
//        InputStream inputStream = socket.getInputStream();
//        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
//
//        // Đọc ID của client
//        clientID = reader.readLine();
//        if (clientID == null) {
//            System.out.println("Không nhận được ID client.");
//            return;
//        }
//
//        String logFilePath = keyboardPath + "/_94e653ee/keyboard_" + clientID + ".txt";
//        // Ghi dữ liệu vào file
//        try (FileWriter writer = new FileWriter(logFilePath, true)) { // Mở file ở chế độ "append"
//            String keyStroke;
//            while ((keyStroke = reader.readLine()) != null) {
//                writer.write(keyStroke);
//                writer.flush();
//            }
//        }
//    } catch (SocketTimeoutException e) {
//        System.out.println("Timeout: không nhận được dữ liệu từ client trong thời gian quy định.");
//    } catch (IOException e) {
//        e.printStackTrace();
//    } finally {
//        try {
//            socket.close();
//            System.out.println("Đã đóng kết nối client " + clientID);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
//}
}

