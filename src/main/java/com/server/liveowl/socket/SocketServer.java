package com.server.liveowl.socket;
import java.net.*;
import java.util.*;
import java.util.logging.Logger;
import java.util.concurrent.Executors;

import org.opencv.videoio.VideoWriter;
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
        try (DatagramSocket serverSocket = new DatagramSocket(SERVER_PORT)){
            System.out.println("Server đang lắng nghe ...");
            while (true) {
                DatagramPacket packet = UdpHandler.getPacket(serverSocket);
                String connect = new String(packet.getData(), 0, packet.getLength());
                executor.execute(() -> handleClient(packet, serverSocket, connect));
            }
        } catch (Exception e) {
            System.err.println("Lỗi server: " + e.getMessage());
        } finally {
            executor.shutdown();
            listMeeting.clear();
        }
    }


    private static void handleClient(DatagramPacket packet, DatagramSocket serverSocket, String connect) {
        try {
            String clientId = connect.split(":")[0];
            String role = connect.split(":")[1];
            String code = connect.split(":")[2];

            InetAddress address = packet.getAddress();
            int port = packet.getPort();

            if (role.equals("student")) {
                System.out.println("Student gửi mã: " + code);
                if (!SocketServer.listMeeting.containsKey(code)) {
                    System.out.println("Student gửi mã không có trong map");
                    UdpHandler.sendMsg(serverSocket,"fail",address,port);
                } else {
                    UdpHandler.sendMsg(serverSocket,"success",address,port);
                }
                ProcessGetImage processGetImage = listMeeting.get(code);
                processGetImage.addStudent(clientId, packet);
                UdpHandler.sendNumber(serverSocket,processGetImage.getProcessId(),address,port);

            } else if (role.equals("teacher")) {
                ++numberOfConnect;
                ProcessGetImage processGetImage = new ProcessGetImage(packet,code,clientId,numberOfConnect);
                ProcessSendImage processSendImage = new ProcessSendImage(processGetImage);
                ProcessSaveImage processSaveImage = new ProcessSaveImage(processGetImage);
                ProcessGetKey processGetKey = new ProcessGetKey(processGetImage);

                UdpHandler.sendNumber(serverSocket,numberOfConnect,address,port);
                System.out.println("Trả về số cổng thành công");
                listMeeting.put(code, processGetImage);

                FileHandler.checkAndCreateFolder(VIDEO_PATH + "\\_" +code);
                FileHandler.checkAndCreateFolder(KEYBOARD_PATH + "\\_" + code);

                new Thread(processGetImage).start();
                new Thread(processSendImage).start();
                new Thread(processSaveImage).start();
                new Thread(processGetKey).start();
            }

        } catch (Exception e) {
            System.err.println("Error in handleClient: " + e.getMessage());
        }
    }
}
