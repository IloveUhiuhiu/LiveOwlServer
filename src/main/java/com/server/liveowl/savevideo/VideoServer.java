package com.server.liveowl.savevideo;
import com.server.liveowl.util.UdpHandler;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import static com.server.liveowl.ServerConfig.*;

public class VideoServer implements Runnable {

    private ExecutorService executor = Executors.newFixedThreadPool(NUM_OF_THREAD);
    private static int numberOfConnect = 0;
    private DatagramSocket serverSocket;
    public void run() {
        try {
            serverSocket = new DatagramSocket(SERVER_VIDEO_PORT);
            while (true) {
                System.out.println("VideoServer video đang lắng nghe trên cổng " + SERVER_VIDEO_PORT);
                DatagramPacket packet = UdpHandler.getPacket(serverSocket);
                String connect = new String(packet.getData(), 0, packet.getLength());
                executor.execute(() -> handleClient(packet, serverSocket, connect));
            }
        } catch (IOException e) {
            System.err.println("Lỗi server: " + e.getMessage());
        } finally {
            if (serverSocket != null) serverSocket.close();
            executor.shutdown();
        }
    }


    private static void handleClient(DatagramPacket packet, DatagramSocket serverSocket, String connect) {
        try {
            String clientId = connect.split(":")[0];
            String code = connect.split(":")[1];
            System.out.println(clientId + " " + code);
            ++numberOfConnect;
            UdpHandler.sendNumber(serverSocket,numberOfConnect,packet.getAddress(),packet.getPort());
            new Thread(new ProcessSendVideo(packet,code,clientId,numberOfConnect)).start();
        } catch (IOException e) {
            System.err.println("Lỗi trong khi xử lý client: " + e.getMessage());
        }
    }
}
