package com.server.liveowl.socket;

import com.server.liveowl.util.UdpHandler;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.logging.Logger;

import static com.server.liveowl.socket.SocketServer.maxDatagramPacketLength;
import static com.server.liveowl.socket.SocketServer.period;

public class SocketServer implements Runnable {
    public static int maxDatagramPacketLength = 1500;
    private static final int serverPort = 9000;
    public static DatagramSocket serverSocket = null;
    public static Map<String, ProcessGetData> teachers = new HashMap<>();
    public static Queue<String> sendList = new LinkedList<>();
    public static Map<String, byte[]> buffer = new HashMap<>();
    public static Map<String, Integer> numberBuffer = new HashMap<>();
    public static int imageCount = 0;
    public static int period = 0;
    private final static Logger audit = Logger.getLogger("requests");
    private final static Logger errors = Logger.getLogger("errors");
    public static String packetId = null;
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
                    if (!SocketServer.teachers.containsKey(code)) {
                        System.out.println("Student gửi mã không có trong map");
                        UdpHandler.sendMsg(serverSocket,"fail",address,port);
                        continue;
                    } else {
                        UdpHandler.sendMsg(serverSocket,"success",address,port);
                    }

                    SocketServer.teachers.get(code).addStudent(countStudents, packet);
                    UdpHandler.sendNumber(serverSocket,countStudents,address,port);
                    UdpHandler.sendNumber(serverSocket,SocketServer.teachers.get(code).numberOfProcess,address,port);
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
                    ProcessGetData theTeacher = new ProcessGetData(clientSocket,clientSocket2,packet,code,countConnect);
                    ProcessSendData theTecher = new ProcessSendData(packet.getAddress(), packet.getPort());
                    UdpHandler.sendNumber(serverSocket,countConnect,address,port);
                    System.out.println("Trả về số cổng thành công");
                    SocketServer.teachers.put(code, theTeacher);
                    new Thread(theTeacher).start();
                    new Thread(theTecher).start();
                } else {

                }
            }
        } catch (IOException e) {
            System.err.println("Lỗi server: " + e.getMessage());
        }
    }


}


class ProcessGetData implements Runnable {
    public static int numberOfProcess;
    private final DatagramSocket theSocket;
    private final DatagramSocket theSocket2;
    public InetAddress addressTeacher;
    public int portTeacher;
    private final String code;
    Map<Integer, Integer> portStudents = new HashMap<>();
    Map<Integer, InetAddress> addressStudents = new HashMap<>();
    Map<Integer, Boolean> disconnect = new HashMap<>();

    public ProcessGetData(DatagramSocket theSocket,DatagramSocket theSocket2,DatagramPacket thePacket, String code, int numberOfProcess) throws IOException {
        this.theSocket = theSocket;
        this.theSocket2 = theSocket2;
        this.portTeacher = thePacket.getPort();
        this.addressTeacher = thePacket.getAddress();
        this.code = code;
        this.numberOfProcess = numberOfProcess;
    }
    public void addStudent(int numerOfStudent, DatagramPacket thPacket) {
        portStudents.put(numerOfStudent, thPacket.getPort());
        addressStudents.put(numerOfStudent, thPacket.getAddress());
    }

