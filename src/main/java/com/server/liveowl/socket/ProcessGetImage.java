package com.server.liveowl.socket;
import static com.server.liveowl.ServerConfig.*;
import com.server.liveowl.dto.ImageDTO;
import com.server.liveowl.payload.request.AddResultRequest;
import com.server.liveowl.util.ResultHandler;
import com.server.liveowl.util.UdpHandler;

import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.videoio.VideoCapture;
import org.opencv.videoio.VideoWriter;
import org.opencv.videoio.Videoio;


import java.io.File;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;


class ProcessGetImage implements Runnable {
    private volatile boolean running = true;// biến đánh dấu cuộc họp còn đang live hay đã kết thúc
    private final int processId;
    private final String code;
    private final String examId;

    private final DatagramSocket receiveSocket;
    private final DatagramSocket sendSocket;
    public final InetAddress addressTeacher;
    public final int portTeacher;

    public Map<String, Integer> portStudents = new ConcurrentHashMap<>();
    public Map<String, InetAddress> addressStudents = new ConcurrentHashMap<>();

    private Set<String> listClientId = new HashSet<>();
    public Map<String, byte[]> imageBuffer = new ConcurrentHashMap<>();
    public Map<String, VideoWriter> videoWriters = new ConcurrentHashMap<>();
    public ConcurrentLinkedQueue<ImageDTO> queueSendImage = new ConcurrentLinkedQueue<>();
    public ConcurrentLinkedQueue<ImageDTO> queueSaveImage = new ConcurrentLinkedQueue<>();


    public ProcessGetImage(DatagramPacket thePacket, String code, String examId, int processId) throws IOException {
        this.receiveSocket = new DatagramSocket(SERVER_PORT + processId);
        this.sendSocket = new DatagramSocket(SERVER_PORT + 100 + processId);

        this.portTeacher = thePacket.getPort();
        this.addressTeacher = thePacket.getAddress();

        this.code = code;
        this.examId = examId;
        this.processId = processId;
    }
    public String getCode() {
        return this.code;
    }
    public int getProcessId() {
        return this.processId;
    }

