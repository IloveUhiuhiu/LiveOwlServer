package com.server.liveowl.socket;

import com.server.liveowl.util.UdpHandler;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

import static com.server.liveowl.socket.SocketServer.maxDatagramPacketLength;

class ProcessSendData implements Runnable {
    DatagramSocket socket;
    public InetAddress addressTeacher;
    public int portTeacher;
    ProcessSendData(DatagramPacket packet,int countConnection) throws SocketException {
        socket = new DatagramSocket(9500 + countConnection);
        addressTeacher = packet.getAddress();
        portTeacher = packet.getPort();
    }
    @Override
    public void run() {
        while(true) {
            try {
                String packetId = SocketServer.listIds.poll();
                if (packetId != null) {
                    byte[] imageByteArray = SocketServer.imageBuffer.get(packetId);
                    int pos = packetId.lastIndexOf(":");
                    int imageId = Integer.parseInt(packetId.substring(0, pos));
                    String clientId = packetId.substring(pos + 1);
                    int sequenceNumber = 0;
                    boolean flag;
                    int length = imageByteArray.length;
                    byte[] lengthBytes = new byte[maxDatagramPacketLength];
                    lengthBytes[0] = (byte) 0;
                    System.arraycopy(clientId.getBytes(), 0, lengthBytes, 1, 8);
                    lengthBytes[9] = (byte) (imageId);
                    lengthBytes[10] = (byte) (length >> 16);
                    lengthBytes[11] = (byte) (length >> 8);
                    lengthBytes[12] = (byte) (length);
                    UdpHandler.sendBytesArray(socket, lengthBytes, addressTeacher, portTeacher);
                    for (int i = 0; i < length; i = i + maxDatagramPacketLength - 12) {
                        sequenceNumber += 1;
                        byte[] message = new byte[maxDatagramPacketLength];
                        message[0] = (byte) (1);
                        System.arraycopy(clientId.getBytes(), 0, message, 1, 8);

                        message[9] = (byte) (imageId);
                        message[10] = (byte) (sequenceNumber);
                        if ((i + maxDatagramPacketLength - 12) >= imageByteArray.length) {
                            flag = true;
                            message[11] = (byte) (1);
                        } else {
                            flag = false;
                            message[11] = (byte) (0);
                        }

                        if (!flag) {
                            System.arraycopy(imageByteArray, i, message, 12, maxDatagramPacketLength - 12);
                        } else {
                            System.arraycopy(imageByteArray, i, message, 12, length - i);
                        }
                        UdpHandler.sendBytesArray(socket, message, addressTeacher, portTeacher);
                    }
                    ++SocketServer.imageCount;
                    System.out.println("Gửi thành công ảnh thứ: " + SocketServer.imageCount + "length = " + length);
                }
            } catch (Exception e) {
                System.out.println("Loi khi gui anh:" + e.getMessage());
            }
        }
    }
}