    public void run() {

        try {

            while(true) {
                byte[] message = new byte[maxDatagramPacketLength];
                UdpHandler.receiveBytesArr(theSocket,message);
                int packetType = (message[0] & 0xff);

                if (packetType == 0) {
                    // Nhận được packet là LENGTH
                    int clientId = (message[1] & 0xff);
                    if (disconnect.containsKey(clientId)) {
                        continue;
                    }
                    int imageId = (message[2] & 0xff);
                    int lengthOfImage = (message[3] & 0xff) << 16 | (message[4] & 0xff) << 8 | (message[5] & 0xff);
                    //System.out.println("Nhận packet có length = " + lengthOfImage);
                    int numberOfPacket = message[6] & 0xff;
                    byte[] imageBytes = new byte[lengthOfImage];
                    String Key = imageId + ":" + clientId;
                    SocketServer.buffer.put(Key, imageBytes);
                    SocketServer.numberBuffer.put(Key,numberOfPacket);

                } else if (packetType == 1){
                    int clientId = (message[1] & 0xff);
                    if (disconnect.containsKey(clientId)) {
                        continue;
                    }
                    int packetId = (message[2] & 0xff);
                    int sequenceNumber = (message[3] & 0xff);
                    boolean isLastPacket = ((message[4] & 0xff) == 1);
                    int destinationIndex = (sequenceNumber - 1) * (maxDatagramPacketLength - 5);
                    String Key = packetId + ":" + clientId;
                    //System.out.println(sequenceNumber + ", " + isLastPacket + ", " +packetId+ ", " + clientId);
                    if (SocketServer.buffer.containsKey(Key)) {
                        int lengthOfImage = SocketServer.buffer.get(Key).length;
                        byte[] imageBytes = SocketServer.buffer.get(Key);
                        SocketServer.numberBuffer.put(Key, SocketServer.numberBuffer.get(Key) - 1) ;
                        //System.out.println("Nhân ảnh lengthOfImage = " + lengthOfImage +", destinationIndex = " + destinationIndex + "isLastPacket = " + isLastPacket);
                        if (destinationIndex >= 0 && destinationIndex < lengthOfImage) {
                            if (!isLastPacket && (destinationIndex + (maxDatagramPacketLength - 5) < lengthOfImage)) {
                                System.arraycopy(message, 5, imageBytes, destinationIndex, maxDatagramPacketLength - 5);
                            } else {
                                System.arraycopy(message, 5, imageBytes, destinationIndex, lengthOfImage % (maxDatagramPacketLength - 5));
                                //sendImageForTeacher(buffer.get(Key), clientId, packetId);
                            }
                            //System.out.println("Nhận ảnh" + Key + ": có số lượng paket còn lại " + SocketServer.numberBuffer.get(Key));
                            if (SocketServer.numberBuffer.get(Key) == 0) {
                                //System.out.println("Push vào Queue");
                                SocketServer.sendList.add(Key);
                            }
                            //System.out.println("Nhận thành công packet thứ " + sequenceNumber);
                        } else {
                            System.out.println("Chỉ số đích không hợp lệ: " + destinationIndex + ", lengthOfimage" + lengthOfImage);
                        }
                    } else {
                        System.out.println("Lỗi ID_Paket bị xóa khỏi buffer!");
                    }
                } else if (packetType == 2) {
                    try {
                        int clientId = (message[1] & 0xff);
                        int port = portStudents.get(clientId) - 1000;

                        InetAddress address = addressStudents.get(clientId);
                        //System.out.println("bạn " + clientId + " với " + address.toString() + ", " + port + "muốn camera");
                        sendRequestCameraForStudent("camera".getBytes(), address, port);
                    } catch (IOException e) {
                        System.out.println(e.getMessage());
                    }
                } else if (packetType == 3) {
                    System.out.println("send exit to student");
                    for (int key : portStudents.keySet()) {
                        System.out.println(addressStudents.get(key) + ", " + portStudents.get(key));
                        sendRequestExitForStudent("exit".getBytes(), addressStudents.get(key), portStudents.get(key) - 1000);
                    }
                    try {
                        if (theSocket != null && !theSocket.isClosed()) {
                            theSocket.close();
                        }
                        if (theSocket2 != null && !theSocket2.isClosed()) {
                            theSocket2.close();
                        }
                    } catch (Exception ex) {
                        System.out.println("Lỗi khi đóng socket: " + ex.getMessage());
                    }
                    SocketServer.buffer.clear(); // Nếu bạn sử dụng một buffer để lưu trữ dữ liệu
                    portStudents.clear(); // Xóa danh sách cổng của học sinh
                    addressStudents.clear(); // Xóa danh sách địa chỉ của học sinh
                    SocketServer.teachers.remove(code);
                } else if (packetType == 4) {
                    System.out.println("send exit to teacher");
                    int clientId = (message[1] & 0xff);
                    byte[] numberBytes = new byte[2];
                    numberBytes[0] = (byte) (4);
                    numberBytes[1] = (byte) (clientId);
                    System.out.println("ID học sinh" + clientId);
                    disconnect.put(clientId,true);
                    portStudents.remove(clientId);
                    addressStudents.remove(clientId);
                    System.out.println("remove address và port thành công");
                    Iterator<String> iterator = SocketServer.buffer.keySet().iterator();
                    while (iterator.hasNext()) {
                        String key = iterator.next();
                        String ID = key.substring(key.indexOf(":") + 1);

                        //System.out.println(ID + ", " + clientId);

                        if (Integer.parseInt(ID) == clientId) {
                            iterator.remove(); // Xóa an toàn
                        }
                    }
                    System.out.println("remove từ buffer");
                    sendRequestExitForTeacher(numberBytes,addressTeacher, portTeacher);
                }

            }

        } catch (Exception e) {
            System.err.println(e.getMessage());
        } finally {
            try {
                if (theSocket != null ) theSocket.close();
                if (theSocket2 != null) theSocket2.close();
            } catch (Exception e) {

            }
        }
    }
    public synchronized void sendRequestExitForStudent(byte[] imageByteArray,InetAddress address, int port) throws IOException {
        DatagramPacket packet = new DatagramPacket(imageByteArray, imageByteArray.length, address, port);
        theSocket2.send(packet);
    }
    public synchronized void sendRequestExitForTeacher(byte[] imageByteArray,InetAddress address, int port) throws IOException {
        DatagramPacket packet = new DatagramPacket(imageByteArray, imageByteArray.length, address, port);
        theSocket2.send(packet);
    }
    public synchronized void sendRequestCameraForStudent(byte[] imageByteArray,InetAddress address,int port) throws IOException {
        DatagramPacket packet = new DatagramPacket(imageByteArray, imageByteArray.length, address, port);
        theSocket2.send(packet);
    }



}

