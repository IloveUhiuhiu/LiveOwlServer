package com.server.liveowl.socket;
import java.io.*;
import java.net.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;
import java.util.logging.Logger;

public class SocketServer implements Runnable {
    private static final int PORT_SERVER = 9000;
    private static DatagramSocket serverSocket = null;
    public static Map<String, Vector<StudentHandler>> students = new HashMap<>();
    public static Map<String, TeacherHandler> teachers = new HashMap<>();
    public static int LENGTH = 32768;
    private final static Logger audit = Logger.getLogger("requests");
    private final static Logger errors = Logger.getLogger("errors");
    public void sendBytesArray(byte[] messageBytes,InetAddress address, int port) throws IOException {
        DatagramPacket packet = new DatagramPacket(messageBytes, messageBytes.length, address, port);
        serverSocket.send(packet);
    }
    public DatagramPacket getDatagramPacket() {
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
        DatagramPacket receivePacket = getDatagramPacket();
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
            serverSocket = new DatagramSocket(PORT_SERVER);
            System.out.println("Server đang lắng nghe ...");
            int countConnect = 0;
            int countStudents = 0;
            while (true) {
                DatagramPacket packet = getDatagramPacket();
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
                    SocketServer.teachers.get(code).clients.put(countStudents, address.getHostAddress().toString() + ":" + port);
                    ++countConnect;
                    DatagramSocket clientSocket = new DatagramSocket(PORT_SERVER + countConnect);
                    StudentHandler theStudent = new StudentHandler(clientSocket,packet,code,countStudents);
                    countStudents++;

                    sendNumberPort(countConnect,address,port);
                    System.out.println("Trả về số cổng thành công");

                    SocketServer.students.computeIfAbsent(code, k -> new Vector<>()).add(theStudent);
                    // Khởi tạo thread
                    new Thread(theStudent).start();


                } else if (role.equals("teacher")) {
                    // Lấy mã cuộc họp
                    String code = receiveMsg();
                    System.out.println("Teach tạo mã: " + code);
                    ++countConnect;
                    DatagramSocket clientSocket = new DatagramSocket(PORT_SERVER + countConnect);
                    DatagramSocket clientSocket2 = new DatagramSocket(PORT_SERVER + 50 + countConnect);
                    TeacherHandler theTeacher = new TeacherHandler(clientSocket,clientSocket2,packet,code);
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

class StudentHandler implements Runnable {
    private final DatagramSocket theSocket;
    public DatagramPacket thePacket;
    private final String code;
    private TeacherHandler theTeacher;
    Map<String, byte[]> buffer = new HashMap<>();

    private int ID;
    public StudentHandler(DatagramSocket theSocket, DatagramPacket thePacket, String code, int ID) throws IOException {
        this.theSocket = theSocket;
        this.thePacket = thePacket;
        this.code = code;
        this.ID = ID;
        theTeacher = SocketServer.teachers.get(code);
    }
    public void sendBytesArray(byte[] messageBytes,InetAddress address, int port) throws IOException {
        DatagramPacket packet = new DatagramPacket(messageBytes, messageBytes.length, address, port);
        theSocket.send(packet);
    }
    public DatagramPacket getDatagramPacket() {
        byte[] message = new byte[1024];
        DatagramPacket datagramPacket = new DatagramPacket(message, message.length);
        try {
            theSocket.receive(datagramPacket);
        } catch (Exception e) {
            System.err.println("Lỗi trong khi lấy DatagramPacket" + e.getMessage());
        }
        return datagramPacket;
    }
    public String receiveMsg() {
        return new String(getDatagramPacket().getData());
    }
    public void sendMsg(String message, InetAddress address, int port) throws IOException {
        sendBytesArray(message.getBytes(),address,port);
    }
    public int getBytesArray(byte[] messageBytes) throws IOException {
        DatagramPacket packet = new DatagramPacket(messageBytes,messageBytes.length);
        theSocket.receive(packet);
        return packet.getLength();
    }
    @Override
    public void run() {

        try {
            InetAddress address = theSocket.getInetAddress();
            int port = theSocket.getPort();
            while(true) {
                byte[] message = new byte[SocketServer.LENGTH];
                getBytesArray(message);
                int LORI = (message[0] & 0xff);

                if (LORI == 0) {
                    // Nhận được packet là LENGTH
                    int ID_IMAGE = (message[1] & 0xff);
                    int LENGTH_IMAGE =  (message[2] & 0xff) << 16 | (message[3] & 0xff) << 8 | (message[4] & 0xff);
                    int NUMBEROFPACKET = message[5] & 0xff;
                    byte[] imageBytes = new byte[LENGTH_IMAGE];

                    String Key = ID_IMAGE + ":" + ID;
                    if (buffer.containsKey(Key)) {
                        buffer.remove(Key);
                    }
                    buffer.put(Key, imageBytes);

                } else {
                    int ID_Packet = (message[1] & 0xff);
                    int sequenceNumber = (message[2] & 0xff);
                    boolean isLastPacket = ((message[3] & 0xff) == 1);
                    int destinationIndex = (sequenceNumber - 1) * (SocketServer.LENGTH-4);
                    String Key = ID_Packet + ":" + ID;
                    //System.out.println(sequenceNumber + ", " + isLastPacket + ", " + idPacket + ", " + destinationIndex);
                    if (buffer.containsKey(Key)) {
                        int LENGTH_IMAGE = buffer.get(Key).length;
                        if (destinationIndex >= 0 && destinationIndex < LENGTH_IMAGE) {
                            if (!isLastPacket) {
                                System.arraycopy(message, 4, buffer.get(Key), destinationIndex, SocketServer.LENGTH-4);
                            } else {
                                System.arraycopy(message, 4, buffer.get(Key), destinationIndex, LENGTH_IMAGE % (SocketServer.LENGTH-4));
                                theTeacher.sendForTeacher(buffer.get(Key),ID,ID_Packet);
                            }
                            //System.out.println("Nhận thành công packet thứ " + sequenceNumber);
                        } else {
                            System.out.println("Chỉ số đích không hợp lệ: " + destinationIndex);
                        }
                    } else {
                        System.out.println("Lỗi ID_Paket bị xóa khỏi buffer!");
                    }
                }

            }

        } catch (Exception e) {
            System.err.println(e.getMessage());
        } finally {
            try {
                theSocket.close();
            } catch (Exception e) {

            }
        }
    }
}

class TeacherHandler implements Runnable {
    private final DatagramSocket theSocket;
    private final DatagramSocket theSocket2;
    public DatagramPacket thePacket;
    private final String code;
    Map<Integer, String> clients = new HashMap<>();
    public TeacherHandler(DatagramSocket theSocket,DatagramSocket theSocket2,DatagramPacket thePacket, String code) throws IOException {
        this.theSocket = theSocket;
        this.theSocket2 = theSocket2;
        this.thePacket = thePacket;
        this.code = code;
    }
    public void sendBytesArray(byte[] messageBytes,InetAddress address, int port) throws IOException {
        DatagramPacket packet = new DatagramPacket(messageBytes, messageBytes.length, address, port);
        theSocket.send(packet);
    }
    public DatagramPacket getDatagramPacket() {
        byte[] message = new byte[1024];
        DatagramPacket datagramPacket = new DatagramPacket(message, message.length);
        try {
            theSocket2.receive(datagramPacket);
        } catch (Exception e) {
            System.err.println("Lỗi trong khi lấy DatagramPacket" + e.getMessage());
        }
        return datagramPacket;
    }
    public String receiveMsg() {
        return new String(getDatagramPacket().getData());
    }
    public void sendMsg(String message, InetAddress address, int port) throws IOException {
        sendBytesArray(message.getBytes(),address,port);
    }
    public int getBytesArray(byte[] messageBytes) throws IOException {
        DatagramPacket packet = new DatagramPacket(messageBytes,messageBytes.length);
        theSocket2.receive(packet);
        return packet.getLength();
    }

    public void run() {
        try {

            while (true) {
                byte[] numberBytes = new byte[1];
                DatagramPacket packetNumbers = new DatagramPacket(numberBytes, numberBytes.length);
                theSocket2.receive(packetNumbers);
                int number = (numberBytes[0] & 0xff);
                byte[] messageBytes = new byte[1024];
                DatagramPacket packet = new DatagramPacket(messageBytes, messageBytes.length);
                theSocket2.receive(packet);
                String message = new String(packet.getData(),0,packet.getLength());
                String strAddress = clients.get(number);
                System.out.println("bạn " + strAddress + "muốn gì: " + message);
                int pos = clients.get(number).indexOf(':');
                String preAddress = strAddress.substring(0, pos);
                String nextAddress = strAddress.substring(pos+1);
                InetAddress address = InetAddress.getByName(preAddress);
                int port = Integer.parseInt(nextAddress)-1000;
                byte[] message2 = message.getBytes();
                System.out.println("Gửi yeeu cau camera đến : " + address.toString() + ":" + port);
                DatagramPacket packet2 = new DatagramPacket(message2, message2.length, address, port);
                theSocket2.send(packet2);

            }
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());

        }

    }
    public synchronized void sendForTeacher(byte[] imageByteArray, int ID_CLIENT,int ID_IMAGE) throws Exception{
        //System.out.println("Bắt đầu gửi ảnh");
        int sequenceNumber = 0; // For order
        boolean flag; // To see if we got to the end of the file
        InetAddress address = thePacket.getAddress();
        int port = thePacket.getPort();
        int length = imageByteArray.length;
        byte[] lengthBytes = new byte[7];
        // Lưu độ dài vào mảng byte
        lengthBytes[0] = (byte) 0;
        lengthBytes[1] = (byte) (ID_CLIENT);
        lengthBytes[2] = (byte) (ID_IMAGE);
        lengthBytes[3] = (byte) (length >> 16);
        lengthBytes[4] = (byte) (length >> 8);
        lengthBytes[5] = (byte) (length);
        lengthBytes[6] = (byte) ((length + SocketServer.LENGTH - 6)/(SocketServer.LENGTH - 5));

        sendBytesArray(lengthBytes,address,port);
        System.out.println("Sent ảnh của học sinh " + ID_CLIENT);
        for (int i = 0; i < imageByteArray.length; i = i + SocketServer.LENGTH - 5) {
            sequenceNumber += 1;
            // Create message
            byte[] message = new byte[SocketServer.LENGTH];

            message[0] = (byte)(1);
            message[1] = (byte)(ID_CLIENT);
            message[2] = (byte)(ID_IMAGE);
            message[3] = (byte) (sequenceNumber);


            if ((i + SocketServer.LENGTH-5) >= imageByteArray.length) {
                flag = true;
                message[4] = (byte) (1);
            } else {
                flag = false;
                message[4] = (byte) (0);
            }
            if (!flag) {
                System.arraycopy(imageByteArray, i, message, 5, SocketServer.LENGTH - 5);
            } else { // If it is the last datagram
                System.arraycopy(imageByteArray, i, message, 5, imageByteArray.length - i);
            }

            System.out.println(address.toString() + ":" + port);
            sendBytesArray(message,address,port);
            Thread.sleep(1);
            //System.out.println("Gửi thành công packet thứ :" + sequenceNumber);

        }
        System.out.println("Gửi thành công ảnh thứ: " + ID_IMAGE);

    }


}

