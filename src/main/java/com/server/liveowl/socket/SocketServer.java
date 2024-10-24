package com.server.liveowl.socket;

import jakarta.persistence.criteria.CriteriaBuilder;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.*;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.Vector;
import java.util.logging.Logger;

public class SocketServer implements Runnable {
    private static final int PORT_SERVER = 9000;
    private static DatagramSocket serverSocket = null;
    public static Map<String, Vector<StudentHandler>> students = new HashMap<>();
    public static Map<String, TeacherHandler> teachers = new HashMap<>();

    private final static Logger audit = Logger.getLogger("requests");
    private final static Logger errors = Logger.getLogger("errors");

    public void run() {

        try {
            serverSocket = new DatagramSocket(PORT_SERVER);
            System.out.println("Server đang lắng nghe ....");
            int countConnect = 0;
            while (true) {
                DatagramPacket packet = getDatagramPacket();
                InetAddress address = packet.getAddress();
                int port = packet.getPort();
                System.out.println(address.toString() + " kết nối!");

                String role = getMessage();
                if (role.equals("student")) {
                    // Lấy mã
                    String code = getMessage();
                    System.out.println("Student gửi mã: " + code);
                    ++countConnect;
                    DatagramSocket clientSocket = new DatagramSocket(PORT_SERVER + countConnect);
                    StudentHandler theStudent = new StudentHandler(clientSocket,packet,code);
                    byte[] numberPort = new byte[1];
                    numberPort[0] = (byte) countConnect;
                    DatagramPacket requestPacket = new DatagramPacket(numberPort, numberPort.length, address, port);
                    serverSocket.send(requestPacket);
                    System.out.println("Trả về số cổng thành công");
                    SocketServer.students.computeIfAbsent(code, k -> new Vector<>()).add(theStudent);
                    // Khởi tạo thread
                    new Thread(theStudent).start();
                    // Lưu vào map
//                    if (!SocketServer.teachers.containsKey(code)) {
//                        theStudent.sendRequest("fail");
//                    } else {
//                        theStudent.sendRequest("success");
//                        SocketServer.students.computeIfAbsent(code, k -> new Vector<>()).add(theStudent);
//                        // Khởi tạo thread
//                        new Thread(theStudent).start();
//                    }

                } else if (role.equals("teacher")) {
                    // Lấy mã cuộc họp
                    String code = getMessage();
                    System.out.println("Teach tạo mã: " + code);
                    ++countConnect;
                    DatagramSocket clientSocket = new DatagramSocket(PORT_SERVER + countConnect);
                    TeacherHandler theTeacher = new TeacherHandler(clientSocket,packet,code);
                    byte[] numberPort = new byte[1];
                    numberPort[0] = (byte) countConnect;
                    DatagramPacket requestPacket = new DatagramPacket(numberPort, numberPort.length, address, port);
                    serverSocket.send(requestPacket);
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

    private DatagramPacket getDatagramPacket() {
        byte[] message = new byte[1024];
        DatagramPacket datagramPacket = new DatagramPacket(message, message.length);
        try {
            serverSocket.receive(datagramPacket);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return datagramPacket;
    }
    private String getMessage() {
        DatagramPacket datagramPacket = getDatagramPacket();
        return new String(datagramPacket.getData(),0,datagramPacket.getLength());
    }
}
//long
class StudentHandler implements Runnable {
    private final DatagramSocket theSocket;
    public DatagramPacket thePacket;
    private final String code;

    public StudentHandler(DatagramSocket theSocket,DatagramPacket thePacket, String code) throws IOException {
        this.theSocket = theSocket;
        this.thePacket = thePacket;
        this.code = code;
    }
    public void sendRequest(String message) throws IOException {
        byte[] messageBytes = message.getBytes();
        DatagramPacket packet = new DatagramPacket(messageBytes, messageBytes.length, thePacket.getAddress(), thePacket.getPort());
        theSocket.send(packet);
    }
    public String receiveResponse() throws IOException {
        byte[] messageBytes = new byte[1024];
        DatagramPacket packet = new DatagramPacket(messageBytes, messageBytes.length);
        theSocket.receive(packet);
        return new String(packet.getData(),0,packet.getLength());
    }
    public byte[] receiveBytes() throws IOException {
        byte[] messageBytes = new byte[1024];
        DatagramPacket packet = new DatagramPacket(messageBytes, messageBytes.length);
        theSocket.receive(packet);
        return messageBytes;
    }
    @Override
    public void run() {
        try {
            while (true) {
                System.out.println("Bắt đầu nhận ảnh từ student");
                boolean flag; // Have we reached end of file
                int sequenceNumber = 0; // Order of sequences
                int foundLast = 0; // The las sequence found
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                while (true) {
                    byte[] message = new byte[1024];
                    DatagramPacket receivedPacket;
                    receivedPacket = new DatagramPacket(message, message.length);
                    theSocket.receive(receivedPacket);

                    sequenceNumber = ((message[0] & 0xff) << 8) + (message[1] & 0xff);
                    boolean isLastPacket = ((message[2] & 0xff) == 1) && foundLast > 10;

                    if (sequenceNumber == foundLast + 1) {
                        foundLast = sequenceNumber;
                        baos.write(message, 3, receivedPacket.getLength() - 3);
                        sendAck(foundLast);
                        System.out.println("Received: Sequence number: " + foundLast);
                    } else {
                        System.out.println("Expected sequence number: " + (foundLast + 1) + " but received " + sequenceNumber + ". DISCARDING");
                        sendAck(foundLast); // Gửi lại ACK cho gói tin cuối cùng nhận được thành công.
                    }

                    if (isLastPacket) {
                        break;
                    }
                }
                System.out.println("Nhận thành công 1 ảnh!");
//                byte[] imageBytes = baos.toByteArray();
//                sendImageForTeacher(imageBytes);
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

    private void sendAck(int foundLast) throws IOException {
        // send acknowledgement
        byte[] ackPacket = new byte[2];
        ackPacket[0] = (byte) (foundLast >> 8);
        ackPacket[1] = (byte) (foundLast);
        // the datagram packet to be sent
        DatagramPacket acknowledgement = new DatagramPacket(ackPacket, ackPacket.length, thePacket.getAddress(), thePacket.getPort());
        theSocket.send(acknowledgement);
        System.out.println("Sent ack: Sequence Number = " + foundLast);
    }
    private synchronized void sendImageForTeacher(byte[] imageBytes) throws IOException {
        TeacherHandler theTeacher = SocketServer.teachers.get(code);
        theTeacher.sendPacketImage(imageBytes);
    }

}

//Hung
//class StudentHandler implements Runnable {
//    private final DatagramSocket theSocket;
//    public DatagramPacket thePacket;
//    private final String code;
//
//    public StudentHandler(DatagramSocket theSocket, DatagramPacket thePacket, String code) throws IOException {
//        this.theSocket = theSocket;
//        this.thePacket = thePacket;
//        this.code = code;
//    }
//
//    public void sendRequest(String message) throws IOException {
//        byte[] messageBytes = message.getBytes();
//        DatagramPacket packet = new DatagramPacket(messageBytes, messageBytes.length, thePacket.getAddress(), thePacket.getPort());
//        theSocket.send(packet);
//    }
//
//    public String receiveResponse() throws IOException {
//        byte[] messageBytes = new byte[1024];
//        DatagramPacket packet = new DatagramPacket(messageBytes, messageBytes.length);
//        theSocket.receive(packet);
//        return new String(packet.getData(), 0, packet.getLength());
//    }
//
//    public byte[] receiveBytes() throws IOException {
//        byte[] messageBytes = new byte[1024];
//        DatagramPacket packet = new DatagramPacket(messageBytes, messageBytes.length);
//        theSocket.receive(packet);
//        return messageBytes;
//    }
//
//    @Override
//    public void run() {
//        try {
//            while (true) {
//                System.out.println("Bắt đầu nhận ảnh và ký tự từ student");
//
//                // Nhận ký tự từ student
//                String keyboardInput = receiveKeyboardInput();
//                System.out.println("Student gửi ký tự: " + keyboardInput);
//
//                // Gửi ký tự cho teacher
//                sendKeyboardInputToTeacher(keyboardInput);
//
//                // Logic nhận và gửi ảnh
//                receiveAndSendImage();
//            }
//
//        } catch (Exception e) {
//            System.err.println(e.getMessage());
//        } finally {
//            try {
//                theSocket.close();
//            } catch (Exception e) {
//                // Handle exception if needed
//            }
//        }
//    }
//
//    private void receiveAndSendImage() throws IOException {
//        boolean flag;
//        int sequenceNumber = 0;
//        int foundLast = 0;
//        ByteArrayOutputStream baos = new ByteArrayOutputStream();
//        while (true) {
//            byte[] message = new byte[1024];
//            DatagramPacket receivedPacket = new DatagramPacket(message, message.length);
//            theSocket.receive(receivedPacket);
//
//            sequenceNumber = ((message[0] & 0xff) << 8) + (message[1] & 0xff);
//            boolean isLastPacket = ((message[2] & 0xff) == 1) && foundLast > 10;
//
//            if (sequenceNumber == foundLast + 1) {
//                foundLast = sequenceNumber;
//                baos.write(message, 3, receivedPacket.getLength() - 3);
//                sendAck(foundLast);
//                System.out.println("Received: Sequence number: " + foundLast);
//            } else {
//                System.out.println("Expected sequence number: " + (foundLast + 1) + " but received " + sequenceNumber + ". DISCARDING");
//                sendAck(foundLast);
//            }
//
//            if (isLastPacket) {
//                break;
//            }
//        }
//        System.out.println("Nhận thành công 1 ảnh!");
//        byte[] imageBytes = baos.toByteArray();
//        sendImageForTeacher(imageBytes);
//    }
//
//    private String receiveKeyboardInput() throws IOException {
//        byte[] messageBytes = new byte[1024];
//        DatagramPacket packet = new DatagramPacket(messageBytes, messageBytes.length);
//        theSocket.receive(packet);
//        return new String(packet.getData(), 0, packet.getLength());
//    }
//
//    private synchronized void sendKeyboardInputToTeacher(String input) throws IOException {
//        TeacherHandler theTeacher = SocketServer.teachers.get(code);
//        if (theTeacher != null) {
//            theTeacher.sendRequest(input);
//        }
//    }
//
//    private void sendAck(int foundLast) throws IOException {
//        byte[] ackPacket = new byte[2];
//        ackPacket[0] = (byte) (foundLast >> 8);
//        ackPacket[1] = (byte) (foundLast);
//        DatagramPacket acknowledgement = new DatagramPacket(ackPacket, ackPacket.length, thePacket.getAddress(), thePacket.getPort());
//        theSocket.send(acknowledgement);
//        System.out.println("Sent ack: Sequence Number = " + foundLast);
//    }
//
//    private synchronized void sendImageForTeacher(byte[] imageBytes) throws IOException {
//        TeacherHandler theTeacher = SocketServer.teachers.get(code);
//        if (theTeacher != null) {
//            theTeacher.sendPacketImage(imageBytes);
//        }
//    }
//}

//Long
class TeacherHandler implements Runnable {
    private final DatagramSocket theSocket;
    public DatagramPacket thePacket;
    private final String code;

    public TeacherHandler(DatagramSocket theSocket,DatagramPacket thePacket, String code) throws IOException {
        this.theSocket = theSocket;
        this.thePacket = thePacket;
        this.code = code;
    }
    public void sendRequest(String message) throws IOException {
        byte[] messageBytes = message.getBytes();
        DatagramPacket packet = new DatagramPacket(messageBytes, messageBytes.length, thePacket.getAddress(), thePacket.getPort());
        theSocket.send(packet);
    }
    public String receiveResponse() throws IOException {
        byte[] messageBytes = new byte[1024];
        DatagramPacket packet = new DatagramPacket(messageBytes, messageBytes.length);
        theSocket.receive(packet);
        return new String(packet.getData(),0,packet.getLength());
    }
    public DatagramPacket receivePacket() throws IOException {
        byte[] messageBytes = new byte[1024];
        DatagramPacket packet = new DatagramPacket(messageBytes, messageBytes.length);
        theSocket.receive(packet);
        return packet;
    }

    public void run() {

        try {
//            InetAddress address = thePacket.getAddress();
//            int port = thePacket.getPort();
            try {
                while (true) {
                    String request = receiveResponse();
                    System.out.println("Received: " + request);
                    sendRequest(request);
                }
            } catch (Exception e) {
                System.err.println("Error listening required: " + e.getMessage());
            }



        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());

        }

    }

    public void sendPacketImage(byte[] imageByteArray) throws IOException {
        System.out.println("Bắt đầu gửi ảnh");
        int sequenceNumber = 0; // For order
        boolean flag; // To see if we got to the end of the file
        int ackSequence = 0; // To see if the datagram was received correctly

        for (int i = 0; i < imageByteArray.length; i = i + 1021) {
            sequenceNumber += 1;
            // Create message
            byte[] message = new byte[1024]; // First two bytes of the data are for control (datagram integrity and order)
            message[0] = (byte) (sequenceNumber >> 8);
            message[1] = (byte) (sequenceNumber);

            if ((i + 1021) >= imageByteArray.length) { // Have we reached the end of file?
                flag = true;
                message[2] = (byte) (1); // We reached the end of the file (last datagram to be send)
            } else {
                flag = false;
                message[2] = (byte) (0); // We haven't reached the end of the file, still sending datagrams
            }

            if (!flag) {
                System.arraycopy(imageByteArray, i, message, 3, 1021);
            } else { // If it is the last datagram
                System.arraycopy(imageByteArray, i, message, 3, imageByteArray.length - i);
            }
            InetAddress address = thePacket.getAddress();
            int port = thePacket.getPort();

            DatagramPacket sendPacket = new DatagramPacket(message, message.length, address, port); // The data to be sent
            theSocket.send(sendPacket); // Sending the data
            System.out.println("Gửi thành công packet thứ :" + sequenceNumber);

            boolean ackRec; // Was the datagram received?

            while (true) {
                System.out.println("Gửi ack!");
                byte[] ack = new byte[2]; // Create another packet for datagram ackknowledgement
                DatagramPacket ackpack = new DatagramPacket(ack, ack.length);

                try {
                    theSocket.setSoTimeout(500); // Waiting for the server to send the ack
                    theSocket.receive(ackpack);
                    ackSequence = ((ack[0] & 0xff) << 8) + (ack[1] & 0xff); // Figuring the sequence number
                    System.out.println(ackSequence);
                    ackRec = true; // We received the ack
                } catch (SocketTimeoutException e) {
                    System.out.println("Socket timed out waiting for ack");
                    ackRec = false; // We did not receive an ack
                }

                // If the package was received correctly next packet can be sent
                if ((ackSequence == sequenceNumber) && (ackRec)) {
                    System.out.println("Ack received: Sequence Number = " + ackSequence);
                    break;
                } // Package was not received, so we resend it
                else {
                    theSocket.send(sendPacket);
                    System.out.println("Resending: Sequence Number = " + sequenceNumber);
                }
            }
        }
    }

}

//Hung
//class TeacherHandler implements Runnable {
//    private final DatagramSocket theSocket;
//    public DatagramPacket thePacket;
//    private final String code;
//
//    public TeacherHandler(DatagramSocket theSocket, DatagramPacket thePacket, String code) throws IOException {
//        this.theSocket = theSocket;
//        this.thePacket = thePacket;
//        this.code = code;
//    }
//
//    public void sendRequest(String message) throws IOException {
//        byte[] messageBytes = message.getBytes();
//        DatagramPacket packet = new DatagramPacket(messageBytes, messageBytes.length, thePacket.getAddress(), thePacket.getPort());
//        theSocket.send(packet);
//    }
//
//    public String receiveResponse() throws IOException {
//        byte[] messageBytes = new byte[1024];
//        DatagramPacket packet = new DatagramPacket(messageBytes, messageBytes.length);
//        theSocket.receive(packet);
//        return new String(packet.getData(), 0, packet.getLength());
//    }
//
//    public DatagramPacket receivePacket() throws IOException {
//        byte[] messageBytes = new byte[1024];
//        DatagramPacket packet = new DatagramPacket(messageBytes, messageBytes.length);
//        theSocket.receive(packet);
//        return packet;
//    }
//
//    public void run() {
//        try {
//            while (true) {
//                String request = receiveResponse();
//                System.out.println("Received: " + request);
//
//                // Xử lý nếu request là ký tự bàn phím
//                if (request.length() == 1) {
//                    System.out.println("Teacher nhận được ký tự từ student: " + request);
//                } else {
//                    sendRequest(request);
//                }
//            }
//        } catch (Exception e) {
//            System.err.println("Error: " + e.getMessage());
//        }
//    }
//
//    public void sendPacketImage(byte[] imageByteArray) throws IOException {
//        System.out.println("Bắt đầu gửi ảnh");
//        int sequenceNumber = 0;
//        boolean flag;
//        int ackSequence = 0;
//
//        for (int i = 0; i < imageByteArray.length; i += 1021) {
//            sequenceNumber += 1;
//            byte[] message = new byte[1024];
//            message[0] = (byte) (sequenceNumber >> 8);
//            message[1] = (byte) (sequenceNumber);
//
//            if ((i + 1021) >= imageByteArray.length) {
//                flag = true;
//                message[2] = (byte) (1);
//            } else {
//                flag = false;
//                message[2] = (byte) (0);
//            }
//
//            if (!flag) {
//                System.arraycopy(imageByteArray, i, message, 3, 1021);
//            } else {
//                System.arraycopy(imageByteArray, i, message, 3, imageByteArray.length - i);
//            }
//
//            InetAddress address = thePacket.getAddress();
//            int port = thePacket.getPort();
//
//            DatagramPacket sendPacket = new DatagramPacket(message, message.length, address, port);
//            theSocket.send(sendPacket);
//            System.out.println("Gửi thành công packet thứ: " + sequenceNumber);
//
//            boolean ackRec;
//
//            while (true) {
//                byte[] ack = new byte[2];
//                DatagramPacket ackpack = new DatagramPacket(ack, ack.length);
//
//                try {
//                    theSocket.setSoTimeout(500);
//                    theSocket.receive(ackpack);
//                    ackSequence = ((ack[0] & 0xff) << 8) + (ack[1] & 0xff);
//                    ackRec = true;
//                } catch (SocketTimeoutException e) {
//                    ackRec = false;
//                }
//
//                if ((ackSequence == sequenceNumber) && ackRec) {
//                    System.out.println("Ack received: Sequence Number = " + ackSequence);
//                    break;
//                } else {
//                    theSocket.send(sendPacket);
//                    System.out.println("Resending: Sequence Number = " + sequenceNumber);
//                }
//            }
//        }
//    }
//}
