package com.server.liveowl.socket;

import com.server.liveowl.util.UdpHandler;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import static com.server.liveowl.socket.SocketServer.maxDatagramPacketLength;

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

    public ProcessGetData(DatagramSocket theSocket, DatagramSocket theSocket2, DatagramPacket thePacket, String code, int numberOfProcess) throws IOException {
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
                    int clientId = (message[1] & 0xff);
                    if (disconnect.containsKey(clientId)) {
                        continue;
                    }
                    int imageId = (message[2] & 0xff);
                    int lengthOfImage = (message[3] & 0xff) << 16 | (message[4] & 0xff) << 8 | (message[5] & 0xff);
                    int numberOfPacket = message[6] & 0xff;
                    byte[] imageBytes = new byte[lengthOfImage];
                    String Key = imageId + ":" + clientId;
                    SocketServer.imageBuffer.put(Key, imageBytes);
                    //SocketServer.numberBuffer.put(Key,numberOfPacket);

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
                    if (SocketServer.imageBuffer.containsKey(Key)) {
                        int lengthOfImage = SocketServer.imageBuffer.get(Key).length;
                        byte[] imageBytes = SocketServer.imageBuffer.get(Key);
                        //SocketServer.numberBuffer.put(Key, SocketServer.numberBuffer.get(Key) - 1) ;
                        if (destinationIndex >= 0 && destinationIndex < lengthOfImage) {
                            if (!isLastPacket && (destinationIndex + (maxDatagramPacketLength - 5) < lengthOfImage)) {
                                System.arraycopy(message, 5, imageBytes, destinationIndex, maxDatagramPacketLength - 5);
                            } else {
                                System.arraycopy(message, 5, imageBytes, destinationIndex, lengthOfImage % (maxDatagramPacketLength - 5));
                            }
//                            if (SocketServer.numberBuffer.get(Key) == 0) {
//                                SocketServer.listIds.add(Key);
//                            }
                            if (isLastPacket) {
                                SocketServer.listIds.add(Key);
                            }
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
                        UdpHandler.sendRequests(theSocket2, "camera".getBytes(), address, port);
                    } catch (IOException e) {
                        System.out.println(e.getMessage());
                    }
                } else if (packetType == 3) {
                    System.out.println("send exit to student");
                    for (int key : portStudents.keySet()) {
                        System.out.println(addressStudents.get(key) + ", " + portStudents.get(key));
                        UdpHandler.sendRequests(theSocket2,"exit".getBytes(), addressStudents.get(key), portStudents.get(key) - 1000);
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
                    SocketServer.imageBuffer.clear();
                    portStudents.clear();
                    addressStudents.clear();
                    SocketServer.listMeeting.remove(code);
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
                    Iterator<String> iterator = SocketServer.imageBuffer.keySet().iterator();
                    while (iterator.hasNext()) {
                        String key = iterator.next();
                        String ID = key.substring(key.indexOf(":") + 1);
                        if (Integer.parseInt(ID) == clientId) {
                            iterator.remove();
                        }
                    }
                    System.out.println("remove từ buffer");
                    UdpHandler.sendRequests(theSocket2,numberBytes,addressTeacher, portTeacher);
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
//    public synchronized void sendRequestExitForStudent(byte[] imageByteArray,InetAddress address, int port) throws IOException {
//        DatagramPacket packet = new DatagramPacket(imageByteArray, imageByteArray.length, address, port);
//        theSocket2.send(packet);
//    }
//    public synchronized void sendRequestExitForTeacher(byte[] imageByteArray,InetAddress address, int port) throws IOException {
//        DatagramPacket packet = new DatagramPacket(imageByteArray, imageByteArray.length, address, port);
//        theSocket2.send(packet);
//    }
//    public synchronized void sendRequestCameraForStudent(byte[] imageByteArray,InetAddress address,int port) throws IOException {
//        DatagramPacket packet = new DatagramPacket(imageByteArray, imageByteArray.length, address, port);
//        theSocket2.send(packet);
//    }
}


