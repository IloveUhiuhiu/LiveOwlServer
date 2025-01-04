package com.server.liveowl.util;

import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;

public class TcpHandler {
    public static boolean sendCameraRequest(InetAddress serverHostName, int serverPort, String index) {
        try (Socket cameraSocket = new Socket(serverHostName, serverPort)) {
            // Tạo PrintWriter để gửi yêu cầu
            PrintWriter writer = new PrintWriter(cameraSocket.getOutputStream(), true);
            // Gửi thông điệp yêu cầu camera
            writer.println("camera:" + index); // Sử dụng println để gửi chuỗi
            System.out.println("Gửi thành công yêu cầu button camera cho " + index);
            return true; // Yêu cầu đã được gửi thành công
        } catch (Exception e) {
            e.printStackTrace(); // Xử lý ngoại lệ
            return false; // Xảy ra lỗi khi gửi yêu cầu
        }
    }

    public static boolean sendExitRequest(InetAddress serverHostName, int serverPort, String token) {
        try (Socket exitSocket = new Socket(serverHostName, serverPort)) {
            // Tạo PrintWriter để gửi yêu cầu
            PrintWriter writer = new PrintWriter(exitSocket.getOutputStream(), true);
            // Gửi yêu cầu exit với token
            writer.println("exit:" + token); // Gửi thông điệp
            return true; // Yêu cầu đã được gửi thành công
        } catch (Exception e) {
            e.printStackTrace(); // Xử lý ngoại lệ
            return false; // Xảy ra lỗi khi gửi yêu cầu
        }
    }
}
