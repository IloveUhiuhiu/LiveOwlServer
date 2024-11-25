package com.server.liveowl.socket;
import com.server.liveowl.dto.ImageDTO;
import com.server.liveowl.savedvideo.SavedVideo;
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
import java.util.logging.Logger;

public class SocketServer implements Runnable {

    public static int imageCount = 0;
    private static final int serverPort = 9000;
    public static int maxDatagramPacketLength = 1500;
    public static DatagramSocket serverSocket = null;
    public static Map<String, byte[]> imageBuffer = new HashMap<>();
    //public static Map<String, Integer> numberBuffer = new HashMap<>();
    public static Map<String, ProcessGetData> listMeeting = new HashMap<>();
    public static ConcurrentLinkedQueue<String> listIds = new ConcurrentLinkedQueue<>();
    public static ConcurrentLinkedQueue<ImageDTO> savedImages = new ConcurrentLinkedQueue<>();
    public static Map<String, VideoWriter> videoWriters = new HashMap<>();
    private final static Logger audit = Logger.getLogger("requests");
    private final static Logger errors = Logger.getLogger("errors");

    public void run() {
        try {
            serverSocket = new DatagramSocket(serverPort);
            int countConnect = 0;
            while (true) {
                System.out.println("Server đang lắng nghe ...");
                DatagramPacket packet = UdpHandler.getPacket(serverSocket);
                // Client Id đây nha
                String clientId = new String(packet.getData(), 0, packet.getLength());
                System.out.println("User có Id kết nối " + clientId);
                InetAddress address = packet.getAddress();
                int port = packet.getPort();
                System.out.println(address.toString() +", " + port +  " kết nối!");

                String role = UdpHandler.receiveMsg(serverSocket,address,port);
                System.out.println("Role là : " + role);
                if (role.equals("student")) {
                    String code = UdpHandler.receiveMsg(serverSocket,address,port);
                    System.out.println("Student gửi mã: " + code);
                    if (!SocketServer.listMeeting.containsKey(code)) {
                        System.out.println("Student gửi mã không có trong map");
                        UdpHandler.sendMsg(serverSocket,"fail",address,port);
                        continue;
                    } else {
                        UdpHandler.sendMsg(serverSocket,"success",address,port);
                    }

                    SocketServer.listMeeting.get(code).addStudent(clientId, packet);
                    //System.out.println("Xong listMeeting");
                    System.out.println(serverPort +", " + SocketServer.listMeeting.get(code).numberOfProcess + ", " + address.toString() + ", " + port);
                    UdpHandler.sendNumber(serverSocket,SocketServer.listMeeting.get(code).numberOfProcess,address,port);
                    //System.out.println("Xong sendNumber");
                    try {
                        if (!videoWriters.containsKey(clientId)) {
                            videoWriters.put(clientId,new VideoWriter(SavedVideo.outputFilePath + "video_" + code + "_" + clientId +".mp4",
                                    VideoWriter.fourcc('H', '2', '6', '4'),SavedVideo.fps,
                                    new org.opencv.core.Size(SavedVideo.frameWidth, SavedVideo.frameHeight), true));
                        } else {
                            System.out.println("client " + clientId + " vào lần tiếp theo!");
                        }
                    } catch (Exception e) {
                        System.out.println("Lỗi lưu video " + e.getMessage());
                    }
                    System.out.println("add " + SavedVideo.outputFilePath + "video_" + code + "_" + clientId +".mp4");
                    System.out.println("Trả về số cổng thành công");
                } else if (role.equals("teacher")) {
                    // Lấy mã cuộc họp
                    System.out.println("Teacher");
                    String code = UdpHandler.receiveMsg(serverSocket,address,port);
                    System.out.println("Teach tạo mã: " + code);
                    ++countConnect;
                    DatagramSocket clientSocket = new DatagramSocket(serverPort + countConnect);
                    DatagramSocket clientSocket2 = new DatagramSocket(serverPort + 50 + countConnect);
                    ProcessGetData processGetData = new ProcessGetData(clientSocket,clientSocket2,packet,code,countConnect);
                    ProcessSendData processSendData = new ProcessSendData(packet,countConnect);
                    UdpHandler.sendNumber(serverSocket,countConnect,address,port);
                    System.out.println("Trả về số cổng thành công");
                    SocketServer.listMeeting.put(code, processGetData);
                    new Thread(processGetData).start();
                    new Thread(processSendData).start();
                } else {

                }
            }
        } catch (IOException e) {
            System.err.println("Lỗi server: " + e.getMessage());
        }
    }
}