class ProcessSendData implements Runnable {
    DatagramSocket socket;
    public InetAddress addressTeacher;
    public int portTeacher;
    ProcessSendData(InetAddress addr, int port) throws SocketException {
        socket = new DatagramSocket(9080);
        addressTeacher = addr;
        portTeacher = port;
    }
    @Override
    public void run() {
        while(true) {
            try {
                //System.out.println("Gửi ảnh");
                String packetId = SocketServer.sendList.poll();
                //System.out.println("gửi packetid = " + packetId);
                if (packetId != null) {
                    byte[] imageByteArray = SocketServer.buffer.get(packetId);
                    int pos = packetId.lastIndexOf(":");
                    int imageId = Integer.parseInt(packetId.substring(0, pos));
                    int clientId = Integer.parseInt(packetId.substring(pos + 1));
                    //System.out.println(packetId + ", " + imageId + ", " + clientId);
                    int sequenceNumber = 0; // For order
                    boolean flag; // To see if we got to the end of the file
                    int length = imageByteArray.length;
                    byte[] lengthBytes = new byte[maxDatagramPacketLength];
                    // Lưu độ dài vào mảng byte
                    lengthBytes[0] = (byte) 0;
                    lengthBytes[1] = (byte) (clientId);
                    lengthBytes[2] = (byte) (imageId);
                    lengthBytes[3] = (byte) (length >> 16);
                    lengthBytes[4] = (byte) (length >> 8);
                    lengthBytes[5] = (byte) (length);
                    lengthBytes[6] = (byte) ((length + maxDatagramPacketLength - 6) / (maxDatagramPacketLength - 5));
                    UdpHandler.sendBytesArray(socket, lengthBytes, addressTeacher, portTeacher);
                    for (int i = 0; i < length; i = i + maxDatagramPacketLength - 5) {
                        sequenceNumber += 1;
                        // Create message
                        byte[] message = new byte[maxDatagramPacketLength];
                        message[0] = (byte) (1);
                        message[1] = (byte) (clientId);
                        message[2] = (byte) (imageId);
                        message[3] = (byte) (sequenceNumber);
                        if ((i + maxDatagramPacketLength - 5) >= imageByteArray.length) {
                            flag = true;
                            message[4] = (byte) (1);
                        } else {
                            flag = false;
                            message[4] = (byte) (0);
                        }

                        if (!flag) {
                            System.arraycopy(imageByteArray, i, message, 5, maxDatagramPacketLength - 5);
                        } else {
                            System.arraycopy(imageByteArray, i, message, 5, length - i);
                        }
                        UdpHandler.sendBytesArray(socket, message, addressTeacher, portTeacher);
                        ++period;
                        if (period == 20) {
                            Thread.sleep(0,1);
                            period = 0;
                        }
                    }

                    ++SocketServer.imageCount;
                    System.out.println("Gửi thành công ảnh thứ: " + SocketServer.imageCount + "length = " + length);
                }

            } catch (Exception e) {
                System.out.println("Loi khi gui anh");
            }
        }
    }
}

