package com.server.liveowl.socket;
import static com.server.liveowl.ServerConfig.*;
import com.server.liveowl.dto.ImageDTO;
import com.server.liveowl.payload.request.AddResultRequest;
import com.server.liveowl.util.ResultHandler;
import com.server.liveowl.util.UdpHandler;
import org.opencv.videoio.VideoWriter;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;


public class ProcessGetData implements Runnable {
    private volatile boolean running = true;
    public final int processId;
    private final DatagramSocket receiveSocket;
    private final DatagramSocket sendSocket;
    public final InetAddress addressTeacher;
    public final int portTeacher;
    private final String code;
    private final String examId;
    public Map<String, Integer> portStudents = new HashMap<>();
    public Map<String, InetAddress> addressStudents = new HashMap<>();
    public Map<String, byte[]> imageBuffer = new HashMap<>();
    public ConcurrentLinkedQueue<String> queueSendIds = new ConcurrentLinkedQueue<>();
    public ConcurrentLinkedQueue<ImageDTO> queueSavedImages = new ConcurrentLinkedQueue<>();
    public Set<String> listClientIds = new HashSet<>();
    public Map<String, VideoWriter> videoWriters = new HashMap<>();
    public String getCode()
    {
        return this.code;
    }
    public ProcessGetData(DatagramSocket receiveSocket, DatagramSocket sendSocket, DatagramPacket thePacket, String code,String examId, int processId) throws IOException {
        this.receiveSocket = receiveSocket;
        this.sendSocket = sendSocket;
        this.portTeacher = thePacket.getPort();
        this.addressTeacher = thePacket.getAddress();
        this.code = code;
        this.examId = examId;
        this.processId = processId;
    }
    public void addStudent(String clientId, DatagramPacket thePacket) {
        portStudents.put(clientId, thePacket.getPort());
        addressStudents.put(clientId, thePacket.getAddress());
        listClientIds.add(clientId);
    }
    public synchronized boolean isRunning() {
        return running;
    }

    public synchronized void setRunning(boolean running) {
        this.running = running;
    }

