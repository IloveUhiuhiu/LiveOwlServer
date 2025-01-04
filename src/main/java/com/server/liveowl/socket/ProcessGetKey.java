package com.server.liveowl.socket;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.server.liveowl.ServerConfig.*;

public class ProcessGetKey implements Runnable {
    private ProcessGetImage processGetData;
    private static int PORT;
    private final ExecutorService threadPool = Executors.newFixedThreadPool(NUM_OF_THREAD);
    public ProcessGetKey(ProcessGetImage processGetData) {
        this.processGetData = processGetData;
        PORT = processGetData.getProcessId() + 12345;
    }
    public void run() {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {

            while (processGetData.isRunning()) {
                try {
                    serverSocket.setSoTimeout(2000); 
                    Socket socket = serverSocket.accept();
                    threadPool.submit(() -> handleClient(socket));
                } catch (SocketTimeoutException e) {
                    //System.out.println("Không có kết nối mới, tiếp tục lắng nghe...");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            threadPool.shutdown();
            System.out.println("Close ProcessGetKey");
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

}

