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

    public static int ID = 0;
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
    public String getMessage() {
        DatagramPacket receivePacket = getDatagramPacket();
        return new String(receivePacket.getData(),0,receivePacket.getLength());
    }
    public void sendMessage(String message, InetAddress address, int port) throws IOException {
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
                    if (!SocketServer.teachers.containsKey(code)) {
                        System.out.println("Student gửi mã không có trong map");
                        sendMessage("fail",address,port);
                        continue;
                    } else {
                        sendMessage("success",address,port);
                    }
                    ++countConnect;
                    DatagramSocket clientSocket = new DatagramSocket(PORT_SERVER + countConnect);
                    StudentHandler theStudent = new StudentHandler(clientSocket,packet,code);

                    sendNumberPort(countConnect,address,port);
                    System.out.println("Trả về số cổng thành công");

                    SocketServer.students.computeIfAbsent(code, k -> new Vector<>()).add(theStudent);
                    // Khởi tạo thread
                    new Thread(theStudent).start();


                } else if (role.equals("teacher")) {
                    // Lấy mã cuộc họp
                    String code = getMessage();
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
//long
class StudentHandler implements Runnable {
    private final DatagramSocket theSocket;
    public DatagramPacket thePacket;
    private final String code;

    public StudentHandler(DatagramSocket theSocket, DatagramPacket thePacket, String code) throws IOException {
        this.theSocket = theSocket;
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
            theSocket.receive(datagramPacket);
        } catch (Exception e) {
            System.err.println("Lỗi trong khi lấy DatagramPacket" + e.getMessage());
        }
        return datagramPacket;
    }
    public String getMessage() {
        return new String(getDatagramPacket().getData());
    }
    public void sendMessage(String message, InetAddress address, int port) throws IOException {
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
            while (true) {
                System.out.println("Bắt đầu nhận ảnh từ student");

                int sequenceNumber = 0; // Order of sequences

                byte[] numberBytes = new byte[4];
                getBytesArray(numberBytes);

                int length = (numberBytes[1] & 0xff) << 16 | (numberBytes[2] & 0xff) << 8 | (numberBytes[3] & 0xff);
                int id = numberBytes[0] & 0xff;
                System.out.println("Nhận ảnh có độ dài là: " + length + ", id: " + id);
                byte imageBytes[] = new byte[length];

                while (true) {
                    byte[] message = new byte[SocketServer.LENGTH];
                    int lengthOfPacket = getBytesArray(message);

                    sequenceNumber = ((message[0] & 0xff) << 8) + (message[1] & 0xff);
                    boolean isLastPacket = ((message[2] & 0xff) == 1);
                    int idPacket = (message[3] & 0xff);

                    int destinationIndex = (sequenceNumber - 1) * (SocketServer.LENGTH-4);
                    System.out.println(sequenceNumber + ", " + isLastPacket + ", " + idPacket + ", " + destinationIndex);
                    if (id == idPacket) {
                        if (destinationIndex >= 0 && destinationIndex < imageBytes.length) {
                            if (!isLastPacket) {
                                System.arraycopy(message, 4, imageBytes, destinationIndex, SocketServer.LENGTH-4);
                            } else {
                                System.arraycopy(message, 4, imageBytes, destinationIndex, length % (SocketServer.LENGTH-4));
                            }
                            System.out.println("Nhận thành công packet thứ " + sequenceNumber);
                        } else {
                            System.out.println("Chỉ số đích không hợp lệ: " + destinationIndex);
                        }
                    } else {
                        System.out.println("Lỗi id : " + id + ", " + idPacket);
                        continue;
                    }

                    if (isLastPacket) {
                        System.out.println("Break rùi nha");
                        break;
                    }
                }
                System.out.println("Nhận thành công 1 ảnh!");
                System.out.println("Size ảnh vừa nhận là : " + imageBytes.length);
                sendForTeacher(imageBytes);
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
    private void sendForTeacher(byte[] imageBytes) {
        TeacherHandler theTeacher = SocketServer.teachers.get(code);
        theTeacher.imageBytes = imageBytes;
        theTeacher.Id = thePacket.getAddress().getHostAddress().toString() + ":" + thePacket.getPort();
        System.out.println(theTeacher.Id);
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
    private final DatagramSocket theSocket2;
    public DatagramPacket thePacket;
    private final String code;
    public byte[] imageBytes = null;
    private Map<String, Integer> clients= new HashMap<>();
    private Map<Integer,String> getClients = new HashMap<>();
    public String Id = null;

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
    public String getMessage() {
        return new String(getDatagramPacket().getData());
    }
    public void sendMessage(String message, InetAddress address, int port) throws IOException {
        sendBytesArray(message.getBytes(),address,port);
    }
    public int getBytesArray(byte[] messageBytes) throws IOException {
        DatagramPacket packet = new DatagramPacket(messageBytes,messageBytes.length);
        theSocket2.receive(packet);
        return packet.getLength();
    }

    public void run() {
        try {
            Thread thread = new Thread(() -> {
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
                        String strAddress = getClients.get(number);
                        System.out.println("bạn " + strAddress + "muốn gì: " + message);
                        int pos = getClients.get(number).indexOf(':');
                        String preAddress = strAddress.substring(0, pos);
                        String nextAddress = strAddress.substring(pos+1);
                        InetAddress address = InetAddress.getByName(preAddress);
                        int port = Integer.parseInt(nextAddress)-1000;
                        byte[] message2 = new byte[1024];
                        message2 = message.getBytes();
                        System.out.println("Gửi yeeu cau camera đến : " + address.toString() + ":" + port);
                        DatagramPacket packet2 = new DatagramPacket(message2, message2.length, address, port);
                        theSocket2.send(packet2);
                    }
                } catch (Exception e) {
                    System.err.println("Lỗi khi nhận ở thread2: " + e.getMessage());
                }
            });

            thread.start();
            try {
                while (true) {
                    if (Id !=null && imageBytes != null && imageBytes.length > 0) {
                        sendPacketImage(imageBytes);
                        Id = null;
                        imageBytes = null;
                    }

                }
            } catch (Exception e) {
                System.err.println("Lỗi khi gửi ở thread: " + e.getMessage());
            }
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());

        }

    }
    public synchronized void sendPacketImage(byte[] imageByteArray) throws Exception{
        System.out.println("Bắt đầu gửi ảnh");
        int sequenceNumber = 0; // For order
        boolean flag; // To see if we got to the end of the file
        InetAddress address = thePacket.getAddress();
        int port = thePacket.getPort();
        if (!clients.containsKey(Id)) {
            int numberOfConnections = clients.size();
            clients.put(Id,numberOfConnections);
            getClients.put(numberOfConnections,Id);
            System.out.println(Id + ": " + numberOfConnections);
        }
        int number = clients.get(Id);

        int length = imageByteArray.length;
        byte[] lengthBytes = new byte[5];
        // Lưu độ dài vào mảng byte
        lengthBytes[0] = (byte) number;
        lengthBytes[1] = (byte) (SocketServer.ID);
        lengthBytes[2] = (byte) (length >> 16);
        lengthBytes[3] = (byte) (length >> 8);
        lengthBytes[4] = (byte) (length);

        sendBytesArray(lengthBytes,address,port);
        System.out.println("Sent ảnh của học sinh " + number + " có Id =  " + Id);
        for (int i = 0; i < imageByteArray.length; i = i + SocketServer.LENGTH - 4) {
            sequenceNumber += 1;
            // Create message
            byte[] message = new byte[SocketServer.LENGTH];
            message[0] = (byte) (sequenceNumber >> 8);
            message[1] = (byte) (sequenceNumber);

            if ((i + SocketServer.LENGTH-4) >= imageByteArray.length) {
                flag = true;
                message[2] = (byte) (1);
            } else {
                flag = false;
                message[2] = (byte) (0);
            }
            message[3] = (byte) (SocketServer.ID);
            if (!flag) {
                System.arraycopy(imageByteArray, i, message, 4, SocketServer.LENGTH - 4);
            } else { // If it is the last datagram
                System.arraycopy(imageByteArray, i, message, 4, imageByteArray.length - i);
            }

            System.out.println(address.toString() + ":" + port);
            sendBytesArray(message,address,port);
            Thread.sleep(100);
            System.out.println("Gửi thành công packet thứ :" + sequenceNumber);

        }
        SocketServer.ID+=1;
        SocketServer.ID%=10;
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
