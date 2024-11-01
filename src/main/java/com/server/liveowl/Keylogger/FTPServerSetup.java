package com.server.liveowl.Keylogger;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;

public class FTPServerSetup {
    public static void main(String[] args) throws Exception {
        ServerSocket server = new ServerSocket(8888);
        System.out.println("Server đã sẵn sàng lắng nghe...");
        while (true) {
            try (Socket soc = server.accept();
                 DataInputStream dis = new DataInputStream(soc.getInputStream());
                 DataOutputStream dos = new DataOutputStream(soc.getOutputStream())) {

                String msg = dis.readUTF();

                if (msg.equals("getTime")) {
                    dos.writeUTF(new Date().toString());
                } else if (msg.equals("getClassName")) {
                    dos.writeUTF("Lập Trình Mạng");
                } else if (msg.equals("getFile")) {
                    String filePath = "D:/PBL4/LiveOwlServer/src/main/java/com/server/liveowl/Keylogger/fd720a2e_keylogs.txt"; // Đường dẫn file chính xác
                    try (BufferedInputStream bufferedInputStream = new BufferedInputStream(new FileInputStream(filePath));
                         BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(soc.getOutputStream())) {
                        int c;
                        while ((c = bufferedInputStream.read()) != -1) {
                            bufferedOutputStream.write(c);
                        }
                    }
                }
            } catch (Exception e) {
                System.out.println("Lỗi: " + e.getMessage());
            }
        }
    }
}
