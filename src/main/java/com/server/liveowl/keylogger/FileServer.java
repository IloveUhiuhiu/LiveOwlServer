package com.server.liveowl.keylogger;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.server.liveowl.ServerConfig.NUM_OF_THREAD;

public class FileServer implements Runnable {
private final int port;
private final String basePath;
private final ExecutorService executor;

public FileServer(int port, String basePath) {
    this.port = port;
    this.basePath = basePath;
    this.executor = Executors.newFixedThreadPool(NUM_OF_THREAD);
}

@Override
public void run() {
    try (ServerSocket server = new ServerSocket(port)) {
        System.out.println("Server đã sẵn sàng lắng nghe trên cổng: " + port);
        while (true) {
            Socket clientSocket = server.accept();
            executor.execute(new ProcessSendFile(clientSocket, basePath));
        }
    } catch (IOException e) {
        System.out.println("Lỗi khi khởi tạo server: " + e.getMessage());
    } finally {
        executor.shutdown();
    }
}
}
