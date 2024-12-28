
package com.server.liveowl.keylogger;

import java.io.*;
import java.net.Socket;

import static com.server.liveowl.ServerConfig.KEYBOARD_PATH;

public class ProcessSendFile implements Runnable {
private final Socket clientSocket;

public ProcessSendFile(Socket clientSocket) {
    this.clientSocket = clientSocket;
}

@Override
public void run() {
    try (DataInputStream dis = new DataInputStream(clientSocket.getInputStream());
         DataOutputStream dos = new DataOutputStream(clientSocket.getOutputStream())) {
        String clientId = dis.readUTF();
        String code = dis.readUTF();
        String filePath = KEYBOARD_PATH + "/_" + code + "/keyboard_" + clientId + ".txt";
        System.out.println("Đường dẫn file gửi: " + filePath);
        try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(filePath)))) {
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                dos.writeUTF(line);
            }
            dos.writeUTF("EOF");
        }
    } catch (IOException e) {
        System.out.println("Lỗi khi xử lý client: " + e.getMessage());
    } finally {
        try {
            clientSocket.close();
        } catch (IOException e) {
            System.out.println("Lỗi khi đóng kết nối client: " + e.getMessage());
        }
    }
}
}
