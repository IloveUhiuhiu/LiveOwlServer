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
    private ExecutorService executor = Executors.newFixedThreadPool(NUM_OF_THREAD);
    private static int countConnected = 0;
    public static DatagramSocket serverSocket = null;
    public static Map<String, ProcessGetData> listMeeting = new HashMap<>();
    private final static Logger audit = Logger.getLogger("requests");
    private final static Logger errors = Logger.getLogger("errors");

    public void run() {
        try {
            serverSocket = new DatagramSocket(serverPort);
            while (true) {
                System.out.println("Server đang lắng nghe ...");
                DatagramPacket packet = UdpHandler.getPacket(serverSocket);
                String connect = new String(packet.getData(), 0, packet.getLength());
                executor.execute(() -> handleClient(packet, serverSocket, connect));
            }
        } catch (Exception e) {
            System.err.println("Lỗi server: " + e.getMessage());
        } finally {
            if (serverSocket != null) serverSocket.close();
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
                ProcessGetData processGetData = listMeeting.get(code);
                Map<String, VideoWriter> videoWriters = processGetData.videoWriters;
                processGetData.addStudent(clientId, packet);
                UdpHandler.sendNumber(serverSocket,processGetData.processId,address,port);
                try {
                    if (!videoWriters.containsKey(clientId)) {
                        videoWriters.put(clientId,new VideoWriter(videoPath + "\\_" +code + "\\video_" + clientId +".mp4",
                                VideoWriter.fourcc('H', '2', '6', '4'), ProcessSavedData.fps,
                                new org.opencv.core.Size(ProcessSavedData.frameWidth, ProcessSavedData.frameHeight), true));
                    } else {
                        System.out.println("client " + clientId + " vào không phải là lần đầu!");
                    }
                } catch (Exception e) {
                    System.out.println("Lỗi tạo videoWriter " + e.getMessage());
                }
            } else if (role.equals("teacher")) {
                ++countConnected;
                DatagramSocket receiveSocket = new DatagramSocket(serverPort + countConnected);
                DatagramSocket sendSocket = new DatagramSocket(serverPort + 50 + countConnected);
                ProcessGetData processGetData = new ProcessGetData(receiveSocket,sendSocket,packet,code,clientId,countConnected);
                ProcessSendData processSendData = new ProcessSendData(processGetData);
                ProcessSavedData processSavedData = new ProcessSavedData(processGetData);
                ServerKeylogger serverKeylogger = new ServerKeylogger(processGetData);
                UdpHandler.sendNumber(serverSocket,countConnected,address,port);
                System.out.println("Trả về số cổng thành công");
                SocketServer.listMeeting.put(code, processGetData);
                FileHandler.checkAndCreateFolder(videoPath + "\\_" +code);
                FileHandler.checkAndCreateFolder(keyboardPath + "\\_" + code);
                new Thread(processGetData).start();
                new Thread(processSendData).start();
                new Thread(processSavedData).start();
                new Thread(serverKeylogger).start();
            } else {

            }

        } catch (Exception e) {
            System.err.println("Error in handleClient: " + e.getMessage());
        }
    }
}
