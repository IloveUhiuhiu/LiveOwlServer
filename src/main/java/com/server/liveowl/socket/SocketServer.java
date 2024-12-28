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
    public static DatagramSocket serverSocket = null;
    public static Map<String, ProcessGetImage> listMeeting = new HashMap<>();
    private final static Logger audit = Logger.getLogger("requests");
    private final static Logger errors = Logger.getLogger("errors");
    private ExecutorService executor = Executors.newFixedThreadPool(NUM_OF_THREAD);
    public void run() {
        try {
            serverSocket = new DatagramSocket(SERVER_PORT);
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
            if (role.equals("student")) {
                System.out.println("Student gửi mã: " + code);
                if (!SocketServer.listMeeting.containsKey(code)) {
                    System.out.println("Student gửi mã không có trong map");
                    UdpHandler.sendMsg(serverSocket,"fail",address,port);
                } else {
                    UdpHandler.sendMsg(serverSocket,"success",address,port);
                }
                ProcessGetImage processGetData = listMeeting.get(code);
                processGetData.addStudent(clientId, packet);
                UdpHandler.sendNumber(serverSocket,processGetData.processId,address,port);

            } else if (role.equals("teacher")) {
                ++numberOfConnect;
                ProcessGetImage processGetData = new ProcessGetImage(packet,code,clientId,numberOfConnect);
                ProcessSendImage processSendData = new ProcessSendImage(processGetData);
                ProcessSaveImage processSavedData = new ProcessSaveImage(processGetData);
                ProcessGetKey serverKeylogger = new ProcessGetKey(processGetData);

                UdpHandler.sendNumber(serverSocket,numberOfConnect,address,port);
                System.out.println("Trả về số cổng thành công");
                SocketServer.listMeeting.put(code, processGetData);
                FileHandler.checkAndCreateFolder(videoPath + "\\_" +code);
                FileHandler.checkAndCreateFolder(keyboardPath + "\\_" + code);

                new Thread(processGetData).start();
                new Thread(processSendData).start();
                new Thread(processSavedData).start();
                new Thread(serverKeylogger).start();
            }

        } catch (Exception e) {
            System.err.println("Error in handleClient: " + e.getMessage());
        }
    }
}
