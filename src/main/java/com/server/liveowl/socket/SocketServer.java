package com.server.liveowl.socket;

import java.io.*;
import java.net.*;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Logger;

public class SocketServer implements Runnable {
    private static final int serverPort = 9000;
    public static DatagramSocket serverSocket = null;
    public static Map<String, ServerProcess> teachers = new HashMap<>();
    public static int maxDatagramPacketLength = 32768;
    public static int imageCount = 0;
    private final static Logger audit = Logger.getLogger("requests");
    private final static Logger errors = Logger.getLogger("errors");

    public void sendBytesArray(byte[] messageBytes,InetAddress address, int port) throws IOException {
        DatagramPacket packet = new DatagramPacket(messageBytes, messageBytes.length, address, port);
        serverSocket.send(packet);
    }
    public DatagramPacket getPacket() {
        byte[] message = new byte[1024];
        DatagramPacket datagramPacket = new DatagramPacket(message, message.length);
        try {
            serverSocket.receive(datagramPacket);
        } catch (Exception e) {
            System.err.println("Lỗi trong khi lấy DatagramPacket" + e.getMessage());
        }
        return datagramPacket;
    }
    public String receiveMsg() {
        DatagramPacket receivePacket = getPacket();
        return new String(receivePacket.getData(),0,receivePacket.getLength());
    }
    public void sendMsg(String message, InetAddress address, int port) throws IOException {
        sendBytesArray(message.getBytes(),address,port);
    }
    public void sendNumberPort(int number, InetAddress address, int port) throws IOException {
        byte[] numberPort = new byte[1];
        numberPort[0] = (byte) number;
        sendBytesArray(numberPort,address,port);
    }
    public void run() {
        try {
            serverSocket = new DatagramSocket(serverPort);
            int countConnect = 0;
            int countStudents = 0;
            while (true) {
                System.out.println("Server đang lắng nghe ...");
                DatagramPacket packet = getPacket();
                InetAddress address = packet.getAddress();
                int port = packet.getPort();
                System.out.println(address.toString() +", " + port +  " kết nối!");

                String role = receiveMsg();
                System.out.println("Role là : " + role);
                if (role.equals("student")) {
                    String code = receiveMsg();
                    System.out.println("Student gửi mã: " + code);
                    if (!SocketServer.teachers.containsKey(code)) {
                        System.out.println("Student gửi mã không có trong map");
                        sendMsg("fail",address,port);
                        continue;
                    } else {
                        sendMsg("success",address,port);
                    }

                    SocketServer.teachers.get(code).addStudent(countStudents, packet);
                    sendNumberPort(countStudents,address,port);
                    sendNumberPort(SocketServer.teachers.get(code).numberOfProcess,address,port);
                    ++countStudents;
                    System.out.println("Trả về số cổng thành công");
                } else if (role.equals("teacher")) {
                    // Lấy mã cuộc họp
                    System.out.println("Teacher");
                    String code = receiveMsg();
                    System.out.println("Teach tạo mã: " + code);
                    ++countConnect;
                    DatagramSocket clientSocket = new DatagramSocket(serverPort + countConnect);
                    DatagramSocket clientSocket2 = new DatagramSocket(serverPort + 50 + countConnect);
                    ServerProcess theTeacher = new ServerProcess(clientSocket,clientSocket2,packet,code,countConnect);
                    sendNumberPort(countConnect,address,port);
                    System.out.println("Trả về số cổng thành công");
                    // Lưu vào map
                    SocketServer.teachers.put(code, theTeacher);
                    // Khởi tạo thread
                    new Thread(theTeacher).start();
                } else {

                }
            }
        } catch (IOException e) {
            System.err.println("Lỗi server: " + e.getMessage());
        }
    }


}


class ServerProcess implements Runnable {
    public static int numberOfProcess;
    private final DatagramSocket theSocket;
    private final DatagramSocket theSocket2;
    public InetAddress addressTeacher;
    public int portTeacher;
    private final String code;
    Map<Integer, Integer> portStudents = new HashMap<>();
    Map<Integer, InetAddress> addressStudents = new HashMap<>();
    Map<String, byte[]> buffer = new HashMap<>();
    Map<Integer, Boolean> disconnect = new HashMap<>();

