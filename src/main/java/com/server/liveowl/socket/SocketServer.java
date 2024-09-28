package com.server.liveowl.socket;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.Vector;
import java.util.logging.Logger;

public class SocketServer implements Runnable {
    private static final int PORT = 9876;
    public static Map<String, Vector<StudentHandler>> students = new HashMap<>();
    public static Map<String, TeacherHandler> teachers = new HashMap<>();

    private final static Logger audit = Logger.getLogger("requests");
    private final static Logger errors = Logger.getLogger("errors");

    public void run() {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Server đang lắng nghe ....");
            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println(clientSocket.getInetAddress().getHostAddress() + "kết nối");
                DataInputStream in = new DataInputStream(clientSocket.getInputStream());

                String role = in.readUTF();
                if (role.equals("student")) {
                    // Lấy mã
                    String code = in.readUTF();
                    System.out.println("Student gửi mã: " + code);
                    StudentHandler theStudent = new StudentHandler(clientSocket,code);
                    // Lưu vào map
                    if (!SocketServer.teachers.containsKey(code)) {
                        theStudent.sendRequest("fail");
                    } else {
                        theStudent.sendRequest("success");
                        SocketServer.students.computeIfAbsent(code, k -> new Vector<>()).add(theStudent);
                        // Khởi tạo thread
                        new Thread(new StudentHandler(clientSocket, code)).start();
                    }

                } else if (role.equals("teacher")) {
                    // Lấy mã cuộc họp
                    String code = in.readUTF();
                    System.out.println("Teach tạo mã: " + code);
                    TeacherHandler theTeacher = new TeacherHandler(clientSocket,code);
                    // Lưu vào map
                    SocketServer.teachers.put(code, theTeacher);
                    // Khởi tạo thread
                    new Thread(new TeacherHandler(clientSocket, code)).start();
                } else {

                }
            }
        } catch (IOException e) {
            System.err.println("Lỗi server: " + e.getMessage());
        }
    }
}

class StudentHandler implements Runnable {
    private final Socket theSocket;
    public final DataOutputStream dos;
    public final DataInputStream dis;
    private final String code;

    public StudentHandler(Socket theSocket, String code) throws IOException {
        this.theSocket = theSocket;
        this.dos = new DataOutputStream(theSocket.getOutputStream());
        this.dis = new DataInputStream(theSocket.getInputStream());
        this.code = code;
    }
    public void sendRequest(String message) {
        try {
            dos.writeUTF(message);
        } catch (IOException e) {
            throw new RuntimeException("Lỗi khi gửi " + message);
        }
    }
    public String receiveResponse() {
        try {
            return dis.readUTF();
        } catch (IOException e) {
            throw new RuntimeException("Lỗi khi nhập phản hổi!!!");
        }

    }
    @Override
    public void run() {
        String headerOfRequest;
        int size = -1;
        byte[] imageBytes = new byte[0];
        try {
            while (true) {
                headerOfRequest = dis.readUTF();
                switch (headerOfRequest) {
                    case "Length":
                        size = dis.readInt();
                        if (size <= 0) {
                            throw new IOException("Lỗi size ảnh bằng 0");
                        }
                        break;

                    case "Image":
                        imageBytes = new byte[size];
                        dis.readFully(imageBytes);

                        sendImageForTeacher(imageBytes,size);
                        size = -1;
                        break;
                    default:
                        System.err.println("Yêu cầu không hợp lệ: " + headerOfRequest);
                }
            }
        } catch (IOException ex) {
            System.err.println("Lỗi trong vòng lặp: " + ex.getMessage());
        } finally {
            try {
                theSocket.close();
            } catch (IOException e) {
                System.err.println("Lỗi khi đóng socket: " + e.getMessage());
            }
        }
    }
    private void sendImageForTeacher(byte[] imageBytes, int size) throws IOException {
        TeacherHandler theTeacher = SocketServer.teachers.get(code);
        // server sử lý, gộp thành một khung ảnh và gửi cho teacher
        // Thử trường hợp 1 teacher nhận
        theTeacher.dos.writeInt(size);
        theTeacher.dos.write(imageBytes);
    }

}

class TeacherHandler implements Runnable {
    private final Socket theSocket;
    public final DataOutputStream dos;
    public final DataInputStream dis;
    private final String code;

    public TeacherHandler(Socket theSocket, String code) throws IOException {
        this.theSocket = theSocket;
        this.dos = new DataOutputStream(theSocket.getOutputStream());
        this.dis = new DataInputStream(theSocket.getInputStream());
        this.code = code;
    }
    public void sendRequest(String message) {
        try {
            dos.writeUTF(message);
        } catch (IOException e) {
            throw new RuntimeException("Lỗi khi gửi " + message);
        }
    }
    public String receiveResponse() {
        try {
            return dis.readUTF();
        } catch (IOException e) {
            throw new RuntimeException("Lỗi khi nhập phản hổi!!!");
        }

    }
    @Override
    public void run() {
        String headerOfRequest;

        try {
            while (true) {
                headerOfRequest = dis.readUTF();

                switch (headerOfRequest) {
                    case "Camera":
                        // Xử lý camera
                        // Yêu cầu id hay gì đó để nhận biết được clients cần mở camera
                        break;
                    default:
                        System.err.println("Yêu cầu không hợp lệ: " + headerOfRequest);
                }
            }
        } catch (IOException ex) {
            System.err.println("Lỗi trong vòng lặp: " + ex.getMessage());
        } finally {
            try {
                theSocket.close();
            } catch (IOException e) {
                System.err.println("Lỗi khi đóng socket: " + e.getMessage());
            }
        }
    }

}

