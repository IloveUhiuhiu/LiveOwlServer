package com.server.liveowl.socket;
import com.server.liveowl.util.UdpHandler;
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

    private final static Logger audit = Logger.getLogger("requests");
    private final static Logger errors = Logger.getLogger("errors");
    public void run() {
        try {
            serverSocket = new DatagramSocket(serverPort);
            int countConnect = 0;
            int countStudents = 0;
            while (true) {
                System.out.println("Server đang lắng nghe ...");
                DatagramPacket packet = UdpHandler.getPacket(serverSocket);
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
                    SocketServer.listMeeting.get(code).addStudent(countStudents, packet);
                    UdpHandler.sendNumber(serverSocket,countStudents,address,port);
                    UdpHandler.sendNumber(serverSocket,SocketServer.listMeeting.get(code).numberOfProcess,address,port);
                    ++countStudents;
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
                    ProcessSendData processSendData = new ProcessSendData(packet);
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

