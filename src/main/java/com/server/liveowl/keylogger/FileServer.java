package com.server.liveowl.keylogger;

import com.server.liveowl.ServerConfig;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import static com.server.liveowl.ServerConfig.NUM_OF_THREAD;

public class FileServer implements Runnable {
private final ExecutorService executor = Executors.newFixedThreadPool(NUM_OF_THREAD);
@Override
public void run() {
    try (ServerSocket server = new ServerSocket(ServerConfig.SEND_KEYBOARD_PORT)) {
        System.out.println("FileServer đã sẵn sàng lắng nghe trên cổng: " + ServerConfig.SEND_KEYBOARD_PORT);
        while (true) {
            Socket clientSocket = server.accept();
            executor.execute(new ProcessSendFile(clientSocket));
        }
    } catch (IOException e) {
        System.out.println("Lỗi khi khởi tạo FileServer: " + e.getMessage());
    } finally {
        executor.shutdown();
    }
}
}