    public void run() {

        try {
            while(isRunning()) {
                byte[] message = new byte[maxDatagramPacketLength];
                UdpHandler.receiveBytesArr(receiveSocket,message);
                processPacket(message);
            }
        } catch (Exception e) {
            System.err.println("Error in ProcessGetData: " + e.getMessage());
        } finally {
            cleanupResources();
            System.out.println("Close thread ProcessGetData");
        }
    }
    private void processPacket(byte[] message) throws IOException {
        int packetType = (message[0] & 0xff);
        switch (packetType) {
            case 0:
                handlePacketLength(message);
                break;
            case 1:
                handlePacketImage(message);
                break;
            case 2:
                handleCameraRequest(message);
                break;
            case 3:
                handleTeacherExit(message);
                break;
            case 4:
                handleStudentExit(message);
                break;
            default:
                System.err.println("Unknown packet type: " + packetType);
        }
    }
    private void handlePacketLength(byte[] message) {
        String clientId = new String(message,1,8);
        int imageId = (message[9] & 0xff);
        int lengthOfImage = (message[10] & 0xff) << 16 | (message[11] & 0xff) << 8 | (message[12] & 0xff);
        byte[] imageBytes = new byte[lengthOfImage];
        String Key = imageId + ":" + clientId;
        imageBuffer.put(Key, imageBytes);
    }
    private void handlePacketImage(byte[] message) {
        String clientId = new String(message,1,8);
        int packetId = (message[9] & 0xff);
        int sequenceNumber = (message[10] & 0xff);
        boolean isLastPacket = ((message[11] & 0xff) == 1);
        int destinationIndex = (sequenceNumber - 1) * (maxDatagramPacketLength - 12);
        String Key = packetId + ":" + clientId;
        if (imageBuffer.containsKey(Key)) {
            int lengthOfImage = imageBuffer.get(Key).length;
            byte[] imageBytes = imageBuffer.get(Key);
            if (destinationIndex >= 0 && destinationIndex < lengthOfImage) {
                if (!isLastPacket && (destinationIndex + (maxDatagramPacketLength - 12) < lengthOfImage)) {
                    System.arraycopy(message, 12, imageBytes, destinationIndex, maxDatagramPacketLength - 12);
                } else {
                    System.arraycopy(message, 12, imageBytes, destinationIndex, lengthOfImage % (maxDatagramPacketLength - 12));
                }
                if (isLastPacket) {
                    queueSendIds.add(Key);
                    queueSavedImages.add(new ImageDTO(clientId, imageBytes.clone()));
                }
            } else {
                System.err.println("Error destinationIndex: " + destinationIndex + ", lengthOfimage" + lengthOfImage);
            }
        } else {
            System.err.println("Not found" + Key + " in imageBuffer!");
        }
    }
    private void handleCameraRequest(byte[] message) throws IOException {
        String clientId = new String(message,1,8);
        System.out.println("request camera " + clientId);
        int port = portStudents.get(clientId) - 1000;
        InetAddress address = addressStudents.get(clientId);
        UdpHandler.sendRequests(sendSocket, "camera".getBytes(), address, port);
    }
    private void handleTeacherExit(byte[] message) throws IOException {
        String token = new String(message,1,message.length-1);
        for (String key : portStudents.keySet()) {
            System.out.println(addressStudents.get(key) + ", " + portStudents.get(key));
            UdpHandler.sendRequests(sendSocket,"exit".getBytes(), addressStudents.get(key), portStudents.get(key) - 1000);
        }
        addResult(token);
        handleTeacherDisconnect();
    }
    private void handleStudentExit(byte[] message) throws IOException {
        String clientId = new String(message,1,8);
        byte[] numberBytes = new byte[9];
        numberBytes[0] = (byte) (4);
        System.arraycopy(clientId.getBytes(), 0, numberBytes, 1, 8);
        UdpHandler.sendRequests(sendSocket,numberBytes,addressTeacher, portTeacher);
        handleStudentDisconnect(clientId);
    }
    private void cleanupResources() {
        try {
            if (receiveSocket != null) receiveSocket.close();
            if (sendSocket != null) sendSocket.close();
            if (imageBuffer != null)imageBuffer.clear();
            if (portStudents != null) portStudents.clear();
            if (addressStudents != null )addressStudents.clear();
            if (SocketServer.listMeeting.containsKey(code))SocketServer.listMeeting.remove(code);
            setRunning(false);
        } catch (Exception e) {
            System.err.println("Error closing resources: " + e.getMessage());
        }


    }
    private void handleStudentDisconnect(String clientId) {
        new Thread(() -> {
            try {
                Thread.sleep(2000);
                portStudents.remove(clientId);
                addressStudents.remove(clientId);
                Iterator<String> iterator = imageBuffer.keySet().iterator();
                while (iterator.hasNext()) {
                    String key = iterator.next();
                    String clientIdTmp = key.substring(key.indexOf(":") + 1);
                    if (clientIdTmp.equals(clientId)) {
                        System.out.println("Đã remove nhé");
                        iterator.remove();
                    }
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
        }).start();
    }
    private void handleTeacherDisconnect() {
        new Thread(() -> {
            try {
                Thread.sleep(2000);
                videoWriters.forEach((integer, videoWriter) -> {
                    videoWriter.release();
                    System.out.println("Đã giải phóng VideoWriter cho clientId: " + integer);
                });
                cleanupResources();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
        }).start();
    }
    private void addResult(String token) {

            List<String> studentId = new ArrayList<>();
            List<String> linkVideo = new ArrayList<>();
            List<String> linkKeyBoard = new ArrayList<>();
            for (String Id: listClientIds) {
                System.out.println(Id);
                System.out.println(videoPath + "\\_" +code + "\\video_" + Id + ".mp4");
                System.out.println(keyboardPath + "\\_" +code + "\\keyboard_" + Id + ".txt");
                studentId.add(Id);
                linkVideo.add(videoPath + "\\_" +code + "\\video_" + Id + ".mp4");
                linkKeyBoard.add(keyboardPath + "\\_" +code + "\\keyboard_" + Id + ".txt");
            }
            AddResultRequest request = new AddResultRequest(linkVideo,linkKeyBoard,studentId,examId);
            ResultHandler.addresult(request, token);
            System.out.println("Add Thanh Cong");




    }


}


