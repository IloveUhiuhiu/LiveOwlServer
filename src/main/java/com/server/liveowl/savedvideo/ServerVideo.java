package com.server.liveowl.savedvideo;
import com.server.liveowl.util.UdpHandler;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class ServerVideo implements Runnable {
    public static int NUM_OF_THREAD = 10;
    private ExecutorService executor = Executors.newFixedThreadPool(NUM_OF_THREAD);
    private static int countConnected = 0;
    private String serverName = "localhost";
    private int serverPort = 9512;
    private DatagramSocket serverSocket;
    public void run() {
        try {
            serverSocket = new DatagramSocket(serverPort);
            while (true) {
                System.out.println("Server video đang lắng nghe ...");
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
            String code = connect.split(":")[1];
            System.out.println(clientId + " " + code);
            ++countConnected;
            UdpHandler.sendNumber(serverSocket,countConnected,packet.getAddress(),packet.getPort());
            new Thread(new SendVideo(packet,code,clientId,countConnected)).start();
        } catch (IOException e) {
            System.err.println("Error handling client: " + e.getMessage());
        }
    }
}
