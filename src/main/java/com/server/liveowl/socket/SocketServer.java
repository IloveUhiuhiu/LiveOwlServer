package com.server.liveowl.socket;
import com.server.liveowl.dto.ImageDTO;
import com.server.liveowl.savedvideo.SavedVideo;
import com.server.liveowl.util.FileHandler;
import com.server.liveowl.util.UdpHandler;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.videoio.VideoWriter;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

public class SocketServer implements Runnable {
    public static int NUM_OF_THREAD = 10;
    public static int imageCount = 0;
    private static final int serverPort = 9000;
    public static int maxDatagramPacketLength = 1500;
    public static DatagramSocket serverSocket = null;
    public static Map<String, byte[]> imageBuffer = new HashMap<>();
    //public static Map<String, Integer> numberBuffer = new HashMap<>();
    public static Map<String, ProcessGetData> listMeeting = new HashMap<>();
    public static ConcurrentLinkedQueue<String> listIds = new ConcurrentLinkedQueue<>();
    public static ConcurrentLinkedQueue<ImageDTO> savedImages = new ConcurrentLinkedQueue<>();
    private final static Logger audit = Logger.getLogger("requests");
    private final static Logger errors = Logger.getLogger("errors");
    private ExecutorService executor = Executors.newFixedThreadPool(NUM_OF_THREAD);
    private static int countConnected = 0;
    public void run() {
        try {

            serverSocket = new DatagramSocket(serverPort);
            while (true) {
                System.out.println("Server đang lắng nghe ...");
                DatagramPacket packet = UdpHandler.getPacket(serverSocket);
                String connect = new String(packet.getData(), 0, packet.getLength());
                executor.execute(() -> handleClient(packet, serverSocket, connect));
            }
        } catch (IOException e) {
            System.err.println("Lỗi server: " + e.getMessage());
        } finally {
            if (serverSocket != null) serverSocket.close();
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
                Map<String, VideoWriter> videoWriters = SocketServer.listMeeting.get(code).videoWriters;
                SocketServer.listMeeting.get(code).addStudent(clientId, packet);
                UdpHandler.sendNumber(serverSocket,SocketServer.listMeeting.get(code).numberOfProcess,address,port);
                try {
                    if (!videoWriters.containsKey(clientId)) {
                        videoWriters.put(clientId,new VideoWriter(SavedVideo.outputFilePath + "\\_" +code + "\\video_" + clientId +".mp4",
                                VideoWriter.fourcc('H', '2', '6', '4'),SavedVideo.fps,
                                new org.opencv.core.Size(SavedVideo.frameWidth, SavedVideo.frameHeight), true));
                    } else {
                        System.out.println("client " + clientId + " vào lần tiếp theo!");
                    }
                } catch (Exception e) {
                    System.out.println("Lỗi lưu video " + e.getMessage());
                }
//                System.out.println("add " + SavedVideo.outputFilePath + "video_" + code + "_" + clientId +".mp4");
//                System.out.println("Trả về số cổng thành công");
            } else if (role.equals("teacher")) {
                ++countConnected;
                DatagramSocket clientSocket = new DatagramSocket(serverPort + countConnected);
                DatagramSocket clientSocket2 = new DatagramSocket(serverPort + 50 + countConnected);
                ProcessGetData processGetData = new ProcessGetData(clientSocket,clientSocket2,packet,code,countConnected);
                ProcessSendData processSendData = new ProcessSendData(packet,countConnected);
                UdpHandler.sendNumber(serverSocket,countConnected,address,port);
                System.out.println("Trả về số cổng thành công");
                SocketServer.listMeeting.put(code, processGetData);
                FileHandler.checkAndCreateFolder(SavedVideo.outputFilePath + "\\_" +code);
                new Thread(processGetData).start();
                new Thread(processSendData).start();
                new Thread(new SavedVideo(processGetData.videoWriters)).start();
            } else {

            }

        } catch (IOException e) {
            System.err.println("Error handling client: " + e.getMessage());
        }
    }
}
