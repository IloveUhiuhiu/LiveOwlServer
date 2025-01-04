package com.server.liveowl.socket;
import java.net.*;
import java.util.*;
import java.util.logging.Logger;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import com.server.liveowl.util.FileHandler;
import com.server.liveowl.util.UdpHandler;
import static com.server.liveowl.ServerConfig.*;

public class SocketServer implements Runnable {

    private static int numberOfConnect = 0;
    public static Map<String, ProcessGetImage> listMeeting = new HashMap<>();
    private final static Logger audit = Logger.getLogger("requests");
    private final static Logger errors = Logger.getLogger("errors");
    private ExecutorService executor = Executors.newFixedThreadPool(NUM_OF_THREAD);
    public void run() {
        //new ProscessTesting().start();
        try (DatagramSocket serverSocket = new DatagramSocket(SERVER_PORT)){
            System.out.println("Server đang lắng nghe trên cổng " + SERVER_PORT);
            while (true) {
                DatagramPacket packet = UdpHandler.getPacket(serverSocket);
                String connect = new String(packet.getData(), 0, packet.getLength());
                executor.execute(() -> handleClient(packet, serverSocket, connect));// xử lý mỗi khi có request đến
            }
        } catch (Exception e) {
            System.err.println("Lỗi server: " + e.getMessage());
        } finally {
            listMeeting.clear();
            executor.shutdown();
        }
    }


    private static void handleClient(DatagramPacket packet, DatagramSocket serverSocket, String connect) {
        try {
            String clientId = connect.split(":")[0];
            String role = connect.split(":")[1];
            String code = connect.split(":")[2];

            InetAddress address = packet.getAddress();
            int port = packet.getPort();
            // chia trường hợp request
            if (role.equals("student")) {
                // nếu là học sinh
                // kiểm tra listmeeting có chứa code không
                // nếu không thì gửi chuỗi fail
                // nếu có thì gửi chuỗi success
                System.out.println("Student có mã " + clientId + " gửi mã: " + code);
                if (!SocketServer.listMeeting.containsKey(code)) {
                    UdpHandler.sendMsg(serverSocket,"fail",address,port);
                } else {
                    UdpHandler.sendMsg(serverSocket,"success",address,port);
                }
                ProcessGetImage processGetImage = listMeeting.get(code);// lấy luồng xử lý cuộc họp
                processGetImage.addStudent(clientId, packet);// thêm học sinh vào luồng để quản lý
                UdpHandler.sendNumber(serverSocket,processGetImage.getProcessId(),address,port);// gửi lại cổng mới cho hs
                // ++countStudent;
            } else if (role.equals("teacher")) {
                // nếu là giáo viên
                // tạo luồng nhận, gửi, lưu ảnh và nhận key
                System.out.println("Giáo viên có mã " + clientId + " gửi mã: " + code);
                ++numberOfConnect;
                ProcessGetImage processGetImage = new ProcessGetImage(packet,code,clientId,numberOfConnect);
                ProcessSendImage processSendImage = new ProcessSendImage(processGetImage);
                ProcessSaveImage processSaveImage = new ProcessSaveImage(processGetImage);
                ProcessGetKey processGetKey = new ProcessGetKey(processGetImage);

                UdpHandler.sendNumber(serverSocket,numberOfConnect,address,port);// gửi lại cổng mới cho gv
                listMeeting.put(code, processGetImage);

                FileHandler.checkAndCreateFolder(VIDEO_PATH + "\\_" +code);// kiêm tra hoặc tạo folder chứa video
                FileHandler.checkAndCreateFolder(KEYBOARD_PATH + "\\_" + code);// kiêm tra hoặc tạo folder chứa keylogger

                new Thread(processGetImage).start();
                new Thread(processSendImage).start();
                new Thread(processSaveImage).start();
                new Thread(processGetKey).start();
            }

        } catch (Exception e) {
            System.err.println("Lỗi trong hàm handleClient: " + e.getMessage());
        }
    }
}