    public void addStudent(String clientId, DatagramPacket thePacket) {
        portStudents.put(clientId, thePacket.getPort());// thêm port vảo danh sách quản lý
        addressStudents.put(clientId, thePacket.getAddress());// thêm địa chỉ vào danh sách quản lý

        listClientId.add(clientId);// thêm vào danh sách Id để quản lý
        try {
            if (!videoWriters.containsKey(clientId)) {
                videoWriters.put(clientId,new VideoWriter(VIDEO_PATH + "\\_" +code + "\\video_" + clientId +".mp4",
                        VideoWriter.fourcc('H', '2', '6', '4'), ProcessSaveImage.fps,
                        new org.opencv.core.Size(ProcessSaveImage.frameWidth, ProcessSaveImage.frameHeight), true));
            } else {
                System.out.println("Học sinh có Id là " + clientId + " đã thoát ra và vào lại!");
            }
        } catch (Exception e) {
            System.out.println("Lỗi tạo videoWriter " + e.getMessage());
        }
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
                byte[] message = new byte[MAX_DATAGRAM_PACKET_LENGTH];
                UdpHandler.receiveBytesArr(receiveSocket,message);// Nhận packet này để xác định loại request
                processPacket(message);// xử lý packet
            }
        } catch (Exception e) {

        } finally {
            cleanupResources();
            System.out.println("Close thread ProcessGetData");
        }
    }
    private void processPacket(byte[] message) throws IOException {
        int packetType = (message[0] & 0xff);
        switch (packetType) {
            case 0:
                handlePacketLength(message);// trường hợp gửi độ dài ảnh
                break;
            case 1:
                handlePacketImage(message);// trường hợp gửi mảng byte của ảnh
                break;
            case 2:
                handleCameraRequest(message);// trường hợp gửi request bật/ tắt camera
                break;
            case 3:
                handleTeacherExit(message);// trường hợp gửi request hủy cuộc họp của gv
                break;
            case 4:
                handleStudentExit(message);// trường hợp gửi request thoát của hs
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
        int destinationIndex = (sequenceNumber - 1) * (MAX_DATAGRAM_PACKET_LENGTH - 12);
        String Key = packetId + ":" + clientId;
        if (imageBuffer.containsKey(Key)) {
            int lengthOfImage = imageBuffer.get(Key).length;
            byte[] imageBytes = imageBuffer.get(Key);
            if (destinationIndex >= 0 && destinationIndex < lengthOfImage) {
                if (!isLastPacket && (destinationIndex + (MAX_DATAGRAM_PACKET_LENGTH - 12) < lengthOfImage)) {
                    System.arraycopy(message, 12, imageBytes, destinationIndex, MAX_DATAGRAM_PACKET_LENGTH - 12);
                } else {
                    System.arraycopy(message, 12, imageBytes, destinationIndex, lengthOfImage % (MAX_DATAGRAM_PACKET_LENGTH - 12));
                }
                if (isLastPacket) {
                    // nếu mảng byte được đánh dấu là cuối cùng
                    // thì thêm vào queue để gửi và lưu
                    SocketServer.countImageKiemThu++;
                    queueSendImage.add(new ImageDTO(Key, imageBytes.clone()));
                    queueSaveImage.add(new ImageDTO(clientId, imageBytes.clone()));
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
        //System.out.println("request camera " + clientId);
        int port = portStudents.get(clientId) - 1000;
        InetAddress address = addressStudents.get(clientId);
        UdpHandler.sendRequests(sendSocket, "camera".getBytes(), address, port);
    }

    private void handleTeacherExit(byte[] message) throws IOException {
        String token = new String(message,1,message.length-1);
        for (String key : portStudents.keySet()) {
            //System.out.println(addressStudents.get(key) + ", " + portStudents.get(key));
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
            setRunning(false);
            if (receiveSocket != null) receiveSocket.close();
            if (sendSocket != null) sendSocket.close();
            if (imageBuffer != null)imageBuffer.clear();
            if (portStudents != null) portStudents.clear();
            if (addressStudents != null )addressStudents.clear();
            if (SocketServer.listMeeting.containsKey(code))SocketServer.listMeeting.remove(code);

        } catch (Exception e) {
            System.err.println("Error closing resources: " + e.getMessage());
        }


    }
    private void handleStudentDisconnect(String clientId) {
        // đợi 2s để packet đến hết
        // rồi bắt đầu xóa
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
                        iterator.remove();
                    }
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.out.println("Lỗi khi học sinh tắt kết nối + " + e.getMessage());
            }
        }).start();
    }

    private void handleTeacherDisconnect() {
        // đợi 2s để packet dến hết
        // rồi bắt đầu xóa
        new Thread(() -> {
            try {
                Thread.sleep(2000);
                videoWriters.forEach((integer, videoWriter) -> {
                    videoWriter.release();
                });
                cleanupResources();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.out.println("Lỗi khi giáo viên tắt kết nối + " + e.getMessage());
            }
        }).start();
    }

    private void addResult(String token) {

            List<String> studentId = new ArrayList<>();
            List<String> linkVideo = new ArrayList<>();
            List<String> linkKeyBoard = new ArrayList<>();
            for (String Id: listClientId) {
                studentId.add(Id);
                linkVideo.add(VIDEO_PATH + "\\_" +code + "\\video_" + Id + ".mp4");
                linkKeyBoard.add(KEYBOARD_PATH + "\\_" +code + "\\keyboard_" + Id + ".txt");
            }
            AddResultRequest request = new AddResultRequest(linkVideo,linkKeyBoard,studentId,examId);
            ResultHandler.addresult(request, token);
    }
}