    public ServerProcess(DatagramSocket theSocket,DatagramSocket theSocket2,DatagramPacket thePacket, String code, int numberOfProcess) throws IOException {
        this.theSocket = theSocket;
        this.theSocket2 = theSocket2;
        this.portTeacher = thePacket.getPort();
        this.addressTeacher = thePacket.getAddress();
        this.code = code;
        this.numberOfProcess = numberOfProcess;
    }
    public void addStudent(int numerOfStudent, DatagramPacket thPacket) {
        portStudents.put(numerOfStudent, thPacket.getPort());
        addressStudents.put(numerOfStudent, addressTeacher);
    }
    public void sendBytesArray(byte[] messageBytes,InetAddress address, int port) throws IOException {
        DatagramPacket packet = new DatagramPacket(messageBytes, messageBytes.length, address, port);
        theSocket2.send(packet);
    }
    public void getBytesArray(byte[] messageBytes) throws IOException {
        DatagramPacket packet = new DatagramPacket(messageBytes,messageBytes.length);
        theSocket.receive(packet);
    }

    public void run() {

        try {

            while(true) {
                byte[] message = new byte[SocketServer.maxDatagramPacketLength];
                getBytesArray(message);
                int LORI = (message[0] & 0xff);

                if (LORI == 0) {
                    // Nhận được packet là LENGTH
                    int ID_CLIENT = (message[1] & 0xff);
                    if (disconnect.containsKey(ID_CLIENT)) {
                        continue;
                    }
                    int ID_IMAGE = (message[2] & 0xff);
                    int LENGTH_IMAGE = (message[3] & 0xff) << 16 | (message[4] & 0xff) << 8 | (message[5] & 0xff);
                    int NUMBEROFPACKET = message[6] & 0xff;
                    byte[] imageBytes = new byte[LENGTH_IMAGE];
                    String Key = ID_IMAGE + ":" + ID_CLIENT;
                    if (buffer.containsKey(Key)) {
                        buffer.remove(Key);
                    }
                    buffer.put(Key, imageBytes);

                } else if (LORI == 1){
                    int ID_CLIENT = (message[1] & 0xff);
                    if (disconnect.containsKey(ID_CLIENT)) {
                        continue;
                    }
                    int ID_Packet = (message[2] & 0xff);
                    int sequenceNumber = (message[3] & 0xff);
                    boolean isLastPacket = ((message[4] & 0xff) == 1);
                    int destinationIndex = (sequenceNumber - 1) * (SocketServer.maxDatagramPacketLength - 5);
                    String Key = ID_Packet + ":" + ID_CLIENT;
                    //System.out.println(sequenceNumber + ", " + isLastPacket + ", " + idPacket + ", " + destinationIndex);
                    if (buffer.containsKey(Key)) {
                        int LENGTH_IMAGE = buffer.get(Key).length;
                        if (destinationIndex >= 0 && destinationIndex < LENGTH_IMAGE) {
                            if (!isLastPacket) {
                                System.arraycopy(message, 5, buffer.get(Key), destinationIndex, SocketServer.maxDatagramPacketLength - 5);
                            } else {
                                System.arraycopy(message, 5, buffer.get(Key), destinationIndex, LENGTH_IMAGE % (SocketServer.maxDatagramPacketLength - 5));
                                sendForTeacher(buffer.get(Key), ID_CLIENT, ID_Packet);
                            }
                            //System.out.println("Nhận thành công packet thứ " + sequenceNumber);
                        } else {
                            System.out.println("Chỉ số đích không hợp lệ: " + destinationIndex);
                        }
                    } else {
                        System.out.println("Lỗi ID_Paket bị xóa khỏi buffer!");
                    }
                } else if (LORI == 2) {
                    int ID_CLIENT = (message[1] & 0xff);
                    int port = portStudents.get(ID_CLIENT) - 1000;
                    InetAddress address = addressStudents.get(ID_CLIENT);
                    //System.out.println("bạn " + ID_CLIENT + " với " + address.toString() + ", " + port + "muốn camera");
                    sendForStudent("camera".getBytes(),address,port);
                } else if (LORI == 3) {
                    System.out.println("send exit to student");
                    for (int key : portStudents.keySet()) {
                        System.out.println(addressStudents.get(key) + ", " + portStudents.get(key));
                        sendExitLiveForStudent("exit".getBytes(), addressStudents.get(key), portStudents.get(key) - 1000);
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
                    buffer.clear(); // Nếu bạn sử dụng một buffer để lưu trữ dữ liệu
                    portStudents.clear(); // Xóa danh sách cổng của học sinh
                    addressStudents.clear(); // Xóa danh sách địa chỉ của học sinh
                    SocketServer.teachers.remove(code);
                } else if (LORI == 4) {
                    System.out.println("send exit to teacher");
                    int ID_CLIENT = (message[1] & 0xff);
                    byte[] numberBytes = new byte[2];
                    numberBytes[0] = (byte) (4);
                    numberBytes[1] = (byte) (ID_CLIENT);
                    System.out.println("ID học sinh" + ID_CLIENT);
                    disconnect.put(ID_CLIENT,true);
                    portStudents.remove(ID_CLIENT);
                    addressStudents.remove(ID_CLIENT);
                    System.out.println("remove address và port thành công");
                    Iterator<String> iterator = buffer.keySet().iterator();
                    while (iterator.hasNext()) {
                        String key = iterator.next();
                        String ID = key.substring(key.indexOf(":") + 1);

                        System.out.println(ID + ", " + ID_CLIENT);

                        if (Integer.parseInt(ID) == ID_CLIENT) {
                            iterator.remove(); // Xóa an toàn
                        }
                    }
                    System.out.println("remove từ buffer");
                    sendExitLiveForTeacher(numberBytes,addressTeacher, portTeacher);
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
    public synchronized void sendExitLiveForStudent(byte[] imageByteArray,InetAddress address, int port) throws IOException {
        DatagramPacket packet = new DatagramPacket(imageByteArray, imageByteArray.length, address, port);
        theSocket2.send(packet);
    }
    public synchronized void sendExitLiveForTeacher(byte[] imageByteArray,InetAddress address, int port) throws IOException {
        DatagramPacket packet = new DatagramPacket(imageByteArray, imageByteArray.length, address, port);
        theSocket2.send(packet);
    }
    public synchronized void sendForStudent(byte[] imageByteArray,InetAddress address,int port) throws IOException {
        DatagramPacket packet = new DatagramPacket(imageByteArray, imageByteArray.length, address, port);
        theSocket2.send(packet);
    }
    public synchronized void sendForTeacher(byte[] imageByteArray, int ID_CLIENT,int ID_IMAGE) throws Exception{
        //System.out.println("Bắt đầu gửi ảnh");
        System.out.println(addressTeacher.getHostAddress().toString() + ", " + portTeacher);
        int sequenceNumber = 0; // For order
        boolean flag; // To see if we got to the end of the file
        int length = imageByteArray.length;
        byte[] lengthBytes = new byte[7];
        // Lưu độ dài vào mảng byte
        lengthBytes[0] = (byte) 0;
        lengthBytes[1] = (byte) (ID_CLIENT);
        lengthBytes[2] = (byte) (ID_IMAGE);
        lengthBytes[3] = (byte) (length >> 16);
        lengthBytes[4] = (byte) (length >> 8);
        lengthBytes[5] = (byte) (length);
        lengthBytes[6] = (byte) ((length + SocketServer.maxDatagramPacketLength - 6)/(SocketServer.maxDatagramPacketLength - 5));
        sendBytesArray(lengthBytes,addressTeacher,portTeacher);
        //System.out.println("Sent ảnh của học sinh " + ID_CLIENT);
        for (int i = 0; i < imageByteArray.length; i = i + SocketServer.maxDatagramPacketLength - 5) {
            sequenceNumber += 1;
            // Create message
            byte[] message = new byte[SocketServer.maxDatagramPacketLength];
            message[0] = (byte)(1);
            message[1] = (byte)(ID_CLIENT);
            message[2] = (byte)(ID_IMAGE);
            message[3] = (byte) (sequenceNumber);
            if ((i + SocketServer.maxDatagramPacketLength-5) >= imageByteArray.length) {
                flag = true;
                message[4] = (byte) (1);
            } else {
                flag = false;
                message[4] = (byte) (0);
            }
            if (!flag) {
                System.arraycopy(imageByteArray, i, message, 5, SocketServer.maxDatagramPacketLength - 5);
            } else { // If it is the last datagram
                System.arraycopy(imageByteArray, i, message, 5, imageByteArray.length - i);
            }

            //System.out.println(addressTeacher.toString() + ":" + portTeacher);
            sendBytesArray(message,addressTeacher,portTeacher);
            Thread.sleep(0,1000);
            //System.out.println("Gửi thành công packet thứ :" + sequenceNumber);

        }
        ++SocketServer.imageCount;
        System.out.println("Gửi thành công ảnh thứ: " + SocketServer.imageCount);

    }


}

